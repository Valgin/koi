package ru.effectivegroup.client.algoil;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.effectivegroup.client.algoil.bots.Bot;
import ru.effectivegroup.client.algoil.bots.BotMode;
import ru.effectivegroup.client.algoil.bots.BotStartMode;
import ru.effectivegroup.client.algoil.bots.BotStateChangeListener;
import ru.effectivegroup.client.algoil.bots.states.BotStateKind;
import ru.effectivegroup.client.algoil.execution.ExecutionManagerEventsConsumer;
import ru.effectivegroup.client.algoil.execution.Transaction.TransactionManager;
import ru.effectivegroup.client.algoil.marketdata.DataFeed;
import ru.effectivegroup.client.algoil.marketdata.DataFeedMessageConsumer;
import ru.effectivegroup.client.algoil.marketdata.OrderBook;
import ru.effectivegroup.client.algoil.settings.BotPaneSettingsManager;
import ru.effectivegroup.client.algoil.settings.TransactionQueueSettings;
import ru.effectivegroup.client.algoil.settings.TransactionQueueSettingsManager;
import ru.effectivegroup.client.context.Context;
import ru.effectivegroup.client.crypto.cipher.CipherHolder;
import ru.effectivegroup.client.model.data.ste.AccountData;
import ru.effectivegroup.client.model.data.ste.ClientData;
import ru.effectivegroup.client.model.data.ste.OrderData;
import ru.effectivegroup.client.model.data.ste.SecurityData;
import ru.effectivegroup.client.model.data.ste.dictionary.AccountType;
import ru.effectivegroup.client.service.ServiceContext;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/BotPane.class */
public class BotPane extends VBox implements BotsListControlEventsConsumer, ExecutionManagerEventsConsumer, DataFeedMessageConsumer, BotStateChangeListener {
    private static final Logger logger = LogManager.getLogger("algoil");
    private final AlgoilApp algoilApp;
    private final BotPaneSettingsManager botPaneSettingsManager;
    private final TransactionQueueSettingsManager transactionQueueSettingsManager;
    private final TransactionManager transactionManager;
    private OrderCounter orderCounter;
    private Bot bot;
    private ChangeListener<BotMode> botModeListener;
    private ChangeListener<BotStartMode> botStartModeListener;

    @FXML
    private Button runButton;

    @FXML
    private Label positionLabel;

    @FXML
    private ComboBox<String> moneyAccountsComboBox;

    @FXML
    private ComboBox<String> commAccountsComboBox;

    @FXML
    private ComboBox<String> clientControl;

    @FXML
    private ComboBox<BotMode> botMode;

    @FXML
    private ComboBox<BotStartMode> botStartMode;

    @FXML
    private TextField maxPrice;

    @FXML
    private TextField startPrice;

    @FXML
    private TextField delayPlace;

    @FXML
    private TextField positionLimit;

    @FXML
    private TextField orderCountLimit;

    @FXML
    private TextField orderSize;

    @FXML
    private TextField maxOverlapStep;

    @FXML
    private TextField maxPricePercentage;

    @FXML
    private TextField timeSpread;

    @FXML
    private TextField startVolume;

    @FXML
    private TextField startTime;

    @FXML
    private TextField minOverlapStep;

    @FXML
    private QuotationControl quotationControl;

    @FXML
    private OrderControl orderControl;

    @FXML
    private TradeControl tradeControl;

    @FXML
    private TitledPane quotationPane;
    private ObservableList<String> commAccounts;
    private ObservableList<String> moneyAccounts;
    private ObservableList<String> clients;
    private List<ClientData> clientsDataList;
    private BooleanBinding runBotButtonDisabled;
    private StringBinding runBotButtonTextBinding;
    private StringBinding positionLabelTextBinding;

    @FXML
    private Label overlapLabel;

    @FXML
    private Label orderTodayCountLabel;

    @FXML
    private Label upperPrice;

    @FXML
    private Label lowerPrice;

    @FXML
    private Label currentPrice;

    @FXML
    private Label ourUpperPrice;

    @FXML
    private Label ourLowerPrice;

    @FXML
    private Label tradeVolume;

    @FXML
    private Label timeSpreadLabel;

    @FXML
    private VBox settingsBox;

    @FXML
    private VBox quotationBox;

    @FXML
    private VBox orderBox;

    @FXML
    private VBox tradeBox;

    @FXML
    private CheckBox showOrderControlCheckBox;

    @FXML
    private CheckBox fallDown;

    @FXML
    private TextField discreteAuctionEndTime;

    @FXML
    private TextField transactionQueue100Delay;

    @FXML
    private TextField transactionQueue100Size;

    @FXML
    private TextField transactionQueue1000Delay;

    @FXML
    private TextField transactionQueue1000Size;
    private final Object syncRoot = new Object();
    HashMap<Instrument, SecurityData> securityData = new HashMap<>();
    DecimalFormat format = new DecimalFormat("###,###,###,###");
    private String runBotButtonText = "Запустить";
    private String positionLabelText = "0";

    public BotPane() {
        loadFXML();
        this.orderCounter = OrderCounter.getInstance();
        this.algoilApp = AlgoilApp.getInstance();
        configureRunButton();
        initBotModeListener();
        initBotStartModeListener();
        configureAccountsUI();
        configureAlgoSettingsUI();
        disableAllControls();
        this.algoilApp.setBotPane(this);
        this.botPaneSettingsManager = this.algoilApp.getBotPaneManager();
        this.transactionQueueSettingsManager = this.algoilApp.getTransactionQueueSettingsManager();
        this.transactionManager = this.algoilApp.getTransactionManager();
        configureComponentsResize();
        configureHideTradeControlListener();
        configureFallDownListener();
        configureTimeSpread();
        configureQueue100Delay();
        configureQueue100Size();
        configureQueue1000Delay();
        configureQueue1000Size();
    }

    @FXML
    public void initialize() {
        logger.debug("BotPane initialize");
    }

