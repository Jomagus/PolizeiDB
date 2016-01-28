package PolizeiPackage.Ansichten;

import PolizeiPackage.DatenbankHandler;
import PolizeiPackage.InfoErrorManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Liefert Tabellen fuer verschiedene ArtAnsichtManager
 */
public class ArtAnsichtManager {

    private DatenbankHandler DH;
    private InfoErrorManager IM;
    private TableView<ArtDaten> Tabelle;
    private BorderPane DatenAnsicht;
    private ObservableList<ArtDaten> ArtDatenListe;
    private boolean ArtAnsichtGeneriert;

    public ArtAnsichtManager(DatenbankHandler DBH, InfoErrorManager IEM) {
        DH = DBH;
        IM = IEM;
        ArtDatenListe = FXCollections.observableArrayList();
        Tabelle = new TableView<>();
        ArtAnsichtGeneriert = false;
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
        return Tabelle;
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
        LabelBeschreibung.setWrapText(true);
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
