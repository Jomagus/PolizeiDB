package PolizeiPackage.Ansichten;

import PolizeiPackage.DatenbankHandler;
import PolizeiPackage.InfoErrorManager;
import PolizeiPackage.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Liefert Tabelle fuer die Art
 */
public class BezirkAnsichtManager {

    private DatenbankHandler DH;
    private InfoErrorManager IM;
    private Main Hauptprogramm;
    private TableView<BezirkDaten> Tabelle;
    private BorderPane DatenAnsicht;
    private ObservableList<BezirkDaten> BezirkDatenListe;
    private boolean BezirkAnsichtGeneriert;

    public BezirkAnsichtManager(DatenbankHandler DBH, InfoErrorManager IEM, Main HauptFenster) {
        DH = DBH;
        IM = IEM;
        Hauptprogramm = HauptFenster;
        BezirkDatenListe = FXCollections.observableArrayList();
        Tabelle = new TableView<>();
        BezirkAnsichtGeneriert = false;
    }

    public Node getBezirkAnsicht() {
        if (BezirkAnsichtGeneriert) {
            refreshBezirkAnsicht();
            return DatenAnsicht;
        }
        IM.setInfoText("Lade Bezirk Ansicht");
        DatenAnsicht = new BorderPane(getBezirkAnsichtInnereTabelle());

        HBox ButtonLeiste = new HBox(10);
        ButtonLeiste.setPadding(new Insets(10));

        Button ButtonNeue = new Button("Neuer Eintrag...");
        Button ButtonChan = new Button("Eintrag ändern...");
        Button ButtonDele = new Button("Eintrag löschen");

        ButtonNeue.setOnAction(event -> insertNewEntry());
        ButtonChan.setOnAction(event -> updateSelectedEntry());
        ButtonDele.setOnAction(event -> deleteSelectedEntrys());

        ButtonLeiste.getChildren().addAll(ButtonNeue, ButtonChan, ButtonDele);
        DatenAnsicht.setBottom(ButtonLeiste);

        IM.setInfoText("Laden der Bezirk Ansicht erfolgreich");
        BezirkAnsichtGeneriert = true;
        return DatenAnsicht;
    }

