package ru.effectivegroup.client.algoil.bots.states;

import ru.effectivegroup.client.algoil.execution.CancelOrderResult;
import ru.effectivegroup.client.algoil.execution.Order;
import ru.effectivegroup.client.model.data.ste.OrderData;
import ru.effectivegroup.client.network.message.STEProtos;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/states/BotStateStopping.class */
public class BotStateStopping extends BotStateBase {
    private final String reason;

    public BotStateStopping(BotStateContext context, String reason) {
        super("STOPPING", context);
        if (reason == null) {
            this.reason = "Остановка пользователем";
        } else {
            this.reason = reason;
        }
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public BotStateKind getStateKind() {
        return BotStateKind.Running;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Enter() {
        Trace("Enter");
        Trace(String.format("причина остановки: %s", this.reason));
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
            this.context.setState(new BotStateIdle(this.context));
            return;
        }
        if (activeOrder.getStatus() == STEProtos.OrderStatusType.CANCELED) {
            Trace("Заявка снята");
            this.context.clearActiveOrder();
            this.context.setState(new BotStateIdle(this.context));
        } else if (activeOrder.getStatus() == STEProtos.OrderStatusType.MATCHED && activeOrder.getQtyLeft() == 0) {
            Trace("Заявка исполнена");
            this.context.clearActiveOrder();
            this.context.setState(new BotStateIdle(this.context));
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
