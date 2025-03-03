package ru.effectivegroup.client.algoil.bots.states;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import ru.effectivegroup.client.algoil.bots.BotMode;
import ru.effectivegroup.client.algoil.bots.BotStartMode;
import ru.effectivegroup.client.algoil.marketdata.OrderBook;
import ru.effectivegroup.client.algoil.marketdata.OrderBookRow;
import ru.effectivegroup.client.model.data.ste.SecurityData;
import ru.effectivegroup.client.network.message.STEProtos;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/states/BotStateOverlapWaiting.class */
public class BotStateOverlapWaiting extends BotStateBase {
    private final boolean isDelayRequired;
    protected long price;
    protected long targetPrice;
    LocalTime time;

    public BotStateOverlapWaiting(BotStateContext context, boolean isDelayRequired) {
        super("OVERLAP_WAITING", context);
        this.isDelayRequired = isDelayRequired;
    }

    private void GoPlacingState() {
        if (this.context.bookHasOwnBids()) {
            Trace("В стакане есть наши биды, ждём их снятия.");
        }
        if (this.isDelayRequired) {
            if (this.context.getDelayPlace() != 0) {
                Trace("Задержка != 0, переходим в состояние задержки бота");
                this.context.setState(new BotStateDelayed(this.context, this.context.getDelayPlace(), new BotStateOverlapWaiting(this.context, false)));
                return;
            }
            Trace("Задержка = 0, выставляем заявку");
        }
        if (this.context.getActiveOrder() != null && this.context.getActiveOrder().getStatus() == STEProtos.OrderStatusType.QUEUED) {
            Trace("Есть активная заявка. previousOrder = activeOrder");
            this.context.setActiveOrderAsPrevious();
        }
        if (this.context.getBotMode() == BotMode.TurboOverlap) {
            CalculateOrderPriceTurbo();
        } else {
            CalculateOrderPrice();
        }
        Trace(String.format("параметры для заявки price=%d qty=%d", Long.valueOf(this.price), Integer.valueOf(this.context.getQty())));
        this.context.setState(new BotStateOverlapPlacing(this.context, this.price, this.targetPrice, this.context.getQty()));
    }

    protected void CalculateOrderPrice() {
        Long exchangePriceLimit = this.context.getUpperPriceLimit();
        OrderBookRow bestAsk = this.context.getBestAsk();
        String auctionEndTime = this.context.getDiscreteAuctionEndTime();
        int discreteTimeMillis = Integer.parseInt(auctionEndTime) * 100000;
        int currentTimeMillis = Integer.parseInt(this.context.getTime().format(DateTimeFormatter.ofPattern("HHmmssSSS")));
        if ((Objects.equals(auctionEndTime, "0") || (!Objects.equals(auctionEndTime, "0") && currentTimeMillis > discreteTimeMillis)) && bestAsk != null && bestAsk.getPrice() <= this.context.getPriceLimit()) {
            Trace(String.format("Захватывает предложение bestAsk(%d) <= priceLimit(%d)", Long.valueOf(bestAsk.getPrice()), Long.valueOf(this.context.getPriceLimit())));
            this.price = bestAsk.getPrice();
            this.targetPrice = this.price;
            return;
        }
        OrderBookRow bidToOverlap = getBidToOverlap(this.context.getBids(), this.context.getPriceLimit());
        if (bidToOverlap == null) {
            this.price = this.context.getStartPrice();
            this.targetPrice = this.price;
            Trace(String.format("bestBid == null, price → %d", Long.valueOf(this.price)));
        } else {
            this.price = bidToOverlap.getPrice() + this.context.getOverlapStep();
            this.price = Math.min(this.price, this.context.getPriceLimit());
            this.targetPrice = this.price;
            Trace(String.format("bestBid != null (%d), price → %d", Long.valueOf(bidToOverlap.getPrice()), Long.valueOf(this.price)));
        }
        if (this.price < this.context.getStartPrice()) {
            this.price = this.context.getStartPrice();
            this.targetPrice = this.price;
            Trace(String.format("price < startPrice (%d), price → %d", Integer.valueOf(this.context.getStartPrice()), Long.valueOf(this.price)));
        }
        if (exchangePriceLimit != null) {
            this.price = this.price <= exchangePriceLimit.longValue() ? this.price : exchangePriceLimit.longValue();
            Trace(String.format("exchangePriceLimit != null (%d), price → %d", exchangePriceLimit, Long.valueOf(this.price)));
        }
    }

