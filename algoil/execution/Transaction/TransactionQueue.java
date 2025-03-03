package ru.effectivegroup.client.algoil.execution.Transaction;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.effectivegroup.client.algoil.ExchangeTimeProvider;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/execution/Transaction/TransactionQueue.class */
public class TransactionQueue {
    private long maxTimeDifference;
    private int queueSize;
    private static final Object lock = new Object();
    private final Logger logger = LogManager.getLogger("algoil");
    private final LinkedBlockingQueue<LocalTime> timeQueue = new LinkedBlockingQueue<>();

    public TransactionQueue(long maxTimeDiff, int queueSize) {
        this.maxTimeDifference = maxTimeDiff;
        this.queueSize = queueSize;
    }

    public void addPlaceOrderAction() {
        LocalTime currentTime = ExchangeTimeProvider.getTime();
        synchronized (lock) {
            if (this.timeQueue.size() == this.queueSize) {
                LocalTime latestTime = this.timeQueue.poll();
                long diff = this.maxTimeDifference - latestTime.until(currentTime, ChronoUnit.MILLIS);
                if (diff > 0) {
                    try {
                        this.logger.info(String.format("Очередь транзакций (%s в %sмс): ожидание %s мс", Integer.valueOf(this.queueSize), Long.valueOf(this.maxTimeDifference), Long.valueOf(diff)));
                        Thread.sleep(diff);
                    } catch (InterruptedException e) {
                    }
                }
            }
            this.timeQueue.add(ExchangeTimeProvider.getTime());
        }
    }

    public void setMaxTimeDifference(long maxTimeDifference) {
        this.maxTimeDifference = maxTimeDifference;
    }

    public void setQueueSize(int queueSize) {
        synchronized (lock) {
            while (this.timeQueue.size() > queueSize) {
                this.timeQueue.poll();
            }
        }
        this.queueSize = queueSize;
    }
}
