package i.valerii_timakov.serial_monitor.controllers;

import i.valerii_timakov.serial_monitor.controllers.select_wrappers.ItemWrapper;
import i.valerii_timakov.serial_monitor.dto.Propery;
import i.valerii_timakov.serial_monitor.services.SettingsService;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService service;
    private final CheckBox rawLogSaveToFileCheckbox;
    private final TextField rawLogFileEdit;
    private final Button rawLogFilePathSelectButton;
    private final ChoiceBox<ItemWrapper<String>> delimiterSelect;
    private final CheckBox addTimestampCheckbox;
    private final CheckBox addDirectionCheckbox;
    private final TextField messageWaitTimeoutEdit;
    private final ChoiceBox<ItemWrapper<Integer>> wordSizeSelect;

    private final List<ItemWrapper<String>> endItemWrappers = Arrays.asList(
        new ItemWrapper<>("\n", "NL"),
        new ItemWrapper<>("\r", "CR"),
        new ItemWrapper<>("\r\n", "CR & NL"),
        new ItemWrapper<>("\u0000", "ZERO"),
        new ItemWrapper<>("", "NONE")
    );

    private final List<ItemWrapper<Integer>> wordSizeItemWrappers = Arrays.asList(
        new ItemWrapper<>(1, "Byte"),
        new ItemWrapper<>(2, "Short"),
        new ItemWrapper<>(4, "Integer"),
        new ItemWrapper<>(8, "Long")
    );

    public void init(Stage stage) {

        addTimestampCheckbox.setOnAction(e ->
            service.set(Propery.AddTimestampToTextMessageLog, addTimestampCheckbox.isSelected()));
        addTimestampCheckbox.setSelected(service.get(Propery.AddTimestampToTextMessageLog));

        addDirectionCheckbox.setOnAction(e ->
            service.set(Propery.AddDirectionToTextMessageLog, addDirectionCheckbox.isSelected()));
        addDirectionCheckbox.setSelected(service.get(Propery.AddDirectionToTextMessageLog));

        messageWaitTimeoutEdit.setOnAction(e ->
            service.set(Propery.MaxMessageWaitSeconds, Integer.valueOf(messageWaitTimeoutEdit.getText())));
        messageWaitTimeoutEdit.setText(Integer.toString(service.get(Propery.MaxMessageWaitSeconds)));

        //communicating messages delimiter
        delimiterSelect.getItems().setAll(endItemWrappers);
        delimiterSelect.setOnAction(event ->
            service.set(Propery.Delimiter, delimiterSelect.getValue().getValue()));
        delimiterSelect.setValue(endItemWrappers.stream()
            .filter(w -> w.getValue().equals(service.get(Propery.Delimiter))).findAny().orElse(endItemWrappers.get(0)));
/*
        wordSizeSelect.getItems().setAll(wordSizeItemWrappers);
        wordSizeSelect.setOnAction(e -> service.set(Propery.WordSize, wordSizeSelect.getValue().getValue()));
        wordSizeSelect.setValue(wordSizeItemWrappers.stream()
            .filter(w -> service.get(Propery.WordSize).equals(w.getValue())).findAny().orElse(wordSizeItemWrappers.get(0)));
*/

        //binary log save path
        rawLogSaveToFileCheckbox.setOnAction(event -> updateMaybeConversationLogPath());
        rawLogFileEdit.setOnKeyPressed(event -> {
            if (KeyCode.ENTER.equals(event.getCode())){
                updateMaybeConversationLogPath();
            }
        });
        rawLogFilePathSelectButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select file");
            if (rawLogFileEdit.getText() != null && !rawLogFileEdit.getText().isEmpty()) {
                File file = new File(rawLogFileEdit.getText());
                fileChooser.setInitialDirectory(file.getParentFile());
            }
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                rawLogFileEdit.setText(selectedFile.getAbsolutePath());
            }
            updateMaybeConversationLogPath();
        });
        Optional<String> mclpvalue = service.get(Propery.MaybeConversationLogPath);
        rawLogSaveToFileCheckbox.setSelected(mclpvalue.isPresent());
        rawLogFileEdit.setText(mclpvalue.orElse(""));
    }

    private void updateMaybeConversationLogPath() {
        String pathValue = rawLogFileEdit.getText();
        if (rawLogSaveToFileCheckbox.isSelected() && pathValue != null && !pathValue.isEmpty()) {
            service.set(Propery.MaybeConversationLogPath, Optional.of(Path.of(pathValue)));
        } else {
            service.set(Propery.MaybeConversationLogPath, Optional.empty());
        }
    }


}
