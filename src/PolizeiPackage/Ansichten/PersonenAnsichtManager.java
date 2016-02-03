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
import javafx.util.Callback;
import sun.util.locale.provider.SPILocaleProviderAdapter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Liefert Tabelle fuer die Art
 */
public class PersonenAnsichtManager {

    private DatenbankHandler DH;
    private InfoErrorManager IM;
    private Main Hauptprogramm;
    private TableView<PersonenDaten> Tabelle;
    private BorderPane DatenAnsicht;
    private ObservableList<PersonenDaten> PersonenDatenListe;
    private boolean PersonenAnsichtGeneriert;
    private OpferAnsichtManager OpferAM;
    private VerdachtigeAnsichtManager VerAM;

    public PersonenAnsichtManager(DatenbankHandler DBH, InfoErrorManager IEM, Main HauptFenster) {
        DH = DBH;
        IM = IEM;
        Hauptprogramm = HauptFenster;
        PersonenDatenListe = FXCollections.observableArrayList();
        Tabelle = new TableView<>();
        PersonenAnsichtGeneriert = false;
    }

    public void setOpferAM(OpferAnsichtManager opferAM) {
        OpferAM = opferAM;
    }

    public void setVerAM(VerdachtigeAnsichtManager verAM) {
        VerAM = verAM;
    }

    public Node getPersonenAnsicht() {
        if (PersonenAnsichtGeneriert) {
            refreshPersonenAnsicht();
            return DatenAnsicht;
        }
        IM.setInfoText("Lade Personen Ansicht");
        DatenAnsicht = new BorderPane(getPersonenAnsichtInnereTabelle());

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

        IM.setInfoText("Laden der Personen Ansicht erfolgreich");
        PersonenAnsichtGeneriert = true;
        return DatenAnsicht;
    }

