package i.valerii_timakov.serial_monitor.services;

import i.valerii_timakov.serial_monitor.dto.Propery;
import i.valerii_timakov.serial_monitor.dto.ByteMessage;
import i.valerii_timakov.serial_monitor.utils.Log;
import i.valerii_timakov.serial_monitor.utils.PooledLinkedList;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class ByteLogService implements ByteArrayMessageConsumer {

    private final List<Integer> CORRECT_WORD_SIZES = Arrays.asList(1, 2, 4, 8);

    private final SettingsService settingsService;

    private List<Consumer<ByteMessage>> wordConsumers = new ArrayList<>();

    private PooledLinkedList<Byte> buffer = new PooledLinkedList<>();

    public void addWordConsumer(@NonNull Consumer<ByteMessage> consumer) {
        wordConsumers.add(consumer);
    }

    @Override
    public void consume(byte[] src, int count, boolean incoming) {
        for (int i = 0; i < count; i++) {
            buffer.add(src[i]);
        }
        while (buffer.getSize() > 0) {
            ByteMessage message = new ByteMessage(buffer.poll(), LocalDateTime.now(), incoming);
            consume(c -> c.accept(message));
        }
    }

    private void consume(Consumer<Consumer<ByteMessage>> consumer) {
        wordConsumers.forEach(c -> {
            try {
                consumer.accept(c);
            } catch (Throwable t) {
                Log.error("Error consuming word message!", t);
            }
        });
    }
}
