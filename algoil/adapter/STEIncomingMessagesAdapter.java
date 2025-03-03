package ru.effectivegroup.client.algoil.adapter;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import javafx.beans.property.BooleanProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.effectivegroup.client.context.Context;
import ru.effectivegroup.client.context.ContextProperties;
import ru.effectivegroup.client.crypto.cipher.CipherHolder;
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
import ru.effectivegroup.client.model.data.ste.dictionary.AuctionOrderMode;
import ru.effectivegroup.client.model.data.ste.helper.SecurityHelper;
import ru.effectivegroup.client.network.manager.event.Event;
import ru.effectivegroup.client.network.manager.event.EventType;
import ru.effectivegroup.client.network.manager.event.MessageEvent;
import ru.effectivegroup.client.network.message.STEProtos;
import ru.effectivegroup.client.repository.DataRepository;
import ru.effectivegroup.client.service.ServiceContext;
import ru.effectivegroup.client.service.auth.spimexid.SpimexIdTokenRefresher;
import ru.effectivegroup.client.service.cma.Convert;
import ru.effectivegroup.client.service.fte.IdTracker;
import ru.effectivegroup.client.service.ste.TranslationKeyDictionary;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/adapter/STEIncomingMessagesAdapter.class */
public class STEIncomingMessagesAdapter implements IncomingMessagesAdapter, Runnable {
    private final Logger logger = LogManager.getLogger("algoil");
    private final IdTracker buyLimitsIdTracker = new IdTracker();
    private final IdTracker sellLimitsIdTracker = new IdTracker();
    private final IdTracker bplLimitsIdTracker = new IdTracker();
    private final IdTracker groupLimitsIdTracker = new IdTracker();
    private final HashSet<STEMessageConsumer> subscribers = new HashSet<>();
    private final ConcurrentLinkedQueue<Event> eventsQueue = new ConcurrentLinkedQueue<>();
    private final ArrayList<OrderData> orderDataBuffer = new ArrayList<>();
    private boolean isRunning = true;

    public STEIncomingMessagesAdapter() {
        Context.serviceContext.networkManager.subscribe(EventType.MESSAGE_RECEIVED, e -> {
            processEvent(e);
        });
        new Thread(this, "STE Incoming Messages Thread").start();
        this.logger.info("Создан STEIncomingMessagesAdapter");
    }

    @Override // ru.effectivegroup.client.algoil.adapter.IncomingMessagesAdapter
    public void Stop() {
        this.isRunning = false;
    }

    @Override // java.lang.Runnable
    public void run() {
        this.logger.info("Запущен поток обработки сообщений STEIncomingMessagesAdapter");
        while (this.isRunning) {
            try {
                MessageEvent messageEvent = (Event) this.eventsQueue.poll();
                if (messageEvent == null) {
                    Thread.sleep(100L);
                } else {
                    STEProtos.Envelope envelope = messageEvent.message;
                    if (envelope instanceof STEProtos.Envelope) {
                        STEProtos.Envelope envelope2 = envelope;
                        if (!envelope2.hasReqid() || envelope2.getReqid() == 0) {
                            if (envelope2.getTypeCount() != 0 && envelope2.getType(0) == STEProtos.MessageType.REP_CHANGES) {
                                try {
                                    STEProtos.Changes changes = STEProtos.Changes.parseFrom(envelope2.getData(0));
                                    for (ByteString rowBytes : changes.getRowList()) {
                                        STEProtos.RowChange row = STEProtos.RowChange.parseFrom(rowBytes);
                                        try {
                                            handleRow(row);
                                        } catch (Throwable var9) {
                                            LogManager.getLogger().error("Ошибка обработки строки СЭТ ТР", var9);
                                            if (ContextProperties.isDevEnvironment()) {
                                                Context.serviceContext.notificationService.createError(String.format("Ошибка обработки строки СЭТ ТР %s: %s", row.getTabId(), var9.getMessage()));
                                            }
                                        }
                                    }
                                } catch (Exception var10) {
                                    this.logger.error("Не удалось сохранить изменения СЭТ ТР", var10);
                                }
                            }
                        }
                    } else if (!envelope.getClass().getTypeName().contains("Heartbeat") && !envelope.getClass().getTypeName().contains("FTEProtos") && !envelope.getClass().getTypeName().contains("AuthProtos") && !envelope.getClass().getTypeName().contains("SessionAlertProtos") && !envelope.getClass().getTypeName().contains("SystemProtos") && !envelope.getClass().getTypeName().contains("TariffProtos")) {
                        this.logger.debug("Получено необрабатываемое сообщение типа {}", envelope.getClass());
                    }
                }
            } catch (Exception e) {
                this.logger.error("Ошибка в потоке обработки сообщений STEIncomingMessagesAdapter", e);
            }
        }
        this.logger.info("Остановлен поток обработки сообщений STEIncomingMessagesAdapter");
    }

    @Override // ru.effectivegroup.client.algoil.adapter.IncomingMessagesAdapter
    public void Subscribe(STEMessageConsumer consumer) {
        this.logger.debug("Добавление {} в список получателей входящих сообщений", consumer);
        synchronized (this.subscribers) {
            this.subscribers.add(consumer);
        }
    }

    @Override // ru.effectivegroup.client.algoil.adapter.IncomingMessagesAdapter
    public void Unsubscribe(STEMessageConsumer consumer) {
        synchronized (this.subscribers) {
            this.subscribers.remove(consumer);
        }
    }

