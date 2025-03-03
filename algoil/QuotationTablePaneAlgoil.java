package ru.effectivegroup.client.algoil;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableListBase;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import ru.effectivegroup.client.context.Context;
import ru.effectivegroup.client.gui.control.money.Formatter;
import ru.effectivegroup.client.gui.service.formatter.TableFormattingService;
import ru.effectivegroup.client.gui.widget.impl.ste.quotation.STEQuotationTable;
import ru.effectivegroup.client.gui.window.order.quotation.QuotationTableCell;
import ru.effectivegroup.client.model.Money;
import ru.effectivegroup.client.model.data.QuotationBaseData;
import ru.effectivegroup.client.model.widget.table.TableColumnContent;
import ru.effectivegroup.client.model.widget.table.TableColumnType;
import ru.effectivegroup.client.repository.TableColumnContentModel;
import ru.effectivegroup.client.repository.TableFormattingModel;
import ru.effectivegroup.client.service.quotation.BaseQuotationTable;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/QuotationTablePaneAlgoil.class */
public class QuotationTablePaneAlgoil<Q extends QuotationBaseData> extends VBox {
    private TableView<Q> quotationDataTable;
    private TableView<Q> quotationTotalTable;

    public void init(String quotationType, ObservableListBase<Q> items, ObservableListBase<Q> totalItem) {
        this.quotationDataTable = new TableView<>();
        initTable(this.quotationDataTable, false, quotationType);
        this.quotationDataTable.getStyleClass().addAll(new String[]{"table-widget"});
        this.quotationTotalTable = new TableView<>();
        initTable(this.quotationTotalTable, true, quotationType);
        this.quotationTotalTable.getStyleClass().addAll(new String[]{"total-table-view", "totals"});
        this.quotationTotalTable.setMaxHeight(20.0d);
        this.quotationTotalTable.setPrefHeight(20.0d);
        this.quotationDataTable.setItems(items);
        this.quotationTotalTable.setItems(totalItem);
        VBox.setVgrow(this.quotationDataTable, Priority.ALWAYS);
        getChildren().addAll(new Node[]{this.quotationDataTable, this.quotationTotalTable});
    }

