package org.juammpa;

import java.io.*;
import java.net.*;
import java.util.*;

public class Master {
    static final long MAX_DIFF = 60_000;   // slaves further than this are excluded from the average

    record Conn(String addr, Socket socket, DataInputStream in, DataOutputStream out) {}

    // java Master <offsetMillis> <host:port> <host:port>
    public static void main(String[] args) throws Exception {
        SimClock clock = new SimClock(Long.parseLong(args[0]));
        List<Conn> slaves = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            String[] hp = args[i].split(":");
            Socket s = new Socket(hp[0], Integer.parseInt(hp[1]));
            slaves.add(new Conn(args[i], s, new DataInputStream(s.getInputStream()),
                    new DataOutputStream(s.getOutputStream())));
        }
        System.out.println("Master clock: " + SimClock.fmt(clock.now()));

        System.out.println("\n--- BEFORE synchronization ---");
        long[] diffs = measure(clock, slaves);

        // average of all differences, the master counts as 0
        long sum = 0;
        int count = 1;
        for (long d : diffs) {
            if (Math.abs(d) <= MAX_DIFF) { sum += d; count++; }
        }
        long avg = sum / count;
        System.out.println("\nAverage difference vs master: " + avg + " ms");

        for (int i = 0; i < slaves.size(); i++) {
            long adjustment = avg - diffs[i];
            slaves.get(i).out().writeUTF("ADJUST");
            slaves.get(i).out().writeLong(adjustment);
            slaves.get(i).out().flush();
            System.out.printf("Adjustment sent to %s: %+d ms%n", slaves.get(i).addr(), adjustment);
        }
        clock.adjust(avg);
        System.out.printf("Master adjusts itself: %+d ms%n", avg);

        System.out.println("\n--- AFTER synchronization ---");
        measure(clock, slaves);
        System.out.println("Master clock: " + SimClock.fmt(clock.now()));

        for (Conn c : slaves) c.socket().close();
    }

    // Returns each slave's difference vs the master, compensating half the RTT
    static long[] measure(SimClock clock, List<Conn> slaves) throws IOException {
        long[] diffs = new long[slaves.size()];
        for (int i = 0; i < slaves.size(); i++) {
            Conn c = slaves.get(i);
            long t0 = clock.now();
            c.out().writeUTF("TIME");
            c.out().flush();
            long slaveTime = c.in().readLong();
            long t1 = clock.now();
            diffs[i] = (slaveTime + (t1 - t0) / 2) - t1;
            System.out.printf("  %s: time=%s, RTT=%d ms, diff=%+d ms%n",
                    c.addr(), SimClock.fmt(slaveTime), t1 - t0, diffs[i]);
        }
        return diffs;
    }
}