package ru.effectivegroup.client.algoil;

import java.io.IOException;
import java.util.List;
import ru.effectivegroup.client.algoil.bots.BotManager;
import ru.effectivegroup.client.algoil.settings.AlgoilSettingsManager;
import ru.effectivegroup.client.algoil.settings.BotSettings;
import ru.effectivegroup.client.algoil.settings.SettingsManager;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/Program.class */
public class Program {
    public static void main(String[] args) throws IOException {
        SettingsManager sm = new AlgoilSettingsManager();
        List<BotSettings> bots = (List) sm.GetSettings(BotManager.class);
        sm.SaveSettings(bots);
        System.out.println("done");
    }
}
