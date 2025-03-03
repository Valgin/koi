package ru.effectivegroup.client.algoil.marketdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.effectivegroup.client.algoil.Instrument;
import ru.effectivegroup.client.algoil.OrderCounter;
import ru.effectivegroup.client.algoil.adapter.DefaultSTEMessageConsumer;
import ru.effectivegroup.client.algoil.adapter.IncomingMessagesAdapter;
import ru.effectivegroup.client.context.Context;
import ru.effectivegroup.client.crypto.cipher.CipherHolder;
import ru.effectivegroup.client.model.data.ste.OrderData;
import ru.effectivegroup.client.model.data.ste.SecurityData;
import ru.effectivegroup.client.model.data.ste.TradeData;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/marketdata/DataFeed.class */
public class DataFeed extends DefaultSTEMessageConsumer {
    private final IncomingMessagesAdapter adapter;
    private final Logger logger = LogManager.getLogger("algoil");
    private final Object syncRoot = new Object();
    private final HashMap<Instrument, HashSet<DataFeedMessageConsumer>> instrumentDataSubs = new HashMap<>();
    private final HashMap<Instrument, HashSet<DataFeedMessageConsumer>> quotationSubs = new HashMap<>();
    private final HashSet<DataFeedMessageConsumer> newInstrumentSubs = new HashSet<>();
    private final HashMap<Instrument, HashSet<DataFeedMessageConsumer>> tradeDataSubs = new HashMap<>();
    private final HashSet<Instrument> instruments = new HashSet<>();
    private final ArrayList<TradeData> tradeDataBuffer = new ArrayList<>();
    private final ArrayList<OrderData> orderDataBuffer = new ArrayList<>();
    private final HashMap<Instrument, SecurityData> instrumentsData = new HashMap<>();
    private final HashMap<Instrument, HashSet<TradeData>> tradeData = new HashMap<>();
    private final HashMap<Instrument, OrderBookBuilder> orderBookBuilders = new HashMap<>();
    private final HashMap<Instrument, OrderBook> orderBooks = new HashMap<>();
    private final OrderCounter orderCounter = OrderCounter.getInstance();

    /* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/marketdata/DataFeed$DataFeedEventType.class */
    public enum DataFeedEventType {
        NewInstrument,
        InstrumentData,
        Quotation,
        TradeData
    }

    public DataFeed(IncomingMessagesAdapter adapter) {
        this.adapter = adapter;
        getInstruments();
    }

    public void Init() {
        List<SecurityData> securityDatas = Context.repositoryContext.get(SecurityData.class).getData();
        securityDatas.forEach(this::Handle);
        List<OrderData> orderDatas = Context.repositoryContext.get(OrderData.class).getData();
        orderDatas.forEach(this::Handle);
        List<TradeData> tradeDatas = Context.repositoryContext.get(TradeData.class).getData();
        tradeDatas.forEach(this::Handle);
        this.adapter.Subscribe(this);
    }

    /* renamed from: ru.effectivegroup.client.algoil.marketdata.DataFeed$1, reason: invalid class name */
    /* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/marketdata/DataFeed$1.class */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ru$effectivegroup$client$algoil$marketdata$DataFeed$DataFeedEventType = new int[DataFeedEventType.values().length];

