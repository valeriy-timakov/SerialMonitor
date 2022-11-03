package i.valerii_timakov.serial_monitor.serial_monitor.select_wrappers;

import com.fazecast.jSerialComm.SerialPort;

public class IntWrapper extends ItemWrapper<Integer> {
    public IntWrapper(Integer value) {
        super(value, Integer.toString(value));
    }
}
