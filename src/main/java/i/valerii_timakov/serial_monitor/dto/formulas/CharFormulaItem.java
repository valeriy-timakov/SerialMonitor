package i.valerii_timakov.serial_monitor.dto.formulas;

import i.valerii_timakov.serial_monitor.utils.BytesStream;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CoderResult;
import java.nio.charset.UnsupportedCharsetException;

public class CharFormulaItem extends FormulaItem {

    private final Charset charset;

    public CharFormulaItem(byte length, String name, String modifier, String conversion) {
        super(FormulaItemType.Char, length, name, modifier, conversion);
        try {
            charset = StringUtils.isNotEmpty(modifier) ? Charset.forName(modifier) : Charset.defaultCharset();
        } catch (UnsupportedCharsetException e) {
            throw new UnsupportedCharsetException("Unsupported charset! Value: '" + e.getCharsetName() + "'");
        }
    }

    @Override
    protected void appendValue(StringBuilder result, BytesStream bytesStream) throws IOException {
        char[] buff = new char[length];
        CharBuffer cb = CharBuffer.wrap(buff);
        ByteBuffer bb = ByteBuffer.wrap(bytesStream.readArrayWithoutShift(
            (int) (length * charset.newDecoder().maxCharsPerByte() + 1)));
        CoderResult coderResult = charset.newDecoder().decode(bb, cb, false);
        checkError(coderResult, result);
        int bytesCount = charset.newEncoder().encode(cb).position();
        bytesStream.shift(bytesCount);
        result.append("\"");
        result.append(new String(buff));
        if (coderResult.isUnderflow()) {
            result.append("... [not enough bytes!]");
        }
        result.append("\"");
    }

    private void checkError(CoderResult coderResult, StringBuilder result) {
        String error = null;
        if (coderResult.isError()) {
            error = "ERROR";
        }
        if (coderResult.isMalformed()) {
            error = "Malformed error";
        }
        if (coderResult.isUnmappable()) {
            error = "Unmappable error";
        }
        if (error != null) {
            result.append("[");
            result.append(error);
            result.append("! " + coderResult + "] Result: ");
        }

    }

    @Override
    public String readItem(BytesStream bytesStream) {
        throw new NotImplementedException();
    }
}
