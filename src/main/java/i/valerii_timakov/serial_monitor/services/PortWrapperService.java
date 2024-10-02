package i.valerii_timakov.serial_monitor.services;

import com.fazecast.jSerialComm.SerialPort;
import i.valerii_timakov.serial_monitor.exceptions.PortException;
import i.valerii_timakov.serial_monitor.utils.Log;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class PortWrapperService implements OutgoingMessageConsumer {

    private final List<ByteArrayMessageConsumer> consumers;


    private Optional<OpenedPortWrapper> currentOpenedPortWrapper;
    private final List<Consumer<Boolean>> connectionStateListeners = new ArrayList<>();

    public void init() {
        setCurrentOpenedPortWrapper(null);
    }

    private final Supplier<IOException> noPortErrorSuplier = () -> new IOException("No port for outcoming message!");

    @Override
    public void consume(byte[] src, int length) throws IOException {
        currentOpenedPortWrapper.orElseThrow(noPortErrorSuplier).write(src, length);
    }

    public void openPort(SerialPort port) {
        currentOpenedPortWrapper.ifPresent(portWrapper -> {
            if (portWrapper.getPort().equals(port) && portWrapper.isRunning()) {
                return;
            }
            closeCurrentPort();
        });

        OpenedPortWrapper op = new OpenedPortWrapper(port);
        op.setOnClose(() -> setCurrentOpenedPortWrapper(null));
        op.setConsumers(consumers);
        try {
            op.init();
        } catch (PortException e) {
            Log.error("Error opening port! Port: " + port.getSystemPortName(), e);
            op = null;
        }
        setCurrentOpenedPortWrapper(op);
    }

    public void addConnectionStateListener(@NonNull Consumer<Boolean> listener) {
        connectionStateListeners.add(listener);
        callListener(listener);
    }

    public void closeCurrentPort() {
        currentOpenedPortWrapper.ifPresent(portWrapper -> {
            portWrapper.stop();
            setCurrentOpenedPortWrapper(null);
        });
    }

    private void setCurrentOpenedPortWrapper(OpenedPortWrapper value) {
        currentOpenedPortWrapper = Optional.ofNullable(value);
        connectionStateListeners.forEach(this::callListener);
    }

    private void callListener(Consumer<Boolean> connectionStateListener) {
        try {
            connectionStateListener.accept(currentOpenedPortWrapper.isPresent());
        } catch (Throwable t) {
            Log.error("Error processing current open port wrapper change listener!", t);
        }
    }

}