    @Override // ru.effectivegroup.client.algoil.BotsListControlEventsConsumer
    public void Handle(Bot bot) {
        if (bot == null) {
            return;
        }
        synchronized (this.syncRoot) {
            this.bot = bot;
            updateOrderBookPosition();
            enableAllControls();
            this.bot.SubscribeStateChange(this);
            this.algoilApp.getDataFeed().Subscribe(bot.getInstrument(), this, DataFeed.DataFeedEventType.InstrumentData);
            configureStateListener();
            this.bot.SetPositionChangeListener(position -> {
                updatePositionLabel();
            });
            this.bot.SetPriceLimitsChangeListener(() -> {
                updateLimitsLabels();
            });
            this.bot.SetTodayOrdersCountChangeListener(() -> {
                updateTodayOrdersCount();
            });
            if (this.clientsDataList.size() != 0 && !Helper.isClientDataExist(this.clientsDataList, this.bot)) {
                disableAccounts();
            } else {
                enableAccounts();
            }
            configureBotModeListener(false);
            configureBotStartModeListener(false);
            configureBotSettingsControls();
            configureBotModeListener(true);
            configureBotStartModeListener(true);
            if (hasInstrumendSecData()) {
                setTradingVolume();
                setPriceLimitsLabels();
                updateTodayOrdersCount();
                updatePositionLabel();
                setSecurityAmountStep();
            }
        }
    }

    private void setSecurityAmountStep() {
        synchronized (this.syncRoot) {
            if (!this.securityData.containsKey(this.bot.getInstrument()) || this.securityData.isEmpty()) {
                Platform.runLater(() -> {
                    this.overlapLabel.setText("Шаг перебивания");
                });
            } else {
                int step = (int) (this.securityData.get(this.bot.getInstrument()).getAmountStep() / 100);
                Platform.runLater(() -> {
                    this.overlapLabel.setText("Шаг перебивания (" + step + ")");
                });
                if (this.bot.getInstrumentPriceStep() == 0) {
                    this.bot.setInstrumentPriceStep(step);
                }
            }
        }
    }

    private void setTradingVolume() {
        synchronized (this.syncRoot) {
            if (!this.securityData.containsKey(this.bot.getInstrument()) || this.securityData.isEmpty()) {
                Platform.runLater(() -> {
                    this.tradeVolume.setText("0");
                });
            } else {
                SecurityData sec = this.securityData.get(this.bot.getInstrument());
                String volume = String.valueOf(sec.getVolume() / sec.getLotSize());
                String volumeTonUnits = String.valueOf(sec.getVolume());
                Platform.runLater(() -> {
                    this.tradeVolume.setText(volume + "  (" + volumeTonUnits + " т.)");
                });
            }
        }
    }

    private void setPriceLimitsLabels() {
        synchronized (this.syncRoot) {
            if (!this.securityData.containsKey(this.bot.getInstrument()) || this.securityData.isEmpty()) {
                Platform.runLater(() -> {
                    this.lowerPrice.setText("0");
                    this.upperPrice.setText("0");
                    this.currentPrice.setText("");
                });
            } else {
                String lowerPriceText = this.format.format(this.securityData.get(this.bot.getInstrument()).getLowerPrice() / 100);
                String upperPriceText = this.format.format(this.securityData.get(this.bot.getInstrument()).getUpperPrice() / 100);
                String currentPriceText = this.format.format(this.securityData.get(this.bot.getInstrument()).getMarketPrice() / 100);
                Platform.runLater(() -> {
                    this.lowerPrice.setText(lowerPriceText);
                    this.upperPrice.setText(upperPriceText);
                    this.currentPrice.setText(currentPriceText);
                });
            }
        }
        if (this.bot.getCalculatedPriceUpperLimit() == null && this.bot.getCalculatedPriceLowerLimit() == null) {
            Platform.runLater(() -> {
                this.ourLowerPrice.setText("0");
                this.ourUpperPrice.setText("0");
            });
            return;
        }
        String ourLowerPriceText = this.format.format(this.bot.getCalculatedPriceLowerLimit());
        String ourUpperPriceText = this.format.format(this.bot.getCalculatedPriceUpperLimit());
        Platform.runLater(() -> {
            this.ourLowerPrice.setText(ourLowerPriceText);
            this.ourUpperPrice.setText(ourUpperPriceText);
        });
    }

    @Override // ru.effectivegroup.client.algoil.BotsListControlEventsConsumer
    public void Handle(Integer size) {
        if (size.intValue() != 0) {
            enableAllControls();
        } else {
            disableAllControls();
        }
    }

    private boolean hasInstrumendSecData() {
        boolean containsKey;
        synchronized (this.syncRoot) {
            containsKey = this.securityData.containsKey(this.bot.getInstrument());
        }
        return containsKey;
    }

    private void configureStateListener() {
        try {
            switch (this.bot.getState().getStateKind()) {
                case Running:
                    this.runBotButtonText = "Остановить";
                    break;
                case Stopped:
                    this.runBotButtonText = "Запустить";
                    break;
            }
        } finally {
            Platform.runLater(() -> {
                this.runBotButtonTextBinding.invalidate();
            });
        }
    }

