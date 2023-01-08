package i.valerii_timakov.serial_monitor.utils;

public interface BytesReader {
    byte get(int pos);
    boolean hasItem(int pos);
}
