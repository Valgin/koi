package ru.effectivegroup.client.algoil.bots.states;

import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import ru.effectivegroup.client.algoil.bots.BotMode;
import ru.effectivegroup.client.algoil.bots.WaitApprovalTimerState;
import ru.effectivegroup.client.algoil.execution.Order;
import ru.effectivegroup.client.algoil.marketdata.OrderBook;
import ru.effectivegroup.client.algoil.marketdata.OrderBookRow;
import ru.effectivegroup.client.crypto.cipher.CipherHolder;
import ru.effectivegroup.client.model.data.ste.OrderData;
import ru.effectivegroup.client.model.data.ste.SecurityData;
import ru.effectivegroup.client.model.data.ste.dictionary.OrderStatus;
import ru.effectivegroup.client.network.message.STEProtos;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/states/BotStateOverlapPlaced.class */
public class BotStateOverlapPlaced extends BotStateBase {
    private final long price;
    private final long targetPrice;
    private Timer timer;
    private boolean isTimerRunning;
    private Order previousOrder;
    private Order activeOrder;
    private boolean isStopRequired;
    private boolean isActiveOrderReadyToOverlap;
    private boolean waitApprovalTimerEnds;
    private boolean isPrevOrderCancelled;
    private boolean isPrevOrderShouldBeCancelled;
    private boolean isLimitExceeded;

