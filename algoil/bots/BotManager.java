package ru.effectivegroup.client.algoil.bots;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.effectivegroup.client.algoil.ExchangeTimeProvider;
import ru.effectivegroup.client.algoil.Instrument;
import ru.effectivegroup.client.algoil.execution.CancelOrderResult;
import ru.effectivegroup.client.algoil.execution.ExecutionManager;
import ru.effectivegroup.client.algoil.execution.Order;
import ru.effectivegroup.client.algoil.execution.OrderEmitter;
import ru.effectivegroup.client.algoil.execution.PlaceOrderResult;
import ru.effectivegroup.client.algoil.marketdata.DataFeed;
import ru.effectivegroup.client.algoil.settings.BotSettings;
import ru.effectivegroup.client.algoil.settings.SettingsManager;
import ru.effectivegroup.client.model.data.ste.dictionary.BSType;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/BotManager.class */
public class BotManager implements BotContext {
    private static final Logger logger = LogManager.getLogger("algoil");
    private SettingsManager settingsManager;
    private DataFeed dataFeed;
    private ExecutionManager executionManager;
    private ExchangeTimeProvider timeProvider;
    private final Object syncRoot = new Object();
    private final List<Bot> bots = new ArrayList();
    private final HashSet<BotManagerEventsConsumer> subscribers = new HashSet<>();
    private final LocalTime before1045 = LocalTime.of(10, 44, 59);
    private final LocalTime after1045 = LocalTime.of(10, 45, 1);
    private final LocalTime localTime1045 = LocalTime.of(10, 45);

    public BotManager(SettingsManager settingsManager, DataFeed dataFeed, ExecutionManager executionManager, ExchangeTimeProvider timeProvider) {
        this.settingsManager = settingsManager;
        this.dataFeed = dataFeed;
        this.executionManager = executionManager;
        this.timeProvider = timeProvider;
        List<BotSettings> botsSettings = (List) settingsManager.GetSettings(BotManager.class);
        if (botsSettings.size() == 0) {
            return;
        }
        logger.info("Восстановление {} ботов из настроек", Integer.valueOf(botsSettings.size()));
        synchronized (this.syncRoot) {
            for (BotSettings botSettings : botsSettings) {
                this.bots.add(restoreBot(botSettings));
            }
        }
        for (Bot bot : this.bots) {
            this.dataFeed.Subscribe(bot.getInstrument(), bot, DataFeed.DataFeedEventType.InstrumentData);
            this.dataFeed.Subscribe(bot.getInstrument(), bot, DataFeed.DataFeedEventType.TradeData);
            executionManager.Subscribe(bot);
        }
    }

    public void AddBot(Instrument instrument, String cashAccount, String commAccount) {
        Bot newBot;
        logger.info("Добавление бота для инструмента {}", instrument.getCode());
        List<BotSettings> botSettings = new ArrayList<>();
        synchronized (this.syncRoot) {
            newBot = new Bot(this, instrument, cashAccount, commAccount);
            this.timeProvider.Subscribe(newBot);
            this.bots.add(newBot);
            for (Bot bot : this.bots) {
                botSettings.add(bot.getSettings());
            }
        }
        this.settingsManager.SaveSettings(botSettings);
        this.dataFeed.Subscribe(instrument, newBot, DataFeed.DataFeedEventType.InstrumentData);
        this.dataFeed.Subscribe(instrument, newBot, DataFeed.DataFeedEventType.TradeData);
        this.executionManager.Subscribe(newBot);
        raiseNewBotEvent(newBot);
    }

    public void DeleteBot(Bot botToDelete) {
        boolean removed;
        logger.info("Удаление бота для инструмента {}", botToDelete.getInstrument().getCode());
        List<BotSettings> botsSettings = null;
        synchronized (this.syncRoot) {
            removed = this.bots.remove(botToDelete);
            if (removed) {
                botsSettings = new ArrayList<>();
                for (Bot bot : this.bots) {
                    botsSettings.add(bot.getSettings());
                }
            }
        }
        if (removed) {
            this.timeProvider.Unsubscribe(botToDelete);
            raiseBotDeletedEvent(botToDelete);
            this.settingsManager.SaveSettings(botsSettings);
        }
    }

    public boolean ContainsBotListByInstrument(Instrument instrument) {
        return getBots().stream().map((v0) -> {
            return v0.getInstrument();
        }).toList().contains(instrument);
    }

    public ArrayList<Bot> getBots() {
        ArrayList<Bot> arrayList;
        synchronized (this.syncRoot) {
            arrayList = new ArrayList<>(this.bots);
        }
        return arrayList;
    }

