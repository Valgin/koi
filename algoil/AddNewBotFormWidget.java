package ru.effectivegroup.client.algoil;

import ru.effectivegroup.client.model.BlockWidget;
import ru.effectivegroup.client.model.WidgetCategory;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/AddNewBotFormWidget.class */
public class AddNewBotFormWidget extends BlockWidget {
    public AddNewBotFormWidget() {
        setTitle("Новый бот");
    }

    public String getType() {
        return "NewBot";
    }

    public WidgetCategory getWidgetCategory() {
        return WidgetCategory.STE;
    }

    public double getDefaultWidth() {
        return 800.0d;
    }

    public double getDefaultHeight() {
        return 720.0d;
    }
}
