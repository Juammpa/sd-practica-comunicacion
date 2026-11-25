package org.juammpa;

import java.net.*;
import java.nio.charset.StandardCharsets;

public class UDPClient {
    public static void main(String[] args) throws Exception {
        String host = args[0];          // server IP
        String msg = args[1];
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(3000);
            byte[] data = msg.getBytes(StandardCharsets.UTF_8);
            socket.send(new DatagramPacket(data, data.length, InetAddress.getByName(host), 9876));

            DatagramPacket reply = new DatagramPacket(new byte[1024], 1024);
            socket.receive(reply);
            System.out.println(new String(reply.getData(), 0, reply.getLength(), StandardCharsets.UTF_8));
        }
    }
}
