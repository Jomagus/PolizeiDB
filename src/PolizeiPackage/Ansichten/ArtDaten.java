package PolizeiPackage.Ansichten;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ArtDaten {
    private IntegerProperty ArtID;
    private StringProperty Name;
    private StringProperty Beschreibung;

    public ArtDaten(Integer ArtID, String Name, String Beschreibung) {
        this.ArtID = new SimpleIntegerProperty(ArtID);
        this.Name = new SimpleStringProperty(Name);
        this.Beschreibung = new SimpleStringProperty(Beschreibung);
    }

    public int getArtID() {
        return ArtID.get();
    }

    public IntegerProperty artIDProperty() {
        return ArtID;
    }

    public void setArtID(int artID) {
        this.ArtID.set(artID);
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

    public String getBeschreibung() {
        return Beschreibung.get();
    }

    public StringProperty beschreibungProperty() {
        return Beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.Beschreibung.set(beschreibung);
    }
}
