package ru.effectivegroup.client.algoil.bots.states;

import java.time.LocalTime;
import java.util.List;
import ru.effectivegroup.client.algoil.bots.BotLogger;
import ru.effectivegroup.client.algoil.marketdata.OrderBook;
import ru.effectivegroup.client.algoil.marketdata.OrderBookRow;
import ru.effectivegroup.client.model.data.ste.OrderData;
import ru.effectivegroup.client.model.data.ste.SecurityData;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/states/BotStateBase.class */
public abstract class BotStateBase implements BotState {
    protected final BotStateContext context;
    private final BotLogger logger;
    protected final String name;

    @Override // ru.effectivegroup.client.algoil.bots.states.BotState
    public abstract BotStateKind getStateKind();

    public BotStateBase(String name, BotStateContext context) {
        this.name = name;
        this.context = context;
        this.logger = context.getLogger();
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotState
    public String getName() {
        return this.name;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public OrderBookRow getBidToOverlap(List<OrderBookRow> bids, long priceLimit) {
        if (bids == null || bids.isEmpty()) {
            return null;
        }
        Trace(String.format("поиск бида для перебивания, всего %d бидов", Integer.valueOf(bids.size())));
        for (OrderBookRow bid : bids) {
            if (bid.getPrice() <= priceLimit) {
                return bid;
            }
        }
        return null;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotState
    public void Start() {
        Trace("Start abstract");
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotState
    public void Stop() {
        Trace("Stop abstract");
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotState
    public void Enter() {
        Trace("Enter abstract");
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotState
    public void Exit() {
        Trace("Exit abstract");
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotState
    public void Handle(OrderBook book) {
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotState
    public void Handle(SecurityData securityData) {
        Trace("HandleSecurityData abstract");
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotState
    public void Handle(OrderData orderData) {
        Trace("HandleOrderData abstract");
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotState
    public void Handle(LocalTime time) {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void Trace(String message) {
        this.logger.info("[{}] {}", this.name, message);
    }
}
