package i.valerii_timakov.serial_monitor.utils;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;

public class PooledLinkedList<T>  {

    @Data
    private static final class Node<T> {
        private T value;
        private Node<T> next;
    }

    private List<Node<T>> pool = new ArrayList<>();
    private Node<T> first;
    private Node<T> last;
    @Getter
    private int size = 0;

    public void add(T value) {
        if (value == null) {
            throw new NullPointerException("Cannot add null to list!");
        }
        Node<T> newNode = getNode();
        newNode.setValue(value);
        newNode.setNext(null);
        if (last != null) {
            last.setNext(newNode);
        } else {
            first = newNode;
        }
        last = newNode;
        size++;
    }

    public void addAll(Collection<T> values) {
        values.forEach(this::add);
    }

    public T poll() {
        if (first == null) {
            throw new IllegalStateException("No elements to poll!");
        }
        return extractFirst();
    }

    public Optional<T> tryPoll() {
        if (first == null) {
            return Optional.empty();
        }
        return Optional.of(extractFirst());
    }

    private T extractFirst() {
        T result = first.getValue();
        pool.add(first);
        first = first.getNext();
        if (first == null) {
            last = null;
        }
        size--;
        return result;
    }

    private Node<T> getNode() {
        if (!pool.isEmpty()) {
            return pool.remove(pool.size() - 1);
        }
        return new Node<>();
    }

    public boolean isEmpty() {
        return first == null;
    }
}
