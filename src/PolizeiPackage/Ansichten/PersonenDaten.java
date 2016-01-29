package PolizeiPackage.Ansichten;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PersonenDaten {
    private IntegerProperty PersonenID;
    private StringProperty Name;
    private StringProperty GebDatum;
    private StringProperty Nation;
    private StringProperty Geschlecht;
    private StringProperty TodDatum;

    public PersonenDaten(int ID, String Na, String Ge, String Nat, String Gesch, String To) {
        PersonenID = new SimpleIntegerProperty(ID);
        Name = new SimpleStringProperty(Na);
        GebDatum = new SimpleStringProperty(Ge);
        Nation = new SimpleStringProperty(Nat);
        Geschlecht = new SimpleStringProperty(Gesch);
        TodDatum = new SimpleStringProperty(To);
    }

    public int getPersonenID() {
        return PersonenID.get();
    }

    public IntegerProperty personenIDProperty() {
        return PersonenID;
    }

    public void setPersonenID(int personenID) {
        this.PersonenID.set(personenID);
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

    public String getGebDatum() {
        return GebDatum.get();
    }

    public StringProperty gebDatumProperty() {
        return GebDatum;
    }

    public void setGebDatum(String gebDatum) {
        this.GebDatum.set(gebDatum);
    }

    public String getNation() {
        return Nation.get();
    }

    public StringProperty nationProperty() {
        return Nation;
    }

    public void setNation(String nation) {
        this.Nation.set(nation);
    }

    public String getGeschlecht() {
        return Geschlecht.get();
    }

    public StringProperty geschlechtProperty() {
        return Geschlecht;
    }

    public void setGeschlecht(String geschlecht) {
        this.Geschlecht.set(geschlecht);
    }

    public String getTodDatum() {
        return TodDatum.get();
    }

    public StringProperty todDatumProperty() {
        return TodDatum;
    }

    public void setTodDatum(String todDatum) {
        this.TodDatum.set(todDatum);
    }
}
