package PolizeiPackage.Ansichten;

import PolizeiPackage.DatenbankHandler;
import PolizeiPackage.InfoErrorManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Liefert Tabellen fuer verschiedene ArtAnsichtManager
 */
public class ArtAnsichtManager {

    private DatenbankHandler DH;
    private InfoErrorManager IM;
    private TableView<ArtDaten> Tabelle;
    private ObservableList<ArtDaten> ArtDatenListe;

    public ArtAnsichtManager(DatenbankHandler DBH, InfoErrorManager IEM) {
        DH = DBH;
        IM = IEM;
        ArtDatenListe = FXCollections.observableArrayList();
        Tabelle = new TableView<>();
    }

    public Node getArtAnsicht() {
        IM.setInfoText("Lade Art Ansicht");
        BorderPane DatenAnsicht = new BorderPane(getArtAnsichtInnereTabelle());

        HBox ButtonLeiste = new HBox(10);
        ButtonLeiste.setPadding(new Insets(10));

        Button ButtonNeue = new Button("Neuer Eintrag");
        Button ButtonChan = new Button("Eintrag ändern");
        Button ButtonDele = new Button("Eintrag löschen");

        ButtonNeue.setOnAction(event -> {});
        ButtonChan.setOnAction(event -> {});
        ButtonDele.setOnAction(event -> deleteSelectedEntrys());

        ButtonLeiste.getChildren().addAll(ButtonNeue, ButtonChan, ButtonDele);
        DatenAnsicht.setBottom(ButtonLeiste);

        IM.setInfoText("Laden der Art Ansicht erfolgreich");
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

    private void deleteSelectedEntrys() {
        ObservableList<ArtDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        Nutzerauswahl.forEach(artDaten -> {
            try {
                DH.getAnfrageObjekt().executeQuery("DELETE FROM ART WHERE ART.ArtID = "+ artDaten.getArtID());
            } catch (SQLException e) {
                IM.setErrorText("Löschen fehlgeschlagen", e);
            }
        });
    }
}
