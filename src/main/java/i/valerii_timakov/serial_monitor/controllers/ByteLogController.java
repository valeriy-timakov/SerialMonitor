package i.valerii_timakov.serial_monitor.controllers;

import i.valerii_timakov.serial_monitor.controls.WordView;
import i.valerii_timakov.serial_monitor.dto.WordMessage;
import i.valerii_timakov.serial_monitor.services.ByteLogService;
import javafx.application.Platform;
import javafx.scene.layout.TilePane;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class ByteLogController {
    private final TilePane byteHexOutput;
    private final TilePane byteCustomOutput;
    private final ByteLogService service;
    private Set<WordView> selectesWords = new HashSet<>();

    public void init() {
        service.addWordConsumer(wordMessage -> {
            Platform.runLater(() -> addData(wordMessage));
        });
    }

    private byte[] getValue(WordView view, byte length, byte offset) {
        byte[] result = new byte[length];
        int pos = byteHexOutput.getChildren().indexOf(view);
        int count = 0;
        //byteHexOutput.getChildren().stream().map(WordView.class::cast).
        return result;
    }

    private void addData(WordMessage wordMessage) {
        final WordView text = new WordView(wordMessage);
        text.setStyle("-fx-border-color: blue;");
        text.setSelectedChangeListener(selected -> {
            if (selected) {
                selectesWords.add(text);
            } else {
                selectesWords.remove(text);
            }
        });
        byteHexOutput.getChildren().add(text);
    }
}
