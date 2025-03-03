package ru.effectivegroup.client.algoil.bots.states;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/states/BotStateError.class */
public class BotStateError extends BotStateBase {
    private final String reason;

    public BotStateError(BotStateContext context, String reason) {
        super("ERROR", context);
        this.reason = reason;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public BotStateKind getStateKind() {
        return BotStateKind.Running;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Enter() {
        Trace("Enter");
        Trace(String.format("Остановка бота по причине: %s", this.reason));
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Stop() {
        Trace("Stop");
        this.context.setState(new BotStateIdle(this.context));
    }
}
