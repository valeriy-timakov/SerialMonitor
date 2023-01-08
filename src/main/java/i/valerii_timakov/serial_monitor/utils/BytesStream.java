package i.valerii_timakov.serial_monitor.utils;

import i.valerii_timakov.serial_monitor.errors.NotEnoughDataException;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

@RequiredArgsConstructor
public class BytesStream extends InputStream {

    private final byte[] data;
    private final BytesReader bytesReader;
    private int position = 0;

    public byte readByte() {
        byte result;
        if (position < data.length) {
            result = data[position];
        } else {
            result = bytesReader.get(position);
        }
        position++;
        return result;
    }

    public byte[] readArrayWithoutShift(int length) {
        if (position + length < data.length) {
            return Arrays.copyOfRange(data, position, position + length);
        } else {
            byte[] result = new byte[length];
            for (int i = 0; i < result.length; i++) {
                result[i] = bytesReader.get(position + i);
            }
            return result;
        }
    }

    public void shift(int count) {
        position += count;
    }

    public boolean hasNext() {
        return  bytesReader.hasItem(position);
    }

    @Override
    public int read() throws IOException {
        try {
            return readByte();
        } catch (NotEnoughDataException e) {
            return 256;
        }
    }
}
