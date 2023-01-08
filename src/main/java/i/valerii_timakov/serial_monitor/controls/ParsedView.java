package i.valerii_timakov.serial_monitor.controls;

import i.valerii_timakov.serial_monitor.dto.formulas.FormulaItem;
import i.valerii_timakov.serial_monitor.dto.formulas.FormulaItemType;
import i.valerii_timakov.serial_monitor.dto.formulas.ParseErrorFormulaItem;
import i.valerii_timakov.serial_monitor.utils.BytesReader;
import i.valerii_timakov.serial_monitor.utils.BytesStream;
import i.valerii_timakov.serial_monitor.utils.Log;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ParsedView extends SplitPane {

    private static final String RESULT_VIEW_CLASS = "result_view";
    private static final String PARSED_VIEW_CLASS = "parsed_view";
    private static final String ERROR_VIEW_CLASS = "error_view";
    private final TextArea formulaEdit = new TextArea();
    private final TextArea parseResult = new TextArea();
    private final Pattern formulaPattern = Pattern.compile(
        "^(?<type>Byte|Short|Int|Long|Float|Double|Char)(-(?<conversion>((rb|ra)_)+))?(\\[(?<length>\\d+)])?(\\{(?<name>\\w+)})?(\\|(?<modifier>[\\w_\\-.,]+))?$");
    private final Function<Integer, byte[]> bytesProvider;
    private final BytesReader bytesReader;
    private String error;

    public ParsedView(@NonNull Function<Integer, byte[]> bytesProvider, BytesReader bytesReader) {
        this.bytesProvider = bytesProvider;
        this.bytesReader = bytesReader;
        getItems().addAll(formulaEdit, parseResult);
        formulaEdit.setMinWidth(100);
        formulaEdit.setMinHeight(80);
        parseResult.setMinWidth(100);
        parseResult.setMinHeight(80);
        parseResult.setDisable(true);
        layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            formulaEdit.setPrefWidth(newValue.getWidth() * 0.4);
            formulaEdit.setPrefHeight(newValue.getHeight());
            parseResult.setPrefWidth(newValue.getWidth() * 0.6);
            parseResult.setPrefHeight(newValue.getHeight());
        });
        formulaEdit.setOnMouseExited(event -> calculateForm());
    }

    private void calculateForm() {
        String formulaText = formulaEdit.getText();
        error = "";
        if (StringUtils.isEmpty(formulaText)) {
            error += "Formula is empty!";
        }
        String[] lines = formulaText.replaceAll("\\s+", "").split(";");
        StringBuilder textView = new StringBuilder();
        try {
            List<FormulaItem> formula = Arrays.stream(lines).map(this::parse)
                    .filter(Objects::nonNull).collect(Collectors.toList());
            int totalLength = formula.stream()
                    .map(item -> (int) item.getSize())
                    .reduce(0, (acc, size) -> acc + size);
            byte[] data = bytesProvider.apply(totalLength);
            BytesStream is = new BytesStream(data, bytesReader);
            for (FormulaItem f : formula) {
                f.appendRead(textView, is);
                textView.append(";\n");
            }
        } catch (Throwable e) {
            error += e.getMessage();
            Log.error("Error processing formula!", e);
        }
        parseResult.getStyleClass().clear();
        parseResult.getStyleClass().add(RESULT_VIEW_CLASS);
        if (error.isEmpty()) {
            parseResult.getStyleClass().add(PARSED_VIEW_CLASS);
            parseResult.setText(textView.toString());
        } else {
            parseResult.getStyleClass().add(ERROR_VIEW_CLASS);
            parseResult.setText(error);
        }
    }

    private FormulaItem parse(String line) {
        Matcher matcher = formulaPattern.matcher(line);
        byte length = 0;
        String name = null, modifier = null, conversion = null;
        FormulaItemType type = null;
        if (matcher.find()) {
            try {
                String lengthLine = matcher.group("length");
                length = lengthLine != null ? Byte.valueOf(lengthLine) : 1;
                name = matcher.group("name");
                modifier = matcher.group("modifier");
                conversion = matcher.group("conversion");
                type = FormulaItemType.valueOf(matcher.group("type"));
                return FormulaItem.crearte(type, length, name, modifier, conversion);
            } catch (Throwable e) {
                return  new ParseErrorFormulaItem(type, length, name, modifier, conversion,
                    "Error creating formula item! " + e.getMessage());
            }
        }
        return new ParseErrorFormulaItem(type, length, name, modifier, conversion,
            "Error creating formula item! Formula line not matches pattern! Line: " + line);
    }
}
