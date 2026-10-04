package com.movieflix.util;

public class CustomQueue<T> {
    private Object[] elements;
    private int head;
    private int tail;
    private int size;
    private static final int INITIAL_CAPACITY = 10;

    public CustomQueue() {
        elements = new Object[INITIAL_CAPACITY];
        head = 0;
        tail = 0;
        size = 0;
    }

    public void enqueue(T item) {
        if (size == elements.length) {
            resize();
        }
        elements[tail] = item;
        tail = (tail + 1) % elements.length;
        size++;
    }

    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (size == 0) {
            throw new IllegalStateException("Queue is empty");
        }
        T item = (T) elements[head];
        elements[head] = null;
        head = (head + 1) % elements.length;
        size--;
        return item;
    }

    public int size() {
        return size;
    }

    private void resize() {
        Object[] newElements = new Object[elements.length * 2];
        for (int i = 0; i < size; i++) {
            newElements[i] = elements[(head + i) % elements.length];
        }
        elements = newElements;
        head = 0;
        tail = size;
    }
}