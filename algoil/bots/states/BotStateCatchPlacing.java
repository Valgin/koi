package ru.effectivegroup.client.algoil.bots.states;

import ru.effectivegroup.client.algoil.execution.PlaceOrderResult;
import ru.effectivegroup.client.model.data.ste.dictionary.BSType;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/states/BotStateCatchPlacing.class */
public class BotStateCatchPlacing extends BotStateBase {
    private final long price;
    private final int quantity;

    public BotStateCatchPlacing(BotStateContext context, long price, int quantity) {
        super("CATCH_PLACING", context);
        this.price = price;
        this.quantity = quantity;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public BotStateKind getStateKind() {
        return BotStateKind.Running;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Enter() {
        Trace("Enter");
        PlaceOrderResult placeOrderResult = this.context.PlaceOrder(BSType.BUY, this.quantity, this.price);
        if (placeOrderResult.isSuccessful()) {
            this.context.setState(new BotStateCatchCancelling(this.context));
        } else {
            this.context.setState(new BotStateStopping(this.context, "Ошибка выставления заявки"));
        }
    }
}
