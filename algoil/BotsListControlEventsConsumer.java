package ru.effectivegroup.client.algoil;

import ru.effectivegroup.client.algoil.bots.Bot;

/* compiled from: BotsListControl.java */
/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/BotsListControlEventsConsumer.class */
interface BotsListControlEventsConsumer {
    void Handle(Bot bot);

    void Handle(Integer num);
}