    private void processEvent(Event event) {
        this.eventsQueue.add(event);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: ru.effectivegroup.client.algoil.adapter.STEIncomingMessagesAdapter$1, reason: invalid class name */
    /* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/adapter/STEIncomingMessagesAdapter$1.class */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ru$effectivegroup$client$network$message$STEProtos$TableType = new int[STEProtos.TableType.values().length];

        static {
            try {
                $SwitchMap$ru$effectivegroup$client$network$message$STEProtos$TableType[STEProtos.TableType.TAB_FIRM.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$network$message$STEProtos$TableType[STEProtos.TableType.TAB_CLIENT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$network$message$STEProtos$TableType[STEProtos.TableType.TAB_USER.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$network$message$STEProtos$TableType[STEProtos.TableType.TAB_ACCOUNT.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$network$message$STEProtos$TableType[STEProtos.TableType.TAB_SECURITY.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$network$message$STEProtos$TableType[STEProtos.TableType.TAB_HOLDING.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$network$message$STEProtos$TableType[STEProtos.TableType.TAB_SECTYPE.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$network$message$STEProtos$TableType[STEProtos.TableType.TAB_BOARD.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$network$message$STEProtos$TableType[STEProtos.TableType.TAB_DICTIONARY.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$network$message$STEProtos$TableType[STEProtos.TableType.TAB_DELIVERYBASIS.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$network$message$STEProtos$TableType[STEProtos.TableType.TAB_TRADE.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$network$message$STEProtos$TableType[STEProtos.TableType.TAB_ORDER.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$network$message$STEProtos$TableType[STEProtos.TableType.TAB_AUCTION_SCHEDULE.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$network$message$STEProtos$TableType[STEProtos.TableType.TAB_ERROR.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$network$message$STEProtos$TableType[STEProtos.TableType.TAB_BDSTATE.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$network$message$STEProtos$TableType[STEProtos.TableType.TAB_SECTION.ordinal()] = 16;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$network$message$STEProtos$TableType[STEProtos.TableType.TAB_MEDDELANDE.ordinal()] = 17;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$network$message$STEProtos$TableType[STEProtos.TableType.TAB_NBO_TRADE.ordinal()] = 18;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$network$message$STEProtos$TableType[STEProtos.TableType.TAB_BUYLIMITS.ordinal()] = 19;
            } catch (NoSuchFieldError e19) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$network$message$STEProtos$TableType[STEProtos.TableType.TAB_SELLLIMITS.ordinal()] = 20;
            } catch (NoSuchFieldError e20) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$network$message$STEProtos$TableType[STEProtos.TableType.TAB_BPLIMITS.ordinal()] = 21;
            } catch (NoSuchFieldError e21) {
            }
            try {
                $SwitchMap$ru$effectivegroup$client$network$message$STEProtos$TableType[STEProtos.TableType.TAB_GROUPLIMITS.ordinal()] = 22;
            } catch (NoSuchFieldError e22) {
            }
        }
    }

    private void handleRow(STEProtos.RowChange rowChange) throws InvalidProtocolBufferException {
        switch (AnonymousClass1.$SwitchMap$ru$effectivegroup$client$network$message$STEProtos$TableType[rowChange.getTabId().ordinal()]) {
            case 1:
                handleFirm(rowChange.getRowData().toByteArray());
                return;
            case 2:
                handleClient(rowChange.getRowData().toByteArray());
                return;
            case CipherHolder.CIPHER_PARAM_ENCRYPTED_SECRET_KEY /* 3 */:
                handleUser(rowChange.getRowData().toByteArray());
                return;
            case CipherHolder.CIPHER_PARAM_IV /* 4 */:
                handleAccount(rowChange.getRowData().toByteArray());
                return;
            case 5:
                handleSecurity(rowChange.getRowData().toByteArray());
                return;
            case CipherHolder.CIPHER_PARAM_MAC_KEY /* 6 */:
                handleHolding(rowChange.getRowData().toByteArray());
                return;
            case 7:
                handleSecType(rowChange.getRowData().toByteArray());
                return;
            case ServiceContext.DEFAULT_TASK_POOL_SIZE /* 8 */:
                handleBoard(rowChange.getRowData().toByteArray());
                return;
            case 9:
                handleDictionary(rowChange.getRowData().toByteArray());
                return;
            case 10:
                handleDeliveryBasis(rowChange.getRowData().toByteArray());
                return;
            case 11:
                handleTrade(rowChange.getRowData().toByteArray());
                return;
            case 12:
                handleOrder(rowChange.getRowData().toByteArray());
                return;
            case 13:
                handleAuctionSchedule(rowChange.getRowData().toByteArray());
                return;
            case 14:
                handleError(rowChange.getRowData().toByteArray());
                return;
            case 15:
                handleBDState(rowChange.getRowData().toByteArray());
                return;
            case 16:
                handleSection(rowChange.getRowData().toByteArray());
                return;
            case 17:
                handleMessagesARM(rowChange.getRowData().toByteArray());
                return;
            case 18:
                handleNBOTrade(rowChange.getRowData().toByteArray());
                return;
            case 19:
                handleBuyLimits(rowChange.getRowData().toByteArray());
                return;
            case SpimexIdTokenRefresher.REFRESH_DEFAULT_TIMEOUT /* 20 */:
                handleSellLimits(rowChange.getRowData().toByteArray());
                return;
            case 21:
                handleBPLLimits(rowChange.getRowData().toByteArray());
                return;
            case 22:
                handleGroupLimits(rowChange.getRowData().toByteArray());
                return;
            default:
                throw new RuntimeException("Неподдерживаемый тип изменений: " + rowChange.getTabId());
        }
    }

    private void handleMessagesARM(byte[] message) {
    }

    public void handleClient(byte[] clientData) throws InvalidProtocolBufferException {
        STEProtos.ClientInfo client = STEProtos.ClientInfo.parseFrom(clientData);
        ClientData pojo = new ClientData();
        pojo.setId(client.getId());
        pojo.setCode(Convert.toUTF8(client.getCodeBytes()));
        pojo.setStatus(TranslationKeyDictionary.statusTypes.get(client.getStatus()));
        pojo.setFirmCode(Convert.toUTF8(client.getFirmCodeBytes()));
        pojo.setName(Convert.toUTF8(client.getNameBytes()));
        pojo.setInn(Convert.toUTF8(client.getInnBytes()));
        pojo.setShortName(Convert.toUTF8(client.getShortNameBytes()));
        synchronized (this.subscribers) {
            Iterator<STEMessageConsumer> it = this.subscribers.iterator();
            while (it.hasNext()) {
                STEMessageConsumer sub = it.next();
                sub.Handle(pojo);
            }
        }
    }

    public void handleUser(byte[] userData) throws InvalidProtocolBufferException {
        STEProtos.UserInfo user = STEProtos.UserInfo.parseFrom(userData);
        UserData pojo = new UserData();
        pojo.setId(user.getId());
        pojo.setCode(Convert.toUTF8(user.getCodeBytes()));
        pojo.setName(Convert.toUTF8(user.getNameBytes()));
        pojo.setType(TranslationKeyDictionary.userType.get(user.getType()));
        pojo.setStatus(TranslationKeyDictionary.statusTypes.get(user.getStatus()));
        pojo.setEncrypt(TranslationKeyDictionary.encryptType.get(user.getEncrypt()));
        pojo.setSign(TranslationKeyDictionary.signType.get(user.getSign()));
        pojo.setLogonTime(Convert.toDate(user.getLogonTime()));
        pojo.setLogoffTime(Convert.toDate(user.getLogoffTime()));
        pojo.setAuctionManager(TranslationKeyDictionary.aManagerStatusType.get(user.getAuctionManager()));
        pojo.firmCode = Convert.toUTF8(user.getFirmCodeBytes());
        HashSet<Object> auctionListBuilder = new HashSet<>();
        for (int i = 0; i < user.getAuctManagerSecurityListList().size(); i++) {
            auctionListBuilder.add(Convert.toUTF8(user.getAuctManagerSecurityListBytes(i)));
        }
        pojo.setAuctManagerSecurityList(String.join(",", auctionListBuilder.toString()));
        synchronized (this.subscribers) {
            Iterator<STEMessageConsumer> it = this.subscribers.iterator();
            while (it.hasNext()) {
                STEMessageConsumer sub = it.next();
                sub.Handle(pojo);
            }
            tryProcessOrderDataBuffer();
        }
    }

    public void handleAccount(byte[] accountData) throws InvalidProtocolBufferException {
        STEProtos.AccountInfo account = STEProtos.AccountInfo.parseFrom(accountData);
        String clearingCode = Convert.toUTF8(account.getClearingFirmCodeBytes());
        String clearingName = Convert.toUTF8(account.getClearingFirmNameBytes());
        handleClearing(clearingCode, clearingName);
        AccountData pojo = new AccountData();
        pojo.setId(account.getId());
        pojo.setCode(Convert.toUTF8(account.getCodeBytes()));
        pojo.setType(TranslationKeyDictionary.accountTypes.get(account.getType()));
        pojo.setFirmCode(Convert.toUTF8(account.getFirmCodeBytes()));
        pojo.setClearingFirmCode(Convert.toUTF8(account.getClearingFirmCodeBytes()));
        pojo.setVat(account.getVat());
        pojo.setStatus(TranslationKeyDictionary.statusTypes.get(account.getStatus()));
        pojo.setClient(account.getIsClient());
        pojo.setCommon(account.getIsCommon());
        pojo.setClientCode(Convert.toUTF8(account.getClientCodeBytes()));
        synchronized (this.subscribers) {
            Iterator<STEMessageConsumer> it = this.subscribers.iterator();
            while (it.hasNext()) {
                STEMessageConsumer sub = it.next();
                sub.Handle(pojo);
            }
        }
    }

    public void handleClearing(String code, String name) {
        DataRepository<ClearingData> repository = Context.repositoryContext.get(ClearingData.class);
        ClearingData pojo = (ClearingData) repository.firstByEq("code", code);
        if (pojo == null) {
            pojo = new ClearingData();
        }
        pojo.setCode(code);
        pojo.setName(name);
        synchronized (this.subscribers) {
            Iterator<STEMessageConsumer> it = this.subscribers.iterator();
            while (it.hasNext()) {
                STEMessageConsumer sub = it.next();
                sub.Handle(pojo);
            }
        }
    }

    public void handleSecurity(byte[] securityData) throws InvalidProtocolBufferException {
        STEProtos.SecurityInfo security = STEProtos.SecurityInfo.parseFrom(securityData);
        SecurityData pojo = new SecurityData();
        pojo.setId(security.getId());
        pojo.setCode(Convert.toUTF8(security.getCodeBytes()));
        pojo.setName(Convert.toUTF8(security.getNameBytes()));
        pojo.setFullName(Convert.toUTF8(security.getFullNameBytes()));
        pojo.setLotSize(security.getLotSize());
        pojo.setMinMarketPrice(security.getMinMarketPrice());
        pojo.setMaxMarketPrice(security.getMaxMarketPrice());
        pojo.setAmountStep(security.getAmountStep());
        pojo.setSellerMoneySupport(security.getSellerMoneySupport());
        pojo.setSellerProductSupport(security.getSellerProductSupport());
        pojo.setBuyerMoneySupport(security.getBuyerMoneySupport());
        pojo.setBuyerProductSupport(security.getBuyerProductSupport());
        pojo.setMoneyRoundingPrecision(security.getMoneyRoundingPrecision());
        pojo.setProductRoundingPrecision(security.getProductRoundingPrecision());
        pojo.setProductCategoryCode(Convert.toUTF8(security.getProductCategoryCodeBytes()));
        pojo.setProductTypeCode(Convert.toUTF8(security.getProductTypeCodeBytes()));
        pojo.setQualityStandardCode(Convert.toUTF8(security.getQualityStandardCodeBytes()));
        pojo.setEcologyClassCode(Convert.toUTF8(security.getEcologyClassCodeBytes()));
        pojo.setStatus(TranslationKeyDictionary.statusTypes.get(security.getStatus()));
        pojo.setLastTradeCode(Convert.toUTF8(security.getLastTradeCodeBytes()));
        pojo.setHigh(security.getHigh());
        pojo.setLow(security.getLow());
        pojo.setCount(security.getCount());
        pojo.setVolume(security.getVolume());
        pojo.setValue(security.getValue());
        pojo.setAddrCount(security.getAddrCount());
        pojo.setAddrVolume(security.getAddrVolume());
        pojo.setAddrValue(security.getAddrValue());
        pojo.setAskCount(security.getAskCount());
        pojo.setAskVolume(security.getAskVolume());
        pojo.setAskValue(security.getAskValue());
        pojo.setBestAsk(security.getBestAsk());
        pojo.setBidCount(security.getBidCount());
        pojo.setBidVolume(security.getBidVolume());
        pojo.setBidValue(security.getBidValue());
        pojo.setBestBid(security.getBestBid());
        pojo.setOpenPrice(security.getOpenPrice());
        pojo.setPrevMarketPrice(security.getPrevMarketPrice());
        pojo.setMarketPrice(security.getMarketPrice());
        if (security.getPrevMarketPrice() != 0 && security.getMarketPrice() != 0) {
            pojo.setMarketPriceChange(security.getMarketPrice() - security.getPrevMarketPrice());
        } else {
            pojo.setMarketPriceChange(security.getMarketPriceChange());
        }
        pojo.setGoodForFp(security.getGoodForFp());
        pojo.setPriceChangesLimit(security.getPriceChangesLimit());
        pojo.setBpLimit(security.getBpLimit());
        pojo.setSessionStatus(TranslationKeyDictionary.tradeStatusType.get(security.getSessionStatus()));
        pojo.setFixPrice(security.getFixPrice());
        pojo.setLowerPrice(security.getLowerPrice());
        pojo.setUpperPrice(security.getUpperPrice());
        pojo.setDeliveryBasisCode(Convert.toUTF8(security.getDeliveryBasisCodeBytes()));
        pojo.setDimensionCode(Convert.toUTF8(security.getDimensionCodeBytes()));
        pojo.setSecTypeCode(Convert.toUTF8(security.getSecTypeCodeBytes()));
        pojo.setMinIpPrice(security.getMinIpPrice());
        pojo.setCurrIpVolume(security.getCurrIpVolume());
        pojo.setVisibleInMainTrading(security.getIsVisibleInMainTrading());
        Context.repositoryContext.get(SecurityData.class).save(pojo);
        SecurityHelper.getInstance().processSecurityData(pojo);
        Context.serviceContext.steSecurityListenerManager.processData(pojo);
        synchronized (this.subscribers) {
            Iterator<STEMessageConsumer> it = this.subscribers.iterator();
            while (it.hasNext()) {
                STEMessageConsumer sub = it.next();
                sub.Handle(pojo);
            }
        }
    }

    public void handleHolding(byte[] data) throws InvalidProtocolBufferException {
        STEProtos.Holding holding = STEProtos.Holding.parseFrom(data);
        HoldingData pojo = new HoldingData();
        pojo.setCode(Convert.toUTF8(holding.getCodeBytes()));
        pojo.setIncomming(holding.getIncomming());
        pojo.setResOrder(holding.getResOrder());
        pojo.setResTrade(holding.getResTrade());
        pojo.setAvailable(holding.getAvailable());
        pojo.setId(pojo.getCode().hashCode());
        String accountCode = Convert.toUTF8(holding.getAccountCodeBytes());
        pojo.setAccountCode(accountCode);
        pojo.setSecurityCode(Convert.toUTF8(holding.getSecurityCodeBytes()));
        synchronized (this.subscribers) {
            Iterator<STEMessageConsumer> it = this.subscribers.iterator();
            while (it.hasNext()) {
                STEMessageConsumer sub = it.next();
                sub.Handle(pojo);
            }
        }
    }

    public void handleSecType(byte[] data) throws InvalidProtocolBufferException {
        STEProtos.SecTypeInfo secType = STEProtos.SecTypeInfo.parseFrom(data);
        SecTypeData pojo = new SecTypeData();
        pojo.setId(secType.getId());
        pojo.setCode(Convert.toUTF8(secType.getCodeBytes()));
        pojo.setName(Convert.toUTF8(secType.getNameBytes()));
        pojo.setBoardCode(Convert.toUTF8(secType.getBoardCodeBytes()));
        pojo.setVat(secType.getVat());
        pojo.setTradeStatus(Convert.toUTF8(secType.getTradeStatusBytes()));
        pojo.setSessionStatus(TranslationKeyDictionary.tradeStatusType.get(secType.getSessionStatus()));
        synchronized (this.subscribers) {
            Iterator<STEMessageConsumer> it = this.subscribers.iterator();
            while (it.hasNext()) {
                STEMessageConsumer sub = it.next();
                sub.Handle(pojo);
            }
        }
    }

    public void handleBoard(byte[] data) throws InvalidProtocolBufferException {
        STEProtos.BoardInfo board = STEProtos.BoardInfo.parseFrom(data);
        BoardData pojo = new BoardData();
        pojo.setId(board.getId());
        pojo.setCode(Convert.toUTF8(board.getCodeBytes()));
        pojo.setName(Convert.toUTF8(board.getNameBytes()));
        pojo.setSectionCode(Convert.toUTF8(board.getSectionCodeBytes()));
        synchronized (this.subscribers) {
            Iterator<STEMessageConsumer> it = this.subscribers.iterator();
            while (it.hasNext()) {
                STEMessageConsumer sub = it.next();
                sub.Handle(pojo);
            }
        }
    }

    public void handleDictionary(byte[] data) throws InvalidProtocolBufferException {
        STEProtos.DictionaryInfo dictionary = STEProtos.DictionaryInfo.parseFrom(data);
        DictionaryData pojo = new DictionaryData();
        pojo.setId(dictionary.getId());
        pojo.setCode(Convert.toUTF8(dictionary.getCodeBytes()));
        pojo.setName(Convert.toUTF8(dictionary.getNameBytes()));
        synchronized (this.subscribers) {
            Iterator<STEMessageConsumer> it = this.subscribers.iterator();
            while (it.hasNext()) {
                STEMessageConsumer sub = it.next();
                sub.Handle(pojo);
            }
        }
    }

    public void handleDeliveryBasis(byte[] data) throws InvalidProtocolBufferException {
        STEProtos.DeliveryBasisInfo basis = STEProtos.DeliveryBasisInfo.parseFrom(data);
        DeliveryBasisData pojo = new DeliveryBasisData();
        pojo.setId(basis.getId());
        pojo.setCode(Convert.toUTF8(basis.getCodeBytes()));
        pojo.setName(Convert.toUTF8(basis.getNameBytes()));
        synchronized (this.subscribers) {
            Iterator<STEMessageConsumer> it = this.subscribers.iterator();
            while (it.hasNext()) {
                STEMessageConsumer sub = it.next();
                sub.Handle(pojo);
            }
        }
    }

    public void handleTrade(byte[] data) throws InvalidProtocolBufferException {
        STEProtos.TradeInfo trade = STEProtos.TradeInfo.parseFrom(data);
        TradeData pojo = new TradeData();
        pojo.setId(trade.getId());
        pojo.setCode(Convert.toUTF8(trade.getCodeBytes()));
        pojo.setTradeType(TranslationKeyDictionary.tradeTypes.get(trade.getType()));
        pojo.setTradeRealType(TranslationKeyDictionary.tradeRealTypes.get(trade.getType()));
        pojo.setCreationTime(Convert.toDateTime(trade.getCreationTime()));
        pojo.setBuyOrderCode(Convert.toUTF8(trade.getBuyOrderCodeBytes()));
        pojo.setSellOrderCode(Convert.toUTF8(trade.getSellOrderCodeBytes()));
        pojo.setQuantity(trade.getQty());
        pojo.setPrice(trade.getPrice());
        pojo.setSecurityCode(Convert.toUTF8(trade.getSecurityCodeBytes()));
        pojo.setBuyerFirmCode(Convert.toUTF8(trade.getBuyerFirmCodeBytes()));
        pojo.setSellerFirmCode(Convert.toUTF8(trade.getSellerFirmCodeBytes()));
        pojo.setBuyerClrFirmCode(Convert.toUTF8(trade.getBuyerClrFirmCodeBytes()));
        pojo.setSellerClrFirmCode(Convert.toUTF8(trade.getSellerClrFirmCodeBytes()));
        pojo.setBuyerClrFirmName(Convert.toUTF8(trade.getBuyerClrFirmNameBytes()));
        pojo.setSellerClrFirmName(Convert.toUTF8(trade.getSellerClrFirmNameBytes()));
        pojo.setValueRub(trade.getValue());
        pojo.setBuyerTerCode(Convert.toUTF8(trade.getBuyerTerCodeBytes()));
        pojo.setSellerTerCode(Convert.toUTF8(trade.getSellerTerCodeBytes()));
        pojo.setBuyOrderIsAuto(trade.hasBuyOrderIsAuto());
        pojo.setSellOrderIsAuto(trade.hasSellOrderIsAuto());
        synchronized (this.subscribers) {
            Iterator<STEMessageConsumer> it = this.subscribers.iterator();
            while (it.hasNext()) {
                STEMessageConsumer sub = it.next();
                sub.Handle(pojo);
            }
        }
    }

    public void handleAuctionSchedule(byte[] data) throws InvalidProtocolBufferException {
        STEProtos.AuctionScheduleInfo auction = STEProtos.AuctionScheduleInfo.parseFrom(data);
        AuctionScheduleData pojo = new AuctionScheduleData();
        pojo.setId(auction.getId());
        pojo.setCode(Convert.toUTF8(auction.getCodeBytes()));
        pojo.setName(Convert.toUTF8(auction.getNameBytes()));
        pojo.setStartTime(Convert.toDate(auction.getStartTime()));
        pojo.setFinishTime(Convert.toDate(auction.getFinishTime()));
        pojo.setManagerOrderFinishTime(Convert.toDate(auction.getManagerOrderFinishTime()));
        pojo.setCutoffTime(Convert.toDate(auction.getCutoffTime()));
        pojo.setAutoClose(auction.getAutoClose());
        pojo.setUserCode(Convert.toUTF8(auction.getUserCodeBytes()));
        pojo.setClientCode(Convert.toUTF8(auction.getClientCodeBytes()));
        pojo.setClientName(Convert.toUTF8(auction.getClientNameBytes()));
        pojo.setFirmCode(Convert.toUTF8(auction.getFirmCodeBytes()));
        pojo.setFirmName(Convert.toUTF8(auction.getFirmNameBytes()));
        pojo.setSecurityCode(Convert.toUTF8(auction.getSecurityCodeBytes()));
        pojo.setType(Convert.toUTF8(auction.getTypeBytes()).equalsIgnoreCase("D") ? AuctionOrderMode.DIV_AUCT : AuctionOrderMode.INDIV_AUCT);
        pojo.setBuySell(TranslationKeyDictionary.auctionBSTypes.get(auction.getBuySell()));
        pojo.setTradeDate(Convert.toDate(auction.getTradeDate()));
        pojo.setPrice(auction.getPrice());
        pojo.setQty(auction.getQty());
        pojo.setMinBids(auction.getMinBids());
        pojo.setSessionType(Convert.toUTF8(auction.getSessionTypeBytes()));
        pojo.setManagerOrderCode(Convert.toUTF8(auction.getManagerOrderCodeBytes()));
        pojo.setStatus(TranslationKeyDictionary.auctionMiniSessionStatusTypes.get(auction.getStatus()));
        pojo.setOrderCount(auction.getOrderCount());
        pojo.setBestPrice(auction.getBestPrice());
        pojo.setExecCount(auction.getExecCount());
        pojo.setExecVolume(auction.getExecVolume());
        pojo.setExecValue(auction.getExecValue());
        synchronized (this.subscribers) {
            Iterator<STEMessageConsumer> it = this.subscribers.iterator();
            while (it.hasNext()) {
                STEMessageConsumer sub = it.next();
                sub.Handle(pojo);
            }
        }
    }

    public void handleOrder(byte[] data) throws InvalidProtocolBufferException {
        STEProtos.OrderInfo order = STEProtos.OrderInfo.parseFrom(data);
        OrderData pojo = new OrderData();
        pojo.setId(order.getId());
        pojo.setCode(Convert.toUTF8(order.getCodeBytes()));
        pojo.setTrn(Convert.toUTF8(order.getTrnBytes()));
        pojo.setCreationTime(Convert.toDate(order.getCreationTime()));
        pojo.setRealType(TranslationKeyDictionary.realOrderTypes.get(order.getType()));
        pojo.setType(TranslationKeyDictionary.orderTypes.get(order.getType()));
        pojo.setView(TranslationKeyDictionary.orderViews.get(order.getType()));
        pojo.setSubType(TranslationKeyDictionary.orderSubTypes.get(order.getSubType()));
        pojo.setMode(TranslationKeyDictionary.orderModeTypes.get(order.getMode()));
        pojo.setBuySell(TranslationKeyDictionary.bsTypes.get(order.getBuySell()));
        pojo.setFirmCode(Convert.toUTF8(order.getFirmCodeBytes()));
        pojo.setUserCode(Convert.toUTF8(order.getUserCodeBytes()));
        pojo.setCommAccountCode(Convert.toUTF8(order.getCommAccountCodeBytes()));
        pojo.setCashAccountCode(Convert.toUTF8(order.getCashAccountCodeBytes()));
        pojo.setQty(order.getQty());
        pojo.setQtyLeft(order.getQtyLeft());
        pojo.setPrice(order.getPrice());
        pojo.setCounterPartyCode(Convert.toUTF8(order.getCounterPartyCodeBytes()));
        pojo.setComment(Convert.toUTF8(order.getCommentBytes()));
        pojo.setPawnQty(order.getPawnQty());
        pojo.setPawnAmount(order.getPawnAmount());
        pojo.setAuctionScheduleCode(Convert.toUTF8(order.getAuctionScheduleCodeBytes()));
        pojo.setManagerOrderCode(Convert.toUTF8(order.getManagerOrderCodeBytes()));
        pojo.setPrevStatus(TranslationKeyDictionary.orderStatuses.get(order.getPrevStatus()));
        pojo.setStatus(TranslationKeyDictionary.orderStatuses.get(order.getStatus()));
        pojo.setErrorNo(order.getErrorNo().getNumber());
        pojo.setErrorInfo(Convert.toUTF8(order.getErrorInfoBytes()));
        pojo.setAuto(order.getIsAuto());
        pojo.setClrReqTime(Convert.toDate(order.getClrReqTime()));
        pojo.setClrRepTime(Convert.toDate(order.getClrRepTime()));
        pojo.setTeRepTime(Convert.toDate(order.getTeRepTime()));
        pojo.setFinishTime(Convert.toDate(order.getFinishTime()));
        pojo.setPawnMoney(order.getPawnMoney());
        pojo.setValue(order.getValue());
        pojo.setTerCode(Convert.toUTF8(order.getTerCodeBytes()));
        pojo.setSecurityCode(Convert.toUTF8(order.getSecurityCodeBytes()));
        pojo.setClientCode(Convert.toUTF8(order.getClientCodeBytes()));
        if (pojo.getCommAccountCode().length() > 12) {
            pojo.setClearingFirmCode(pojo.getCommAccountCode().substring(pojo.getCommAccountCode().length() - 12));
        } else if (pojo.getCashAccountCode().length() > 12) {
            pojo.setClearingFirmCode(pojo.getCashAccountCode().substring(pojo.getCashAccountCode().length() - 12));
        } else if (!pojo.getCommAccountCode().isEmpty() || !pojo.getCashAccountCode().isEmpty()) {
            LogManager.getLogger().error(String.format("Не удалось найти счет клиринговой системы для заявки с ID: %s", Long.valueOf(pojo.getId())));
        }
        synchronized (this.subscribers) {
            BooleanProperty isUserDataAvailable = Context.serviceContext.steService.isUserDataAvailable;
            if (!isUserDataAvailable.get()) {
                this.orderDataBuffer.add(pojo);
            } else {
                Iterator<STEMessageConsumer> it = this.subscribers.iterator();
                while (it.hasNext()) {
                    STEMessageConsumer sub = it.next();
                    sub.Handle(pojo);
                }
                tryProcessOrderDataBuffer();
            }
        }
    }

    private void tryProcessOrderDataBuffer() {
        if (this.orderDataBuffer.size() == 0) {
            return;
        }
        Iterator<OrderData> it = this.orderDataBuffer.iterator();
        while (it.hasNext()) {
            OrderData orderData = it.next();
            Iterator<STEMessageConsumer> it2 = this.subscribers.iterator();
            while (it2.hasNext()) {
                STEMessageConsumer sub = it2.next();
                sub.Handle(orderData);
            }
        }
        this.orderDataBuffer.clear();
    }

    public void handleError(byte[] data) throws InvalidProtocolBufferException {
        STEProtos.ErrorInfo error = STEProtos.ErrorInfo.parseFrom(data);
        ErrorData pojo = new ErrorData();
        pojo.setId(error.getId());
        pojo.setCode(Convert.toUTF8(error.getCodeBytes()));
        pojo.setName(Convert.toUTF8(error.getNameBytes()));
        synchronized (this.subscribers) {
            Iterator<STEMessageConsumer> it = this.subscribers.iterator();
            while (it.hasNext()) {
                STEMessageConsumer sub = it.next();
                sub.Handle(pojo);
            }
        }
    }

    public void handleBDState(byte[] data) throws InvalidProtocolBufferException {
        STEProtos.BDStateInfo state = STEProtos.BDStateInfo.parseFrom(data);
        BDStateData pojo = new BDStateData();
        pojo.setId(state.getId());
        pojo.setCode(Convert.toUTF8(state.getCodeBytes()));
        pojo.setName(Convert.toUTF8(state.getNameBytes()));
        pojo.setHandle(Convert.toUTF8(state.getHandleBytes()));
        pojo.setStartTime(Convert.toUTF8(state.getStartTimeBytes()));
        pojo.setActualStartTime(Convert.toUTF8(state.getActualStartTimeBytes()));
        pojo.setFinishTime(Convert.toUTF8(state.getFinishTimeBytes()));
        pojo.setActualFinishTime(Convert.toUTF8(state.getActualFinishTimeBytes()));
        pojo.setStatus(Convert.toUTF8(state.getStatusBytes()));
        pojo.setDate(Convert.toDate(state.getDate(), "yyyy-MM-dd"));
        synchronized (this.subscribers) {
            Iterator<STEMessageConsumer> it = this.subscribers.iterator();
            while (it.hasNext()) {
                STEMessageConsumer sub = it.next();
                sub.Handle(pojo);
            }
        }
    }

    public void handleSection(byte[] data) throws InvalidProtocolBufferException {
        STEProtos.SectionInfo state = STEProtos.SectionInfo.parseFrom(data);
        SectionData pojo = new SectionData();
        pojo.setId(state.getId());
        pojo.setCode(Convert.toUTF8(state.getCodeBytes()));
        pojo.setName(Convert.toUTF8(state.getNameBytes()));
        pojo.setStatus(TranslationKeyDictionary.sectionStatuses.get(state.getStatus()));
        synchronized (this.subscribers) {
            Iterator<STEMessageConsumer> it = this.subscribers.iterator();
            while (it.hasNext()) {
                STEMessageConsumer sub = it.next();
                sub.Handle(pojo);
            }
        }
    }

    public void handleNBOTrade(byte[] data) throws InvalidProtocolBufferException {
        STEProtos.NBOTradeInfo nboTrade = STEProtos.NBOTradeInfo.parseFrom(data);
        NBOTradeData pojo = new NBOTradeData();
        pojo.setId(nboTrade.getId());
        pojo.setCode(Convert.toUTF8(nboTrade.getCodeBytes()));
        pojo.setCreationTime(Convert.toDate(nboTrade.getCreationTime()));
        pojo.setQty(nboTrade.getQty());
        pojo.setPrice(nboTrade.getPrice());
        pojo.setSecurityCode(Convert.toUTF8(nboTrade.getSecurityCodeBytes()));
        pojo.setBuyerFirmCode(Convert.toUTF8(nboTrade.getBuyerFirmCodeBytes()));
        pojo.setBuyerClrFirmCode(Convert.toUTF8(nboTrade.getBuyerClrFirmCodeBytes()));
        pojo.setBuyerClrFirmName(Convert.toUTF8(nboTrade.getBuyerClrFirmNameBytes()));
        pojo.setBuyerClientCode(Convert.toUTF8(nboTrade.getBuyerClientCodeBytes()));
        pojo.setBuyerCommAccountCode(Convert.toUTF8(nboTrade.getBuyerCommAccountCodeBytes()));
        pojo.setBuyerCashAccountCode(Convert.toUTF8(nboTrade.getBuyerCashAccountCodeBytes()));
        pojo.setSellerFirmCode(Convert.toUTF8(nboTrade.getSellerFirmCodeBytes()));
        pojo.setSellerClrFirmCode(Convert.toUTF8(nboTrade.getSellerClrFirmCodeBytes()));
        pojo.setSellerClrFirmName(Convert.toUTF8(nboTrade.getSellerClrFirmNameBytes()));
        pojo.setSellerClientCode(Convert.toUTF8(nboTrade.getSellerClientCodeBytes()));
        pojo.setSellerCommAccountCode(Convert.toUTF8(nboTrade.getSellerCommAccountCodeBytes()));
        pojo.setSellerCashAccountCode(Convert.toUTF8(nboTrade.getSellerCashAccountCodeBytes()));
        pojo.setValue(nboTrade.getValue());
        pojo.setBuyerTerCode(Convert.toUTF8(nboTrade.getBuyerTerCodeBytes()));
        pojo.setSellerTerCode(Convert.toUTF8(nboTrade.getSellerTerCodeBytes()));
        synchronized (this.subscribers) {
            Iterator<STEMessageConsumer> it = this.subscribers.iterator();
            while (it.hasNext()) {
                STEMessageConsumer sub = it.next();
                sub.Handle(pojo);
            }
        }
    }

    public void handleBuyLimits(byte[] data) throws InvalidProtocolBufferException {
        STEProtos.BuyLimitInfo buyLimit = STEProtos.BuyLimitInfo.parseFrom(data);
        BuyLimitData pojo = new BuyLimitData();
        pojo.setCode(Convert.toUTF8(buyLimit.getCodeBytes()));
        pojo.setApCode(Convert.toUTF8(buyLimit.getApCodeBytes()));
        pojo.setTermCode(Convert.toUTF8(buyLimit.getTermCodeBytes()));
        pojo.setInitialAmount(buyLimit.getInitialAmount());
        pojo.setAmount(buyLimit.getAmount());
        pojo.setId(this.buyLimitsIdTracker.id(pojo.getCode()));
        synchronized (this.subscribers) {
            Iterator<STEMessageConsumer> it = this.subscribers.iterator();
            while (it.hasNext()) {
                STEMessageConsumer sub = it.next();
                sub.Handle(pojo);
            }
        }
    }

    public void handleSellLimits(byte[] data) throws InvalidProtocolBufferException {
        STEProtos.SellLimitInfo buyLimit = STEProtos.SellLimitInfo.parseFrom(data);
        SellLimitData pojo = new SellLimitData();
        pojo.setCode(Convert.toUTF8(buyLimit.getCodeBytes()));
        pojo.setApCode(Convert.toUTF8(buyLimit.getApCodeBytes()));
        pojo.setTermCode(Convert.toUTF8(buyLimit.getTermCodeBytes()));
        pojo.setInitialAmount(buyLimit.getInitialAmount());
        pojo.setAmount(buyLimit.getAmount());
        pojo.setSellerCode(Convert.toUTF8(buyLimit.getSellerCodeBytes()));
        pojo.setId(this.sellLimitsIdTracker.id(pojo.getCode()));
        synchronized (this.subscribers) {
            Iterator<STEMessageConsumer> it = this.subscribers.iterator();
            while (it.hasNext()) {
                STEMessageConsumer sub = it.next();
                sub.Handle(pojo);
            }
        }
    }

    public void handleBPLLimits(byte[] data) throws InvalidProtocolBufferException {
        STEProtos.BPLimitInfo buyLimit = STEProtos.BPLimitInfo.parseFrom(data);
        BPLLimitData pojo = new BPLLimitData();
        pojo.setCode(Convert.toUTF8(buyLimit.getCodeBytes()));
        pojo.setBpCode(Convert.toUTF8(buyLimit.getBpCodeBytes()));
        pojo.setTermCode(Convert.toUTF8(buyLimit.getTermCodeBytes()));
        pojo.setInitialAmount(buyLimit.getInitialAmount());
        pojo.setAmount(buyLimit.getAmount());
        pojo.setId(this.bplLimitsIdTracker.id(pojo.getCode()));
        synchronized (this.subscribers) {
            Iterator<STEMessageConsumer> it = this.subscribers.iterator();
            while (it.hasNext()) {
                STEMessageConsumer sub = it.next();
                sub.Handle(pojo);
            }
        }
    }

    public void handleGroupLimits(byte[] data) throws InvalidProtocolBufferException {
        STEProtos.GroupLimitInfo buyLimit = STEProtos.GroupLimitInfo.parseFrom(data);
        GroupLimitData pojo = new GroupLimitData();
        pojo.setCode(Convert.toUTF8(buyLimit.getCodeBytes()));
        pojo.setGroupCode(Convert.toUTF8(buyLimit.getGroupCodeBytes()));
        pojo.setTermCode(Convert.toUTF8(buyLimit.getTermCodeBytes()));
        pojo.setInitialAmount(buyLimit.getInitialAmount());
        pojo.setAmount(buyLimit.getAmount());
        pojo.setId(this.groupLimitsIdTracker.id(pojo.getCode()));
        synchronized (this.subscribers) {
            Iterator<STEMessageConsumer> it = this.subscribers.iterator();
            while (it.hasNext()) {
                STEMessageConsumer sub = it.next();
                sub.Handle(pojo);
            }
        }
    }

    public void handleFirm(byte[] firmData) throws InvalidProtocolBufferException {
        STEProtos.FirmInfo firm = STEProtos.FirmInfo.parseFrom(firmData);
        MemberData pojo = new MemberData();
        pojo.setId(firm.getId());
        pojo.setCode(Convert.toUTF8(firm.getCodeBytes()));
        pojo.setName(Convert.toUTF8(firm.getNameBytes()));
        pojo.setShortName(Convert.toUTF8(firm.getShortNameBytes()));
        pojo.setStatus(TranslationKeyDictionary.statusTypes.get(firm.getStatus()));
        synchronized (this.subscribers) {
            Iterator<STEMessageConsumer> it = this.subscribers.iterator();
            while (it.hasNext()) {
                STEMessageConsumer sub = it.next();
                sub.Handle(pojo);
            }
        }
    }
}
