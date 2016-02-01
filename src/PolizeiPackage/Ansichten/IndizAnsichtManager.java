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
public class IndizAnsichtManager {

    private DatenbankHandler DH;
    private InfoErrorManager IM;
    private Main Hauptprogramm;
    private TableView<IndizDaten> Tabelle;
    private BorderPane DatenAnsicht;
    private ObservableList<IndizDaten> IndizDatenListe;
    private boolean IndizAnsichtGeneriert;

    public IndizAnsichtManager(DatenbankHandler DBH, InfoErrorManager IEM, Main HauptFenster) {
        DH = DBH;
        IM = IEM;
        Hauptprogramm = HauptFenster;
        IndizDatenListe = FXCollections.observableArrayList();
        Tabelle = new TableView<>();
        IndizAnsichtGeneriert = false;
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
                if (event.getClickCount() == 2 && (! Spalte.isEmpty())) {
                    erzeugeDetailAnsicht(Spalte.getItem());
                }
            });
            return Spalte;
        });

        return Tabelle;
    }

    private void erzeugeDetailAnsicht(IndizDaten SpaltenDaten) {
        Label LabelA = new Label("IndizsID");
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
        Button ButtonSucheIndizsId = new Button("Suche nach Vorkommen von IndizsID");
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
        ButtonSucheIndizsId.setMaxWidth(Double.MAX_VALUE);
        ButtonSucheBezirksId.setMaxWidth(Double.MAX_VALUE);
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
        Mittelteil.setPadding(new Insets(10,20,10,10));
        Mittelteil.getChildren().addAll(Oben, Unten, ButtonSucheIndizsId, ButtonSucheBezirksId, ButtonSucheFallId, ButtonSucheArtId, ButtonClose);

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
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT IndizID, Indiz.Datum, INDIZ.Bild, Indiz.Text, angelegt_von_PersonenID, angelegt_zu_FallID, PERSON.Name, FALL.Name " +
                    "FROM Indiz, PERSON, FALL WHERE angelegt_zu_FallID = FALL.FallID AND angelegt_von_PersonenID = PersonenID;");
            while (AnfrageAntwort.next()) {
                Text = "";
                if (AnfrageAntwort.getObject(4) != null) {
                    Text = AnfrageAntwort.getString(4);
                }
                IndizDatenListe.add(new IndizDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2), AnfrageAntwort.getBytes(3),
                        Text, AnfrageAntwort.getInt(5), AnfrageAntwort.getInt(6), AnfrageAntwort.getString(7),
                        AnfrageAntwort.getString(8)));
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
        TextField LabelBWert = new TextField();

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

        Gitter.addColumn(0, LabelB, LabelC, LabelD, LabelE, LabelF, LabelG);
        Gitter.addColumn(1, LabelBWert, LabelCWert, LabelDWert, LabelEWert, LabelFWert, LabelGWert);

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
            String SQLString = "INSERT INTO Indiz (Datum, Text, angelegt_von_PersonenID, angelegt_zu_FallID) VALUES (?, ?, ?, ?)";
            try {
                PreparedStatement InsertStatement = DH.prepareStatement(SQLString);
                InsertStatement.setString(1, LabelBWert.getText());
                InsertStatement.setString(2, LabelCWert.getText());
                InsertStatement.setString(3, LabelEWert.getText());
                InsertStatement.setString(4, LabelGWert.getText());
                InsertStatement.executeUpdate();
                IM.setInfoText("Einfügen durchgeführt");
            } catch (SQLException e) {
                IM.setErrorText("Einfügen Fehlgeschlagen", e);
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
        TextField LabelBWert = new TextField();

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

        LabelBWert.setText(Auswahl.getDatum());
        LabelCWert.setText(Auswahl.getText());
        LabelEWert.setText(Integer.toString(Auswahl.getPersonenID()));
        LabelGWert.setText(Integer.toString(Auswahl.getFallID()));

        Button ButtonFort = new Button("Fortfahren");
        Button ButtonAbb = new Button("Abbrechen");

        ButtonFort.defaultButtonProperty();
        ButtonAbb.cancelButtonProperty();

        ButtonFort.setMaxWidth(Double.MAX_VALUE);
        ButtonAbb.setMaxWidth(Double.MAX_VALUE);

        Gitter.addColumn(0, LabelA, LabelB, LabelC, LabelD, LabelE, LabelF, LabelG);
        Gitter.addColumn(1, LabelAWert, LabelBWert, LabelCWert, LabelDWert, LabelEWert, LabelFWert, LabelGWert);

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
            String SQLString = "UPDATE Indiz SET Datum=?, Text=?, angelegt_von_PersonenID=?, angelegt_zu_FallID=?  WHERE IndizID = " + Auswahl.getIndizID();
            try {
                PreparedStatement SQLInjektionNeinNein = DH.prepareStatement(SQLString);
                SQLInjektionNeinNein.setString(1, LabelBWert.getText());
                SQLInjektionNeinNein.setString(2, LabelCWert.getText());
                SQLInjektionNeinNein.setInt(3, Integer.parseInt(LabelEWert.getText()));
                SQLInjektionNeinNein.setInt(4, Integer.parseInt(LabelGWert.getText()));
                SQLInjektionNeinNein.executeUpdate();
                IM.setInfoText("Änderung durchgeführt");
            } catch (SQLException e) {
                IM.setErrorText("Ändern Fehlgeschlagen", e);
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
                DH.getAnfrageObjekt().executeUpdate("DELETE FROM Indiz WHERE IndizID = "+ IndizDaten.getIndizID());
            } catch (SQLException e) {
                IM.setErrorText("Löschen fehlgeschlagen", e);
            }
        });
        refreshIndizAnsicht();
    }
}
