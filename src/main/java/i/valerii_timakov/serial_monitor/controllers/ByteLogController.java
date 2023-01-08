package i.valerii_timakov.serial_monitor.controllers;

import i.valerii_timakov.serial_monitor.controls.ByteView;
import i.valerii_timakov.serial_monitor.controls.ParsedView;
import i.valerii_timakov.serial_monitor.dto.ByteMessage;
import i.valerii_timakov.serial_monitor.errors.NotEnoughDataException;
import i.valerii_timakov.serial_monitor.services.ByteLogService;
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.FlowPane;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ByteLogController {

    private static final int PARSED_VIEW_HEIGHT_CHANGE = 200;
    private final FlowPane byteHexOutput;
    private final ScrollPane byteHexOutputScrool;
    private final SplitPane byteCustomOutput;
    private final ScrollPane byteCusomOutputScrool;
    private final ByteLogService service;
    private List<ByteView> lastHighlighted;

    private final Map<ByteView, ParsedView> representationsMap = new HashMap<>();

    public void init() {
        service.addWordConsumer(byteMessage -> {
            Platform.runLater(() -> addData(byteMessage));
        });
        byteHexOutputScrool.viewportBoundsProperty().addListener((observable, oldValue, newValue) -> {
            byteHexOutput.setPrefWidth(newValue.getWidth());
            byteHexOutput.setPrefHeight(newValue.getHeight());
            byteHexOutput.setPrefWrapLength(newValue.getWidth());
        });
        byteCusomOutputScrool.viewportBoundsProperty().addListener((observable, oldValue, newValue) -> {
            byteCustomOutput.setPrefWidth(newValue.getWidth());
            byteCustomOutput.setPrefHeight(newValue.getHeight());
        });
    }

    private void addData(ByteMessage byteMessage) {
        final ByteView text = new ByteView(byteMessage);
        text.setSelectedChangeListener(selected -> {
            if (selected) {
                ParsedView parsedView = new ParsedView(len -> {
                    updateHieghlidhted(text, len);
                    return getValue(text, len);
                }, new BytesReader(text));
                representationsMap.put(text, parsedView);
                byteCustomOutput.getItems().add(parsedView);
                byteCustomOutput.setMinHeight(byteCustomOutput.getHeight() + PARSED_VIEW_HEIGHT_CHANGE);
            } else {
                ParsedView parsedView = representationsMap.get(text);
                if (parsedView != null) {
                    byteCustomOutput.getItems().remove(parsedView);
                    representationsMap.remove(text);
                    byteCustomOutput.setMinHeight(byteCustomOutput.getHeight() - PARSED_VIEW_HEIGHT_CHANGE);
                }
                removeHighlighted(text);
            }
        });
        byteHexOutput.getChildren().add(text);
    }

    private class BytesReader implements i.valerii_timakov.serial_monitor.utils.BytesReader {

        private final int startPos;

        public BytesReader(ByteView view) {
            startPos = byteHexOutput.getChildren().indexOf(view);
        }

        public byte get(int pos) {
            int shiftedPos = startPos + pos;
            if (byteHexOutput.getChildren().size() <= shiftedPos) {
                throw new NotEnoughDataException("There is not enough bytes for provided position! Position: " + pos);
            }
            ByteView nextView = (ByteView) byteHexOutput.getChildren().get(shiftedPos);
            return nextView.getMessage().getValue();
        }

        public boolean hasItem(int pos) {
            int shiftedPos = startPos + pos;
            return byteHexOutput.getChildren().size() > shiftedPos;
        }

    }

    private byte[] getValue(ByteView view, int length) {
        byte[] result = new byte[length];
        if (length < 1) {
            return result;
        }
        int pos = byteHexOutput.getChildren().indexOf(view);
        int count = 0;
        result[count] = view.getMessage().getValue();
        count++;
        ByteView nextView;
        while (count < length) {
            pos++;
            if (byteHexOutput.getChildren().size() <= pos) {
                throw new IllegalArgumentException("There is not enough bytes for provided length! Length: " + length);
            }
            nextView = (ByteView) byteHexOutput.getChildren().get(pos);
            result[count] = nextView.getMessage().getValue();
            count++;
        }
        //byteHexOutput.getChildren().stream().map(WordView.class::cast).
        return result;
    }

    private Map<ByteView, Integer> highlightedCount = new HashMap<>();

    private void updateHieghlidhted(ByteView startByteView, int len) {
        if (lastHighlighted != null) {
            lastHighlighted.forEach(bv -> bv.setCurrent(false));
        }
        lastHighlighted = new LinkedList<>();
        int startPos = byteHexOutput.getChildren().indexOf(startByteView);
        int nextByteNo = 1;
        if (startPos >= 0) {
            while (nextByteNo < len) {
                ByteView tmpByteView = (ByteView) byteHexOutput.getChildren().get(startPos + nextByteNo);
                tmpByteView.setHighlighed(true);
                tmpByteView.setCurrent(true);
                highlightedCount.put(startByteView, len);
                lastHighlighted.add(tmpByteView);
                nextByteNo++;
            }
            Integer lastCount = highlightedCount.get(startByteView);
            if (lastCount != null && lastCount > len) {
                while (nextByteNo < lastCount) {
                    ByteView tmpByteView = (ByteView) byteHexOutput.getChildren().get(startPos + nextByteNo);
                    tmpByteView.setHighlighed(false);
                    nextByteNo++;
                }
            }
        }
    }

    private void removeHighlighted(ByteView startItem) {
        int startPos = byteHexOutput.getChildren().indexOf(startItem);
        Integer lastCount = highlightedCount.get(startItem);
        if (lastCount != null && startPos != -1) {
            int nextByteNo = 1;
            while (nextByteNo < lastCount) {
                ByteView tmpByteView = (ByteView) byteHexOutput.getChildren().get(startPos + nextByteNo);
                tmpByteView.setHighlighed(false);
                nextByteNo++;
            }
        }

    }
}
