package i.valerii_timakov.serial_monitor.errors;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NotEnoughDataException extends IllegalArgumentException {

    public NotEnoughDataException(String msg) {
        super(msg);
    }
}