    public BotStateOverlapPlaced(BotStateContext context, long price, long targetPrice) {
        super("OVERLAP_PLACED", context);
        this.isTimerRunning = false;
        this.isStopRequired = false;
        this.isActiveOrderReadyToOverlap = false;
        this.waitApprovalTimerEnds = false;
        this.isPrevOrderCancelled = false;
        this.isPrevOrderShouldBeCancelled = false;
        this.isLimitExceeded = false;
        this.price = price;
        this.targetPrice = targetPrice;
        context.setNextOrderFirst(false);
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public BotStateKind getStateKind() {
        return BotStateKind.Running;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Stop() {
        Trace("Stop");
        this.isStopRequired = true;
        setNextState();
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotState
    public void Handle(WaitApprovalTimerState state) {
        Trace("Получено событие истечение таймера ожидания состояния отличного от WAIT_APPROVAL");
        setNextState();
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Enter() {
        Trace("Enter");
        if (this.context.hasPreviousOrder()) {
            Trace("Есть бид для снятия");
            this.isPrevOrderShouldBeCancelled = true;
            this.previousOrder = this.context.getPreviousOrder();
            this.context.CancelPreviousOrder();
            Trace(String.format("Заявка на снятие отправлена id = %s", this.previousOrder.getId()));
        }
        this.activeOrder = this.context.getActiveOrder();
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Handle(OrderBook book) {
        Trace("HandleOrderBook");
        this.activeOrder = this.context.getActiveOrder();
        if (this.activeOrder == null) {
            return;
        }
        OrderBookRow bestAsk = this.context.getBestAsk();
        String auctionEndTime = this.context.getDiscreteAuctionEndTime();
        int discreteTimeMillis = Integer.parseInt(auctionEndTime) * 100000;
        int currentTimeMillis = Integer.parseInt(this.context.getTime().format(DateTimeFormatter.ofPattern("HHmmssSSS")));
        if ((Objects.equals(auctionEndTime, "0") || (!Objects.equals(auctionEndTime, "0") && currentTimeMillis > discreteTimeMillis)) && bestAsk != null && bestAsk.getPrice() <= this.context.getPriceLimit() && bestAsk.getPrice() <= this.context.getUpperPriceLimit().longValue()) {
            Trace(String.format("Захватывает предложение bestAsk(%d) <= priceLimit", Long.valueOf(bestAsk.getPrice()), Long.valueOf(this.context.getPriceLimit())));
            this.isActiveOrderReadyToOverlap = true;
            setNextState();
            return;
        }
        OrderBookRow bidToOverlap = getBidToOverlap(this.context.getBids(), this.context.getPriceLimit());
        if (bidToOverlap != null) {
            Trace(String.format("bidToOverlap = %d", Long.valueOf(bidToOverlap.getPrice())));
        }
        if (bidToOverlap != null && bidToOverlap.getPrice() > this.price && PriceLowerThanLimits(this.price)) {
            Trace(String.format("bestBid != null && bestBid(%d) > price(%d) && price < priceLimit(%d) && price < getUpperPriceLimit(%d)", Long.valueOf(bidToOverlap.getPrice()), Long.valueOf(this.price), Long.valueOf(this.context.getPriceLimit()), this.context.getUpperPriceLimit()));
            this.isActiveOrderReadyToOverlap = true;
            setNextState();
            return;
        }
        if (this.context.getBotMode() == BotMode.TurboOverlap) {
            return;
        }
        if (bidToOverlap != null && bidToOverlap.getPrice() == this.price && bidToOverlap.getQty() > this.activeOrder.getQtyLeft()) {
            Trace(String.format("bidToOverlap != null && bidToOverlap.getPrice(%d) == price(%d) && bidToOverlap.getQty(%d) > activeOrder.getQtyLeft(%d)", Long.valueOf(bidToOverlap.getPrice()), Long.valueOf(this.price), Integer.valueOf(bidToOverlap.getQty()), Long.valueOf(this.activeOrder.getQtyLeft())));
            if (bidToOverlap.myOrderIsFirst()) {
                Trace(String.format("Мы стоим первыми на лучшем уровне %d, не перебиваем", Long.valueOf(bidToOverlap.getPrice())));
                return;
            } else if (PriceLowerThanLimits(this.price)) {
                Trace(String.format("Мы стоим не первыми на лучшем уровне %d, перебиваем", Long.valueOf(bidToOverlap.getPrice())));
                this.isActiveOrderReadyToOverlap = true;
                setNextState();
                return;
            }
        }
        if (!this.context.getFallDown()) {
            Trace("Падение вниз отключено, логика выставления ниже отработана не будет");
            return;
        }
        if (this.context.getPreviousOrder() != null) {
            Trace("Предыдущая заявка активна, логика выставления ниже отработана не будет ");
            return;
        }
        if (bidToOverlap != null && bidToOverlap.getPrice() == this.price && bidToOverlap.getQty() == this.activeOrder.getQtyLeft()) {
            Trace(String.format("bidToOverlap (%d) != null && bidToOverlap.qty(%d) == myQtyLeft(%d), myPrice(%d)", Long.valueOf(bidToOverlap.getPrice()), Integer.valueOf(bidToOverlap.getQty()), Long.valueOf(this.context.getActiveOrder().getQtyLeft()), Long.valueOf(this.price)));
            OrderBookRow preBestBid = book.getPreBestBid();
            if (preBestBid != null && preBestBid.getPrice() < this.price - this.context.getMaxOverlapStep() && preBestBid.getPrice() > this.context.getStartPrice()) {
                Trace(String.format("preBestBid != null && (preBestBid(%d) < price(%d) - getMaxOverlapStep(%d)) && (preBestBid.getPrice(%d) > context.getStartPrice(%d))", Long.valueOf(preBestBid.getPrice()), Long.valueOf(this.price), Integer.valueOf(this.context.getMaxOverlapStep()), Long.valueOf(preBestBid.getPrice()), Integer.valueOf(this.context.getStartPrice())));
                this.context.setState(new BotStateOverlapCancelling(this.context, true));
            } else if (preBestBid == null && this.price > this.context.getStartPrice()) {
                Trace(String.format("preBestBid == null && price (%d) > startPrice(%d)", Long.valueOf(this.price), Integer.valueOf(this.context.getStartPrice())));
                this.context.setState(new BotStateOverlapCancelling(this.context, true));
            }
        }
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Handle(SecurityData securityData) {
        Trace("HandleSecurityData");
        this.activeOrder = this.context.getActiveOrder();
        this.previousOrder = this.context.getPreviousOrder();
        if (securityData == null || this.context.getBotMode() != BotMode.TurboOverlap) {
            return;
        }
        if (this.activeOrder == null) {
            Trace("При обработке SecurityData произошла ошибка, order == null");
            return;
        }
        if (this.activeOrder.getStatus().equals(STEProtos.OrderStatusType.MATCHED)) {
            return;
        }
        long possiblePriceViaPriceStep = this.price + this.context.getInstrumentPriceStep();
        if (PriceLowerThanLimits(this.price) && PriceLowerThanLimits(possiblePriceViaPriceStep)) {
            Trace(String.format("price (%d) < %d, price < %d, шаг цены %d позволяет подняться, перевыставляем", Long.valueOf(this.price), Long.valueOf(this.context.getPriceLimit()), this.context.getUpperPriceLimit(), Integer.valueOf(this.context.getInstrumentPriceStep())));
            this.isActiveOrderReadyToOverlap = true;
            setNextState();
        }
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Handle(OrderData orderData) {
        Trace("HandleOrderData");
        this.activeOrder = this.context.getActiveOrder();
        this.previousOrder = this.context.getPreviousOrder();
        checkLimitExceeded();
        if (this.activeOrder != null && orderData.getId() == this.activeOrder.getId().longValue()) {
            switch (AnonymousClass2.$SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$OrderStatus[orderData.getStatus().ordinal()]) {
                case 1:
                    stopTimer(OrderStatus.QUEUED);
                    break;
                case 2:
                    stopTimer(OrderStatus.CANCELED);
                    this.isStopRequired = true;
                    setNextState();
                    break;
                case CipherHolder.CIPHER_PARAM_ENCRYPTED_SECRET_KEY /* 3 */:
                    stopTimer(OrderStatus.MATCHED);
                    if (orderData.getQtyLeft() <= 0) {
                        this.isActiveOrderReadyToOverlap = true;
                        this.context.clearActiveOrder();
                        setNextState();
                        break;
                    }
                    break;
                case CipherHolder.CIPHER_PARAM_IV /* 4 */:
                    this.timer = new Timer("WaitApproval Timer");
                    Trace("Получена заявка со статусом WAIT_APPROVAL. Запускается таймер на ожидания изменения статуса");
                    this.isTimerRunning = true;
                    this.timer.schedule(new TimerTask() { // from class: ru.effectivegroup.client.algoil.bots.states.BotStateOverlapPlaced.1
                        @Override // java.util.TimerTask, java.lang.Runnable
                        public void run() {
                            cancel();
                            BotStateOverlapPlaced.this.timer.cancel();
                            BotStateOverlapPlaced.this.waitApprovalTimerEnds = true;
                            BotStateOverlapPlaced.this.context.Handle(WaitApprovalTimerState.Timeout);
                        }
                    }, 3000L);
                    break;
            }
        }
        if (this.previousOrder != null && orderData.getId() == this.previousOrder.getId().longValue()) {
            if (this.previousOrder == null) {
                Trace("Нет предыдущей заявки");
                this.isPrevOrderCancelled = true;
                this.context.clearPreviousOrder();
                setNextState();
                return;
            }
            if (this.previousOrder.getStatus() == STEProtos.OrderStatusType.CANCELED) {
                Trace("Предыдущая заявка снята");
                this.isPrevOrderCancelled = true;
                this.context.clearPreviousOrder();
                setNextState();
                return;
            }
            if (this.previousOrder.getStatus() == STEProtos.OrderStatusType.MATCHED && this.previousOrder.getQtyLeft() == 0) {
                Trace("Предыдущая заявка исполнена");
                this.isPrevOrderCancelled = true;
                this.context.clearPreviousOrder();
                setNextState();
                return;
            }
            Trace(String.format("orderData!=null, orderData.getStatus(%s)", orderData.getStatus()));
            if (orderData.getCode() != null && orderData.getCode().equals(this.previousOrder.getCode())) {
                Trace(String.format("orderData.getCode(%s) == activeOrder.getCode(%s)", orderData.getCode(), this.previousOrder.getCode()));
                this.isPrevOrderCancelled = true;
                this.context.clearPreviousOrder();
                setNextState();
                return;
            }
            Trace(String.format("orderData.getCode(%s) != activeOrder.getCode(%s)", orderData.getCode(), this.previousOrder.getCode()));
        }
    }

    /* renamed from: ru.effectivegroup.client.algoil.bots.states.BotStateOverlapPlaced$2, reason: invalid class name */
    /* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/states/BotStateOverlapPlaced$2.class */
    static /* synthetic */ class AnonymousClass2 {
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

    private void setNextState() {
        if (this.isPrevOrderShouldBeCancelled && !this.isPrevOrderCancelled) {
            Trace("Предыдущая заявка еще активна, ждем");
            return;
        }
        if (this.isStopRequired) {
            this.context.setState(new BotStateStopping(this.context, null));
            return;
        }
        if (this.isLimitExceeded) {
            this.context.setState(new BotStateStopping(this.context, "position >= positionLimit. Позиция набрана"));
        }
        if (this.waitApprovalTimerEnds) {
            this.context.setState(new BotStateError(this.context, "Не удалось получить статус заявки отличный от WAIT_APPROVAL"));
            return;
        }
        if (this.context.getOrdersCount() >= this.context.getOrderCountLimit()) {
            Trace("Достигнут лимит по количеству заявок в день, новую заявку не выставляем");
            if (this.context.getActiveOrder() == null) {
                this.context.setState(new BotStateIdle(this.context));
                return;
            }
            return;
        }
        if (this.isActiveOrderReadyToOverlap) {
            Trace("Заявки для снятия нет, переходим к выставлению новой заявки");
            this.context.setState(new BotStateOverlapWaiting(this.context, true));
        }
    }

    private boolean PriceLowerThanLimits(long price) {
        if (price >= this.context.getPriceLimit()) {
            return false;
        }
        if (this.context.getUpperPriceLimit() != null && price >= this.context.getUpperPriceLimit().longValue()) {
            return false;
        }
        return true;
    }

    private void checkLimitExceeded() {
        this.isLimitExceeded = this.context.getPosition() >= this.context.getPositionLimit();
    }

    private void stopTimer(OrderStatus status) {
        if (!this.isTimerRunning) {
            return;
        }
        Trace(String.format("Заявка в состоянии %s, таймер ожидания остановлен", status));
        this.timer.cancel();
        this.isTimerRunning = false;
    }
}
