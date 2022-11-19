package i.valerii_timakov.serial_monitor.services;

import i.valerii_timakov.serial_monitor.dto.Propery;
import i.valerii_timakov.serial_monitor.dto.WordMessage;
import i.valerii_timakov.serial_monitor.utils.Log;
import i.valerii_timakov.serial_monitor.utils.PooledLinkedList;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class ByteLogService implements ByteArrayMessageConsumer {

    private final List<Integer> CORRECT_WORD_SIZES = Arrays.asList(1, 2, 4, 8);

    private final SettingsService settingsService;

    private List<Consumer<WordMessage>> wordConsumers = new ArrayList<>();

    private PooledLinkedList<Byte> buffer = new PooledLinkedList<>();

    private int wordSize;

    public void init() {
        updateWordSize();
    }

    public void addWordConsumer(@NonNull Consumer<WordMessage> consumer) {
        wordConsumers.add(consumer);
    }

    @Override
    public void consume(byte[] src, int count, boolean incoming) {
        updateWordSize();
        for (int i = 0; i < count; i++) {
            buffer.add(src[i]);
        }
        while (buffer.getSize() >= wordSize) {
            Number word;
            if (wordSize == 8) {
                long wordValue  = buffer.poll() << 24;
                wordValue |= buffer.poll() << 16;
                wordValue |= buffer.poll() << 8;
                wordValue |= buffer.poll();
                word = wordValue;
            } else if (wordSize == 4) {
                int wordValue  = buffer.poll() << 16;
                wordValue |= buffer.poll() << 8;
                wordValue |= buffer.poll();
                word = wordValue;
            } else if (wordSize == 2) {
                short wordValue  = (short) (buffer.poll() << 8);
                wordValue |= buffer.poll();
                word = wordValue;
            } else if (wordSize == 1) {
                word = buffer.poll();
            } else {
                throw new IllegalStateException("Wrong word size to extract! Value: " + wordSize);
            }
            WordMessage message = new WordMessage(word, LocalDateTime.now(), incoming, wordSize);
            consume(c -> c.accept(message));
        }
    }

    private void updateWordSize() {
        wordSize = settingsService.get(Propery.WordSize);
        if (!CORRECT_WORD_SIZES.contains(wordSize)) {
            throw new IllegalStateException("Wrong word size computed! Value: " + wordSize);
        }
    }

    private void consume(Consumer<Consumer<WordMessage>> consumer) {
        wordConsumers.forEach(c -> {
            try {
                consumer.accept(c);
            } catch (Throwable t) {
                Log.error("Error consuming word message!", t);
            }
        });
    }
}