        static {
            try {
                $SwitchMap$ru$effectivegroup$client$algoil$marketdata$DataFeed$DataFeedEventType[DataFeedEventType.NewInstrument.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$algoil$marketdata$DataFeed$DataFeedEventType[DataFeedEventType.InstrumentData.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$algoil$marketdata$DataFeed$DataFeedEventType[DataFeedEventType.Quotation.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$algoil$marketdata$DataFeed$DataFeedEventType[DataFeedEventType.TradeData.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public void Subscribe(Instrument instrument, DataFeedMessageConsumer consumer, DataFeedEventType type) {
        HashSet<DataFeedMessageConsumer> instrumentMessagesConsumers;
        HashSet<DataFeedMessageConsumer> instrumentMessagesConsumers2;
        HashSet<DataFeedMessageConsumer> instrumentMessagesConsumers3;
        boolean isAdded;
        HashSet<DataFeedMessageConsumer> instrumentMessagesConsumers4;
        switch (AnonymousClass1.$SwitchMap$ru$effectivegroup$client$algoil$marketdata$DataFeed$DataFeedEventType[type.ordinal()]) {
            case 1:
                synchronized (this.syncRoot) {
                    if (!this.newInstrumentSubs.contains(consumer)) {
                        instrumentMessagesConsumers4 = new HashSet<>();
                        this.newInstrumentSubs.add(consumer);
                    } else {
                        instrumentMessagesConsumers4 = this.newInstrumentSubs;
                    }
                    instrumentMessagesConsumers4.add(consumer);
                }
                return;
            case 2:
                if (instrument == null) {
                    return;
                }
                synchronized (this.syncRoot) {
                    if (!this.instrumentDataSubs.containsKey(instrument)) {
                        instrumentMessagesConsumers3 = new HashSet<>();
                        this.instrumentDataSubs.put(instrument, instrumentMessagesConsumers3);
                    } else {
                        instrumentMessagesConsumers3 = this.instrumentDataSubs.get(instrument);
                    }
                    isAdded = instrumentMessagesConsumers3.add(consumer);
                }
                if (isAdded) {
                    SecurityData securityData = getInstrumentData(instrument);
                    if (securityData != null) {
                        consumer.Handle(securityData);
                    }
                    OrderBook orderBook = getOrderBook(instrument);
                    if (orderBook != null) {
                        consumer.Handle(orderBook);
                        return;
                    }
                    return;
                }
                return;
            case CipherHolder.CIPHER_PARAM_ENCRYPTED_SECRET_KEY /* 3 */:
                synchronized (this.syncRoot) {
                    if (!this.quotationSubs.containsKey(instrument)) {
                        instrumentMessagesConsumers2 = new HashSet<>();
                        this.quotationSubs.put(instrument, instrumentMessagesConsumers2);
                    } else {
                        instrumentMessagesConsumers2 = this.quotationSubs.get(instrument);
                    }
                    instrumentMessagesConsumers2.add(consumer);
                }
                return;
            case CipherHolder.CIPHER_PARAM_IV /* 4 */:
                synchronized (this.syncRoot) {
                    if (!this.tradeDataSubs.containsKey(instrument)) {
                        instrumentMessagesConsumers = new HashSet<>();
                        this.tradeDataSubs.put(instrument, instrumentMessagesConsumers);
                    } else {
                        instrumentMessagesConsumers = this.tradeDataSubs.get(instrument);
                    }
                    boolean isAddedTrade = instrumentMessagesConsumers.add(consumer);
                    if (isAddedTrade && instrument != null) {
                        Iterator<DataFeedMessageConsumer> it = this.tradeDataSubs.get(instrument).iterator();
                        while (it.hasNext()) {
                            DataFeedMessageConsumer messageConsumer = it.next();
                            if (this.tradeData.get(instrument) != null) {
                                Iterator<TradeData> it2 = this.tradeData.get(instrument).iterator();
                                while (it2.hasNext()) {
                                    TradeData td = it2.next();
                                    messageConsumer.Handle(td);
                                }
                            }
                        }
                    }
                }
                return;
            default:
                this.logger.debug("Ошибка, невозможно подписаться на инструмент {}, тип подписки - {}", instrument, type);
                return;
        }
    }

    public void Unsubscribe(Instrument instrument, DataFeedMessageConsumer consumer, DataFeedEventType type) {
        switch (AnonymousClass1.$SwitchMap$ru$effectivegroup$client$algoil$marketdata$DataFeed$DataFeedEventType[type.ordinal()]) {
            case 1:
                synchronized (this.syncRoot) {
                    if (!this.newInstrumentSubs.contains(consumer)) {
                        this.logger.info("Невозможно отписаться от инструмента {}, так как получатель {} не был подписан на изменения типа {}", instrument.getCode(), consumer.toString(), type);
                    } else {
                        this.newInstrumentSubs.remove(consumer);
                    }
                }
                return;
            case 2:
                synchronized (this.syncRoot) {
                    if (!this.instrumentDataSubs.get(instrument).contains(consumer)) {
                        this.logger.info("Невозможно отписаться от инструмента {}, так как получатель {} не был подписан на изменения типа {}", instrument.getCode(), consumer.toString(), type);
                    } else {
                        this.instrumentDataSubs.get(instrument).remove(consumer);
                    }
                }
                return;
            case CipherHolder.CIPHER_PARAM_ENCRYPTED_SECRET_KEY /* 3 */:
                synchronized (this.syncRoot) {
                    if (!this.quotationSubs.get(instrument).contains(consumer)) {
                        this.logger.info("Невозможно отписаться от инструмента {}, так как получатель {} не был подписан на изменения типа {}", instrument.getCode(), consumer.toString(), type);
                    } else {
                        this.quotationSubs.get(instrument).remove(consumer);
                    }
                }
                return;
            default:
                this.logger.info("Неподдерживаемый тип подписки {}", type);
                return;
        }
    }

    public List<Instrument> getInstruments() {
        ArrayList arrayList;
        synchronized (this.syncRoot) {
            if (this.instruments.size() == 0) {
                List<SecurityData> fromDb = Context.repositoryContext.get(SecurityData.class).getData();
                this.logger.info("Загружено {} инструментов из БД, SecurityData", Integer.valueOf(fromDb.size()));
                for (SecurityData securityData : fromDb) {
                    if (securityData.isVisibleInMainTrading) {
                        Instrument instrument = new Instrument(securityData.getCode(), securityData.getFullName());
                        this.instrumentsData.put(instrument, securityData);
                        this.instruments.add(instrument);
                    }
                }
            }
            arrayList = new ArrayList(this.instruments);
        }
        return arrayList;
    }

    public HashMap<Instrument, HashSet<TradeData>> getTradeData() {
        HashMap<Instrument, HashSet<TradeData>> hashMap;
        synchronized (this.syncRoot) {
            if (this.tradeData.size() == 0) {
                List<TradeData> fromDb = Context.repositoryContext.get(TradeData.class).getData();
                this.logger.info("Загружено {} инструментов из БД, TradeData", Integer.valueOf(fromDb.size()));
                for (TradeData td : fromDb) {
                    Instrument ins = (Instrument) this.instruments.stream().filter(c -> {
                        return c.getCode().equals(td.getSecurityCode());
                    }).toList().get(0);
                    this.tradeData.computeIfAbsent(ins, k -> {
                        return new HashSet();
                    });
                    this.tradeData.get(ins).add(td);
                }
            }
            hashMap = new HashMap<>(this.tradeData);
        }
        return hashMap;
    }

    @Override // ru.effectivegroup.client.algoil.adapter.DefaultSTEMessageConsumer, ru.effectivegroup.client.algoil.adapter.STEMessageConsumer, ru.effectivegroup.client.algoil.marketdata.DataFeedMessageConsumer
    public void Handle(SecurityData message) {
        Instrument instrument = new Instrument(message.getCode(), message.getFullName());
        HashSet<DataFeedMessageConsumer> subs = null;
        notifyNewInstruments(message, instrument);
        synchronized (this.syncRoot) {
            if (message.isVisibleInMainTrading) {
                this.instrumentsData.put(instrument, message);
                this.logger.debug("Поступила SecurityData. Код инструмента - {}, статус - {}", message.getCode(), message.getSessionStatus());
                if (this.instrumentDataSubs.containsKey(instrument)) {
                    subs = new HashSet<>(this.instrumentDataSubs.get(instrument));
                }
            }
        }
        if (subs != null && subs.size() > 0) {
            Iterator<DataFeedMessageConsumer> it = subs.iterator();
            while (it.hasNext()) {
                DataFeedMessageConsumer cons = it.next();
                cons.Handle(message);
            }
        }
        if (this.tradeDataBuffer.size() != 0) {
            List<TradeData> tempTradeDataList = this.tradeDataBuffer.stream().filter(data -> {
                return data.getSecurityCode().equals(instrument.getCode());
            }).toList();
            this.tradeDataBuffer.removeAll(tempTradeDataList);
            for (TradeData data2 : tempTradeDataList) {
                if (data2.getSecurityCode().equals(instrument.getCode())) {
                    Handle(data2);
                }
            }
        }
        if (this.orderDataBuffer.size() != 0) {
            List<OrderData> tempOrderDataList = this.orderDataBuffer.stream().filter(data3 -> {
                return data3.getSecurityCode().equals(instrument.getCode());
            }).toList();
            this.orderDataBuffer.removeAll(tempOrderDataList);
            for (OrderData data4 : tempOrderDataList) {
                if (data4.getSecurityCode().equals(instrument.getCode())) {
                    Handle(data4);
                }
            }
        }
    }

    @Override // ru.effectivegroup.client.algoil.adapter.DefaultSTEMessageConsumer, ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(OrderData message) {
        OrderBookBuilder orderBookBuilder;
        if (message.getSecurity() == null) {
            this.orderDataBuffer.add(message);
            return;
        }
        Instrument instrument = new Instrument(message.getSecurityCode(), message.getSecurity().getFullName());
        this.orderCounter.Handle(instrument, message);
        synchronized (this.syncRoot) {
            if (!this.orderBookBuilders.containsKey(instrument)) {
                orderBookBuilder = new OrderBookBuilder(instrument);
                this.orderBookBuilders.put(instrument, orderBookBuilder);
                this.logger.debug("Создан OrderBookBuilder для инструмента {}", instrument.getCode());
            } else {
                orderBookBuilder = this.orderBookBuilders.get(instrument);
            }
        }
        OrderBook orderBook = orderBookBuilder.Handle(message);
        if (orderBook == null) {
            return;
        }
        this.logger.info("Обновлён стакан {} бидов {} асков {} (price {} qty {} qtyLeft {} status {} orderId {} acc {})", instrument.getCode(), Integer.valueOf(orderBook.getBids().size()), Integer.valueOf(orderBook.getAsks().size()), Long.valueOf(message.getPrice()), Long.valueOf(message.getQty()), Integer.valueOf(message.getQtyLeft()), message.getStatus(), message.getCode(), message.cashAccountCode);
        HashSet<DataFeedMessageConsumer> subs = null;
        synchronized (this.syncRoot) {
            if (this.instrumentDataSubs.containsKey(instrument)) {
                subs = new HashSet<>(this.instrumentDataSubs.get(instrument));
            }
            this.orderBooks.put(instrument, orderBook);
        }
        if (subs != null && subs.size() > 0) {
            Iterator<DataFeedMessageConsumer> it = subs.iterator();
            while (it.hasNext()) {
                DataFeedMessageConsumer cons = it.next();
                cons.Handle(orderBook);
                cons.Handle(message);
            }
        }
    }

    @Override // ru.effectivegroup.client.algoil.adapter.DefaultSTEMessageConsumer, ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(TradeData message) {
        if (this.instruments.size() == 0 || this.instruments.stream().noneMatch(i -> {
            return i.getCode().equals(message.securityCode);
        })) {
            this.tradeDataBuffer.add(message);
            return;
        }
        Instrument instrument = new Instrument();
        Iterator<Instrument> it = this.instruments.iterator();
        while (it.hasNext()) {
            Instrument i2 = it.next();
            if (message.getSecurityCode().equals(i2.getCode())) {
                instrument = new Instrument(i2.getCode(), i2.getFullName());
            }
        }
        HashSet<DataFeedMessageConsumer> subs = null;
        synchronized (this.syncRoot) {
            this.tradeData.computeIfAbsent(instrument, k -> {
                return new HashSet();
            });
            this.tradeData.get(instrument).add(message);
            if (this.tradeDataSubs.containsKey(instrument)) {
                subs = new HashSet<>(this.tradeDataSubs.get(instrument));
            }
        }
        if (subs != null && subs.size() > 0) {
            Iterator<DataFeedMessageConsumer> it2 = subs.iterator();
            while (it2.hasNext()) {
                DataFeedMessageConsumer cons = it2.next();
                cons.Handle(message);
            }
        }
    }

    public SecurityData getInstrumentData(Instrument instrument) {
        SecurityData securityData;
        synchronized (this.syncRoot) {
            securityData = this.instrumentsData.get(instrument);
        }
        return securityData;
    }

    public OrderBook getOrderBook(Instrument instrument) {
        synchronized (this.syncRoot) {
            if (!this.orderBooks.containsKey(instrument)) {
                return null;
            }
            return this.orderBooks.get(instrument);
        }
    }

    private void notifyNewInstruments(SecurityData message, Instrument instrument) {
        boolean newInstrumentReceived;
        if (!message.isVisibleInMainTrading) {
            return;
        }
        synchronized (this.syncRoot) {
            newInstrumentReceived = this.instruments.add(instrument);
        }
        if (newInstrumentReceived) {
            HashSet<DataFeedMessageConsumer> newInstrumentConsumers = null;
            synchronized (this.syncRoot) {
                if (this.newInstrumentSubs.size() > 0) {
                    newInstrumentConsumers = new HashSet<>(this.newInstrumentSubs);
                }
            }
            if (newInstrumentConsumers != null && newInstrumentConsumers.size() > 0) {
                Iterator<DataFeedMessageConsumer> it = newInstrumentConsumers.iterator();
                while (it.hasNext()) {
                    DataFeedMessageConsumer mc = it.next();
                    if (message.isVisibleInMainTrading) {
                        mc.Handle(instrument);
                    }
                }
            }
        }
    }
}
