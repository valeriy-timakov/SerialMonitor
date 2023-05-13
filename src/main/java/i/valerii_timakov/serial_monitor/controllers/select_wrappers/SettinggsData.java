package i.valerii_timakov.serial_monitor.controllers.select_wrappers;

import com.fazecast.jSerialComm.SerialPort;
import javafx.scene.control.ChoiceBox;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SettinggsData<T, W extends ItemWrapper<T>> {
    public final ChoiceBox<W> select;
    public final List<W> variants;
    public final Consumer<T> setter;
    public final Function<SerialPort, T> portGetter;
    public final Supplier<T> getter;
    public final Function<T, W> createWrapper;

    public SettinggsData(ChoiceBox<W> select, List<W> variants, Consumer<T> setter, Function<SerialPort, T> portGetter,
                          Supplier<T> getter, Function<T, W> createWrapper) {
        this.select = select;
        this.variants = variants;
        this.setter = setter;
        this.portGetter = portGetter;
        this.getter = getter;
        this.createWrapper = createWrapper;
    }

    public SettinggsData(ChoiceBox<W> select, List<W> variants, Consumer<T> setter, Supplier<T> getter,
                          Function<T, W> createWrapper) {
        this(select, variants, setter, null, getter, createWrapper);
    }

    public SettinggsData(ChoiceBox<W> select, List<W> variants, Consumer<T> setter, Function<SerialPort, T> portGetter,
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
