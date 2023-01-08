package i.valerii_timakov.serial_monitor.dto.formulas;

import i.valerii_timakov.serial_monitor.utils.BytesStream;

public class ShortFormulaItem extends IntegerFormulaItem<Short> {

    public ShortFormulaItem(byte length, String name, String modifier, String conversion) {
        super(FormulaItemType.Short, length, name, modifier, conversion);
    }

    @Override
    protected Short readNumber(BytesStream bytesStream) {
        if (reverseBytes) {
            return (short) (bytesStream.readByte() | bytesStream.readByte() << 8);
        } else {
            return (short) (bytesStream.readByte() << 8 | bytesStream.readByte());
        }
    }
}
