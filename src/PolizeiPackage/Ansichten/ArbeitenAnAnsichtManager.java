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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Liefert Tabelle fuer die Art
 */
public class ArbeitenAnAnsichtManager {

    private DatenbankHandler DH;
    private InfoErrorManager IM;
    private Main Hauptprogramm;
    private TableView<ArbeitenAnDaten> Tabelle;
    private BorderPane DatenAnsicht;
    private ObservableList<ArbeitenAnDaten> ArbeitenAnDatenListe;
    private boolean ArbeitenAnAnsichtGeneriert;
    private PolizistAnsichtManager PolizistAM;
    private FallAnsichtManager FallAM;

    public ArbeitenAnAnsichtManager(DatenbankHandler DBH, InfoErrorManager IEM, Main HauptFenster) {
        DH = DBH;
        IM = IEM;
        Hauptprogramm = HauptFenster;
        ArbeitenAnDatenListe = FXCollections.observableArrayList();
        Tabelle = new TableView<>();
        ArbeitenAnAnsichtGeneriert = false;
    }

    public void setPolizistAM(PolizistAnsichtManager PAM) {
        PolizistAM = PAM;
    }

    public void setFallAM(FallAnsichtManager FAM) {
        FallAM = FAM;
    }

    public Node getArbeitenAnAnsicht() {
        if (ArbeitenAnAnsichtGeneriert) {
            refreshArbeitenAnAnsicht();
            return DatenAnsicht;
        }
        IM.setInfoText("Lade ArbeitenAn Ansicht");
        DatenAnsicht = new BorderPane(getArbeitenAnAnsichtInnereTabelle());

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

        IM.setInfoText("Laden der ArbeitenAn Ansicht erfolgreich");
        ArbeitenAnAnsichtGeneriert = true;
        return DatenAnsicht;
    }

