package ru.effectivegroup.client.algoil.marketdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import ru.effectivegroup.client.model.data.ste.OrderData;
import ru.effectivegroup.client.model.data.ste.dictionary.BSType;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/marketdata/OrderBookHalfBuilder.class */
public class OrderBookHalfBuilder {
    private Boolean isBidsBuilder;
    private Object syncRoot = new Object();
    private final HashMap<Long, OrderBookRow> rows = new HashMap<>();
    private final List<Long> prices = new ArrayList();

    public void Handle(OrderData orderData) {
        OrderBookRow row;
        if (!checkOperation(orderData)) {
            return;
        }
        long price = orderData.getPrice() / 100;
        synchronized (this.syncRoot) {
            row = this.rows.getOrDefault(Long.valueOf(price), null);
            if (row == null) {
                row = new OrderBookRow(price);
                this.rows.put(Long.valueOf(price), row);
                this.prices.add(Long.valueOf(price));
            }
        }
        if (!row.Handle(orderData)) {
            synchronized (this.syncRoot) {
                this.rows.remove(Long.valueOf(price));
                this.prices.remove(Long.valueOf(price));
            }
        }
    }

    public OrderBookRow getBest() {
        List<OrderBookRow> rowsCopy = getRows();
        if (rowsCopy.size() > 0) {
            return rowsCopy.get(0);
        }
        return null;
    }

    public List<OrderBookRow> getRows() {
        synchronized (this.syncRoot) {
            List<OrderBookRow> result = new ArrayList<>();
            if (this.prices.size() == 0) {
                return result;
            }
            if (this.isBidsBuilder.booleanValue()) {
                this.prices.sort(Comparator.comparing((v0) -> {
                    return v0.longValue();
                }, Collections.reverseOrder()));
            } else {
                this.prices.sort(Comparator.comparing((v0) -> {
                    return v0.longValue();
                }));
            }
            Iterator<Long> it = this.prices.iterator();
            while (it.hasNext()) {
                long price = it.next().longValue();
                result.add(this.rows.get(Long.valueOf(price)));
            }
            return result;
        }
    }

    private boolean checkOperation(OrderData orderData) {
        if (this.isBidsBuilder == null) {
            this.isBidsBuilder = Boolean.valueOf(orderData.getBuySell() == BSType.BUY);
            return true;
        }
        if (this.isBidsBuilder.booleanValue() && orderData.getBuySell() == BSType.SELL) {
            return false;
        }
        return true;
    }
}
