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
import java.time.LocalDate;

/**
 * Liefert Tabelle fuer die Art
 */
public class VerbrechenAnsichtManager {

    private DatenbankHandler DH;
    private InfoErrorManager IM;
    private Main Hauptprogramm;
    private TableView<VerbrechenDaten> Tabelle;
    private BorderPane DatenAnsicht;
    private ObservableList<VerbrechenDaten> VerbrechenDatenListe;
    private boolean VerbrechenAnsichtGeneriert;

    public VerbrechenAnsichtManager(DatenbankHandler DBH, InfoErrorManager IEM, Main HauptFenster) {
        DH = DBH;
        IM = IEM;
        Hauptprogramm = HauptFenster;
        VerbrechenDatenListe = FXCollections.observableArrayList();
        Tabelle = new TableView<>();
        VerbrechenAnsichtGeneriert = false;
    }

    public Node getVerbrechenAnsicht() {
        if (VerbrechenAnsichtGeneriert) {
            refreshVerbrechenAnsicht();
            return DatenAnsicht;
        }
        IM.setInfoText("Lade Verbrechen Ansicht");
        DatenAnsicht = new BorderPane(getVerbrechenAnsichtInnereTabelle());

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

        IM.setInfoText("Laden der Verbrechen Ansicht erfolgreich");
        VerbrechenAnsichtGeneriert = true;
        return DatenAnsicht;
    }

