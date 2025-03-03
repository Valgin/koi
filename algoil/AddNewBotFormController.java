package ru.effectivegroup.client.algoil;

import java.net.URL;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.effectivegroup.client.algoil.bots.BotManager;
import ru.effectivegroup.client.algoil.marketdata.DataFeed;
import ru.effectivegroup.client.algoil.marketdata.DataFeedMessageConsumer;
import ru.effectivegroup.client.context.Context;
import ru.effectivegroup.client.gui.widget.WidgetComponent;
import ru.effectivegroup.client.model.widget.WidgetFactory;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/AddNewBotFormController.class */
public class AddNewBotFormController implements DataFeedMessageConsumer, WidgetComponent<AddNewBotFormWidget> {
    private static final Logger logger = LogManager.getLogger("algoil");
    private Instrument selectedItem;
    private ObservableList<Instrument> instruments;

    @FXML
    private Button cancelButton;

    @FXML
    private Button createButton;
    private BotManager botManager;
    private AddNewBotFormWidget widget;

    @FXML
    private TextField searchField = new TextField();

    @FXML
    public ListView<Instrument> InstrumentsList = new ListView<>();

    public AddNewBotFormController() {
    }

    public BotManager getBotManager() {
        return this.botManager;
    }

    public AddNewBotFormController(BotManager botManager) {
        this.botManager = botManager;
    }

    public URL getFXMLResourcePath() {
        return AddNewBotFormController.class.getResource("AddNewAlgoForm.fxml");
    }

    public void onCreate(AddNewBotFormWidget addNewBotFormWidget) {
        this.widget = addNewBotFormWidget;
    }

    @FXML
    public void initialize() {
        this.botManager = AlgoilApp.getInstance().getBotManager();
        AlgoilApp.getInstance().getDataFeed().Subscribe(null, this, DataFeed.DataFeedEventType.NewInstrument);
        this.InstrumentsList.setCellFactory(param -> {
            return new AddNewBotFormListViewCell();
        });
        this.instruments = FXCollections.observableArrayList(AlgoilApp.getInstance().getDataFeed().getInstruments().stream().sorted().toList());
        this.instruments.removeIf(instrument -> {
            return this.botManager.ContainsBotListByInstrument(instrument);
        });
        this.InstrumentsList.setItems(this.instruments);
        this.searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            search(newValue);
        });
    }

    private void search(String text) {
        ObservableList<Instrument> instruments = FXCollections.observableArrayList(AlgoilApp.getInstance().getDataFeed().getInstruments().stream().sorted().toList());
        if (!Objects.equals(text, "")) {
            Predicate<Instrument> searchPredicate = instrument -> {
                return instrument.getFullName().toLowerCase().contains(text.toLowerCase().trim());
            };
            instruments = FXCollections.observableArrayList(instruments.stream().filter(searchPredicate).sorted().toList());
        }
        instruments.removeIf(instrument2 -> {
            return this.botManager.ContainsBotListByInstrument(instrument2);
        });
        this.InstrumentsList.setItems(instruments);
    }

    @FXML
    public void createButtonAction() {
        this.selectedItem = (Instrument) this.InstrumentsList.getSelectionModel().getSelectedItem();
        this.botManager.AddBot(this.selectedItem, null, null);
        this.instruments.remove(this.selectedItem);
    }

    @Override // ru.effectivegroup.client.algoil.marketdata.DataFeedMessageConsumer
    public void Handle(Instrument instrument) {
        if (instrument == null || this.instruments.contains(instrument) || this.botManager.ContainsBotListByInstrument(instrument)) {
            return;
        }
        Platform.runLater(() -> {
            this.instruments.add(instrument);
            this.InstrumentsList.getItems().sort(Comparator.comparing((v0) -> {
                return v0.getFullName();
            }));
        });
    }

    @FXML
    public void cancelButtonAction() {
        Context.uiContext.widgetManager.close(this.widget);
    }

    public Instrument getSelectedItem() {
        return this.selectedItem;
    }

    public static void AddAlgo() {
        AddNewBotFormWidget widget = WidgetFactory.create(AddNewBotFormWidget.class);
        Stage desktop = Context.uiContext.mainWindowManager.getMainStage();
        Context.uiContext.widgetManager.open(widget, Context.uiContext.desktopWindowRegister.getDesktopWindow(desktop).getActiveDesktop());
    }
}
