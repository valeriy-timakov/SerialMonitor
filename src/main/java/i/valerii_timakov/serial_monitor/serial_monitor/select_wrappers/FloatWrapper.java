package i.valerii_timakov.serial_monitor.serial_monitor.select_wrappers;

import com.fazecast.jSerialComm.SerialPort;

public class FloatWrapper extends ItemWrapper<Float> {
    public FloatWrapper(Float value) {
        super(value, Float.toString(value));
    }
}