    /**
     * Erzeugt eine TableView mit den Daten aus der Datenbank.
     *
     * @return Eine TableView mit Verbrechen Daten
     */
    private Node getVerbrechenAnsichtInnereTabelle() {
        refreshVerbrechenAnsicht();

        // Name Spalte
        TableColumn<VerbrechenDaten, String> SpalteName = new TableColumn<>("Name");
        SpalteName.setMinWidth(200);
        SpalteName.setCellValueFactory(new PropertyValueFactory<>("Name"));

        // Datum Spalte
        TableColumn<VerbrechenDaten, String> SpalteDatum = new TableColumn<>("Datum");
        SpalteDatum.setMinWidth(200);
        SpalteDatum.setCellValueFactory(new PropertyValueFactory<>("Datum"));

        // Bezirk Spalte
        TableColumn<VerbrechenDaten, String> SpalteBezirk = new TableColumn<>("Bezirk");
        SpalteBezirk.setMinWidth(200);
        SpalteBezirk.setCellValueFactory(new PropertyValueFactory<>("BezirkName"));

        // Fall Spalte
        TableColumn<VerbrechenDaten, String> SpalteFall = new TableColumn<>("Fall");
        SpalteFall.setMinWidth(200);
        SpalteFall.setCellValueFactory(new PropertyValueFactory<>("FallName"));

        // Art Spalte
        TableColumn<VerbrechenDaten, String> SpalteArt = new TableColumn<>("Art");
        SpalteArt.setMinWidth(200);
        SpalteArt.setCellValueFactory(new PropertyValueFactory<>("ArtName"));

        Tabelle.setItems(VerbrechenDatenListe);
        Tabelle.getColumns().add(SpalteName);
        Tabelle.getColumns().add(SpalteDatum);
        Tabelle.getColumns().add(SpalteBezirk);
        Tabelle.getColumns().add(SpalteFall);
        Tabelle.getColumns().add(SpalteArt);

        Tabelle.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Doppelklicke auf Spalten sollen Detailansichten oeffnen:
        Tabelle.setRowFactory(param -> {
            TableRow<VerbrechenDaten> Spalte = new TableRow<>();
            Spalte.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!Spalte.isEmpty())) {
                    erzeugeDetailAnsicht(Spalte.getItem());
                }
            });
            return Spalte;
        });

        return Tabelle;
    }

    private void erzeugeDetailAnsicht(VerbrechenDaten SpaltenDaten) {
        Label LabelA = new Label("VerbrechensID");
        Label LabelAWert = new Label(Integer.toString(SpaltenDaten.getVerbrechensID()));

        Label LabelB = new Label("Name");
        Label LabelBWert = new Label(SpaltenDaten.getName());

        Label LabelC = new Label("Datum");
        Label LabelCWert = new Label(SpaltenDaten.getDatum());

        Label LabelD = new Label("Bezirk");
        Label LabelDWert = new Label(SpaltenDaten.getBezirkName());

        Label LabelE = new Label("BezirksID");
        Label LabelEWert = new Label(Integer.toString(SpaltenDaten.getBezirksID()));

        Label LabelF = new Label("Fall");
        Label LabelFWert = new Label(SpaltenDaten.getFallName());

        Label LabelG = new Label("FallID");
        Label LabelGWert = new Label(Integer.toString(SpaltenDaten.getFallID()));

        Label LabelH = new Label("Art");
        Label LabelHWert = new Label(SpaltenDaten.getArtName());

        Label LabelI = new Label("ArtID");
        Label LabelIWert = new Label(Integer.toString(SpaltenDaten.getArtID()));


        Button ButtonBearbeiten = new Button("Bearbeiten...");
        Button ButtonLoeschen = new Button("Löschen");
        Button ButtonSucheVerbrechensId = new Button("Suche nach Vorkommen von VerbrechensID");
        Button ButtonSucheBezirksId = new Button("Suche nach Vorkommen von BezirksID");
        Button ButtonSucheFallId = new Button("Suche nach Vorkommen von FallID");
        Button ButtonSucheArtId = new Button("Suche nach Vorkommen von ArtID");
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
        ButtonSucheVerbrechensId.setMaxWidth(Double.MAX_VALUE);
        ButtonSucheBezirksId.setMaxWidth(Double.MAX_VALUE);
        ButtonSucheFallId.setMaxWidth(Double.MAX_VALUE);
        ButtonSucheArtId.setMaxWidth(Double.MAX_VALUE);
        ButtonClose.setMaxWidth(Double.MAX_VALUE);

        // Wir haben ein Gridpane oben, eine HBox unten in einer VBox in einem ScrollPane
        GridPane Oben = new GridPane();
        Oben.setHgap(10);
        Oben.setVgap(10);
        Oben.addColumn(0, LabelA, LabelB, LabelC, LabelD, LabelE, LabelF, LabelG, LabelH, LabelI);
        Oben.addColumn(1, LabelAWert, LabelBWert, LabelCWert, LabelDWert, LabelEWert, LabelFWert, LabelGWert, LabelHWert, LabelIWert);
        Oben.getColumnConstraints().add(new ColumnConstraints(100));
        Oben.getColumnConstraints().add(new ColumnConstraints(200));

        HBox Unten = new HBox(10);
        Unten.getChildren().addAll(ButtonBearbeiten, ButtonLoeschen);
        Unten.setMaxWidth(300);
        Unten.alignmentProperty().setValue(Pos.CENTER);

        VBox Mittelteil = new VBox(10);
        Mittelteil.setPadding(new Insets(10, 20, 10, 10));
        Mittelteil.getChildren().addAll(Oben, Unten, ButtonSucheVerbrechensId, ButtonSucheBezirksId, ButtonSucheFallId, ButtonSucheArtId, ButtonClose);

        ScrollPane Aussen = new ScrollPane();

        Aussen.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        Aussen.setContent(Mittelteil);

        Hauptprogramm.setRechteAnsicht(Aussen);
    }

    /**
     * Aktualisiert die JavaFX vorliegenden Daten mit Daten aus der Datenbank.
     */
    public void refreshVerbrechenAnsicht() {
        VerbrechenDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT VerbrechensID, VERBRECHEN.Name, VERBRECHEN.Datum, VERBRECHEN.geschieht_in_BezirksID, VERBRECHEN.gehört_zu_FallID, VERBRECHEN.gehört_zu_ArtID,\n" +
                    "  BEZIRK.Name AS BezirkName, FALL.Name AS FallName, ART.Name AS ArtName\n" +
                    "FROM VERBRECHEN, BEZIRK, FALL, ART\n" +
                    "WHERE VERBRECHEN.gehört_zu_ArtID = ArtID AND VERBRECHEN.gehört_zu_FallID = FALL.FallID AND VERBRECHEN.geschieht_in_BezirksID = BEZIRK.BezirksID");
            while (AnfrageAntwort.next()) {
                VerbrechenDatenListe.add(new VerbrechenDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
                        AnfrageAntwort.getString(3), AnfrageAntwort.getInt(4), AnfrageAntwort.getInt(5), AnfrageAntwort.getInt(6),
                        AnfrageAntwort.getString(7), AnfrageAntwort.getString(8), AnfrageAntwort.getString(9)));
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

        Label LabelC = new Label("Datum");
        DatePicker LabelCWert = new DatePicker();

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

        Label LabelH = new Label("Art");
        Label LabelHWert = new Label();

        Label LabelI = new Label("ArtID");
        TextField LabelIWert = new TextField();

        LabelIWert.textProperty().addListener(((observable, oldValue, newValue) -> {
            try {
                PreparedStatement NutzerInput = DH.prepareStatement("SELECT Name FROM ART WHERE ArtID = ?");
                NutzerInput.setInt(1, Integer.parseInt(newValue));
                ResultSet Antwort = NutzerInput.executeQuery();
                if (Antwort.next()) {
                    LabelHWert.setText(Antwort.getString(1));
                } else {
                    LabelHWert.setText("Ungültige ArtID");
                }
            } catch (SQLException e) {
                IM.setErrorText("Unbekannter SQL Fehler", e);
            } catch (NumberFormatException e) {
                LabelHWert.setText("ArtID muss eine Zahl sein");
            }
        }));

        Button ButtonFort = new Button("Fortfahren");
        Button ButtonAbb = new Button("Abbrechen");

        ButtonFort.defaultButtonProperty();
        ButtonAbb.cancelButtonProperty();

        ButtonFort.setMaxWidth(Double.MAX_VALUE);
        ButtonAbb.setMaxWidth(Double.MAX_VALUE);

        Gitter.addColumn(0, LabelB, LabelC, LabelD, LabelE, LabelF, LabelG, LabelH, LabelI);
        Gitter.addColumn(1, LabelBWert, LabelCWert, LabelDWert, LabelEWert, LabelFWert, LabelGWert, LabelHWert, LabelIWert);

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
            String SQLString = "INSERT INTO VERBRECHEN (Name, Datum, geschieht_in_BezirksID, gehört_zu_FallID, gehört_zu_ArtID ) VALUES (?, ?, ?, ?, ?)";
            try {
                PreparedStatement InsertStatement = DH.prepareStatement(SQLString);
                InsertStatement.setString(1, LabelBWert.getText());
                InsertStatement.setString(2, LabelCWert.getValue().toString()); //TODO hier vorther nullpointer pruefen oder abfangen
                InsertStatement.setString(3, LabelEWert.getText());
                InsertStatement.setString(4, LabelGWert.getText());
                InsertStatement.setString(5, LabelIWert.getText());
                InsertStatement.executeUpdate();
                IM.setInfoText("Einfügen durchgeführt");
            } catch (SQLException e) {
                IM.setErrorText("Einfügen Fehlgeschlagen", e);
            }
            refreshVerbrechenAnsicht();
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    private void updateSelectedEntry() {
        ObservableList<VerbrechenDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        if (Nutzerauswahl.size() != 1) {
            IM.setErrorText("Es muss genau ein Element ausgewählt werden");
            return;
        }
        VerbrechenDaten Auswahl = Nutzerauswahl.get(0);

        // Jetzt erzeugen wir ein PopUp zum veraendern des Eintrags

        Stage PopUp = new Stage();
        PopUp.initModality(Modality.APPLICATION_MODAL);
        PopUp.setTitle("Eintrag ändern");
        PopUp.setAlwaysOnTop(true);
        PopUp.setResizable(false);

        GridPane Gitter = new GridPane();
        Gitter.setHgap(10);
        Gitter.setVgap(10);

        Label LabelA = new Label("VerbrechensID");
        Label LabelAWert = new Label(Integer.toString(Auswahl.getVerbrechensID()));

        Label LabelB = new Label("Name");
        TextField LabelBWert = new TextField();

        Label LabelC = new Label("Datum");
        DatePicker LabelCWert = new DatePicker();

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

        Label LabelF = new Label("Fall");
        Label LabelFWert = new Label(Auswahl.getFallName());

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

        Label LabelH = new Label("Art");
        Label LabelHWert = new Label(Auswahl.getArtName());

        Label LabelI = new Label("ArtID");
        TextField LabelIWert = new TextField();

        LabelIWert.textProperty().addListener(((observable, oldValue, newValue) -> {
            try {
                PreparedStatement NutzerInput = DH.prepareStatement("SELECT Name FROM ART WHERE ArtID = ?");
                NutzerInput.setInt(1, Integer.parseInt(newValue));
                ResultSet Antwort = NutzerInput.executeQuery();
                if (Antwort.next()) {
                    LabelHWert.setText(Antwort.getString(1));
                } else {
                    LabelHWert.setText("Ungültige ArtID");
                }
            } catch (SQLException e) {
                IM.setErrorText("Unbekannter SQL Fehler", e);
            } catch (NumberFormatException e) {
                LabelHWert.setText("ArtID muss eine Zahl sein");
            }
        }));

        LabelBWert.setText(Auswahl.getArtName());
        LabelCWert.setValue(LocalDate.parse(Auswahl.getDatum()));       //TODO exception fangen
        LabelEWert.setText(Integer.toString(Auswahl.getBezirksID()));
        LabelGWert.setText(Integer.toString(Auswahl.getFallID()));
        LabelIWert.setText(Integer.toString(Auswahl.getArtID()));

        Button ButtonFort = new Button("Fortfahren");
        Button ButtonAbb = new Button("Abbrechen");

        ButtonFort.defaultButtonProperty();
        ButtonAbb.cancelButtonProperty();

        ButtonFort.setMaxWidth(Double.MAX_VALUE);
        ButtonAbb.setMaxWidth(Double.MAX_VALUE);

        Gitter.addColumn(0, LabelA, LabelB, LabelC, LabelD, LabelE, LabelF, LabelG, LabelH, LabelI);
        Gitter.addColumn(1, LabelAWert, LabelBWert, LabelCWert, LabelDWert, LabelEWert, LabelFWert, LabelGWert, LabelHWert, LabelIWert);

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
            String SQLString = "UPDATE VERBRECHEN SET Name=?, Datum=?, geschieht_in_BezirksID=?, gehört_zu_FallID=?, gehört_zu_ArtID=?  WHERE VerbrechensID = " + Auswahl.getVerbrechensID();
            try {
                PreparedStatement SQLInjektionNeinNein = DH.prepareStatement(SQLString);
                SQLInjektionNeinNein.setString(1, LabelBWert.getText());
                SQLInjektionNeinNein.setString(2, LabelCWert.getValue().toString());
                SQLInjektionNeinNein.setInt(3, Integer.parseInt(LabelEWert.getText()));
                SQLInjektionNeinNein.setInt(4, Integer.parseInt(LabelGWert.getText()));
                SQLInjektionNeinNein.setInt(5, Integer.parseInt(LabelIWert.getText()));
                SQLInjektionNeinNein.executeUpdate();
                IM.setInfoText("Änderung durchgeführt");
            } catch (SQLException e) {
                IM.setErrorText("Ändern Fehlgeschlagen", e);
            }
            refreshVerbrechenAnsicht();
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    private void deleteSelectedEntrys() {
        ObservableList<VerbrechenDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        if (Nutzerauswahl.isEmpty()) {
            IM.setErrorText("Es muss mindestens ein Eintrag ausgewählt sein");
            return;
        }

        Nutzerauswahl.forEach(VerbrechenDaten -> {
            try {
                DH.getAnfrageObjekt().executeUpdate("DELETE FROM Verbrechen WHERE VerbrechensID = " + VerbrechenDaten.getVerbrechensID());
            } catch (SQLException e) {
                IM.setErrorText("Löschen fehlgeschlagen", e);
            }
        });
        refreshVerbrechenAnsicht();
    }

    /**
     * Praesentiert nur Verbrechen die die spezifizierte ArtID haben
     *
     * @param ArtID Die ArtID der Darzustellenden Verbrechen
     */
    public void SucheNachArt(int ArtID) {
        Hauptprogramm.setMittlereAnsicht(getVerbrechenAnsicht());
        VerbrechenDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            PreparedStatement Anfrage = DH.prepareStatement("SELECT VerbrechensID, VERBRECHEN.Name, VERBRECHEN.Datum, VERBRECHEN.geschieht_in_BezirksID, VERBRECHEN.gehört_zu_FallID, VERBRECHEN.gehört_zu_ArtID,\n" +
                    "  BEZIRK.Name as BezirkName, FALL.Name as FallName, ART.Name as ArtName\n" +
                    "FROM VERBRECHEN, BEZIRK, FALL, ART\n" +
                    "WHERE VERBRECHEN.gehört_zu_ArtID = ArtID AND VERBRECHEN.gehört_zu_FallID = FALL.FallID AND VERBRECHEN.geschieht_in_BezirksID = BEZIRK.BezirksID AND VERBRECHEN.gehört_zu_ArtID = ?");
            Anfrage.setInt(1, ArtID);
            AnfrageAntwort = Anfrage.executeQuery();
            while (AnfrageAntwort.next()) {
                VerbrechenDatenListe.add(new VerbrechenDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
                        AnfrageAntwort.getString(3), AnfrageAntwort.getInt(4), AnfrageAntwort.getInt(5), AnfrageAntwort.getInt(6),
                        AnfrageAntwort.getString(7), AnfrageAntwort.getString(8), AnfrageAntwort.getString(9)));
            }
        } catch (SQLException e) {
            IM.setErrorText("Unbekannter Fehler beim Queransichtladen", e);
        }
    }

    public void SucheNachFall(int FallID) {
        Hauptprogramm.setMittlereAnsicht(getVerbrechenAnsicht());
        VerbrechenDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            PreparedStatement Anfrage = DH.prepareStatement("SELECT VerbrechensID, VERBRECHEN.Name, VERBRECHEN.Datum, VERBRECHEN.geschieht_in_BezirksID, VERBRECHEN.gehört_zu_FallID, VERBRECHEN.gehört_zu_ArtID,\n" +
                    "  BEZIRK.Name as BezirkName, FALL.Name as FallName, ART.Name as ArtName\n" +
                    "FROM VERBRECHEN, BEZIRK, FALL, ART\n" +
                    "WHERE VERBRECHEN.gehört_zu_ArtID = ArtID AND VERBRECHEN.gehört_zu_FallID = FALL.FallID AND VERBRECHEN.geschieht_in_BezirksID = BEZIRK.BezirksID AND VERBRECHEN.gehört_zu_FallID = ?");
            Anfrage.setInt(1, FallID);
            AnfrageAntwort = Anfrage.executeQuery();
            while (AnfrageAntwort.next()) {
                VerbrechenDatenListe.add(new VerbrechenDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
                        AnfrageAntwort.getString(3), AnfrageAntwort.getInt(4), AnfrageAntwort.getInt(5), AnfrageAntwort.getInt(6),
                        AnfrageAntwort.getString(7), AnfrageAntwort.getString(8), AnfrageAntwort.getString(9)));
            }
        } catch (SQLException e) {
            IM.setErrorText("Unbekannter Fehler beim Queransichtladen", e);
        }
    }

    public void SucheNachBezirk(int BezirksID) {
        Hauptprogramm.setMittlereAnsicht(getVerbrechenAnsicht());
        VerbrechenDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            PreparedStatement Anfrage = DH.prepareStatement("SELECT VerbrechensID, VERBRECHEN.Name, VERBRECHEN.Datum, VERBRECHEN.geschieht_in_BezirksID, VERBRECHEN.gehört_zu_FallID, VERBRECHEN.gehört_zu_ArtID,\n" +
                    "  BEZIRK.Name as BezirkName, FALL.Name as FallName, ART.Name as ArtName\n" +
                    "FROM VERBRECHEN, BEZIRK, FALL, ART\n" +
                    "WHERE VERBRECHEN.gehört_zu_ArtID = ArtID AND VERBRECHEN.gehört_zu_FallID = FALL.FallID AND VERBRECHEN.geschieht_in_BezirksID = BEZIRK.BezirksID AND VERBRECHEN.geschieht_in_BezirksID = ?");
            Anfrage.setInt(1, BezirksID);
            AnfrageAntwort = Anfrage.executeQuery();
            while (AnfrageAntwort.next()) {
                VerbrechenDatenListe.add(new VerbrechenDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
                        AnfrageAntwort.getString(3), AnfrageAntwort.getInt(4), AnfrageAntwort.getInt(5), AnfrageAntwort.getInt(6),
                        AnfrageAntwort.getString(7), AnfrageAntwort.getString(8), AnfrageAntwort.getString(9)));
            }
        } catch (SQLException e) {
            IM.setErrorText("Unbekannter Fehler beim Queransichtladen", e);
        }
    }
}
