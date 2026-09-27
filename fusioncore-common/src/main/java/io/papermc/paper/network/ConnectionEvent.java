// Fusion Core: Paper 1.21.1 server class (patch 0521 "Add Channel initialization listeners"), checker-framework annotations swapped for JetBrains ones.
package io.papermc.paper.network;

/**
 * Internal connection pipeline events.
 */
public enum ConnectionEvent {

    COMPRESSION_THRESHOLD_SET,
    COMPRESSION_DISABLED
}
