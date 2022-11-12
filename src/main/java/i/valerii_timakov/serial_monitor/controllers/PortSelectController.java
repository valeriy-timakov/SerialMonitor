package i.valerii_timakov.serial_monitor.controllers;

import com.fazecast.jSerialComm.SerialPort;
import i.valerii_timakov.serial_monitor.controllers.select_wrappers.IntWrapper;
import i.valerii_timakov.serial_monitor.controllers.select_wrappers.ItemWrapper;
import i.valerii_timakov.serial_monitor.controllers.select_wrappers.PortWrapper;
import i.valerii_timakov.serial_monitor.services.PortWrapperService;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TitledPane;
import lombok.RequiredArgsConstructor;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class PortSelectController {

    private final ChoiceBox<PortWrapper> portsSelect;
    private final Button openPortButton;
    private final Button closeCurrentPortButton;
    private final Button refreshPortsButton;
    private final ChoiceBox<IntWrapper> baudRateSelect;
    private final ChoiceBox<IntWrapper> dataBitsSelect;
    private final ChoiceBox<ItemWrapper<Integer>> stopBitsSelect;
    private final ChoiceBox<ItemWrapper<Integer>> paritySelect;
    private final ChoiceBox<ItemWrapper<Integer>> flowControlSelect;
    private final ChoiceBox<ItemWrapper<Charset>> encodingSelect;
    private final TitledPane settingsPane;
    private final Accordion accodrion;
    private PortWrapperService portWrapperService;

    public void init(PortWrapperService portWrapperService) {
        this.portWrapperService = portWrapperService;

        init();

        portsSelect.setOnAction(actionEvent -> {
            Optional<SerialPort> maybePort = getSelectedPort();
            onPortSelect(maybePort);
            final boolean noPort = maybePort.isEmpty();
            openPortButton.setDisable(noPort);
            if (noPort) {
                settingsPane.setExpanded(false);
            }
        });
        settingsPane.setOnMouseClicked(mouseEvent -> accodrion.setPrefHeight(settingsPane.isExpanded() ? 70 : 20));
        portWrapperService.addConnectionStateListener(opened -> closeCurrentPortButton.setDisable(!opened));
        closeCurrentPortButton.setOnAction(event -> portWrapperService.closeCurrentPort());
        refreshPortsButton.setOnAction(event -> refreshPorts());
        openPortButton.setOnAction(event -> {

            SerialPort port = portsSelect.getValue().getValue();
            portWrapperService.openPort(port);

        });

        refreshPorts();
    }

    private void onPortSelect(Optional<SerialPort> maybePort) {
        settinggsData.forEach(s -> s.onPortSelect(maybePort));
    }

    Optional<SerialPort> getSelectedPort() {
        return Optional.ofNullable(portsSelect.getValue()).map(PortWrapper::getValue);
    }

    private void refreshPorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        portsSelect.getItems().setAll(Arrays.stream(ports).map(PortWrapper::new)
                .collect(Collectors.toList()));
    }


    private static final class SettinggsData<T, W extends ItemWrapper<T>> {
        public final ChoiceBox<W> select;
        public final List<W> variants;
        public final Consumer<T> setter;
        public final Function<SerialPort, T> portGetter;
        public final Supplier<T> getter;
        public final Function<T, W> createWrapper;

        private SettinggsData(ChoiceBox<W> select, List<W> variants, Consumer<T> setter, Function<SerialPort, T> portGetter,
                              Supplier<T> getter, Function<T, W> createWrapper) {
            this.select = select;
            this.variants = variants;
            this.setter = setter;
            this.portGetter = portGetter;
            this.getter = getter;
            this.createWrapper = createWrapper;
        }

        private SettinggsData(ChoiceBox<W> select, List<W> variants, Consumer<T> setter, Supplier<T> getter,
                              Function<T, W> createWrapper) {
            this(select, variants, setter, null, getter, createWrapper);
        }

        private SettinggsData(ChoiceBox<W> select, List<W> variants, Consumer<T> setter, Function<SerialPort, T> portGetter,
                              Function<T, W> createWrapper) {
            this(select, variants, setter, portGetter, null, createWrapper);
        }

        public void init() {
            select.getItems().setAll(variants);
            select.setValue(select.getItems().get(0));
            select.setOnAction(actionEvent -> Optional.ofNullable(select.getValue()).map(ItemWrapper::getValue).ifPresent(setter));
            if (getter != null) {
                setSelect(Optional.ofNullable(getter.get()));
            }
        }

        private void setSelect(Optional<T> maybeValue) {
            W newValue = maybeValue.flatMap(value -> select.getItems().stream()
                    .filter(item -> value.equals(item.getValue())).findFirst()
                    .or(() -> Stream.of(createWrapper.apply(value)).peek(select.getItems()::add).findAny())
                )
                .orElse(null);
            select.setValue(newValue);
        }

        public void onPortSelect(Optional<SerialPort> maybePort) {
            select.setDisable(maybePort.isEmpty());
            if (portGetter != null) {
                maybePort.map(portGetter)
                    .flatMap(value -> select.getItems().stream()
                        .filter(item -> value.equals(item.getValue())).findFirst()
                        .or(() -> Stream.of(createWrapper.apply(value)).peek(select.getItems()::add).findAny())
                    )
                    .ifPresent(select::setValue);
            }
        }
    }

    private List<SettinggsData<?, ? extends ItemWrapper<?>>> settinggsData;
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

    private final List<ItemWrapper<Charset>> charsetsWrapper =
        Stream.of("utf-8", "cp1251", "US-ASCII", "ISO-8859-1")
            .map(name -> new ItemWrapper<>(Charset.forName(name), name))
            .collect(Collectors.toList());

    private void init() {
        settinggsData = Arrays.asList(
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
                SerialPort::getFlowControlSettings, IntWrapper::new),
            new SettinggsData<>(encodingSelect, charsetsWrapper,
                portWrapperService::setCurrentPortCharset,
                () -> portWrapperService.getCurrentPortCharset().orElseGet(Charset::defaultCharset),
                (Charset value) -> new ItemWrapper<>(value, value.name()))
        );
        settinggsData.forEach(SettinggsData::init);
    }
}
