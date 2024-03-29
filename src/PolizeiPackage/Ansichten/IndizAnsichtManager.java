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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Liefert Tabelle fuer die Art
 */
public class IndizAnsichtManager {

    private DatenbankHandler DH;
    private InfoErrorManager IM;
    private Main Hauptprogramm;
    private TableView<IndizDaten> Tabelle;
    private BorderPane DatenAnsicht;
    private ObservableList<IndizDaten> IndizDatenListe;
    private boolean IndizAnsichtGeneriert;
    private PolizistAnsichtManager PolizistAM;
    private FallAnsichtManager FallAM;

    // unschoene Sachen fuer das Bild laden
    private File Bild;
    private Image GeladenesBild;

    public IndizAnsichtManager(DatenbankHandler DBH, InfoErrorManager IEM, Main HauptFenster) {
        DH = DBH;
        IM = IEM;
        Hauptprogramm = HauptFenster;
        IndizDatenListe = FXCollections.observableArrayList();
        Tabelle = new TableView<>();
        IndizAnsichtGeneriert = false;
        Bild = null;
        GeladenesBild = null;
    }

    public void setPolizistAM(PolizistAnsichtManager polizistAM) {
        PolizistAM = polizistAM;
    }

    public void setFallAM(FallAnsichtManager fallAM) {
        FallAM = fallAM;
    }

    public Node getIndizAnsicht() {
        if (IndizAnsichtGeneriert) {
            refreshIndizAnsicht();
            return DatenAnsicht;
        }
        IM.setInfoText("Lade Indiz Ansicht");
        DatenAnsicht = new BorderPane(getIndizAnsichtInnereTabelle());

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

        IM.setInfoText("Laden der Indiz Ansicht erfolgreich");
        IndizAnsichtGeneriert = true;
        return DatenAnsicht;
    }

