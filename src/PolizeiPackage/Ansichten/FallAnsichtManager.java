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
public class FallAnsichtManager {

    private DatenbankHandler DH;
    private InfoErrorManager IM;
    private Main Hauptprogramm;
    private TableView<FallDaten> Tabelle;
    private BorderPane DatenAnsicht;
    private ObservableList<FallDaten> FallDatenListe;
    private boolean FallAnsichtGeneriert;
    private VerbrechenAnsichtManager VerAM;

    public FallAnsichtManager(DatenbankHandler DBH, InfoErrorManager IEM, Main HauptFenster) {
        DH = DBH;
        IM = IEM;
        Hauptprogramm = HauptFenster;
        FallDatenListe = FXCollections.observableArrayList();
        Tabelle = new TableView<>();
        FallAnsichtGeneriert = false;
    }

    public void setVerbrechenAnsichtManager(VerbrechenAnsichtManager VAM) {
        VerAM = VAM;
    }

    public Node getFallAnsicht() {
        if (FallAnsichtGeneriert) {
            refreshFallAnsicht();
            return DatenAnsicht;
        }
        IM.setInfoText("Lade Fall Ansicht");
        DatenAnsicht = new BorderPane(getFallAnsichtInnereTabelle());

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

        IM.setInfoText("Laden der Fall Ansicht erfolgreich");
        FallAnsichtGeneriert = true;
        return DatenAnsicht;
    }

