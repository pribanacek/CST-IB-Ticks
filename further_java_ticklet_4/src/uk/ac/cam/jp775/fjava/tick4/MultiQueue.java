package uk.ac.cam.jp775.fjava.tick4;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MultiQueue<T> {
    private Set<MessageQueue<T>> outputs = new HashSet<MessageQueue<T>>(); //TODO

    public void register(MessageQueue<T> q) {
        synchronized (outputs) {
            outputs.add(q);
        }
    }

    public void deregister(MessageQueue<T> q) {
        synchronized (outputs) {
            outputs.remove(q);
        }
    }

    public void put(T message) {
        synchronized (outputs) {
            Iterator<MessageQueue<T>> iterator = outputs.iterator();
            while (iterator.hasNext()) {
                MessageQueue<T> queue = iterator.next();
                queue.put(message);
            }
        }
    }
}