    /**
     * Erzeugt eine TableView mit den Daten aus der Datenbank.
     *
     * @return Eine TableView mit ArbeitenAn Daten
     */
    private Node getArbeitenAnAnsichtInnereTabelle() {
        refreshArbeitenAnAnsicht();

        TableColumn<ArbeitenAnDaten, String> SpalteName = new TableColumn<>("Name");
        SpalteName.setMinWidth(200);
        SpalteName.setCellValueFactory(new PropertyValueFactory<>("PersonenName"));

        TableColumn<ArbeitenAnDaten, String> SpalteDatum = new TableColumn<>("Fall");
        SpalteDatum.setMinWidth(200);
        SpalteDatum.setCellValueFactory(new PropertyValueFactory<>("FallName"));

        TableColumn<ArbeitenAnDaten, String> SpalteBezirk = new TableColumn<>("von");
        SpalteBezirk.setMinWidth(200);
        SpalteBezirk.setCellValueFactory(new PropertyValueFactory<>("VonDatum"));

        TableColumn<ArbeitenAnDaten, String> SpalteFall = new TableColumn<>("bis");
        SpalteFall.setMinWidth(200);
        SpalteFall.setCellValueFactory(new PropertyValueFactory<>("BisDatum"));

        Tabelle.setItems(ArbeitenAnDatenListe);
        Tabelle.getColumns().add(SpalteName);
        Tabelle.getColumns().add(SpalteDatum);
        Tabelle.getColumns().add(SpalteBezirk);
        Tabelle.getColumns().add(SpalteFall);

        Tabelle.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Doppelklicke auf Spalten sollen Detailansichten oeffnen:
        Tabelle.setRowFactory(param -> {
            TableRow<ArbeitenAnDaten> Spalte = new TableRow<>();
            Spalte.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!Spalte.isEmpty())) {
                    erzeugeDetailAnsicht(Spalte.getItem());
                }
            });
            return Spalte;
        });

        return Tabelle;
    }

    private void erzeugeDetailAnsicht(ArbeitenAnDaten SpaltenDaten) {
        Label LabelA = new Label("PersonenID");
        Label LabelAWert = new Label(Integer.toString(SpaltenDaten.getPersonenID()));

        Label LabelB = new Label("Name");
        Label LabelBWert = new Label(SpaltenDaten.getPersonenName());

        Label LabelC = new Label("FallID");
        Label LabelCWert = new Label(Integer.toString(SpaltenDaten.getFallID()));

        Label LabelD = new Label("Fall");
        Label LabelDWert = new Label(SpaltenDaten.getFallName());

        Label LabelE = new Label("von");
        Label LabelEWert = new Label(SpaltenDaten.getVonDatum());

        Label LabelF = new Label("bis");
        Label LabelFWert = new Label(SpaltenDaten.getBisDatum());

        Button ButtonBearbeiten = new Button("Bearbeiten...");
        Button ButtonLoeschen = new Button("Löschen");
        Button ButtonSuchePersonenID = new Button("Suche nach Polizist");
        Button ButtonSucheFallId = new Button("Suche nach Fall");
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
        ButtonSuchePersonenID.setOnAction(event -> {
            Hauptprogramm.setRechteAnsicht(null);
            PolizistAM.SucheNachPolizist(SpaltenDaten.getPersonenID());
        });
        ButtonSucheFallId.setOnAction(event -> {
            Hauptprogramm.setRechteAnsicht(null);
            FallAM.FallSuchAnsicht(SpaltenDaten.getFallID());
        });
        ButtonClose.setOnAction(event -> Hauptprogramm.setRechteAnsicht(null));

        ButtonBearbeiten.setMaxWidth(Double.MAX_VALUE);
        ButtonBearbeiten.setMinWidth(150);
        ButtonLoeschen.setMaxWidth(Double.MAX_VALUE);
        ButtonLoeschen.setMinWidth(150);
        ButtonSuchePersonenID.setMaxWidth(Double.MAX_VALUE);
        ButtonSucheFallId.setMaxWidth(Double.MAX_VALUE);
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
        Mittelteil.getChildren().addAll(Oben, Unten, ButtonSuchePersonenID, ButtonSucheFallId, ButtonClose);

        ScrollPane Aussen = new ScrollPane();

        Aussen.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        Aussen.setContent(Mittelteil);

        Hauptprogramm.setRechteAnsicht(Aussen);
    }

    /**
     * Aktualisiert die JavaFX vorliegenden Daten mit Daten aus der Datenbank.
     */
    public void refreshArbeitenAnAnsicht() {
        ArbeitenAnDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT ARBEITEN_AN.PersonenID, PERSON.Name, ARBEITEN_AN.FallID, FALL.Name, ARBEITEN_AN.von, ARBEITEN_AN.bis " +
                    "FROM ARBEITEN_AN, PERSON, FALL WHERE ARBEITEN_AN.FallID = FALL.FallID AND ARBEITEN_AN.PersonenID = PERSON.PersonenID;");
            while (AnfrageAntwort.next()) {
                String BisDatum = "";
                if (AnfrageAntwort.getObject(6) != null) {
                    BisDatum = AnfrageAntwort.getString(6);
                }
                ArbeitenAnDatenListe.add(new ArbeitenAnDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
                        AnfrageAntwort.getInt(3), AnfrageAntwort.getString(4), AnfrageAntwort.getString(5), BisDatum));
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

        Label LabelD = new Label("Name");
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

        Label LabelF = new Label("Fall");
        Label LabelFWert = new Label();

        Label LabelG = new Label("FallID");
        TextField LabelGWert = new TextField();

        LabelGWert.textProperty().addListener(((observable, oldValue, newValue) -> {
            try {
                PreparedStatement NutzerInput = DH.prepareStatement("SELECT Name FROM FALL WHERE FallID= ?");
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

        Label LabelH = new Label("von");
        DatePicker LabelHWert = new DatePicker();

        Label LabelI = new Label("bis");
        DatePicker LabelIWert = new DatePicker();

        final Callback<DatePicker, DateCell> TagesZellenFabtrik = new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(final DatePicker DP) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item.isBefore(LabelHWert.getValue().plusDays(1))) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }
                    }
                };
            }
        };
        LabelIWert.setDayCellFactory(TagesZellenFabtrik);

        Button ButtonFort = new Button("Fortfahren");
        Button ButtonAbb = new Button("Abbrechen");

        ButtonFort.defaultButtonProperty();
        ButtonAbb.cancelButtonProperty();

        ButtonFort.setMaxWidth(Double.MAX_VALUE);
        ButtonAbb.setMaxWidth(Double.MAX_VALUE);

        Gitter.addColumn(0, LabelD, LabelE, LabelF, LabelG, LabelH, LabelI);
        Gitter.addColumn(1, LabelDWert, LabelEWert, LabelFWert, LabelGWert, LabelHWert, LabelIWert);

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
            if (LabelIWert.getValue() == null) {
                SQLString = "INSERT INTO ARBEITEN_AN (PersonenID, FallID, von) VALUES (?, ?, ?)";
            } else {
                SQLString = "INSERT INTO ARBEITEN_AN (PersonenID, FallID, von, bis) VALUES (?, ?, ?, ?)";
            }
            try {
                PreparedStatement InsertStatement = DH.prepareStatement(SQLString);
                InsertStatement.setString(1, LabelEWert.getText());
                InsertStatement.setString(2, LabelGWert.getText());
                InsertStatement.setString(3, LabelHWert.getValue().toString());
                if (LabelIWert.getValue() != null) {
                    InsertStatement.setString(4, LabelIWert.getValue().toString());
                }
                InsertStatement.executeUpdate();
                IM.setInfoText("Einfügen durchgeführt");
            } catch (Exception e) {
                IM.setErrorText("Einfügen Fehlgeschlagen", e);
            }
            refreshArbeitenAnAnsicht();
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    private void updateSelectedEntry() {
        ObservableList<ArbeitenAnDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        if (Nutzerauswahl.size() != 1) {
            IM.setErrorText("Es muss genau ein Element ausgewählt werden");
            return;
        }
        ArbeitenAnDaten Auswahl = Nutzerauswahl.get(0);

        // Jetzt erzeugen wir ein PopUp zum veraendern des Eintrags

        Stage PopUp = new Stage();
        PopUp.initModality(Modality.APPLICATION_MODAL);
        PopUp.setTitle("Eintrag ändern");
        PopUp.setAlwaysOnTop(true);
        PopUp.setResizable(false);

        GridPane Gitter = new GridPane();
        Gitter.setHgap(10);
        Gitter.setVgap(10);

        Label LabelD = new Label("Name");
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

        Label LabelF = new Label("Fall");
        Label LabelFWert = new Label();

        Label LabelG = new Label("FallID");
        TextField LabelGWert = new TextField();

        LabelGWert.textProperty().addListener(((observable, oldValue, newValue) -> {
            try {
                PreparedStatement NutzerInput = DH.prepareStatement("SELECT Name FROM FALL WHERE FallID= ?");
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

        Label LabelH = new Label("von");
        DatePicker LabelHWert = new DatePicker();

        Label LabelI = new Label("bis");
        DatePicker LabelIWert = new DatePicker();

        final Callback<DatePicker, DateCell> TagesZellenFabtrik = new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(final DatePicker DP) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item.isBefore(LabelHWert.getValue().plusDays(1))) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }
                    }
                };
            }
        };
        LabelIWert.setDayCellFactory(TagesZellenFabtrik);

        LabelEWert.setText(Integer.toString(Auswahl.getPersonenID()));
        LabelGWert.setText(Integer.toString(Auswahl.getFallID()));
        LabelHWert.setValue(LocalDate.parse(Auswahl.getVonDatum()));    //TODO exceptionSSS (2 mal hier)
        if (!Auswahl.getBisDatum().isEmpty()) {
            LabelIWert.setValue(LocalDate.parse(Auswahl.getBisDatum()));
        }

        Button ButtonFort = new Button("Fortfahren");
        Button ButtonAbb = new Button("Abbrechen");

        ButtonFort.defaultButtonProperty();
        ButtonAbb.cancelButtonProperty();

        ButtonFort.setMaxWidth(Double.MAX_VALUE);
        ButtonAbb.setMaxWidth(Double.MAX_VALUE);

        Gitter.addColumn(0, LabelD, LabelE, LabelF, LabelG, LabelH, LabelI);
        Gitter.addColumn(1, LabelDWert, LabelEWert, LabelFWert, LabelGWert, LabelHWert, LabelIWert);

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
            if (LabelIWert.getEditor().getText().isEmpty()) {
                SQLString = "UPDATE ARBEITEN_AN SET PersonenID=?, FallID=?, von=?, bis=NULL " +
                        "WHERE PersonenID= " + Auswahl.getPersonenID() +
                        " AND FallID= " + Auswahl.getFallID();
            } else {
                SQLString = "UPDATE ARBEITEN_AN SET PersonenID=?, FallID=?, von=?, bis=?  " +
                        " WHERE PersonenID= " + Auswahl.getPersonenID() +
                        " AND FallID= " + Auswahl.getFallID();
            }
            try {
                PreparedStatement SQLInjektionNeinNein = DH.prepareStatement(SQLString);
                SQLInjektionNeinNein.setInt(1, Integer.parseInt(LabelEWert.getText()));
                SQLInjektionNeinNein.setInt(2, Integer.parseInt(LabelGWert.getText()));
                SQLInjektionNeinNein.setString(3, LabelHWert.getValue().toString());
                if (LabelIWert.getValue() != null && !LabelIWert.getEditor().getText().isEmpty()) {
                    SQLInjektionNeinNein.setString(4, LabelIWert.getValue().toString());
                }
                SQLInjektionNeinNein.executeUpdate();
                IM.setInfoText("Änderung durchgeführt");
            } catch (Exception e) {
                IM.setErrorText("Ändern Fehlgeschlagen", e);
            }
            refreshArbeitenAnAnsicht();
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    private void deleteSelectedEntrys() {
        ObservableList<ArbeitenAnDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        if (Nutzerauswahl.isEmpty()) {
            IM.setErrorText("Es muss mindestens ein Eintrag ausgewählt sein");
            return;
        }

        Nutzerauswahl.forEach(ArbeitenAnDaten -> {
            try {
                DH.getAnfrageObjekt().executeUpdate("DELETE FROM ARBEITEN_AN WHERE PersonenID= " + ArbeitenAnDaten.getPersonenID() +
                        " AND FallID= " + ArbeitenAnDaten.getFallID());
            } catch (SQLException e) {
                IM.setErrorText("Löschen fehlgeschlagen", e);
            }
        });
        refreshArbeitenAnAnsicht();
    }

    public void SucheNachPersonenID(int PersonenID) {
        Hauptprogramm.setMittlereAnsicht(getArbeitenAnAnsicht());
        ArbeitenAnDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT ARBEITEN_AN.PersonenID, PERSON.Name, ARBEITEN_AN.FallID, FALL.Name, ARBEITEN_AN.von, ARBEITEN_AN.bis " +
                    "FROM ARBEITEN_AN, PERSON, FALL WHERE ARBEITEN_AN.FallID = FALL.FallID AND ARBEITEN_AN.PersonenID = PERSON.PersonenID AND ARBEITEN_AN.PersonenID = " + PersonenID);
            while (AnfrageAntwort.next()) {
                String BisDatum = "";
                if (AnfrageAntwort.getObject(6) != null) {
                    BisDatum = AnfrageAntwort.getString(6);
                }
                ArbeitenAnDatenListe.add(new ArbeitenAnDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
                        AnfrageAntwort.getInt(3), AnfrageAntwort.getString(4), AnfrageAntwort.getString(5), BisDatum));
            }
        } catch (SQLException e) {
            IM.setErrorText("Unbekannter Fehler bei aktualisieren der Ansicht", e);
        }
    }

    public void SucheNachFallID(int FallID) {
        Hauptprogramm.setMittlereAnsicht(getArbeitenAnAnsicht());
        ArbeitenAnDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT ARBEITEN_AN.PersonenID, PERSON.Name, ARBEITEN_AN.FallID, FALL.Name, ARBEITEN_AN.von, ARBEITEN_AN.bis " +
                    "FROM ARBEITEN_AN, PERSON, FALL WHERE ARBEITEN_AN.FallID = FALL.FallID AND ARBEITEN_AN.PersonenID = PERSON.PersonenID AND ARBEITEN_AN.FallID = " + FallID);
            while (AnfrageAntwort.next()) {
                String BisDatum = "";
                if (AnfrageAntwort.getObject(6) != null) {
                    BisDatum = AnfrageAntwort.getString(6);
                }
                ArbeitenAnDatenListe.add(new ArbeitenAnDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
                        AnfrageAntwort.getInt(3), AnfrageAntwort.getString(4), AnfrageAntwort.getString(5), BisDatum));
            }
        } catch (SQLException e) {
            IM.setErrorText("Unbekannter Fehler bei aktualisieren der Ansicht", e);
        }
    }
}