    /**
     * Erzeugt eine TableView mit den Daten aus der Datenbank.
     *
     * @return Eine TableView mit Fall Daten
     */
    private Node getFallAnsichtInnereTabelle() {
        refreshFallAnsicht();

        // Name Spalte
        TableColumn<FallDaten, String> SpalteName = new TableColumn<>("Name");
        SpalteName.setMinWidth(200);
        SpalteName.setCellValueFactory(new PropertyValueFactory<>("Name"));

        // Eroeffnungsdatum Spalte
        TableColumn<FallDaten, String> SpalteDatumA = new TableColumn<>("Eröffnungsdatum");
        SpalteDatumA.setMinWidth(200);
        SpalteDatumA.setCellValueFactory(new PropertyValueFactory<>("Eroeffnungsdatum"));

        // Enddatum Spalte
        TableColumn<FallDaten, String> SpalteDatumB = new TableColumn<>("Enddatum");
        SpalteDatumB.setMinWidth(200);
        SpalteDatumB.setCellValueFactory(new PropertyValueFactory<>("Enddatum"));

        Tabelle.setItems(FallDatenListe);
        Tabelle.getColumns().add(SpalteName);
        Tabelle.getColumns().add(SpalteDatumA);
        Tabelle.getColumns().add(SpalteDatumB);
        Tabelle.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Doppelklicke auf Spalten sollen Detailansichten oeffnen:
        Tabelle.setRowFactory(param -> {
            TableRow<FallDaten> Spalte = new TableRow<>();
            Spalte.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! Spalte.isEmpty())) {
                    erzeugeDetailAnsicht(Spalte.getItem());
                }
            });
            return Spalte;
        });

        return Tabelle;
    }

    private void erzeugeDetailAnsicht(FallDaten SpaltenDaten) {
        Label LabelArtId = new Label("FallID");
        Label LabelArtIdWert = new Label(Integer.toString(SpaltenDaten.getFallID()));

        Label LabelName = new Label("Name");
        Label TextFeldName = new Label(SpaltenDaten.getName());

        Label LabelDatumA = new Label("Enddatum");
        Label LabelDatumAInhalt = new Label(SpaltenDaten.getEroeffnungsdatum());

        Label LabelDatumB = new Label("Eröffnungsdatum");
        Label LabelDatumBInhalt = new Label(SpaltenDaten.getEnddatum());

        Button ButtonBearbeiten = new Button("Bearbeiten...");
        Button ButtonLoeschen = new Button("Löschen");
        Button ButtonSucheFallId = new Button("Suche nach Vorkommen von FallID");
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
        ButtonSucheFallId.setOnAction(event -> {
            Hauptprogramm.setRechteAnsicht(null);
            VerAM.FallAnsichtQuer(SpaltenDaten.getFallID());
        });
        ButtonClose.setOnAction(event -> Hauptprogramm.setRechteAnsicht(null));

        ButtonBearbeiten.setMaxWidth(Double.MAX_VALUE);
        ButtonBearbeiten.setMinWidth(150);
        ButtonLoeschen.setMaxWidth(Double.MAX_VALUE);
        ButtonLoeschen.setMinWidth(150);
        ButtonSucheFallId.setMaxWidth(Double.MAX_VALUE);
        ButtonClose.setMaxWidth(Double.MAX_VALUE);

        // Wir haben ein Gridpane oben, eine HBox unten in einer VBox in einem ScrollPane
        GridPane Oben = new GridPane();
        Oben.setHgap(10);
        Oben.setVgap(10);
        Oben.addColumn(0,LabelArtId, LabelName, LabelDatumA, LabelDatumB);
        Oben.addColumn(1,LabelArtIdWert, TextFeldName, LabelDatumAInhalt, LabelDatumBInhalt);
        Oben.getColumnConstraints().add(new ColumnConstraints(100));
        Oben.getColumnConstraints().add(new ColumnConstraints(200));

        HBox Unten = new HBox(10);
        Unten.getChildren().addAll(ButtonBearbeiten, ButtonLoeschen);
        Unten.setMaxWidth(300);
        Unten.alignmentProperty().setValue(Pos.CENTER);

        VBox Mittelteil = new VBox(10);
        Mittelteil.setPadding(new Insets(10,20,10,10));
        Mittelteil.getChildren().addAll(Oben, Unten, ButtonSucheFallId, ButtonClose);

        ScrollPane Aussen = new ScrollPane();

        Aussen.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        Aussen.setContent(Mittelteil);

        Hauptprogramm.setRechteAnsicht(Aussen);
    }

    /**
     * Aktualisiert die JavaFX vorliegenden Daten mit Daten aus der Datenbank.
     */
    public void refreshFallAnsicht() {
        FallDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT * FROM FALL");
            while (AnfrageAntwort.next()) {
                if (AnfrageAntwort.getObject("Enddatum") != null) {
                    FallDatenListe.add(new FallDaten(AnfrageAntwort.getInt("FallID"), AnfrageAntwort.getString("Name"),
                            AnfrageAntwort.getString("Eröffnungsdatum"), AnfrageAntwort.getString("Enddatum")));
                } else {
                    FallDatenListe.add(new FallDaten(AnfrageAntwort.getInt("FallID"), AnfrageAntwort.getString("Name"),
                            AnfrageAntwort.getString("Eröffnungsdatum"), "Unbekannt"));
                }
            }
        } catch (SQLException e) {} //TODO evtl null returnen bei Fehler
    }

    private void insertNewEntry() {
        Stage PopUp = new Stage();
        PopUp.initModality(Modality.APPLICATION_MODAL);
        PopUp.setTitle("Eintrag hinzufügen");
        PopUp.setAlwaysOnTop(true);
        PopUp.setResizable(false);

        GridPane Gitter = new GridPane();
        Gitter.setHgap(10);
        Gitter.setVgap(10);

        Label LabelName = new Label("Name");
        Label LabelA = new Label("Eröffnungsdatum");
        Label LabelB = new Label("Enddatum");

        TextField TextFeldName = new TextField();
        TextField TextFeldA = new TextField();
        TextField TextFeldB = new TextField();

        Button ButtonFort = new Button("Hinzufügen");
        Button ButtonAbb = new Button("Abbrechen");

        ButtonFort.defaultButtonProperty();
        ButtonAbb.cancelButtonProperty();

        ButtonFort.setMaxWidth(Double.MAX_VALUE);
        ButtonAbb.setMaxWidth(Double.MAX_VALUE);

        Gitter.addColumn(0,  LabelName, LabelA, LabelB);
        Gitter.addColumn(1, TextFeldName, TextFeldA, TextFeldB);

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
            String SQLString = "INSERT INTO ART (Name, Eröffnungsdatum, Enddatum) VALUES (?, ?, ?)";
            try {
                PreparedStatement InsertStatement = DH.prepareStatement(SQLString);
                InsertStatement.setString(1, TextFeldName.getText());
                InsertStatement.setString(2, TextFeldA.getText());
                InsertStatement.setString(3, TextFeldB.getText());
                InsertStatement.executeUpdate();
                IM.setInfoText("Einfügen durchgeführt");
            } catch (SQLException e) {
                IM.setErrorText("Einfügen Fehlgeschlagen", e);
            }
            refreshFallAnsicht();
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    private void updateSelectedEntry() {
        ObservableList<FallDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        if (Nutzerauswahl.size() != 1) {
            IM.setErrorText("Es muss genau ein Element ausgewählt werden");
            return;
        }
        FallDaten Auswahl = Nutzerauswahl.get(0);

        // Jetzt erzeugen wir ein PopUp zum veraendern des Eintrags

        Stage PopUp = new Stage();
        PopUp.initModality(Modality.APPLICATION_MODAL);
        PopUp.setTitle("Eintrag ändern");
        PopUp.setAlwaysOnTop(true);
        PopUp.setResizable(false);

        GridPane Gitter = new GridPane();
        Gitter.setHgap(10);
        Gitter.setVgap(10);

        Label LabelArtId = new Label("FallID");
        Label LabelArtIdWert = new Label(Integer.toString(Auswahl.getFallID()));

        Label LabelName = new Label("Name");
        TextField TextFeldName = new TextField();

        Label LabelDatumA = new Label("Beschreibung");
        TextField TextFeldA = new TextField();

        Label LabelDatumB = new Label("Beschreibung");
        TextField TextFeldB = new TextField();

        TextFeldName.setText(Auswahl.getName());
        TextFeldA.setText(Auswahl.getEroeffnungsdatum());
        TextFeldB.setText(Auswahl.getEnddatum());

        Button ButtonFort = new Button("Fortfahren");
        Button ButtonAbb = new Button("Abbrechen");

        ButtonFort.defaultButtonProperty();
        ButtonAbb.cancelButtonProperty();

        ButtonFort.setMaxWidth(Double.MAX_VALUE);
        ButtonAbb.setMaxWidth(Double.MAX_VALUE);

        Gitter.addColumn(0, LabelArtId, LabelName, LabelDatumA, LabelDatumB);
        Gitter.addColumn(1, LabelArtIdWert, TextFeldName, TextFeldA, TextFeldB);

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
            String SQLString = "UPDATE FALL SET Name=?, Eröffnungsdatum=?, Enddatum=? WHERE FallID = " + Auswahl.getFallID();
            try {
                PreparedStatement SQLInjektionNeinNein = DH.prepareStatement(SQLString);
                SQLInjektionNeinNein.setString(1, TextFeldName.getText());
                SQLInjektionNeinNein.setString(2, TextFeldA.getText());
                SQLInjektionNeinNein.setString(3, TextFeldB.getText());
                SQLInjektionNeinNein.executeUpdate();
                IM.setInfoText("Änderung durchgeführt");
            } catch (SQLException e) {
                IM.setErrorText("Ändern Fehlgeschlagen", e);
            }
            refreshFallAnsicht();
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    private void deleteSelectedEntrys() {
        ObservableList<FallDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        if (Nutzerauswahl.isEmpty()) {
            IM.setErrorText("Es muss mindestens ein Eintrag ausgewählt sein");
            return;
        }

        Nutzerauswahl.forEach(fallDaten -> {
            try {
                DH.getAnfrageObjekt().executeUpdate("DELETE FROM FALL WHERE FallID = "+ fallDaten.getFallID());
            } catch (SQLException e) {
                IM.setErrorText("Löschen fehlgeschlagen", e);
            }
        });
        refreshFallAnsicht();
    }
}
