package ru.effectivegroup.client.algoil.execution;

import ru.effectivegroup.client.model.data.ste.OrderData;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/execution/OrderEmitter.class */
public interface OrderEmitter {
    void HandleOrderStateChange(OrderData orderData);
}
