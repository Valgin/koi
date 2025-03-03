package ru.effectivegroup.client.algoil.settings;

import java.util.Collections;
import java.util.List;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/settings/AlgoilSettings.class */
public class AlgoilSettings {
    private List<BotSettings> bots;
    private BotPaneSettings botPaneSettings;
    private TransactionQueueSettings transactionQueueSettings;
    private Boolean isBotListSortByCode;

    public TransactionQueueSettings getTransactionQueueSettings() {
        return this.transactionQueueSettings;
    }

    public void setTransactionQueueSettings(TransactionQueueSettings transactionQueueSettings) {
        this.transactionQueueSettings = transactionQueueSettings;
    }

    public BotPaneSettings getBotPaneSettings() {
        return this.botPaneSettings;
    }

    public void setBotPaneSettings(BotPaneSettings botPaneSettings) {
        this.botPaneSettings = botPaneSettings;
    }

    public List<BotSettings> getBots() {
        return this.bots;
    }

    public void setBots(List<BotSettings> bots) {
        this.bots = bots;
    }

    public Boolean isBotListSortByCode() {
        return this.isBotListSortByCode;
    }

    public void setBotListSortByCode(Boolean botListSortByCode) {
        this.isBotListSortByCode = botListSortByCode;
    }

    public static AlgoilSettings emptySettings() {
        AlgoilSettings emptySettings = new AlgoilSettings();
        emptySettings.setBotPaneSettings(BotPaneSettings.defaultSettings());
        emptySettings.setTransactionQueueSettings(TransactionQueueSettings.defaultSettings());
        emptySettings.setBots(Collections.emptyList());
        emptySettings.setBotListSortByCode(false);
        return emptySettings;
    }
}
