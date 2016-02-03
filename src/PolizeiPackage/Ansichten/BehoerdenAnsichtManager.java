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
public class BehoerdenAnsichtManager {

    private DatenbankHandler DH;
    private InfoErrorManager IM;
    private Main Hauptprogramm;
    private TableView<BehoerdenDaten> Tabelle;
    private BorderPane DatenAnsicht;
    private ObservableList<BehoerdenDaten> BehoerdenDatenListe;
    private boolean BehoerdenAnsichtGeneriert;

    public BehoerdenAnsichtManager(DatenbankHandler DBH, InfoErrorManager IEM, Main HauptFenster) {
        DH = DBH;
        IM = IEM;
        Hauptprogramm = HauptFenster;
        BehoerdenDatenListe = FXCollections.observableArrayList();
        Tabelle = new TableView<>();
        BehoerdenAnsichtGeneriert = false;
    }

    public Node getBehoerdenAnsicht() {
        if (BehoerdenAnsichtGeneriert) {
            refreshBehoerdenAnsicht();
            return DatenAnsicht;
        }
        IM.setInfoText("Lade Behoerden Ansicht");
        DatenAnsicht = new BorderPane(getBehoerdenAnsichtInnereTabelle());

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

        IM.setInfoText("Laden der Behoerden Ansicht erfolgreich");
        BehoerdenAnsichtGeneriert = true;
        return DatenAnsicht;
    }

