package i.valerii_timakov.serial_monitor.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class ByteMessage {
    @Getter
    private final byte value;
    @Getter
    private final LocalDateTime time;
    @Getter
    private final boolean incoming;
}

