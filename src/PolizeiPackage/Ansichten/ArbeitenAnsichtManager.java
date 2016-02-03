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
public class ArbeitenAnsichtManager {

    private DatenbankHandler DH;
    private InfoErrorManager IM;
    private Main Hauptprogramm;
    private TableView<ArbeitenDaten> Tabelle;
    private BorderPane DatenAnsicht;
    private ObservableList<ArbeitenDaten> ArbeitenDatenListe;
    private boolean ArbeitenAnsichtGeneriert;

    private PolizistAnsichtManager PolizistAM;
    private BehoerdenAnsichtManager BehoerdenAM;

    public ArbeitenAnsichtManager(DatenbankHandler DBH, InfoErrorManager IEM, Main HauptFenster) {
        DH = DBH;
        IM = IEM;
        Hauptprogramm = HauptFenster;
        ArbeitenDatenListe = FXCollections.observableArrayList();
        Tabelle = new TableView<>();
        ArbeitenAnsichtGeneriert = false;
    }

    public void setPolizistAM(PolizistAnsichtManager polizistAM) {
        PolizistAM = polizistAM;
    }

    public void setBehoerdenAM(BehoerdenAnsichtManager behoerdenAM) {
        BehoerdenAM = behoerdenAM;
    }

    public Node getArbeitenAnsicht() {
        if (ArbeitenAnsichtGeneriert) {
            refreshArbeitenAnsicht();
            return DatenAnsicht;
        }
        IM.setInfoText("Lade Arbeiten Ansicht");
        DatenAnsicht = new BorderPane(getArbeitenAnsichtInnereTabelle());

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

        IM.setInfoText("Laden der Arbeiten Ansicht erfolgreich");
        ArbeitenAnsichtGeneriert = true;
        return DatenAnsicht;
    }

