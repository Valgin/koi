package ru.effectivegroup.client.algoil.execution;

import ru.effectivegroup.client.model.data.ste.AccountData;
import ru.effectivegroup.client.model.data.ste.ClientData;
import ru.effectivegroup.client.model.data.ste.HoldingData;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/execution/ExecutionManagerEventsConsumer.class */
public interface ExecutionManagerEventsConsumer {
    default void Handle(OrderStateChange osc) {
    }

    default void Handle(AccountData accountData) {
    }

    default void Handle(ClientData clientData) {
    }

    default void Handle(HoldingData holdingData) {
    }
}
