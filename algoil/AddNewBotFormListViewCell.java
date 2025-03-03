package ru.effectivegroup.client.algoil;

import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/AddNewBotFormListViewCell.class */
public class AddNewBotFormListViewCell extends ListCell<Instrument> {
    HBox hbox = new HBox();
    private final Logger logger = LogManager.getLogger("algoil");
    Pane pane = new Pane();
    boolean active = false;
    private static final String GREEN_STYLE = "-fx-background-color: #22cc22dd";
    private static final String RED_STYLE = "-fx-background-color: #cc2200ff";
    private static final String GRAY_STYLE = "-fx-background-color: #888888ff";
    private Instrument instrument;

    /* JADX INFO: Access modifiers changed from: protected */
    public void updateItem(Instrument instrument, boolean empty) {
        super.updateItem(instrument, empty);
        if (this.instrument == null) {
            this.instrument = instrument;
        }
        if (empty) {
            setText(null);
        } else {
            setText(String.format("%s", instrument.getFullName()));
        }
    }
}
