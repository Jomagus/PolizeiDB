package PolizeiPackage;

import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

/**
 * Gibt dem Nutzer aktuelle Informationen ueber die Fussleiste aus.
 */
public class InfoErrorManager { //TODO evtl diese klasse benutzen um logs zu erzeugen
    private Label InfoLabel;
    private Label RechtesLabel;
    private AnchorPane FussLeiste;

    public InfoErrorManager() {
        FussLeiste = new AnchorPane();
        FussLeiste.setStyle("-fx-background-color: #505050;");

        InfoLabel = new Label();
        InfoLabel.setTextFill(Color.WHITE);


        RechtesLabel = new Label("NOCH ÄNDERN!");
        RechtesLabel.setTextFill(Color.WHITE);


        FussLeiste.getChildren().addAll(InfoLabel, RechtesLabel);
        AnchorPane.setLeftAnchor(InfoLabel, (double) 10);
        AnchorPane.setRightAnchor(RechtesLabel, (double) 10);
    }

    /**
     * Erzeugt eine Fussleiste.
     *
     * @return Die Fussleiste
     */
    public AnchorPane getFussleiste() {
        return FussLeiste;
    }

    /**
     * Setzt einen Info Label Text in der Fussleiste.
     *
     * @param InfoText Der angezeigte Text
     */
    public void setInfoText(String InfoText) {
        InfoLabel.setTextFill(Color.WHITE);
        InfoLabel.setText(InfoText);

    }

    /**
     * Setzt einen Error Label Text in der Fussleiste.
     *
     * @param InfoText Der angezeigte Text
     */
    public void setErrorText(String InfoText) {
        InfoLabel.setTextFill(Color.RED);
        InfoLabel.setText(InfoText);
    }

    /**
     * Löscht den Text aus der Fussleiste
     */
    public void resetText() {
        InfoLabel.setText("");
    }

    /**
     * Löscht den Text aus der Fussleiste aber speichert trotzdem eine Nachricht im Log
     *
     * @param Nachricht Die Nachricht fuer das Log
     */
    public void resetText(String Nachricht) {
        // TODO Nachricht loggen
        resetText();
    }
}
