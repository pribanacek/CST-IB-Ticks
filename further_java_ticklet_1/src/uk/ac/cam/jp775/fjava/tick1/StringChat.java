package uk.ac.cam.jp775.fjava.tick1;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.AccessControlException;

public class StringChat {

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

        // s is declared final to ensure thread safety
		final Socket s = new Socket();
        SocketAddress address = new InetSocketAddress(server, port);
        OutputStream outputStream;

        try {
            s.connect(address);
            outputStream = s.getOutputStream();
        } catch (AccessControlException | IOException e) {
            System.err.println("Cannot connect to " + server + " on port " + port);
            return;
        }

		Thread output = new Thread() {

            byte[] buffer = new byte[1024];

            @Override
			public void run() {
				try {
					InputStream inputStream = s.getInputStream();
					while (true) {
						int bytesRead = inputStream.read(buffer);
						String text = new String(buffer, 0, bytesRead);
						System.out.println(text);
					}
				} catch (IOException e) {
				    System.err.println("Socket disconnected");
					return;
				}
			}
		};

		output.setDaemon(true);
		output.start();

		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));

        while(true) {
            try {
                String message = r.readLine();
                outputStream.write(message.getBytes());
            } catch (IOException e) {
                System.err.println("Socket disconnected");
                return;
            }
        }

    }
}