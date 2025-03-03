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

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/adapter/DefaultSTEMessageConsumer.class */
public abstract class DefaultSTEMessageConsumer implements STEMessageConsumer {
    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(STEProtos.RepOrderOK message) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(STEProtos.RepOrderBAD message) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(ClientData message) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(UserData message) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(AccountData message) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(ClearingData message) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer, ru.effectivegroup.client.algoil.marketdata.DataFeedMessageConsumer
    public void Handle(SecurityData message) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(MemberData message) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(OrderData message) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(TradeData message) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(HoldingData message) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(SecTypeData message) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(BoardData message) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(AuctionScheduleData message) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(ErrorData message) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(DictionaryData message) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(DeliveryBasisData message) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(BDStateData message) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(SectionData message) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(MessagesData message) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(NBOTradeData message) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(BuyLimitData message) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(SellLimitData message) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(BPLLimitData message) {
    }

    @Override // ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(GroupLimitData message) {
    }
}
