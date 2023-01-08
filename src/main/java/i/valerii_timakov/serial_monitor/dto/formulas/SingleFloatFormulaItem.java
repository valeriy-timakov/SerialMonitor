package i.valerii_timakov.serial_monitor.dto.formulas;

import i.valerii_timakov.serial_monitor.utils.BytesStream;

import java.nio.ByteBuffer;

public class SingleFloatFormulaItem extends FloatFormulaItem<Float> {

    public SingleFloatFormulaItem(byte length, String name, String modifier, String conversion) {
        super(FormulaItemType.Float, length, name, modifier, conversion);
    }

    @Override
    protected Float readNumber(BytesStream bytesStream) {
        byte[] buff = new byte[4];
        for (int i = 0; i < buff.length; i++) {
            if (reverseBytes) {
                buff[i] = bytesStream.readByte();
            } else {
                buff[buff.length - 1 - i] = bytesStream.readByte();
            }
        }
        return ByteBuffer.wrap(buff).getFloat();
    }
}
