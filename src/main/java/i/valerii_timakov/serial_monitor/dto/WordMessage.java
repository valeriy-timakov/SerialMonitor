package i.valerii_timakov.serial_monitor.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class WordMessage {
    @Getter
    private final Number word;
    @Getter
    private final LocalDateTime time;
    @Getter
    private final boolean incoming;
    @Getter
    private final int wordSize;
}

