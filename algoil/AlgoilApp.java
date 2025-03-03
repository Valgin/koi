package ru.effectivegroup.client.algoil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.effectivegroup.client.algoil.adapter.IncomingMessagesAdapter;
import ru.effectivegroup.client.algoil.adapter.STEIncomingMessagesAdapter;
import ru.effectivegroup.client.algoil.bots.BotManager;
import ru.effectivegroup.client.algoil.execution.ExecutionManager;
import ru.effectivegroup.client.algoil.execution.ExecutionManagerImpl;
import ru.effectivegroup.client.algoil.execution.Transaction.TransactionManager;
import ru.effectivegroup.client.algoil.marketdata.DataFeed;
import ru.effectivegroup.client.algoil.marketdata.OrderDataWriter;
import ru.effectivegroup.client.algoil.settings.AlgoilSettingsManager;
import ru.effectivegroup.client.algoil.settings.BotPaneSettingsManager;
import ru.effectivegroup.client.algoil.settings.SettingsManager;
import ru.effectivegroup.client.algoil.settings.TransactionQueueSettingsManager;
import ru.effectivegroup.client.context.Context;
import ru.effectivegroup.client.service.table.writer.TableWriter;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/AlgoilApp.class */
public class AlgoilApp {
    private static AlgoilApp _instance;
    private final Logger logger = LogManager.getLogger("algoil");
    private final IncomingMessagesAdapter adapter;
    private final DataFeed dataFeed;
    private final ExecutionManager executionManager;
    private final BotPaneSettingsManager botPaneManager;
    private final TransactionQueueSettingsManager transactionQueueSettingsManager;
    private final SettingsManager settingsManager;
    private final BotManager botManager;
    private final OrderDataWriter orderDataWriter;
    private ExchangeTimeProvider timeProvider;
    private final TransactionManager transactionManager;
    private BotPane botPane;

    private AlgoilApp() {
        this.logger.info("Создание AlgoilApp");
        this.logger.info("Версия проекта от {}", getBuildTimeStamp());
        this.settingsManager = new AlgoilSettingsManager();
        this.adapter = new STEIncomingMessagesAdapter();
        this.dataFeed = new DataFeed(this.adapter);
        this.executionManager = new ExecutionManagerImpl(this.adapter);
        this.orderDataWriter = new OrderDataWriter(this.adapter, this.settingsManager);
        this.timeProvider = new ExchangeTimeProvider();
        this.botManager = new BotManager(this.settingsManager, this.dataFeed, this.executionManager, this.timeProvider);
        this.botPaneManager = new BotPaneSettingsManager(this.settingsManager);
        this.transactionQueueSettingsManager = new TransactionQueueSettingsManager(this.settingsManager);
        this.transactionManager = TransactionManager.getInstance();
        this.transactionManager.restoreSettings(this.transactionQueueSettingsManager);
        this.dataFeed.Init();
        this.logger.info("AlgoilApp создан");
    }

    public static void Init() {
        _instance = new AlgoilApp();
        Context.uiContext.mainWindowManager.getMainStage().setOnHiding(event -> {
            LogManager.getLogger("algoil").info("Приложение закрыто");
            getInstance().Stop();
        });
    }

    public void Stop() {
        this.logger.info("Остановка AlgoilApp");
        this.timeProvider.Stop();
        this.adapter.Stop();
        this.botManager.StopProcessingAll();
        _instance = null;
    }

    private String getBuildTimeStamp() {
        String buildTimeStamp = "";
        try {
            String classPathVariable = System.getProperty("java.class.path");
            File classPath = new File(classPathVariable.split(TableWriter.DELIMITER)[0]);
            File buildTxtPath = new File(classPath.getParentFile().getParentFile() + "\\runtime\\build.txt");
            buildTimeStamp = Files.readString(buildTxtPath.toPath());
        } catch (IOException e) {
            LogManager.getLogger("algoil").info("Не удалось загрузить время сборки");
        }
        return buildTimeStamp;
    }

    public static AlgoilApp getInstance() {
        return _instance;
    }

    public DataFeed getDataFeed() {
        return this.dataFeed;
    }

    public ExecutionManager getExecutionManager() {
        return this.executionManager;
    }

    public BotManager getBotManager() {
        return this.botManager;
    }

    public BotPane getBotPane() {
        return this.botPane;
    }

    public SettingsManager getSettingsManager() {
        return this.settingsManager;
    }

    public BotPaneSettingsManager getBotPaneManager() {
        return this.botPaneManager;
    }

    public void setBotPane(BotPane botPane) {
        this.botPane = botPane;
    }

    public TransactionQueueSettingsManager getTransactionQueueSettingsManager() {
        return this.transactionQueueSettingsManager;
    }

    public TransactionManager getTransactionManager() {
        return this.transactionManager;
    }
}
