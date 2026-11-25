package org.juammpa;

import java.net.*;
import java.nio.charset.StandardCharsets;

public class ReqRep {
    static final int SERVER_IN = 5000, SERVER_OUT = 5001, CLIENT_IN = 6000, CLIENT_OUT = 6001;

    static void send(DatagramSocket s, String msg, String host, int port) throws Exception {
        byte[] b = msg.getBytes(StandardCharsets.UTF_8);
        s.send(new DatagramPacket(b, b.length, InetAddress.getByName(host), port));
        System.out.println("-> " + msg);
    }

    static DatagramPacket recv(DatagramSocket s) throws Exception {
        DatagramPacket p = new DatagramPacket(new byte[1024], 1024);
        s.receive(p);
        System.out.println("<- " + text(p));
        return p;
    }

    static String text(DatagramPacket p) {
        return new String(p.getData(), 0, p.getLength(), StandardCharsets.UTF_8);
    }

    // java ReqRep server <2|3|4>
    static void server(int ways) throws Exception {
        try (DatagramSocket in = new DatagramSocket(SERVER_IN);
             DatagramSocket out = new DatagramSocket(SERVER_OUT)) {
            System.out.println(ways + "-way server ready");
            while (true) {
                DatagramPacket req = recv(in);
                String client = req.getAddress().getHostAddress();
                if (ways == 4) send(out, "ACK", client, CLIENT_IN);          // ack of request
                send(out, "REPLY:" + text(req).toUpperCase(), client, CLIENT_IN);
                if (ways >= 3) recv(in);                                     // ack of reply
            }
        }
    }

    // java ReqRep client <serverIp> <2|3|4> <message>
    static void client(String host, int ways, String msg) throws Exception {
        try (DatagramSocket in = new DatagramSocket(CLIENT_IN);
             DatagramSocket out = new DatagramSocket(CLIENT_OUT)) {
            send(out, msg, host, SERVER_IN);
            if (ways == 4) recv(in);                                         // ack of request
            recv(in);                                                        // reply
            if (ways >= 3) send(out, "ACK", host, SERVER_IN);                // ack of reply
        }
    }

    public static void main(String[] a) throws Exception {
        if (a[0].equals("server")) server(Integer.parseInt(a[1]));
        else client(a[1], Integer.parseInt(a[2]), a[3]);
    }
}
