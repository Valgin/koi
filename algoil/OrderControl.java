package ru.effectivegroup.client.algoil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
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
import ru.effectivegroup.client.model.data.ste.OrderData;
import ru.effectivegroup.client.model.data.ste.dictionary.OrderStatus;
import ru.effectivegroup.client.repository.DataRepository;
import ru.effectivegroup.client.utils.CollectionUtil;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/OrderControl.class */
public class OrderControl extends VBox implements BotsListControlEventsConsumer {
    private final AlgoilApp algoilApp = AlgoilApp.getInstance();
    private Bot bot;
    private ScheduledFuture<?> tableUpdateTask;

    @FXML
    private TableView<OrderData> orderDataTable;

    @FXML
    private VBox orderBox;

    @FXML
    private HBox box;

    @FXML
    private CheckBox onlyOwnCheckBox;

    @FXML
    private CheckBox onlyQueuedCheckBox;

    public OrderControl() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("OrderControl.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
            this.orderBox.getChildren().clear();
            configureCheckBox();
            createOrderWidget();
        } catch (IOException exception) {
            exception.printStackTrace();
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
            this.orderBox.getChildren().clear();
        }
    }

    protected void show() {
        if (this.tableUpdateTask != null) {
            this.tableUpdateTask.cancel(false);
        }
        DataRepository<OrderData> repository = Context.repositoryContext.get(OrderData.class, Map.of("securityCode", this.bot.getInstrument().getCode()));
        this.tableUpdateTask = Context.serviceContext.dataCollectionTaskExecutor.scheduleWithFixedDelay(() -> {
            DataFilter filter = new DataFilter();
            filter.setPageable(new Pageable());
            filter.getPageable().setSorting(Sorting.desc("creationTime"));
            Predicate<OrderData> ownPredicate = orderData -> {
                return !Objects.equals(orderData.getTrn(), "");
            };
            Predicate<OrderData> queuedPredicate = orderData2 -> {
                return orderData2.getStatus().equals(OrderStatus.QUEUED);
            };
            List<OrderData> items = repository.getData(filter);
            if (this.onlyOwnCheckBox.isSelected()) {
                items = repository.getData(filter).stream().filter(ownPredicate).toList();
            }
            if (this.onlyQueuedCheckBox.isSelected()) {
                items = repository.getData(filter).stream().filter(queuedPredicate).toList();
            }
            if (this.onlyOwnCheckBox.isSelected() && this.onlyQueuedCheckBox.isSelected()) {
                items = repository.getData(filter).stream().filter(queuedPredicate).filter(ownPredicate).toList();
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String today = dateFormat.format(new Date());
            List<OrderData> filteredItems = items.stream().filter(orderData3 -> {
                return dateFormat.format(orderData3.getCreationTime()).equals(today);
            }).toList();
            if (!CollectionUtil.equals(filteredItems, this.orderDataTable.getItems())) {
                OrderData selected = (OrderData) this.orderDataTable.getSelectionModel().getSelectedItem();
                this.orderDataTable.getItems().clear();
                this.orderDataTable.getItems().addAll(filteredItems);
                if (filteredItems.contains(selected)) {
                    this.orderDataTable.getSelectionModel().select(selected);
                }
            }
        }, 0L, 1000L, TimeUnit.MILLISECONDS);
        Platform.runLater(() -> {
            this.orderBox.getChildren().clear();
            this.orderBox.getChildren().addAll(new Node[]{this.orderDataTable, this.box});
        });
    }

    private void createOrderWidget() {
        this.orderDataTable = new TableView<>();
        OrderComponent.setRowFactory(this.orderDataTable);
        this.orderDataTable.getColumns().addAll(OrderComponent.createColumns());
        this.orderDataTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.orderDataTable.setPlaceholder(new Label(" "));
        this.orderDataTable.setPrefHeight(390.0d);
        this.orderDataTable.setMaxHeight(390.0d);
        this.orderDataTable.skinProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ru.effectivegroup.client.gui.utils.Utils.hideNode(ru.effectivegroup.client.gui.utils.Utils.findScrollBar(newValue.getNode(), Orientation.HORIZONTAL));
            }
        });
    }

    public void configureCheckBox() {
        this.onlyOwnCheckBox.selectedProperty().setValue(Boolean.valueOf(this.algoilApp.getBotPaneManager().getBotPaneSettings().isOrderControlOwnSelected()));
        this.onlyOwnCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            this.algoilApp.getBotPaneManager().saveSettings("orderControlOwnSelected", newValue.booleanValue());
        });
        this.onlyQueuedCheckBox.selectedProperty().setValue(Boolean.valueOf(this.algoilApp.getBotPaneManager().getBotPaneSettings().isOrderControlQueuedSelected()));
        this.onlyQueuedCheckBox.selectedProperty().addListener((observable2, oldValue2, newValue2) -> {
            this.algoilApp.getBotPaneManager().saveSettings("orderControlQueuedSelected", newValue2.booleanValue());
        });
    }

    public void Stop() {
        LogManager.getLogger("algoil").info("Остановлен поток обработки таблицы заявок OrderControl");
        this.tableUpdateTask.cancel(false);
    }
}
