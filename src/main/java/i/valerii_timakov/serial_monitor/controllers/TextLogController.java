package i.valerii_timakov.serial_monitor.controllers;

import i.valerii_timakov.serial_monitor.dto.Message;
import i.valerii_timakov.serial_monitor.dto.Propery;
import i.valerii_timakov.serial_monitor.services.SettingsService;
import i.valerii_timakov.serial_monitor.services.TextLogService;
import i.valerii_timakov.serial_monitor.utils.Log;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
public class TextLogController {
    private final TextArea view;
    private final Button saveTextLogButton;
    private final Button clearTextLogButton;
    private final TextLogService textLogService;
    private final SettingsService settingsService;
    @Setter
    @NonNull
    private String inputDirectionMessage = "[>>>>>>] ";
    @Setter
    @NonNull
    private String outputDirectionMessage = "[<<<<<<] ";
    @Setter
    @NonNull
    private String messagesDelimiter = "\n";
    @NonNull
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSS - ");

    public void consume(Message message) {

        StringBuilder result = new StringBuilder();

        Log.debug("TextLogService: message extracted from queue: " + message.getValue());
        if (settingsService.get(Propery.AddTimestampToTextMessageLog)) {
            Log.debug("TextLogService: showTime");
            result.append(message.getTime().format(dateTimeFormatter));
        }
        if (settingsService.get(Propery.AddDirectionToTextMessageLog)) {
            Log.debug("TextLogService: showDirection");
            result.append(message.isIncoming() ? inputDirectionMessage : outputDirectionMessage);
        }
        result.append(message.getValue());
        result.append(messagesDelimiter);

        view.appendText(result.toString());

        int logCaretPosition = view.getLength();
        view.positionCaret(logCaretPosition);
    }

    public void init(Stage stage) {
        saveTextLogButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select file");
            File selectedFile = fileChooser.showSaveDialog(stage);
            try {
                save(selectedFile.toPath(), Charset.defaultCharset());
            } catch (IOException e) {
                Log.error("Error saving text log!", e);
            }
        });
        clearTextLogButton.setOnAction(event -> view.clear());
        textLogService.addMessageConsumer(this::consume);
    }

    public void save(Path filePath, Charset charset) throws IOException {
        Log.debug("TextLogService: save messages called");
        if (filePath.toFile().exists()) {
            throw new IOException("File already exists!");
        }
        Files.writeString(filePath, view.getText(), charset, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
