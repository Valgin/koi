package ru.effectivegroup.client.algoil.bots;

import java.time.LocalTime;
import ru.effectivegroup.client.algoil.execution.CancelOrderResult;
import ru.effectivegroup.client.algoil.execution.Order;
import ru.effectivegroup.client.algoil.execution.OrderEmitter;
import ru.effectivegroup.client.algoil.execution.PlaceOrderResult;
import ru.effectivegroup.client.model.data.ste.dictionary.BSType;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/BotContext.class */
public interface BotContext {
    PlaceOrderResult PlaceOrder(String str, BSType bSType, long j, long j2, String str2, String str3, String str4, String str5, String str6, OrderEmitter orderEmitter);

    CancelOrderResult CancelOrder(Order order);

    boolean timeIsBefore94ErrorProcessingTime(LocalTime localTime);

    boolean timeIsBefore1045(Long l);

    boolean timeIsBeforeStartTime(LocalTime localTime, Long l);

    boolean timeIsAfter1045();
}
