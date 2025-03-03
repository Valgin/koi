package ru.effectivegroup.client.algoil.execution.Transaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.effectivegroup.client.algoil.settings.TransactionQueueSettings;
import ru.effectivegroup.client.algoil.settings.TransactionQueueSettingsManager;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/execution/Transaction/TransactionManager.class */
public class TransactionManager {
    private final TransactionQueue transactionQueue100ms = new TransactionQueue(110, 2);
    private final TransactionQueue transactionQueue1000ms = new TransactionQueue(1100, 6);
    private static TransactionManager instance;
    private static final Logger logger = LogManager.getLogger("algoil");
    private static final Object lock = new Object();

    private TransactionManager() {
    }

    public static TransactionManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new TransactionManager();
                }
            }
        }
        return instance;
    }

    public void addPlaceOrderAction() {
        this.transactionQueue100ms.addPlaceOrderAction();
        this.transactionQueue1000ms.addPlaceOrderAction();
    }

    public void setTransactionQueue100Delay(int delay) {
        this.transactionQueue100ms.setMaxTimeDifference(delay);
    }

    public void setTransactionQueue1000Delay(int delay) {
        this.transactionQueue1000ms.setMaxTimeDifference(delay);
    }

    public void setTransactionQueue100Size(int size) {
        this.transactionQueue100ms.setQueueSize(size);
    }

    public void setTransactionQueue1000Size(int size) {
        this.transactionQueue1000ms.setQueueSize(size);
    }

    public void restoreSettings(TransactionQueueSettingsManager settingsManager) {
        try {
            logger.info("Восстановление настроек очедерей транзакций");
            TransactionQueueSettings settings = settingsManager.getTransactionQueueSettings();
            setTransactionQueue100Delay(settings.getTransactionQueue100Delay());
            setTransactionQueue1000Delay(settings.getTransactionQueue1000Delay());
            setTransactionQueue100Size(settings.getTransactionQueue100Size());
            setTransactionQueue1000Size(settings.getTransactionQueue1000Size());
        } catch (Exception e) {
            logger.info("Ошибка применения настроек очедерей транзакций");
        }
    }
}
