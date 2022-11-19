package i.valerii_timakov.serial_monitor.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.Function;

@RequiredArgsConstructor
public enum Propery {
    Delimiter(null, null),
    MaybeConversationLogPath(v -> ((Optional<String>)v).orElse(null), Optional::ofNullable),
    MaxMessageWaitSeconds(Object::toString, Integer::valueOf),
    AddTimestampToTextMessageLog(Object::toString, Boolean::valueOf),
    AddDirectionToTextMessageLog(Object::toString, Boolean::valueOf),

    WordSize(Object::toString, Integer::valueOf);

    @Getter
    private final Function<Object, String> serializer;
    @Getter
    private final Function<String, Object> deserializer;
}