    /**
     * Erzeugt eine TableView mit den Daten aus der Datenbank.
     *
     * @return Eine TableView mit Arbeiten Daten
     */
    private Node getArbeitenAnsichtInnereTabelle() {
        refreshArbeitenAnsicht();

        TableColumn<ArbeitenDaten, String> SpalteName = new TableColumn<>("Name");
        SpalteName.setMinWidth(200);
        SpalteName.setCellValueFactory(new PropertyValueFactory<>("PersonenName"));

        TableColumn<ArbeitenDaten, String> SpalteDatum = new TableColumn<>("Behörde");
        SpalteDatum.setMinWidth(200);
        SpalteDatum.setCellValueFactory(new PropertyValueFactory<>("BehordenName"));

        TableColumn<ArbeitenDaten, String> SpalteBezirk = new TableColumn<>("von");
        SpalteBezirk.setMinWidth(200);
        SpalteBezirk.setCellValueFactory(new PropertyValueFactory<>("VonDatum"));

        TableColumn<ArbeitenDaten, String> SpalteFall = new TableColumn<>("bis");
        SpalteFall.setMinWidth(200);
        SpalteFall.setCellValueFactory(new PropertyValueFactory<>("BisDatum"));

        Tabelle.setItems(ArbeitenDatenListe);
        Tabelle.getColumns().add(SpalteName);
        Tabelle.getColumns().add(SpalteDatum);
        Tabelle.getColumns().add(SpalteBezirk);
        Tabelle.getColumns().add(SpalteFall);

        Tabelle.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Doppelklicke auf Spalten sollen Detailansichten oeffnen:
        Tabelle.setRowFactory(param -> {
            TableRow<ArbeitenDaten> Spalte = new TableRow<>();
            Spalte.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!Spalte.isEmpty())) {
                    erzeugeDetailAnsicht(Spalte.getItem());
                }
            });
            return Spalte;
        });

        return Tabelle;
    }

    private void erzeugeDetailAnsicht(ArbeitenDaten SpaltenDaten) {
        Label LabelA = new Label("PersonenID");
        Label LabelAWert = new Label(Integer.toString(SpaltenDaten.getPersonenID()));

        Label LabelB = new Label("Name");
        Label LabelBWert = new Label(SpaltenDaten.getPersonenName());

        Label LabelC = new Label("BehördenID");
        Label LabelCWert = new Label(Integer.toString(SpaltenDaten.getBehordenID()));

        Label LabelD = new Label("Behörde");
        Label LabelDWert = new Label(SpaltenDaten.getBehordenName());

        Label LabelE = new Label("von");
        Label LabelEWert = new Label(SpaltenDaten.getVonDatum());

        Label LabelF = new Label("bis");
        Label LabelFWert = new Label(SpaltenDaten.getBisDatum());

        Button ButtonBearbeiten = new Button("Bearbeiten...");
        Button ButtonLoeschen = new Button("Löschen");
        Button SuchePolizist = new Button("Suche nach Polizist");
        Button SucheBehoerde = new Button("Suche nach Behörde");
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
        SuchePolizist.setOnAction(event -> {
            Hauptprogramm.setRechteAnsicht(null);
            PolizistAM.SucheNachPolizist(SpaltenDaten.getPersonenID());
        });
        SucheBehoerde.setOnAction(event -> {
            Hauptprogramm.setRechteAnsicht(null);
            BehoerdenAM.SucheNachBehoercde(SpaltenDaten.getBehordenID());
        });
        ButtonClose.setOnAction(event -> Hauptprogramm.setRechteAnsicht(null));

        ButtonBearbeiten.setMaxWidth(Double.MAX_VALUE);
        ButtonBearbeiten.setMinWidth(150);
        ButtonLoeschen.setMaxWidth(Double.MAX_VALUE);
        ButtonLoeschen.setMinWidth(150);
        SuchePolizist.setMaxWidth(Double.MAX_VALUE);
        SucheBehoerde.setMaxWidth(Double.MAX_VALUE);
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
        Mittelteil.getChildren().addAll(Oben, Unten, SuchePolizist, SucheBehoerde, ButtonClose);

        ScrollPane Aussen = new ScrollPane();

        Aussen.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        Aussen.setContent(Mittelteil);

        Hauptprogramm.setRechteAnsicht(Aussen);
    }

    /**
     * Aktualisiert die JavaFX vorliegenden Daten mit Daten aus der Datenbank.
     */
    public void refreshArbeitenAnsicht() {
        ArbeitenDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT ARBEITEN.PersonenID, PERSON.Name, ARBEITEN.BehördenID, BEHÖRDE.Name, von, bis " +
                    "FROM ARBEITEN, PERSON, BEHÖRDE WHERE ARBEITEN.PersonenID = PERSON.PersonenID AND ARBEITEN.BehördenID = BEHÖRDE.BehördenID;");
            while (AnfrageAntwort.next()) {
                ArbeitenDatenListe.add(new ArbeitenDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
                        AnfrageAntwort.getInt(3), AnfrageAntwort.getString(4), AnfrageAntwort.getString(5), AnfrageAntwort.getString(6)));
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

        Label LabelF = new Label("Behörde");
        Label LabelFWert = new Label();

        Label LabelG = new Label("BehördenID");
        TextField LabelGWert = new TextField();

        LabelGWert.textProperty().addListener(((observable, oldValue, newValue) -> {
            try {
                PreparedStatement NutzerInput = DH.prepareStatement("SELECT Name FROM BEHÖRDE WHERE BehördenID = ?");
                NutzerInput.setInt(1, Integer.parseInt(newValue));
                ResultSet Antwort = NutzerInput.executeQuery();
                if (Antwort.next()) {
                    LabelFWert.setText(Antwort.getString(1));
                } else {
                    LabelFWert.setText("Ungültige BehördenID");
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
            String SQLString = "INSERT INTO ARBEITEN (PersonenID, BehördenID, von, bis) VALUES (?, ?, ?, ?)";
            try {
                PreparedStatement InsertStatement = DH.prepareStatement(SQLString);
                InsertStatement.setString(1, LabelEWert.getText());
                InsertStatement.setString(2, LabelGWert.getText());
                InsertStatement.setString(3, LabelHWert.getValue().toString());
                InsertStatement.setString(4, LabelIWert.getValue().toString());
                InsertStatement.executeUpdate();
                IM.setInfoText("Einfügen durchgeführt");
            } catch (Exception e) {
                IM.setErrorText("Einfügen Fehlgeschlagen", e);
            }
            refreshArbeitenAnsicht();
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    private void updateSelectedEntry() {
        ObservableList<ArbeitenDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        if (Nutzerauswahl.size() != 1) {
            IM.setErrorText("Es muss genau ein Element ausgewählt werden");
            return;
        }
        ArbeitenDaten Auswahl = Nutzerauswahl.get(0);

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

        Label LabelF = new Label("Behörde");
        Label LabelFWert = new Label();

        Label LabelG = new Label("BehördenID");
        TextField LabelGWert = new TextField();

        LabelGWert.textProperty().addListener(((observable, oldValue, newValue) -> {
            try {
                PreparedStatement NutzerInput = DH.prepareStatement("SELECT Name FROM BEHÖRDE WHERE BehördenID = ?");
                NutzerInput.setInt(1, Integer.parseInt(newValue));
                ResultSet Antwort = NutzerInput.executeQuery();
                if (Antwort.next()) {
                    LabelFWert.setText(Antwort.getString(1));
                } else {
                    LabelFWert.setText("Ungültige BehördenID");
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
        LabelGWert.setText(Integer.toString(Auswahl.getBehordenID()));
        LabelHWert.setValue(LocalDate.parse(Auswahl.getVonDatum()));    //TODO exceptionSSS (2 mal hier)
        LabelIWert.setValue(LocalDate.parse(Auswahl.getBisDatum()));

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
            String SQLString = "UPDATE ARBEITEN SET PersonenID=?, BehördenID=?, von=?, bis=?  " +
                    "WHERE PersonenID= " + Auswahl.getPersonenID() +
                    " AND BehördenID= " + Auswahl.getBehordenID() +
                    " AND von= " + Auswahl.getVonDatum() +
                    " AND bis =" + Auswahl.getBisDatum();
            try {
                PreparedStatement SQLInjektionNeinNein = DH.prepareStatement(SQLString);
                SQLInjektionNeinNein.setInt(1, Integer.parseInt(LabelEWert.getText()));
                SQLInjektionNeinNein.setInt(2, Integer.parseInt(LabelGWert.getText()));
                SQLInjektionNeinNein.setString(3, LabelHWert.getValue().toString());
                SQLInjektionNeinNein.setString(4, LabelIWert.getValue().toString());
                SQLInjektionNeinNein.executeUpdate();
                IM.setInfoText("Änderung durchgeführt");
            } catch (Exception e) {
                IM.setErrorText("Ändern Fehlgeschlagen", e);
            }
            refreshArbeitenAnsicht();
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    private void deleteSelectedEntrys() {
        ObservableList<ArbeitenDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        if (Nutzerauswahl.isEmpty()) {
            IM.setErrorText("Es muss mindestens ein Eintrag ausgewählt sein");
            return;
        }

        Nutzerauswahl.forEach(ArbeitenDaten -> {
            try {
                DH.getAnfrageObjekt().executeUpdate("DELETE FROM Arbeiten WHERE PersonenID= " + ArbeitenDaten.getPersonenID() +
                        " AND BehördenID= " + ArbeitenDaten.getBehordenID() +
                        " AND von= " + ArbeitenDaten.getVonDatum() +
                        " AND bis =" + ArbeitenDaten.getBisDatum());
            } catch (SQLException e) {
                IM.setErrorText("Löschen fehlgeschlagen", e);
            }
        });
        refreshArbeitenAnsicht();
    }

    public void SucheNachBehoerdenID(int BehID) {
        Hauptprogramm.setMittlereAnsicht(getArbeitenAnsicht());
        ArbeitenDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT ARBEITEN.PersonenID, PERSON.Name, ARBEITEN.BehördenID, BEHÖRDE.Name, von, bis " +
                    "FROM ARBEITEN, PERSON, BEHÖRDE WHERE ARBEITEN.PersonenID = PERSON.PersonenID AND ARBEITEN.BehördenID = BEHÖRDE.BehördenID AND ARBEITEN.BehördenID = " + BehID);
            while (AnfrageAntwort.next()) {
                ArbeitenDatenListe.add(new ArbeitenDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
                        AnfrageAntwort.getInt(3), AnfrageAntwort.getString(4), AnfrageAntwort.getString(5), AnfrageAntwort.getString(6)));
            }
        } catch (SQLException e) {
            IM.setErrorText("Unbekannter Fehler bei aktualisieren der Ansicht", e);
        }
    }
}
