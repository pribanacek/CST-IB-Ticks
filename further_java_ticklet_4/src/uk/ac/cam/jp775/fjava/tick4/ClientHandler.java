package uk.ac.cam.jp775.fjava.tick4;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

import uk.ac.cam.cl.fjava.messages.*;

public class ClientHandler {
    private Socket socket;
    private MultiQueue<Message> multiQueue;
    private String nickname;
    private MessageQueue<Message> clientMessages;

    public ClientHandler(Socket s, MultiQueue<Message> q) {
        this.socket = s;
        this.multiQueue = q;
        this.clientMessages = new SafeMessageQueue<Message>();
        this.multiQueue.register(this.clientMessages);
        this.nickname = "Anonymous" + (new Random()).nextInt(100000);

        sendStatusConnectedMessage();

        Thread tReceive = getReceiveThread();
        Thread tSend = getSendThread();

        tReceive.setDaemon(true);
        tSend.setDaemon(true);
        tReceive.start();
        tSend.start();
    }

    private void sendStatusConnectedMessage() {
        String hostName = this.socket.getInetAddress().getHostName();
        String message = this.nickname + " connected from " + hostName + ".";
        StatusMessage statusMessage = new StatusMessage(message);
        this.multiQueue.put(statusMessage);
    }

    private Thread getReceiveThread() {
        return new Thread(() -> {
            try {
                ObjectInputStream inputStream = new ObjectInputStream(this.socket.getInputStream());
                while (true) {
                    Object o = inputStream.readObject();
                    if (o instanceof ChangeNickMessage) {
                        String newNick = ((ChangeNickMessage) o).name;
                        String message = this.nickname + " is now known as " + newNick + ".";
                        this.nickname = newNick;
                        this.multiQueue.put(new StatusMessage(message));
                    } else if (o instanceof ChatMessage) {
                        this.multiQueue.put(new RelayMessage(this.nickname, (ChatMessage) o));
                    }
                }
            } catch (IOException e) {
                this.handleDisconnect();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    private Thread getSendThread() {
        return new Thread(() -> {
            try {
                ObjectOutputStream outputStream = new ObjectOutputStream(this.socket.getOutputStream());
                while (true) {
                    Message message = this.clientMessages.take(); //blocks until message available
                    outputStream.writeObject(message);
                }
            } catch (IOException e) {
                System.err.println("Could not write to socket");
                e.printStackTrace();
            }
        });
    }

    private void handleDisconnect() {
        this.multiQueue.deregister(this.clientMessages);
        String message = this.nickname + " has disconnected.";
        this.multiQueue.put(new StatusMessage(message));
    }
}