    /**
     * Erzeugt eine TableView mit den Daten aus der Datenbank.
     *
     * @return Eine TableView mit Personen Daten
     */
    private Node getPersonenAnsichtInnereTabelle() {
        refreshPersonenAnsicht();

        // Name Spalte
        TableColumn<PersonenDaten, String> SpalteName = new TableColumn<>("Name");
        SpalteName.setMinWidth(200);
        SpalteName.setCellValueFactory(new PropertyValueFactory<>("Name"));

        // GebDatum Spalte
        TableColumn<PersonenDaten, String> SpalteDatum = new TableColumn<>("Geburtsdatum");
        SpalteDatum.setMinWidth(200);
        SpalteDatum.setCellValueFactory(new PropertyValueFactory<>("GebDatum"));

        // Bezirk Spalte
        TableColumn<PersonenDaten, String> SpalteNation = new TableColumn<>("Nationalität");
        SpalteNation.setMinWidth(200);
        SpalteNation.setCellValueFactory(new PropertyValueFactory<>("Nation"));

        // Fall Spalte
        TableColumn<PersonenDaten, String> SpalteGeschlecht = new TableColumn<>("Geschlecht");
        SpalteGeschlecht.setMinWidth(200);
        SpalteGeschlecht.setCellValueFactory(new PropertyValueFactory<>("Geschlecht"));

        // Art Spalte
        TableColumn<PersonenDaten, String> SpalteTod = new TableColumn<>("Todesdatum");
        SpalteTod.setMinWidth(200);
        SpalteTod.setCellValueFactory(new PropertyValueFactory<>("TodDatum"));

        Tabelle.setItems(PersonenDatenListe);
        Tabelle.getColumns().add(SpalteName);
        Tabelle.getColumns().add(SpalteDatum);
        Tabelle.getColumns().add(SpalteNation);
        Tabelle.getColumns().add(SpalteGeschlecht);
        Tabelle.getColumns().add(SpalteTod);

        Tabelle.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Doppelklicke auf Spalten sollen Detailansichten oeffnen:
        Tabelle.setRowFactory(param -> {
            TableRow<PersonenDaten> Spalte = new TableRow<>();
            Spalte.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!Spalte.isEmpty())) {
                    erzeugeDetailAnsicht(Spalte.getItem());
                }
            });
            return Spalte;
        });

        return Tabelle;
    }

    private void erzeugeDetailAnsicht(PersonenDaten SpaltenDaten) {
        Label LabelA = new Label("PersonenID");
        Label LabelAWert = new Label(Integer.toString(SpaltenDaten.getPersonenID()));

        Label LabelB = new Label("Name");
        Label LabelBWert = new Label(SpaltenDaten.getName());

        Label LabelC = new Label("Geburtsdatum");
        Label LabelCWert = new Label(SpaltenDaten.getGebDatum());

        Label LabelD = new Label("Nationalität");
        Label LabelDWert = new Label(SpaltenDaten.getNation());

        Label LabelE = new Label("Geschlecht");
        Label LabelEWert = new Label(SpaltenDaten.getGeschlecht());

        Label LabelF = new Label("Todesdatum");
        Label LabelFWert = new Label(SpaltenDaten.getTodDatum());

        Button ButtonBearbeiten = new Button("Bearbeiten...");
        Button ButtonLoeschen = new Button("Löschen");
        Button ButtonSucheOpfer = new Button("Suche nach Rolle als Opfer");
        Button ButtonSucheVerd = new Button("Suche nach Rolle als Verdächtiger");
        Button ButtonPromote = new Button("Mache zum Polizisten");
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
        ButtonSucheOpfer.setOnAction(event -> {
            Hauptprogramm.setRechteAnsicht(null);
            OpferAM.SucheNachPerson(SpaltenDaten.getPersonenID());
        });
        ButtonSucheVerd.setOnAction(event -> {
            Hauptprogramm.setRechteAnsicht(null);
            VerAM.SuchePerson(SpaltenDaten.getPersonenID());
        });
        ButtonPromote.setOnAction(event -> {}); //TODO einen zum Polizisten mach knopf einfuegen

        ButtonClose.setOnAction(event -> Hauptprogramm.setRechteAnsicht(null));

        ButtonBearbeiten.setMaxWidth(Double.MAX_VALUE);
        ButtonBearbeiten.setMinWidth(150);
        ButtonLoeschen.setMaxWidth(Double.MAX_VALUE);
        ButtonLoeschen.setMinWidth(150);
        ButtonSucheOpfer.setMaxWidth(Double.MAX_VALUE);
        ButtonSucheVerd.setMaxWidth(Double.MAX_VALUE);
        ButtonPromote.setMaxWidth(Double.MAX_VALUE);
        ButtonClose.setMaxWidth(Double.MAX_VALUE);

        // Wir haben ein Gridpane oben, eine HBox unten in einer VBox in einem ScrollPane
        GridPane Oben = new GridPane();
        Oben.setHgap(10);
        Oben.setVgap(10);
        Oben.addColumn(0, LabelA, LabelB, LabelC, LabelD, LabelE, LabelF);
        Oben.addColumn(1, LabelAWert, LabelBWert, LabelCWert, LabelDWert, LabelEWert, LabelFWert);
        Oben.getColumnConstraints().add(new ColumnConstraints(100));
        Oben.getColumnConstraints().add(new ColumnConstraints(200));

        HBox Unten = new HBox(10);
        Unten.getChildren().addAll(ButtonBearbeiten, ButtonLoeschen);
        Unten.setMaxWidth(300);
        Unten.alignmentProperty().setValue(Pos.CENTER);

        VBox Mittelteil = new VBox(10);
        Mittelteil.setPadding(new Insets(10, 20, 10, 10));
        Mittelteil.getChildren().addAll(Oben, Unten, ButtonSucheOpfer, ButtonSucheVerd, ButtonPromote, ButtonClose);

        ScrollPane Aussen = new ScrollPane();

        Aussen.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        Aussen.setContent(Mittelteil);

        Hauptprogramm.setRechteAnsicht(Aussen);
    }

    /**
     * Aktualisiert die JavaFX vorliegenden Daten mit Daten aus der Datenbank.
     */
    public void refreshPersonenAnsicht() {
        PersonenDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT * FROM PERSON");
            while (AnfrageAntwort.next()) {
                String Todesdatum = "";
                if (AnfrageAntwort.getObject(6) != null) {
                    Todesdatum = AnfrageAntwort.getString(6);
                }
                PersonenDatenListe.add(new PersonenDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
                        AnfrageAntwort.getString(3), AnfrageAntwort.getString(4), AnfrageAntwort.getString(5), Todesdatum));
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

        Label LabelC = new Label("Geburtsdatum");
        DatePicker LabelCWert = new DatePicker();

        Label LabelD = new Label("Nationalität");
        TextField LabelDWert = new TextField();

        Label LabelE = new Label("Geschlecht");
        ComboBox LabelEWert = new ComboBox();

        Label LabelF = new Label("Todesdatum");
        DatePicker LabelFWert = new DatePicker();

        // wir erlauben nicht ein Todesdatum vor dem Geburtsdatum
        // nach https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/date-picker.htm

        final Callback<DatePicker, DateCell> TagesZellenFabtrik = new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(final DatePicker DP) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item.isBefore(LabelCWert.getValue().plusDays(1))) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }
                    }
                };
            }
        };
        LabelFWert.setDayCellFactory(TagesZellenFabtrik);

        LabelEWert.getItems().addAll("m", "w");
        LabelEWert.setValue("m");

        Button ButtonFort = new Button("Fortfahren");
        Button ButtonAbb = new Button("Abbrechen");

        ButtonFort.defaultButtonProperty();
        ButtonAbb.cancelButtonProperty();

        ButtonFort.setMaxWidth(Double.MAX_VALUE);
        ButtonAbb.setMaxWidth(Double.MAX_VALUE);

        Gitter.addColumn(0, LabelB, LabelC, LabelD, LabelE, LabelF);
        Gitter.addColumn(1, LabelBWert, LabelCWert, LabelDWert, LabelEWert, LabelFWert);

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
            String SQLString;
            if (LabelFWert.getValue() != null) {
                SQLString = "INSERT INTO PERSON (Name, Geburtsdatum, Nationalität, Geschlecht, Todesdatum) VALUES (?, ?, ?, ?, ?)";
            } else {
                SQLString = "INSERT INTO PERSON (Name, Geburtsdatum, Nationalität, Geschlecht) VALUES (?, ?, ?, ?)";
            }
            try {
                PreparedStatement InsertStatement = DH.prepareStatement(SQLString);
                InsertStatement.setString(1, LabelBWert.getText());
                InsertStatement.setString(2, LabelCWert.getValue().toString()); //TODO exception
                InsertStatement.setString(3, LabelDWert.getText());
                InsertStatement.setString(4, LabelEWert.getValue().toString());
                if (LabelFWert.getValue() != null) {
                    InsertStatement.setString(5, LabelFWert.getValue().toString());     //TODO exception
                }
                InsertStatement.executeUpdate();
                IM.setInfoText("Einfügen durchgeführt");
            } catch (SQLException e) {
                IM.setErrorText("Einfügen Fehlgeschlagen", e);
            }
            refreshPersonenAnsicht();
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    private void updateSelectedEntry() {
        ObservableList<PersonenDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        if (Nutzerauswahl.size() != 1) {
            IM.setErrorText("Es muss genau ein Element ausgewählt werden");
            return;
        }
        PersonenDaten Auswahl = Nutzerauswahl.get(0);

        // Jetzt erzeugen wir ein PopUp zum veraendern des Eintrags

        Stage PopUp = new Stage();
        PopUp.initModality(Modality.APPLICATION_MODAL);
        PopUp.setTitle("Neuer Eintrag");
        PopUp.setAlwaysOnTop(true);
        PopUp.setResizable(false);

        GridPane Gitter = new GridPane();
        Gitter.setHgap(10);
        Gitter.setVgap(10);

        Label LabelA = new Label("PersonenID");
        Label LabelAWert = new Label(Integer.toString(Auswahl.getPersonenID()));

        Label LabelB = new Label("Name");
        TextField LabelBWert = new TextField();

        Label LabelC = new Label("Geburtsdatum");
        DatePicker LabelCWert = new DatePicker();

        Label LabelD = new Label("Nationalität");
        TextField LabelDWert = new TextField();

        Label LabelE = new Label("Geschlecht");
        ComboBox LabelEWert = new ComboBox();

        Label LabelF = new Label("Todesdatum");
        DatePicker LabelFWert = new DatePicker();

        LabelEWert.getItems().addAll("m", "w");
        LabelEWert.setValue(Auswahl.getGeschlecht());

        final Callback<DatePicker, DateCell> TagesZellenFabtrik = new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(final DatePicker DP) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item.isBefore(LabelCWert.getValue().plusDays(1))) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }
                    }
                };
            }
        };
        LabelFWert.setDayCellFactory(TagesZellenFabtrik);

        LabelBWert.setText(Auswahl.getName());
        LabelCWert.setValue(LocalDate.parse(Auswahl.getGebDatum()));    //TODO exception
        LabelDWert.setText(Auswahl.getNation());
        if (!Auswahl.getTodDatum().isEmpty()) {
            LabelFWert.setValue(LocalDate.parse(Auswahl.getTodDatum()));    //TODO exception
        }

        Button ButtonFort = new Button("Fortfahren");
        Button ButtonAbb = new Button("Abbrechen");

        ButtonFort.defaultButtonProperty();
        ButtonAbb.cancelButtonProperty();

        ButtonFort.setMaxWidth(Double.MAX_VALUE);
        ButtonAbb.setMaxWidth(Double.MAX_VALUE);

        Gitter.addColumn(0, LabelA, LabelB, LabelC, LabelD, LabelE, LabelF);
        Gitter.addColumn(1, LabelAWert, LabelBWert, LabelCWert, LabelDWert, LabelEWert, LabelFWert);

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
            String SQLString;
            if (LabelFWert.getValue() == null) {
                SQLString = "UPDATE PERSON SET Name=?, Geburtsdatum=?, Nationalität=?, Geschlecht=? WHERE PersonenID = " + Auswahl.getPersonenID();
            } else {
                SQLString = "UPDATE PERSON SET Name=?, Geburtsdatum=?, Nationalität=?, Geschlecht=?, Todesdatum=? WHERE PersonenID = " + Auswahl.getPersonenID();
            }
            try {
                PreparedStatement InsertStatement = DH.prepareStatement(SQLString);
                InsertStatement.setString(1, LabelBWert.getText());
                InsertStatement.setString(2, LabelCWert.getValue().toString()); //TODO exception
                InsertStatement.setString(3, LabelDWert.getText());
                InsertStatement.setString(4, LabelEWert.getValue().toString());
                if (LabelFWert.getValue() != null) {
                    InsertStatement.setString(5, LabelFWert.getValue().toString()); //TODO exception
                }
                InsertStatement.executeUpdate();
                IM.setInfoText("Änderung durchgeführt");
            } catch (SQLException e) {
                IM.setErrorText("Ändern Fehlgeschlagen", e);
            }
            refreshPersonenAnsicht();
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    private void deleteSelectedEntrys() {
        ObservableList<PersonenDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        if (Nutzerauswahl.isEmpty()) {
            IM.setErrorText("Es muss mindestens ein Eintrag ausgewählt sein");
            return;
        }

        Nutzerauswahl.forEach(PersonenDaten -> {
            try {
                DH.getAnfrageObjekt().executeUpdate("DELETE FROM PERSON WHERE PersonenID = " + PersonenDaten.getPersonenID());
            } catch (SQLException e) {
                IM.setErrorText("Löschen fehlgeschlagen", e);
            }
        });
        refreshPersonenAnsicht();
    }

    /**
     * Setzt eine Personenansicht wo nur Personen mit dieser PersonenID angezeigt werden
     *
     * @param PID PersonenID die gesucht ist
     */
    public void PersonenSuchAnsicht(int PID) {
        Hauptprogramm.setMittlereAnsicht(getPersonenAnsicht());
        PersonenDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            PreparedStatement Anfrage = DH.prepareStatement("SELECT * FROM PERSON WHERE PersonenID = ?;");
            Anfrage.setInt(1, PID);
            AnfrageAntwort = Anfrage.executeQuery();
            while (AnfrageAntwort.next()) {
                PersonenDatenListe.add(new PersonenDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
                        AnfrageAntwort.getString(3), AnfrageAntwort.getString(4), AnfrageAntwort.getString(5), AnfrageAntwort.getString(6)));
            }
        } catch (SQLException e) {
            IM.setErrorText("Unbekannter Fehler beim Queransichtladen", e);
        }
    }
}
