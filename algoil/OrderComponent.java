package ru.effectivegroup.client.algoil;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import ru.effectivegroup.client.crypto.cipher.CipherHolder;
import ru.effectivegroup.client.gui.control.base.table.content.TableBaseRow;
import ru.effectivegroup.client.model.data.ste.OrderData;
import ru.effectivegroup.client.model.data.ste.dictionary.BSType;
import ru.effectivegroup.client.model.data.ste.dictionary.OrderStatus;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/OrderComponent.class */
public class OrderComponent {
    public static Collection<? extends TableColumn<OrderData, ?>> createColumns() {
        TableColumn<OrderData, OrderData> timeColumn = new TableColumn<>();
        timeColumn.setMinWidth(65.0d);
        timeColumn.setPrefWidth(110.0d);
        timeColumn.setMaxWidth(120.0d);
        timeColumn.setText("Время");
        timeColumn.setSortable(false);
        timeColumn.setCellValueFactory(q -> {
            return new SimpleObjectProperty((OrderData) q.getValue());
        });
        timeColumn.setCellFactory(param -> {
            return getFormatter("order-time");
        });
        TableColumn<OrderData, OrderData> isOurColumn = new TableColumn<>();
        isOurColumn.setMinWidth(40.0d);
        isOurColumn.setMaxWidth(55.0d);
        isOurColumn.setText("Своя");
        isOurColumn.setSortable(false);
        isOurColumn.setCellValueFactory(q2 -> {
            return new SimpleObjectProperty((OrderData) q2.getValue());
        });
        isOurColumn.setCellFactory(param2 -> {
            return getFormatter("order-is-our");
        });
        TableColumn<OrderData, OrderData> volumeColumn = new TableColumn<>();
        volumeColumn.setMinWidth(35.0d);
        volumeColumn.setMaxWidth(55.0d);
        volumeColumn.setText("Всего");
        volumeColumn.setSortable(false);
        volumeColumn.setCellValueFactory(q3 -> {
            return new SimpleObjectProperty((OrderData) q3.getValue());
        });
        volumeColumn.setCellFactory(param3 -> {
            return getFormatter("order-volume");
        });
        TableColumn<OrderData, OrderData> volumeLeftColumn = new TableColumn<>();
        volumeLeftColumn.setMinWidth(35.0d);
        volumeLeftColumn.setMaxWidth(55.0d);
        volumeLeftColumn.setText("Осталось");
        volumeLeftColumn.setSortable(false);
        volumeLeftColumn.setCellValueFactory(q4 -> {
            return new SimpleObjectProperty((OrderData) q4.getValue());
        });
        volumeLeftColumn.setCellFactory(param4 -> {
            return getFormatter("order-volume-left");
        });
        TableColumn<OrderData, OrderData> priceColumn = new TableColumn<>();
        priceColumn.setMinWidth(55.0d);
        priceColumn.setMaxWidth(55.0d);
        priceColumn.setText("Цена");
        priceColumn.setSortable(false);
        priceColumn.setCellValueFactory(q5 -> {
            return new SimpleObjectProperty((OrderData) q5.getValue());
        });
        priceColumn.setCellFactory(param5 -> {
            return getFormatter("order-price");
        });
        TableColumn<OrderData, OrderData> statusColumn = new TableColumn<>();
        statusColumn.setMinWidth(70.0d);
        statusColumn.setMaxWidth(90.0d);
        statusColumn.setText("Статус");
        statusColumn.setSortable(false);
        statusColumn.setCellValueFactory(q6 -> {
            return new SimpleObjectProperty((OrderData) q6.getValue());
        });
        statusColumn.setCellFactory(param6 -> {
            return getFormatter("order-status");
        });
        return List.of(timeColumn, isOurColumn, volumeColumn, volumeLeftColumn, priceColumn, statusColumn);
    }

