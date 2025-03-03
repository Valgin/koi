package ru.effectivegroup.client.algoil.bots.states;

import ru.effectivegroup.client.algoil.execution.CancelOrderResult;
import ru.effectivegroup.client.algoil.execution.Order;
import ru.effectivegroup.client.model.data.ste.OrderData;
import ru.effectivegroup.client.network.message.STEProtos;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/states/BotStateOverlapCancelling.class */
public class BotStateOverlapCancelling extends BotStateBase {
    private boolean cancelSent;
    private boolean stopRequested;
    private final boolean isDelayRequired;

    public BotStateOverlapCancelling(BotStateContext context, boolean isDelayRequired) {
        super("OVERLAP_CANCELLING", context);
        this.cancelSent = false;
        this.stopRequested = false;
        this.isDelayRequired = isDelayRequired;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public BotStateKind getStateKind() {
        return BotStateKind.Running;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Stop() {
        Trace("Stop");
        this.stopRequested = true;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Enter() {
        Trace("Enter");
        TryCancelOrder(null);
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Handle(OrderData orderData) {
        Trace("HandleOrderData");
        TryCancelOrder(orderData);
    }

    private void TryCancelOrder(OrderData orderData) {
        Order activeOrder = this.context.getActiveOrder();
        if (activeOrder == null) {
            Trace("Нет активной заявки");
            setNextState();
            return;
        }
        if (activeOrder.getStatus() == STEProtos.OrderStatusType.CANCELED) {
            Trace("Заявка снята");
            this.context.clearActiveOrder();
            setNextState();
            return;
        }
        if (activeOrder.getStatus() == STEProtos.OrderStatusType.MATCHED && activeOrder.getQtyLeft() == 0) {
            Trace("Заявка исполнена");
            this.context.clearActiveOrder();
            setNextState();
            return;
        }
        if (orderData != null) {
            Trace(String.format("orderData!=null, orderData.getStatus(%s)", orderData.getStatus()));
            if (orderData.getCode() != null && orderData.getCode().equals(activeOrder.getCode())) {
                Trace(String.format("orderData.getCode(%s) == activeOrder.getCode(%s)", orderData.getCode(), activeOrder.getCode()));
                this.context.clearActiveOrder();
                setNextState();
                return;
            }
            Trace(String.format("orderData.getCode(%s) != activeOrder.getCode(%s)", orderData.getCode(), activeOrder.getCode()));
        }
        if (this.isDelayRequired) {
            if (this.context.getDelayCancel() != 0) {
                Trace("Задержка != 0, переходим в состояние задержки бота");
                this.context.setState(new BotStateDelayed(this.context, this.context.getDelayCancel(), new BotStateOverlapCancelling(this.context, false)));
                return;
            }
            Trace("Задержка = 0, снимаем заявку");
        }
        if (this.cancelSent) {
            Trace(String.format("Уже был отправлен запрос на снятие заявки %s, не отправляем повторно", orderData.getCode()));
            return;
        }
        this.cancelSent = true;
        CancelOrderResult cancelOrderResult = this.context.CancelActiveOrder();
        if (cancelOrderResult.isSuccessful()) {
            Trace("Заявка снята, дожидаемся события изменения статуса");
            return;
        }
        Trace("Ошибка снятия заявки, дожидаемся события изменения статуса");
        if (this.context.getActiveOrder() == null) {
            this.context.setState(new BotStateOverlapWaiting(this.context, true));
        } else {
            this.context.setState(new BotStateError(this.context, String.format("Ошибка снятия заявки: %s", cancelOrderResult.getErrorCode())));
        }
    }

    private void setNextState() {
        if (this.stopRequested) {
            this.context.setState(new BotStateStopping(this.context, "Остановка пользователем"));
        } else {
            this.context.setState(new BotStateOverlapWaiting(this.context, true));
        }
    }
}
