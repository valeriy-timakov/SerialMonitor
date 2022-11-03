package i.valerii_timakov.serial_monitor.serial_monitor;

import com.fazecast.jSerialComm.SerialPort;
import i.valerii_timakov.serial_monitor.serial_monitor.exceptions.PortException;
import i.valerii_timakov.serial_monitor.serial_monitor.select_wrappers.IntWrapper;
import i.valerii_timakov.serial_monitor.serial_monitor.select_wrappers.ItemWrapper;
import i.valerii_timakov.serial_monitor.serial_monitor.select_wrappers.PortWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class SerialMonitorController {

    @FXML
    private ChoiceBox<PortWrapper> portsSelect;
    @FXML
    private ChoiceBox<ItemWrapper<String>> appendSelect;
    @FXML
    private TextArea communicationOutput;
    @FXML
    private Button sendButton;
    @FXML
    private TextField sendInput;
    @FXML
    private Button openPortButton;
    @FXML
    private ChoiceBox<IntWrapper> baudRateSelect;
    @FXML
    private ChoiceBox<IntWrapper> dataBitsSelect;
    @FXML
    private ChoiceBox<ItemWrapper<Integer>> stopBitsSelect;
    @FXML
    private ChoiceBox<ItemWrapper<Integer>> paritySelect;
    @FXML
    private ChoiceBox<ItemWrapper<Integer>> flowControlSelect;
    @FXML
    private TitledPane settingsPane;
    @FXML
    private Button closeCurrentPortButton;

    private OpenedPortWrapper currentOpenedPortWrapper;
    private List<String> sendHistory = new LinkedList<>();
    private int logCaretPosition = 0;

    private final List<ItemWrapper<String>> endItemWrappers = Arrays.asList(
        new ItemWrapper<>("\n", "NL"),
        new ItemWrapper<>("\r", "CR"),
        new ItemWrapper<>("\r\n", "CR & NL"),
        new ItemWrapper<>("\u0000", "ZERO"),
        new ItemWrapper<>("", "NONE")
    );

    private final List<IntWrapper> baudRateItemWrappers = Stream.of(9600, 19200, 38400, 56000, 115200, 128000, 256000)
        .map(IntWrapper::new).collect(Collectors.toList());

    private final List<IntWrapper> dataBitsItemWrappers = Stream.of(5, 6, 7, 8, 9)
        .map(IntWrapper::new).collect(Collectors.toList());

    private final List<ItemWrapper<Integer>> stopBitsItemWrappers = Arrays.asList(
        new ItemWrapper<>(SerialPort.ONE_STOP_BIT, "1"),
        new ItemWrapper<>(SerialPort.ONE_POINT_FIVE_STOP_BITS, "1.5"),
        new ItemWrapper<>(SerialPort.TWO_STOP_BITS, "2")
    );

    private final List<ItemWrapper<Integer>> parityItemWrappers = Arrays.asList(
        new ItemWrapper<>(SerialPort.NO_PARITY, "None"),
        new ItemWrapper<>(SerialPort.EVEN_PARITY, "Even"),
        new ItemWrapper<>(SerialPort.ODD_PARITY, "Odd"),
        new ItemWrapper<>(SerialPort.MARK_PARITY, "Mark"),
        new ItemWrapper<>(SerialPort.SPACE_PARITY, "Space")
    );

    private final List<ItemWrapper<Integer>> controlFlowItemWrappers = Arrays.asList(
        new ItemWrapper<>(SerialPort.FLOW_CONTROL_DISABLED, "None"),
        new ItemWrapper<>(SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED, "XON/XOFF IN"),
        new ItemWrapper<>(SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED, "XON/XOFF OUT"),
        new ItemWrapper<>(SerialPort.FLOW_CONTROL_RTS_ENABLED, "RTS"),
        new ItemWrapper<>(SerialPort.FLOW_CONTROL_CTS_ENABLED, "CTS"),
        new ItemWrapper<>(SerialPort.FLOW_CONTROL_DSR_ENABLED, "DSR"),
        new ItemWrapper<>(SerialPort.FLOW_CONTROL_DTR_ENABLED, "DTR")
    );

    private static final class SettinggsData<T, W extends ItemWrapper<T>> {
        public final ChoiceBox<W> select;
        public final List<W> variants;
        public final Consumer<T> setter;
        public final Function<SerialPort, T> getter;
        public final Function<T, W> createWrapper;

        private SettinggsData(ChoiceBox<W> select, List<W> variants, Consumer<T> setter, Function<SerialPort, T> getter, Function<T, W> createWrapper) {
            this.select = select;
            this.variants = variants;
            this.setter = setter;
            this.getter = getter;
            this.createWrapper = createWrapper;
        }

        public void init() {
            select.getItems().setAll(variants);
            select.setValue(select.getItems().get(0));
            select.setOnAction(actionEvent -> {
                Optional.ofNullable(select.getValue()).map(ItemWrapper::getValue).ifPresent(setter);
            });
        }

        public void onPortSelect(Optional<SerialPort> maybePort) {
            select.setDisable(maybePort.isEmpty());
            maybePort.map(getter)
                .flatMap(value -> select.getItems().stream()
                    .filter(item -> value.equals(item.getValue())).findFirst()
                    .or(() -> Stream.of(createWrapper.apply(value)).peek(select.getItems()::add).findAny())
                )
                .ifPresent(wrapper -> select.setValue(wrapper));
        }
    }

    private Optional<SerialPort> getSelectedPort() {
        return Optional.ofNullable(portsSelect.getValue()).map(PortWrapper::getValue);
    }

    @FXML
    public void initialize() {
        setCurrentOpenedPortWrapper(null);
        appendSelect.getItems().setAll(endItemWrappers);

        final List<SettinggsData<?, ? extends ItemWrapper<?>>> settinggsData = Arrays.asList(
            new SettinggsData<>(baudRateSelect, baudRateItemWrappers,
                (Integer value) -> getSelectedPort().ifPresent(port -> port.setBaudRate(value)),
                SerialPort::getBaudRate, IntWrapper::new),
            new SettinggsData<>(dataBitsSelect, dataBitsItemWrappers,
                (Integer value) -> getSelectedPort().ifPresent(port -> port.setNumDataBits(value)),
                SerialPort::getNumDataBits, IntWrapper::new),
            new SettinggsData<>(stopBitsSelect, stopBitsItemWrappers,
                (Integer value) -> getSelectedPort().ifPresent(port -> port.setNumStopBits(value)),
                SerialPort::getNumStopBits, IntWrapper::new),
            new SettinggsData<>(paritySelect, parityItemWrappers,
                (Integer value) -> getSelectedPort().ifPresent(port -> port.setParity(value)),
                SerialPort::getParity, IntWrapper::new),
            new SettinggsData<>(flowControlSelect, controlFlowItemWrappers,
                (Integer value) -> getSelectedPort().ifPresent(port -> port.setFlowControl(value)),
                SerialPort::getFlowControlSettings, IntWrapper::new)
            );

        settinggsData.forEach(SettinggsData::init);
        portsSelect.setOnAction(actionEvent -> {
            Optional<SerialPort> maybePort = getSelectedPort();
            settinggsData.forEach(s -> s.onPortSelect(maybePort));
            final boolean noPort = maybePort.isEmpty();
            openPortButton.setDisable(noPort);
            if (noPort) {
                settingsPane.setExpanded(false);
            }
        });
        refreshPorts();
    }

    @FXML
    protected void refreshPorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        portsSelect.getItems().setAll(Arrays.stream(ports).map(PortWrapper::new)
            .collect(Collectors.toList()));
    }

    private StringBuilder gotData = new StringBuilder();

    @FXML
    protected void openPort() throws PortException {
        SerialPort port = portsSelect.getValue().getValue();

        if (currentOpenedPortWrapper != null) {
            if (currentOpenedPortWrapper.getPort().equals(port) && currentOpenedPortWrapper.isRunning()) {
                return;
            }
            closeCurrentPort();
        }

        OpenedPortWrapper op = new OpenedPortWrapper(port);
        setCurrentOpenedPortWrapper(op);
        op.setOnClose(() -> setCurrentOpenedPortWrapper(null));
        op.setInputConsumer(dataReceived -> {
            if (!dataReceived.isEmpty()) {
                communicationOutput.appendText(dataReceived);
                logCaretPosition = communicationOutput.getLength();
                communicationOutput.positionCaret(logCaretPosition);
            }
        });
        op.init();
    }

    @FXML
    protected void closeCurrentPort() {
        if (currentOpenedPortWrapper != null) {
            currentOpenedPortWrapper.stop();
        }
    }

    private void setCurrentOpenedPortWrapper(OpenedPortWrapper value) {
        currentOpenedPortWrapper = value;
        sendButton.setDisable(currentOpenedPortWrapper == null);
        closeCurrentPortButton.setDisable(currentOpenedPortWrapper == null);
    }

    @FXML
    protected void send() {
        String append = appendSelect.getValue() != null ? appendSelect.getValue().getValue() : "";
        try {
            currentOpenedPortWrapper.print(sendInput.getText() + append);
        } catch (IOException e) {

        }
    }
}
