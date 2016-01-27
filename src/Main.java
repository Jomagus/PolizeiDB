import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
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
        HauptMenueEventManagment HauptMenueManager = new HauptMenueEventManagment(PrimaereStage);

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



        // Setze die Primaere Scene
        PrimaereScene = new Scene(PrimaeresLayout,1080,720);

        // Zeige alles an
        PrimaereStage.setScene(PrimaereScene);
        PrimaereStage.show();
    }

    /**
     * Erzeugt das Hauptmenü.
     *
     * @return Das Hauptmenü.
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

        SuchMenue.getItems().add(ItemPolizisten);
        SuchMenue.getItems().add(ItemOpfer);
        SuchMenue.getItems().add(ItemVerdachtige);

        MenuBar HauptLeiste = new MenuBar();
        HauptLeiste.getMenus().addAll(AllgMenue, SuchMenue);

        return HauptLeiste;
    }

    /**
     * Beendet das Programm
     */
    private void BeendeProgramm() {
        PrimaereStage.close();
    }
}
