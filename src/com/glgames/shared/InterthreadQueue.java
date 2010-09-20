package com.glgames.shared;

/* Utility class that is thread safe if no writes are performed concurrently and no reads are performed concurrently.
   A read and a write my safely occur concurrently. The traditional scenario here is to have one thread writing based
   on some outside event, and another thread that consumes the written elements as part of a processing loop. 		*/

public class InterthreadQueue<T> {
	private Node<T> head, tail;

	public InterthreadQueue() {
		head = tail = new Node<T>();
	}

	public T pull() {
		Node<T> tt = tail.prev;	// Local reference created to ensure that the value does not change concurrently after being read.
		if (tt == null)
			return null; // If this is non-null, we know that tail != head
		tail = tt;
		return tt.item;
	}
	
	public void push(T t) {
		Node<T> nh = new Node<T>();
		nh.item = t;
		head.prev = nh;
		head = nh;
	}

	private class Node<NT> {
		private Node<NT> prev;
		private NT item;
	}
}

