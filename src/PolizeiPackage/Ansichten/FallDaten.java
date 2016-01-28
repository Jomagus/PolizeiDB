package PolizeiPackage.Ansichten;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FallDaten {
    private IntegerProperty FallID;
    private StringProperty Name;
    private StringProperty Eroeffnungsdatum;
    private StringProperty Enddatum;

    public FallDaten(Integer ID, String Namee, String DatumA, String DatumB) {
        this.FallID = new SimpleIntegerProperty(ID);
        this.Name = new SimpleStringProperty(Namee);
        this.Eroeffnungsdatum = new SimpleStringProperty(DatumA);
        this.Enddatum = new SimpleStringProperty(DatumB);
    }

    public int getFallID() {
        return FallID.get();
    }

    public IntegerProperty fallIDProperty() {
        return FallID;
    }

    public void setFallID(int fallID) {
        this.FallID.set(fallID);
    }

    public String getName() {
        return Name.get();
    }

    public StringProperty nameProperty() {
        return Name;
    }

    public void setName(String name) {
        this.Name.set(name);
    }

    public String getEroeffnungsdatum() {
        return Eroeffnungsdatum.get();
    }

    public StringProperty eroeffnungsdatumProperty() {
        return Eroeffnungsdatum;
    }

    public void setEroeffnungsdatum(String eroeffnungsdatum) {
        this.Eroeffnungsdatum.set(eroeffnungsdatum);
    }

    public String getEnddatum() {
        return Enddatum.get();
    }

    public StringProperty enddatumProperty() {
        return Enddatum;
    }

    public void setEnddatum(String enddatum) {
        this.Enddatum.set(enddatum);
    }
}
