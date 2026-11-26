package org.juammpa;

import java.io.*;
import java.net.*;

public class CristianServer {
    // java CristianServer <port>
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);
        SimClock clock = new SimClock(0);              // reference clock
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Time server listening on " + port);
            while (true) {
                Socket s = server.accept();
                new Thread(() -> serve(s, clock)).start();
            }
        }
    }

    static void serve(Socket socket, SimClock clock) {
        try (socket;
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            while (true) {
                in.readUTF();                           // "TIME"
                long now = clock.now();
                out.writeLong(now);
                out.flush();
                System.out.println("Time requested -> sent " + SimClock.fmt(now));
            }
        } catch (EOFException e) {
            System.out.println("Client disconnected");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}