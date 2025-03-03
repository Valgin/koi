package ru.effectivegroup.client.algoil.bots;

import ru.effectivegroup.client.algoil.bots.states.BotStateKind;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/BotStateChangeListener.class */
public interface BotStateChangeListener {
    void Handle(BotStateKind botStateKind);
}