    public void SaveBotSettings(Bot bot) {
        List<BotSettings> botsSettings = (List) this.settingsManager.GetSettings(BotManager.class);
        botsSettings.removeIf(s -> {
            if (s.getInstrument().getCode() == bot.getInstrument().getCode()) {
                return true;
            }
            return false;
        });
        botsSettings.add(bot.getSettings());
        this.settingsManager.SaveSettings(botsSettings);
        raiseBotSettingsChanged(bot);
    }

    public void Subscribe(BotManagerEventsConsumer subscriber) {
        synchronized (this.subscribers) {
            this.subscribers.add(subscriber);
        }
    }

    private Bot restoreBot(BotSettings botSettings) {
        Bot bot = new Bot(this, botSettings);
        this.timeProvider.Subscribe(bot);
        logger.info("Восстановлен бот {}", bot.getInstrument().getCode());
        return bot;
    }

    private void raiseNewBotEvent(Bot newBot) {
        HashSet<BotManagerEventsConsumer> subs;
        synchronized (this.subscribers) {
            subs = new HashSet<>(this.subscribers);
        }
        Iterator<BotManagerEventsConsumer> it = subs.iterator();
        while (it.hasNext()) {
            BotManagerEventsConsumer consumer = it.next();
            consumer.HandleNewBot(newBot);
        }
    }

    private void raiseBotDeletedEvent(Bot newBot) {
        HashSet<BotManagerEventsConsumer> subs;
        synchronized (this.subscribers) {
            subs = new HashSet<>(this.subscribers);
        }
        Iterator<BotManagerEventsConsumer> it = subs.iterator();
        while (it.hasNext()) {
            BotManagerEventsConsumer consumer = it.next();
            consumer.HandleBotDeleted(newBot);
        }
    }

    private void raiseBotSettingsChanged(Bot bot) {
        HashSet<BotManagerEventsConsumer> subs;
        synchronized (this.subscribers) {
            subs = new HashSet<>(this.subscribers);
        }
        Iterator<BotManagerEventsConsumer> it = subs.iterator();
        while (it.hasNext()) {
            BotManagerEventsConsumer consumer = it.next();
            consumer.HandleBotSettingsChanged(bot);
        }
    }

    @Override // ru.effectivegroup.client.algoil.bots.BotContext
    public PlaceOrderResult PlaceOrder(String securityCode, BSType operation, long quantity, long price, String moneyAccount, String commAccount, String firmCode, String userCode, String clientCode, OrderEmitter emitter) {
        return this.executionManager.PlaceOrder(securityCode, operation, quantity, price, moneyAccount, commAccount, emitter);
    }

    @Override // ru.effectivegroup.client.algoil.bots.BotContext
    public CancelOrderResult CancelOrder(Order order) {
        return this.executionManager.CancelOrder(order);
    }

    @Override // ru.effectivegroup.client.algoil.bots.BotContext
    public boolean timeIsBefore94ErrorProcessingTime(LocalTime errorProcessingTime) {
        ExchangeTimeProvider exchangeTimeProvider = this.timeProvider;
        LocalTime now = ExchangeTimeProvider.getTime();
        return now.isBefore(errorProcessingTime);
    }

    @Override // ru.effectivegroup.client.algoil.bots.BotContext
    public boolean timeIsBefore1045(Long timeSpread) {
        ExchangeTimeProvider exchangeTimeProvider = this.timeProvider;
        return ExchangeTimeProvider.getTime().isBefore(this.localTime1045.minus(timeSpread.longValue(), (TemporalUnit) ChronoUnit.MILLIS));
    }

    @Override // ru.effectivegroup.client.algoil.bots.BotContext
    public boolean timeIsBeforeStartTime(LocalTime startTime, Long timeSpread) {
        ExchangeTimeProvider exchangeTimeProvider = this.timeProvider;
        LocalTime now = ExchangeTimeProvider.getTime();
        if (timeSpread == null) {
            timeSpread = 1000L;
        }
        LocalTime startTimeWithTimeSpread = startTime.minus(timeSpread.longValue(), (TemporalUnit) ChronoUnit.MILLIS);
        return now.isBefore(startTimeWithTimeSpread);
    }

    @Override // ru.effectivegroup.client.algoil.bots.BotContext
    public boolean timeIsAfter1045() {
        ExchangeTimeProvider exchangeTimeProvider = this.timeProvider;
        LocalTime now = ExchangeTimeProvider.getTime();
        return now.isAfter(this.after1045);
    }

    public boolean HasRunningBots() {
        boolean anyMatch;
        synchronized (this.syncRoot) {
            anyMatch = this.bots.stream().anyMatch((v0) -> {
                return v0.isRunning();
            });
        }
        return anyMatch;
    }

    public void StopProcessingAll() {
        synchronized (this.syncRoot) {
            for (Bot bot : this.bots) {
                bot.StopProcessing();
            }
        }
    }
}
