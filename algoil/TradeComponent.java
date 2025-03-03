package ru.effectivegroup.client.algoil;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import ru.effectivegroup.client.crypto.cipher.CipherHolder;
import ru.effectivegroup.client.gui.control.base.table.content.TableBaseRow;
import ru.effectivegroup.client.model.data.ste.TradeData;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/TradeComponent.class */
public class TradeComponent {
    public static Collection<? extends TableColumn<TradeData, ?>> createColumns() {
        TableColumn<TradeData, TradeData> tradeCodeColummn = new TableColumn<>();
        tradeCodeColummn.setMinWidth(95.0d);
        tradeCodeColummn.setMaxWidth(120.0d);
        tradeCodeColummn.setText("Номер сделки");
        tradeCodeColummn.setSortable(false);
        tradeCodeColummn.setCellValueFactory(q -> {
            return new SimpleObjectProperty((TradeData) q.getValue());
        });
        tradeCodeColummn.setCellFactory(param -> {
            return getFormatter("trade-code");
        });
        TableColumn<TradeData, TradeData> volumeColumn = new TableColumn<>();
        volumeColumn.setMinWidth(35.0d);
        volumeColumn.setMaxWidth(70.0d);
        volumeColumn.setText("Объем");
        volumeColumn.setSortable(false);
        volumeColumn.setCellValueFactory(q2 -> {
            return new SimpleObjectProperty((TradeData) q2.getValue());
        });
        volumeColumn.setCellFactory(param2 -> {
            return getFormatter("trade-volume");
        });
        TableColumn<TradeData, TradeData> timeColumn = new TableColumn<>();
        timeColumn.setMinWidth(40.0d);
        timeColumn.setMaxWidth(90.0d);
        timeColumn.setText("Время");
        timeColumn.setSortable(false);
        timeColumn.setCellValueFactory(q3 -> {
            return new SimpleObjectProperty((TradeData) q3.getValue());
        });
        timeColumn.setCellFactory(param3 -> {
            return getFormatter("trade-time");
        });
        TableColumn<TradeData, TradeData> priceColumn = new TableColumn<>();
        priceColumn.setMinWidth(55.0d);
        priceColumn.setMaxWidth(55.0d);
        priceColumn.setText("Цена");
        priceColumn.setSortable(false);
        priceColumn.setCellValueFactory(q4 -> {
            return new SimpleObjectProperty((TradeData) q4.getValue());
        });
        priceColumn.setCellFactory(param4 -> {
            return getFormatter("trade-price");
        });
        return List.of(timeColumn, priceColumn, volumeColumn, tradeCodeColummn);
    }

    public static void setRowFactory(TableView<TradeData> table) {
        table.setRowFactory(tableView -> {
            TableRow<TradeData> row = new TableBaseRow<>();
            row.setAlignment(Pos.CENTER_RIGHT);
            row.setMaxHeight(18.0d);
            row.setMinHeight(18.0d);
            row.setPrefHeight(18.0d);
            return row;
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static TableCell<TradeData, TradeData> getFormatter(final String property) {
        return new TableCell<TradeData, TradeData>() { // from class: ru.effectivegroup.client.algoil.TradeComponent.1
            /* JADX INFO: Access modifiers changed from: protected */
            public void updateItem(TradeData item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setGraphic(null);
                getStyleClass().removeAll(new String[]{"quotation-price"});
                if (!empty) {
                    if (!item.getBuyerFirmCode().equals("")) {
                        getStyleClass().add("quotation-price");
                    }
                    alignmentProperty().setValue(Pos.CENTER);
                    TradeComponent.ConfigureCellText(this, item, property);
                }
            }
        };
    }

    private static void ConfigureCellText(TableCell component, TradeData item, String property) {
        boolean z = -1;
        switch (property.hashCode()) {
            case 876092320:
                if (property.equals("trade-price")) {
                    z = 3;
                    break;
                }
                break;
            case 1558169507:
                if (property.equals("trade-volume")) {
                    z = false;
                    break;
                }
                break;
            case 1828986038:
                if (property.equals("trade-code")) {
                    z = 2;
                    break;
                }
                break;
            case 1829486998:
                if (property.equals("trade-time")) {
                    z = true;
                    break;
                }
                break;
        }
        switch (z) {
            case false:
                component.setText(String.valueOf(item.getQuantity()));
                return;
            case true:
                LocalTime time = Instant.ofEpochMilli(item.getCreationTime().getTime()).atZone(ZoneId.systemDefault()).toLocalTime();
                component.setText(String.valueOf(time));
                return;
            case true:
                String result = "";
                if (!item.getBuyerFirmCode().equals("")) {
                    result = String.valueOf(item.getCode());
                }
                component.setText(result);
                return;
            case CipherHolder.CIPHER_PARAM_ENCRYPTED_SECRET_KEY /* 3 */:
                component.setText(new DecimalFormat("###,###,###").format(item.getPrice() / 100));
                return;
            default:
                return;
        }
    }
}
