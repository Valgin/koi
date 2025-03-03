package ru.effectivegroup.client.algoil.settings;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/settings/BotPaneSettings.class */
public class BotPaneSettings {
    private boolean orderControlShowSelected;
    private boolean orderControlOwnSelected;
    private boolean orderControlQueuedSelected;
    private boolean tradeControlOwnSelected;

    public BotPaneSettings() {
    }

    public BotPaneSettings(boolean orderControlShowCheckBox, boolean orderControlOwnCheckBox, boolean orderControlQueuedCheckBox, boolean tradeControlOwnCheckBox) {
        this.orderControlShowSelected = orderControlShowCheckBox;
        this.orderControlOwnSelected = orderControlOwnCheckBox;
        this.orderControlQueuedSelected = orderControlQueuedCheckBox;
        this.tradeControlOwnSelected = tradeControlOwnCheckBox;
    }

    public boolean isOrderControlShowSelected() {
        return this.orderControlShowSelected;
    }

    public void setOrderControlShowSelected(boolean orderControlShowSelected) {
        this.orderControlShowSelected = orderControlShowSelected;
    }

    public boolean isOrderControlOwnSelected() {
        return this.orderControlOwnSelected;
    }

    public void setOrderControlOwnSelected(boolean orderControlOwnSelected) {
        this.orderControlOwnSelected = orderControlOwnSelected;
    }

    public boolean isOrderControlQueuedSelected() {
        return this.orderControlQueuedSelected;
    }

    public void setOrderControlQueuedSelected(boolean orderControlQueuedSelected) {
        this.orderControlQueuedSelected = orderControlQueuedSelected;
    }

    public boolean isTradeControlOwnSelected() {
        return this.tradeControlOwnSelected;
    }

    public void setTradeControlOwnSelected(boolean tradeControlOwnSelected) {
        this.tradeControlOwnSelected = tradeControlOwnSelected;
    }

    public static BotPaneSettings defaultSettings() {
        return new BotPaneSettings(true, false, false, false);
    }
}
