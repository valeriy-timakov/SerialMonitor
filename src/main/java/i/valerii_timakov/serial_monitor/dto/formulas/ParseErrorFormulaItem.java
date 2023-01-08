package i.valerii_timakov.serial_monitor.dto.formulas;

import i.valerii_timakov.serial_monitor.utils.BytesStream;

import java.io.IOException;
import java.util.stream.Collectors;

public class ParseErrorFormulaItem extends FormulaItem {

    private final String message;

    public ParseErrorFormulaItem(FormulaItemType type, byte length, String name, String modifier, String conversion, String message) {
        super(type, length, name, modifier, conversion);
        this.message = message;
    }

    @Override
    public int getSize() {
        if (type != null) {
            return super.getSize();
        } else {
            return 0;
        }
    }

    @Override
    public void appendRead(StringBuilder result, BytesStream bytesStream) throws IOException {
        if (type != null && length > 0) {
            bytesStream.shift(getSize());
        }
        result.append(message != null ? message : "ERROR parsing formula! Parsed data: type=");
        result.append(type != null ? type.name() : "null");
        result.append(", length=");
        result.append(length);
        result.append(", name=");
        result.append(name);
        result.append(", modifier=");
        result.append(modifier);
        result.append(", conversions=");
        result.append(conversions.stream().collect(Collectors.joining("_")));
    }


    @Override
    public String readItem(BytesStream bytesStream) {
        throw new UnsupportedOperationException();
    }
}
