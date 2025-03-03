package ru.effectivegroup.client.algoil;

import java.io.IOException;
import java.time.LocalTime;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.effectivegroup.client.algoil.BotPane;
import ru.effectivegroup.client.algoil.bots.Bot;
import ru.effectivegroup.client.algoil.bots.BotManagerEventsConsumer;
import ru.effectivegroup.client.algoil.bots.BotOrderChangeListener;
import ru.effectivegroup.client.algoil.bots.BotStateChangeListener;
import ru.effectivegroup.client.algoil.bots.states.BotStateKind;
import ru.effectivegroup.client.algoil.marketdata.DataFeed;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/BotListViewCell.class */
public class BotListViewCell extends ListCell<Bot> implements BotStateChangeListener, BotManagerEventsConsumer, BotOrderChangeListener {
    private static final String GREEN_STYLE = "-fx-background-color: #22cc22dd";
    private static final String RED_STYLE = "-fx-background-color: #cc2200ff";
    private final AlgoilApp algoilApp;

    @FXML
    public Button button;

    @FXML
    private AnchorPane graphic;

    @FXML
    private Label instrumentCode;

    @FXML
    private Label mode;

    @FXML
    private Label price;

    @FXML
    private Text fullName;

    @FXML
    private Label startTime;
    private Bot bot;
    private BotPane botPane;
    private final Logger logger = LogManager.getLogger("algoil");
    private final Object syncRoot = new Object();

    public BotListViewCell(DataFeed dataFeed) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("BotListViewCell.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
            this.algoilApp = AlgoilApp.getInstance();
            this.algoilApp.getBotManager().Subscribe(this);
            this.botPane = this.algoilApp.getBotPane();
            this.button.setStyle(GREEN_STYLE);
            this.button.setOnAction(event -> {
                toggleButton();
            });
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void updateItem(Bot bot, boolean empty) {
        super.updateItem(bot, empty);
        if (bot != null) {
            if (this.bot != null && !this.bot.equals(bot)) {
                this.bot.UnsubscribeStateChange(this);
                this.bot.UnsubscribeOrderChange(this);
            }
            if (selectedProperty().get()) {
                this.fullName.setFill(Color.WHITE);
            } else {
                this.fullName.setFill(Color.GRAY);
            }
            this.bot = bot;
            this.bot.SubscribeOrderChange(this);
            boolean isSubscribed = this.bot.SubscribeStateChange(this);
            if (isSubscribed) {
                HandleBotSettingsChanged(this.bot);
                HandleBotOrderPriceChanged(this.bot);
            }
        }
        Platform.runLater(() -> {
            if (empty) {
                setText(null);
                setGraphic(null);
                return;
            }
            if (bot.isRunning()) {
                setRunningStyle();
            } else {
                setStoppedStyle();
            }
            this.fullName.setText(bot.getInstrument().getFullName());
            this.instrumentCode.setText(bot.getInstrument().getCode());
            setGraphic(this.graphic);
            setPrefWidth(150.0d);
        });
    }

    public void toggleButton() {
        getListView().getSelectionModel().select(this.bot);
        if (this.bot.isRunning()) {
            this.bot.Stop();
        } else if (BotPane.Helper.isBotSetupCorrectly(this.botPane.getBotMode(), this.botPane.getBotStartMode(), this.botPane.getClientsDataList(), this.bot)) {
            this.bot.Start();
        }
    }

    private void setRunningStyle() {
        Platform.runLater(() -> {
            this.button.setText("Остановить");
            this.button.setStyle(RED_STYLE);
        });
    }

    private void setStoppedStyle() {
        Platform.runLater(() -> {
            this.button.setStyle(GREEN_STYLE);
            this.button.setText("Запустить");
        });
    }

    @Override // ru.effectivegroup.client.algoil.bots.BotStateChangeListener
    public void Handle(BotStateKind stateKind) {
        switch (this.bot.getState().getStateKind()) {
            case Running:
                setRunningStyle();
                return;
            case Stopped:
                setStoppedStyle();
                return;
            default:
                return;
        }
    }

    @Override // ru.effectivegroup.client.algoil.bots.BotOrderChangeListener
    public void HandleBotOrderPriceChanged(Bot bot) {
        if (this.bot == null || !this.bot.equals(bot)) {
            return;
        }
        Long priceValue = bot.getCurrentPrice();
        if (priceValue == null || priceValue.longValue() == 0 || bot.getActiveOrder() == null) {
            Platform.runLater(() -> {
                this.price.setText("");
            });
            return;
        }
        Long qty = Long.valueOf(bot.getActiveOrder().getQtyLeft());
        if (qty.longValue() == 0) {
            Platform.runLater(() -> {
                this.price.setText("");
            });
        } else {
            Platform.runLater(() -> {
                this.price.setText(String.format("%s (%s)", priceValue, qty));
            });
        }
    }

    @Override // ru.effectivegroup.client.algoil.bots.BotManagerEventsConsumer
    public void HandleBotSettingsChanged(Bot bot) {
        if (this.bot != null && !this.bot.equals(bot)) {
            return;
        }
        if (bot.getBotMode() != null) {
            Platform.runLater(() -> {
                this.mode.setText(bot.getBotMode().name());
            });
        } else {
            Platform.runLater(() -> {
                this.mode.setText("");
            });
        }
        if (bot.getStartTime() != null && bot.getTimeSpread() != null) {
            LocalTime time = Utils.convertToLocalTime(bot.getStartTime());
            long spread = bot.getTimeSpread().longValue();
            String result = Utils.LocalTimeToReadable(time, false) + " - " + spread + "мс";
            Platform.runLater(() -> {
                this.startTime.setText(result);
            });
            return;
        }
        Platform.runLater(() -> {
            this.startTime.setText("");
        });
    }
}
