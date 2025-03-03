package ru.effectivegroup.client.algoil.marketdata;

import java.util.HashMap;
import ru.effectivegroup.client.context.Context;
import ru.effectivegroup.client.model.data.ste.OrderData;
import ru.effectivegroup.client.model.data.ste.dictionary.OrderStatus;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/marketdata/OrderBookRow.class */
public class OrderBookRow {
    private long price;
    private int qty;
    private boolean hasOwn;
    private boolean isMyFirst;
    private Object syncRoot = new Object();
    private final HashMap<String, OrderData> items = new HashMap<>();

    public OrderBookRow(long price) {
        this.price = price;
    }

    public boolean Handle(OrderData orderData) {
        boolean z;
        String code = orderData.getCode();
        int activeQty = getActiveQty(orderData);
        synchronized (this.syncRoot) {
            if (this.items.size() == 0 && hasOwnQty(orderData)) {
                this.isMyFirst = true;
            }
            if (this.items.containsKey(code)) {
                OrderData item = this.items.get(code);
                if (activeQty > 0) {
                    this.qty = (this.qty - getActiveQty(item)) + activeQty;
                    this.hasOwn |= hasOwnQty(item);
                    this.items.replace(code, orderData);
                } else {
                    this.qty -= getActiveQty(item);
                    this.items.remove(code);
                    if (hasOwnQty(item)) {
                        this.hasOwn = false;
                    }
                }
            } else if (activeQty > 0) {
                this.items.put(orderData.getCode(), orderData);
                this.qty += activeQty;
                this.hasOwn = hasOwnQty(orderData);
            }
            z = this.items.size() > 0;
        }
        return z;
    }

    public String toString() {
        if (this.hasOwn) {
            return String.format("%d %d%s", Long.valueOf(this.price), Integer.valueOf(this.qty), "(hasOwn)");
        }
        return String.format("%d %d", Long.valueOf(this.price), Integer.valueOf(this.qty));
    }

    public long getPrice() {
        return this.price;
    }

    public int getQty() {
        return this.qty;
    }

    public boolean hasOwnQty() {
        return this.hasOwn;
    }

    public boolean myOrderIsFirst() {
        return this.isMyFirst;
    }

    private boolean hasOwnQty(OrderData orderData) {
        if (orderData.getCashAccountCode() == null || orderData.getCashAccountCode().isEmpty() || !orderData.getUserCode().equals(Context.serviceContext.steService.getUserCode())) {
            return false;
        }
        return true;
    }

    private int getActiveQty(OrderData orderData) {
        OrderStatus status = orderData.getStatus();
        if (status == OrderStatus.CANCELED) {
            return 0;
        }
        return orderData.getQtyLeft();
    }
}
