package ru.effectivegroup.client.algoil.settings;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/settings/TransactionQueueSettings.class */
public class TransactionQueueSettings {
    private int transactionQueue100Delay;
    private int transactionQueue100Size;
    private int transactionQueue1000Delay;
    private int transactionQueue1000Size;

    public TransactionQueueSettings() {
    }

    public TransactionQueueSettings(int transactionQueue100Delay, int transactionQueue100Size, int transactionQueue1000Delay, int transactionQueue1000Size) {
        this.transactionQueue100Delay = transactionQueue100Delay;
        this.transactionQueue100Size = transactionQueue100Size;
        this.transactionQueue1000Delay = transactionQueue1000Delay;
        this.transactionQueue1000Size = transactionQueue1000Size;
    }

    public int getTransactionQueue100Delay() {
        return this.transactionQueue100Delay;
    }

    public void setTransactionQueue100Delay(int transactionQueue100Delay) {
        this.transactionQueue100Delay = transactionQueue100Delay;
    }

    public int getTransactionQueue100Size() {
        return this.transactionQueue100Size;
    }

    public void setTransactionQueue100Size(int transactionQueue100Size) {
        this.transactionQueue100Size = transactionQueue100Size;
    }

    public int getTransactionQueue1000Delay() {
        return this.transactionQueue1000Delay;
    }

    public void setTransactionQueue1000Delay(int transactionQueue1000Delay) {
        this.transactionQueue1000Delay = transactionQueue1000Delay;
    }

    public int getTransactionQueue1000Size() {
        return this.transactionQueue1000Size;
    }

    public void setTransactionQueue1000Size(int transactionQueue1000Size) {
        this.transactionQueue1000Size = transactionQueue1000Size;
    }

    public static TransactionQueueSettings defaultSettings() {
        return new TransactionQueueSettings(110, 2, 1050, 6);
    }
}
