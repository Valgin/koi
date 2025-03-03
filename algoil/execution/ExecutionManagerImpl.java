package ru.effectivegroup.client.algoil.execution;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.effectivegroup.client.algoil.OrderCounter;
import ru.effectivegroup.client.algoil.Utils;
import ru.effectivegroup.client.algoil.adapter.DefaultSTEMessageConsumer;
import ru.effectivegroup.client.algoil.adapter.IncomingMessagesAdapter;
import ru.effectivegroup.client.algoil.execution.Transaction.TransactionManager;
import ru.effectivegroup.client.context.Context;
import ru.effectivegroup.client.model.data.ste.AccountData;
import ru.effectivegroup.client.model.data.ste.ClientData;
import ru.effectivegroup.client.model.data.ste.HoldingData;
import ru.effectivegroup.client.model.data.ste.OrderData;
import ru.effectivegroup.client.model.data.ste.OrderInfo;
import ru.effectivegroup.client.model.data.ste.UserData;
import ru.effectivegroup.client.model.data.ste.dictionary.AccountType;
import ru.effectivegroup.client.model.data.ste.dictionary.BSType;
import ru.effectivegroup.client.model.data.ste.dictionary.OrderMode;
import ru.effectivegroup.client.model.data.ste.dictionary.OrderStatus;
import ru.effectivegroup.client.model.data.ste.dictionary.OrderSubType;
import ru.effectivegroup.client.model.data.ste.dictionary.OrderType;
import ru.effectivegroup.client.model.data.ste.dictionary.OrderViewType;
import ru.effectivegroup.client.model.widget.impl.Order;
import ru.effectivegroup.client.network.message.STEProtos;
import ru.effectivegroup.client.service.EnterOrderService;
import ru.effectivegroup.client.service.ste.order.cancel.CancelOrderService;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/execution/ExecutionManagerImpl.class */
public class ExecutionManagerImpl extends DefaultSTEMessageConsumer implements ExecutionManager {
    private UserData userData;
    private String userCode;
    private String firmCode;
    private final Logger logger = LogManager.getLogger("algoil");
    private final Object syncRoot = new Object();
    private final EnterOrderService enterOrderService = Context.serviceContext.enterOrderService;
    private final CancelOrderService cancelOrderService = Context.serviceContext.cancelOrderService;
    private final HashMap<String, OrderData> mapOrderIdOnOrderData = new HashMap<>();
    private final HashMap<String, String> mapAccountOnClient = new HashMap<>();
    private final HashMap<String, OrderEmitter> mapTrnIdOnEmitter = new HashMap<>();
    private List<String> commAccounts = new ArrayList();
    private List<String> moneyAccounts = new ArrayList();
    private List<ClientData> clients = new ArrayList();
    private HashSet<ExecutionManagerEventsConsumer> subscribers = new HashSet<>();
    private final TransactionManager transactionManager = TransactionManager.getInstance();
    private final OrderCounter orderCounter = OrderCounter.getInstance();

    public ExecutionManagerImpl(IncomingMessagesAdapter adapter) {
        adapter.Subscribe(this);
    }

    private static OrderData fillOrderData(String securityCode, BSType operation, long quantity, long price, String moneyAccount, String commAccount, String firmCode, String userCode, String clientCode, String transId) {
        OrderData orderData = new OrderData();
        orderData.setSecurityCode(securityCode);
        orderData.setCashAccountCode(moneyAccount);
        orderData.setCommAccountCode(commAccount);
        orderData.setFirmCode(firmCode);
        orderData.setUserCode(userCode);
        orderData.setClientCode(clientCode);
        orderData.setTrn(transId);
        orderData.setId(0L);
        orderData.setVolumePhysicalMeasure(0L);
        orderData.setType(OrderType.LIMITED);
        orderData.setView(OrderViewType.NOT_NEGOTIATED);
        orderData.setSubType(OrderSubType.EORD);
        orderData.setMode(OrderMode.PIQ);
        orderData.setBuySell(operation);
        orderData.setQty(quantity);
        orderData.setPrice(price);
        orderData.setAuto(false);
        return orderData;
    }

