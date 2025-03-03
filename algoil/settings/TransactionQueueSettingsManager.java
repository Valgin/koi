package ru.effectivegroup.client.algoil.settings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.effectivegroup.client.crypto.cipher.CipherHolder;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/settings/TransactionQueueSettingsManager.class */
public class TransactionQueueSettingsManager {
    private static final Logger logger = LogManager.getLogger("algoil");
    private TransactionQueueSettings transactionQueueSettings;
    private SettingsManager settingsManager;

    public TransactionQueueSettingsManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        TransactionQueueSettings settings = (TransactionQueueSettings) settingsManager.GetSettings(TransactionQueueSettings.class);
        if (settings == null) {
            return;
        }
        this.transactionQueueSettings = settings;
        logger.info("Настройки TransactionQueueSettings восстановлены");
    }

    public TransactionQueueSettings getTransactionQueueSettings() {
        return this.transactionQueueSettings;
    }

    public void saveSettings(String property, int value) {
        boolean z = -1;
        switch (property.hashCode()) {
            case -1660487439:
                if (property.equals("transactionQueue1000Delay")) {
                    z = 2;
                    break;
                }
                break;
            case -1300038957:
                if (property.equals("transactionQueue1000Size")) {
                    z = 3;
                    break;
                }
                break;
            case -1281030171:
                if (property.equals("transactionQueue100Delay")) {
                    z = false;
                    break;
                }
                break;
            case 1898790239:
                if (property.equals("transactionQueue100Size")) {
                    z = true;
                    break;
                }
                break;
        }
        switch (z) {
            case false:
                this.transactionQueueSettings.setTransactionQueue100Delay(value);
                logger.info("Обновлены настройки. Задержка очереди \"100 мс\" изменена на {}", Integer.valueOf(value));
                break;
            case true:
                this.transactionQueueSettings.setTransactionQueue100Size(value);
                logger.info("Обновлены настройки. Размер очереди \"100 мс\" изменен на {}", Integer.valueOf(value));
                break;
            case true:
                this.transactionQueueSettings.setTransactionQueue1000Delay(value);
                logger.info("Обновлены настройки. Задержка очереди \"1000 мс\" изменена на {}", Integer.valueOf(value));
                break;
            case CipherHolder.CIPHER_PARAM_ENCRYPTED_SECRET_KEY /* 3 */:
                this.transactionQueueSettings.setTransactionQueue1000Size(value);
                logger.info("Обновлены настройки. Размер очереди \"1000 мс\" изменен на {}", Integer.valueOf(value));
                break;
            default:
                logger.info("Неизвестный параметр настройки BotPane {}", property);
                break;
        }
        this.settingsManager.SaveSettings(this.transactionQueueSettings);
    }
}
