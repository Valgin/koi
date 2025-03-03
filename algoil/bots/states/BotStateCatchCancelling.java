package ru.effectivegroup.client.algoil.bots.states;

import ru.effectivegroup.client.algoil.execution.CancelOrderResult;
import ru.effectivegroup.client.algoil.execution.Order;
import ru.effectivegroup.client.model.data.ste.OrderData;
import ru.effectivegroup.client.network.message.STEProtos;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/states/BotStateCatchCancelling.class */
public class BotStateCatchCancelling extends BotStateBase {
    public BotStateCatchCancelling(BotStateContext context) {
        super("CATCH_CANCELLING", context);
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public BotStateKind getStateKind() {
        return BotStateKind.Running;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Enter() {
        Trace("Enter");
        TryCancelOrder();
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Handle(OrderData orderData) {
        Trace("HandleOrderData");
        TryCancelOrder();
    }

    private void TryCancelOrder() {
        Order activeOrder = this.context.getActiveOrder();
        if (activeOrder == null) {
            Trace("Нет активной заявки");
            this.context.setState(new BotStateCatchWaiting(this.context, true));
            return;
        }
        if (activeOrder.getStatus() == STEProtos.OrderStatusType.CANCELED) {
            this.context.clearActiveOrder();
            Trace("Заявка снята");
            this.context.setState(new BotStateCatchWaiting(this.context, true));
        } else if (activeOrder.getStatus() == STEProtos.OrderStatusType.MATCHED && activeOrder.getQtyLeft() == 0) {
            this.context.clearActiveOrder();
            Trace("Заявка исполнена");
            this.context.setState(new BotStateCatchWaiting(this.context, true));
        } else {
            CancelOrderResult cancelOrderResult = this.context.CancelActiveOrder();
            if (cancelOrderResult.isSuccessful()) {
                Trace("Заявка снята, дожидаемся события изменения статуса");
            } else {
                Trace("Ошибка снятия заявки, дожидаемся события изменения статуса");
            }
        }
    }
}
