package PolizeiPackage.Ansichten;

import PolizeiPackage.DatenbankHandler;
import PolizeiPackage.InfoErrorManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Liefert Tabellen fuer verschiedene AnsichtenManager
 */
public class AnsichtenManager {

    private DatenbankHandler DH;
    private InfoErrorManager IM;

    public AnsichtenManager(DatenbankHandler DBH, InfoErrorManager IEM) {
        this.DH = DBH;
        this.IM = IEM;
    }



    public Node getArtAnsicht() {
        IM.setInfoText("Lade Art Ansicht");
        ObservableList<ArtDaten> daten = FXCollections.observableArrayList();
        ResultSet AnfrageAntwort = null;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT * FROM ART");
            while (AnfrageAntwort.next()) {
                daten.add(new ArtDaten(AnfrageAntwort.getInt("ArtID"), AnfrageAntwort.getString("Name"), AnfrageAntwort.getString("Beschreibung")));
            }
        } catch (SQLException e) {} //TODO evtl null returnen bei Fehler

        // Name Spalte
        TableColumn<ArtDaten, String> SpalteName = new TableColumn<>("Name");
        SpalteName.setMinWidth(200);
        SpalteName.setCellValueFactory(new PropertyValueFactory<>("Name"));

        // Beschreibung Spalte
        TableColumn<ArtDaten, String> SpalteBeschreibung = new TableColumn<>("Beschreibung");
        SpalteBeschreibung.setMinWidth(200);
        SpalteBeschreibung.setCellValueFactory(new PropertyValueFactory<>("Beschreibung"));

        TableView<ArtDaten> Tabelle = new TableView<>();
        Tabelle.setItems(daten);
        Tabelle.getColumns().add(SpalteName);
        Tabelle.getColumns().add(SpalteBeschreibung);

        IM.setInfoText("Laden der Art Ansicht erfolgreich");
        return Tabelle;
    }
}
