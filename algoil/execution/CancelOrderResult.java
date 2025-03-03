package ru.effectivegroup.client.algoil.execution;

import ru.effectivegroup.client.network.message.STEProtos;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/execution/CancelOrderResult.class */
public class CancelOrderResult {
    private boolean successful;
    private String errorCode;
    private String errorDescription;

    CancelOrderResult(boolean successful, String errorCode, String errorDescription) {
        this.successful = successful;
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }

    public static CancelOrderResult CreateSuccessful() {
        return new CancelOrderResult(true, null, null);
    }

    public static CancelOrderResult CreateSuccessful(String orderCode, STEProtos.OrderStatusType status, int qty, int qtyLeft) {
        return new CancelOrderResult(true, null, null);
    }

    public static CancelOrderResult CreateFailed(String errorCode, String errorDescription) {
        CancelOrderResult result = new CancelOrderResult(false, errorCode, errorDescription);
        return result;
    }

    public boolean isSuccessful() {
        return this.successful;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public String getErrorDescription() {
        return this.errorDescription;
    }
}
