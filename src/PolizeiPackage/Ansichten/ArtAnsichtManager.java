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
public class ArtAnsichtManager {

    private DatenbankHandler DH;
    private InfoErrorManager IM;
    private Main Hauptprogramm;
    private TableView<ArtDaten> Tabelle;
    private BorderPane DatenAnsicht;
    private ObservableList<ArtDaten> ArtDatenListe;
    private boolean ArtAnsichtGeneriert;
    private VerbrechenAnsichtManager VerAM;

    public ArtAnsichtManager(DatenbankHandler DBH, InfoErrorManager IEM, Main HauptFenster) {
        DH = DBH;
        IM = IEM;
        Hauptprogramm = HauptFenster;
        ArtDatenListe = FXCollections.observableArrayList();
        Tabelle = new TableView<>();
        ArtAnsichtGeneriert = false;
    }

    public void setVerbrechensManager(VerbrechenAnsichtManager VAM) {
        VerAM = VAM;
    }

    public Node getArtAnsicht() {
        if (ArtAnsichtGeneriert) {
            refreshArtAnsicht();
            return DatenAnsicht;
        }
        IM.setInfoText("Lade Art Ansicht");
        DatenAnsicht = new BorderPane(getArtAnsichtInnereTabelle());

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

        IM.setInfoText("Laden der Art Ansicht erfolgreich");
        ArtAnsichtGeneriert = true;
        return DatenAnsicht;
    }

