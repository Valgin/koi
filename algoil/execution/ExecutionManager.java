package ru.effectivegroup.client.algoil.execution;

import java.util.List;
import ru.effectivegroup.client.model.data.ste.ClientData;
import ru.effectivegroup.client.model.data.ste.dictionary.BSType;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/execution/ExecutionManager.class */
public interface ExecutionManager {
    PlaceOrderResult PlaceOrder(String str, BSType bSType, long j, long j2, String str2, String str3, OrderEmitter orderEmitter);

    CancelOrderResult CancelOrder(Order order);

    void Subscribe(ExecutionManagerEventsConsumer executionManagerEventsConsumer);

    List<String> getCommAccounts();

    List<String> getMoneyAccounts();

    List<ClientData> getClients();
}
