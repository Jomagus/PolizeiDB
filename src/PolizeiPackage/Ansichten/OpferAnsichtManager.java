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
public class OpferAnsichtManager {

    private DatenbankHandler DH;
    private InfoErrorManager IM;
    private Main Hauptprogramm;
    private TableView<OpferDaten> Tabelle;
    private BorderPane DatenAnsicht;
    private ObservableList<OpferDaten> OpferDatenListe;
    private boolean OpferAnsichtGeneriert;
    private PersonenAnsichtManager PersonenAM;
    private VerbrechenAnsichtManager VerbrechenAM;

    public OpferAnsichtManager(DatenbankHandler DBH, InfoErrorManager IEM, Main HauptFenster) {
        DH = DBH;
        IM = IEM;
        Hauptprogramm = HauptFenster;
        OpferDatenListe = FXCollections.observableArrayList();
        Tabelle = new TableView<>();
        OpferAnsichtGeneriert = false;
    }

    public void setPersonenAM(PersonenAnsichtManager personenAM) {
        PersonenAM = personenAM;
    }

    public void setVerbrechenAM(VerbrechenAnsichtManager verbrechenAM) {
        VerbrechenAM = verbrechenAM;
    }

    public Node getOpferAnsicht() {
        if (OpferAnsichtGeneriert) {
            refreshOpferAnsicht();
            return DatenAnsicht;
        }
        IM.setInfoText("Lade Opfer Ansicht");
        DatenAnsicht = new BorderPane(getOpferAnsichtInnereTabelle());

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

        IM.setInfoText("Laden der Opfer Ansicht erfolgreich");
        OpferAnsichtGeneriert = true;
        return DatenAnsicht;
    }

