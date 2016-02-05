package PolizeiPackage;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Erzeugt eine Bestaetigungsbox
 */
public abstract class BestaetigungsBox {

    static boolean AntwortWert;

    public static boolean ErstellePopUp(String Fenstertitel, String Meldungstext) {
        Stage Fenster = new Stage();
        Fenster.initModality(Modality.APPLICATION_MODAL);
        Fenster.setTitle(Fenstertitel);
        Fenster.setAlwaysOnTop(true);
        Fenster.setResizable(false);
        Fenster.setOnCloseRequest(event -> {
            event.consume();
            AntwortWert = false;
            Fenster.close();
        });

        Label AnzeigeText = new Label(Meldungstext);

        Button ButtonFort = new Button("Fortfahren");
        Button ButtonAbb = new Button("Abbrechen");

        ButtonFort.defaultButtonProperty();
        ButtonAbb.cancelButtonProperty();

        ButtonFort.setMaxWidth(Double.MAX_VALUE);
        ButtonAbb.setMaxWidth(Double.MAX_VALUE);

        ButtonFort.setOnAction(event -> {
            AntwortWert = true;
            Fenster.close();
        });
        ButtonAbb.setOnAction(event -> {
            AntwortWert = false;
            Fenster.close();
        });

        VBox AussenBox = new VBox(10);
        HBox InnenBox = new HBox();

        AussenBox.setSpacing(10);
        AussenBox.setPadding(new Insets(10));
        InnenBox.setSpacing(10);

        AussenBox.setAlignment(Pos.CENTER);
        InnenBox.setAlignment(Pos.BOTTOM_CENTER);

        AussenBox.getChildren().addAll(AnzeigeText, InnenBox);
        InnenBox.getChildren().addAll(ButtonFort, ButtonAbb);

        Fenster.setScene(new Scene(AussenBox));
        Fenster.showAndWait();
        return AntwortWert;
    }
}
