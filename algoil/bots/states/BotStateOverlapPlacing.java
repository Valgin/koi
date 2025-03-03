package ru.effectivegroup.client.algoil.bots.states;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import ru.effectivegroup.client.algoil.execution.PlaceOrderResult;
import ru.effectivegroup.client.crypto.cipher.CipherHolder;
import ru.effectivegroup.client.model.data.ste.OrderData;
import ru.effectivegroup.client.model.data.ste.dictionary.BSType;
import ru.effectivegroup.client.model.data.ste.dictionary.OrderStatus;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/states/BotStateOverlapPlacing.class */
public class BotStateOverlapPlacing extends BotStateBase {
    private final long price;
    private final long targetPrice;
    private final int quantity;

    public BotStateOverlapPlacing(BotStateContext context, long price, long targetPrice, int quantity) {
        super("OVERLAP_PLACING", context);
        this.price = price;
        this.targetPrice = targetPrice;
        this.quantity = quantity;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public BotStateKind getStateKind() {
        return BotStateKind.Running;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Stop() {
        Trace("Stop");
        this.context.setState(new BotStateStopping(this.context, null));
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Enter() {
        Trace("Enter");
        PlaceOrderResult placeOrderResult = this.context.PlaceOrder(BSType.BUY, this.quantity, this.price);
        if (this.context.getErrorProcessingTime() == null) {
            LocalTime errorTime = this.context.getTime().plus(2L, (TemporalUnit) ChronoUnit.SECONDS);
            this.context.setErrorProcessingTime(errorTime);
        }
        if (placeOrderResult.isSuccessful()) {
            this.context.setState(new BotStateOverlapPlaced(this.context, this.price, this.targetPrice));
        } else if (placeOrderResult.getErrorCode() == 94 && this.context.timeIsBefore94ErrorProcessingTime()) {
            Trace("Получена ошибка выставления заявки с кодом 94 в районе 2 секунд после запуска, перевыставляемся");
            this.context.setState(new BotStateOverlapWaiting(this.context, true));
        } else {
            this.context.setState(new BotStateStopping(this.context, String.format("Ошибка выставления заявки code=%d", Integer.valueOf(placeOrderResult.getErrorCode()))));
        }
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Handle(OrderData orderData) {
        Trace("HandleOrderData");
        switch (AnonymousClass1.$SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$OrderStatus[orderData.getStatus().ordinal()]) {
            case 1:
                this.context.setState(new BotStateOverlapPlaced(this.context, this.price, this.targetPrice));
                return;
            case 2:
                this.context.setState(new BotStateStopping(this.context, String.format("В состоянии %s получен статус заявки CANCELED", getName())));
                return;
            case CipherHolder.CIPHER_PARAM_ENCRYPTED_SECRET_KEY /* 3 */:
                if (orderData.getQtyLeft() > 0) {
                    this.context.setState(new BotStateOverlapPlaced(this.context, this.price, this.targetPrice));
                    return;
                } else {
                    this.context.setState(new BotStateOverlapWaiting(this.context, true));
                    return;
                }
            case CipherHolder.CIPHER_PARAM_IV /* 4 */:
                this.context.setState(new BotStateError(this.context, "Неизвестное состояние заявки WAIT_APPROVAL"));
                return;
            default:
                return;
        }
    }

    /* renamed from: ru.effectivegroup.client.algoil.bots.states.BotStateOverlapPlacing$1, reason: invalid class name */
    /* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/states/BotStateOverlapPlacing$1.class */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$OrderStatus = new int[OrderStatus.values().length];

        static {
            try {
                $SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$OrderStatus[OrderStatus.QUEUED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$OrderStatus[OrderStatus.CANCELED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$OrderStatus[OrderStatus.MATCHED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$OrderStatus[OrderStatus.WAIT_APPROVAL.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }
}
