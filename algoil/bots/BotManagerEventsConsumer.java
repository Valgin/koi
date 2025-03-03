package ru.effectivegroup.client.algoil.bots;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/BotManagerEventsConsumer.class */
public interface BotManagerEventsConsumer {
    default void HandleNewBot(Bot bot) {
    }

    default void HandleBotDeleted(Bot bot) {
    }

    default void HandleBotSettingsChanged(Bot bot) {
    }
}
