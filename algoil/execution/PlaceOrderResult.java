package ru.effectivegroup.client.algoil.execution;

import ru.effectivegroup.client.model.data.ste.OrderData;
import ru.effectivegroup.client.network.message.STEProtos;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/execution/PlaceOrderResult.class */
public class PlaceOrderResult {
    private String id;
    private boolean successful;
    private int errorCode;
    private String errorDescription;
    private STEProtos.OrderStatusType orderStatus;
    private Order order;

    public static PlaceOrderResult CreateSuccessful(String orderCode, STEProtos.OrderStatusType status, int qty, int qtyLeft, OrderData orderData) {
        PlaceOrderResult result = new PlaceOrderResult();
        result.successful = true;
        result.id = orderData.getTrn();
        result.orderStatus = status;
        result.order = new Order(orderCode, orderData.securityCode, qty, qtyLeft, orderData.price, status);
        return result;
    }

    public static PlaceOrderResult CreateFailed(int errorCode, String errorDescr) {
        PlaceOrderResult result = new PlaceOrderResult();
        result.successful = false;
        result.errorCode = errorCode;
        result.errorDescription = errorDescr;
        return result;
    }

    public String getId() {
        return this.id;
    }

    public boolean isSuccessful() {
        return this.successful;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public String getErrorDescription() {
        return this.errorDescription;
    }

    public STEProtos.OrderStatusType getOrderStatus() {
        return this.orderStatus;
    }

    public Order getOrder() {
        return this.order;
    }
}
