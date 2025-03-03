package ru.effectivegroup.client.algoil.settings;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/settings/SettingsManager.class */
public interface SettingsManager {
    String getSettingDirectory();

    void SaveSettings(Object obj);

    Object GetSettings(Class cls);
}
