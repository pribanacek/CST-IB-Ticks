package uk.ac.cam.jp775.fjava.tick4;

import uk.ac.cam.cl.fjava.messages.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    public static void main(String args[]) {
        if (args.length != 1) {
            System.out.println("Usage: java ChatServer <port>");
            return;
        }

        int port = 0;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Cannot use port number " + args[0]);
            return;
        }

        try {
            ServerSocket socket = new ServerSocket(port);
            MultiQueue<Message> multiQueue = new MultiQueue<Message>();
            while (true) {
                Socket s = socket.accept();
                ClientHandler clientHandler = new ClientHandler(s, multiQueue);
            }
        } catch (IOException e) {
            System.err.println("Socket disconnected");
        }


    }
}