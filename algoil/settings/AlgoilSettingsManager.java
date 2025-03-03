package ru.effectivegroup.client.algoil.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.effectivegroup.client.crypto.cipher.CipherHolder;
import ru.effectivegroup.client.utils.OSDetector;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/settings/AlgoilSettingsManager.class */
public class AlgoilSettingsManager implements SettingsManager {
    private static final Logger logger = LogManager.getLogger("algoil");
    private AlgoilSettings settings;
    private static String filePath;

    public AlgoilSettingsManager() {
        filePath = Paths.get(getSettingDirectory(), "settings.json").toString();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            this.settings = (AlgoilSettings) objectMapper.readValue(new File(filePath), AlgoilSettings.class);
            if (this.settings.getBots().isEmpty()) {
                this.settings.setBots(AlgoilSettings.emptySettings().getBots());
            }
            if (this.settings.getBotPaneSettings() == null) {
                this.settings.setBotPaneSettings(AlgoilSettings.emptySettings().getBotPaneSettings());
            }
            if (this.settings.getTransactionQueueSettings() == null) {
                this.settings.setTransactionQueueSettings(AlgoilSettings.emptySettings().getTransactionQueueSettings());
            }
            if (this.settings.isBotListSortByCode() == null) {
                this.settings.setBotListSortByCode(AlgoilSettings.emptySettings().isBotListSortByCode());
            }
        } catch (Exception e) {
            logger.error("Ошибка чтения настроек", e);
            this.settings = AlgoilSettings.emptySettings();
        }
    }

    @Override // ru.effectivegroup.client.algoil.settings.SettingsManager
    public String getSettingDirectory() {
        String var1 = OSDetector.getPlatformUserDirectory();
        String var12 = var1.substring(0, var1.lastIndexOf(47)) + "/Algoil";
        File directory = new File(var12);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return var12;
    }

    @Override // ru.effectivegroup.client.algoil.settings.SettingsManager
    public void SaveSettings(Object settingsSection) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String filePath2 = Paths.get(getSettingDirectory(), "settings.json").toString();
            if (settingsSection instanceof BotPaneSettings) {
                this.settings.setBotPaneSettings((BotPaneSettings) settingsSection);
                logger.info("Сохранение настроек BotPane");
            } else if (settingsSection instanceof TransactionQueueSettings) {
                this.settings.setTransactionQueueSettings((TransactionQueueSettings) settingsSection);
                logger.info("Сохранение настроек очереди транзакций");
            } else if (settingsSection instanceof Boolean) {
                this.settings.setBotListSortByCode((Boolean) settingsSection);
                logger.info("Сохранение настроек сортировки листа ботов");
            } else {
                this.settings.setBots((List) settingsSection);
                logger.info("Сохранение настроек ботов");
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath2), this.settings);
        } catch (IOException e) {
            logger.error("Ошибка сохранения настроек", e);
        }
    }

    @Override // ru.effectivegroup.client.algoil.settings.SettingsManager
    public Object GetSettings(Class T) {
        String simpleName = T.getSimpleName();
        boolean z = -1;
        switch (simpleName.hashCode()) {
            case -341489658:
                if (simpleName.equals("BotManager")) {
                    z = false;
                    break;
                }
                break;
            case 1086333019:
                if (simpleName.equals("BotPaneSettingsManager")) {
                    z = true;
                    break;
                }
                break;
            case 1352840083:
                if (simpleName.equals("BotsListControl")) {
                    z = 2;
                    break;
                }
                break;
            case 1773482550:
                if (simpleName.equals("TransactionQueueSettings")) {
                    z = 3;
                    break;
                }
                break;
        }
        switch (z) {
            case false:
                return this.settings.getBots();
            case true:
                return this.settings.getBotPaneSettings();
            case true:
                return this.settings.isBotListSortByCode();
            case CipherHolder.CIPHER_PARAM_ENCRYPTED_SECRET_KEY /* 3 */:
                return this.settings.getTransactionQueueSettings();
            default:
                return null;
        }
    }
}
