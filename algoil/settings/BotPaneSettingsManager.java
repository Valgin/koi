package ru.effectivegroup.client.algoil.settings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.effectivegroup.client.crypto.cipher.CipherHolder;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/settings/BotPaneSettingsManager.class */
public class BotPaneSettingsManager {
    private static final Logger logger = LogManager.getLogger("algoil");
    private BotPaneSettings botPaneSettings;
    private SettingsManager settingsManager;

    public BotPaneSettingsManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        BotPaneSettings settings = (BotPaneSettings) settingsManager.GetSettings(BotPaneSettingsManager.class);
        if (settings == null) {
            return;
        }
        this.botPaneSettings = settings;
        logger.info("Настройки BotPane восстановлены");
    }

    public BotPaneSettings getBotPaneSettings() {
        return this.botPaneSettings;
    }

    public void saveSettings(String property, boolean value) {
        boolean z = -1;
        switch (property.hashCode()) {
            case -1752650617:
                if (property.equals("orderControlShowSelected")) {
                    z = false;
                    break;
                }
                break;
            case -1159793880:
                if (property.equals("tradeControlOwnSelected")) {
                    z = 3;
                    break;
                }
                break;
            case -956652910:
                if (property.equals("orderControlOwnSelected")) {
                    z = true;
                    break;
                }
                break;
            case 1018252605:
                if (property.equals("orderControlQueuedSelected")) {
                    z = 2;
                    break;
                }
                break;
        }
        switch (z) {
            case false:
                this.botPaneSettings.setOrderControlShowSelected(value);
                logger.info("Обновлены настройки. Выбор \"Показывать таблицу заявок\" изменен на {}", Boolean.valueOf(value));
                break;
            case true:
                this.botPaneSettings.setOrderControlOwnSelected(value);
                logger.info("Обновлены настройки. Выбор \"Только свои\" таблицы заявок изменен на {}", Boolean.valueOf(value));
                break;
            case true:
                this.botPaneSettings.setOrderControlQueuedSelected(value);
                logger.info("Обновлены настройки. Выбор \"Только активные\" таблицы заявок изменен на {}", Boolean.valueOf(value));
                break;
            case CipherHolder.CIPHER_PARAM_ENCRYPTED_SECRET_KEY /* 3 */:
                this.botPaneSettings.setTradeControlOwnSelected(value);
                logger.info("Обновлены настройки. Выбор \"Только свои\" таблицы сделок изменен на {}", Boolean.valueOf(value));
                break;
            default:
                logger.info("Неизвестный параметр настройки BotPane {}", property);
                break;
        }
        this.settingsManager.SaveSettings(this.botPaneSettings);
    }
}
