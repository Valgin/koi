package ru.effectivegroup.client.algoil.marketdata;

import ru.effectivegroup.client.algoil.Instrument;
import ru.effectivegroup.client.model.data.ste.OrderData;
import ru.effectivegroup.client.model.data.ste.SecurityData;
import ru.effectivegroup.client.model.data.ste.TradeData;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/marketdata/DataFeedMessageConsumer.class */
public interface DataFeedMessageConsumer {
    default void Handle(Instrument instrument) {
    }

    default void Handle(SecurityData message) {
    }

    default void Handle(TradeData message) {
    }

    default void Handle(OrderData message) {
    }

    default void Handle(OrderBook message) {
    }
}
