package ru.effectivegroup.client.algoil.bots.states;

import java.time.format.DateTimeFormatter;
import java.util.Objects;
import ru.effectivegroup.client.algoil.marketdata.OrderBook;
import ru.effectivegroup.client.algoil.marketdata.OrderBookRow;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/states/BotStateCatchWaiting.class */
public class BotStateCatchWaiting extends BotStateBase {
    private boolean isDelayRequired;

    public BotStateCatchWaiting(BotStateContext context, boolean isDelayRequired) {
        super("CATCH_WAITING", context);
        this.isDelayRequired = isDelayRequired;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public BotStateKind getStateKind() {
        return BotStateKind.Running;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Enter() {
        Trace("Enter");
        if (this.context.getPosition() >= this.context.getPositionLimit()) {
            Trace(String.format("Достигнут лимит позиции %d, останавливаем бота", Integer.valueOf(this.context.getPosition())));
            this.context.setState(new BotStateIdle(this.context));
        } else {
            TryGoPlacingState();
        }
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Handle(OrderBook book) {
        Trace("HandleOrderBook");
        TryGoPlacingState();
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Stop() {
        Trace("Stop");
        this.context.setState(new BotStateStopping(this.context, "По запросу пользователя"));
    }

    private void TryGoPlacingState() {
        if (this.isDelayRequired) {
            if (this.context.getDelayPlace() != 0) {
                Trace("Задержка != 0, переходим в состояние задержки бота");
                this.context.setState(new BotStateDelayed(this.context, this.context.getDelayPlace(), new BotStateCatchWaiting(this.context, false)));
                return;
            }
            Trace("Задержка = 0, выставляем заявку");
        }
        OrderBookRow bestAsk = this.context.getBestAsk();
        if (bestAsk == null || bestAsk.getPrice() > this.context.getPriceLimit()) {
            return;
        }
        String auctionEndTime = this.context.getDiscreteAuctionEndTime();
        int discreteTimeMillis = Integer.parseInt(auctionEndTime) * 100000;
        int currentTimeMillis = Integer.parseInt(this.context.getTime().format(DateTimeFormatter.ofPattern("HHmmssSSS")));
        if (Objects.equals(auctionEndTime, "0") || (!Objects.equals(auctionEndTime, "0") && currentTimeMillis > discreteTimeMillis)) {
            this.context.setState(new BotStateCatchPlacing(this.context, bestAsk.getPrice(), Math.min(this.context.getQty(), bestAsk.getQty())));
        }
    }
}
