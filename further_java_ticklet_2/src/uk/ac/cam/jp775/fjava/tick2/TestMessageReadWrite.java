package uk.ac.cam.jp775.fjava.tick2;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

class TestMessageReadWrite {
 
    static boolean writeMessage(String message, String filename) {
        TestMessage testMessage = new TestMessage();
        testMessage.setMessage(message);

        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(testMessage);
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    static String readMessage(String location) {

        String returnText = null;
        ObjectInputStream ois;

        try {
            if (location.startsWith("http://") || location.startsWith("https://")) {
                URL url = new URL(location);
                URLConnection conn = url.openConnection();
                ois = new ObjectInputStream(conn.getInputStream());
            } else {
                FileInputStream fis = new FileInputStream(location);
                ois = new ObjectInputStream(fis);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        try {
            Object o = ois.readObject();
            if (o instanceof TestMessage) {
                returnText = ((TestMessage) o).getMessage();
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return null;
        }
        return returnText;
    }

    public static void main(String args[]) {
        writeMessage(args[0], args[1]);
        System.out.println(readMessage(args[1]));
    }
}
