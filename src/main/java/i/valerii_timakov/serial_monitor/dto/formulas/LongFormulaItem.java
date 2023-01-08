package i.valerii_timakov.serial_monitor.dto.formulas;

import i.valerii_timakov.serial_monitor.utils.BytesStream;

public class LongFormulaItem extends IntegerFormulaItem<Long> {

    public LongFormulaItem(byte length, String name, String modifier, String conversion) {
        super(FormulaItemType.Long, length, name, modifier, conversion);
    }

    @Override
    protected Long readNumber(BytesStream bytesStream) {
        long result = 0;
        for (int i = 0; i < 64; i+= 8) {
            if (reverseBytes) {
                result |= (long) bytesStream.readByte() << i;
            } else {
                result |= (long) bytesStream.readByte() << (56 - i);
            }
        }
        return result;
    }
}
