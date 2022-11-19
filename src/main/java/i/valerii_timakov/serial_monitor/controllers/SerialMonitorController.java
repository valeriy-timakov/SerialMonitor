package i.valerii_timakov.serial_monitor.controllers;

import i.valerii_timakov.serial_monitor.ServicesFactory;
import i.valerii_timakov.serial_monitor.controllers.select_wrappers.IntWrapper;
import i.valerii_timakov.serial_monitor.controllers.select_wrappers.ItemWrapper;
import i.valerii_timakov.serial_monitor.controllers.select_wrappers.PortWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;

import java.nio.charset.Charset;

@Log4j2
public class SerialMonitorController {

    @FXML
    private ChoiceBox<PortWrapper> portsSelect;
    @FXML
    private ChoiceBox<ItemWrapper<String>> delimiterSelect;
    @FXML
    private TextArea communicationOutput;
    @FXML
    private Button sendButton;
    @FXML
    private TextField sendInput;
    @FXML
    private Button openPortButton;
    @FXML
    ChoiceBox<IntWrapper> baudRateSelect;
    @FXML
    ChoiceBox<IntWrapper> dataBitsSelect;
    @FXML
    ChoiceBox<ItemWrapper<Integer>> stopBitsSelect;
    @FXML
    ChoiceBox<ItemWrapper<Integer>> paritySelect;
    @FXML
    ChoiceBox<ItemWrapper<Integer>> flowControlSelect;
    @FXML
    ChoiceBox<ItemWrapper<Charset>> encodingSelect;
    @FXML
    private TitledPane settingsPane;
    @FXML
    private Accordion accodrion;
    @FXML
    private Button closeCurrentPortButton;
    @FXML
    private CheckBox addTimestampCheckbox;
    @FXML
    private CheckBox addDirectionCheckbox;
    @FXML
    private TextField messageWaitTimeoutEdit;
    @FXML
    private CheckBox rawLogSaveToFileCheckbox;
    @FXML
    private TextField rawLogFileEdit;
    @FXML
    private Button rawLogFilePathSelectButton;
    @FXML
    private Button saveTextLogButton;
    @FXML
    private Button refreshPortsButton;
    @FXML
    private CheckBox sendTextDataCheckbox;
    @FXML
    private TilePane textMessagesTiles;
    @FXML
    private TilePane byteHexOutput;
    @FXML
    private TilePane byteCusomOutput;
    @FXML
    private ChoiceBox wordSizeSelect;

    public void init(Stage stage, ServicesFactory servicesFactory) {
        TextLogController textLogController = new TextLogController(communicationOutput, saveTextLogButton,
            servicesFactory.getTextLogService(), servicesFactory.getSettingsService());
        textLogController.init(stage);

        SendDataController sendDataController = new SendDataController(sendButton, sendInput, sendTextDataCheckbox,
            servicesFactory.getMainMessageService());
        sendDataController.init();
        servicesFactory.getPortWrapperService().addConnectionStateListener(sendDataController::updateOpenedPortDataWrapper);

        SettingsController settingsController = new SettingsController(servicesFactory.getSettingsService(), rawLogSaveToFileCheckbox,
            rawLogFileEdit, rawLogFilePathSelectButton, delimiterSelect, addTimestampCheckbox, addDirectionCheckbox,
            messageWaitTimeoutEdit, wordSizeSelect);
        settingsController.init(stage);

        PortSelectController portSelectController = new PortSelectController(portsSelect, openPortButton, closeCurrentPortButton,
            refreshPortsButton, baudRateSelect, dataBitsSelect, stopBitsSelect, paritySelect, flowControlSelect, encodingSelect,
            settingsPane, accodrion);
        portSelectController.init(servicesFactory.getPortWrapperService());

        TileMessagesController tileMessagesController = new TileMessagesController(textMessagesTiles,
            servicesFactory.getTextLogService(), servicesFactory.getSettingsService());
        tileMessagesController.init();

        ByteLogController byteLogController = new ByteLogController(byteHexOutput, byteCusomOutput,
            servicesFactory.getByteLogService());
        byteLogController.init();
    }

}
