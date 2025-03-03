package ru.effectivegroup.client.algoil.execution;

import ru.effectivegroup.client.network.message.STEProtos;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/execution/TransactionResult.class */
public class TransactionResult {
    private String _id;
    private boolean _successful;
    private String _errorCode;
    private String _errorDescription;
    private STEProtos.OrderStatusType _orderStatus;

    public static TransactionResult CreateSuccessful(String orderCode, STEProtos.OrderStatusType status) {
        TransactionResult result = new TransactionResult();
        result._successful = true;
        result._orderStatus = status;
        return result;
    }

    public static TransactionResult CreateFailed(String errorCode, String errorDescr) {
        TransactionResult result = new TransactionResult();
        result._successful = false;
        result._errorCode = errorCode;
        result._errorDescription = errorDescr;
        return result;
    }

    public String getId() {
        return this._id;
    }

    public boolean isSuccessful() {
        return this._successful;
    }
}
