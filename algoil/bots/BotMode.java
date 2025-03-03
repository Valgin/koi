package ru.effectivegroup.client.algoil.bots;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/bots/BotMode.class */
public enum BotMode {
    Overlap,
    TurboOverlap,
    Catch
}
