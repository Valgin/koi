package ru.effectivegroup.client.algoil;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableListBase;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import ru.effectivegroup.client.NewContext;
import ru.effectivegroup.client.algoil.bots.Bot;
import ru.effectivegroup.client.context.Context;
import ru.effectivegroup.client.gui.control.base.table.content.TableBaseRow;
import ru.effectivegroup.client.gui.widget.impl.ste.quotation.STEQuotationTable;
import ru.effectivegroup.client.model.Widget;
import ru.effectivegroup.client.model.data.filter.Sorting;
import ru.effectivegroup.client.service.quotation.BaseQuotationTable;
import ru.effectivegroup.client.ste.quotation.api.STEQuotationTableSpecificService;
import ru.effectivegroup.client.ste.quotation.model.STEQuotationViewItem;
import ru.effectivegroup.client.ste.quotation.view.STEQuotationsItemsListItem;
import ru.effectivegroup.client.ste.quotation.view.STEQuotationsTotalList;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/QuotationControl.class */
public class QuotationControl extends VBox implements BotsListControlEventsConsumer {
    private final AlgoilApp algoilApp = AlgoilApp.getInstance();

    @FXML
    private VBox quotationBox;

    @FXML
    private QuotationTablePaneAlgoil<STEQuotationViewItem> quotationTablePane;
    private STEQuotationTableSpecificService specificService;
    protected final VBox quotationsContainer;
    private Bot bot;

    public QuotationControl() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("QuotationControl.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
            this.quotationsContainer = new VBox();
            this.quotationBox.getChildren().clear();
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
            this.quotationBox.getChildren().clear();
        }
    }

    protected void show() {
        if (this.quotationTablePane != null) {
            this.quotationTablePane = null;
        }
        String securityCode = this.bot.getInstrument().getCode();
        STEQuotationTable widget = new STEQuotationTable(securityCode);
        widget.setSort(Sorting.desc(BaseQuotationTable.PRICE_FIELD_NAME));
        this.quotationTablePane = new QuotationTablePaneAlgoil<>();
        Consumer<List<Integer>> selectionConsumer = indices -> {
            TableView.TableViewSelectionModel<STEQuotationViewItem> selectionModel = this.quotationTablePane.getQuotationDataTable().getSelectionModel();
            selectionModel.clearSelection();
            if (!indices.isEmpty()) {
                Objects.requireNonNull(selectionModel);
                Objects.requireNonNull(selectionModel);
                indices.forEach(x$0 -> {
                    selectionModel.clearAndSelect(x$0);
                });
            }
        };
        ObservableListBase<STEQuotationViewItem> sTEQuotationsItemsListItem = new STEQuotationsItemsListItem<>(() -> {
            return this.quotationTablePane.getQuotationDataTable().getSelectionModel().getSelectedItems();
        }, selectionConsumer, (ObjectProperty) null);
        ObservableListBase<STEQuotationViewItem> sTEQuotationsTotalList = new STEQuotationsTotalList<>();
        this.specificService = Context.serviceContext.steServicesFactory.getQuotationTableService(new SimpleBooleanProperty(true), new SimpleObjectProperty(Widget.WindowState.NORMAL), securityCode, widget.getSorting(), widget.dataFilter(), (Supplier) null, sTEQuotationsItemsListItem, sTEQuotationsTotalList, NewContext.applicationModelController);
        this.quotationTablePane.init(STEQuotationTable.TYPE, sTEQuotationsItemsListItem, sTEQuotationsTotalList);
        this.specificService.initialization();
        this.quotationTablePane.getQuotationDataTable().setRowFactory(tableView -> {
            TableRow<STEQuotationViewItem> row = new TableBaseRow<>();
            row.setAlignment(Pos.CENTER_RIGHT);
            row.setMaxHeight(18.0d);
            row.setMinHeight(18.0d);
            row.setPrefHeight(18.0d);
            return row;
        });
        this.quotationTablePane.getQuotationTotalTable().setRowFactory(tableView2 -> {
            TableRow<STEQuotationViewItem> row = new TableBaseRow<>();
            row.setAlignment(Pos.CENTER_RIGHT);
            row.setMaxHeight(18.0d);
            row.setMinHeight(18.0d);
            row.setPrefHeight(18.0d);
            return row;
        });
        this.quotationTablePane.setMinWidth(220.0d);
        this.quotationTablePane.setMaxWidth(220.0d);
        this.quotationTablePane.getQuotationDataTable().setMaxWidth(220.0d);
        this.quotationTablePane.getQuotationDataTable().setMinWidth(220.0d);
        this.quotationTablePane.getQuotationTotalTable().setMinWidth(220.0d);
        this.quotationTablePane.getQuotationTotalTable().setMinWidth(220.0d);
        Platform.runLater(() -> {
            this.quotationBox.getChildren().clear();
            this.quotationBox.getChildren().addAll(new Node[]{this.quotationTablePane});
        });
    }

    public void Stop() {
        LogManager.getLogger("algoil").info("Остановлен поток обработки стакана QuotationControl");
    }
}