    private void configureBotSettingsControls() {
        this.clientControl.valueProperty().setValue(this.bot.getClient());
        this.commAccountsComboBox.valueProperty().setValue(this.bot.getCommodityAccount());
        this.moneyAccountsComboBox.valueProperty().setValue(this.bot.getMoneyAccount());
        this.botMode.getSelectionModel().select(this.bot.getBotMode());
        this.botStartMode.getSelectionModel().select(this.bot.getBotStartMode());
        this.maxPrice.textProperty().setValue(String.valueOf(this.bot.getPriceLimit()));
        this.startPrice.textProperty().setValue(String.valueOf(this.bot.getStartPrice()));
        this.delayPlace.textProperty().setValue(String.valueOf(this.bot.getDelayPlace()));
        this.orderSize.textProperty().setValue(String.valueOf(this.bot.getOrderSize()));
        this.positionLimit.textProperty().setValue(String.valueOf(this.bot.getPositionLimit()));
        this.orderCountLimit.textProperty().setValue(String.valueOf(this.bot.getOrderCountLimit()));
        this.maxOverlapStep.textProperty().set(String.valueOf(this.bot.getMaxOverlapStep()));
        this.minOverlapStep.textProperty().set(String.valueOf(this.bot.getMinOverlapStep()));
        this.maxPricePercentage.textProperty().set(String.valueOf(this.bot.getMaxPricePercentage()));
        this.timeSpread.textProperty().setValue(String.valueOf(this.bot.getTimeSpread()));
        this.startVolume.textProperty().setValue(String.valueOf(this.bot.getStartVolume()));
        this.startTime.textProperty().setValue(String.valueOf(this.bot.getStartTime()));
        this.discreteAuctionEndTime.textProperty().setValue(String.valueOf(this.bot.getDiscreteAuctionEndTime()));
        this.fallDown.selectedProperty().setValue(Boolean.valueOf(this.bot.getFallDown()));
        TransactionQueueSettings transactionSettings = this.transactionQueueSettingsManager.getTransactionQueueSettings();
        this.transactionQueue100Delay.textProperty().setValue(String.valueOf(transactionSettings.getTransactionQueue100Delay()));
        this.transactionQueue100Size.textProperty().setValue(String.valueOf(transactionSettings.getTransactionQueue100Size()));
        this.transactionQueue1000Delay.textProperty().setValue(String.valueOf(transactionSettings.getTransactionQueue1000Delay()));
        this.transactionQueue1000Size.textProperty().setValue(String.valueOf(transactionSettings.getTransactionQueue1000Size()));
        LocalTime time = Utils.convertToLocalTime(this.bot.getStartTime());
        this.timeSpreadLabel.setText("Старт " + Utils.LocalTimeToReadable(time, true) + " минус ");
    }

    @Override // ru.effectivegroup.client.algoil.execution.ExecutionManagerEventsConsumer
    public void Handle(AccountData accountData) {
        synchronized (this.syncRoot) {
            switch (AnonymousClass4.$SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$AccountType[accountData.type.ordinal()]) {
                case 1:
                    this.commAccounts.add(accountData.code);
                    break;
                case 2:
                    this.moneyAccounts.add(accountData.code);
                    break;
            }
        }
    }

    @Override // ru.effectivegroup.client.algoil.execution.ExecutionManagerEventsConsumer
    public void Handle(ClientData clientData) {
        synchronized (this.syncRoot) {
            this.clientsDataList.add(clientData);
            this.clients.add(clientData.getShortName());
        }
    }

    @Override // ru.effectivegroup.client.algoil.bots.BotStateChangeListener
    public void Handle(BotStateKind stateKind) {
        switch (stateKind) {
            case Running:
                this.runBotButtonText = "Остановить";
                break;
            case Stopped:
                this.runBotButtonText = "Запустить";
                break;
        }
        Platform.runLater(() -> {
            this.runBotButtonTextBinding.invalidate();
        });
    }

    @Override // ru.effectivegroup.client.algoil.marketdata.DataFeedMessageConsumer
    public void Handle(SecurityData message) {
        if (message == null) {
            return;
        }
        synchronized (this.syncRoot) {
            this.securityData.put(new Instrument(message.getCode(), message.getFullName()), message);
        }
        if (hasInstrumendSecData()) {
            setPriceLimitsLabels();
            setTradingVolume();
            setSecurityAmountStep();
        }
    }

    @Override // ru.effectivegroup.client.algoil.marketdata.DataFeedMessageConsumer
    public void Handle(OrderData message) {
        updateTodayOrdersCount();
        updateOrderBookPosition();
    }

    public void updateTodayOrdersCount() {
        String ordersCount = String.valueOf(this.orderCounter.getOrdersCount(this.bot.getInstrument().getCode()));
        Platform.runLater(() -> {
            this.orderTodayCountLabel.setText(ordersCount);
        });
    }