    /**
     * Erzeugt eine TableView mit den Daten aus der Datenbank.
     *
     * @return Eine TableView mit Behoerden Daten
     */
    private Node getBehoerdenAnsichtInnereTabelle() {
        refreshBehoerdenAnsicht();

        // Name Spalte
        TableColumn<BehoerdenDaten, String> SpalteName = new TableColumn<>("Name");
        SpalteName.setMinWidth(200);
        SpalteName.setCellValueFactory(new PropertyValueFactory<>("Name"));

        // Typ Spalte
        TableColumn<BehoerdenDaten, String> SpalteTyp = new TableColumn<>("Typ");
        SpalteTyp.setMinWidth(200);
        SpalteTyp.setCellValueFactory(new PropertyValueFactory<>("Typ"));

        // Bezirk Spalte
        TableColumn<BehoerdenDaten, String> SpalteBezirk = new TableColumn<>("Bezirk");
        SpalteBezirk.setMinWidth(200);
        SpalteBezirk.setCellValueFactory(new PropertyValueFactory<>("BezirkName"));

        Tabelle.setItems(BehoerdenDatenListe);
        Tabelle.getColumns().add(SpalteName);
        Tabelle.getColumns().add(SpalteTyp);
        Tabelle.getColumns().add(SpalteBezirk);

        Tabelle.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Doppelklicke auf Spalten sollen Detailansichten oeffnen:
        Tabelle.setRowFactory(param -> {
            TableRow<BehoerdenDaten> Spalte = new TableRow<>();
            Spalte.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!Spalte.isEmpty())) {
                    erzeugeDetailAnsicht(Spalte.getItem());
                }
            });
            return Spalte;
        });

        return Tabelle;
    }

    private void erzeugeDetailAnsicht(BehoerdenDaten SpaltenDaten) {
        Label LabelA = new Label("BehoerdenID");
        Label LabelAWert = new Label(Integer.toString(SpaltenDaten.getBehoerdenID()));

        Label LabelB = new Label("Name");
        Label LabelBWert = new Label(SpaltenDaten.getName());

        Label LabelC = new Label("Typ");
        Label LabelCWert = new Label(SpaltenDaten.getTyp());

        Label LabelD = new Label("BezirksID");
        Label LabelDWert = new Label(Integer.toString(SpaltenDaten.getVerantwortlichBezirksID()));

        Label LabelE = new Label("Bezirk");
        Label LabelEWert = new Label(SpaltenDaten.getBezirkName());


        Button ButtonBearbeiten = new Button("Bearbeiten...");
        Button ButtonLoeschen = new Button("Löschen");
        Button ButtonSucheBehoerdenId = new Button("Suche nach Vorkommen von BehoerdenID");
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
        ButtonSucheBehoerdenId.setMaxWidth(Double.MAX_VALUE);
        ButtonSucheBezirksId.setMaxWidth(Double.MAX_VALUE);
        ButtonClose.setMaxWidth(Double.MAX_VALUE);

        // Wir haben ein Gridpane oben, eine HBox unten in einer VBox in einem ScrollPane
        GridPane Oben = new GridPane();
        Oben.setHgap(10);
        Oben.setVgap(10);
        Oben.addColumn(0, LabelA, LabelB, LabelC, LabelD, LabelE);
        Oben.addColumn(1, LabelAWert, LabelBWert, LabelCWert, LabelDWert, LabelEWert);
        Oben.getColumnConstraints().add(new ColumnConstraints(100));
        Oben.getColumnConstraints().add(new ColumnConstraints(200));

        HBox Unten = new HBox(10);
        Unten.getChildren().addAll(ButtonBearbeiten, ButtonLoeschen);
        Unten.setMaxWidth(300);
        Unten.alignmentProperty().setValue(Pos.CENTER);

        VBox Mittelteil = new VBox(10);
        Mittelteil.setPadding(new Insets(10, 20, 10, 10));
        Mittelteil.getChildren().addAll(Oben, Unten, ButtonSucheBehoerdenId, ButtonSucheBezirksId, ButtonClose);

        ScrollPane Aussen = new ScrollPane();

        Aussen.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        Aussen.setContent(Mittelteil);

        Hauptprogramm.setRechteAnsicht(Aussen);
    }

    /**
     * Aktualisiert die JavaFX vorliegenden Daten mit Daten aus der Datenbank.
     */
    public void refreshBehoerdenAnsicht() {
        BehoerdenDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT BehördenID, BEHÖRDE.Name, Typ, verantwortlich_für_BezirksID, BEZIRK.Name FROM BEHÖRDE, BEZIRK WHERE verantwortlich_für_BezirksID = BEZIRK.BezirksID");
            while (AnfrageAntwort.next()) {
                BehoerdenDatenListe.add(new BehoerdenDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
                        AnfrageAntwort.getString(3), AnfrageAntwort.getInt(4), AnfrageAntwort.getString(5)));
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

        Label LabelC = new Label("Typ");
        TextField LabelCWert = new TextField();

        Label LabelD = new Label("Bezirk");
        Label LabelDWert = new Label();

        Label LabelE = new Label("BezirksID");
        TextField LabelEWert = new TextField();

        LabelEWert.textProperty().addListener(((observable, oldValue, newValue) -> {
            try {
                PreparedStatement NutzerInput = DH.prepareStatement("SELECT Name FROM BEZIRK WHERE BezirksID = ?");
                NutzerInput.setInt(1, Integer.parseInt(newValue));
                ResultSet Antwort = NutzerInput.executeQuery();
                if (Antwort.next()) {
                    LabelDWert.setText(Antwort.getString(1));
                } else {
                    LabelDWert.setText("Ungültige BezirksID");
                }
            } catch (SQLException e) {
                IM.setErrorText("Unbekannter SQL Fehler", e);
            } catch (NumberFormatException e) {
                LabelDWert.setText("BezirksID muss eine Zahl sein");
            }
        }));

        Button ButtonFort = new Button("Fortfahren");
        Button ButtonAbb = new Button("Abbrechen");

        ButtonFort.defaultButtonProperty();
        ButtonAbb.cancelButtonProperty();

        ButtonFort.setMaxWidth(Double.MAX_VALUE);
        ButtonAbb.setMaxWidth(Double.MAX_VALUE);

        Gitter.addColumn(0, LabelB, LabelC, LabelD, LabelE);
        Gitter.addColumn(1, LabelBWert, LabelCWert, LabelDWert, LabelEWert);

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
            String SQLString = "INSERT INTO BEHÖRDE (Name, Typ, verantwortlich_für_BezirksID) VALUES (?, ?, ?)";
            try {
                PreparedStatement InsertStatement = DH.prepareStatement(SQLString);
                InsertStatement.setString(1, LabelBWert.getText());
                InsertStatement.setString(2, LabelCWert.getText());
                InsertStatement.setString(3, LabelEWert.getText());
                InsertStatement.executeUpdate();
                IM.setInfoText("Einfügen durchgeführt");
            } catch (SQLException e) {
                IM.setErrorText("Einfügen Fehlgeschlagen", e);
            }
            refreshBehoerdenAnsicht();
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    private void updateSelectedEntry() {
        ObservableList<BehoerdenDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        if (Nutzerauswahl.size() != 1) {
            IM.setErrorText("Es muss genau ein Element ausgewählt werden");
            return;
        }
        BehoerdenDaten Auswahl = Nutzerauswahl.get(0);

        // Jetzt erzeugen wir ein PopUp zum veraendern des Eintrags

        Stage PopUp = new Stage();
        PopUp.initModality(Modality.APPLICATION_MODAL);
        PopUp.setTitle("Eintrag ändern");
        PopUp.setAlwaysOnTop(true);
        PopUp.setResizable(false);

        GridPane Gitter = new GridPane();
        Gitter.setHgap(10);
        Gitter.setVgap(10);

        Label LabelA = new Label("BehoerdensID");
        Label LabelAWert = new Label(Integer.toString(Auswahl.getBehoerdenID()));

        Label LabelB = new Label("Name");
        TextField LabelBWert = new TextField();

        Label LabelC = new Label("Typ");
        TextField LabelCWert = new TextField();

        Label LabelD = new Label("Bezirk");
        Label LabelDWert = new Label(Auswahl.getBezirkName());

        Label LabelE = new Label("BezirksID");
        TextField LabelEWert = new TextField();

        LabelEWert.textProperty().addListener(((observable, oldValue, newValue) -> {
            try {
                PreparedStatement NutzerInput = DH.prepareStatement("SELECT Name FROM BEZIRK WHERE BezirksID = ?");
                NutzerInput.setInt(1, Integer.parseInt(newValue));
                ResultSet Antwort = NutzerInput.executeQuery();
                if (Antwort.next()) {
                    LabelDWert.setText(Antwort.getString(1));
                } else {
                    LabelDWert.setText("Ungültige BezirksID");
                }
            } catch (SQLException e) {
                IM.setErrorText("Unbekannter SQL Fehler", e);
            } catch (NumberFormatException e) {
                LabelDWert.setText("BezirksID muss eine Zahl sein");
            }
        }));

        LabelBWert.setText(Auswahl.getName());
        LabelCWert.setText(Auswahl.getTyp());
        LabelEWert.setText(Integer.toString(Auswahl.getVerantwortlichBezirksID()));

        Button ButtonFort = new Button("Fortfahren");
        Button ButtonAbb = new Button("Abbrechen");

        ButtonFort.defaultButtonProperty();
        ButtonAbb.cancelButtonProperty();

        ButtonFort.setMaxWidth(Double.MAX_VALUE);
        ButtonAbb.setMaxWidth(Double.MAX_VALUE);

        Gitter.addColumn(0, LabelA, LabelB, LabelC, LabelD, LabelE);
        Gitter.addColumn(1, LabelAWert, LabelBWert, LabelCWert, LabelDWert, LabelEWert);

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
            String SQLString = "UPDATE BEHÖRDE SET Name=?, Typ=?, verantwortlich_für_BezirksID=?  WHERE BehördenID = " + Auswahl.getBehoerdenID();
            try {
                PreparedStatement SQLInjektionNeinNein = DH.prepareStatement(SQLString);
                SQLInjektionNeinNein.setString(1, LabelBWert.getText());
                SQLInjektionNeinNein.setString(2, LabelCWert.getText());
                SQLInjektionNeinNein.setInt(3, Integer.parseInt(LabelEWert.getText()));
                SQLInjektionNeinNein.executeUpdate();
                IM.setInfoText("Änderung durchgeführt");
            } catch (SQLException e) {
                IM.setErrorText("Ändern Fehlgeschlagen", e);
            }
            refreshBehoerdenAnsicht();
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    private void deleteSelectedEntrys() {
        ObservableList<BehoerdenDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        if (Nutzerauswahl.isEmpty()) {
            IM.setErrorText("Es muss mindestens ein Eintrag ausgewählt sein");
            return;
        }

        Nutzerauswahl.forEach(BehoerdenDaten -> {
            try {
                DH.getAnfrageObjekt().executeUpdate("DELETE FROM BEHÖRDE WHERE BehördenID = " + BehoerdenDaten.getBehoerdenID());
            } catch (SQLException e) {
                IM.setErrorText("Löschen fehlgeschlagen", e);
            }
        });
        refreshBehoerdenAnsicht();
    }

    public void SucheNachBezirk(int BezirksID) {
        Hauptprogramm.setMittlereAnsicht(getBehoerdenAnsicht());
        BehoerdenDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT BehördenID, BEHÖRDE.Name, Typ, verantwortlich_für_BezirksID, BEZIRK.Name " +
                    "FROM BEHÖRDE, BEZIRK WHERE verantwortlich_für_BezirksID = BEZIRK.BezirksID AND verantwortlich_für_BezirksID = " + BezirksID);
            while (AnfrageAntwort.next()) {
                BehoerdenDatenListe.add(new BehoerdenDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
                        AnfrageAntwort.getString(3), AnfrageAntwort.getInt(4), AnfrageAntwort.getString(5)));
            }
        } catch (SQLException e) {
            IM.setErrorText("Unbekannter Fehler bei aktualisieren der Ansicht", e);
        }
    }
}
