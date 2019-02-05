package net.threesided.shared;

/**
 * Utility class that is thread safe if no writes are performed concurrently and no reads are performed concurrently.
 * A read and a write may safely occur concurrently. The traditional scenario here is to have one thread writing based
 * on some outside event, and another thread that consumes the written elements as part of a processing loop.
 */
public class InterthreadQueue<T> {

    private Node<T> head, tail;

    /**
     *
     */
    public InterthreadQueue() {
        this.head = this.tail = new Node<>();
    }

    /**
     * Pulls an item off the queue.
     * @return The item taken from the queue.
     */
    public T pull() {
        // Local reference created to ensure that the value does not change concurrently after being read.
        Node<T> tt = tail.prev;
        if (tt == null) {
            // If this is non-null, we know that tail != head
            return null;
        }
        this.tail = tt;
        return tt.item;
    }

    /**
     * Pushes an item to the queue.
     * @param t The item to push.
     */
    public void push(final T t) {
        final Node<T> nh = new Node<>();
        nh.item = t;
        this.head.prev = nh;
        this.head = nh;
    }

    private class Node<NT> {
        private Node<NT> prev;
        private NT item;
    }

}
