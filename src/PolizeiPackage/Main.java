package PolizeiPackage;

import PolizeiPackage.Ansichten.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

/**
 * Hauptklasse und Startpunkt
 */
public class Main extends Application {

    Stage PrimaereStage;
    Scene PrimaereScene;
    BorderPane PrimaeresLayout;
    DatenbankHandler DBH;
    InfoErrorManager IEM;
    ArtAnsichtManager ArtAM;
    FallAnsichtManager FallAM;
    VerbrechenAnsichtManager VerbrechenAM;
    BezirkAnsichtManager BezirkAM;
    BehoerdenAnsichtManager BehAM;
    PersonenAnsichtManager PersonenAM;
    PolizistAnsichtManager PolizistAM;
    NotizAnsichtManager NotizAM;
    IndizAnsichtManager IndizAM;
    OpferAnsichtManager OpferAM;
    VerdachtigeAnsichtManager VerdachtigeAM;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialisiere benoetigte Subsysteme
        IEM = new InfoErrorManager();
        DBH = new DatenbankHandler(IEM);
        HauptMenueEventManagment HauptMenueManager = new HauptMenueEventManagment(PrimaereStage);
        ArtAM = new ArtAnsichtManager(DBH, IEM, this);
        FallAM = new FallAnsichtManager(DBH, IEM, this);
        VerbrechenAM = new VerbrechenAnsichtManager(DBH, IEM, this);
        BezirkAM = new BezirkAnsichtManager(DBH, IEM, this);
        BehAM = new BehoerdenAnsichtManager(DBH, IEM, this);
        PersonenAM = new PersonenAnsichtManager(DBH, IEM, this);
        PolizistAM = new PolizistAnsichtManager(DBH, IEM, this);
        NotizAM = new NotizAnsichtManager(DBH, IEM, this);
        IndizAM = new IndizAnsichtManager(DBH, IEM, this);
        OpferAM = new OpferAnsichtManager(DBH, IEM, this);
        VerdachtigeAM = new VerdachtigeAnsichtManager(DBH, IEM, this);

        ArtAM.setVerbrechensManager(VerbrechenAM);
        FallAM.setVerbrechenAnsichtManager(VerbrechenAM);

        // Setze die Primaere Stage
        PrimaereStage = primaryStage;
        PrimaereStage.setTitle("PolizeiDB");
        PrimaereStage.setOnCloseRequest(event -> {
            event.consume();
            BeendeProgramm();
        });

        // Setze das Primaere Layout
        PrimaeresLayout = new BorderPane();
        PrimaeresLayout.setTop(getHauptMenueLeiste(HauptMenueManager));
        PrimaeresLayout.setLeft(getLinkeAnsichtenLeiste());
        PrimaeresLayout.setBottom(IEM.getFussleiste());

        // Setze die Primaere Scene
        PrimaereScene = new Scene(PrimaeresLayout,1080,720);

