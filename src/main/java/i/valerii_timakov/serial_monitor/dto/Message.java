package i.valerii_timakov.serial_monitor.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class Message {
    @Getter
    private final String value;
    @Getter
    private final LocalDateTime time;
    @Getter
    private final boolean incoming;
}
