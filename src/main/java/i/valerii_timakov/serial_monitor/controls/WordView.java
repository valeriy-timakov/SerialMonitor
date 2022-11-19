package i.valerii_timakov.serial_monitor.controls;

import i.valerii_timakov.serial_monitor.dto.WordMessage;
import javafx.scene.text.Text;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.function.Consumer;

public class WordView extends Text {

    @Setter
    private Consumer<Boolean> selectedChangeListener;

    @Getter
    private boolean selected = false;
    private final WordMessage message;

    public WordView(@NonNull WordMessage wordMessage) {
        super(stringify(wordMessage.getWord()));
        this.message = wordMessage;
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

    private static final String ITEM_CLASS = "word";
    private static final String SELECTED_CLASS = "selected";
    private static final String UNSELECTED_CLASS = "unselected";
    private static final String INCOMING_CLASS = "incoming";
    private static final String OUTCOMING_CLASS = "outcoming";


    private void calculateClass() {
        getStyleClass().clear();
        getStyleClass().add(ITEM_CLASS);
        getStyleClass().add(selected ? SELECTED_CLASS : UNSELECTED_CLASS);
        getStyleClass().add(message.isIncoming() ? INCOMING_CLASS : OUTCOMING_CLASS);
    }

    private static String stringify(Number value) {
        int count;
        if (value instanceof Byte) {
            count = 1;
        } else if (value instanceof Short) {
            count = 2;
        } else if (value instanceof Integer) {
            count = 4;
        } else if (value instanceof Long) {
            count = 8;
        } else {
            throw new IllegalArgumentException("Wrong word type!");
        }
        char[] result = new char[count * 2];
        long tmpVal = value.longValue();
        for (int i = count - 1; i >= 0; i--) {
            result[2 * i + 1] = Character.forDigit((int) (tmpVal & 0xf), 16);
            tmpVal = tmpVal >> 4;
            result[2 * i] = Character.forDigit((int) (tmpVal & 0xf), 16);
            tmpVal = tmpVal >> 4;
        }
        return new String(result);
    }

}
