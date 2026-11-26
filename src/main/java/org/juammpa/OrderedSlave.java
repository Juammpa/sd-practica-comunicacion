package org.juammpa;

import java.io.*;
import java.net.*;

public class OrderedSlave {
    // java OrderedSlave <port> <offsetMillis> <processName>
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);
        SimClock clock = new SimClock(Long.parseLong(args[1]));
        String name = args[2];
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println(name + " listening on " + port + ", clock = " + SimClock.fmt(clock.now()));
            while (true) handle(server.accept(), clock, name);
        }
    }

    static void handle(Socket socket, SimClock clock, String name) {
        try (socket;
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            while (true) {
                switch (in.readUTF()) {
                    case "TIME" -> {
                        long now = clock.now();
                        out.writeLong(now);
                        out.flush();
                        System.out.println("Master asked the time -> sent " + SimClock.fmt(now));
                    }
                    case "ADJUST" -> {
                        long delta = in.readLong();
                        String before = SimClock.fmt(clock.now());
                        clock.adjust(delta);
                        System.out.printf("Adjustment %+d ms: %s -> %s%n", delta, before, SimClock.fmt(clock.now()));
                    }
                    case "RUN" -> {
                        long when = in.readLong();
                        System.out.println(name + " scheduled for " + SimClock.fmt(when));
                        while (clock.now() < when) Thread.sleep(1);
                        long now = clock.now();
                        System.out.println(">>> " + name + " EXECUTING at " + SimClock.fmt(now));
                        out.writeUTF(name + " executed at " + SimClock.fmt(now));
                        out.flush();
                    }
                }
            }
        } catch (EOFException e) {
            System.out.println("Master disconnected");
        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}