    protected void CalculateOrderPriceTurbo() {
        Long exchangePriceLimit = this.context.getUpperPriceLimit();
        this.price = this.context.getStartPrice();
        OrderBookRow bestAsk = this.context.getBestAsk();
        String auctionEndTime = this.context.getDiscreteAuctionEndTime();
        int discreteTimeMillis = Integer.parseInt(auctionEndTime) * 100000;
        int currentTimeMillis = Integer.parseInt(this.context.getTime().format(DateTimeFormatter.ofPattern("HHmmssSSS")));
        if ((Objects.equals(auctionEndTime, "0") || (!Objects.equals(auctionEndTime, "0") && currentTimeMillis > discreteTimeMillis)) && bestAsk != null && bestAsk.getPrice() <= this.context.getPriceLimit()) {
            Trace(String.format("Ударяем по офферу, bestAsk != null && bestAsk.getPrice(%d) <= context.getPriceLimit(%d), turbo price → %d", Long.valueOf(bestAsk.getPrice()), Long.valueOf(this.context.getPriceLimit()), Long.valueOf(bestAsk.getPrice())));
            this.price = bestAsk.getPrice();
            this.targetPrice = this.price;
            return;
        }
        if (exchangePriceLimit != null) {
            this.price = exchangePriceLimit.longValue();
            Trace(String.format("exchangePriceLimit != null (%d), turbo price → %d", exchangePriceLimit, Long.valueOf(this.price)));
        }
        if (this.price > this.context.getPriceLimit()) {
            Trace(String.format("price(%d) > context.getPriceLimit(%d), turbo price → %d", Long.valueOf(this.price), Long.valueOf(this.context.getPriceLimit()), Long.valueOf(this.context.getPriceLimit())));
            this.price = this.context.getPriceLimit();
        }
        int step = this.context.getInstrumentPriceStep();
        if (step != 0 && step != 1) {
            this.price -= this.price % step;
            Trace(String.format("instrumentPriceStep(%d) != (0,1), turbo price → %d", Integer.valueOf(step), Long.valueOf(this.price)));
        }
        this.targetPrice = this.price;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public BotStateKind getStateKind() {
        return BotStateKind.Running;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Stop() {
        Trace("Stop");
        this.context.setState(new BotStateStopping(this.context, "По запросу пользователя"));
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Enter() {
        Trace("Enter");
        if (this.context.getPosition() >= this.context.getPositionLimit()) {
            Trace(String.format("Достигнут лимит позиции %d, останавливаем бота", Integer.valueOf(this.context.getPosition())));
            this.context.setState(new BotStateIdle(this.context));
            return;
        }
        if (this.context.getOrdersCount() >= this.context.getOrderCountLimit()) {
            Trace(String.format("Достигнут лимит %d по количеству заявок в день, робот будет остановлен", Integer.valueOf(this.context.getOrderCountLimit())));
            this.context.setState(new BotStateStopping(this.context, "Достигнут лимит заявок"));
            return;
        }
        if (this.context.timeIsBefore1045()) {
            Trace("Время < 104459, ожидаем начала торгов");
            return;
        }
        if (this.context.getBotStartMode() == BotStartMode.Time) {
            Trace("Режим запуска по времени. Проверяем время запуска");
            if (this.context.timeIsBeforeStartTime()) {
                Trace(String.format("Время < %s, ждем", this.context.getStartTime()));
                return;
            }
        } else {
            Trace("Режим запуска по состоянию инструмента. Игнорируем время старта");
        }
        if (this.context.getStartVolume() > this.context.getTodayVolume()) {
            Trace(String.format("Начальный объем (%d) > (%d) проторгованный объем, ожидаем совершения сделок", Integer.valueOf(this.context.getStartVolume()), Integer.valueOf(this.context.getTodayVolume())));
        } else if (!this.context.getSecurityIsTradable()) {
            Trace("Инструмент не торгуется, ожидаем смены состояния");
        } else {
            GoPlacingState();
        }
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Handle(SecurityData securityData) {
        Trace("HandleSecurityData");
        if (this.context.getSecurityIsTradable()) {
            Trace("Инструмент начал торговаться");
            if (this.time != null) {
                Handle(this.time);
            }
        }
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Handle(OrderBook book) {
        Trace("HandleOrderBook");
        if (!this.context.getSecurityIsTradable()) {
            Trace("Инструмент не торгуется, ожидаем смены состояния");
            return;
        }
        if (this.context.getStartVolume() > this.context.getTodayVolume()) {
            Trace(String.format("Начальный объем (%d) > (%d) проторгованный объем", Integer.valueOf(this.context.getStartVolume()), Integer.valueOf(this.context.getTodayVolume())));
            return;
        }
        if (this.context.getBotStartMode() == BotStartMode.Time) {
            Trace("Режим запуска по времени. Проверяем время запуска");
            if (this.context.timeIsBeforeStartTime()) {
                Trace(String.format("Время < %s, ждем", this.context.getStartTime()));
                return;
            }
        } else {
            Trace("Режим запуска по состоянию инструмента. Игнорируем время старта");
        }
        if (this.context.timeIsBefore1045()) {
            Trace("Время < 104459, ожидаем начала торгов");
        } else {
            GoPlacingState();
        }
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateBase, ru.effectivegroup.client.algoil.bots.states.BotState
    public void Handle(LocalTime time) {
        this.time = time;
        if ((this.context.getBotStartMode() == BotStartMode.Time && this.context.timeIsBeforeStartTime()) || this.context.timeIsBefore1045() || !this.context.getSecurityIsTradable() || this.context.getStartVolume() > this.context.getTodayVolume()) {
            return;
        }
        GoPlacingState();
    }
}