        // Zeige alles an
        PrimaereStage.setScene(PrimaereScene);
        PrimaereStage.show();
    }

    /**
     * Erzeugt das Hauptmenue.
     *
     * @param EventManager Der EventManager der die ganzen Submenues verwaltet.
     * @return Das Hauptmenue.
     */
    private MenuBar getHauptMenueLeiste(HauptMenueEventManagment EventManager) {
        // Das Allgemeine Menue
        Menu AllgMenue = new Menu("_Allgemeines");
        MenuItem ItemFiltern = new MenuItem("_Filtern...");
        MenuItem ItemBeenden = new MenuItem("B_eenden");

        ItemFiltern.setOnAction(event -> EventManager.FilterSubMenue());
        ItemBeenden.setOnAction(event -> BeendeProgramm());

        AllgMenue.getItems().add(ItemFiltern);
        AllgMenue.getItems().add(new SeparatorMenuItem());
        AllgMenue.getItems().add(ItemBeenden);

        // Das Such Menue
        Menu SuchMenue = new Menu("_Suchen");
        MenuItem ItemPolizisten = new MenuItem("_Polizisten...");
        MenuItem ItemOpfer = new MenuItem("_Opfer...");
        MenuItem ItemVerdachtige = new MenuItem("_Verdächtige...");

        ItemPolizisten.setOnAction(event -> EventManager.SuchenPolizisten());
        ItemOpfer.setOnAction(event -> EventManager.SuchenOpfer());
        ItemVerdachtige.setOnAction(event -> EventManager.SuchenVerdaechtige());

        SuchMenue.getItems().add(ItemPolizisten);
        SuchMenue.getItems().add(ItemOpfer);
        SuchMenue.getItems().add(ItemVerdachtige);

        // Das Hilfe Menue
        Menu HilfeMenue = new Menu("_Hilfe");
        MenuItem ItemDBVacuum = new MenuItem("Datenbank optimieren");
        MenuItem ItemDBRebuild = new MenuItem("Datenbank neu erstellen");

        ItemDBVacuum.setOnAction(event -> {
            try {
                DBH.getAnfrageObjekt().execute("VACUUM ;");
                IEM.setInfoText("Datenbank optimiert");
            } catch (SQLException e) {
                IEM.setErrorText("Unbekannter Fehler beim optimieren der Datenbank", e);
            }
        });

        ItemDBRebuild.setOnAction(event -> {
            boolean DBRebuildBestaetigung = BestaetigungsBox.ErstellePopUp("Bestätigung", "Sind sie sicher? Alle vorhandenen Daten\nwerden dabei gelöscht werden!");
            if (DBRebuildBestaetigung) {
                IEM.setInfoText("Erstelle Datenbank neu...");
                DBH.RebuildDatabase();
            } else {
                IEM.setInfoText("Datenbankneuerstellung wurde abgebrochen");
            }
        });

        HilfeMenue.getItems().add(ItemDBVacuum);
        HilfeMenue.getItems().add(ItemDBRebuild);

        // Wir fuegen alle in die Hauptleiste ein

        MenuBar HauptLeiste = new MenuBar();
        HauptLeiste.getMenus().addAll(AllgMenue, SuchMenue, HilfeMenue);

        return HauptLeiste;
    }

    private ScrollPane getLinkeAnsichtenLeiste() {
        // Erzeuge eine Innere VBox
        VBox Inneres = new VBox();
        Inneres.setPadding(new Insets(10,20,10,10));    // Damit die Scrollleiste nicht ueber die Buttons geht
        Inneres.setSpacing(8);
        Inneres.alignmentProperty().set(Pos.BASELINE_CENTER);

        Label Beschriftung = new Label("Ansichten");

        // Lege Buttons an
        Button Faelle = new Button("Fälle");
        Button Verbrechen = new Button("Verbrechen");
        Button Arten = new Button("Arten");
        Button Bezirke = new Button("Bezirke");
        Button Behoerden = new Button("Behörden");
        Button Personen = new Button("Personen");
        Button Polizisten = new Button("Polizisten");
        Button Notizen = new Button("Notizen");
        Button Indizien = new Button("Indizien");
        Button Opfer = new Button("Opfer");
        Button Verdaechtige = new Button("Verdächtige");
        Button Arbeiten = new Button("Arbeiten");
        Button ArbeitenAn = new Button("Arbeiten an");
        Button LiegtIn = new Button("Liegt in");

        // Formatiere alle Buttons gleich breit
        Faelle.setMaxWidth(Double.MAX_VALUE);
        Verbrechen.setMaxWidth(Double.MAX_VALUE);
        Arten.setMaxWidth(Double.MAX_VALUE);
        Bezirke.setMaxWidth(Double.MAX_VALUE);
        Behoerden.setMaxWidth(Double.MAX_VALUE);
        Personen.setMaxWidth(Double.MAX_VALUE);
        Polizisten.setMaxWidth(Double.MAX_VALUE);
        Notizen.setMaxWidth(Double.MAX_VALUE);
        Indizien.setMaxWidth(Double.MAX_VALUE);
        Opfer.setMaxWidth(Double.MAX_VALUE);
        Verdaechtige.setMaxWidth(Double.MAX_VALUE);
        Arbeiten.setMaxWidth(Double.MAX_VALUE);
        ArbeitenAn.setMaxWidth(Double.MAX_VALUE);
        LiegtIn.setMaxWidth(Double.MAX_VALUE);

        // Definiere Klickverhalten

        Faelle.setOnAction(event -> PrimaeresLayout.setCenter(FallAM.getFallAnsicht()));
        Verbrechen.setOnAction(event -> PrimaeresLayout.setCenter(VerbrechenAM.getVerbrechenAnsicht()));
        Arten.setOnAction(event -> PrimaeresLayout.setCenter(ArtAM.getArtAnsicht()));
        Bezirke.setOnAction(event -> PrimaeresLayout.setCenter(BezirkAM.getBezirkAnsicht()));
        Behoerden.setOnAction(event -> PrimaeresLayout.setCenter(BehAM.getBehoerdenAnsicht()));
        Personen.setOnAction(event -> PrimaeresLayout.setCenter(PersonenAM.getPersonenAnsicht()));
        Polizisten.setOnAction(event -> PrimaeresLayout.setCenter(PolizistAM.getPolizistAnsicht()));
        Notizen.setOnAction(event -> PrimaeresLayout.setCenter(NotizAM.getNotizAnsicht()));
        Indizien.setOnAction(event -> PrimaeresLayout.setCenter(IndizAM.getIndizAnsicht()));
        Opfer.setOnAction(event -> PrimaeresLayout.setCenter(OpferAM.getOpferAnsicht()));
        Verdaechtige.setOnAction(event -> PrimaeresLayout.setCenter(VerdachtigeAM.getVerdachtigeAnsicht()));
        Arbeiten.setOnAction(event -> {});
        ArbeitenAn.setOnAction(event -> {});
        LiegtIn.setOnAction(event -> {});


        Inneres.getChildren().addAll(Beschriftung, Faelle, Verbrechen, Arten, Bezirke, Behoerden, Personen, Polizisten, Notizen, Indizien, Opfer, Verdaechtige, Arbeiten, ArbeitenAn, LiegtIn);

        // Bette alles in ein ScrollPane ein
        ScrollPane LinkeLeiste = new ScrollPane();
        LinkeLeiste.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        LinkeLeiste.setContent(Inneres);

        return LinkeLeiste;
    }

    public void setRechteAnsicht(Node Detailansicht) {
        PrimaeresLayout.setRight(Detailansicht);
    }

    public void setMittlereAnsicht(Node Centeransicht) {
        PrimaeresLayout.setCenter(Centeransicht);
    }

    public Stage getPrimaereStage() {
        return PrimaereStage;
    }

    /**
     * Beendet das Programm
     */
    private void BeendeProgramm() {
        DBH.BeendeDatenbankAnbindung();
        PrimaereStage.close();
    }
}
