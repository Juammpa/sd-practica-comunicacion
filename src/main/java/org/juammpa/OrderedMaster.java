package org.juammpa;

import java.io.*;
import java.net.*;
import java.util.*;

public class OrderedMaster {
    // java OrderedMaster <offsetMillis> <host:port> <host:port> <host:port>
    public static void main(String[] args) throws Exception {
        SimClock clock = new SimClock(Long.parseLong(args[0]));
        List<Master.Conn> slaves = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            String[] hp = args[i].split(":");
            Socket s = new Socket(hp[0], Integer.parseInt(hp[1]));
            slaves.add(new Master.Conn(args[i], s, new DataInputStream(s.getInputStream()),
                    new DataOutputStream(s.getOutputStream())));
        }
        System.out.println("P1 (master) clock: " + SimClock.fmt(clock.now()));

        System.out.println("\n--- BEFORE synchronization ---");
        long[] diffs = Master.measure(clock, slaves);

        long sum = 0;
        for (long d : diffs) sum += d;
        long avg = sum / (slaves.size() + 1);          // the master counts as 0
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
        Master.measure(clock, slaves);

        // ordered execution in synchronized time: P1, then P2, P3, P4 every 2 seconds
        System.out.println("\n--- ORDERED EXECUTION ---");
        long t0 = clock.now() + 3000;
        for (int i = 0; i < slaves.size(); i++) {
            long when = t0 + 2000L * (i + 1);
            slaves.get(i).out().writeUTF("RUN");
            slaves.get(i).out().writeLong(when);
            slaves.get(i).out().flush();
            System.out.println("P" + (i + 2) + " scheduled for " + SimClock.fmt(when));
        }
        System.out.println("P1 scheduled for " + SimClock.fmt(t0));
        while (clock.now() < t0) Thread.sleep(1);
        System.out.println(">>> P1 EXECUTING at " + SimClock.fmt(clock.now()));

        for (Master.Conn c : slaves) System.out.println(c.in().readUTF());
        for (Master.Conn c : slaves) c.socket().close();
    }
}