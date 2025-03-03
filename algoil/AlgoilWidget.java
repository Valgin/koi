package ru.effectivegroup.client.algoil;

import ru.effectivegroup.client.model.Widget;
import ru.effectivegroup.client.model.WidgetCategory;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/AlgoilWidget.class */
public class AlgoilWidget extends Widget {
    public static final String type = "Algoil";

    public AlgoilWidget() {
        setTitle(type);
    }

    public String getType() {
        return type;
    }

    public WidgetCategory getWidgetCategory() {
        return WidgetCategory.STE;
    }

    public double getDefaultWidth() {
        return 1600.0d;
    }

    public double getDefaultHeight() {
        return 800.0d;
    }
}
