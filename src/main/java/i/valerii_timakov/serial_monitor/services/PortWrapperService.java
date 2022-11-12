package i.valerii_timakov.serial_monitor.services;

import com.fazecast.jSerialComm.SerialPort;
import i.valerii_timakov.serial_monitor.exceptions.PortException;
import i.valerii_timakov.serial_monitor.utils.Log;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class PortWrapperService implements UnidirectedMessageConsumer {

    private final TextMessageConsumer textMessageConsumer;
    private final ByteArrayMessageConsumer byteArrayMessageConsumer;

    private Optional<OpenedPortWrapper> currentOpenedPortWrapper;
    private final List<Consumer<Boolean>> connectionStateListeners = new ArrayList<>();

    public void init() {
        setCurrentOpenedPortWrapper(null);
    }

    private final Supplier<IOException> noPortErrorSuplier = () -> new IOException("No port for outcoming message!");
    @Override
    public void consume(String message) throws IOException {
        currentOpenedPortWrapper.orElseThrow(noPortErrorSuplier).print(message);
    }

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
        op.setTextMessageConsumer(textMessageConsumer);
        op.setByteArrayMessageConsumer(byteArrayMessageConsumer);
        try {
            op.init();
        } catch (PortException e) {
            Log.error("Error opening port! Port: " + port.getSystemPortName(), e);
            op = null;
        }
        setCurrentOpenedPortWrapper(op);
    }

    public void addConnectionStateListener(Consumer<Boolean> listener) {
        if (listener != null) {
            connectionStateListeners.add(listener);
        }
    }

    public void closeCurrentPort() {
        currentOpenedPortWrapper.ifPresent(portWrapper -> {
            portWrapper.stop();
            setCurrentOpenedPortWrapper(null);
        });
    }

    private void setCurrentOpenedPortWrapper(OpenedPortWrapper value) {
        currentOpenedPortWrapper = Optional.ofNullable(value);
        connectionStateListeners.forEach(connectionStateListener -> {
            try {
                connectionStateListener.accept(currentOpenedPortWrapper.isPresent());
            } catch (Throwable t) {
                Log.error("Error processing current open port wrapper change listener!", t);
            }
        });
    }

    public Optional<Charset> getCurrentPortCharset() {
        return currentOpenedPortWrapper.map(OpenedPortWrapper::getCharset);
    }

    public void setCurrentPortCharset(Charset currentPortCharset) {
        currentOpenedPortWrapper.ifPresent(p -> p.setCharset(currentPortCharset));
    }
}
