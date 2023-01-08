package i.valerii_timakov.serial_monitor.dto.formulas;

import i.valerii_timakov.serial_monitor.utils.BytesStream;

public class IntFormulaItem extends IntegerFormulaItem<Integer> {

    public IntFormulaItem(byte length, String name, String modifier, String conversion) {
        super(FormulaItemType.Int, length, name, modifier, conversion);
    }

    @Override
    protected Integer readNumber(BytesStream bytesStream) {
        int result = 0;
        for (int i = 0; i < 32; i+= 8) {
            if (reverseBytes) {
                result |= (long) bytesStream.readByte() << i;
            } else {
                result |= (long) bytesStream.readByte() << (24 - i);
            }
        }
        return result;
    }

}
