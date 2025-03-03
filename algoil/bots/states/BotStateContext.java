package ru.effectivegroup.client.algoil.bots.states;

import java.time.LocalTime;
import java.util.List;
import ru.effectivegroup.client.algoil.bots.BotLogger;
import ru.effectivegroup.client.algoil.bots.BotMode;
import ru.effectivegroup.client.algoil.bots.BotStartMode;
import ru.effectivegroup.client.algoil.bots.WaitApprovalTimerState;
import ru.effectivegroup.client.algoil.execution.CancelOrderResult;
import ru.effectivegroup.client.algoil.execution.Order;
import ru.effectivegroup.client.algoil.execution.PlaceOrderResult;
import ru.effectivegroup.client.algoil.marketdata.OrderBookRow;
import ru.effectivegroup.client.model.data.ste.dictionary.BSType;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/states/BotStateContext.class */
public interface BotStateContext {
    BotLogger getLogger();

    List<OrderBookRow> getBids();

    OrderBookRow getBestAsk();

    OrderBookRow getBestBid();

    Order getActiveOrder();

    Order getPreviousOrder();

    void setActiveOrderAsPrevious();

    boolean hasPreviousOrder();

    void setState(BotState botState);

    Long getUpperPriceLimit();

    int getStartPrice();

    PlaceOrderResult PlaceOrder(BSType bSType, long j, long j2);

    CancelOrderResult CancelActiveOrder();

    CancelOrderResult CancelPreviousOrder();

    int getPositionLimit();

    int getOrderCountLimit();

    int getOverlapStep();

    int getMaxOverlapStep();

    int getDelayPlace();

    int getInstrumentPriceStep();

    int getDelayCancel();

    int getPosition();

    BotMode getBotMode();

    long getPriceLimit();

    int getOrderSize();

    boolean bookHasOwnBids();

    int getQty();

    void clearActiveOrder();

    void clearPreviousOrder();

    int getOrdersCount();

    boolean getSecurityIsTradable();

    boolean timeIsBefore94ErrorProcessingTime();

    void setErrorProcessingTime(LocalTime localTime);

    LocalTime getErrorProcessingTime();

    boolean timeIsBefore1045();

    boolean timeIsBeforeStartTime();

    boolean timeIsAfter1045();

    int getStartVolume();

    int getTodayVolume();

    String getStartTime();

    boolean getFallDown();

    boolean isNextOrderFirst();

    void setNextOrderFirst(boolean z);

    String getDiscreteAuctionEndTime();

    BotStartMode getBotStartMode();

    LocalTime getTime();

    void Handle(WaitApprovalTimerState waitApprovalTimerState);
}
