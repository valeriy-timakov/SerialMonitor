package i.valerii_timakov.serial_monitor.services;

import i.valerii_timakov.serial_monitor.dto.Propery;
import i.valerii_timakov.serial_monitor.utils.Log;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@RequiredArgsConstructor
public class InputScanner implements TextMessageConsumer {
    @NonNull
    private final TextMessageConsumer wrappedConsumer;
    private final Queue<String> result = new LinkedList<>();
    private final List<String> buffer = new LinkedList<>();
    private final SettingsService settingsService;
    private LocalTime lastAddTime = null;

    @Override
    public void consume(String message, boolean incoming) {
        add(message);
        while (hasResult()) {
            wrappedConsumer.consume(getResult(), incoming);
        }
    }

    private void add(String data) {

        if (lastAddTime == null) {
            lastAddTime = LocalTime.now();
        } else {
            int maxMessageWaitSeconds = settingsService.get(Propery.MaxMessageWaitSeconds);
            if (Duration.between(lastAddTime, LocalTime.now()).toSeconds() > maxMessageWaitSeconds){
                Log.debug("scan time out - extracting messages");
                extractMessage();
            }
        }

        if (data == null || data.isEmpty()) {
            return;
        }


        String delimiter = settingsService.get(Propery.Delimiter);
        if (delimiter != null && !delimiter.isEmpty()) {
            String[] found = data.split(delimiter, -1);
            Log.debug("splitting by delimiter: " + String.join(";", found));
            if (found.length > 1) {
                Log.debug("add to buffer for extracting: [0]=\"" + found[0] + "\"");
                buffer.add(found[0]);
                extractMessage();
                for (int i = 1; i < found.length - 1; i++) {
                    Log.debug("add to queue: [" + i + "]=\"" + found[i] + "\"");
                    result.add(found[i]);
                }
            } else {
                Log.debug("NOT splitted string: \"" + data + "\"!");
            }
            Log.debug("add to buffer rest: [" + (found.length - 1) + "]=\"" + found[found.length - 1] + "\"");
            buffer.add(found[found.length - 1]);
        } else {
            buffer.add(data);
        }

    }

    private void extractMessage() {
        String bufferContent = String.join("", buffer);
        String delimiter = settingsService.get(Propery.Delimiter);
        Log.debug("add rest to queue=\"" + String.join("", buffer) + "\"");
        if (delimiter != null && !delimiter.isEmpty()) {
            int i = 0;
            for (String message : bufferContent.split(delimiter, -1)) {
                Log.debug("add rest to queue[" + i + "]=\"" + message + "\"");
                i++;
                result.add(message);
            }
        } else {
            result.add(bufferContent);
        }


        buffer.clear();
        lastAddTime = LocalTime.now();
    }

    private boolean hasResult() {
        return !result.isEmpty();
    }

    private String getResult() {
        if (!result.isEmpty()) {
            return result.poll();
        } else {
            throw new IllegalStateException("No result to retrieve!");
        }
    }
}