    @Override // ru.effectivegroup.client.algoil.adapter.DefaultSTEMessageConsumer, ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(STEProtos.RepOrderOK message) {
        this.logger.debug("RepOrderOK code={} id={}", message.getOrderCode(), Long.valueOf(message.getItemId()));
    }

    @Override // ru.effectivegroup.client.algoil.adapter.DefaultSTEMessageConsumer, ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(STEProtos.RepOrderBAD message) {
        this.logger.debug("RepOrderBAD code={} id={}", message.getOrderCode(), Long.valueOf(message.getItemId()));
    }

    @Override // ru.effectivegroup.client.algoil.adapter.DefaultSTEMessageConsumer, ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(ClientData message) {
        synchronized (this.syncRoot) {
            this.clients.add(message);
        }
        for (ExecutionManagerEventsConsumer cons : subscribersCopy()) {
            cons.Handle(message);
        }
    }

    @Override // ru.effectivegroup.client.algoil.adapter.DefaultSTEMessageConsumer, ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(UserData userData) {
        this.userData = userData;
    }

    @Override // ru.effectivegroup.client.algoil.adapter.DefaultSTEMessageConsumer, ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(AccountData message) {
        boolean newAccount = false;
        if (message.getType().equals(AccountType.CASH)) {
            synchronized (this.syncRoot) {
                newAccount = this.moneyAccounts.add(message.code);
            }
        } else if (message.getType().equals(AccountType.COMM)) {
            synchronized (this.syncRoot) {
                newAccount = this.commAccounts.add(message.code);
            }
        } else {
            this.logger.debug("Получено необрабатываемое сообщение типа AccountData");
        }
        if (newAccount) {
            synchronized (this.syncRoot) {
                this.mapAccountOnClient.put(message.code, message.clientCode);
            }
            for (ExecutionManagerEventsConsumer cons : subscribersCopy()) {
                cons.Handle(message);
            }
        }
    }

    @Override // ru.effectivegroup.client.algoil.adapter.DefaultSTEMessageConsumer, ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(OrderData orderData) {
        if (Utils.IsNullOrEmpty(orderData.getCashAccountCode())) {
            return;
        }
        traceOrderStateChangeReceived(orderData);
        String orderId = orderData.getCode();
        if (Utils.IsNullOrEmpty(orderId)) {
            this.logger.warn("OrderData без orderId: id={} sec={}", Long.valueOf(orderData.getId()), orderData.getSecurityCode());
            return;
        }
        OrderEmitter emitter = tryGetEmitter(orderId);
        if (emitter == null) {
            this.logger.warn("Не найден emitter для OrderData: id={} sec={} orderId={}", Long.valueOf(orderData.getId()), orderData.getSecurityCode(), orderId);
        } else {
            emitter.HandleOrderStateChange(orderData);
        }
    }

    @Override // ru.effectivegroup.client.algoil.adapter.DefaultSTEMessageConsumer, ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(HoldingData message) {
        for (ExecutionManagerEventsConsumer cons : subscribersCopy()) {
            cons.Handle(message);
        }
    }

    private OrderEmitter tryGetEmitter(String trnId) {
        synchronized (this.syncRoot) {
            if (this.mapTrnIdOnEmitter.containsKey(trnId)) {
                return this.mapTrnIdOnEmitter.get(trnId);
            }
            return null;
        }
    }

    private String getUserCode() {
        if (this.userCode == null) {
            initFirmAndUser();
        }
        return this.userCode;
    }

    private String getFirmCode() {
        if (this.firmCode == null) {
            initFirmAndUser();
        }
        return this.firmCode;
    }

    @Override // ru.effectivegroup.client.algoil.execution.ExecutionManager
    public PlaceOrderResult PlaceOrder(String securityCode, BSType operation, long quantity, long price, String moneyAccount, String commAccount, OrderEmitter emitter) {
        String clientCode = getClientCode(moneyAccount);
        this.logger.debug("Placing order: sec={} oper={} price={} Q={} macc={} cacc={} firm={} user={} client={}", securityCode, operation, Long.valueOf(price), Long.valueOf(quantity), moneyAccount, commAccount, getFirmCode(), getUserCode(), clientCode);
        String trnId = "";
        OrderData orderData = fillOrderData(securityCode, operation, quantity, 100 * price, moneyAccount, commAccount, getFirmCode(), getUserCode(), clientCode, "");
        PlaceOrderResult[] result = new PlaceOrderResult[1];
        FutureTask<String> awaiter = new FutureTask<>(() -> {
            return "";
        });
        this.transactionManager.addPlaceOrderAction();
        this.enterOrderService.send(orderData, Order.Source.WIDGET_MENU, response -> {
            result[0] = newOrderTransactionCallback(orderData, response);
            if (result[0].isSuccessful()) {
                synchronized (this.syncRoot) {
                    this.logger.debug("Кладём заявку в mapTrnIdOnEmitter trnId={} orderCode={} orderId={}", trnId, result[0].getOrder().getCode(), result[0].getOrder().getId());
                    this.mapTrnIdOnEmitter.put(result[0].getOrder().getCode(), emitter);
                    this.mapTrnIdOnEmitter.put(result[0].getOrder().getId().toString(), emitter);
                }
            }
            awaiter.run();
        }, null);
        this.orderCounter.addPreprocessedInstrument(securityCode);
        this.orderCounter.IncreaseOrderCount(securityCode);
        try {
            awaiter.get();
            return result[0];
        } catch (InterruptedException e) {
            this.logger.error(e);
            throw new RuntimeException(e);
        } catch (ExecutionException e2) {
            this.logger.error(e2);
            throw new RuntimeException(e2);
        }
    }

    @Override // ru.effectivegroup.client.algoil.execution.ExecutionManager
    public CancelOrderResult CancelOrder(Order order) {
        this.logger.debug("Cancelling order: id={}", order.getCode());
        synchronized (this.syncRoot) {
            if (!this.mapOrderIdOnOrderData.containsKey(order.getCode())) {
                return CancelOrderResult.CreateFailed("UNKNOWN_ORDER", "Попытка снять неизвестную заявку");
            }
            OrderData orderData = (OrderData) Context.repositoryContext.get(OrderData.class).get(order.getId().longValue());
            if (orderData == null) {
                this.logger.info("OrderData not found: code={} id={}", order.getCode(), order.getId());
                orderData = waitAndGetOrderData(order.getId());
                if (orderData == null) {
                    return CancelOrderResult.CreateFailed("NO_ORDER_DATA", "Не смогли получить OrderData");
                }
            }
            if (orderData.getStatus() == OrderStatus.CANCELED || orderData.getStatus() == OrderStatus.MATCHED) {
                this.logger.info("OrderData status in {}", orderData.getStatus());
            } else {
                this.logger.info("Cancelling orderData: code={} id={} status={}", orderData.getCode(), Long.valueOf(orderData.getId()), orderData.getStatus());
                this.cancelOrderService.cancel((OrderInfo) orderData);
            }
            return CancelOrderResult.CreateSuccessful();
        }
    }

    private OrderData waitAndGetOrderData(Long id) {
        for (int i = 0; i < 1000; i++) {
            OrderData orderData = (OrderData) Context.repositoryContext.get(OrderData.class).get(id.longValue());
            if (orderData != null) {
                return orderData;
            }
            sleep(5L);
        }
        this.logger.info("Can't find OrderData with id {}", id);
        return null;
    }

    private void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            this.logger.info("Error occurred while waiting for OrderData");
        }
    }

    @Override // ru.effectivegroup.client.algoil.execution.ExecutionManager
    public void Subscribe(ExecutionManagerEventsConsumer consumer) {
        synchronized (this.syncRoot) {
            this.subscribers.add(consumer);
        }
    }

    @Override // ru.effectivegroup.client.algoil.execution.ExecutionManager
    public List<String> getCommAccounts() {
        ArrayList arrayList;
        synchronized (this.syncRoot) {
            if (this.commAccounts.isEmpty()) {
                for (AccountData data : Context.repositoryContext.get(AccountData.class).getData()) {
                    if (data.getType().equals(AccountType.COMM)) {
                        this.commAccounts.add(data.code);
                    }
                }
            }
            arrayList = new ArrayList(this.commAccounts);
        }
        return arrayList;
    }

    @Override // ru.effectivegroup.client.algoil.execution.ExecutionManager
    public List<String> getMoneyAccounts() {
        ArrayList arrayList;
        synchronized (this.syncRoot) {
            if (this.moneyAccounts.isEmpty()) {
                for (AccountData data : Context.repositoryContext.get(AccountData.class).getData()) {
                    if (data.getType().equals(AccountType.CASH)) {
                        this.moneyAccounts.add(data.code);
                    }
                }
            }
            arrayList = new ArrayList(this.moneyAccounts);
        }
        return arrayList;
    }

    @Override // ru.effectivegroup.client.algoil.execution.ExecutionManager
    public List<ClientData> getClients() {
        ArrayList arrayList;
        synchronized (this.syncRoot) {
            if (this.clients.size() == 0) {
                this.clients.addAll(Context.repositoryContext.get(ClientData.class).getData());
            }
            arrayList = new ArrayList(this.clients);
        }
        return arrayList;
    }

    private void sendKillOrderTransaction(String orderId) {
        OrderInfo orderData = new OrderData();
        orderData.setCode(orderId);
        this.cancelOrderService.cancel(orderData);
    }

    private PlaceOrderResult newOrderTransactionCallback(OrderData orderData, AbstractMessage abstractMessage) {
        STEProtos.Envelope message = (STEProtos.Envelope) abstractMessage;
        if (message.getTypeCount() > 1) {
            throw new IllegalStateException("В ответ на постановку заявки получено %d сообщений, ожидается одно".formatted(Integer.valueOf(message.getTypeCount())));
        }
        try {
            STEProtos.MessageType type = message.getType(0);
            if (type.equals(STEProtos.MessageType.REP_OK)) {
                STEProtos.RepOrderOK msgOK = STEProtos.RepOrderOK.parseFrom(message.getData(0));
                this.logger.info("REP_OK trn_id=%s order_code=%s status=%s qty=%d qtyLeft=%d itemId=%d ".formatted(orderData.getTrn(), msgOK.getOrderCode(), msgOK.getStatus(), Integer.valueOf(msgOK.getQty()), Integer.valueOf(msgOK.getQtyLeft()), Long.valueOf(msgOK.getItemId())));
                PlaceOrderResult placeOrderResult = PlaceOrderResult.CreateSuccessful(msgOK.getOrderCode(), msgOK.getStatus(), msgOK.getQty(), msgOK.getQtyLeft(), orderData);
                synchronized (this.syncRoot) {
                    this.mapOrderIdOnOrderData.put(placeOrderResult.getOrder().getCode(), orderData);
                }
                return placeOrderResult;
            }
            if (type.equals(STEProtos.MessageType.REP_ERROR)) {
                STEProtos.RepError msgErr = STEProtos.RepError.parseFrom(message.getData(0));
                this.logger.info("REP_ERROR trn_id=%s type=%d info=%s".formatted(orderData.getTrn(), Integer.valueOf(msgErr.getType()), msgErr.getInfo()));
                return PlaceOrderResult.CreateFailed(msgErr.getType(), msgErr.getInfo());
            }
            this.logger.error("trn_id=%s В ответ на постановку заявки получено сообщение неизвестного типа: %s".formatted(orderData.getTrn(), type));
            return PlaceOrderResult.CreateFailed(0, "UNKNOWN_RESPONSE_TYPE");
        } catch (InvalidProtocolBufferException exception) {
            throw new RuntimeException((Throwable) exception);
        }
    }

    private CancelOrderResult cancelOrderTransactionCallback(OrderData orderData, AbstractMessage abstractMessage) {
        this.logger.debug("cancelOrderTransactionCallback");
        STEProtos.Envelope message = (STEProtos.Envelope) abstractMessage;
        if (message.getTypeCount() > 1) {
            return CancelOrderResult.CreateFailed("MSG_COUNT_EXCEED", String.format("В ответ на снятие заявки получено %d сообщений, ожидается одно", Integer.valueOf(message.getTypeCount())));
        }
        try {
            STEProtos.MessageType type = message.getType(0);
            this.logger.debug("cancelOrderTransactionCallback msgType = {}", type);
            if (type.equals(STEProtos.MessageType.REP_OK)) {
                STEProtos.RepOrderOK msgOK = STEProtos.RepOrderOK.parseFrom(message.getData(0));
                this.logger.info("REP_OK trn_id=%s order_code=%s status=%s".formatted(orderData.getTrn(), msgOK.getOrderCode(), msgOK.getStatus()));
                CancelOrderResult cancelOrderResult = CancelOrderResult.CreateSuccessful(msgOK.getOrderCode(), msgOK.getStatus(), msgOK.getQty(), msgOK.getQtyLeft());
                return cancelOrderResult;
            }
            if (type.equals(STEProtos.MessageType.REP_ERROR)) {
                STEProtos.RepError msgErr = STEProtos.RepError.parseFrom(message.getData(0));
                this.logger.info("REP_ERROR trn_id=%s info=%s".formatted(orderData.getTrn(), msgErr.getInfo()));
                return CancelOrderResult.CreateFailed(String.valueOf(msgErr.getType()), msgErr.getInfo());
            }
            this.logger.error("trn_id=%s В ответ на постановку заявки получено сообщение неизвестного типа: %s".formatted(orderData.getTrn(), type));
            return CancelOrderResult.CreateFailed("UNKNOWN_RESPONSE_TYPE", type.toString());
        } catch (InvalidProtocolBufferException exception) {
            this.logger.fatal(exception);
            throw new RuntimeException((Throwable) exception);
        }
    }

    private List<ExecutionManagerEventsConsumer> subscribersCopy() {
        ArrayList arrayList;
        synchronized (this.syncRoot) {
            arrayList = new ArrayList(this.subscribers);
        }
        return arrayList;
    }

    private void initFirmAndUser() {
        this.userCode = Context.serviceContext.steService.getUserData().getCode();
        this.firmCode = Context.serviceContext.steService.getUserData().getFirm().getCode();
        this.logger.info("initFirmAndUser userCode = {}, firmCode = {}", this.userCode, this.firmCode);
    }

    private String getClientCode(String account) {
        String orDefault;
        synchronized (this.syncRoot) {
            orDefault = this.mapAccountOnClient.getOrDefault(account, "");
        }
        return orDefault;
    }

    private void traceOrderStateChangeReceived(OrderData orderData) {
        this.logger.info("OSC: id={} trnId={} inst={} state={} qty={} qtyleft={} price={} cash={} comm={}", Long.valueOf(orderData.getId()), orderData.getTrn(), orderData.getSecurityCode(), orderData.getStatus(), Long.valueOf(orderData.getQty()), Integer.valueOf(orderData.getQtyLeft()), Long.valueOf(orderData.getPrice()), orderData.getCashAccountCode(), orderData.getCommAccountCode());
    }
}
