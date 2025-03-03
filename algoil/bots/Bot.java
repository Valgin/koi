package ru.effectivegroup.client.algoil.bots;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import ru.effectivegroup.client.algoil.ExchangeTimeProvider;
import ru.effectivegroup.client.algoil.Instrument;
import ru.effectivegroup.client.algoil.OrderCounter;
import ru.effectivegroup.client.algoil.Utils;
import ru.effectivegroup.client.algoil.bots.states.BotState;
import ru.effectivegroup.client.algoil.bots.states.BotStateContext;
import ru.effectivegroup.client.algoil.bots.states.BotStateIdle;
import ru.effectivegroup.client.algoil.bots.states.BotStateKind;
import ru.effectivegroup.client.algoil.execution.CancelOrderResult;
import ru.effectivegroup.client.algoil.execution.ExecutionManagerEventsConsumer;
import ru.effectivegroup.client.algoil.execution.Order;
import ru.effectivegroup.client.algoil.execution.OrderEmitter;
import ru.effectivegroup.client.algoil.execution.PlaceOrderResult;
import ru.effectivegroup.client.algoil.marketdata.DataFeedMessageConsumer;
import ru.effectivegroup.client.algoil.marketdata.OrderBook;
import ru.effectivegroup.client.algoil.marketdata.OrderBookRow;
import ru.effectivegroup.client.algoil.settings.BotSettings;
import ru.effectivegroup.client.crypto.cipher.CipherHolder;
import ru.effectivegroup.client.model.data.ste.HoldingData;
import ru.effectivegroup.client.model.data.ste.OrderData;
import ru.effectivegroup.client.model.data.ste.SecurityData;
import ru.effectivegroup.client.model.data.ste.TradeData;
import ru.effectivegroup.client.model.data.ste.dictionary.BSType;
import ru.effectivegroup.client.model.data.ste.dictionary.OrderStatus;
import ru.effectivegroup.client.model.data.ste.dictionary.TradeStatus;
import ru.effectivegroup.client.network.message.STEProtos;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/Bot.class */
public class Bot implements BotStateContext, OrderEmitter, DataFeedMessageConsumer, Runnable, ExecutionManagerEventsConsumer, ExchangeTimeProvider.TimeConsumer {
    private final Object sync;
    private final BotLogger logger;
    private final ConcurrentLinkedQueue<UserEventType> userEventTypeQueue;
    private final ConcurrentLinkedQueue<OrderData> orderDataQueue;
    private final ConcurrentLinkedQueue<Object> eventsQueue;
    private final HashSet<String> ordersInFinalState;
    private final HashSet<Long> tradesInPosition;
    private final HashSet<String> ordersThatUpdatePosition;
    private BotContext context;
    private Instrument instrument;
    private String moneyAccount;
    private String commodityAccount;
    private String client;
    private int orderSize;
    private int startPrice;
    private int priceLimit;
    private int maxOverlapStep;
    private int minOverlapStep;
    private int instrumentPriceStep;
    private int positionLimit;
    private int orderCountLimit;
    private int delayPlace;
    private int delayCancel;
    private double maxPricePercentage;
    private BotMode botMode;
    private int position;
    private BotState state;
    private OrderBook orderBook;
    private Order activeOrder;
    private Order previousOrder;
    private Long exchangePriceUpperLimit;
    private Long exchangePriceLowerLimit;
    private Long calculatedPriceUpperLimit;
    private Long calculatedPriceLowerLimit;
    private Long marketPrice;
    private Long currentPrice;
    private int startVolume;
    private int todayVolume;
    private SecurityData lastSecurityData;
    private String startTime;
    private Long timeSpread;
    private boolean fallDown;
    private boolean botListFixation;
    private boolean isNextOrderFirst;
    private String discreteAuctionEndTime;
    private BotStartMode botStartMode;
    private LocalTime localTime;
    private LocalTime errorProcessingTime;
    private boolean securityIsTradable;
    private HashSet<BotStateChangeListener> botStateChangeListeners;
    private HashSet<BotOrderChangeListener> botOrderChangeListeners;
    private SimpleEventListener priceLimitsListener;
    private BotPositionChangeListener positionChangeListener;
    private SimpleEventListener todayOrdersCountChangeListener;
    private boolean processingStopped;
    private Thread processingThread;
    private final HashMap<String, Integer> mapOrderOnFilledQty;

