package i.valerii_timakov.serial_monitor.controllers;

import i.valerii_timakov.serial_monitor.services.*;
import i.valerii_timakov.serial_monitor.utils.Log;
import javafx.scene.control.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
public class SendDataController {
    private final Button sendButton;
    private final TextField sendInput;
    private final CheckBox sendTextDataCheckbox;
    @NonNull
    private final UnidirectedMessageConsumer messageConsumer;
    private int historyPosition = 0;
    private final List<String> textSendHistory = new LinkedList<>();
    private final List<String> byteArrSendHistory = new LinkedList<>();
    private List<String> currSendHistory;
    private final Alert alert = new Alert(Alert.AlertType.NONE);

    public void init() {
        sendButton.setOnAction(e -> send());
        sendInput.setOnKeyPressed(keyEvent -> {
            switch (keyEvent.getCode()) {
                case DOWN -> moveSendToHistory(1);
                case UP -> moveSendToHistory(-1);
                case ENTER -> send();
            }
        });
        sendInput.setTextFormatter(new TextFormatter<>(change -> {
            if (!sendTextDataCheckbox.isSelected()) {
                change.setText(filterNotHexChars(change.getText()));
            }
            return change;
        }));
        sendTextDataCheckbox.setOnAction(event -> updateMode());
        sendTextDataCheckbox.setSelected(true);
        updateMode();
    }

    private void updateMode() {
        if (sendTextDataCheckbox.isSelected()) {
            currSendHistory = textSendHistory;
        } else {
            sendInput.setText(filterNotHexChars(sendInput.getText()));
            currSendHistory = byteArrSendHistory;
        }
        historyPosition = currSendHistory.size();
    }

    private void send() {
        String enteredText = sendInput.getText();
        try {
            if (sendTextDataCheckbox.isSelected()) {
                messageConsumer.consume(enteredText);
            } else {
                byte[] data = convertToBytes(enteredText);
                messageConsumer.consume(data, data.length);
            }
            currSendHistory.add(enteredText);
            historyPosition = currSendHistory.size();
            sendInput.setText("");
        } catch (Throwable t) {
            Log.error("Error message consumer!", t);
            showError("Error occured on sending data! " + t.getMessage());
        }
    }

    private byte[] convertToBytes(String value) {
        byte[] result = new byte[value.length()];
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch >= '0' && ch <= '9') {
                result[i] = (byte) (ch - '0');
            } else if (ch >= 'a' && ch <= 'f') {
                result[i] = (byte) (ch - 'a' + 10);
            } else {
                throw new IllegalArgumentException("Invalid char for HEG number! Char: " + ch);
            }
        }
        return result;
    }

    public void updateOpenedPortDataWrapper(Boolean connected) {
        setDisabled(!connected);
    }

    private void setDisabled(boolean value) {
        sendInput.setDisable(value);
        sendButton.setDisable(value);

    }

    private void moveSendToHistory(int shift) {
        historyPosition += shift;
        if (historyPosition > currSendHistory.size()) {
            historyPosition = currSendHistory.size();
        }
        if (historyPosition < 0) {
            historyPosition = 0;
        }
        sendInput.setText(historyPosition < currSendHistory.size() ? currSendHistory.get(historyPosition) : "");
    }

    private String filterNotHexChars(String value) {
        if (value == null) {
            return value;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char currChar = value.charAt(i);
            if (currChar >= '0' && currChar <= '9' || currChar >= 'a' && currChar <= 'f') {
                result.append(currChar);
            }
        }
        return result.toString();
    }

    private void showError(String message) {
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle("Error sending data!");
        alert.setContentText(message);
        alert.show();
    }
}