    /**
     * Erzeugt eine TableView mit den Daten aus der Datenbank.
     *
     * @return Eine TableView mit Bezirk Daten
     */
    private Node getBezirkAnsichtInnereTabelle() {
        refreshBezirkAnsicht();

        // Name Spalte
        TableColumn<BezirkDaten, String> SpalteName = new TableColumn<>("Name");
        SpalteName.setMinWidth(200);
        SpalteName.setCellValueFactory(new PropertyValueFactory<>("Name"));

        Tabelle.setItems(BezirkDatenListe);
        Tabelle.getColumns().add(SpalteName);

        Tabelle.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Doppelklicke auf Spalten sollen Detailansichten oeffnen:
        Tabelle.setRowFactory(param -> {
            TableRow<BezirkDaten> Spalte = new TableRow<>();
            Spalte.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!Spalte.isEmpty())) {
                    erzeugeDetailAnsicht(Spalte.getItem());
                }
            });
            return Spalte;
        });

        return Tabelle;
    }

    private void erzeugeDetailAnsicht(BezirkDaten SpaltenDaten) {
        Label LabelA = new Label("BezirksID");
        Label LabelAWert = new Label(Integer.toString(SpaltenDaten.getBezirksID()));

        Label LabelB = new Label("Name");
        Label LabelBWert = new Label(SpaltenDaten.getName());

        Button ButtonBearbeiten = new Button("Bearbeiten...");
        Button ButtonLoeschen = new Button("Löschen");
        Button ButtonSucheBezirksId = new Button("Suche nach Vorkommen von BezirksID");
        Button ButtonClose = new Button("Detailansicht verlassen");

        ButtonBearbeiten.setOnAction(event -> {
            Tabelle.getSelectionModel().clearSelection();
            Tabelle.getSelectionModel().select(SpaltenDaten);
            updateSelectedEntry();
            Hauptprogramm.setRechteAnsicht(null);
        });
        ButtonLoeschen.setOnAction(event -> {
            Tabelle.getSelectionModel().clearSelection();
            Tabelle.getSelectionModel().select(SpaltenDaten);
            deleteSelectedEntrys();
            Hauptprogramm.setRechteAnsicht(null);
        });


        //TODO eventhandler fuer die Such Buttons


        ButtonClose.setOnAction(event -> Hauptprogramm.setRechteAnsicht(null));

        ButtonBearbeiten.setMaxWidth(Double.MAX_VALUE);
        ButtonBearbeiten.setMinWidth(150);
        ButtonLoeschen.setMaxWidth(Double.MAX_VALUE);
        ButtonLoeschen.setMinWidth(150);
        ButtonSucheBezirksId.setMaxWidth(Double.MAX_VALUE);
        ButtonClose.setMaxWidth(Double.MAX_VALUE);

        // Wir haben ein Gridpane oben, eine HBox unten in einer VBox in einem ScrollPane
        GridPane Oben = new GridPane();
        Oben.setHgap(10);
        Oben.setVgap(10);
        Oben.addColumn(0, LabelA, LabelB);
        Oben.addColumn(1, LabelAWert, LabelBWert);
        Oben.getColumnConstraints().add(new ColumnConstraints(100));
        Oben.getColumnConstraints().add(new ColumnConstraints(200));

        HBox Unten = new HBox(10);
        Unten.getChildren().addAll(ButtonBearbeiten, ButtonLoeschen);
        Unten.setMaxWidth(300);
        Unten.alignmentProperty().setValue(Pos.CENTER);

        VBox Mittelteil = new VBox(10);
        Mittelteil.setPadding(new Insets(10, 20, 10, 10));
        Mittelteil.getChildren().addAll(Oben, Unten, ButtonSucheBezirksId, ButtonClose);

        ScrollPane Aussen = new ScrollPane();

        Aussen.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        Aussen.setContent(Mittelteil);

        Hauptprogramm.setRechteAnsicht(Aussen);
    }

    /**
     * Aktualisiert die JavaFX vorliegenden Daten mit Daten aus der Datenbank.
     */
    public void refreshBezirkAnsicht() {
        BezirkDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT * FROM BEZIRK");
            while (AnfrageAntwort.next()) {
                BezirkDatenListe.add(new BezirkDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2)));
            }
        } catch (SQLException e) {
            IM.setErrorText("Unbekannter Fehler bei aktualisieren der Ansicht", e);
        }
    }

    private void insertNewEntry() {
        Stage PopUp = new Stage();
        PopUp.initModality(Modality.APPLICATION_MODAL);
        PopUp.setTitle("Neuer Eintrag");
        PopUp.setAlwaysOnTop(true);
        PopUp.setResizable(false);

        GridPane Gitter = new GridPane();
        Gitter.setHgap(10);
        Gitter.setVgap(10);

        Label LabelB = new Label("Name");
        TextField LabelBWert = new TextField();

        Button ButtonFort = new Button("Fortfahren");
        Button ButtonAbb = new Button("Abbrechen");

        ButtonFort.defaultButtonProperty();
        ButtonAbb.cancelButtonProperty();

        ButtonFort.setMaxWidth(Double.MAX_VALUE);
        ButtonAbb.setMaxWidth(Double.MAX_VALUE);

        Gitter.addColumn(0, LabelB);
        Gitter.addColumn(1, LabelBWert);

        VBox AussenBox = new VBox(10);
        HBox InnenBox = new HBox();

        AussenBox.setSpacing(10);
        AussenBox.setPadding(new Insets(10));
        InnenBox.setSpacing(10);

        AussenBox.setAlignment(Pos.CENTER);
        InnenBox.setAlignment(Pos.BOTTOM_CENTER);

        AussenBox.getChildren().addAll(Gitter, InnenBox);
        InnenBox.getChildren().addAll(ButtonFort, ButtonAbb);

        ButtonAbb.setOnAction(event -> PopUp.close());
        ButtonFort.setOnAction(event -> {
            String SQLString = "INSERT INTO Bezirk (Name) VALUES (?)";
            try {
                PreparedStatement InsertStatement = DH.prepareStatement(SQLString);
                InsertStatement.setString(1, LabelBWert.getText());
                InsertStatement.executeUpdate();
                IM.setInfoText("Einfügen durchgeführt");
            } catch (SQLException e) {
                IM.setErrorText("Einfügen Fehlgeschlagen", e);
            }
            refreshBezirkAnsicht();
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    private void updateSelectedEntry() {
        ObservableList<BezirkDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        if (Nutzerauswahl.size() != 1) {
            IM.setErrorText("Es muss genau ein Element ausgewählt werden");
            return;
        }
        BezirkDaten Auswahl = Nutzerauswahl.get(0);

        // Jetzt erzeugen wir ein PopUp zum veraendern des Eintrags

        Stage PopUp = new Stage();
        PopUp.initModality(Modality.APPLICATION_MODAL);
        PopUp.setTitle("Eintrag ändern");
        PopUp.setAlwaysOnTop(true);
        PopUp.setResizable(false);

        GridPane Gitter = new GridPane();
        Gitter.setHgap(10);
        Gitter.setVgap(10);

        Label LabelA = new Label("BezirksID");
        Label LabelAWert = new Label(Integer.toString(Auswahl.getBezirksID()));

        Label LabelB = new Label("Name");
        TextField LabelBWert = new TextField();

        LabelBWert.setText(Auswahl.getName());

        Button ButtonFort = new Button("Fortfahren");
        Button ButtonAbb = new Button("Abbrechen");

        ButtonFort.defaultButtonProperty();
        ButtonAbb.cancelButtonProperty();

        ButtonFort.setMaxWidth(Double.MAX_VALUE);
        ButtonAbb.setMaxWidth(Double.MAX_VALUE);

        Gitter.addColumn(0, LabelA, LabelB);
        Gitter.addColumn(1, LabelAWert, LabelBWert);

        VBox AussenBox = new VBox(10);
        HBox InnenBox = new HBox();

        AussenBox.setSpacing(10);
        AussenBox.setPadding(new Insets(10));
        InnenBox.setSpacing(10);

        AussenBox.setAlignment(Pos.CENTER);
        InnenBox.setAlignment(Pos.BOTTOM_CENTER);

        AussenBox.getChildren().addAll(Gitter, InnenBox);
        InnenBox.getChildren().addAll(ButtonFort, ButtonAbb);

        ButtonAbb.setOnAction(event -> PopUp.close());
        ButtonFort.setOnAction(event -> {
            String SQLString = "UPDATE Bezirk SET Name=? WHERE BezirksID = " + Auswahl.getBezirksID();
            try {
                PreparedStatement SQLInjektionNeinNein = DH.prepareStatement(SQLString);
                SQLInjektionNeinNein.setString(1, LabelBWert.getText());
                SQLInjektionNeinNein.executeUpdate();
                IM.setInfoText("Änderung durchgeführt");
            } catch (SQLException e) {
                IM.setErrorText("Ändern Fehlgeschlagen", e);
            }
            refreshBezirkAnsicht();
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    private void deleteSelectedEntrys() {
        ObservableList<BezirkDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        if (Nutzerauswahl.isEmpty()) {
            IM.setErrorText("Es muss mindestens ein Eintrag ausgewählt sein");
            return;
        }

        Nutzerauswahl.forEach(BezirkDaten -> {
            try {
                DH.getAnfrageObjekt().executeUpdate("DELETE FROM Bezirk WHERE BezirksID = " + BezirkDaten.getBezirksID());
            } catch (SQLException e) {
                IM.setErrorText("Löschen fehlgeschlagen", e);
            }
        });
        refreshBezirkAnsicht();
    }

    public void SucheBezirk(int BezirksID) {
        Hauptprogramm.setMittlereAnsicht(getBezirkAnsicht());
        BezirkDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT * FROM BEZIRK WHERE BezirksID = " + BezirksID);
            while (AnfrageAntwort.next()) {
                BezirkDatenListe.add(new BezirkDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2)));
            }
        } catch (SQLException e) {
            IM.setErrorText("Unbekannter Fehler bei aktualisieren der Ansicht", e);
        }
    }
}