    public static void setRowFactory(TableView<OrderData> table) {
        table.setRowFactory(tableView -> {
            TableRow<OrderData> row = new TableBaseRow<>();
            row.setAlignment(Pos.CENTER_RIGHT);
            row.setMaxHeight(18.0d);
            row.setMinHeight(18.0d);
            row.setPrefHeight(18.0d);
            return row;
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static TableCell<OrderData, OrderData> getFormatter(final String property) {
        return new TableCell<OrderData, OrderData>() { // from class: ru.effectivegroup.client.algoil.OrderComponent.1
            /* JADX INFO: Access modifiers changed from: protected */
            public void updateItem(OrderData item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setGraphic(null);
                getStyleClass().removeAll(new String[]{"quotation-buy", "quotation-sell"});
                if (!empty) {
                    if (item.getStatus().equals(OrderStatus.QUEUED)) {
                        if (item.getBuySell().equals(BSType.BUY)) {
                            getStyleClass().add("quotation-buy");
                        } else {
                            getStyleClass().add("quotation-sell");
                        }
                    }
                    alignmentProperty().setValue(Pos.CENTER);
                    OrderComponent.ConfigureCellText(this, item, property);
                }
            }
        };
    }

    private static void ConfigureCellText(TableCell component, OrderData item, String property) {
        boolean z = -1;
        switch (property.hashCode()) {
            case -672908408:
                if (property.equals("order-is-our")) {
                    z = true;
                    break;
                }
                break;
            case -384139439:
                if (property.equals("order-status")) {
                    z = 4;
                    break;
                }
                break;
            case -302541191:
                if (property.equals("order-volume")) {
                    z = 2;
                    break;
                }
                break;
            case -58896677:
                if (property.equals("order-volume-left")) {
                    z = 3;
                    break;
                }
                break;
            case 538974730:
                if (property.equals("order-price")) {
                    z = 5;
                    break;
                }
                break;
            case 710233580:
                if (property.equals("order-time")) {
                    z = false;
                    break;
                }
                break;
        }
        switch (z) {
            case false:
                LocalTime time = Instant.ofEpochMilli(item.getCreationTime().getTime()).atZone(ZoneId.systemDefault()).toLocalTime();
                component.setText(String.valueOf(time));
                return;
            case true:
                String isOur = "";
                if (!Objects.equals(item.getTrn(), "")) {
                    isOur = "Своя";
                }
                component.setText(isOur);
                return;
            case true:
                String signPrefix = "-";
                if (item.getBuySell().equals(BSType.BUY)) {
                    signPrefix = "+";
                }
                component.setText(signPrefix + " " + item.getQty());
                return;
            case CipherHolder.CIPHER_PARAM_ENCRYPTED_SECRET_KEY /* 3 */:
                component.setText(String.valueOf(item.getQtyLeft()));
                return;
            case CipherHolder.CIPHER_PARAM_IV /* 4 */:
                String result = null;
                switch (AnonymousClass2.$SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$OrderStatus[item.getStatus().ordinal()]) {
                    case 1:
                        result = "Активна";
                        break;
                    case 2:
                        result = "Исполнена";
                        break;
                    case CipherHolder.CIPHER_PARAM_ENCRYPTED_SECRET_KEY /* 3 */:
                        result = "Снята";
                        break;
                    case CipherHolder.CIPHER_PARAM_IV /* 4 */:
                        result = "Ожидание";
                        break;
                }
                component.setText(result);
                return;
            case true:
                component.setText(new DecimalFormat("###,###,###").format(item.getPrice() / 100));
                return;
            default:
                return;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: ru.effectivegroup.client.algoil.OrderComponent$2, reason: invalid class name */
    /* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/OrderComponent$2.class */
    public static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$OrderStatus = new int[OrderStatus.values().length];

        static {
            try {
                $SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$OrderStatus[OrderStatus.QUEUED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$OrderStatus[OrderStatus.MATCHED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$OrderStatus[OrderStatus.CANCELED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$OrderStatus[OrderStatus.WAIT_APPROVAL.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }
}
