package org.juammpa;

import java.io.*;
import java.net.*;

public class TCPServer {
    public static void main(String[] args) throws Exception {
        try (ServerSocket server = new ServerSocket(7000)) {
            System.out.println("TCP server listening on 7000");
            while (true) {
                Socket client = server.accept();
                new Thread(() -> handle(client)).start();   // one thread per client
            }
        }
    }

    static void handle(Socket socket) {
        try (socket;
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            while (true) {
                String msg = in.readUTF();                  // blocks until a full message arrives
                System.out.println("From " + socket.getInetAddress() + ": " + msg);
                out.writeUTF("OK: " + msg);
                out.flush();
            }
        } catch (EOFException e) {
            System.out.println("Client disconnected");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
