package org.juammpa;

import java.net.*;
import java.nio.charset.StandardCharsets;

public class UDPServer {
    public static void main(String[] args) throws Exception {
        int port = 9876;
        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("UDP server listening on " + port);
            byte[] buf = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                System.out.println("From " + packet.getAddress() + ": " + msg);

                byte[] reply = ("Received: " + msg).getBytes(StandardCharsets.UTF_8);
                socket.send(
                    new DatagramPacket(
                        reply, reply.length, packet.getAddress(), packet.getPort()));
            }
        }
    }
}
