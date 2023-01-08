package i.valerii_timakov.serial_monitor.controls;

import i.valerii_timakov.serial_monitor.dto.ByteMessage;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.function.Consumer;

public class ByteView extends TextFlow {

    private static final String CONTAINER_CLASS = "word_view";
    private static final String ITEM_CLASS = "word";
    private static final String SELECTED_CLASS = "selected";
    private static final String INCOMING_CLASS = "incoming";
    private static final String OUTCOMING_CLASS = "outcoming";
    private static final String HIGHLIGHTED_CLASS = "highlighted";
    private static final String CURRENT_CLASS = "current";

    @Setter
    private Consumer<Boolean> selectedChangeListener;

    @Getter
    private boolean selected = false;
    @Getter
    private final ByteMessage message;
    private Text textNode;
    private boolean highlighed;
    private boolean current;

    public ByteView(@NonNull ByteMessage byteMessage) {
        super(new Text(stringify(byteMessage.getValue())));
        textNode = (Text)getChildren().get(0);
        this.message = byteMessage;
        setOnMouseClicked(event -> {
            onSelectChanged();
        });
        calculateClass();
    }

    private void onSelectChanged() {
        selected = !selected;
        calculateClass();
        if (selectedChangeListener != null) {
            selectedChangeListener.accept(selected);
        }
    }

    private void calculateClass() {
        getStyleClass().clear();
        textNode.getStyleClass().clear();
        getStyleClass().add(CONTAINER_CLASS);
        textNode.getStyleClass().add(ITEM_CLASS);
        if (message.isIncoming()) {
            getStyleClass().add(INCOMING_CLASS);
            textNode.getStyleClass().add(INCOMING_CLASS);
        } else {
            getStyleClass().add(OUTCOMING_CLASS);
            textNode.getStyleClass().add(OUTCOMING_CLASS);
        }
        if (selected) {
            textNode.getStyleClass().add(SELECTED_CLASS);
            getStyleClass().add(SELECTED_CLASS);
        }
        if (highlighed) {
            textNode.getStyleClass().add(HIGHLIGHTED_CLASS);
            getStyleClass().add(HIGHLIGHTED_CLASS);
        }
        if (current) {
            textNode.getStyleClass().add(CURRENT_CLASS);
            getStyleClass().add(CURRENT_CLASS);
        }
    }

    private static String stringify(byte value) {
        char[] result = new char[2];
        result[0] = Character.forDigit((value >> 4) & 0xf, 16);
        result[1] = Character.forDigit(value & 0xf, 16);
        return new String(result);
    }

    public void setHighlighed(boolean highlighed) {
        this.highlighed = highlighed;
        calculateClass();
    }

    public void setCurrent(boolean current) {
        this.current = current;
        calculateClass();
    }
}