    /**
     * Erzeugt eine TableView mit den Daten aus der Datenbank.
     *
     * @return Eine TableView mit Indiz Daten
     */
    private Node getIndizAnsichtInnereTabelle() {
        refreshIndizAnsicht();

        // Datum Spalte
        TableColumn<IndizDaten, String> SpalteName = new TableColumn<>("Datum");
        SpalteName.setMinWidth(200);
        SpalteName.setCellValueFactory(new PropertyValueFactory<>("Datum"));

        // Text Spalte
        TableColumn<IndizDaten, String> SpalteDatum = new TableColumn<>("Text");
        SpalteDatum.setMinWidth(200);
        SpalteDatum.setCellValueFactory(new PropertyValueFactory<>("Text"));

        // Fall Spalte
        TableColumn<IndizDaten, String> SpalteBezirk = new TableColumn<>("angelegt zu Fall");
        SpalteBezirk.setMinWidth(200);
        SpalteBezirk.setCellValueFactory(new PropertyValueFactory<>("FallName"));

        // Personen Spalte
        TableColumn<IndizDaten, String> SpalteFall = new TableColumn<>("angelegt von");
        SpalteFall.setMinWidth(200);
        SpalteFall.setCellValueFactory(new PropertyValueFactory<>("PersonenName"));

        Tabelle.setItems(IndizDatenListe);
        Tabelle.getColumns().add(SpalteName);
        Tabelle.getColumns().add(SpalteDatum);
        Tabelle.getColumns().add(SpalteBezirk);
        Tabelle.getColumns().add(SpalteFall);

        Tabelle.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Doppelklicke auf Spalten sollen Detailansichten oeffnen:
        Tabelle.setRowFactory(param -> {
            TableRow<IndizDaten> Spalte = new TableRow<>();
            Spalte.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!Spalte.isEmpty())) {
                    erzeugeDetailAnsicht(Spalte.getItem());
                }
            });
            return Spalte;
        });

        return Tabelle;
    }

    private void erzeugeDetailAnsicht(IndizDaten SpaltenDaten) {
        Label LabelA = new Label("IndizID");
        Label LabelAWert = new Label(Integer.toString(SpaltenDaten.getIndizID()));

        Label LabelB = new Label("Datum");
        Label LabelBWert = new Label(SpaltenDaten.getDatum());

        Label LabelC = new Label("Text");
        Label LabelCWert = new Label(SpaltenDaten.getText());

        Label LabelD = new Label("PersonenID");
        Label LabelDWert = new Label(Integer.toString(SpaltenDaten.getPersonenID()));

        Label LabelE = new Label("Person");
        Label LabelEWert = new Label(SpaltenDaten.getPersonenName());

        Label LabelF = new Label("FallID");
        Label LabelFWert = new Label(Integer.toString(SpaltenDaten.getFallID()));

        Label LabelG = new Label("Fall");
        Label LabelGWert = new Label(SpaltenDaten.getFallName());

        Button ButtonBearbeiten = new Button("Bearbeiten...");
        Button ButtonLoeschen = new Button("Löschen");
        Button ButtonSucheFallId = new Button("Suche nach Fall");
        Button ButtonSucheArtId = new Button("Suche nach Polizist");
        Button ButtonClose = new Button("Detailansicht verlassen");

        ResultSet AntwortBild;
        GeladenesBild = null;
        try {
            AntwortBild = DH.getAnfrageObjekt().executeQuery("SELECT Bild FROM INDIZ WHERE IndizID =" + SpaltenDaten.getIndizID());
            if (AntwortBild.next()) {
                byte[] TempBild = AntwortBild.getBytes(1);
                GeladenesBild = new Image(new ByteArrayInputStream(TempBild));
            }
        } catch (Exception e) {
            IM.setErrorText("Konnte Bild nicht für Detailansicht laden", e);
            return;
        }
        if (GeladenesBild == null) {
            IM.setErrorText("Unbekannter Fehler beim laden des Bildes für Detailansicht");
            return;
        }

        ImageView BildFenster = new ImageView();
        BildFenster.setImage(GeladenesBild);
        BildFenster.setFitWidth(300);
        BildFenster.setPreserveRatio(true);
        BildFenster.setSmooth(true);
        BildFenster.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                BildMaximiertAnzeigen();
            }
        });

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
        ButtonSucheFallId.setOnAction(event -> {
            Hauptprogramm.setRechteAnsicht(null);
            FallAM.FallSuchAnsicht(SpaltenDaten.getFallID());
        });
        ButtonSucheArtId.setOnAction(event -> {
            Hauptprogramm.setRechteAnsicht(null);
            PolizistAM.SucheNachPolizist(SpaltenDaten.getPersonenID());
        });

        ButtonClose.setOnAction(event -> Hauptprogramm.setRechteAnsicht(null));

        ButtonBearbeiten.setMaxWidth(Double.MAX_VALUE);
        ButtonBearbeiten.setMinWidth(150);
        ButtonLoeschen.setMaxWidth(Double.MAX_VALUE);
        ButtonLoeschen.setMinWidth(150);
        ButtonSucheFallId.setMaxWidth(Double.MAX_VALUE);
        ButtonSucheArtId.setMaxWidth(Double.MAX_VALUE);
        ButtonClose.setMaxWidth(Double.MAX_VALUE);

        // Wir haben ein Gridpane oben, eine HBox unten in einer VBox in einem ScrollPane
        GridPane Oben = new GridPane();
        Oben.setHgap(10);
        Oben.setVgap(10);
        Oben.addColumn(0, LabelA, LabelB, LabelC, LabelD, LabelE, LabelF, LabelG);
        Oben.addColumn(1, LabelAWert, LabelBWert, LabelCWert, LabelDWert, LabelEWert, LabelFWert, LabelGWert);
        Oben.getColumnConstraints().add(new ColumnConstraints(100));
        Oben.getColumnConstraints().add(new ColumnConstraints(200));

        HBox Unten = new HBox(10);
        Unten.getChildren().addAll(ButtonBearbeiten, ButtonLoeschen);
        Unten.setMaxWidth(300);
        Unten.alignmentProperty().setValue(Pos.CENTER);

        VBox Mittelteil = new VBox(10);
        Mittelteil.setPadding(new Insets(10, 20, 10, 10));
        Mittelteil.getChildren().addAll(Oben, BildFenster, Unten, ButtonSucheFallId, ButtonSucheArtId, ButtonClose);

        ScrollPane Aussen = new ScrollPane();

        Aussen.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        Aussen.setContent(Mittelteil);

        Hauptprogramm.setRechteAnsicht(Aussen);
    }

    /**
     * Aktualisiert die JavaFX vorliegenden Daten mit Daten aus der Datenbank.
     */
    public void refreshIndizAnsicht() {
        IndizDatenListe.clear();
        ResultSet AnfrageAntwort;
        String Text;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT IndizID, Indiz.Datum, Indiz.Text, angelegt_von_PersonenID, angelegt_zu_FallID, PERSON.Name, FALL.Name " +
                    "FROM Indiz, PERSON, FALL WHERE angelegt_zu_FallID = FALL.FallID AND angelegt_von_PersonenID = PersonenID;");
            while (AnfrageAntwort.next()) {
                Text = "";
                if (AnfrageAntwort.getObject(3) != null) {
                    Text = AnfrageAntwort.getString(3);
                }
                IndizDatenListe.add(new IndizDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
                        Text, AnfrageAntwort.getInt(4), AnfrageAntwort.getInt(5), AnfrageAntwort.getString(6),
                        AnfrageAntwort.getString(7)));
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

        Label LabelB = new Label("Datum");
        DatePicker LabelBWert = new DatePicker();

        Label LabelImage = new Label("Bild");
        Button ButtonImage = new Button("Bild auswählen...");

        ButtonImage.setMaxWidth(Double.MAX_VALUE);
        ButtonImage.setOnAction(event -> HandleBildLaden(PopUp));

        Label LabelC = new Label("Text");
        TextField LabelCWert = new TextField();

        Label LabelD = new Label("Person");
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
                LabelDWert.setText("PersonenID muss eine Zahl sein");
            }
        }));

        Label LabelF = new Label("Fall");
        Label LabelFWert = new Label();

        Label LabelG = new Label("FallID");
        TextField LabelGWert = new TextField();

        LabelGWert.textProperty().addListener(((observable, oldValue, newValue) -> {
            try {
                PreparedStatement NutzerInput = DH.prepareStatement("SELECT Name FROM FALL WHERE FallID = ?");
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

        Gitter.addColumn(0, LabelB, LabelImage, LabelC, LabelD, LabelE, LabelF, LabelG);
        Gitter.addColumn(1, LabelBWert, ButtonImage, LabelCWert, LabelDWert, LabelEWert, LabelFWert, LabelGWert);

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
            String SQLString = "INSERT INTO Indiz (Datum, Text, angelegt_von_PersonenID, angelegt_zu_FallID, Bild) VALUES (?, ?, ?, ?, ?)";
            try {
                PreparedStatement InsertStatement = DH.prepareStatement(SQLString);
                InsertStatement.setString(1, LabelBWert.getValue().toString());
                InsertStatement.setString(2, LabelCWert.getText());
                InsertStatement.setString(3, LabelEWert.getText());
                InsertStatement.setString(4, LabelGWert.getText());
                InsertStatement.setBytes(5, Files.readAllBytes(Bild.toPath()));
                InsertStatement.executeUpdate();
                IM.setInfoText("Einfügen durchgeführt");
            } catch (Exception e) {
                IM.setErrorText("Einfügen Fehlgeschlagen", e);
            } finally {
                Bild = null;
            }
            refreshIndizAnsicht();
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    private void updateSelectedEntry() {
        ObservableList<IndizDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        if (Nutzerauswahl.size() != 1) {
            IM.setErrorText("Es muss genau ein Element ausgewählt werden");
            return;
        }
        IndizDaten Auswahl = Nutzerauswahl.get(0);

        // Jetzt erzeugen wir ein PopUp zum veraendern des Eintrags

        Stage PopUp = new Stage();
        PopUp.initModality(Modality.APPLICATION_MODAL);
        PopUp.setTitle("Eintrag ändern");
        PopUp.setAlwaysOnTop(true);
        PopUp.setResizable(false);

        GridPane Gitter = new GridPane();
        Gitter.setHgap(10);
        Gitter.setVgap(10);

        Label LabelA = new Label("IndizsID");
        Label LabelAWert = new Label(Integer.toString(Auswahl.getIndizID()));

        Label LabelB = new Label("Datum");
        DatePicker LabelBWert = new DatePicker();

        Label LabelImage = new Label("Bild");
        Button ButtonImage = new Button("Bild auswählen...");

        ButtonImage.setMaxWidth(Double.MAX_VALUE);
        Bild = null;
        ButtonImage.setOnAction(event -> HandleBildLaden(PopUp));

        Label LabelC = new Label("Text");
        TextField LabelCWert = new TextField();

        Label LabelD = new Label("Person");
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
                LabelDWert.setText("PersonenID muss eine Zahl sein");
            }
        }));

        Label LabelF = new Label("Fall");
        Label LabelFWert = new Label();

        Label LabelG = new Label("FallID");
        TextField LabelGWert = new TextField();

        LabelGWert.textProperty().addListener(((observable, oldValue, newValue) -> {
            try {
                PreparedStatement NutzerInput = DH.prepareStatement("SELECT Name FROM FALL WHERE FallID = ?");
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

        LabelBWert.setValue(LocalDate.parse(Auswahl.getDatum()));   //TODO exception abfangen
        LabelCWert.setText(Auswahl.getText());
        LabelEWert.setText(Integer.toString(Auswahl.getPersonenID()));
        LabelGWert.setText(Integer.toString(Auswahl.getFallID()));

        Button ButtonFort = new Button("Fortfahren");
        Button ButtonAbb = new Button("Abbrechen");

        ButtonFort.defaultButtonProperty();
        ButtonAbb.cancelButtonProperty();

        ButtonFort.setMaxWidth(Double.MAX_VALUE);
        ButtonAbb.setMaxWidth(Double.MAX_VALUE);

        Gitter.addColumn(0, LabelA, LabelB, LabelImage, LabelC, LabelD, LabelE, LabelF, LabelG);
        Gitter.addColumn(1, LabelAWert, LabelBWert, ButtonImage, LabelCWert, LabelDWert, LabelEWert, LabelFWert, LabelGWert);

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
            if (Bild == null) {
                SQLString = "UPDATE Indiz SET Datum=?, Text=?, angelegt_von_PersonenID=?, angelegt_zu_FallID=?  WHERE IndizID = " + Auswahl.getIndizID();
            } else {
                SQLString = "UPDATE Indiz SET Datum=?, Text=?, angelegt_von_PersonenID=?, angelegt_zu_FallID=?, Bild=?  WHERE IndizID = " + Auswahl.getIndizID();
            }
            try {
                PreparedStatement SQLInjektionNeinNein = DH.prepareStatement(SQLString);
                SQLInjektionNeinNein.setString(1, LabelBWert.getValue().toString());    //TODO exception abfangen
                SQLInjektionNeinNein.setString(2, LabelCWert.getText());
                SQLInjektionNeinNein.setInt(3, Integer.parseInt(LabelEWert.getText()));
                SQLInjektionNeinNein.setInt(4, Integer.parseInt(LabelGWert.getText()));
                if (Bild != null) {
                    SQLInjektionNeinNein.setBytes(5, Files.readAllBytes(Bild.toPath()));
                }
                SQLInjektionNeinNein.executeUpdate();
                IM.setInfoText("Änderung durchgeführt");
            } catch (Exception e) {
                IM.setErrorText("Ändern Fehlgeschlagen", e);
            } finally {
                Bild = null;
            }
            refreshIndizAnsicht();
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    private void deleteSelectedEntrys() {
        ObservableList<IndizDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        if (Nutzerauswahl.isEmpty()) {
            IM.setErrorText("Es muss mindestens ein Eintrag ausgewählt sein");
            return;
        }

        Nutzerauswahl.forEach(IndizDaten -> {
            try {
                DH.getAnfrageObjekt().executeUpdate("DELETE FROM Indiz WHERE IndizID = " + IndizDaten.getIndizID());
            } catch (SQLException e) {
                IM.setErrorText("Löschen fehlgeschlagen", e);
            }
        });
        refreshIndizAnsicht();
    }

    /**
     * Zeigt einen FileCHooser an fuer das laden von Bildern. Setzt globale Variable File Bild null, falls keine Auswahl.
     *
     * @param Aufrufer Aufruferstage die geblockt wird
     */
    private void HandleBildLaden(Stage Aufrufer) {
        FileChooser BildAuswaehler = new FileChooser();
        Bild = BildAuswaehler.showOpenDialog(Aufrufer);
    }

    private void BildMaximiertAnzeigen() {
        if (GeladenesBild == null) return;
        Stage PopUp = new Stage();
        PopUp.initModality(Modality.APPLICATION_MODAL);
        PopUp.setTitle("Indiz");

        ImageView Innen = new ImageView(GeladenesBild);

        ScrollPane Aussen = new ScrollPane();
        Aussen.setContent(Innen);

        Aussen.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                PopUp.close();
            }
        });

        PopUp.setScene(new Scene(Aussen));
        PopUp.showAndWait();
    }

    public void SucheNachAnlegendem(int PersonenID) {
        Hauptprogramm.setMittlereAnsicht(getIndizAnsicht());
        IndizDatenListe.clear();
        ResultSet AnfrageAntwort;
        String Text;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT IndizID, Indiz.Datum, Indiz.Text, angelegt_von_PersonenID, angelegt_zu_FallID, PERSON.Name, FALL.Name " +
                    "FROM Indiz, PERSON, FALL WHERE angelegt_zu_FallID = FALL.FallID AND angelegt_von_PersonenID = PersonenID AND PersonenID = " + PersonenID);
            while (AnfrageAntwort.next()) {
                Text = "";
                if (AnfrageAntwort.getObject(3) != null) {
                    Text = AnfrageAntwort.getString(3);
                }
                IndizDatenListe.add(new IndizDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
                        Text, AnfrageAntwort.getInt(4), AnfrageAntwort.getInt(5), AnfrageAntwort.getString(6),
                        AnfrageAntwort.getString(7)));
            }
        } catch (SQLException e) {
            IM.setErrorText("Unbekannter Fehler bei aktualisieren der Ansicht", e);
        }
    }

    public void SucheNachFall(int FallID) {
        Hauptprogramm.setMittlereAnsicht(getIndizAnsicht());
        IndizDatenListe.clear();
        ResultSet AnfrageAntwort;
        String Text;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT IndizID, Indiz.Datum, Indiz.Text, angelegt_von_PersonenID, angelegt_zu_FallID, PERSON.Name, FALL.Name " +
                    "FROM Indiz, PERSON, FALL WHERE angelegt_zu_FallID = FALL.FallID AND angelegt_von_PersonenID = PersonenID AND angelegt_zu_FallID = " + FallID);
            while (AnfrageAntwort.next()) {
                Text = "";
                if (AnfrageAntwort.getObject(3) != null) {
                    Text = AnfrageAntwort.getString(3);
                }
                IndizDatenListe.add(new IndizDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
                        Text, AnfrageAntwort.getInt(4), AnfrageAntwort.getInt(5), AnfrageAntwort.getString(6),
                        AnfrageAntwort.getString(7)));
            }
        } catch (SQLException e) {
            IM.setErrorText("Unbekannter Fehler bei aktualisieren der Ansicht", e);
        }
    }
}
