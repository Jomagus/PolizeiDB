package PolizeiPackage;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;

/**
 * Gibt dem Nutzer aktuelle Informationen ueber die Fussleiste aus.
 */
public class FussleistenInfo {
    private Label InfoLabel;

    /**
     * Konstruktor. ACHTUNG: setLabel Funktion muss einmal gelaufen sein, bevor diese Klasse arbeiten kann.
     */
    public FussleistenInfo() {
        InfoLabel = null;
    }

    public void setLabel(Label InfoboxLabel) {
        InfoLabel = InfoboxLabel;
    }

    /**
     * Setzt einen Info Label Text.
     *
     * @param InfoText Der angezeigte Text
     */
    public void setInfoText(String InfoText) {
        if (InfoLabel != null) {
            InfoLabel.setTextFill(Color.WHITE);
            InfoLabel.setText(InfoText);
        }
    }

    /**
     * Setzt einen Error Label Text.
     *
     * @param InfoText Der angezeigte Text
     */
    public void setErrorText(String InfoText) {
        if (InfoLabel != null) {
            InfoLabel.setTextFill(Color.RED);
            InfoLabel.setText(InfoText);
        }
    }
}
