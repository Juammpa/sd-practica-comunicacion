package org.juammpa;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class SimClock {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private volatile long offset;

    public SimClock(long offset) { this.offset = offset; }

    public long now() { return System.currentTimeMillis() + offset; }

    public synchronized void adjust(long delta) { offset += delta; }

    public static String fmt(long millis) {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(FORMAT);
    }
}