package PolizeiPackage;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Hauptklasse und Startpunkt
 */
public class Main extends Application {

    Stage PrimaereStage;
    Scene PrimaereScene;
    BorderPane PrimaeresLayout;


    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialisiere benoetigte Subsysteme
        DatenbankHandler DBH = new DatenbankHandler();
        HauptMenueEventManagment HauptMenueManager = new HauptMenueEventManagment(PrimaereStage);

        DBH.RebuildDatabase(); // TODO DAS HIER LOESCHEN!!!

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
        PrimaeresLayout.setBottom(getFussLeiste());



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

        MenuBar HauptLeiste = new MenuBar();
        HauptLeiste.getMenus().addAll(AllgMenue, SuchMenue);

        return HauptLeiste;
    }

    private ScrollPane getLinkeAnsichtenLeiste() {
        // Erzeuge eine Innere VBox
        VBox Inneres = new VBox();
        Inneres.setPadding(new Insets(10));
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

        Inneres.getChildren().addAll(Beschriftung, Faelle, Verbrechen, Arten, Bezirke, Behoerden, Personen, Polizisten, Notizen, Indizien);

        // Bette alles in ein ScrollPane ein
        ScrollPane LinkeLeiste = new ScrollPane();
        LinkeLeiste.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        LinkeLeiste.setContent(Inneres);

        return LinkeLeiste;
    }

    private AnchorPane getFussLeiste() {
        AnchorPane FussLeiste = new AnchorPane();
        FussLeiste.setStyle("-fx-background-color: #505050;");

        Label TestLabel = new Label("NOCH ÄNDERN!");
        TestLabel.setTextFill(Color.WHITE);

        FussLeiste.getChildren().add(TestLabel);
        AnchorPane.setRightAnchor(TestLabel, (double) 10);



        return FussLeiste;
    }

    /**
     * Beendet das Programm
     */
    private void BeendeProgramm() {
        PrimaereStage.close();
    }
}
