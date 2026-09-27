package io.izzel.arclight.common.mod.util;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

/** Fusion Core: bootstrap for plugin access to Minecraft fields that Spigot makes public and this server does not. */
public final class FusionCoreNmsAccess {

    public static final String GET = "get", PUT = "put", GET_STATIC = "getStatic", PUT_STATIC = "putStatic";

    private FusionCoreNmsAccess() {
    }

    public static CallSite bootstrap(MethodHandles.Lookup caller, String kind, MethodType type, Class<?> owner, String name) throws Throwable {
        Field field = find(owner, name);
        field.setAccessible(true);
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle handle = switch (kind) {
            case GET, GET_STATIC -> lookup.unreflectGetter(field);
            case PUT, PUT_STATIC -> lookup.unreflectSetter(field);
            default -> throw new IllegalArgumentException(kind);
        };
        return new ConstantCallSite(handle.asType(type));
    }

    static Field find(Class<?> owner, String name) throws NoSuchFieldException {
        for (Class<?> c = owner; c != null; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
            for (Class<?> itf : c.getInterfaces()) {
                try {
                    return itf.getField(name);
                } catch (NoSuchFieldException ignored) {
                }
            }
        }
        throw new NoSuchFieldException(owner.getName() + "." + name);
    }
}
