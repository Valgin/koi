package ru.effectivegroup.client.algoil;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import ru.effectivegroup.client.context.Context;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/ExchangeTimeProvider.class */
public class ExchangeTimeProvider {
    private static final long DELAY_DEFAULT = 1;
    private final Object syncRoot = new Object();
    private final HashSet<TimeConsumer> timeConsumers = new HashSet<>();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final Runnable notificationDefault = () -> {
        notifyTimeConsumers(getTime());
    };
    private ScheduledFuture<?> futureTask = this.executorService.scheduleWithFixedDelay(this.notificationDefault, 0, DELAY_DEFAULT, TimeUnit.MILLISECONDS);

    /* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/ExchangeTimeProvider$TimeConsumer.class */
    public interface TimeConsumer {
        void HandleTime(LocalTime localTime);
    }

    private void notifyTimeConsumers(LocalTime time) {
        synchronized (this.syncRoot) {
            Iterator<TimeConsumer> it = this.timeConsumers.iterator();
            while (it.hasNext()) {
                TimeConsumer cons = it.next();
                cons.HandleTime(time);
            }
        }
    }

    public static ZonedDateTime getZonedTime() {
        return Context.serviceContext.timeService.getTradeSystemTime();
    }

    public void Subscribe(TimeConsumer consumer) {
        synchronized (this.syncRoot) {
            this.timeConsumers.add(consumer);
        }
    }

    public void Unsubscribe(TimeConsumer consumer) {
        synchronized (this.syncRoot) {
            this.timeConsumers.remove(consumer);
        }
    }

    public static LocalTime getTime() {
        LocalTime result = LocalTime.now();
        if (getZonedTime() != null) {
            result = getZonedTime().toLocalTime();
        }
        return result;
    }

    public void Stop() {
        this.executorService.shutdown();
        this.futureTask.cancel(false);
    }
}
