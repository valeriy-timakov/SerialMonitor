package i.valerii_timakov.serial_monitor.dto.formulas;

import i.valerii_timakov.serial_monitor.utils.BytesStream;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
public abstract class FormulaItem {

    private static final String ARRAY_ITEMS_DELIMITER = ", ";
    protected final FormulaItemType type;
    protected final byte length;
    protected final String name;
    protected final String modifier;
    protected final List<String> conversions;
    protected final boolean reverseBytes;
    protected final boolean reverseArray;

    public FormulaItem(FormulaItemType type, byte length, String name, String modifier, String conversions) {
        this.type = type;
        this.length = length;
        this.name = name;
        this.modifier = modifier;
        this.conversions = conversions != null ? Arrays.asList(conversions.split("_")) : Collections.emptyList();
        this.reverseBytes = this.conversions.contains("rb");
        this.reverseArray = this.conversions.contains("ra");
    }

    public int getSize() {
        return type.getSize() * length;
    }

    public static FormulaItem crearte(@NonNull FormulaItemType type, byte length, String name, String modifier, String conversion) {
        return type.getCreator().apply(length, name, modifier, conversion);
    }

    protected interface Creator {
        FormulaItem apply(byte length, String name, String modifier, String conversion);
    }

    public void appendRead(StringBuilder result, BytesStream bytesStream) throws IOException {
        result.append(name != null ? name : "no_name");
        result.append("=");
        appendValue(result, bytesStream);
    }

    protected void appendValue(StringBuilder result, BytesStream bytesStream) throws IOException {
        if (length == 1) {
            result.append(readItem(bytesStream));
        } else {
            result.append("[");
            for (int i = 0; i < length; i++) {
                if (i > 0) {
                    result.append(ARRAY_ITEMS_DELIMITER);
                }
                result.append(readItem(bytesStream));
            }
            result.append("]");
        }
    }

    public abstract String readItem(BytesStream bytesStream);

}
