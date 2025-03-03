package ru.effectivegroup.client.algoil.marketdata;

import java.util.List;
import ru.effectivegroup.client.algoil.Instrument;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/marketdata/OrderBook.class */
public class OrderBook {
    private final Instrument instrument;
    private final List<OrderBookRow> bids;
    private final List<OrderBookRow> asks;

    public OrderBook(Instrument instrument, List<OrderBookRow> bids, List<OrderBookRow> asks) {
        this.instrument = instrument;
        this.bids = bids;
        this.asks = asks;
    }

    public OrderBookRow getBestBid() {
        if (this.bids == null || this.bids.size() == 0) {
            return null;
        }
        return this.bids.get(0);
    }

    public OrderBookRow getPreBestBid() {
        if (this.bids == null || this.bids.size() <= 1) {
            return null;
        }
        return this.bids.get(1);
    }

    public OrderBookRow getBestAsk() {
        if (this.asks == null || this.asks.size() == 0) {
            return null;
        }
        return this.asks.get(0);
    }

    public boolean hasOwnBids() {
        if (this.bids == null || this.bids.size() == 0) {
            return false;
        }
        for (OrderBookRow bid : this.bids) {
            if (bid.hasOwnQty()) {
                return true;
            }
        }
        return false;
    }

    public List<OrderBookRow> getBids() {
        return this.bids;
    }

    public List<OrderBookRow> getAsks() {
        return this.asks;
    }

    public Instrument getInstrument() {
        return this.instrument;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.bids != null && this.bids.size() > 0) {
            sb.append("Bids: ");
            for (OrderBookRow bid : this.bids) {
                sb.append(bid.toString());
            }
        }
        if (this.asks != null && this.asks.size() > 0) {
            sb.append("Asks: ");
            for (OrderBookRow ask : this.asks) {
                sb.append(ask.toString());
            }
        }
        return sb.toString();
    }
}
