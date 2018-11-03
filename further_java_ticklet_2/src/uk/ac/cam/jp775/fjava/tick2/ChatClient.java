package uk.ac.cam.jp775.fjava.tick2;

import uk.ac.cam.cl.fjava.messages.*;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.AccessControlException;
import java.text.SimpleDateFormat;
import java.util.Date;

@FurtherJavaPreamble(
        author = "Jakub Priban",
        date = "31st October 2018",
        crsid = "jp775",
        summary = "Chatting with serialization",
        ticker = FurtherJavaPreamble.Ticker.B)
public class ChatClient {

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

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

        final Socket s = new Socket();
        SocketAddress address = new InetSocketAddress(server, port);
        ObjectOutputStream outputStream;

        try {
            s.connect(address);
            outputStream = new ObjectOutputStream(s.getOutputStream());

            String date = dateFormat.format(new Date());
            System.out.println(date + " [Client] Connected to " + server + " on port " + port + ".");
        } catch (AccessControlException | IOException e) {
            System.err.println("Cannot connect to " + server + " on port " + port);
            return;
        }

        Thread output = new Thread() {

            @Override
            public void run() {
                try {
                    DynamicObjectInputStream inputStream = new DynamicObjectInputStream(s.getInputStream());
                    while (true) {
                        Object o = inputStream.readObject();
                        if (o instanceof RelayMessage) {
                            RelayMessage message = (RelayMessage) o;

                            String date = dateFormat.format(message.getCreationTime());
                            String from = message.getFrom();
                            String text = message.getMessage();

                            System.out.println(date + " [" + from + "] " + text);
                        } else if (o instanceof StatusMessage) {
                            StatusMessage message = (StatusMessage) o;

                            String date = dateFormat.format(message.getCreationTime());
                            String text = message.getMessage();

                            System.out.println(date + " [Server] " + text);

                        } else if (o instanceof NewMessageType) {
                            NewMessageType message = (NewMessageType) o;

                            String date = dateFormat.format(message.getCreationTime());
                            String className = message.getName();

                            inputStream.addClass(className, message.getClassData());

                            System.out.println(date + " [Client] New class " + className + " loaded.");
                        } else {
                            String date = dateFormat.format(new Date());
                            Class<?> clazz = o.getClass();
                            Field[] fields = clazz.getDeclaredFields();
                            String fieldsText = "";
                            for (Field field : fields) {
                                try {
                                    field.setAccessible(true);
                                    String name = field.getName();
                                    String value = field.get(o).toString();
                                    fieldsText = fieldsText + " " + name + "(" + value + "),";
                                } catch (IllegalAccessException e) {
                                    System.err.println("This should never happen; pls fix");
                                    e.printStackTrace();
                                    continue;
                                }
                            }
                            fieldsText = fieldsText.substring(0, fieldsText.length() - 1);
                            System.out.println(date + " [Client] " + clazz.getSimpleName() + ":" + fieldsText);

                            Method[] methods = clazz.getMethods();
                            for (Method method : methods) {
                                if (method.getAnnotation(Execute.class) != null && method.getParameterCount() == 0) {
                                    try {
                                        method.invoke(o);
                                    } catch (IllegalAccessException | InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    return;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return;
                }
            }
        };

        //daemon threads don't prevent the JVM exiting, if the thread is still running
        output.setDaemon(true);
        output.start();

        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));

        while(true) {
            try {
                String input = r.readLine();
                Message m;
                if (!input.startsWith("\\")) {
                    m = new ChatMessage(input);
                } else {
                    if (input.startsWith("\\nick ")) {
                        String nick = input.substring(6);
                        m = new ChangeNickMessage(nick);
                    } else if (input.startsWith("\\quit")) {
                        String date = dateFormat.format(new Date());
                        System.out.println(date + " [Client] Connection terminated.");
                        s.close();
                        return;
                    } else {
                        System.err.println("Invalid command entered");
                        continue;
                    }
                }
                outputStream.writeObject(m);
            } catch (IOException e) {
                System.err.println("Socket disconnected");
                return;
            }
        }

    }

}
