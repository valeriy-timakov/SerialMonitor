package i.valerii_timakov.serial_monitor.dto.formulas;

import i.valerii_timakov.serial_monitor.utils.BytesStream;

import java.nio.ByteBuffer;

public class DoubleFormulaItem extends FloatFormulaItem<Double> {

    public DoubleFormulaItem(byte length, String name, String modifier, String conversion) {
        super(FormulaItemType.Double, length, name, modifier, conversion);
    }

    @Override
    protected Double readNumber(BytesStream bytesStream) {
        byte[] buff = new byte[8];
        for (int i = 0; i < buff.length; i++) {
            if (reverseBytes) {
                buff[i] = bytesStream.readByte();
            } else {
                buff[buff.length - 1 - i] = bytesStream.readByte();
            }
        }
        return ByteBuffer.wrap(buff).getDouble();
    }
}
