package i.valerii_timakov.serial_monitor.controllers.select_wrappers;

import com.fazecast.jSerialComm.SerialPort;
public class PortWrapper extends ItemWrapper<SerialPort> {
    public PortWrapper(SerialPort port) {
        super(port, port.getPortDescription() + "(" + port.getSystemPortName() + ")");
    }
}
