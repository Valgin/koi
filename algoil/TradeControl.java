package ru.effectivegroup.client.algoil;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import ru.effectivegroup.client.algoil.bots.Bot;
import ru.effectivegroup.client.context.Context;
import ru.effectivegroup.client.model.data.filter.DataFilter;
import ru.effectivegroup.client.model.data.filter.Pageable;
import ru.effectivegroup.client.model.data.filter.Sorting;
import ru.effectivegroup.client.model.data.ste.TradeData;
import ru.effectivegroup.client.repository.DataRepository;
import ru.effectivegroup.client.utils.CollectionUtil;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/TradeControl.class */
public class TradeControl extends VBox implements BotsListControlEventsConsumer {
    private final AlgoilApp algoilApp = AlgoilApp.getInstance();
    private ScheduledFuture<?> tableUpdateTask;
    private TableView<TradeData> tradeDataTable;

    @FXML
    private VBox tradeBox;
    private Bot bot;

    @FXML
    private CheckBox onlyOwnCheckBox;

    @FXML
    private HBox box;

    public TradeControl() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TradeControl.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
            this.tradeBox.getChildren().clear();
            configureCheckBox();
            createTradeWidget();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override // ru.effectivegroup.client.algoil.BotsListControlEventsConsumer
    public void Handle(Bot bot) {
        if (bot == null) {
            return;
        }
        this.bot = bot;
        show();
    }

    @Override // ru.effectivegroup.client.algoil.BotsListControlEventsConsumer
    public void Handle(Integer size) {
        if (size.intValue() == 0) {
            this.tradeBox.getChildren().clear();
        }
    }

    protected void show() {
        try {
            if (this.tableUpdateTask != null) {
                this.tableUpdateTask.cancel(false);
            }
            DataRepository<TradeData> repository = Context.repositoryContext.get(TradeData.class, Map.of("securityCode", this.bot.getInstrument().getCode()));
            this.tableUpdateTask = Context.serviceContext.dataCollectionTaskExecutor.scheduleWithFixedDelay(() -> {
                DataFilter filter = new DataFilter();
                filter.setPageable(new Pageable());
                filter.getPageable().setSorting(Sorting.desc("creationTime"));
                List<TradeData> items = repository.getData(filter);
                if (this.onlyOwnCheckBox.isSelected()) {
                    items = repository.getData(filter).stream().filter(data -> {
                        return !data.getBuyerFirmCode().equals("");
                    }).toList();
                }
                if (!CollectionUtil.equals(items, this.tradeDataTable.getItems())) {
                    TradeData selected = (TradeData) this.tradeDataTable.getSelectionModel().getSelectedItem();
                    this.tradeDataTable.getItems().clear();
                    this.tradeDataTable.getItems().addAll(items);
                    if (items.contains(selected)) {
                        this.tradeDataTable.getSelectionModel().select(selected);
                    }
                }
            }, 0L, 1000L, TimeUnit.MILLISECONDS);
            Platform.runLater(() -> {
                this.tradeBox.getChildren().clear();
                this.tradeBox.getChildren().addAll(new Node[]{this.tradeDataTable, this.box});
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTradeWidget() {
        this.tradeDataTable = new TableView<>();
        TradeComponent.setRowFactory(this.tradeDataTable);
        this.tradeDataTable.getColumns().addAll(TradeComponent.createColumns());
        this.tradeDataTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.tradeDataTable.setPlaceholder(new Label(" "));
        this.tradeDataTable.setMaxHeight(390.0d);
        this.tradeDataTable.setPrefHeight(390.0d);
        this.tradeDataTable.skinProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ru.effectivegroup.client.gui.utils.Utils.hideNode(ru.effectivegroup.client.gui.utils.Utils.findScrollBar(newValue.getNode(), Orientation.HORIZONTAL));
            }
        });
    }

    public void configureCheckBox() {
        this.onlyOwnCheckBox.selectedProperty().setValue(Boolean.valueOf(this.algoilApp.getBotPaneManager().getBotPaneSettings().isTradeControlOwnSelected()));
        this.onlyOwnCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            this.algoilApp.getBotPaneManager().saveSettings("tradeControlOwnSelected", newValue.booleanValue());
        });
    }

    public void Stop() {
        LogManager.getLogger("algoil").info("Остановлен поток обработки таблицы сделок TradeControl");
        this.tableUpdateTask.cancel(false);
    }
}
