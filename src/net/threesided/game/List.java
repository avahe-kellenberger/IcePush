package net.threesided.game;

public class List<T> {
	private ListElement<T> head;
	
	public List() {
		head = new ListElement<T>();
	}
	
	public void push(ListElement<T> e) {
		e.next = head;
		head = e;
	}
	
	public ListElement<T> getFirst() {
		return head;
	}
}

class ListElement<T> {
	public T data;
	public ListElement<T> next;
	
	public ListElement() {
		data = null;
	}
	
	public ListElement(T d) {
		data = d;
	}
}
