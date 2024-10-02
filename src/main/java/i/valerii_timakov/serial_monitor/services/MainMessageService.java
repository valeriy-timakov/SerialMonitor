package i.valerii_timakov.serial_monitor.services;

import i.valerii_timakov.serial_monitor.dto.Propery;
import i.valerii_timakov.serial_monitor.utils.Log;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class MainMessageService implements TextMessageConsumer, ByteArrayMessageConsumer, UnidirectedMessageConsumer {

    private final SettingsService settingsService;
    private final  InputScanner inputScanner ;

    @Setter
    @Getter
    private Charset charset = Charset.defaultCharset();
    @Setter
    private OutgoingMessageConsumer outcomingMessageConsumer;
    private final List<TextMessageConsumer> incomingTextConsumers = new LinkedList<>();
    private final List<ByteArrayMessageConsumer> incomingByteArrConsumers = new LinkedList<>();

    public MainMessageService(SettingsService settingsService) {
        this.settingsService = settingsService;
        inputScanner = new InputScanner(this, settingsService);
    }

    @Override
    public void consume(String message, boolean incoming) {
        incomingTextConsumers.forEach(consumer -> consumer.consume(message, incoming));
    }

    @Override
    public void consume(byte[] src, int count, boolean incoming) {
        incomingByteArrConsumers.forEach(consumer -> consumer.consume(src, count, incoming));
        saveToConversationLog(src, count);
        if (incoming) {
            String message = new String(src, 0, count, charset);
            inputScanner.consume(message);
        }
    }

    @Override
    public void consume(String message) throws IOException {
        String delimiter = settingsService.get(Propery.Delimiter);
        String append = delimiter != null ? delimiter : "";
        Log.debug("sending string data: " + message);
        byte[] bytes = (message + append).getBytes(charset);
        outcomingMessageConsumer.consume(bytes, bytes.length);
        consume(message, false);
        consume(bytes, bytes.length, false);
    }

    @Override
    public void consume(byte[] src, int length) throws IOException {
        outcomingMessageConsumer.consume(src, length);
        consume(src, length, false);
        consume(new String(Arrays.copyOf(src, length)), false);
    }

    @Override
    public void idle() {
        inputScanner.idle();
    }

    private void saveToConversationLog(byte[] src, int count) {
        Optional<String> maybeConversationLogPath = settingsService.get(Propery.MaybeConversationLogPath);
        maybeConversationLogPath.ifPresent(conversationLogPath -> {
            Path path = Path.of(conversationLogPath);
            path.toFile().getParentFile().mkdirs();
            try {
                Files.write(path, Arrays.copyOf(src, count),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                Log.error("Error saving conversation log!", e);
            }
        });
    }

    public void addIncomingTextConsumer(@NonNull TextMessageConsumer consumer) {
        incomingTextConsumers.add(consumer);
    }

    public void addIncomingByteArrConsumer(@NonNull ByteArrayMessageConsumer consumer) {
        incomingByteArrConsumers.add(consumer);
    }
}
