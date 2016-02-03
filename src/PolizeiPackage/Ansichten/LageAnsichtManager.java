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
public class LageAnsichtManager {

    private DatenbankHandler DH;
    private InfoErrorManager IM;
    private Main Hauptprogramm;
    private TableView<LageDaten> Tabelle;
    private BorderPane DatenAnsicht;
    private ObservableList<LageDaten> LageDatenListe;
    private boolean LageAnsichtGeneriert;
    private BezirkAnsichtManager BezirkAM;

    public LageAnsichtManager(DatenbankHandler DBH, InfoErrorManager IEM, Main HauptFenster) {
        DH = DBH;
        IM = IEM;
        Hauptprogramm = HauptFenster;
        LageDatenListe = FXCollections.observableArrayList();
        Tabelle = new TableView<>();
        LageAnsichtGeneriert = false;
    }

    public void setBezirkAM(BezirkAnsichtManager bezirkAM) {
        BezirkAM = bezirkAM;
    }

    public Node getLageAnsicht() {
        if (LageAnsichtGeneriert) {
            refreshLageAnsicht();
            return DatenAnsicht;
        }
        IM.setInfoText("Lade Lage Ansicht");
        DatenAnsicht = new BorderPane(getLageAnsichtInnereTabelle());

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

        IM.setInfoText("Laden der Lage Ansicht erfolgreich");
        LageAnsichtGeneriert = true;
        return DatenAnsicht;
    }