    /* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/Bot$SimpleEventListener.class */
    public interface SimpleEventListener {
        void Handle();
    }

    public Bot(BotContext context, Instrument instrument, String moneyAccount, String commodityAccount) {
        this.sync = new Object();
        this.userEventTypeQueue = new ConcurrentLinkedQueue<>();
        this.orderDataQueue = new ConcurrentLinkedQueue<>();
        this.eventsQueue = new ConcurrentLinkedQueue<>();
        this.ordersInFinalState = new HashSet<>();
        this.tradesInPosition = new HashSet<>();
        this.ordersThatUpdatePosition = new HashSet<>();
        this.instrumentPriceStep = 0;
        this.securityIsTradable = false;
        this.botStateChangeListeners = new HashSet<>();
        this.botOrderChangeListeners = new HashSet<>();
        this.mapOrderOnFilledQty = new HashMap<>();
        this.delayCancel = 0;
        this.maxOverlapStep = 10;
        this.minOverlapStep = 1;
        this.delayPlace = 1100;
        this.context = context;
        this.instrument = instrument;
        this.moneyAccount = moneyAccount;
        this.commodityAccount = commodityAccount;
        this.orderCountLimit = 50;
        this.logger = new BotLogger(this.instrument);
        this.state = new BotStateIdle(this);
        this.maxPricePercentage = 1.0d;
        this.startVolume = 0;
        this.timeSpread = 1000L;
        this.startTime = "1045";
        this.fallDown = false;
        this.botListFixation = false;
        this.discreteAuctionEndTime = "1100";
        this.botStartMode = BotStartMode.Time;
        runProcessing();
    }

    public Bot(BotContext context, BotSettings botSettings) {
        this.sync = new Object();
        this.userEventTypeQueue = new ConcurrentLinkedQueue<>();
        this.orderDataQueue = new ConcurrentLinkedQueue<>();
        this.eventsQueue = new ConcurrentLinkedQueue<>();
        this.ordersInFinalState = new HashSet<>();
        this.tradesInPosition = new HashSet<>();
        this.ordersThatUpdatePosition = new HashSet<>();
        this.instrumentPriceStep = 0;
        this.securityIsTradable = false;
        this.botStateChangeListeners = new HashSet<>();
        this.botOrderChangeListeners = new HashSet<>();
        this.mapOrderOnFilledQty = new HashMap<>();
        this.context = context;
        this.instrument = botSettings.getInstrument();
        this.priceLimit = botSettings.getMaxPrice();
        this.startPrice = botSettings.getStartPrice();
        this.delayPlace = botSettings.getDelayPlace();
        this.delayCancel = 0;
        this.maxPricePercentage = botSettings.getMaxPricePercentage();
        this.positionLimit = botSettings.getPositionLimit();
        this.orderCountLimit = botSettings.getOrderCountLimit();
        this.maxOverlapStep = botSettings.getOverlapStep();
        this.minOverlapStep = botSettings.getMinOverlapStep();
        this.client = botSettings.getClient();
        this.botMode = botSettings.getBotMode();
        this.commodityAccount = botSettings.getCommodityAccount();
        this.moneyAccount = botSettings.getMoneyAccount();
        this.orderSize = botSettings.getOrderSize();
        this.logger = new BotLogger(this.instrument);
        this.state = new BotStateIdle(this);
        this.startVolume = botSettings.getStartVolume();
        this.fallDown = botSettings.getFallDown();
        this.botListFixation = botSettings.isBotListFixation();
        if (botSettings.getTimeSpread() == null) {
            this.timeSpread = 1000L;
        } else {
            this.timeSpread = botSettings.getTimeSpread();
        }
        if (botSettings.getStartTime() == null) {
            this.startTime = "1045";
        } else {
            this.startTime = botSettings.getStartTime();
        }
        if (botSettings.getDiscreteAuctionEndTime() == null) {
            this.discreteAuctionEndTime = "1100";
        } else {
            this.discreteAuctionEndTime = botSettings.getDiscreteAuctionEndTime();
        }
        if (botSettings.getBotStartMode() == null) {
            this.botStartMode = BotStartMode.Time;
        } else {
            this.botStartMode = botSettings.getBotStartMode();
        }
        runProcessing();
    }

