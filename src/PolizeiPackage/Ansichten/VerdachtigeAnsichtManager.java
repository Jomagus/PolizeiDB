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
 * Liefert Tabelle fuer die Verdaechtigen
 */
public class VerdachtigeAnsichtManager {

    private DatenbankHandler DH;
    private InfoErrorManager IM;
    private Main Hauptprogramm;
    private TableView<VerdachtigeDaten> Tabelle;
    private BorderPane DatenAnsicht;
    private ObservableList<VerdachtigeDaten> VerdachtigeDatenListe;
    private boolean VerdachtigeAnsichtGeneriert;
    private VerbrechenAnsichtManager VerbrechenAM;
    private PersonenAnsichtManager PersonenAM;

    public VerdachtigeAnsichtManager(DatenbankHandler DBH, InfoErrorManager IEM, Main HauptFenster) {
        DH = DBH;
        IM = IEM;
        Hauptprogramm = HauptFenster;
        VerdachtigeDatenListe = FXCollections.observableArrayList();
        Tabelle = new TableView<>();
        VerdachtigeAnsichtGeneriert = false;
    }

    public void setVerbrechenAM(VerbrechenAnsichtManager verbrechenAM) {
        VerbrechenAM = verbrechenAM;
    }

    public void setPersonenAM(PersonenAnsichtManager personenAM) {
        PersonenAM = personenAM;
    }

    public Node getVerdachtigeAnsicht() {
        if (VerdachtigeAnsichtGeneriert) {
            refreshVerdachtigeAnsicht();
            return DatenAnsicht;
        }
        IM.setInfoText("Lade Verdachtige Ansicht");
        DatenAnsicht = new BorderPane(getVerdachtigeAnsichtInnereTabelle());

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

        IM.setInfoText("Laden der Verdachtige Ansicht erfolgreich");
        VerdachtigeAnsichtGeneriert = true;
        return DatenAnsicht;
    }