    private void configureNumericTextField(TextField field, IntSupplier getter, IntConsumer updater, String propName) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                field.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        field.focusedProperty().addListener((observable2, oldValue2, newValue2) -> {
            if (!newValue2.booleanValue() && this.bot != null) {
                String old = "";
                boolean z = -1;
                switch (propName.hashCode()) {
                    case -125326801:
                        if (propName.equals("StartTime")) {
                            z = 8;
                            break;
                        }
                        break;
                    case -117182980:
                        if (propName.equals("StartVolume")) {
                            z = 7;
                            break;
                        }
                        break;
                    case 161754554:
                        if (propName.equals("OrderCountLimit")) {
                            z = 6;
                            break;
                        }
                        break;
                    case 332498276:
                        if (propName.equals("DelayPlace")) {
                            z = 3;
                            break;
                        }
                        break;
                    case 406406695:
                        if (propName.equals("StartPrice")) {
                            z = true;
                            break;
                        }
                        break;
                    case 458833509:
                        if (propName.equals("MaxPrice")) {
                            z = false;
                            break;
                        }
                        break;
                    case 1252780947:
                        if (propName.equals("OverlapStep")) {
                            z = 2;
                            break;
                        }
                        break;
                    case 1612358735:
                        if (propName.equals("OrderSize")) {
                            z = 5;
                            break;
                        }
                        break;
                    case 1847280626:
                        if (propName.equals("PositionLimit")) {
                            z = 4;
                            break;
                        }
                        break;
                    case 1895631358:
                        if (propName.equals("DiscreteAuctionEndTime")) {
                            z = 9;
                            break;
                        }
                        break;
                }
                switch (z) {
                    case false:
                        old = String.valueOf(this.bot.getPriceLimit());
                        break;
                    case true:
                        old = String.valueOf(this.bot.getStartPrice());
                        break;
                    case true:
                        old = String.valueOf(this.bot.getOverlapStep());
                        break;
                    case CipherHolder.CIPHER_PARAM_ENCRYPTED_SECRET_KEY /* 3 */:
                        old = String.valueOf(this.bot.getDelayPlace());
                        break;
                    case CipherHolder.CIPHER_PARAM_IV /* 4 */:
                        old = String.valueOf(this.bot.getPositionLimit());
                        break;
                    case true:
                        old = String.valueOf(this.bot.getOrderSize());
                        break;
                    case CipherHolder.CIPHER_PARAM_MAC_KEY /* 6 */:
                        old = String.valueOf(this.bot.getOrderCountLimit());
                        break;
                    case true:
                        old = String.valueOf(this.bot.getStartVolume());
                        break;
                    case ServiceContext.DEFAULT_TASK_POOL_SIZE /* 8 */:
                        old = String.valueOf(this.bot.getStartTime());
                        break;
                    case true:
                        old = String.valueOf(this.bot.getDiscreteAuctionEndTime());
                        break;
                }
                updater.accept(Integer.parseInt(field.getText()));
                saveSettings();
                logger.debug("Обновлены настройки бота {}. {}: {} → {}", this.bot != null ? this.bot.getInstrument().getCode() : "", propName, old, field.getText());
            }
        });
        field.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                boolean z = -1;
                switch (propName.hashCode()) {
                    case -125326801:
                        if (propName.equals("StartTime")) {
                            z = 9;
                            break;
                        }
                        break;
                    case -117182980:
                        if (propName.equals("StartVolume")) {
                            z = 8;
                            break;
                        }
                        break;
                    case 161754554:
                        if (propName.equals("OrderCountLimit")) {
                            z = 7;
                            break;
                        }
                        break;
                    case 332498276:
                        if (propName.equals("DelayPlace")) {
                            z = 4;
                            break;
                        }
                        break;
                    case 406406695:
                        if (propName.equals("StartPrice")) {
                            z = true;
                            break;
                        }
                        break;
                    case 458833509:
                        if (propName.equals("MaxPrice")) {
                            z = false;
                            break;
                        }
                        break;
                    case 894208975:
                        if (propName.equals("MaxOverlapStep")) {
                            z = 3;
                            break;
                        }
                        break;
                    case 1551125025:
                        if (propName.equals("MinOverlapStep")) {
                            z = 2;
                            break;
                        }
                        break;
                    case 1612358735:
                        if (propName.equals("OrderSize")) {
                            z = 6;
                            break;
                        }
                        break;
                    case 1847280626:
                        if (propName.equals("PositionLimit")) {
                            z = 5;
                            break;
                        }
                        break;
                }
                switch (z) {
                    case false:
                        this.startPrice.requestFocus();
                        return;
                    case true:
                        this.minOverlapStep.requestFocus();
                        return;
                    case true:
                        this.maxOverlapStep.requestFocus();
                        return;
                    case CipherHolder.CIPHER_PARAM_ENCRYPTED_SECRET_KEY /* 3 */:
                        this.delayPlace.requestFocus();
                        return;
                    case CipherHolder.CIPHER_PARAM_IV /* 4 */:
                        this.positionLimit.requestFocus();
                        return;
                    case true:
                        this.orderSize.requestFocus();
                        return;
                    case CipherHolder.CIPHER_PARAM_MAC_KEY /* 6 */:
                        this.orderCountLimit.requestFocus();
                        return;
                    case true:
                        this.startVolume.requestFocus();
                        return;
                    case ServiceContext.DEFAULT_TASK_POOL_SIZE /* 8 */:
                        this.startTime.requestFocus();
                        return;
                    case true:
                        this.discreteAuctionEndTime.requestFocus();
                        return;
                    default:
                        return;
                }
            }
        });
        field.textProperty().setValue(String.valueOf(getter.getAsInt()));
    }

    private void configureDoubleNumericTextField(TextField field, DoubleSupplier getter, DoubleConsumer updater, String propName) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?") || newValue.matches("[.]")) {
                field.setText(newValue.replaceAll("[^\\d.]", ""));
            }
        });
        field.focusedProperty().addListener((observable2, oldValue2, newValue2) -> {
            if (!newValue2.booleanValue() && this.bot != null) {
                String old = String.valueOf(this.bot.getMaxPricePercentage());
                updater.accept(Double.parseDouble(field.getText()));
                saveSettings();
                logger.debug("Обновлены настройки бота {}. {}: {} → {}", this.bot != null ? this.bot.getInstrument().getCode() : "", propName, old, field.getText());
            }
        });
        field.textProperty().setValue(String.valueOf(getter.getAsDouble()));
    }

    private void saveSettings() {
        this.algoilApp.getBotManager().SaveBotSettings(this.bot);
    }

    @FXML
    private void handleRunButtonClick() {
        if (this.bot == null) {
            logger.warn("Попытка запуска когда bot=null");
            return;
        }
        logger.info("Нажата кнопка запуска/остановки бота {}, isRunning={}", this.bot.getInstrument().getCode(), Boolean.valueOf(this.bot.isRunning()));
        if (this.bot.isRunning()) {
            this.bot.Stop();
        } else if (!Helper.isBotSetupCorrectly(this.botMode, this.botStartMode, this.clientsDataList, this.bot)) {
            disableAccounts();
        } else {
            this.bot.Start();
        }
    }

    public void Stop() {
        this.orderControl.Stop();
        this.quotationControl.Stop();
        this.tradeControl.Stop();
    }

    private void loadFXML() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("BotPane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void configureAlgoSettingsUI() {
        configureNumericTextField(this.maxPrice, () -> {
            return (int) (this.bot != null ? this.bot.getPriceLimit() : 0L);
        }, value -> {
            if (this.bot != null) {
                this.bot.setPriceLimit(value);
            }
        }, "MaxPrice");
        configureNumericTextField(this.startPrice, () -> {
            if (this.bot != null) {
                return this.bot.getStartPrice();
            }
            return 0;
        }, value2 -> {
            if (this.bot != null) {
                this.bot.setStartPrice(value2);
            }
        }, "StartPrice");
        configureNumericTextField(this.delayPlace, () -> {
            if (this.bot != null) {
                return this.bot.getDelayPlace();
            }
            return 0;
        }, value3 -> {
            if (this.bot != null) {
                this.bot.setDelayPlace(value3);
            }
        }, "DelayPlace");
        configureNumericTextField(this.positionLimit, () -> {
            if (this.bot != null) {
                return this.bot.getPositionLimit();
            }
            return 0;
        }, value4 -> {
            if (this.bot != null) {
                this.bot.setPositionLimit(value4);
            }
        }, "PositionLimit");
        configureNumericTextField(this.orderCountLimit, () -> {
            if (this.bot != null) {
                return this.bot.getOrderCountLimit();
            }
            return 0;
        }, value5 -> {
            if (this.bot != null) {
                this.bot.setOrderCountLimit(value5);
            }
        }, "OrderCountLimit");
        configureNumericTextField(this.orderSize, () -> {
            if (this.bot != null) {
                return this.bot.getOrderSize();
            }
            return 0;
        }, value6 -> {
            if (this.bot != null) {
                this.bot.setOrderSize(value6);
            }
        }, "OrderSize");
        configureNumericTextField(this.minOverlapStep, () -> {
            if (this.bot != null) {
                return this.bot.getMaxOverlapStep();
            }
            return 0;
        }, value7 -> {
            if (this.bot != null) {
                int instrumentPriceStep = this.bot.getInstrumentPriceStep();
                if (value7 % instrumentPriceStep != 0) {
                    value7 = (value7 / instrumentPriceStep) * instrumentPriceStep;
                }
                this.bot.setMinOverlapStep(value7);
                this.minOverlapStep.textProperty().setValue(String.valueOf(value7));
            }
        }, "MinOverlapStep");
        configureNumericTextField(this.maxOverlapStep, () -> {
            if (this.bot != null) {
                return this.bot.getMaxOverlapStep();
            }
            return 0;
        }, value8 -> {
            if (this.bot != null) {
                int instrumentPriceStep = this.bot.getInstrumentPriceStep();
                if (value8 % instrumentPriceStep != 0) {
                    value8 = (value8 / instrumentPriceStep) * instrumentPriceStep;
                }
                this.bot.setMaxOverlapStep(value8);
                this.maxOverlapStep.textProperty().setValue(String.valueOf(value8));
            }
        }, "MaxOverlapStep");
        configureDoubleNumericTextField(this.maxPricePercentage, () -> {
            if (this.bot != null) {
                return this.bot.getMaxPricePercentage();
            }
            return 0.0d;
        }, value9 -> {
            if (this.bot != null) {
                this.bot.setMaxPricePercentage(Math.min(value9, 100.0d));
                this.maxPricePercentage.textProperty().setValue(String.valueOf(Math.min(value9, 100.0d)));
            }
        }, "MaxPricePercentage");
        configureNumericTextField(this.startVolume, () -> {
            if (this.bot != null) {
                return this.bot.getStartVolume();
            }
            return 0;
        }, value10 -> {
            if (this.bot != null) {
                this.bot.setStartVolume(value10);
            }
        }, "StartVolume");
        configureNumericTextField(this.startTime, () -> {
            if (this.bot != null) {
                return Integer.parseInt(this.bot.getStartTime());
            }
            return 0;
        }, value11 -> {
            if (this.bot != null) {
                if (value11 < 1000) {
                    Context.serviceContext.notificationService.createError("Время должно быть представленно четырехзначных числом");
                    return;
                }
                String stringValue = Utils.validateTimeString(String.valueOf(value11), false);
                this.bot.setStartTime(stringValue);
                this.startTime.textProperty().setValue(stringValue);
                LocalTime time = Utils.convertToLocalTime(stringValue);
                this.timeSpreadLabel.setText("Старт " + Utils.LocalTimeToReadable(time, true) + " минус ");
            }
        }, "StartTime");
        configureNumericTextField(this.discreteAuctionEndTime, () -> {
            if (this.bot != null) {
                return Integer.parseInt(this.bot.getDiscreteAuctionEndTime());
            }
            return 0;
        }, value12 -> {
            if (this.bot != null) {
                if (value12 != 0 && value12 < 1000) {
                    Context.serviceContext.notificationService.createError("Время должно быть представленно четырехзначных числом (например, 1100) или нулем, если дискретный аукцион должен быть выключен для инструмента");
                    return;
                }
                String stringValue = Utils.validateTimeString(String.valueOf(value12), true);
                this.bot.setDiscreteAuctionEndTime(stringValue);
                this.discreteAuctionEndTime.textProperty().setValue(stringValue);
            }
        }, "DiscreteAuctionEndTime");
    }

    private void configureAccountsUI() {
        this.commAccounts = FXCollections.observableArrayList(this.algoilApp.getExecutionManager().getCommAccounts());
        this.commAccountsComboBox.setItems(this.commAccounts);
        this.moneyAccounts = FXCollections.observableArrayList(this.algoilApp.getExecutionManager().getMoneyAccounts());
        this.moneyAccountsComboBox.setItems(this.moneyAccounts);
        this.clientsDataList = this.algoilApp.getExecutionManager().getClients();
        this.clients = FXCollections.observableArrayList(this.clientsDataList.stream().map((v0) -> {
            return v0.getShortName();
        }).toList());
        this.clientControl.setItems(this.clients);
        this.clientControl.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (this.bot == null) {
                return;
            }
            this.bot.setClient(newValue);
            if (newValue == null) {
                disableAccounts();
            } else {
                enableAccounts();
            }
            for (ClientData client : this.clientsDataList) {
                if (this.bot.getClient() == null && !Helper.isClientDataExist(this.clientsDataList, this.bot)) {
                    this.moneyAccounts = FXCollections.observableArrayList(this.algoilApp.getExecutionManager().getMoneyAccounts());
                    this.commAccounts = FXCollections.observableArrayList(this.algoilApp.getExecutionManager().getCommAccounts());
                } else {
                    this.moneyAccounts.clear();
                    this.commAccounts.clear();
                    for (AccountData ad : client.getAccounts()) {
                        if (ad.getType().equals(AccountType.COMM)) {
                            this.commAccounts.add(ad.code);
                        } else {
                            this.moneyAccounts.add(ad.code);
                        }
                    }
                    synchronized (this.syncRoot) {
                        if (this.commAccounts.size() == 1) {
                            this.commAccountsComboBox.getSelectionModel().select((String) this.commAccounts.get(0));
                        } else {
                            this.commAccountsComboBox.getSelectionModel().select((Object) null);
                        }
                        if (this.moneyAccounts.size() == 1) {
                            this.moneyAccountsComboBox.getSelectionModel().select((String) this.moneyAccounts.get(0));
                        } else {
                            this.moneyAccountsComboBox.getSelectionModel().select((Object) null);
                        }
                        enableAccounts();
                    }
                    saveSettings();
                }
            }
            saveSettings();
        });
        this.commAccountsComboBox.getSelectionModel().selectedItemProperty().addListener((observable2, oldValue2, newValue2) -> {
            if (this.bot == null) {
                return;
            }
            this.bot.setCommodityAccount(newValue2);
            saveSettings();
            logger.debug("Обновлены настройки бота {}. Товарный счет изменён {} → {}", this.bot != null ? this.bot.getInstrument().getCode() : "", oldValue2, newValue2);
        });
        this.moneyAccountsComboBox.getSelectionModel().selectedItemProperty().addListener((observable3, oldValue3, newValue3) -> {
            if (this.bot == null) {
                return;
            }
            this.bot.setMoneyAccount(newValue3);
            saveSettings();
            logger.debug("Обновлены настройки бота {}. Денежный счет изменён {} → {}", this.bot != null ? this.bot.getInstrument().getCode() : "", oldValue3, newValue3);
        });
        this.algoilApp.getExecutionManager().Subscribe(this);
    }

    private void initBotModeListener() {
        this.botMode.setItems(FXCollections.observableArrayList(new BotMode[]{BotMode.Overlap, BotMode.TurboOverlap, BotMode.Catch}));
        this.botModeListener = (observable, oldValue, newValue) -> {
            if (this.bot == null) {
                return;
            }
            this.bot.setBotMode(newValue);
            switch (AnonymousClass4.$SwitchMap$ru$effectivegroup$client$algoil$bots$BotMode[newValue.ordinal()]) {
                case 1:
                    setDelayValues(1100, 1100);
                    break;
                case 2:
                    setDelayValues(0, 0);
                    break;
                case CipherHolder.CIPHER_PARAM_ENCRYPTED_SECRET_KEY /* 3 */:
                    setDelayValues(1100, 500);
                    break;
            }
            saveSettings();
            logger.debug("Обновлены настройки бота {}. Режим изменён {} → {}", this.bot.getInstrument().getCode(), oldValue, newValue);
        };
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: ru.effectivegroup.client.algoil.BotPane$4, reason: invalid class name */
    /* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/BotPane$4.class */
    public static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$AccountType;
        static final /* synthetic */ int[] $SwitchMap$ru$effectivegroup$client$algoil$bots$BotMode = new int[BotMode.values().length];

        static {
            try {
                $SwitchMap$ru$effectivegroup$client$algoil$bots$BotMode[BotMode.Overlap.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$algoil$bots$BotMode[BotMode.TurboOverlap.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$algoil$bots$BotMode[BotMode.Catch.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            $SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$AccountType = new int[AccountType.values().length];
            try {
                $SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$AccountType[AccountType.COMM.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$AccountType[AccountType.CASH.ordinal()] = 2;
            } catch (NoSuchFieldError e5) {
            }
            $SwitchMap$ru$effectivegroup$client$algoil$bots$states$BotStateKind = new int[BotStateKind.values().length];
            try {
                $SwitchMap$ru$effectivegroup$client$algoil$bots$states$BotStateKind[BotStateKind.Running.ordinal()] = 1;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$algoil$bots$states$BotStateKind[BotStateKind.Stopped.ordinal()] = 2;
            } catch (NoSuchFieldError e7) {
            }
        }
    }

    private void initBotStartModeListener() {
        this.botStartMode.setItems(FXCollections.observableArrayList(new BotStartMode[]{BotStartMode.Time, BotStartMode.Instrument}));
        this.botStartModeListener = (observable, oldValue, newValue) -> {
            if (this.bot == null) {
                return;
            }
            this.bot.setBotStartMode(newValue);
            saveSettings();
            logger.debug("Обновлены настройки бота {}. Режим старта бота изменён {} → {}", this.bot.getInstrument().getCode(), oldValue, newValue);
        };
    }

    private void configureBotModeListener(boolean enable) {
        if (enable) {
            this.botMode.getSelectionModel().selectedItemProperty().addListener(this.botModeListener);
        } else {
            this.botMode.getSelectionModel().selectedItemProperty().removeListener(this.botModeListener);
        }
    }

    private void configureBotStartModeListener(boolean enable) {
        if (enable) {
            this.botStartMode.getSelectionModel().selectedItemProperty().addListener(this.botStartModeListener);
        } else {
            this.botStartMode.getSelectionModel().selectedItemProperty().removeListener(this.botStartModeListener);
        }
    }

    private void setDelayValues(int place, int cancel) {
        this.bot.setDelayPlace(place);
        this.bot.setDelayCancel(cancel);
        Platform.runLater(() -> {
            this.delayPlace.textProperty().setValue(String.valueOf(place));
        });
    }

    private void configureComponentsResize() {
        DragResizer.makeResizable(this.settingsBox);
        DragResizer.makeResizable(this.quotationBox);
        DragResizer.makeResizable(this.orderBox);
        DragResizer.makeResizable(this.tradeBox);
        this.settingsBox.widthProperty().addListener((observable, oldValue, newValue) -> {
            this.runButton.setMaxWidth(((Double) newValue).doubleValue() - 18.0d);
            this.runButton.setMinWidth(((Double) newValue).doubleValue() - 18.0d);
            this.runButton.setPrefWidth(((Double) newValue).doubleValue() - 18.0d);
        });
    }

    private void configureHideTradeControlListener() {
        this.showOrderControlCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.booleanValue()) {
                this.orderBox.setMinWidth(240.0d);
                this.orderBox.setPrefWidth(315.0d);
            } else {
                this.orderBox.setMinWidth(0.0d);
                this.orderBox.setPrefWidth(0.0d);
            }
            this.botPaneSettingsManager.saveSettings("orderControlShowSelected", newValue.booleanValue());
        });
        this.showOrderControlCheckBox.selectedProperty().setValue(Boolean.valueOf(this.botPaneSettingsManager.getBotPaneSettings().isOrderControlShowSelected()));
    }

    private void configureFallDownListener() {
        this.fallDown.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (this.bot != null) {
                this.bot.setFallDown(newValue);
                saveSettings();
                logger.debug("Обновлены настройки бота {}. FallDown изменен на {}", this.bot.getInstrument().getCode(), newValue);
            }
        });
    }

    private void configureTimeSpread() {
        this.timeSpread.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.booleanValue()) {
                this.bot.setTimeSpread(Long.valueOf(Long.parseLong(this.timeSpread.getText())));
                saveSettings();
                logger.debug("Обновлены настройки бота {}. TimeSpread изменен на {} мс", this.bot.getInstrument().getCode(), this.timeSpread.getText());
            }
        });
    }

    private void configureQueue100Delay() {
        this.transactionQueue100Delay.focusedProperty().addListener((observable, oldValue, newValue) -> {
            int value = Integer.parseInt(this.transactionQueue100Delay.getText());
            this.transactionManager.setTransactionQueue100Delay(value);
            this.transactionQueueSettingsManager.saveSettings("transactionQueue100Delay", Integer.parseInt(this.transactionQueue100Delay.getText()));
        });
    }

    private void configureQueue100Size() {
        this.transactionQueue100Size.focusedProperty().addListener((observable, oldValue, newValue) -> {
            int value = Integer.parseInt(this.transactionQueue100Size.getText());
            this.transactionManager.setTransactionQueue100Size(value);
            this.transactionQueueSettingsManager.saveSettings("transactionQueue100Size", Integer.parseInt(this.transactionQueue100Size.getText()));
        });
    }

    private void configureQueue1000Delay() {
        this.transactionQueue1000Delay.focusedProperty().addListener((observable, oldValue, newValue) -> {
            int value = Integer.parseInt(this.transactionQueue1000Delay.getText());
            this.transactionManager.setTransactionQueue1000Delay(value);
            this.transactionQueueSettingsManager.saveSettings("transactionQueue1000Delay", Integer.parseInt(this.transactionQueue1000Delay.getText()));
        });
    }

    private void configureQueue1000Size() {
        this.transactionQueue1000Size.focusedProperty().addListener((observable, oldValue, newValue) -> {
            int value = Integer.parseInt(this.transactionQueue1000Size.getText());
            this.transactionManager.setTransactionQueue1000Size(value);
            this.transactionQueueSettingsManager.saveSettings("transactionQueue1000Size", value);
        });
    }

    private void configureRunButton() {
        this.runBotButtonDisabled = new BooleanBinding() { // from class: ru.effectivegroup.client.algoil.BotPane.1
            protected boolean computeValue() {
                return false;
            }
        };
        this.runButton.disableProperty().bind(this.runBotButtonDisabled);
        this.runBotButtonTextBinding = new StringBinding() { // from class: ru.effectivegroup.client.algoil.BotPane.2
            protected String computeValue() {
                return BotPane.this.runBotButtonText;
            }
        };
        this.runButton.textProperty().bind(this.runBotButtonTextBinding);
        this.positionLabelTextBinding = new StringBinding() { // from class: ru.effectivegroup.client.algoil.BotPane.3
            protected String computeValue() {
                return BotPane.this.positionLabelText;
            }
        };
        this.positionLabel.textProperty().bind(this.positionLabelTextBinding);
    }

    private void disableAccounts() {
        synchronized (this.syncRoot) {
            Platform.runLater(() -> {
                this.commAccountsComboBox.setDisable(true);
                this.commAccountsComboBox.valueProperty().setValue((Object) null);
                this.moneyAccountsComboBox.setDisable(true);
                this.moneyAccountsComboBox.valueProperty().setValue((Object) null);
                this.clientControl.valueProperty().setValue((Object) null);
            });
        }
    }

    private void enableAccounts() {
        synchronized (this.syncRoot) {
            Platform.runLater(() -> {
                this.commAccountsComboBox.setDisable(false);
                this.moneyAccountsComboBox.setDisable(false);
            });
        }
    }

    private void enableAllControls() {
        this.moneyAccountsComboBox.setDisable(false);
        this.commAccountsComboBox.setDisable(false);
        this.clientControl.setDisable(false);
        this.botMode.setDisable(false);
        this.botStartMode.setDisable(false);
        this.maxPrice.setDisable(false);
        this.startPrice.setDisable(false);
        this.delayPlace.setDisable(false);
        this.orderSize.setDisable(false);
        this.positionLimit.setDisable(false);
        this.maxOverlapStep.setDisable(false);
        this.minOverlapStep.setDisable(false);
        this.maxPricePercentage.setDisable(false);
        this.timeSpread.setDisable(false);
        this.orderCountLimit.setDisable(false);
        this.startVolume.setDisable(false);
        this.startTime.setDisable(false);
        this.discreteAuctionEndTime.setDisable(false);
        setPriceLimitsLabels();
        updateTodayOrdersCount();
        updatePositionLabel();
        setTradingVolume();
    }

    private void disableAllControls() {
        this.moneyAccountsComboBox.setDisable(true);
        this.commAccountsComboBox.setDisable(true);
        this.clientControl.setDisable(true);
        this.commAccountsComboBox.valueProperty().setValue((Object) null);
        this.moneyAccountsComboBox.valueProperty().setValue((Object) null);
        this.clientControl.valueProperty().setValue((Object) null);
        this.botMode.setDisable(true);
        this.botStartMode.setDisable(true);
        this.maxPrice.setDisable(true);
        this.startPrice.setDisable(true);
        this.delayPlace.setDisable(true);
        this.orderSize.setDisable(true);
        this.positionLimit.setDisable(true);
        this.maxOverlapStep.setDisable(true);
        this.minOverlapStep.setDisable(true);
        this.maxPricePercentage.setDisable(true);
        this.timeSpread.setDisable(true);
        this.orderCountLimit.setDisable(true);
        this.startVolume.setDisable(true);
        this.startTime.setDisable(true);
        this.discreteAuctionEndTime.setDisable(true);
        this.botMode.valueProperty().setValue((Object) null);
        this.botStartMode.valueProperty().setValue((Object) null);
        this.maxPrice.textProperty().setValue("0");
        this.startPrice.textProperty().setValue("0");
        this.delayPlace.textProperty().setValue("0");
        this.orderSize.textProperty().setValue("0");
        this.positionLimit.textProperty().setValue("0");
        this.maxOverlapStep.textProperty().setValue("0");
        this.orderTodayCountLabel.textProperty().set("0");
        this.maxPricePercentage.textProperty().set("0");
        this.timeSpread.textProperty().set("0");
        this.ourLowerPrice.textProperty().setValue("0");
        this.ourUpperPrice.textProperty().setValue("0");
        this.lowerPrice.textProperty().setValue("0");
        this.upperPrice.textProperty().setValue("0");
        this.currentPrice.textProperty().setValue("0");
        this.tradeVolume.textProperty().setValue("0");
        this.orderCountLimit.textProperty().setValue("0");
        this.minOverlapStep.textProperty().setValue("0");
        this.startVolume.textProperty().setValue("0");
        this.startTime.textProperty().setValue("0");
        this.discreteAuctionEndTime.textProperty().setValue("0");
        this.quotationPane.textProperty().setValue("Стакан");
        this.overlapLabel.textProperty().setValue("Шаг перебивания");
        updatePositionLabel();
    }

    private void updatePositionLabel() {
        if (this.bot != null) {
            this.positionLabelText = String.valueOf(this.bot.getPosition());
            Platform.runLater(() -> {
                this.positionLabelTextBinding.invalidate();
            });
        }
    }

    private void updateLimitsLabels() {
        if (this.bot.getCalculatedPriceLowerLimit() != null && this.bot.getCalculatedPriceUpperLimit() != null) {
            String ourLowerPriceText = this.format.format(this.bot.getCalculatedPriceLowerLimit());
            String ourUpperPriceText = this.format.format(this.bot.getCalculatedPriceUpperLimit());
            Platform.runLater(() -> {
                this.ourLowerPrice.setText(ourLowerPriceText);
                this.ourUpperPrice.setText(ourUpperPriceText);
            });
        }
    }

    private void updateOrderBookPosition() {
        String isFirstText = "";
        OrderBook book = null;
        if (this.bot != null) {
            book = this.algoilApp.getDataFeed().getOrderBook(this.bot.getInstrument());
        }
        if (book != null && book.hasOwnBids()) {
            isFirstText = book.getBids().stream().anyMatch((v0) -> {
                return v0.myOrderIsFirst();
            }) ? " - первые на уровне" : " - не первые на уровне";
        }
        String titledPaneText = "Стакан".concat(isFirstText);
        Platform.runLater(() -> {
            this.quotationPane.setText(titledPaneText);
        });
    }

    public QuotationControl getQuotationControl() {
        return this.quotationControl;
    }

    public OrderControl getOrderControl() {
        return this.orderControl;
    }

    public TradeControl getTradeControl() {
        return this.tradeControl;
    }

    public ComboBox<BotMode> getBotMode() {
        return this.botMode;
    }

    public ComboBox<BotStartMode> getBotStartMode() {
        return this.botStartMode;
    }

    public List<ClientData> getClientsDataList() {
        return this.clientsDataList;
    }

    /* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/BotPane$Helper.class */
    public static class Helper {
        public static boolean isBotSetupCorrectly(ComboBox<BotMode> botMode, ComboBox<BotStartMode> botStartMode, List<ClientData> clientsDataList, Bot bot) {
            boolean isOk = isClientDataExist(clientsDataList, bot) && bot.getMoneyAccount() != null && bot.getCommodityAccount() != null && isBotModeSetup(botMode) && isBotStartModeSetup(botStartMode);
            if (!isOk) {
                Context.serviceContext.notificationService.createError("Ошибка запуска бота, проверьте настройки");
            }
            return isOk;
        }

        public static boolean isClientDataExist(List<ClientData> clientsDataList, Bot bot) {
            boolean isExist = false;
            if (clientsDataList.size() != 0 && bot != null) {
                isExist = clientsDataList.stream().map((v0) -> {
                    return v0.getShortName();
                }).toList().contains(bot.getClient());
            }
            return isExist;
        }

        public static boolean isBotModeSetup(ComboBox<BotMode> botMode) {
            return botMode.getSelectionModel().getSelectedItem() != null;
        }

        public static boolean isBotStartModeSetup(ComboBox<BotStartMode> botStartMode) {
            return botStartMode.getSelectionModel().getSelectedItem() != null;
        }
    }
}
