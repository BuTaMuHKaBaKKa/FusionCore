// Local build helper: preserve HTTPS validation upstream; serve Gradle on loopback.
// Used only when Gradle's own HTTPS downloads stall on a machine where curl works.
const http = require('node:http');
const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const {spawn} = require('node:child_process');
const roots = {
  central: 'https://repo.maven.apache.org/maven2/',
  neoforge: 'https://maven.neoforged.net/releases/',
  forge: 'https://maven.minecraftforge.net/',
  fabric: 'https://maven.fabricmc.net/',
  architectury: 'https://maven.architectury.dev/',
  parchment: 'https://maven.parchmentmc.org/',
  sponge: 'https://repo.spongepowered.org/repository/maven-public/',
  sonatype: 'https://oss.sonatype.org/content/repositories/snapshots/',
  spigot: 'https://hub.spigotmc.org/nexus/content/repositories/snapshots/',
  izzel: 'https://maven.izzel.io/releases/',
  jitpack: 'https://jitpack.io/',
  mojang: 'https://libraries.minecraft.net/',
  plugins: 'https://plugins.gradle.org/m2/'
};
const cache = path.join(__dirname, '..', 'build', 'maven-relay-cache');
fs.mkdirSync(cache, {recursive:true});
const pending = new Map();
async function fetchFile(url) {
  const file = path.join(cache, crypto.createHash('sha256').update(url).digest('hex'));
  if (fs.existsSync(file)) return {status:200, file};
  if (pending.has(url)) return pending.get(url);
  const promise = new Promise(resolve => {
    let status = '', error = '';
    const child = spawn('curl.exe', ['--location','--silent','--show-error','--connect-timeout','15','--max-time','300','--retry','1','--output',file+'.part','--write-out','%{http_code}',url], {windowsHide:true});
    child.stdout.on('data', data => status += data);
    child.stderr.on('data', data => error += data);
    child.on('error', err => resolve({status:502,error:err.message}));
    child.on('close', code => {
      const result = Number(status.slice(-3));
      if (code === 0 && result === 200) {
        fs.renameSync(file+'.part',file);
        console.log('200 '+url);
        resolve({status:200,file});
      } else {
        console.log((result || 502)+' '+url+' '+error.trim());
        resolve({status: result===404 ? 404 : 502});
      }
    });
  }).finally(() => pending.delete(url));
  pending.set(url,promise);
  return promise;
}
http.createServer(async (req,res) => {
  const match = /^\/([^/]+)\/(.*)$/.exec(req.url);
  if (!match || !roots[match[1]] || !['GET','HEAD'].includes(req.method) || match[2].includes('..')) {
    res.writeHead(400).end(); return;
  }
  const result = await fetchFile(roots[match[1]]+match[2]);
  if (result.status !== 200) {res.writeHead(result.status).end(); return;}
  res.writeHead(200, {'Content-Type':'application/octet-stream','Content-Length':fs.statSync(result.file).size});
  if (req.method === 'HEAD') res.end();
  else fs.createReadStream(result.file).pipe(res);
}).listen(18761,'127.0.0.1',() => console.log('Fusion Core build relay listening on 127.0.0.1:18761'));
