package org.juammpa;

import java.io.*;
import java.net.*;

public class TCPClient {
    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket(args[0], 7000);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = console.readLine()) != null && !line.equals("exit")) {
                out.writeUTF(line);
                out.flush();
                System.out.println(in.readUTF());
            }
        }
    }
}