    private void initTable(TableView<Q> tableView, boolean isTotalTable, String quotationType) {
        tableView.setMinWidth(352.0d);
        tableView.setMaxWidth(352.0d);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPlaceholder(new Label(""));
        tableView.getColumns().addAll(createColumns(isTotalTable));
        tableView.skinProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ru.effectivegroup.client.gui.utils.Utils.hideNode(ru.effectivegroup.client.gui.utils.Utils.findScrollBar(newValue.getNode(), Orientation.HORIZONTAL));
            }
        });
    }

    public Collection<? extends TableColumn<Q, ?>> createColumns(boolean isTotalTable) {
        Predicate colorPredicate;
        if (isTotalTable) {
            colorPredicate = item -> {
                return false;
            };
        } else {
            colorPredicate = null;
        }
        TableColumn<Q, Long> buyVolumeLeft = new TableColumn<>();
        buyVolumeLeft.setMinWidth(35.0d);
        buyVolumeLeft.setMaxWidth(50.0d);
        buyVolumeLeft.setText(Context.bundle.getString("own.volume.active.b"));
        buyVolumeLeft.setSortable(false);
        buyVolumeLeft.setCellValueFactory(q -> {
            return new SimpleObjectProperty(Long.valueOf(((QuotationBaseData) q.getValue()).getBuyMyCount()));
        });
        Predicate predicate = colorPredicate;
        buyVolumeLeft.setCellFactory(param -> {
            return new QuotationTableCell("quotation-buy", getTableNumFormatter("OrdersBuy"), predicate, buyVolumeLeft.widthProperty(), isTotalTable);
        });
        TableColumn<Q, Long> buyVolumeLeftColumn = new TableColumn<>();
        buyVolumeLeftColumn.setMinWidth(35.0d);
        buyVolumeLeftColumn.setMaxWidth(50.0d);
        buyVolumeLeftColumn.setText(Context.bundle.getString("volume.b"));
        buyVolumeLeftColumn.setSortable(true);
        buyVolumeLeftColumn.setCellValueFactory(q2 -> {
            return new SimpleObjectProperty(Long.valueOf(((QuotationBaseData) q2.getValue()).getBuyVolumeLeft()));
        });
        Predicate predicate2 = colorPredicate;
        buyVolumeLeftColumn.setCellFactory(param2 -> {
            return new QuotationTableCell("quotation-buy", getTableNumFormatter("VolumeBuy"), predicate2, buyVolumeLeftColumn.widthProperty(), isTotalTable);
        });
        TableColumn<Q, Long> priceColumn = new TableColumn<>();
        priceColumn.setMinWidth(60.0d);
        priceColumn.setMaxWidth(60.0d);
        priceColumn.setSortType(TableColumn.SortType.DESCENDING);
        priceColumn.setText(Context.bundle.getString(BaseQuotationTable.PRICE_FIELD_NAME));
        priceColumn.setSortable(false);
        priceColumn.setCellValueFactory(q3 -> {
            return new SimpleObjectProperty(Long.valueOf(((QuotationBaseData) q3.getValue()).getPrice()));
        });
        priceColumn.setCellFactory(param3 -> {
            return new QuotationTableCell("quotation-price", getTableMoneyFormatter("Price"), q4 -> {
                return new Formatter().format(new Money(Long.parseLong(String.valueOf(q4))));
            }, q5 -> {
                return (isTotalTable || q5 == null || (q5.getBuyMyCount() <= 0 && q5.getSellMyCount() <= 0)) ? false : true;
            }, priceColumn.widthProperty(), false);
        });
        TableColumn<Q, Long> sellVolumeLeftColumn = new TableColumn<>();
        sellVolumeLeftColumn.setMinWidth(35.0d);
        sellVolumeLeftColumn.setMaxWidth(50.0d);
        sellVolumeLeftColumn.setText(Context.bundle.getString("volume.s"));
        sellVolumeLeftColumn.setSortable(false);
        sellVolumeLeftColumn.setCellValueFactory(q4 -> {
            return new SimpleObjectProperty(Long.valueOf(((QuotationBaseData) q4.getValue()).getSellVolumeLeft()));
        });
        Predicate predicate3 = colorPredicate;
        sellVolumeLeftColumn.setCellFactory(param4 -> {
            return new QuotationTableCell("quotation-sell", getTableNumFormatter("OrdersSell"), predicate3, sellVolumeLeftColumn.widthProperty(), isTotalTable);
        });
        return List.of(buyVolumeLeft, buyVolumeLeftColumn, priceColumn, sellVolumeLeftColumn);
    }

    private ru.effectivegroup.client.gui.widget.table.formatter.Formatter<Q, Long> getTableNumFormatter(String name) {
        return getTableFormatter(name, TableColumnType.NUMBER);
    }

    private ru.effectivegroup.client.gui.widget.table.formatter.Formatter<Q, Long> getTableMoneyFormatter(String name) {
        return getTableFormatter(name, TableColumnType.MONEY);
    }

    private ru.effectivegroup.client.gui.widget.table.formatter.Formatter<Q, Long> getTableFormatter(String name, TableColumnType type) {
        TableColumnContent columnContent = new TableColumnContent(name, name, type);
        TableFormattingService formattingService = Context.serviceContext.tableFormattingService;
        TableFormattingModel tableFormat = formattingService.loadTableFormat(STEQuotationTable.TYPE, (TableFormattingModel) null, List.of(columnContent), true);
        return formattingService.m91createFormatter(STEQuotationTable.TYPE, (TableColumnContentModel) columnContent, tableFormat);
    }

    public TableView<Q> getQuotationDataTable() {
        return this.quotationDataTable;
    }

    public TableView<Q> getQuotationTotalTable() {
        return this.quotationTotalTable;
    }
}
