package com.movieflix.util;

public class CustomList<T> {
    private Object[] elements;
    private int size;
    private static final int DEFAULT_CAPACITY = 10;

    public CustomList() {
        elements = new Object[DEFAULT_CAPACITY];
        size = 0;
    }

    public void add(T element) {
        if (size == elements.length) {
            Object[] newElements = new Object[elements.length * 2];
            System.arraycopy(elements, 0, newElements, 0, elements.length);
            elements = newElements;
        }
        elements[size++] = element;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return (T) elements[index];
    }

    public int size() {
        return size;
    }
}