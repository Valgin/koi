package ru.effectivegroup.client.algoil.bots.states;

import java.time.LocalTime;
import ru.effectivegroup.client.algoil.bots.WaitApprovalTimerState;
import ru.effectivegroup.client.algoil.marketdata.OrderBook;
import ru.effectivegroup.client.model.data.ste.OrderData;
import ru.effectivegroup.client.model.data.ste.SecurityData;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/states/BotState.class */
public interface BotState {
    String getName();

    BotStateKind getStateKind();

    void Enter();

    void Exit();

    void Start();

    void Stop();

    void Handle(OrderBook orderBook);

    void Handle(SecurityData securityData);

    void Handle(OrderData orderData);

    void Handle(LocalTime localTime);

    default void Handle(WaitApprovalTimerState state) {
    }
}
