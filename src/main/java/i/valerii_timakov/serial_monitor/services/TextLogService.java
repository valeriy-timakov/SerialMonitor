package i.valerii_timakov.serial_monitor.services;

import i.valerii_timakov.serial_monitor.dto.Message;
import i.valerii_timakov.serial_monitor.utils.Log;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class TextLogService extends Service<String> implements TextMessageConsumer {

    private boolean stop = false;

    LinkedBlockingQueue<Message> messagesQueue = new LinkedBlockingQueue<>();

    private final List<Consumer<Message>> messageConsumers = new ArrayList<>();

    @Override
    public void consume(String value, boolean incoming) {
        Log.debug("TextLogService: got %1$s message: %2$s", incoming ? "in" : "out", value);
        Message message = new Message(value, LocalDateTime.now(), incoming);
        messagesQueue.add(message);
    }

    public void addMessageConsumer(Consumer<Message> consumer) {
        messageConsumers.add(consumer);
    }

    @Override
    protected Task<String> createTask() {
        Task<String>  task = new Task<>() {
            @Override
            protected String call() throws Exception {

                while (!stop) {
                    Log.debug("TextLogService: service task loop");
                    Message message = messagesQueue.take();
                        Platform.runLater(() -> messageConsumers.forEach(consumer -> {
                            try {
                                consumer.accept(message);
                            } catch (Throwable t) {
                                Log.error("TextLogService: Error processing message in consumer!", t);
                            }
                        }));
                    Log.debug("TextLogService: updating value and exiting");
                }

                return null;
            }
        };
        task.setOnFailed(event -> {
            Log.error("TextLogService: task error!", task.getException());
            if (task.getException() instanceof InterruptedException){
                return;
            }
            restart();
        });
        return task;
    }
}