    private static void sleep(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
        }
    }

    public void Start() {
        this.logger.info("Запуск");
        this.userEventTypeQueue.add(UserEventType.Start);
    }

    public void Stop() {
        this.logger.info("Остановка");
        this.userEventTypeQueue.add(UserEventType.Stop);
    }

    @Override // ru.effectivegroup.client.algoil.marketdata.DataFeedMessageConsumer
    public void Handle(SecurityData securityData) {
        this.eventsQueue.add(securityData);
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public void Handle(WaitApprovalTimerState state) {
        this.eventsQueue.add(state);
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public LocalTime getErrorProcessingTime() {
        return this.errorProcessingTime;
    }

    @Override // ru.effectivegroup.client.algoil.marketdata.DataFeedMessageConsumer
    public void Handle(TradeData message) {
        this.eventsQueue.add(message);
    }

    @Override // ru.effectivegroup.client.algoil.marketdata.DataFeedMessageConsumer
    public void Handle(OrderBook book) {
        this.orderBook = book;
        this.logger.debug("Получен стакан, количество бидов {}", Integer.valueOf(book.getBids().size()));
        this.eventsQueue.add(book);
    }

    @Override // ru.effectivegroup.client.algoil.ExchangeTimeProvider.TimeConsumer
    public void HandleTime(LocalTime time) {
        this.eventsQueue.add(time);
    }

    @Override // ru.effectivegroup.client.algoil.execution.OrderEmitter
    public void HandleOrderStateChange(OrderData orderData) {
        this.orderDataQueue.add(orderData);
        RaiseTodayOrdersCountChanged();
    }

    public boolean SubscribeStateChange(BotStateChangeListener listener) {
        synchronized (this.sync) {
            if (this.botStateChangeListeners.contains(listener)) {
                return false;
            }
            this.botStateChangeListeners.add(listener);
            return true;
        }
    }

    public void UnsubscribeStateChange(BotStateChangeListener listener) {
        synchronized (this.sync) {
            this.botStateChangeListeners.remove(listener);
        }
    }

    public boolean SubscribeOrderChange(BotOrderChangeListener listener) {
        synchronized (this.sync) {
            if (this.botOrderChangeListeners.contains(listener)) {
                return false;
            }
            this.botOrderChangeListeners.add(listener);
            return true;
        }
    }

    public void UnsubscribeOrderChange(BotOrderChangeListener listener) {
        synchronized (this.sync) {
            this.botOrderChangeListeners.remove(listener);
        }
    }

    public void SetPriceLimitsChangeListener(SimpleEventListener listener) {
        this.priceLimitsListener = listener;
    }

    public void SetPositionChangeListener(BotPositionChangeListener listener) {
        this.positionChangeListener = listener;
    }

    public void SetTodayOrdersCountChangeListener(SimpleEventListener listener) {
        this.todayOrdersCountChangeListener = listener;
    }

    @Override // java.lang.Runnable
    public void run() {
        this.logger.info("Bot started");
        while (!this.processingStopped) {
            try {
                Object event = this.userEventTypeQueue.poll();
                if (event == null) {
                    event = this.orderDataQueue.poll();
                    if (event == null) {
                        event = this.eventsQueue.poll();
                    }
                }
                if (event == null) {
                    Thread.sleep(1L);
                } else if (event instanceof UserEventType) {
                    processUserAction((UserEventType) event);
                } else if (event instanceof SecurityData) {
                    processSecurityData((SecurityData) event);
                } else if (event instanceof TradeData) {
                    processTradeData((TradeData) event);
                } else if (event instanceof WaitApprovalTimerState) {
                    this.state.Handle((WaitApprovalTimerState) event);
                } else if (event instanceof OrderData) {
                    processOrderStateChange((OrderData) event);
                } else if (event instanceof OrderBook) {
                    this.state.Handle((OrderBook) event);
                } else if (event instanceof LocalTime) {
                    this.localTime = (LocalTime) event;
                    this.state.Handle((LocalTime) event);
                }
            } catch (InterruptedException e) {
                this.logger.info("Остановка потока обработки событий бота", e);
                return;
            } catch (Exception e2) {
                this.logger.fatal("Необработанная ошибка логики бота, бот остановлен", e2);
                return;
            }
        }
        this.logger.info("Поток обработки завершён штатно");
    }

    public String toString() {
        return this.instrument.getCode();
    }

    public void StopProcessing() {
        this.processingStopped = true;
        this.processingThread.interrupt();
    }

    @Override // ru.effectivegroup.client.algoil.execution.ExecutionManagerEventsConsumer
    public void Handle(HoldingData holdingData) {
    }

    public String getClient() {
        return this.client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public Instrument getInstrument() {
        return this.instrument;
    }

    public boolean isRunning() {
        return this.state.getStateKind() == BotStateKind.Running;
    }

    public BotSettings getSettings() {
        return new BotSettings(this.instrument, this.moneyAccount, this.commodityAccount, this.client, this.orderSize, this.startPrice, this.priceLimit, this.positionLimit, this.orderCountLimit, this.maxOverlapStep, this.minOverlapStep, this.delayPlace, this.delayCancel, this.maxPricePercentage, this.botMode, this.timeSpread, this.startVolume, this.startTime, this.fallDown, this.botListFixation, this.discreteAuctionEndTime, this.botStartMode);
    }

    public BotState getState() {
        return this.state;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public void setState(BotState newState) {
        this.logger.info("state change {} → {}", this.state.getName(), newState.getName());
        this.state.Exit();
        this.state = newState;
        RaiseStateChanged(newState.getStateKind());
        this.state.Enter();
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public Long getUpperPriceLimit() {
        if (this.exchangePriceUpperLimit != null && this.exchangePriceUpperLimit.longValue() != 0) {
            this.logger.info("В качестве верхней границы используется биржевая граница ({})", this.exchangePriceUpperLimit);
            return this.exchangePriceUpperLimit;
        }
        if (this.calculatedPriceUpperLimit != null && this.calculatedPriceUpperLimit.longValue() != 0) {
            this.logger.info("В качестве верхней границы используется расчетная граница ({})", this.calculatedPriceUpperLimit);
            return this.calculatedPriceUpperLimit;
        }
        this.logger.info("В качестве верхней границы используется пользовательская максимальная цена ({})", Integer.valueOf(this.priceLimit));
        return Long.valueOf(Integer.toUnsignedLong(this.priceLimit));
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public int getStartPrice() {
        return this.startPrice;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public String getDiscreteAuctionEndTime() {
        return this.discreteAuctionEndTime;
    }

    public void setDiscreteAuctionEndTime(String discreteAuctionEndTime) {
        this.discreteAuctionEndTime = discreteAuctionEndTime;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public LocalTime getTime() {
        return this.localTime;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public PlaceOrderResult PlaceOrder(BSType operation, long quantity, long price) {
        PlaceOrderResult placeOrderResult = this.context.PlaceOrder(this.instrument.getCode(), operation, quantity, price, this.moneyAccount, this.commodityAccount, "", "", "", this);
        if (placeOrderResult.isSuccessful()) {
            this.activeOrder = placeOrderResult.getOrder();
            if (this.activeOrder.getStatus() == STEProtos.OrderStatusType.MATCHED || this.activeOrder.getStatus() == STEProtos.OrderStatusType.QUEUED) {
                updatePosition(this.activeOrder.getCode(), this.activeOrder.getStatus(), (int) this.activeOrder.getQty(), (int) this.activeOrder.getQtyLeft(), false);
            } else {
                this.logger.warn("Заявка на будет учтена в позиции, статус заявки {}", this.activeOrder.getStatus().toString());
            }
            this.currentPrice = Long.valueOf(price);
            RaiseOrderPriceChanged();
        }
        return placeOrderResult;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public CancelOrderResult CancelActiveOrder() {
        if (this.activeOrder == null) {
            return CancelOrderResult.CreateSuccessful();
        }
        this.currentPrice = 0L;
        RaiseOrderPriceChanged();
        return this.context.CancelOrder(this.activeOrder);
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public CancelOrderResult CancelPreviousOrder() {
        if (this.previousOrder == null) {
            return CancelOrderResult.CreateSuccessful();
        }
        RaiseOrderPriceChanged();
        return this.context.CancelOrder(this.previousOrder);
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public int getPositionLimit() {
        return this.positionLimit;
    }

    public void setPositionLimit(int positionLimit) {
        this.positionLimit = positionLimit;
    }

    public Long getTimeSpread() {
        return this.timeSpread;
    }

    public void setTimeSpread(Long timeSpread) {
        this.timeSpread = timeSpread;
    }

    public Long getCalculatedPriceUpperLimit() {
        return this.calculatedPriceUpperLimit;
    }

    public Long getCalculatedPriceLowerLimit() {
        return this.calculatedPriceLowerLimit;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public int getOrderCountLimit() {
        return this.orderCountLimit;
    }

    public void setOrderCountLimit(int orderCountLimit) {
        this.orderCountLimit = orderCountLimit;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public int getMaxOverlapStep() {
        return this.maxOverlapStep;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public int getOverlapStep() {
        int result;
        if (this.maxOverlapStep <= 1) {
            return this.maxOverlapStep;
        }
        if (this.instrumentPriceStep == 1) {
            result = ThreadLocalRandom.current().nextInt(this.minOverlapStep, this.maxOverlapStep + 1);
        } else {
            int range = this.maxOverlapStep - this.minOverlapStep;
            double shift = Math.random() * range;
            double overlapStep = shift + this.minOverlapStep;
            result = (int) (Math.round(overlapStep / this.instrumentPriceStep) * this.instrumentPriceStep);
        }
        return result;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public int getDelayPlace() {
        return this.delayPlace;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public int getDelayCancel() {
        return this.delayCancel;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public int getInstrumentPriceStep() {
        return this.instrumentPriceStep;
    }

    public void setInstrumentPriceStep(int instrumentPriceStep) {
        this.instrumentPriceStep = instrumentPriceStep;
    }

    public void setDelayPlace(int delayPlace) {
        this.delayPlace = delayPlace;
    }

    public void setDelayCancel(int delayCancel) {
        this.delayCancel = delayCancel;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public int getPosition() {
        return this.position;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public BotMode getBotMode() {
        return this.botMode;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public boolean isNextOrderFirst() {
        return this.isNextOrderFirst;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public void setNextOrderFirst(boolean isFirst) {
        this.isNextOrderFirst = isFirst;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public boolean getFallDown() {
        return this.fallDown;
    }

    public void setFallDown(Boolean fallDown) {
        this.fallDown = fallDown.booleanValue();
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public int getStartVolume() {
        return this.startVolume;
    }

    public void setStartVolume(int startVolume) {
        this.startVolume = startVolume;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public long getPriceLimit() {
        return this.priceLimit;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public int getOrderSize() {
        return this.orderSize;
    }

    public void setOrderSize(int orderSize) {
        this.orderSize = orderSize;
    }

    public boolean isBotListFixation() {
        return this.botListFixation;
    }

    public void setBotListFixation(boolean botListFixation) {
        this.botListFixation = botListFixation;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public boolean bookHasOwnBids() {
        if (this.orderBook == null) {
            return false;
        }
        return this.orderBook.hasOwnBids();
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public int getQty() {
        return Math.min(getPositionLimit() - getPosition(), getOrderSize());
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public void clearActiveOrder() {
        String id = this.activeOrder.getCode();
        this.logger.info("Обнуляем активную заявку {}", id);
        if (id != null && !id.trim().isEmpty()) {
            this.ordersInFinalState.add(id);
        }
        this.activeOrder = null;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public void clearPreviousOrder() {
        if (this.previousOrder == null) {
            return;
        }
        String id = this.previousOrder.getCode();
        this.logger.info("Обнуляем предыдущую заявку {}", id);
        if (id != null && !id.trim().isEmpty()) {
            this.ordersInFinalState.add(id);
        }
        this.previousOrder = null;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public int getOrdersCount() {
        if (this.instrument == null) {
            return 0;
        }
        return OrderCounter.getInstance().getOrdersCount(this.instrument.getCode());
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public boolean getSecurityIsTradable() {
        return this.securityIsTradable;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public boolean timeIsBefore94ErrorProcessingTime() {
        return this.context.timeIsBefore94ErrorProcessingTime(this.errorProcessingTime);
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public void setErrorProcessingTime(LocalTime time) {
        this.errorProcessingTime = time;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public boolean timeIsBefore1045() {
        return this.context.timeIsBefore1045(getTimeSpread());
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public boolean timeIsBeforeStartTime() {
        return this.context.timeIsBeforeStartTime(Utils.convertToLocalTime(getStartTime()), getTimeSpread());
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public boolean timeIsAfter1045() {
        return this.context.timeIsAfter1045();
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public int getTodayVolume() {
        return this.todayVolume;
    }

    public void setPriceLimit(int priceLimit) {
        this.priceLimit = priceLimit;
    }

    public void setBotMode(BotMode botMode) {
        this.botMode = botMode;
    }

    public void setMaxOverlapStep(int maxOverlapStep) {
        this.maxOverlapStep = maxOverlapStep;
    }

    public int getMinOverlapStep() {
        return this.minOverlapStep;
    }

    public void setMinOverlapStep(int minOverlapStep) {
        this.minOverlapStep = minOverlapStep;
    }

    public void setStartPrice(int startPrice) {
        this.startPrice = startPrice;
    }

    public Long getCurrentPrice() {
        return this.currentPrice;
    }

    public String getMoneyAccount() {
        return this.moneyAccount;
    }

    public void setMoneyAccount(String moneyAccount) {
        this.moneyAccount = moneyAccount;
    }

    public String getCommodityAccount() {
        return this.commodityAccount;
    }

    public void setCommodityAccount(String commodityAccount) {
        this.commodityAccount = commodityAccount;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public String getStartTime() {
        return this.startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public BotStartMode getBotStartMode() {
        return this.botStartMode;
    }

    public void setBotStartMode(BotStartMode botStartMode) {
        this.botStartMode = botStartMode;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public BotLogger getLogger() {
        return this.logger;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public List<OrderBookRow> getBids() {
        if (this.orderBook == null) {
            return null;
        }
        return this.orderBook.getBids();
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public OrderBookRow getBestAsk() {
        if (this.orderBook == null) {
            return null;
        }
        return this.orderBook.getBestAsk();
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public OrderBookRow getBestBid() {
        if (this.orderBook == null) {
            return null;
        }
        return this.orderBook.getBestBid();
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public Order getActiveOrder() {
        return this.activeOrder;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public Order getPreviousOrder() {
        return this.previousOrder;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public void setActiveOrderAsPrevious() {
        this.previousOrder = this.activeOrder;
        this.activeOrder = null;
    }

    @Override // ru.effectivegroup.client.algoil.bots.states.BotStateContext
    public boolean hasPreviousOrder() {
        return this.previousOrder != null && this.previousOrder.getStatus() == STEProtos.OrderStatusType.QUEUED;
    }

    public double getMaxPricePercentage() {
        if (this.maxPricePercentage == 0.0d) {
            return 1.0d;
        }
        return this.maxPricePercentage;
    }

    public void setMaxPricePercentage(double value) {
        this.maxPricePercentage = value;
        if (this.lastSecurityData != null) {
            processSecurityData(this.lastSecurityData);
        }
    }

    private void processTradeData(TradeData tradeData) {
        if (!tradeData.getSecurityCode().equals(this.instrument.getCode())) {
            this.logger.debug("Сделка не принадлежит боту {}", tradeData.getSecurityCode());
            return;
        }
        String buyer = tradeData.getBuyerFirmCode();
        if (buyer == null || buyer.isEmpty()) {
            return;
        }
        long tradeId = tradeData.getId();
        String orderId = tradeData.getBuyOrderCode();
        this.logger.info("Получена сделка: tradeId={} orderId={} price={} qty={} buyOrderCode={}", Long.valueOf(tradeId), orderId, Long.valueOf(tradeData.getPrice()), Integer.valueOf(tradeData.getQuantity()), tradeData.getBuyOrderCode());
        if (this.tradesInPosition.contains(Long.valueOf(tradeData.getId()))) {
            this.logger.info("Сделка tradeId={} уже учтена в позиции", Long.valueOf(tradeData.getId()));
            return;
        }
        if (this.ordersThatUpdatePosition.contains(orderId)) {
            this.logger.info("Сделка tradeId={} не будет учтена в позиции, т.к. позиция обновляется по событиям заявки orderId={}", Long.valueOf(tradeId), orderId);
            return;
        }
        this.position += tradeData.getQuantity();
        this.tradesInPosition.add(Long.valueOf(tradeData.getId()));
        this.logger.info("Сделка tradeId={} учтена в позиции: price={} qty={} position={} buyer={}", Long.valueOf(tradeData.getId()), Long.valueOf(tradeData.getPrice()), Integer.valueOf(tradeData.getQuantity()), Integer.valueOf(this.position), buyer);
        RaisePositionChanged();
    }

    private boolean validateOrderData(OrderData orderData) {
        this.logger.info("OSC: id={} trnId={} state={}", Long.valueOf(orderData.getId()), orderData.getTrn(), orderData.getStatus());
        if (!getInstrument().getCode().equals(orderData.getSecurityCode())) {
            this.logger.fatal("Получено OrderData по не своему {} инструменту {}", getInstrument().getCode(), orderData.getSecurityCode());
            return false;
        }
        if (this.ordersInFinalState.contains(orderData.getCode())) {
            this.logger.fatal("Получено OrderData по уже завершенной заявке {}", Long.valueOf(orderData.getId()));
            return false;
        }
        if (orderData.cashAccountCode == null || orderData.cashAccountCode.isEmpty()) {
            this.logger.fatal("Получено OrderData без счета {}", Long.valueOf(orderData.getId()));
            return false;
        }
        if (this.activeOrder == null && this.previousOrder == null) {
            this.logger.warn("Получено OrderData когда activeOrder и cancellingOrder =null", null);
            return true;
        }
        return true;
    }

    private void RaiseStateChanged(BotStateKind stateKind) {
        Iterator<BotStateChangeListener> it = this.botStateChangeListeners.iterator();
        while (it.hasNext()) {
            BotStateChangeListener listener = it.next();
            listener.Handle(stateKind);
        }
    }

    private void RaiseOrderPriceChanged() {
        Iterator<BotOrderChangeListener> it = this.botOrderChangeListeners.iterator();
        while (it.hasNext()) {
            BotOrderChangeListener listener = it.next();
            listener.HandleBotOrderPriceChanged(this);
        }
    }

    private void RaisePositionChanged() {
        if (this.positionChangeListener != null) {
            this.positionChangeListener.Handle(this.position);
        }
    }

    private void RaiseTodayOrdersCountChanged() {
        if (this.todayOrdersCountChangeListener != null) {
            this.todayOrdersCountChangeListener.Handle();
        }
    }

    private void processSecurityData(SecurityData securityData) {
        if (securityData != null) {
            this.lastSecurityData = securityData;
            this.securityIsTradable = securityData.getSessionStatus() == TradeStatus.OPENED_FOR_BUY_ORDERS || securityData.getSessionStatus() == TradeStatus.OPENED;
            Long longMarketPrice = Long.valueOf(securityData.getMarketPrice());
            this.exchangePriceUpperLimit = Long.valueOf(securityData.getUpperPrice() / 100);
            this.exchangePriceLowerLimit = Long.valueOf(securityData.getLowerPrice() / 100);
            if (longMarketPrice != null && longMarketPrice.longValue() != 0) {
                double scale = 1.0d + (getMaxPricePercentage() / 100.0d);
                this.calculatedPriceUpperLimit = Long.valueOf(Math.round(longMarketPrice.longValue() * scale));
                this.calculatedPriceLowerLimit = Long.valueOf(Math.round(longMarketPrice.longValue() * (1.0d - (scale - 1.0d))));
                this.calculatedPriceUpperLimit = Long.valueOf(Math.round(this.calculatedPriceUpperLimit.longValue() / 100.0d));
                this.calculatedPriceLowerLimit = Long.valueOf(Math.round(this.calculatedPriceLowerLimit.longValue() / 100.0d));
            }
            this.marketPrice = Long.valueOf(longMarketPrice.longValue() / 100);
            this.logger.info("Обработка SecurityData: рыночная цена инструмента {}, верхняя биржевая граница {}, верхняя расчетная граница {}, maxPricePercentage {}", this.marketPrice, this.exchangePriceUpperLimit, this.calculatedPriceUpperLimit, Double.valueOf(this.maxPricePercentage));
            if (this.priceLimitsListener != null) {
                this.priceLimitsListener.Handle();
            }
            this.todayVolume = securityData.getVolume() / securityData.getLotSize();
            this.state.Handle(securityData);
            RaiseOrderPriceChanged();
        }
    }

    private void processOrderStateChange(OrderData orderData) {
        if (!validateOrderData(orderData)) {
            return;
        }
        if (this.activeOrder != null && orderData.getId() == this.activeOrder.getId().longValue()) {
            processOrderDataStatus(orderData, this.activeOrder);
        }
        if (this.previousOrder != null && orderData.getId() == this.previousOrder.getId().longValue()) {
            processOrderDataStatus(orderData, this.previousOrder);
        }
        RaiseOrderPriceChanged();
        this.state.Handle(orderData);
    }

    private void processOrderDataStatus(OrderData orderData, Order order) {
        switch (AnonymousClass1.$SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$OrderStatus[orderData.getStatus().ordinal()]) {
            case 1:
                order.setStatus(STEProtos.OrderStatusType.WAIT_APPROVAL);
                return;
            case 2:
                order.setStatus(STEProtos.OrderStatusType.QUEUED);
                int qtyLeft = orderData.getQtyLeft();
                order.setQtyLeft(qtyLeft);
                updatePosition(orderData.getCode(), order.getStatus(), (int) orderData.getQty(), qtyLeft, true);
                return;
            case CipherHolder.CIPHER_PARAM_ENCRYPTED_SECRET_KEY /* 3 */:
                order.setStatus(STEProtos.OrderStatusType.CANCELED);
                return;
            case CipherHolder.CIPHER_PARAM_IV /* 4 */:
                order.setStatus(STEProtos.OrderStatusType.MATCHED);
                int qtyLeft2 = orderData.getQtyLeft();
                order.setQtyLeft(qtyLeft2);
                updatePosition(orderData.getCode(), order.getStatus(), (int) orderData.getQty(), qtyLeft2, true);
                return;
            default:
                return;
        }
    }

    private void updatePosition(String orderId, STEProtos.OrderStatusType status, int qty, int qtyLeft, boolean nullifyPrevFilledQty) {
        long filledQuantity;
        this.logger.info("updatePosition orderId={} status={} qty={} qtyLeft={} nullifyPrevFilledQty={}", orderId, status, Integer.valueOf(qty), Integer.valueOf(qtyLeft), Boolean.valueOf(nullifyPrevFilledQty));
        this.ordersThatUpdatePosition.add(orderId);
        Integer orderPrevFilledQty = this.mapOrderOnFilledQty.get(orderId);
        if (orderPrevFilledQty == null) {
            filledQuantity = qty - qtyLeft;
        } else {
            filledQuantity = (qty - qtyLeft) - orderPrevFilledQty.intValue();
        }
        this.mapOrderOnFilledQty.put(orderId, Integer.valueOf((int) filledQuantity));
        if (filledQuantity == 0) {
            this.logger.warn("updatePosition orderId={} orderPrevFilledQty={} filledQuantity=0", orderId, orderPrevFilledQty, Long.valueOf(filledQuantity));
            return;
        }
        this.position = (int) (this.position + filledQuantity);
        this.logger.info("updatePosition position={} filledQty={} prevFilledQty={} orderQty={} orderQtyLeft={}", Integer.valueOf(this.position), Long.valueOf(filledQuantity), orderPrevFilledQty, Integer.valueOf(qty), Integer.valueOf(qtyLeft));
        RaisePositionChanged();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: ru.effectivegroup.client.algoil.bots.Bot$1, reason: invalid class name */
    /* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/Bot$1.class */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$OrderStatus;

        static {
            try {
                $SwitchMap$ru$effectivegroup$client$algoil$bots$UserEventType[UserEventType.Start.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$algoil$bots$UserEventType[UserEventType.Stop.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            $SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$OrderStatus = new int[OrderStatus.values().length];
            try {
                $SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$OrderStatus[OrderStatus.WAIT_APPROVAL.ordinal()] = 1;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$OrderStatus[OrderStatus.QUEUED.ordinal()] = 2;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$OrderStatus[OrderStatus.CANCELED.ordinal()] = 3;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$model$data$ste$dictionary$OrderStatus[OrderStatus.MATCHED.ordinal()] = 4;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    private void processUserAction(UserEventType event) {
        switch (event) {
            case Start:
                this.logger.info("Обработка запуска");
                this.logger.info("Установка времени обработки 94 ошибки в значение по умолчанию");
                this.errorProcessingTime = null;
                this.state.Start();
                return;
            case Stop:
                this.logger.info("Обработка остановки");
                this.state.Stop();
                return;
            default:
                return;
        }
    }

    private void runProcessing() {
        this.processingThread = new Thread(this, "Bot " + this.instrument.getCode() + " Thread");
        this.processingThread.start();
    }
}
