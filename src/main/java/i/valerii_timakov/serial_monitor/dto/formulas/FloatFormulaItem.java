package i.valerii_timakov.serial_monitor.dto.formulas;

import i.valerii_timakov.serial_monitor.utils.BytesStream;

import java.util.Locale;

public abstract class FloatFormulaItem <T extends Number> extends FormulaItem {
    public FloatFormulaItem(FormulaItemType type, byte length, String name, String modifier, String conversion) {
        super(type, length, name, modifier, conversion);
    }

    @Override
    public String readItem(BytesStream bytesStream) {
        T number = readNumber(bytesStream);
        String _modifier = modifier;
        if (_modifier == null) {
            _modifier = "";
        }
        if (!_modifier.contains("e") && !_modifier.contains("f")) {
            _modifier = _modifier + "f";
        }
        return String.format(Locale.ENGLISH, "%" + _modifier, number);
    }

    protected abstract T readNumber(BytesStream bytesStream);


}
