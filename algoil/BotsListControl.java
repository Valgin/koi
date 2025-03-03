package ru.effectivegroup.client.algoil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.effectivegroup.client.algoil.bots.Bot;
import ru.effectivegroup.client.algoil.bots.BotManagerEventsConsumer;
import ru.effectivegroup.client.gui.control.custom.toggle.EfToggleButton;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/BotsListControl.class */
public class BotsListControl extends VBox implements BotManagerEventsConsumer {
    private ObservableList<Bot> commonBotsObservableList;
    private ObservableList<Bot> fixedBotsObservableList;
    private Bot selectedBot;

    @FXML
    public ListView<Bot> CommonBotsListView;

    @FXML
    public ListView<Bot> FixedBotsListView;

    @FXML
    public EfToggleButton button;

    @FXML
    public Button toFixedListButton;

    @FXML
    public Button toCommonListButton;

    @FXML
    public Label sortLabel;
    private final Logger logger = LogManager.getLogger("algoil");
    private final Object syncRoot = new Object();
    private HashSet<BotsListControlEventsConsumer> eventsConsumers = new HashSet<>();
    private final AlgoilApp algoilApp = AlgoilApp.getInstance();

    public BotsListControl() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("BotsListControl.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
            configureSortButton();
            configureListViewSelection(this.CommonBotsListView, this.FixedBotsListView, true, false);
            configureListViewSelection(this.FixedBotsListView, this.CommonBotsListView, false, true);
            configureBotsMovingButtons(this.toCommonListButton, this.fixedBotsObservableList, this.commonBotsObservableList, this.CommonBotsListView, false);
            configureBotsMovingButtons(this.toFixedListButton, this.commonBotsObservableList, this.fixedBotsObservableList, this.FixedBotsListView, true);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @FXML
    public void initialize() {
        List<Bot> bots = this.algoilApp.getBotManager().getBots().stream().sorted(Comparator.comparing((v0) -> {
            return v0.getInstrument();
        })).toList();
        this.logger.info("Инициализация списка ботов ({} штук) в интерфейсе.", Integer.valueOf(bots.size()));
        this.CommonBotsListView.setCellFactory(param -> {
            return new BotListViewCell(this.algoilApp.getDataFeed());
        });
        synchronized (this.syncRoot) {
            this.commonBotsObservableList = FXCollections.observableArrayList(bots.stream().filter(b -> {
                return !b.isBotListFixation();
            }).toList());
        }
        this.FixedBotsListView.setCellFactory(param2 -> {
            return new BotListViewCell(this.algoilApp.getDataFeed());
        });
        synchronized (this.syncRoot) {
            this.fixedBotsObservableList = FXCollections.observableArrayList(bots.stream().filter((v0) -> {
                return v0.isBotListFixation();
            }).toList());
        }
        BotListSize();
        this.CommonBotsListView.setItems(this.commonBotsObservableList);
        this.FixedBotsListView.setItems(this.fixedBotsObservableList);
        getSettingsAndSortList();
        this.algoilApp.getBotManager().Subscribe(this);
    }

    public void SubscribeSelectionChanged(BotsListControlEventsConsumer consumer) {
        synchronized (this.syncRoot) {
            this.eventsConsumers.add(consumer);
        }
        if (!this.commonBotsObservableList.isEmpty()) {
            RaiseSelectedBotChanged((Bot) this.commonBotsObservableList.get(0));
        }
    }

    public Bot getSelectedBot() {
        return this.selectedBot;
    }

    @Override // ru.effectivegroup.client.algoil.bots.BotManagerEventsConsumer
    public void HandleNewBot(Bot bot) {
        synchronized (this.syncRoot) {
            this.commonBotsObservableList.add(bot);
        }
        this.CommonBotsListView.getSelectionModel().select(bot);
        BotListSize();
    }

    @Override // ru.effectivegroup.client.algoil.bots.BotManagerEventsConsumer
    public void HandleBotDeleted(Bot bot) {
        synchronized (this.syncRoot) {
            botRemoval(bot, this.commonBotsObservableList, this.FixedBotsListView);
            botRemoval(bot, this.fixedBotsObservableList, this.CommonBotsListView);
            bot.StopProcessing();
        }
        BotListSize();
    }

    public void configureBotsMovingButtons(Button btn, ObservableList<Bot> sourceList, ObservableList<Bot> targetList, ListView<Bot> targetListView, boolean fixation) {
        btn.setOnAction(event -> {
            if (sourceList.contains(this.selectedBot)) {
                targetList.add(this.selectedBot);
                sourceList.remove(this.selectedBot);
                targetListView.getSelectionModel().selectLast();
                targetListView.scrollTo(this.selectedBot);
                this.selectedBot.setBotListFixation(fixation);
                AlgoilApp.getInstance().getBotManager().SaveBotSettings(this.selectedBot);
            }
        });
    }

    private void configureListViewSelection(ListView<Bot> selectedListView, ListView<Bot> otherListView, boolean commonButton, boolean fixedButton) {
        selectedListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedListView.requestFocus();
                otherListView.getSelectionModel().clearSelection();
                this.selectedBot = newValue;
                RaiseSelectedBotChanged(this.selectedBot);
                this.toCommonListButton.setDisable(commonButton);
                this.toFixedListButton.setDisable(fixedButton);
            }
        });
    }

    private void RaiseSelectedBotChanged(Bot bot) {
        ArrayList<BotsListControlEventsConsumer> consumers;
        synchronized (this.syncRoot) {
            consumers = new ArrayList<>(this.eventsConsumers);
        }
        if (this.eventsConsumers.size() > 0) {
            Iterator<BotsListControlEventsConsumer> it = consumers.iterator();
            while (it.hasNext()) {
                BotsListControlEventsConsumer cons = it.next();
                cons.Handle(bot);
            }
        }
    }

    private void botRemoval(Bot bot, ObservableList<Bot> observableList, ListView<Bot> listView) {
        if (observableList.contains(bot)) {
            observableList.remove(bot);
            if (observableList.isEmpty()) {
                listView.getSelectionModel().selectFirst();
                listView.scrollTo(0);
            }
        }
    }

    private void BotListSize() {
        ArrayList<BotsListControlEventsConsumer> consumers;
        synchronized (this.syncRoot) {
            consumers = new ArrayList<>(this.eventsConsumers);
        }
        if (this.eventsConsumers.size() > 0) {
            Iterator<BotsListControlEventsConsumer> it = consumers.iterator();
            while (it.hasNext()) {
                BotsListControlEventsConsumer cons = it.next();
                cons.Handle(Integer.valueOf(this.commonBotsObservableList.size()));
            }
        }
    }

    private void configureSortButton() {
        this.button.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.booleanValue()) {
                this.algoilApp.getSettingsManager().SaveSettings(Boolean.TRUE);
                sortByCode();
            } else {
                this.algoilApp.getSettingsManager().SaveSettings(Boolean.FALSE);
                sortByFullName();
            }
        });
    }

    private void getSettingsAndSortList() {
        Boolean sortSetting = (Boolean) this.algoilApp.getSettingsManager().GetSettings(BotsListControl.class);
        if (sortSetting != null) {
            if (sortSetting == Boolean.TRUE) {
                sortByCode();
            } else {
                sortByFullName();
            }
            this.button.setSelected(sortSetting.booleanValue());
        }
    }

    private void sortByCode() {
        Platform.runLater(() -> {
            this.logger.info("Лист ботов сортируется по коду");
            this.sortLabel.setText("Текущая сортировка: по коду");
            this.CommonBotsListView.getItems().sort((b1, b2) -> {
                return b1.getInstrument().getCode().compareToIgnoreCase(b2.getInstrument().getCode());
            });
            this.CommonBotsListView.scrollTo(this.selectedBot);
        });
    }

    private void sortByFullName() {
        Platform.runLater(() -> {
            this.logger.info("Лист ботов сортируется по названию инструмента");
            this.sortLabel.setText("Текущая сортировка: по инструменту");
            this.CommonBotsListView.getItems().sort((b1, b2) -> {
                return b1.getInstrument().getFullName().compareToIgnoreCase(b2.getInstrument().getFullName());
            });
            this.CommonBotsListView.scrollTo(this.selectedBot);
        });
    }
}
