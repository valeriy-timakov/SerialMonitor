package i.valerii_timakov.serial_monitor.controllers;

import i.valerii_timakov.serial_monitor.dto.Message;
import i.valerii_timakov.serial_monitor.dto.Propery;
import i.valerii_timakov.serial_monitor.services.SettingsService;
import i.valerii_timakov.serial_monitor.services.TextLogService;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
public class TileMessagesController {

    @Setter
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSS");

    private final TilePane textMessagesTiles;
    private final TextLogService textLogService;
    private final SettingsService settingsService;

    public void init() {
        textLogService.addMessageConsumer(this::consume);

    }

    private void consume(Message message) {

        VBox pane = new VBox();
        HBox title = null;
        boolean addTimestampToTextMessageLog = settingsService.get(Propery.AddTimestampToTextMessageLog),
            addDirectionToTextMessageLog = settingsService.get(Propery.AddDirectionToTextMessageLog);
        if (addTimestampToTextMessageLog || addDirectionToTextMessageLog) {
            title = new HBox();
            pane.getChildren().add(title);
        }
        if (addTimestampToTextMessageLog) {
            Text timeText = new Text(message.getTime().format(dateTimeFormatter));
            title.getChildren().add(timeText);
        }

        if (addDirectionToTextMessageLog) {
            Text directioImg = new Text(message.isIncoming() ? ">>>>>>" : "<<<<<<");
            title.getChildren().add(directioImg);
        }

        Text messageText = new Text(message.getValue());
        pane.getChildren().add(messageText);

        textMessagesTiles.getChildren().add(pane);

    }
}
