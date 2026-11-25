package org.juammpa;

import java.io.*;
import java.net.*;

public class Slave {
    // java Slave <port> <offsetMillis>
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);
        SimClock clock = new SimClock(Long.parseLong(args[1]));
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Slave listening on " + port + ", clock = " + SimClock.fmt(clock.now()));
            while (true) handle(server.accept(), clock);
        }
    }

    static void handle(Socket socket, SimClock clock) {
        try (socket;
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            while (true) {
                String cmd = in.readUTF();
                if (cmd.equals("TIME")) {
                    long now = clock.now();
                    out.writeLong(now);
                    out.flush();
                    System.out.println("Master asked the time -> sent " + SimClock.fmt(now));
                } else if (cmd.equals("ADJUST")) {
                    long delta = in.readLong();
                    String before = SimClock.fmt(clock.now());
                    clock.adjust(delta);
                    System.out.printf("Adjustment %+d ms: %s -> %s%n", delta, before, SimClock.fmt(clock.now()));
                }
            }
        } catch (EOFException e) {
            System.out.println("Master disconnected");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}