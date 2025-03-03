package ru.effectivegroup.client.algoil.execution;

import ru.effectivegroup.client.network.message.STEProtos;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/execution/Order.class */
public class Order {
    private final Long id;
    private String code;
    private String security;
    private long qty;
    private long qtyLeft;
    private long price;
    private STEProtos.OrderStatusType status;

    public Order(String code, String security, long qty, long qtyLeft, long price, STEProtos.OrderStatusType status) {
        this.code = code;
        this.id = Long.valueOf(Long.parseLong(code));
        this.security = security;
        this.qty = qty;
        this.qtyLeft = qtyLeft;
        this.price = price;
        this.status = status;
    }

    public String getCode() {
        return this.code;
    }

    public String getSecurity() {
        return this.security;
    }

    public long getQty() {
        return this.qty;
    }

    public long getQtyLeft() {
        return this.qtyLeft;
    }

    public void setQtyLeft(long qtyLeft) {
        this.qtyLeft = qtyLeft;
    }

    public long getPrice() {
        return this.price;
    }

    public STEProtos.OrderStatusType getStatus() {
        return this.status;
    }

    public void setStatus(STEProtos.OrderStatusType status) {
        this.status = status;
    }

    public Long getId() {
        return this.id;
    }
}
