package ru.effectivegroup.client.algoil.marketdata;

import java.util.List;
import ru.effectivegroup.client.algoil.Instrument;
import ru.effectivegroup.client.model.data.ste.OrderData;
import ru.effectivegroup.client.model.data.ste.dictionary.BSType;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/marketdata/OrderBookBuilder.class */
public class OrderBookBuilder {
    private Instrument instrument;
    private OrderBookHalfBuilder bidsBuilder = new OrderBookHalfBuilder();
    private OrderBookHalfBuilder asksBuilder = new OrderBookHalfBuilder();

    public OrderBookBuilder(Instrument instrument) {
        this.instrument = instrument;
    }

    public OrderBook Handle(OrderData orderData) {
        if (!orderData.getSecurityCode().equals(this.instrument.getCode())) {
            return null;
        }
        if (orderData.getBuySell() == BSType.BUY) {
            this.bidsBuilder.Handle(orderData);
        } else {
            this.asksBuilder.Handle(orderData);
        }
        return new OrderBook(this.instrument, this.bidsBuilder.getRows(), this.asksBuilder.getRows());
    }

    public List<OrderBookRow> getBids() {
        return this.bidsBuilder.getRows();
    }

    public List<OrderBookRow> getAsks() {
        return this.asksBuilder.getRows();
    }

    public OrderBookRow getBestBid() {
        return this.bidsBuilder.getBest();
    }

    public OrderBookRow getBestAsk() {
        return this.asksBuilder.getBest();
    }
}
