package i.valerii_timakov.serial_monitor.services;

import i.valerii_timakov.serial_monitor.dto.Propery;
import i.valerii_timakov.serial_monitor.utils.Log;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.Queue;

@RequiredArgsConstructor
public class InputScanner  {
    @NonNull
    private final TextMessageConsumer wrappedConsumer;
    private final Queue<String> result = new LinkedList<>();
    private StringBuilder buffer = new StringBuilder();
    private final SettingsService settingsService;
    private LocalTime lastAddTime = null;

    public void consume(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        checkToFlushBuffer();
        add(message);
        checkToFlushResult();
    }

    public void idle() {
        checkToFlushBuffer();
        checkToFlushResult();
    }

    private void checkToFlushBuffer() {
        if (lastAddTime == null) {
            lastAddTime = LocalTime.now();
        } else {
            int maxMessageWaitMillis = settingsService.get(Propery.MessageDelay);
            if (Duration.between(lastAddTime, LocalTime.now()).toMillis() > maxMessageWaitMillis){
                extractMessage();
            }
        }

    }

    private void add(String data) {
        String delimiter = settingsService.get(Propery.Delimiter);
        if (delimiter != null && !delimiter.isEmpty()) {
            String[] found = data.split(delimiter, -1);
            Log.debug("splitting by delimiter: " + String.join(";", found));
            if (found.length > 1) {
                Log.debug("add to buffer for extracting: [0]=\"" + found[0] + "\"");
                buffer.append(found[0]);
                extractMessage();
                for (int i = 1; i < found.length - 1; i++) {
                    Log.debug("add to queue: [" + i + "]=\"" + found[i] + "\"");
                    result.add(found[i]);
                }
            } else {
                Log.debug("NOT splitted string: \"" + data + "\"!");
            }
            Log.debug("add to buffer rest: [" + (found.length - 1) + "]=\"" + found[found.length - 1] + "\"");
            buffer.append(found[found.length - 1]);
        } else {
            buffer.append(data);
        }
    }

    private void extractMessage() {
        if (buffer.isEmpty()) {
            return;
        }
        String bufferContent = buffer.toString();
        String delimiter = settingsService.get(Propery.Delimiter);
        Log.debug("add rest to queue=\"" + bufferContent + "\"");
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


        buffer = new StringBuilder();
        lastAddTime = LocalTime.now();
    }

    private void checkToFlushResult() {
        if (!result.isEmpty()) {
            wrappedConsumer.consume(result.poll(), true);
        }
    }
}
