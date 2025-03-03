package ru.effectivegroup.client.algoil.settings;

import ru.effectivegroup.client.algoil.Instrument;
import ru.effectivegroup.client.algoil.bots.BotMode;
import ru.effectivegroup.client.algoil.bots.BotStartMode;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/settings/BotSettings.class */
public class BotSettings {
    private Instrument instrument;
    private String moneyAccount;
    private String commodityAccount;
    private int orderSize;
    private int startPrice;
    private int maxPricePrice;
    private int positionLimit;
    private int orderCountLimit;
    private int maxOverlapStep;
    private int minOverlapStep;
    private String client;
    private int delayPlace;
    private int delayCancel;
    private double maxPricePercentage;
    private BotMode botMode;
    private Long timeSpread;
    private int startVolume;
    private String startTime;
    private boolean fallDown;
    private boolean botListFixation;
    private String discreteAuctionEndTime;
    private BotStartMode botStartMode;

    public BotSettings() {
        this.orderCountLimit = 50;
    }

    public BotSettings(Instrument instrument, String moneyAccount, String commodityAccount, String client, int orderSize, int startPrice, int maxPricePrice, int positionLimit, int orderCountLimit, int maxOverlapStep, int minOverlapStep, int delayPlace, int delayCancel, double maxPricePercentage, BotMode botMode, Long timeSpread, int startVolume, String startTime, boolean fallDown, boolean isFixed, String discreteAuctionEndTime, BotStartMode botStartMode) {
        this.orderCountLimit = 50;
        this.instrument = instrument;
        this.moneyAccount = moneyAccount;
        this.commodityAccount = commodityAccount;
        this.client = client;
        this.orderSize = orderSize;
        this.startPrice = startPrice;
        this.maxPricePrice = maxPricePrice;
        this.positionLimit = positionLimit;
        this.delayPlace = delayPlace;
        this.delayCancel = delayCancel;
        this.maxOverlapStep = maxOverlapStep;
        this.minOverlapStep = minOverlapStep;
        this.orderCountLimit = orderCountLimit;
        this.maxPricePercentage = maxPricePercentage;
        this.botMode = botMode;
        this.timeSpread = timeSpread;
        this.startVolume = startVolume;
        this.startTime = startTime;
        this.fallDown = fallDown;
        this.botListFixation = isFixed;
        this.discreteAuctionEndTime = discreteAuctionEndTime;
        this.botStartMode = botStartMode;
    }

    public Instrument getInstrument() {
        return this.instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public String getMoneyAccount() {
        return this.moneyAccount;
    }

    public void setMoneyAccount(String moneyAccount) {
        this.moneyAccount = moneyAccount;
    }

    public String getCommodityAccount() {
        return this.commodityAccount;
    }

    public void setCommodityAccount(String commodityAccount) {
        this.commodityAccount = commodityAccount;
    }

    public int getOrderSize() {
        return this.orderSize;
    }

    public void setOrderSize(int orderSize) {
        this.orderSize = orderSize;
    }

    public int getStartPrice() {
        return this.startPrice;
    }

    public void setStartPrice(int startPrice) {
        this.startPrice = startPrice;
    }

    public int getMaxPrice() {
        return this.maxPricePrice;
    }

    public void setMaxPrice(int maxPricePrice) {
        this.maxPricePrice = maxPricePrice;
    }

    public int getStartVolume() {
        return this.startVolume;
    }

    public void setStartVolume(int startVolume) {
        this.startVolume = startVolume;
    }

    public String getStartTime() {
        return this.startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public int getPositionLimit() {
        return this.positionLimit;
    }

    public void setPositionLimit(int positionLimit) {
        this.positionLimit = positionLimit;
    }

    public int getOrderCountLimit() {
        return this.orderCountLimit;
    }

    public void setOrderCountLimit(int orderCountLimit) {
        this.orderCountLimit = orderCountLimit;
    }

    public int getOverlapStep() {
        return this.maxOverlapStep;
    }

    public boolean isBotListFixation() {
        return this.botListFixation;
    }

    public void setBotListFixation(boolean botListFixation) {
        this.botListFixation = botListFixation;
    }

    public void setOverlapStep(int maxOverlapStep) {
        this.maxOverlapStep = maxOverlapStep;
    }

    public int getMinOverlapStep() {
        return this.minOverlapStep;
    }

    public void setMinOverlapStep(int minOverlapStep) {
        this.minOverlapStep = minOverlapStep;
    }

    public boolean getFallDown() {
        return this.fallDown;
    }

    public void setFallDown(Boolean fallDown) {
        this.fallDown = fallDown.booleanValue();
    }

    public int getDelayPlace() {
        return this.delayPlace;
    }

    public int getDelayCancel() {
        return this.delayCancel;
    }

    public void setDelayPlace(int delayPlace) {
        this.delayPlace = delayPlace;
    }

    public void setDelayCancel(int delayCancel) {
        this.delayCancel = delayCancel;
    }

    public BotMode getBotMode() {
        return this.botMode;
    }

    public String getClient() {
        return this.client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public void setBotMode(BotMode botMode) {
        this.botMode = botMode;
    }

    public double getMaxPricePercentage() {
        return this.maxPricePercentage;
    }

    public Long getTimeSpread() {
        return this.timeSpread;
    }

    public void setTimeSpread(Long timeSpread) {
        this.timeSpread = timeSpread;
    }

    public String getDiscreteAuctionEndTime() {
        return this.discreteAuctionEndTime;
    }

    public void setDiscreteAuctionEndTime(String discreteAuctionEndTime) {
        this.discreteAuctionEndTime = discreteAuctionEndTime;
    }

    public BotStartMode getBotStartMode() {
        return this.botStartMode;
    }

    public void setBotStartMode(BotStartMode botStartMode) {
        this.botStartMode = botStartMode;
    }
}
