package PolizeiPackage.Ansichten;

import PolizeiPackage.DatenbankHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ArtAnsicht {
    private DatenbankHandler DH;
    private ObservableList<ArtDaten> Daten;

    public ArtAnsicht(DatenbankHandler DaHa) {
        DH = DaHa;
    }

    public Node getArtAnsicht() {
        Daten = FXCollections.observableArrayList();
        ResultSet AnfrageAntwort = null;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT * FROM ART");
            while (AnfrageAntwort.next()) {
                Daten.add(new ArtDaten(AnfrageAntwort.getInt("ArtID"), AnfrageAntwort.getString("Name"), AnfrageAntwort.getString("Beschreibung")));
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
        Tabelle.setItems(Daten);
        Tabelle.getColumns().add(SpalteName);
        Tabelle.getColumns().add(SpalteBeschreibung);
        return Tabelle;
    }
}
