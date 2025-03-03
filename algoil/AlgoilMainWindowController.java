package ru.effectivegroup.client.algoil;

import com.google.protobuf.AbstractMessage;
import java.net.URL;
import java.util.Iterator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.effectivegroup.client.algoil.adapter.DefaultSTEMessageConsumer;
import ru.effectivegroup.client.algoil.bots.Bot;
import ru.effectivegroup.client.algoil.bots.BotManager;
import ru.effectivegroup.client.algoil.marketdata.DataFeedMessageConsumer;
import ru.effectivegroup.client.context.Context;
import ru.effectivegroup.client.gui.widget.WidgetComponent;
import ru.effectivegroup.client.model.data.ste.AccountData;
import ru.effectivegroup.client.model.data.ste.SecurityData;
import ru.effectivegroup.client.service.OrderService;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/AlgoilMainWindowController.class */
public class AlgoilMainWindowController extends DefaultSTEMessageConsumer implements WidgetComponent<AlgoilWidget>, DataFeedMessageConsumer {
    private final Logger logger = LogManager.getLogger("algoil");
    private final AlgoilApp algoilApp;

    @FXML
    public BotsListControl botsListControl;

    @FXML
    public BotPane botPane;

    @FXML
    private ComboBox<AccountData> moneyHoldingComboBox;

    @FXML
    private ComboBox<AccountData> productHoldingComboBox;

    public AlgoilMainWindowController() {
        this.logger.debug("AlgoilMainWindowController");
        AlgoilApp.Init();
        this.algoilApp = AlgoilApp.getInstance();
    }

    public URL getFXMLResourcePath() {
        return AlgoilMainWindowController.class.getResource("AlgoilMainWindow.fxml");
    }

    public void onCreate(AlgoilWidget algoil) {
        algoil.setOnWidgetClose(() -> {
            this.logger.info("Закрытие виджета Algoil");
            this.algoilApp.Stop();
        });
    }

    @FXML
    public void initialize() {
        this.logger.info("Инициализация");
        this.botsListControl.SubscribeSelectionChanged(this.botPane);
        this.botsListControl.SubscribeSelectionChanged(this.botPane.getQuotationControl());
        this.botsListControl.SubscribeSelectionChanged(this.botPane.getOrderControl());
        this.botsListControl.SubscribeSelectionChanged(this.botPane.getTradeControl());
    }

    @FXML
    void handleButtonAddNewAlgoForm() {
        AddNewBotFormController.AddAlgo();
    }

    @FXML
    void deleteAlgoForm() {
        if (this.botsListControl.getSelectedBot() != null) {
            this.algoilApp.getBotManager().DeleteBot(this.botsListControl.getSelectedBot());
        }
    }

    @FXML
    void stopAll() {
        BotManager botManager = this.algoilApp.getBotManager();
        Iterator<Bot> it = botManager.getBots().iterator();
        while (it.hasNext()) {
            Bot bot = it.next();
            if (bot.isRunning()) {
                bot.Stop();
            }
        }
    }

    public void onRender() {
        super.onRender();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void sendRequest() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Test button");
        alert.setContentText("Send order");
        alert.showAndWait();
    }

    void myTestFunc(AbstractMessage abstractMessage) {
        if (abstractMessage.getInitializationErrorString() != null) {
            System.out.println(abstractMessage.getInitializationErrorString());
        }
        this.logger.debug("\n" + abstractMessage);
    }

    OrderService getOrderService() {
        return Context.serviceContext.enterOrderService;
    }

    @Override // ru.effectivegroup.client.algoil.marketdata.DataFeedMessageConsumer
    public void Handle(Instrument instrument) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.DefaultSTEMessageConsumer, ru.effectivegroup.client.algoil.adapter.STEMessageConsumer, ru.effectivegroup.client.algoil.marketdata.DataFeedMessageConsumer
    public void Handle(SecurityData message) {
    }
}
