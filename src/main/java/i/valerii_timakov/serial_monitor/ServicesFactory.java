package i.valerii_timakov.serial_monitor;

import i.valerii_timakov.serial_monitor.services.*;
import lombok.Getter;

public class ServicesFactory {

    @Getter
    private final TextLogService textLogService;
    @Getter
    private final SettingsService settingsService;
    @Getter
    private final MainMessageService mainMessageService;
    @Getter
    private final InputScanner inputScanner;
    @Getter
    private final PortWrapperService portWrapperService;
    @Getter
    private final ByteLogService byteLogService;

    public ServicesFactory() {
        textLogService = new TextLogService();
        textLogService.start();
        settingsService = new SettingsService();
        settingsService.init();
        byteLogService = new ByteLogService(settingsService);
        mainMessageService = new MainMessageService(settingsService);
        mainMessageService.addIncomingTextConsumer(textLogService);
        mainMessageService.addIncomingByteArrConsumer(byteLogService);
        inputScanner = new InputScanner(mainMessageService, settingsService);
        portWrapperService = new PortWrapperService(inputScanner, mainMessageService);
        portWrapperService.init();
        mainMessageService.setOutcomingMessageConsumer(portWrapperService);

    }
}
