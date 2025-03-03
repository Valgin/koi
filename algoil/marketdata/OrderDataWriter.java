package ru.effectivegroup.client.algoil.marketdata;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.effectivegroup.client.algoil.adapter.DefaultSTEMessageConsumer;
import ru.effectivegroup.client.algoil.adapter.IncomingMessagesAdapter;
import ru.effectivegroup.client.algoil.settings.SettingsManager;
import ru.effectivegroup.client.model.data.ste.OrderData;
import ru.effectivegroup.client.model.data.ste.dictionary.BSType;
import ru.effectivegroup.client.model.data.ste.dictionary.OrderStatus;
import ru.effectivegroup.client.service.table.writer.TableWriter;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/marketdata/OrderDataWriter.class */
public class OrderDataWriter extends DefaultSTEMessageConsumer {
    private final Logger logger = LogManager.getLogger("algoil");
    private SettingsManager settingsManager;

    public OrderDataWriter(IncomingMessagesAdapter adapter, SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        adapter.Subscribe(this);
        clearFile();
    }

    @Override // ru.effectivegroup.client.algoil.adapter.DefaultSTEMessageConsumer, ru.effectivegroup.client.algoil.adapter.STEMessageConsumer
    public void Handle(OrderData message) {
        try {
            FileWriter writer = new FileWriter(getFilePath().toString(), StandardCharsets.UTF_8, true);
            try {
                writer.write(String.format("%s;%s;%s;%s;%d;%d;%d;%s;\n", message.code, message.securityCode, message.buySell, message.status, Long.valueOf(message.qty), Integer.valueOf(message.qtyLeft), Long.valueOf(message.price), message.cashAccountCode));
                writer.flush();
                writer.close();
            } finally {
            }
        } catch (IOException e) {
            this.logger.error("Ошибка записи OrderData на диск", e);
        }
    }

    public static List<OrderData> LoadOrderData(String filePath) {
        List<OrderData> result = new ArrayList<>();
        try {
            FileReader fReader = new FileReader(filePath);
            try {
                BufferedReader reader = new BufferedReader(fReader);
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    OrderData od = createOrderData(line);
                    result.add(od);
                }
                fReader.close();
            } finally {
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    private static OrderData createOrderData(String line) {
        String[] parts = line.split(TableWriter.DELIMITER);
        OrderData od = new OrderData();
        od.setCode(parts[0]);
        od.setSecurityCode(parts[1]);
        od.setBuySell(BSType.valueOf(parts[2]));
        od.setStatus(OrderStatus.valueOf(parts[3]));
        od.setQty(Integer.parseInt(parts[4]));
        od.setQtyLeft(Integer.parseInt(parts[5]));
        od.setPrice(Long.parseLong(parts[6]));
        if (parts.length > 7) {
            od.setCashAccountCode(parts[7]);
        }
        return od;
    }

    private void clearFile() {
        try {
            Files.deleteIfExists(getFilePath());
        } catch (IOException e) {
        }
    }

    private Path getFilePath() {
        return Paths.get(this.settingsManager.getSettingDirectory(), "orderdata.txt");
    }
}
