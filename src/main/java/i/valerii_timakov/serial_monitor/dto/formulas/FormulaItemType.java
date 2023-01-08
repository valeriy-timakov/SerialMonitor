package i.valerii_timakov.serial_monitor.dto.formulas;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public enum FormulaItemType {
    Byte(1, ByteFormulaItem::new),
    Short(2, ShortFormulaItem::new),
    Int(4, IntFormulaItem::new),
    Long(8, LongFormulaItem::new),
    Float(4, SingleFloatFormulaItem::new),
    Double(8, DoubleFormulaItem::new),
    Char(2, CharFormulaItem::new);
    @Getter
    private final byte size;
    @Getter
    private final FormulaItem.Creator creator;

    FormulaItemType(int size, FormulaItem.Creator creator) {
        this.size = (byte)size;
        this.creator = creator;
    }
}