    /**
     * Erzeugt eine TableView mit den Daten aus der Datenbank.
     *
     * @return Eine TableView mit Art Daten
     */
    private Node getArtAnsichtInnereTabelle() {
        refreshArtAnsicht();

        // Name Spalte
        TableColumn<ArtDaten, String> SpalteName = new TableColumn<>("Name");
        SpalteName.setMinWidth(200);
        SpalteName.setCellValueFactory(new PropertyValueFactory<>("Name"));

        // Beschreibung Spalte
        TableColumn<ArtDaten, String> SpalteBeschreibung = new TableColumn<>("Beschreibung");
        SpalteBeschreibung.setMinWidth(200);
        SpalteBeschreibung.setCellValueFactory(new PropertyValueFactory<>("Beschreibung"));

        Tabelle.setItems(ArtDatenListe);
        Tabelle.getColumns().add(SpalteName);
        Tabelle.getColumns().add(SpalteBeschreibung);
        Tabelle.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Doppelklicke auf Spalten sollen Detailansichten oeffnen:
        Tabelle.setRowFactory(param -> {
            TableRow<ArtDaten> Spalte = new TableRow<>();
            Spalte.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! Spalte.isEmpty())) {
                    erzeugeDetailAnsicht(Spalte.getItem());
                }
            });
            return Spalte;
        });

        return Tabelle;
    }

    private void erzeugeDetailAnsicht(ArtDaten SpaltenDaten) {
        Label LabelArtId = new Label("ArtID");
        Label LabelArtIdWert = new Label(Integer.toString(SpaltenDaten.getArtID()));

        Label LabelName = new Label("Name");
        Label TextFeldName = new Label(SpaltenDaten.getName());

        Label LabelBeschreibung = new Label("Beschreibung");
        Label TextFeldBeschreibung = new Label(SpaltenDaten.getBeschreibung());
        TextFeldBeschreibung.setWrapText(true);

        Button ButtonBearbeiten = new Button("Bearbeiten...");
        Button ButtonLoeschen = new Button("Löschen");
        Button ButtonSucheArtID = new Button("Suche nach Vorkommen von ArtID");
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
        ButtonSucheArtID.setOnAction(event -> {
            Hauptprogramm.setRechteAnsicht(null);
            VerAM.ArtAnsichtQuer(SpaltenDaten.getArtID());
        });
        ButtonClose.setOnAction(event -> Hauptprogramm.setRechteAnsicht(null));

        ButtonBearbeiten.setMaxWidth(Double.MAX_VALUE);
        ButtonBearbeiten.setMinWidth(150);
        ButtonLoeschen.setMaxWidth(Double.MAX_VALUE);
        ButtonLoeschen.setMinWidth(150);
        ButtonSucheArtID.setMaxWidth(Double.MAX_VALUE);
        ButtonClose.setMaxWidth(Double.MAX_VALUE);

        // Wir haben ein Gridpane oben, eine HBox unten in einer VBox in einem ScrollPane
        GridPane Oben = new GridPane();
        Oben.setHgap(10);
        Oben.setVgap(10);
        Oben.addColumn(0,LabelArtId, LabelName, LabelBeschreibung);
        Oben.addColumn(1,LabelArtIdWert, TextFeldName, TextFeldBeschreibung);
        Oben.getColumnConstraints().add(new ColumnConstraints(100));
        Oben.getColumnConstraints().add(new ColumnConstraints(200));

        HBox Unten = new HBox(10);
        Unten.getChildren().addAll(ButtonBearbeiten, ButtonLoeschen);
        Unten.setMaxWidth(300);
        Unten.alignmentProperty().setValue(Pos.CENTER);

        VBox Mittelteil = new VBox(10);
        Mittelteil.setPadding(new Insets(10,20,10,10));
        Mittelteil.getChildren().addAll(Oben, Unten, ButtonSucheArtID, ButtonClose);

        ScrollPane Aussen = new ScrollPane();

        Aussen.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        Aussen.setContent(Mittelteil);

        Hauptprogramm.setRechteAnsicht(Aussen);
    }

    /**
     * Aktualisiert die JavaFX vorliegenden Daten mit Daten aus der Datenbank.
     */
    public void refreshArtAnsicht() {
        ArtDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT * FROM ART");
            while (AnfrageAntwort.next()) {
                ArtDatenListe.add(new ArtDaten(AnfrageAntwort.getInt("ArtID"), AnfrageAntwort.getString("Name"), AnfrageAntwort.getString("Beschreibung")));
            }
        } catch (SQLException e) {
            IM.setErrorText("Unbekannter Fehler bei aktualisieren der Ansicht", e);
        }
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
        TextField TextFeldName = new TextField();

        Label LabelBeschreibung = new Label("Beschreibung");
        LabelBeschreibung.setWrapText(true);
        TextField TextFeldBeschreibung = new TextField();

        Button ButtonFort = new Button("Hinzufügen");
        Button ButtonAbb = new Button("Abbrechen");

        ButtonFort.defaultButtonProperty();
        ButtonAbb.cancelButtonProperty();

        ButtonFort.setMaxWidth(Double.MAX_VALUE);
        ButtonAbb.setMaxWidth(Double.MAX_VALUE);

        Gitter.addColumn(0,  LabelName, LabelBeschreibung);
        Gitter.addColumn(1, TextFeldName, TextFeldBeschreibung);

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
            String SQLString = "INSERT INTO ART (Name, Beschreibung) VALUES (?, ?)";
            try {
                PreparedStatement InsertStatement = DH.prepareStatement(SQLString);
                InsertStatement.setString(1, TextFeldName.getText());
                InsertStatement.setString(2, TextFeldBeschreibung.getText());
                InsertStatement.executeUpdate();
                IM.setInfoText("Einfügen durchgeführt");
            } catch (SQLException e) {
                IM.setErrorText("Einfügen Fehlgeschlagen", e);
            }
            refreshArtAnsicht();
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    private void updateSelectedEntry() {
        ObservableList<ArtDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        if (Nutzerauswahl.size() != 1) {
            IM.setErrorText("Es muss genau ein Element ausgewählt werden");
            return;
        }
        ArtDaten Auswahl = Nutzerauswahl.get(0);

        // Jetzt erzeugen wir ein PopUp zum veraendern des Eintrags

        Stage PopUp = new Stage();
        PopUp.initModality(Modality.APPLICATION_MODAL);
        PopUp.setTitle("Eintrag ändern");
        PopUp.setAlwaysOnTop(true);
        PopUp.setResizable(false);

        GridPane Gitter = new GridPane();
        Gitter.setHgap(10);
        Gitter.setVgap(10);

        Label LabelArtId = new Label("ArtID");
        Label LabelArtIdWert = new Label(Integer.toString(Auswahl.getArtID()));

        Label LabelName = new Label("Name");
        TextField TextFeldName = new TextField();

        Label LabelBeschreibung = new Label("Beschreibung");
        TextField TextFeldBeschreibung = new TextField();

        TextFeldName.setText(Auswahl.getName());
        TextFeldBeschreibung.setText(Auswahl.getBeschreibung());

        Button ButtonFort = new Button("Fortfahren");
        Button ButtonAbb = new Button("Abbrechen");

        ButtonFort.defaultButtonProperty();
        ButtonAbb.cancelButtonProperty();

        ButtonFort.setMaxWidth(Double.MAX_VALUE);
        ButtonAbb.setMaxWidth(Double.MAX_VALUE);

        Gitter.addColumn(0, LabelArtId, LabelName, LabelBeschreibung);
        Gitter.addColumn(1, LabelArtIdWert, TextFeldName, TextFeldBeschreibung);

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
            String SQLString = "UPDATE ART SET Name=?, Beschreibung=? WHERE ArtID = " + Auswahl.getArtID();
            try {
                PreparedStatement SQLInjektionNeinNein = DH.prepareStatement(SQLString);
                SQLInjektionNeinNein.setString(1, TextFeldName.getText());
                SQLInjektionNeinNein.setString(2, TextFeldBeschreibung.getText());
                SQLInjektionNeinNein.executeUpdate();
                IM.setInfoText("Änderung durchgeführt");
            } catch (SQLException e) {
                IM.setErrorText("Ändern Fehlgeschlagen", e);
            }
            refreshArtAnsicht();
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    private void deleteSelectedEntrys() {
        ObservableList<ArtDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        if (Nutzerauswahl.isEmpty()) {
            IM.setErrorText("Es muss mindestens ein Eintrag ausgewählt sein");
            return;
        }

        Nutzerauswahl.forEach(artDaten -> {
            try {
                DH.getAnfrageObjekt().executeUpdate("DELETE FROM ART WHERE ArtID = "+ artDaten.getArtID());
            } catch (SQLException e) {
                IM.setErrorText("Löschen fehlgeschlagen", e);
            }
        });
        refreshArtAnsicht();
    }
}
