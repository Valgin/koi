package ru.effectivegroup.client.algoil.bots.states;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import ru.effectivegroup.client.algoil.ExchangeTimeProvider;
import ru.effectivegroup.client.algoil.marketdata.OrderBook;
import ru.effectivegroup.client.model.data.ste.OrderData;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/states/BotStateDelayed.class */
public class BotStateDelayed extends BotStateBase {
    protected long delay;
    private final BotState nextState;
    private LocalTime initialTime;

    public BotStateDelayed(BotStateContext context, long delay, BotState nextState) {
        super("DELAYED", context);
        this.delay = delay;
        this.nextState = nextState;
    }

    private void toNextState() {
        this.context.setState(this.nextState);
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Stop() {
        Trace("Stop");
        this.context.setState(new BotStateStopping(this.context, "По запросу пользователя"));
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Enter() {
        Trace("Enter");
        this.initialTime = ExchangeTimeProvider.getTime();
        Trace(String.format("Задержка работы бота на %s мс", Long.valueOf(this.delay)));
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public BotStateKind getStateKind() {
        return BotStateKind.Running;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Handle(OrderBook book) {
        Trace("HandleOrderBook");
        if (book == null) {
            return;
        }
        if (this.context.isNextOrderFirst()) {
            Trace("Это первая заявка, время задержки не будет откладываться");
        } else {
            this.initialTime = ExchangeTimeProvider.getTime();
            Trace("Получен OrderBook во время задержки на постановку. initialTime = " + this.initialTime);
        }
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Handle(LocalTime time) {
        if (time != null && time.isAfter(this.initialTime.plus(this.delay, (TemporalUnit) ChronoUnit.MILLIS))) {
            Trace(String.format("Время задержки истекло, переход в состояние %s", this.nextState.getName()));
            toNextState();
        }
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Handle(OrderData orderData) {
        if (this.context.getPosition() >= this.context.getPositionLimit()) {
            Trace("position >= positionLimit. Позиция набрана");
            this.context.setState(new BotStateIdle(this.context));
        }
    }
}
