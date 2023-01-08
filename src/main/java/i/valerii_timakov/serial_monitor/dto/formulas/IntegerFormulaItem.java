package i.valerii_timakov.serial_monitor.dto.formulas;

import i.valerii_timakov.serial_monitor.utils.BytesStream;

import java.util.Arrays;

public abstract class IntegerFormulaItem<T extends Number> extends FormulaItem {

    private enum DigitModifier {BIN, OCT, DEC, HEX}

    protected DigitModifier digitModifier;

    public IntegerFormulaItem(FormulaItemType type, byte length, String name, String modifier, String conversion) {
        super(type, length, name, modifier, conversion);
        digitModifier = Arrays.stream(DigitModifier.values()).filter(dm -> dm.name().equals(modifier)).findAny().orElse(DigitModifier.DEC);
    }

    @Override
    public String readItem(BytesStream bytesStream) {
        T numberValue = readNumber(bytesStream);
        String result;
        if (digitModifier == DigitModifier.BIN) {
            result = Integer.toBinaryString(numberValue.intValue());
        } else {
            String mod;
            if (digitModifier == DigitModifier.OCT) {
                mod = "o";
            } else if (digitModifier == DigitModifier.DEC) {
                mod = "d";
            } else if (digitModifier == DigitModifier.HEX) {
                mod = "x";
            } else {
                throw new IllegalStateException("Undefined digital modifier!");
            }
            result = String.format("%" + mod, numberValue);
        }
        return result;
    }

    protected abstract T readNumber(BytesStream bytesStream);
}
