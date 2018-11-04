package uk.ac.cam.jp775.fjava.tick1;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.AccessControlException;

public class StringReceive {

    public static void main(String[] args) {
        String server = null;
        int port = 0;

        try {
            server = args[0];
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.err.println("This application requires two arguments: <machine> <port>");
            return;
        }

        try {
            Socket s = new Socket(server, port);
            readFromSocketContinuously(s);
        } catch (AccessControlException | IOException e) {
            System.err.println("Cannot connect to " + server + " on port " + port);
            return;
        }

    }

    public static void readFromSocketContinuously(Socket s) throws IOException {
        byte[] buffer = new byte[1024];
        InputStream inputStream = s.getInputStream();

        while (true) {
            int bytesRead = inputStream.read(buffer);
            String text = new String(buffer, 0, bytesRead);
            System.out.println(text);
        }
    }
}
