package i.valerii_timakov.serial_monitor.controllers.select_wrappers;

public class ItemWrapper<T> {
    private final String label;
    private final T value;

    public ItemWrapper(T value, String label) {
        this.label = label;
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return label;
    }
}
