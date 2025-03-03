package ru.effectivegroup.client.algoil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import ru.effectivegroup.client.context.Context;
import ru.effectivegroup.client.model.data.ste.OrderData;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/OrderCounter.class */
public class OrderCounter {
    private static final OrderCounter _instance = new OrderCounter();
    private final Object syncRoot = new Object();
    private final HashMap<String, Integer> ordersCountByInstrument = new HashMap<>();
    private final HashMap<String, HashSet<String>> ordersIdsByInstrument = new HashMap<>();
    private final HashSet<String> preProcessedInstrumentCodes = new HashSet<>();

    public static OrderCounter getInstance() {
        return _instance;
    }

    public int getOrdersCount(String securityCode) {
        int result;
        synchronized (this.syncRoot) {
            if (this.preProcessedInstrumentCodes.contains(securityCode)) {
                if (this.ordersCountByInstrument.get(securityCode) == null) {
                    result = 0;
                } else {
                    result = this.ordersCountByInstrument.get(securityCode).intValue();
                }
            } else if (this.ordersIdsByInstrument.get(securityCode) == null) {
                result = 0;
            } else {
                result = this.ordersIdsByInstrument.get(securityCode).size();
            }
        }
        return result;
    }

    public void IncreaseOrderCount(String securityCode) {
        synchronized (this.syncRoot) {
            Integer instrumentIds = this.ordersCountByInstrument.computeIfAbsent(securityCode, k -> {
                return 0;
            });
            this.ordersCountByInstrument.put(securityCode, Integer.valueOf(instrumentIds.intValue() + 1));
        }
    }

    public void Handle(Instrument instrument, OrderData data) {
        if (Objects.equals(data.getTrn(), "") || !data.getUserCode().equals(Context.serviceContext.steService.getUserCode()) || this.preProcessedInstrumentCodes.contains(instrument.getCode())) {
            return;
        }
        HashSet<String> instrumentIds = this.ordersIdsByInstrument.computeIfAbsent(instrument.getCode(), k -> {
            return new HashSet();
        });
        instrumentIds.add(data.getTrn());
    }

    public void addPreprocessedInstrument(String securityCode) {
        int preProcessFinalValue;
        if (this.preProcessedInstrumentCodes.contains(securityCode)) {
            return;
        }
        this.preProcessedInstrumentCodes.add(securityCode);
        if (this.ordersIdsByInstrument.containsKey(securityCode)) {
            preProcessFinalValue = this.ordersIdsByInstrument.get(securityCode).size();
        } else {
            preProcessFinalValue = 0;
        }
        this.ordersCountByInstrument.putIfAbsent(securityCode, Integer.valueOf(preProcessFinalValue));
    }
}
