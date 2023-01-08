package i.valerii_timakov.serial_monitor.dto.formulas;

import i.valerii_timakov.serial_monitor.utils.BytesStream;

public class ByteFormulaItem extends IntegerFormulaItem<Byte> {

    public ByteFormulaItem(byte length, String name, String modifier, String conversion) {
        super(FormulaItemType.Byte, length, name, modifier, conversion);
    }

    @Override
    protected Byte readNumber(BytesStream bytesStream) {
        return bytesStream.readByte();
    }
}