    /**
     * Erzeugt eine TableView mit den Daten aus der Datenbank.
     *
     * @return Eine TableView mit Verdachtige Daten
     */
    private Node getVerdachtigeAnsichtInnereTabelle() {
        refreshVerdachtigeAnsicht();

        // Person Spalte
        TableColumn<VerdachtigeDaten, String> SpalteName = new TableColumn<>("Verdächtige");
        SpalteName.setMinWidth(200);
        SpalteName.setCellValueFactory(new PropertyValueFactory<>("PersonenName"));

        // Verbrechen Spalte
        TableColumn<VerdachtigeDaten, String> SpalteDatum = new TableColumn<>("Verbrechen");
        SpalteDatum.setMinWidth(200);
        SpalteDatum.setCellValueFactory(new PropertyValueFactory<>("VerbrechensName"));

        // Bezirk Spalte
        TableColumn<VerdachtigeDaten, String> SpalteBezirk = new TableColumn<>("Überführt");
        SpalteBezirk.setMinWidth(200);
        SpalteBezirk.setCellValueFactory(new PropertyValueFactory<>("UberfuhrtString"));

        Tabelle.setItems(VerdachtigeDatenListe);
        Tabelle.getColumns().add(SpalteName);
        Tabelle.getColumns().add(SpalteDatum);
        Tabelle.getColumns().add(SpalteBezirk);

        Tabelle.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Doppelklicke auf Spalten sollen Detailansichten oeffnen:
        Tabelle.setRowFactory(param -> {
            TableRow<VerdachtigeDaten> Spalte = new TableRow<>();
            Spalte.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!Spalte.isEmpty())) {
                    erzeugeDetailAnsicht(Spalte.getItem());
                }
            });
            return Spalte;
        });

        return Tabelle;
    }

    private void erzeugeDetailAnsicht(VerdachtigeDaten SpaltenDaten) {
        Label LabelA = new Label("PersonenID");
        Label LabelAWert = new Label(Integer.toString(SpaltenDaten.getPersonenID()));

        Label LabelB = new Label("Verdächtige");
        Label LabelBWert = new Label(SpaltenDaten.getPersonenName());

        Label LabelC = new Label("VerbrechensID");
        Label LabelCWert = new Label(Integer.toString(SpaltenDaten.getVerbrechensID()));

        Label LabelD = new Label("Verbrechen");
        Label LabelDWert = new Label(SpaltenDaten.getVerbrechensName());

        Label LabelE = new Label("Überführt");
        Label LabelEWert = new Label(SpaltenDaten.getUberfuhrtString());

        Button ButtonBearbeiten = new Button("Bearbeiten...");
        Button ButtonLoeschen = new Button("Löschen");
        Button ButtonSucheVerdachtigesId = new Button("Suche nach Verdächtigem");
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
        ButtonSucheVerdachtigesId.setOnAction(event -> {
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
        ButtonSucheVerdachtigesId.setMaxWidth(Double.MAX_VALUE);
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
        Mittelteil.getChildren().addAll(Oben, Unten, ButtonSucheVerdachtigesId, ButtonSucheBezirksId, ButtonClose);

        ScrollPane Aussen = new ScrollPane();

        Aussen.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        Aussen.setContent(Mittelteil);

        Hauptprogramm.setRechteAnsicht(Aussen);
    }

    /**
     * Aktualisiert die JavaFX vorliegenden Daten mit Daten aus der Datenbank.
     */
    public void refreshVerdachtigeAnsicht() {
        VerdachtigeDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT SIND_VERDÄCHTIGE.PersonenID, PERSON.Name, SIND_VERDÄCHTIGE.VerbrechensID, VERBRECHEN.Name, SIND_VERDÄCHTIGE.Überführt " +
                    "FROM SIND_VERDÄCHTIGE, PERSON, VERBRECHEN " +
                    "WHERE SIND_VERDÄCHTIGE.PersonenID = PERSON.PersonenID AND SIND_VERDÄCHTIGE.VerbrechensID = VERBRECHEN.VerbrechensID;");
            while (AnfrageAntwort.next()) {
                VerdachtigeDatenListe.add(new VerdachtigeDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
                        AnfrageAntwort.getInt(3), AnfrageAntwort.getString(4), AnfrageAntwort.getBoolean(5)));
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

        Label LabelD = new Label("Verdächtige");
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

        Label LabelI = new Label("Überführt");
        CheckBox LabelIWert = new CheckBox();
        LabelIWert.setAllowIndeterminate(false);

        Button ButtonFort = new Button("Fortfahren");
        Button ButtonAbb = new Button("Abbrechen");

        ButtonFort.defaultButtonProperty();
        ButtonAbb.cancelButtonProperty();

        ButtonFort.setMaxWidth(Double.MAX_VALUE);
        ButtonAbb.setMaxWidth(Double.MAX_VALUE);

        Gitter.addColumn(0, LabelD, LabelE, LabelF, LabelG, LabelI);
        Gitter.addColumn(1, LabelDWert, LabelEWert, LabelFWert, LabelGWert, LabelIWert);

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
            String SQLString = "INSERT INTO SIND_VERDÄCHTIGE (PersonenID, VerbrechensID, Überführt) VALUES (?, ?, ?)";
            try {
                PreparedStatement InsertStatement = DH.prepareStatement(SQLString);
                InsertStatement.setInt(1, Integer.parseInt(LabelEWert.getText()));
                InsertStatement.setInt(2, Integer.parseInt(LabelGWert.getText()));
                InsertStatement.setBoolean(3, LabelIWert.isSelected());
                InsertStatement.executeUpdate();
                IM.setInfoText("Einfügen durchgeführt");
            } catch (SQLException e) {
                IM.setErrorText("Einfügen Fehlgeschlagen", e);
            }
            refreshVerdachtigeAnsicht();
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    private void updateSelectedEntry() {
        ObservableList<VerdachtigeDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        if (Nutzerauswahl.size() != 1) {
            IM.setErrorText("Es muss genau ein Element ausgewählt werden");
            return;
        }
        VerdachtigeDaten Auswahl = Nutzerauswahl.get(0);

        // Jetzt erzeugen wir ein PopUp zum veraendern des Eintrags

        Stage PopUp = new Stage();
        PopUp.initModality(Modality.APPLICATION_MODAL);
        PopUp.setTitle("Eintrag ändern");
        PopUp.setAlwaysOnTop(true);
        PopUp.setResizable(false);

        GridPane Gitter = new GridPane();
        Gitter.setHgap(10);
        Gitter.setVgap(10);

        Label LabelD = new Label("Verdächtige");
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

        Label LabelI = new Label("Überführt");
        CheckBox LabelIWert = new CheckBox();
        LabelIWert.setAllowIndeterminate(false);

        LabelEWert.setText(Integer.toString(Auswahl.getPersonenID()));
        LabelGWert.setText(Integer.toString(Auswahl.getVerbrechensID()));
        LabelIWert.setSelected(Auswahl.getUberfuhrt());

        Button ButtonFort = new Button("Fortfahren");
        Button ButtonAbb = new Button("Abbrechen");

        ButtonFort.defaultButtonProperty();
        ButtonAbb.cancelButtonProperty();

        ButtonFort.setMaxWidth(Double.MAX_VALUE);
        ButtonAbb.setMaxWidth(Double.MAX_VALUE);

        Gitter.addColumn(0, LabelD, LabelE, LabelF, LabelG, LabelI);
        Gitter.addColumn(1, LabelDWert, LabelEWert, LabelFWert, LabelGWert, LabelIWert);

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
            String SQLString = "UPDATE SIND_VERDÄCHTIGE SET PersonenID=?, VerbrechensID=?, Überführt=? " +
                    "WHERE PersonenID = " + Auswahl.getPersonenID() + " AND VerbrechensID = " + Auswahl.getVerbrechensID();
            try {
                PreparedStatement SQLInjektionNeinNein = DH.prepareStatement(SQLString);
                SQLInjektionNeinNein.setInt(1, Integer.parseInt(LabelEWert.getText()));
                SQLInjektionNeinNein.setInt(2, Integer.parseInt(LabelGWert.getText()));
                SQLInjektionNeinNein.setBoolean(3, LabelIWert.isSelected());
                SQLInjektionNeinNein.executeUpdate();
                IM.setInfoText("Änderung durchgeführt");
            } catch (SQLException e) {
                IM.setErrorText("Ändern Fehlgeschlagen", e);
            }
            refreshVerdachtigeAnsicht();
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    private void deleteSelectedEntrys() {
        ObservableList<VerdachtigeDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        if (Nutzerauswahl.isEmpty()) {
            IM.setErrorText("Es muss mindestens ein Eintrag ausgewählt sein");
            return;
        }

        Nutzerauswahl.forEach(VerdachtigeDaten -> {
            try {
                DH.getAnfrageObjekt().executeUpdate("DELETE FROM SIND_VERDÄCHTIGE WHERE PersonenID = " + VerdachtigeDaten.getPersonenID() +
                        " AND VerbrechensID = " + VerdachtigeDaten.getVerbrechensID());
            } catch (SQLException e) {
                IM.setErrorText("Löschen fehlgeschlagen", e);
            }
        });
        refreshVerdachtigeAnsicht();
    }

    public void SuchePerson(int PersonenID) {
        Hauptprogramm.setMittlereAnsicht(getVerdachtigeAnsicht());
        VerdachtigeDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT SIND_VERDÄCHTIGE.PersonenID, PERSON.Name, SIND_VERDÄCHTIGE.VerbrechensID, VERBRECHEN.Name, SIND_VERDÄCHTIGE.Überführt " +
                    "FROM SIND_VERDÄCHTIGE, PERSON, VERBRECHEN " +
                    "WHERE SIND_VERDÄCHTIGE.PersonenID = PERSON.PersonenID AND SIND_VERDÄCHTIGE.VerbrechensID = VERBRECHEN.VerbrechensID AND SIND_VERDÄCHTIGE.PersonenID = " + PersonenID);
            while (AnfrageAntwort.next()) {
                VerdachtigeDatenListe.add(new VerdachtigeDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
                        AnfrageAntwort.getInt(3), AnfrageAntwort.getString(4), AnfrageAntwort.getBoolean(5)));
            }
        } catch (SQLException e) {
            IM.setErrorText("Unbekannter Fehler bei aktualisieren der Ansicht", e);
        }
    }

    public void SucheVerbrechen(int VerbrechensID) {
        Hauptprogramm.setMittlereAnsicht(getVerdachtigeAnsicht());
        VerdachtigeDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT SIND_VERDÄCHTIGE.PersonenID, PERSON.Name, SIND_VERDÄCHTIGE.VerbrechensID, VERBRECHEN.Name, SIND_VERDÄCHTIGE.Überführt " +
                    "FROM SIND_VERDÄCHTIGE, PERSON, VERBRECHEN " +
                    "WHERE SIND_VERDÄCHTIGE.PersonenID = PERSON.PersonenID AND SIND_VERDÄCHTIGE.VerbrechensID = VERBRECHEN.VerbrechensID AND SIND_VERDÄCHTIGE.VerbrechensID = " + VerbrechensID);
            while (AnfrageAntwort.next()) {
                VerdachtigeDatenListe.add(new VerdachtigeDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
                        AnfrageAntwort.getInt(3), AnfrageAntwort.getString(4), AnfrageAntwort.getBoolean(5)));
            }
        } catch (SQLException e) {
            IM.setErrorText("Unbekannter Fehler bei aktualisieren der Ansicht", e);
        }
    }

    public void ZeigeSuchResultate(ResultSet R) {
        Hauptprogramm.setMittlereAnsicht(getVerdachtigeAnsicht());
        VerdachtigeDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            AnfrageAntwort = R;
            while (AnfrageAntwort.next()) {
                VerdachtigeDatenListe.add(new VerdachtigeDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
                        AnfrageAntwort.getInt(3), AnfrageAntwort.getString(4), AnfrageAntwort.getBoolean(5)));
            }
        } catch (SQLException e) {
            IM.setErrorText("Unbekannter Fehler bei aktualisieren der Ansicht", e);
        }
    }
}
