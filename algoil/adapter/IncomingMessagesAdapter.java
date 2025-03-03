package ru.effectivegroup.client.algoil.adapter;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/adapter/IncomingMessagesAdapter.class */
public interface IncomingMessagesAdapter {
    void Stop();

    void Subscribe(STEMessageConsumer sTEMessageConsumer);

    void Unsubscribe(STEMessageConsumer sTEMessageConsumer);
}
