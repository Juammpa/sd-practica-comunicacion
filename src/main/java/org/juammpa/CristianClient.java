package org.juammpa;

import java.io.*;
import java.net.*;

public class CristianClient {
    // java CristianClient <host:port> <offsetMillis>
    public static void main(String[] args) throws Exception {
        String[] hp = args[0].split(":");
        SimClock clock = new SimClock(Long.parseLong(args[1]));
        try (Socket s = new Socket(hp[0], Integer.parseInt(hp[1]));
             DataInputStream in = new DataInputStream(s.getInputStream());
             DataOutputStream out = new DataOutputStream(s.getOutputStream())) {

            System.out.println("Client clock BEFORE: " + SimClock.fmt(clock.now()));

            double bestRtt = Double.MAX_VALUE;
            long bestDiff = 0;
            for (int i = 1; i <= 5; i++) {
                long start = System.nanoTime();
                out.writeUTF("TIME");
                out.flush();
                long serverTime = in.readLong();
                double rtt = (System.nanoTime() - start) / 1e6;
                long estimated = serverTime + Math.round(rtt / 2);   // Cristian: T + RTT/2
                long diff = estimated - clock.now();
                System.out.printf("  sample %d: server=%s, RTT=%.2f ms, diff=%+d ms%n",
                        i, SimClock.fmt(serverTime), rtt, diff);
                if (rtt < bestRtt) { bestRtt = rtt; bestDiff = diff; }
            }

            clock.adjust(bestDiff);
            System.out.printf("Chosen sample: RTT=%.2f ms, adjustment=%+d ms%n", bestRtt, bestDiff);
            System.out.println("Client clock AFTER: " + SimClock.fmt(clock.now()));

            // verification
            long start = System.nanoTime();
            out.writeUTF("TIME");
            out.flush();
            long serverTime = in.readLong();
            double rtt = (System.nanoTime() - start) / 1e6;
            System.out.printf("Verification: remaining diff=%+d ms%n",
                    serverTime + Math.round(rtt / 2) - clock.now());
        }
    }
}