    /**
     * Erzeugt eine TableView mit den Daten aus der Datenbank.
     *
     * @return Eine TableView mit Lage Daten
     */
    private Node getLageAnsichtInnereTabelle() {
        refreshLageAnsicht();

        TableColumn<LageDaten, String> SpalteName = new TableColumn<>("Innerer Bezirk");
        SpalteName.setMinWidth(200);
        SpalteName.setCellValueFactory(new PropertyValueFactory<>("InnenName"));

        TableColumn<LageDaten, String> SpalteDatum = new TableColumn<>("liegt in");
        SpalteDatum.setMinWidth(200);
        SpalteDatum.setCellValueFactory(new PropertyValueFactory<>("AussenName"));

        Tabelle.setItems(LageDatenListe);
        Tabelle.getColumns().add(SpalteName);
        Tabelle.getColumns().add(SpalteDatum);

        Tabelle.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Doppelklicke auf Spalten sollen Detailansichten oeffnen:
        Tabelle.setRowFactory(param -> {
            TableRow<LageDaten> Spalte = new TableRow<>();
            Spalte.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!Spalte.isEmpty())) {
                    erzeugeDetailAnsicht(Spalte.getItem());
                }
            });
            return Spalte;
        });

        return Tabelle;
    }

    private void erzeugeDetailAnsicht(LageDaten SpaltenDaten) {
        Label LabelA = new Label("Innere BezirksID");
        Label LabelAWert = new Label(Integer.toString(SpaltenDaten.getInnenID()));

        Label LabelB = new Label("Innerer Bezirk");
        Label LabelBWert = new Label(SpaltenDaten.getInnenName());

        Label LabelC = new Label("Äußere BezirksID");
        Label LabelCWert = new Label(Integer.toString(SpaltenDaten.getAussenID()));

        Label LabelD = new Label("Äußerer Bezirk");
        Label LabelDWert = new Label(SpaltenDaten.getAussenName());

        Button ButtonBearbeiten = new Button("Bearbeiten...");
        Button ButtonLoeschen = new Button("Löschen");
        Button ButtonSucheLagesId = new Button("Suche nach innerem Bezirk");
        Button ButtonSucheBezirksId = new Button("Suche nach äußerem Bezirk");
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
        ButtonSucheLagesId.setOnAction(event -> {
            Hauptprogramm.setRechteAnsicht(null);
            BezirkAM.SucheBezirk(SpaltenDaten.getInnenID());
        });
        ButtonSucheBezirksId.setOnAction(event -> {
            Hauptprogramm.setRechteAnsicht(null);
            BezirkAM.SucheBezirk(SpaltenDaten.getAussenID());
        });

        ButtonClose.setOnAction(event -> Hauptprogramm.setRechteAnsicht(null));

        ButtonBearbeiten.setMaxWidth(Double.MAX_VALUE);
        ButtonBearbeiten.setMinWidth(150);
        ButtonLoeschen.setMaxWidth(Double.MAX_VALUE);
        ButtonLoeschen.setMinWidth(150);
        ButtonSucheLagesId.setMaxWidth(Double.MAX_VALUE);
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
        Mittelteil.getChildren().addAll(Oben, Unten, ButtonSucheLagesId, ButtonSucheBezirksId, ButtonClose);

        ScrollPane Aussen = new ScrollPane();

        Aussen.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        Aussen.setContent(Mittelteil);

        Hauptprogramm.setRechteAnsicht(Aussen);
    }

    /**
     * Aktualisiert die JavaFX vorliegenden Daten mit Daten aus der Datenbank.
     */
    public void refreshLageAnsicht() {
        LageDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT innere_BezirksID, INNEN.Name, äußere_BezirksID, AUSSEN.NAME " +
                    "FROM LIEGT_IN, BEZIRK AS INNEN, BEZIRK AS AUSSEN WHERE AUSSEN.BezirksID = LIEGT_IN.äußere_BezirksID AND INNEN.BezirksID = LIEGT_IN.innere_BezirksID;");
            while (AnfrageAntwort.next()) {
                LageDatenListe.add(new LageDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
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

        Label LabelD = new Label("Innerer Bezirk");
        Label LabelDWert = new Label();

        Label LabelE = new Label("Innere BezirksID");
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

        Label LabelF = new Label("Äußerer Bezirk");
        Label LabelFWert = new Label();

        Label LabelG = new Label("Äußere BezirksID");
        TextField LabelGWert = new TextField();

        LabelGWert.textProperty().addListener(((observable, oldValue, newValue) -> {
            try {
                PreparedStatement NutzerInput = DH.prepareStatement("SELECT Name FROM BEZIRK WHERE BezirksID = ?");
                NutzerInput.setInt(1, Integer.parseInt(newValue));
                ResultSet Antwort = NutzerInput.executeQuery();
                if (Antwort.next()) {
                    LabelFWert.setText(Antwort.getString(1));
                } else {
                    LabelFWert.setText("Ungültige BezirksID");
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
            String SQLString = "INSERT INTO LIEGT_IN (innere_BezirksID, äußere_BezirksID) VALUES (?, ?)";
            try {
                PreparedStatement InsertStatement = DH.prepareStatement(SQLString);
                InsertStatement.setInt(1, Integer.parseInt(LabelEWert.getText()));
                InsertStatement.setInt(2, Integer.parseInt(LabelGWert.getText()));
                InsertStatement.executeUpdate();
                IM.setInfoText("Einfügen durchgeführt");
            } catch (Exception e) {
                IM.setErrorText("Einfügen Fehlgeschlagen", e);
            }
            refreshLageAnsicht();
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    private void updateSelectedEntry() {
        ObservableList<LageDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        if (Nutzerauswahl.size() != 1) {
            IM.setErrorText("Es muss genau ein Element ausgewählt werden");
            return;
        }
        LageDaten Auswahl = Nutzerauswahl.get(0);

        // Jetzt erzeugen wir ein PopUp zum veraendern des Eintrags

        Stage PopUp = new Stage();
        PopUp.initModality(Modality.APPLICATION_MODAL);
        PopUp.setTitle("Eintrag ändern");
        PopUp.setAlwaysOnTop(true);
        PopUp.setResizable(false);

        GridPane Gitter = new GridPane();
        Gitter.setHgap(10);
        Gitter.setVgap(10);

        Label LabelD = new Label("Innerer Bezirk");
        Label LabelDWert = new Label();

        Label LabelE = new Label("Innere BezirksID");
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

        Label LabelF = new Label("Äußerer Bezirk");
        Label LabelFWert = new Label();

        Label LabelG = new Label("Äußere BezirksID");
        TextField LabelGWert = new TextField();

        LabelGWert.textProperty().addListener(((observable, oldValue, newValue) -> {
            try {
                PreparedStatement NutzerInput = DH.prepareStatement("SELECT Name FROM BEZIRK WHERE BezirksID = ?");
                NutzerInput.setInt(1, Integer.parseInt(newValue));
                ResultSet Antwort = NutzerInput.executeQuery();
                if (Antwort.next()) {
                    LabelFWert.setText(Antwort.getString(1));
                } else {
                    LabelFWert.setText("Ungültige BezirksID");
                }
            } catch (SQLException e) {
                IM.setErrorText("Unbekannter SQL Fehler", e);
            } catch (NumberFormatException e) {
                LabelFWert.setText("FallID muss eine Zahl sein");
            }
        }));

        LabelEWert.setText(Integer.toString(Auswahl.getInnenID()));
        LabelGWert.setText(Integer.toString(Auswahl.getAussenID()));

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
            String SQLString = "UPDATE LIEGT_IN SET innere_BezirksID=?, äußere_BezirksID=?  " +
                    "WHERE innere_BezirksID= " + Auswahl.getInnenID();
            try {
                PreparedStatement SQLInjektionNeinNein = DH.prepareStatement(SQLString);
                SQLInjektionNeinNein.setInt(1, Integer.parseInt(LabelEWert.getText()));
                SQLInjektionNeinNein.setInt(2, Integer.parseInt(LabelGWert.getText()));
                SQLInjektionNeinNein.executeUpdate();
                IM.setInfoText("Änderung durchgeführt");
            } catch (Exception e) {
                IM.setErrorText("Ändern Fehlgeschlagen", e);
            }
            refreshLageAnsicht();
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    private void deleteSelectedEntrys() {
        ObservableList<LageDaten> Nutzerauswahl = Tabelle.getSelectionModel().getSelectedItems();
        if (Nutzerauswahl.isEmpty()) {
            IM.setErrorText("Es muss mindestens ein Eintrag ausgewählt sein");
            return;
        }

        Nutzerauswahl.forEach(LageDaten -> {
            try {
                DH.getAnfrageObjekt().executeUpdate("DELETE FROM LIEGT_IN WHERE innere_BezirksID= " + LageDaten.getInnenID());
            } catch (SQLException e) {
                IM.setErrorText("Löschen fehlgeschlagen", e);
            }
        });
        refreshLageAnsicht();
    }

    public void SucheLageDaten(int BezirksID) {
        Hauptprogramm.setMittlereAnsicht(getLageAnsicht());
        LageDatenListe.clear();
        ResultSet AnfrageAntwort;
        try {
            AnfrageAntwort = DH.getAnfrageObjekt().executeQuery("SELECT innere_BezirksID, INNEN.Name, äußere_BezirksID, AUSSEN.NAME " +
                    "FROM LIEGT_IN, BEZIRK AS INNEN, BEZIRK AS AUSSEN " +
                    "WHERE AUSSEN.BezirksID = LIEGT_IN.äußere_BezirksID AND INNEN.BezirksID = LIEGT_IN.innere_BezirksID " +
                    "AND (INNEN.BezirksID = "+ BezirksID +" OR AUSSEN.BezirksID = "+ BezirksID +");");
            while (AnfrageAntwort.next()) {
                LageDatenListe.add(new LageDaten(AnfrageAntwort.getInt(1), AnfrageAntwort.getString(2),
                        AnfrageAntwort.getInt(3), AnfrageAntwort.getString(4)));
            }
        } catch (SQLException e) {
            IM.setErrorText("Unbekannter Fehler bei aktualisieren der Ansicht", e);
        }
    }
}
