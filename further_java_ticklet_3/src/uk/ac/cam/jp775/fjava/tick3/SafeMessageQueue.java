package uk.ac.cam.jp775.fjava.tick3;

public class SafeMessageQueue<T> implements MessageQueue<T> {
    private static class Link<L> {
		L val;
		Link<L> next;
		Link(L val) {
			this.val = val;
			this.next = null;
		}
    }
    private Link<T> first = null;
    private Link<T> last = null;

    public synchronized void put(T val) {
    	Link<T> link = new Link<T>(val);
		if (first == null) {
			//last == null (in theory)
    		first = link;
			last = link;
		} else {
    		if (first == last) {
				last = link;
				first.next = last;
			} else {
    			last.next = link;
    			last = link;
			}
		}
		this.notify();
    }

    public synchronized T take() {
		while(first == null) { //use a loop to block thread until data is available
			try {
				this.wait(100);
			} catch(InterruptedException ie) {
			// Ignored exception
			// TODO: what causes this exception to be thrown? and what should
			//       you do with it ideally?
				//if we're interrupted, we should exit, so return here
			}
		}
		Link<T> link = first;
		first = link.next;
		if (first == null) last = null;
		return link.val;
    }
}