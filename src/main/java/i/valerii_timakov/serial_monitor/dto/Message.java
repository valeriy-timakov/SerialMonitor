package i.valerii_timakov.serial_monitor.dto;

import lombok.Getter;

import java.time.LocalDateTime;

public class Message {
    @Getter
    private final String value;
    @Getter
    private final LocalDateTime time;
    @Getter
    private final boolean incoming;

    public Message(String value, LocalDateTime time, boolean incoming) {
        this.value = value;
        this.time = time;
        this.incoming = incoming;
    }
}
