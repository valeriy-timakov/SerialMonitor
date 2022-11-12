package i.valerii_timakov.serial_monitor.controllers.select_wrappers;

public class IntWrapper extends ItemWrapper<Integer> {
    public IntWrapper(Integer value) {
        super(value, Integer.toString(value));
    }
}