    /**
     * Erzeugt eine TableView mit den Daten aus der Datenbank.
     *
     * @return Eine TableView mit Opfer Daten
     */
    private Node getOpferAnsichtInnereTabelle() {
        refreshOpferAnsicht();

        // NameOpfer Spalte
        TableColumn<OpferDaten, String> SpalteName = new TableColumn<>("Opfer");
        SpalteName.setMinWidth(200);
        SpalteName.setCellValueFactory(new PropertyValueFactory<>("PersonenName"));

        // NameVerbrechen Spalte
        TableColumn<OpferDaten, String> SpalteDatum = new TableColumn<>("Verbrechen");
        SpalteDatum.setMinWidth(200);
        SpalteDatum.setCellValueFactory(new PropertyValueFactory<>("VerbrechenName"));

        Tabelle.setItems(OpferDatenListe);
        Tabelle.getColumns().add(SpalteName);
        Tabelle.getColumns().add(SpalteDatum);

        Tabelle.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Doppelklicke auf Spalten sollen Detailansichten oeffnen:
        Tabelle.setRowFactory(param -> {
            TableRow<OpferDaten> Spalte = new TableRow<>();
            Spalte.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!Spalte.isEmpty())) {
                    erzeugeDetailAnsicht(Spalte.getItem());
                }
            });
            return Spalte;
        });

        return Tabelle;
    }

    private void erzeugeDetailAnsicht(OpferDaten SpaltenDaten) {
        Label LabelA = new Label("PersonenID");
        Label LabelAWert = new Label(Integer.toString(SpaltenDaten.getPersonenID()));

        Label LabelB = new Label("Opfer");
        Label LabelBWert = new Label(SpaltenDaten.getPersonenName());

        Label LabelC = new Label("Verbrechen");
        Label LabelCWert = new Label(SpaltenDaten.getVerbrechenName());

        Label LabelD = new Label("VerbrechensID");
        Label LabelDWert = new Label(Integer.toString(SpaltenDaten.getVerbrechensID()));

        Button ButtonBearbeiten = new Button("Bearbeiten...");
        Button ButtonLoeschen = new Button("Löschen");
        Button ButtonSucheOpfersId = new Button("Suche nach Opfer");
        Button ButtonSucheBezirksId = new Button("Suche nach Verbrechen");
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
        ButtonSucheOpfersId.setOnAction(event -> {
            Hauptprogramm.setRechteAnsicht(null);
            PersonenAM.PersonenSuchAnsicht(SpaltenDaten.getPersonenID());
        });
        ButtonSucheBezirksId.setOnAction(event -> {
            Hauptprogramm.setRechteAnsicht(null);
            VerbrechenAM.SucheNachVerbrechen(SpaltenDaten.getVerbrechensID());
        });

        ButtonClose.setOnAction(event -> Hauptprogramm.setRechteAnsicht(null));

        ButtonBearbeiten.setMaxWidth(Double.MAX_VALUE);
        ButtonBearbeiten.setMinWidth(150);
        ButtonLoeschen.setMaxWidth(Double.MAX_VALUE);
        ButtonLoeschen.setMinWidth(150);
        ButtonSucheOpfersId.setMaxWidth(Double.MAX_VALUE);
        ButtonSucheBezirksId.setMaxWidth(Double.MAX_VALUE);
        ButtonClose.setMaxWidth(Double.MAX_VALUE);

        // Wir haben ein Gridpane oben, eine HBox unten in einer VBox in einem ScrollPane
        GridPane Oben = new GridPane();
        Oben.setHgap(10);
        Oben.setVgap(10);
        Oben.addColumn(0, LabelA, LabelB, LabelC, LabelD);
        Oben.addColumn(1, LabelAWert, LabelBWert, LabelCWert, LabelDWert);
        Oben.getColumnConstraints().add(new ColumnConstraints(100));
        Oben.getColumnConstraints().add(new ColumnConstraints(200));

        HBox Unten = new HBox(10);
        Unten.getChildren().addAll(ButtonBearbeiten, ButtonLoeschen);
        Unten.setMaxWidth(300);
        Unten.alignmentProperty().setValue(Pos.CENTER);

        VBox Mittelteil = new VBox(10);
        Mittelteil.setPadding(new Insets(10, 20, 10, 10));
        Mittelteil.getChildren().addAll(Oben, Unten, ButtonSucheOpfersId, ButtonSucheBezirksId, ButtonClose);

        ScrollPane Aussen = new ScrollPane();

        Aussen.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        Aussen.setContent(Mittelteil);

        Hauptprogramm.setRechteAnsicht(Aussen);
    }

    /**
     * Aktualisiert die JavaFX vorliegenden Daten mit Daten aus der Datenbank.
     */
    public void refreshOpferAnsicht() {
        OpferDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT SIND_OPFER.PersonenID, PERSON.Name, SIND_OPFER.VerbrechensID, VERBRECHEN.Name " +
                    "FROM SIND_OPFER, PERSON, VERBRECHEN " +
                    "WHERE SIND_OPFER.PersonenID = PERSON.PersonenID AND SIND_OPFER.VerbrechensID = VERBRECHEN.VerbrechensID;");
            while (AnfrageAntwort.next()) {
                OpferDatenListe.add(new OpferDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
                        AnfrageAntwort.getInt(3), AnfrageAntwort.getString(4)));
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

        Label LabelD = new Label("Opfer");
        Label LabelDWert = new Label();

        Label LabelE = new Label("PersonenID");
        TextField LabelEWert = new TextField();

        LabelEWert.textProperty().addListener(((observable, oldValue, newValue) -> {
            try {
                PreparedStatement NutzerInput = DH.prepareStatement("SELECT Name FROM PERSON WHERE PersonenID = ?");
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

        Label LabelF = new Label("Verbrechen");
        Label LabelFWert = new Label();

        Label LabelG = new Label("VerbrechensID");
        TextField LabelGWert = new TextField();

        LabelGWert.textProperty().addListener(((observable, oldValue, newValue) -> {
            try {
                PreparedStatement NutzerInput = DH.prepareStatement("SELECT Name FROM VERBRECHEN WHERE VerbrechensID = ?");
                NutzerInput.setInt(1, Integer.parseInt(newValue));
                ResultSet Antwort = NutzerInput.executeQuery();
                if (Antwort.next()) {
                    LabelFWert.setText(Antwort.getString(1));
                } else {
                    LabelFWert.setText("Ungültige FallID");
                }
            } catch (SQLException e) {
                IM.setErrorText("Unbekannter SQL Fehler", e);
            } catch (NumberFormatException e) {
                LabelFWert.setText("FallID muss eine Zahl sein");
            }
        }));

        Button ButtonFort = new Button("Fortfahren");
        Button ButtonAbb = new Button("Abbrechen");

        ButtonFort.defaultButtonProperty();
        ButtonAbb.cancelButtonProperty();

        ButtonFort.setMaxWidth(Double.MAX_VALUE);
        ButtonAbb.setMaxWidth(Double.MAX_VALUE);

        Gitter.addColumn(0, LabelD, LabelE, LabelF, LabelG);
        Gitter.addColumn(1, LabelDWert, LabelEWert, LabelFWert, LabelGWert);

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
            String SQLString = "INSERT INTO SIND_OPFER (PersonenID, VerbrechensID) VALUES (?, ?)";
            try {
                PreparedStatement InsertStatement = DH.prepareStatement(SQLString);
                InsertStatement.setString(1, LabelEWert.getText());
                InsertStatement.setString(2, LabelGWert.getText());
                InsertStatement.executeUpdate();
                IM.setInfoText("Einfügen durchgeführt");
            } catch (SQLException e) {
                IM.setErrorText("Einfügen Fehlgeschlagen", e);
            }
            refreshOpferAnsicht();
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    private void updateSelectedEntry() {
        ObservableList<OpferDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        if (Nutzerauswahl.size() != 1) {
            IM.setErrorText("Es muss genau ein Element ausgewählt werden");
            return;
        }
        OpferDaten Auswahl = Nutzerauswahl.get(0);

        // Jetzt erzeugen wir ein PopUp zum veraendern des Eintrags

        Stage PopUp = new Stage();
        PopUp.initModality(Modality.APPLICATION_MODAL);
        PopUp.setTitle("Eintrag ändern");
        PopUp.setAlwaysOnTop(true);
        PopUp.setResizable(false);

        GridPane Gitter = new GridPane();
        Gitter.setHgap(10);
        Gitter.setVgap(10);

        Label LabelD = new Label("Opfer");
        Label LabelDWert = new Label();

        Label LabelE = new Label("PersonenID");
        TextField LabelEWert = new TextField();

        LabelEWert.textProperty().addListener(((observable, oldValue, newValue) -> {
            try {
                PreparedStatement NutzerInput = DH.prepareStatement("SELECT Name FROM PERSON WHERE PersonenID = ?");
                NutzerInput.setInt(1, Integer.parseInt(newValue));
                ResultSet Antwort = NutzerInput.executeQuery();
                if (Antwort.next()) {
                    LabelDWert.setText(Antwort.getString(1));
                } else {
                    LabelDWert.setText("Ungültige PersonenID");
                }
            } catch (SQLException e) {
                IM.setErrorText("Unbekannter SQL Fehler", e);
            } catch (NumberFormatException e) {
                LabelDWert.setText("BezirksID muss eine Zahl sein");
            }
        }));

        Label LabelF = new Label("Verbrechen");
        Label LabelFWert = new Label();

        Label LabelG = new Label("VerbrechensID");
        TextField LabelGWert = new TextField();

        LabelGWert.textProperty().addListener(((observable, oldValue, newValue) -> {
            try {
                PreparedStatement NutzerInput = DH.prepareStatement("SELECT Name FROM VERBRECHEN WHERE VerbrechensID = ?");
                NutzerInput.setInt(1, Integer.parseInt(newValue));
                ResultSet Antwort = NutzerInput.executeQuery();
                if (Antwort.next()) {
                    LabelFWert.setText(Antwort.getString(1));
                } else {
                    LabelFWert.setText("Ungültige VerbrechensID");
                }
            } catch (SQLException e) {
                IM.setErrorText("Unbekannter SQL Fehler", e);
            } catch (NumberFormatException e) {
                LabelFWert.setText("FallID muss eine Zahl sein");
            }
        }));

        LabelEWert.setText(Integer.toString(Auswahl.getPersonenID()));
        LabelGWert.setText(Integer.toString(Auswahl.getVerbrechensID()));

        Button ButtonFort = new Button("Fortfahren");
        Button ButtonAbb = new Button("Abbrechen");

        ButtonFort.defaultButtonProperty();
        ButtonAbb.cancelButtonProperty();

        ButtonFort.setMaxWidth(Double.MAX_VALUE);
        ButtonAbb.setMaxWidth(Double.MAX_VALUE);

        Gitter.addColumn(0, LabelD, LabelE, LabelF, LabelG);
        Gitter.addColumn(1, LabelDWert, LabelEWert, LabelFWert, LabelGWert);

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
            String SQLString = "UPDATE SIND_OPFER SET PersonenID=?, VerbrechensID=? WHERE PersonenID = " + Auswahl.getPersonenID() + "AND VerbrechensID = " + Auswahl.getVerbrechensID();
            try {
                PreparedStatement SQLInjektionNeinNein = DH.prepareStatement(SQLString);
                SQLInjektionNeinNein.setInt(1, Integer.parseInt(LabelEWert.getText()));
                SQLInjektionNeinNein.setInt(2, Integer.parseInt(LabelGWert.getText()));
                SQLInjektionNeinNein.executeUpdate();
                IM.setInfoText("Änderung durchgeführt");
            } catch (SQLException e) {
                IM.setErrorText("Ändern Fehlgeschlagen", e);
            }
            refreshOpferAnsicht();
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    private void deleteSelectedEntrys() {
        ObservableList<OpferDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        if (Nutzerauswahl.isEmpty()) {
            IM.setErrorText("Es muss mindestens ein Eintrag ausgewählt sein");
            return;
        }

        Nutzerauswahl.forEach(OpferDaten -> {
            try {
                DH.getAnfrageObjekt().executeUpdate("DELETE FROM SIND_OPFER  WHERE PersonenID = " + OpferDaten.getPersonenID() + "AND VerbrechensID = " + OpferDaten.getVerbrechensID());
            } catch (SQLException e) {
                IM.setErrorText("Löschen fehlgeschlagen", e);
            }
        });
        refreshOpferAnsicht();
    }

    public void SucheNachPerson(int PersonenID) {
        Hauptprogramm.setMittlereAnsicht(getOpferAnsicht());
        OpferDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT SIND_OPFER.PersonenID, PERSON.Name, SIND_OPFER.VerbrechensID, VERBRECHEN.Name " +
                    "FROM SIND_OPFER, PERSON, VERBRECHEN " +
                    "WHERE SIND_OPFER.PersonenID = PERSON.PersonenID AND SIND_OPFER.VerbrechensID = VERBRECHEN.VerbrechensID AND SIND_OPFER.PersonenID = " + PersonenID);
            while (AnfrageAntwort.next()) {
                OpferDatenListe.add(new OpferDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
                        AnfrageAntwort.getInt(3), AnfrageAntwort.getString(4)));
            }
        } catch (SQLException e) {
            IM.setErrorText("Unbekannter Fehler bei aktualisieren der Ansicht", e);
        }
    }

    public void SucheNachVerbrechen(int VerbrechensID) {
        Hauptprogramm.setMittlereAnsicht(getOpferAnsicht());
        OpferDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT SIND_OPFER.PersonenID, PERSON.Name, SIND_OPFER.VerbrechensID, VERBRECHEN.Name " +
                    "FROM SIND_OPFER, PERSON, VERBRECHEN " +
                    "WHERE SIND_OPFER.PersonenID = PERSON.PersonenID AND SIND_OPFER.VerbrechensID = VERBRECHEN.VerbrechensID AND SIND_OPFER.VerbrechensID = " + VerbrechensID);
            while (AnfrageAntwort.next()) {
                OpferDatenListe.add(new OpferDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
                        AnfrageAntwort.getInt(3), AnfrageAntwort.getString(4)));
            }
        } catch (SQLException e) {
            IM.setErrorText("Unbekannter Fehler bei aktualisieren der Ansicht", e);
        }
    }
}
