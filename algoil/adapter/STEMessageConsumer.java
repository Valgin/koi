package ru.effectivegroup.client.algoil.adapter;

import ru.effectivegroup.client.gui.widget.impl.common.messages.arm.MessagesData;
import ru.effectivegroup.client.model.data.ste.AccountData;
import ru.effectivegroup.client.model.data.ste.AuctionScheduleData;
import ru.effectivegroup.client.model.data.ste.BDStateData;
import ru.effectivegroup.client.model.data.ste.BPLLimitData;
import ru.effectivegroup.client.model.data.ste.BoardData;
import ru.effectivegroup.client.model.data.ste.BuyLimitData;
import ru.effectivegroup.client.model.data.ste.ClearingData;
import ru.effectivegroup.client.model.data.ste.ClientData;
import ru.effectivegroup.client.model.data.ste.DeliveryBasisData;
import ru.effectivegroup.client.model.data.ste.DictionaryData;
import ru.effectivegroup.client.model.data.ste.ErrorData;
import ru.effectivegroup.client.model.data.ste.GroupLimitData;
import ru.effectivegroup.client.model.data.ste.HoldingData;
import ru.effectivegroup.client.model.data.ste.MemberData;
import ru.effectivegroup.client.model.data.ste.NBOTradeData;
import ru.effectivegroup.client.model.data.ste.OrderData;
import ru.effectivegroup.client.model.data.ste.SecTypeData;
import ru.effectivegroup.client.model.data.ste.SectionData;
import ru.effectivegroup.client.model.data.ste.SecurityData;
import ru.effectivegroup.client.model.data.ste.SellLimitData;
import ru.effectivegroup.client.model.data.ste.TradeData;
import ru.effectivegroup.client.model.data.ste.UserData;
import ru.effectivegroup.client.network.message.STEProtos;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/adapter/STEMessageConsumer.class */
public interface STEMessageConsumer {
    void Handle(STEProtos.RepOrderOK repOrderOK);

    void Handle(ClientData clientData);

    void Handle(UserData userData);

    void Handle(AccountData accountData);

    void Handle(ClearingData clearingData);

    void Handle(SecurityData securityData);

    void Handle(MemberData memberData);

    void Handle(STEProtos.RepOrderBAD repOrderBAD);

    void Handle(OrderData orderData);

    void Handle(TradeData tradeData);

    void Handle(HoldingData holdingData);

    void Handle(SecTypeData secTypeData);

    void Handle(BoardData boardData);

    void Handle(AuctionScheduleData auctionScheduleData);

    void Handle(ErrorData errorData);

    void Handle(DictionaryData dictionaryData);

    void Handle(DeliveryBasisData deliveryBasisData);

    void Handle(BDStateData bDStateData);

    void Handle(SectionData sectionData);

    void Handle(MessagesData messagesData);

    void Handle(NBOTradeData nBOTradeData);

    void Handle(BuyLimitData buyLimitData);

    void Handle(SellLimitData sellLimitData);

    void Handle(BPLLimitData bPLLimitData);

    void Handle(GroupLimitData groupLimitData);
}
