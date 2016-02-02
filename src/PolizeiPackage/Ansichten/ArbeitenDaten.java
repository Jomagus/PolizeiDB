package PolizeiPackage.Ansichten;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ArbeitenDaten {
    private IntegerProperty PersonenID;
    private StringProperty PersonenName;
    private IntegerProperty BehordenID;
    private StringProperty BehordenName;
    private StringProperty VonDatum;
    private StringProperty BisDatum;

    public ArbeitenDaten(int PID, String PNa, int BID, String BNa, String VDa, String BDa) {
        PersonenID = new SimpleIntegerProperty(PID);
        PersonenName = new SimpleStringProperty(PNa);
        BehordenID = new SimpleIntegerProperty(BID);
        BehordenName = new SimpleStringProperty(BNa);
        VonDatum = new SimpleStringProperty(VDa);
        BisDatum = new SimpleStringProperty(BDa);
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

    public String getPersonenName() {
        return PersonenName.get();
    }

    public StringProperty personenNameProperty() {
        return PersonenName;
    }

    public void setPersonenName(String personenName) {
        this.PersonenName.set(personenName);
    }

    public int getBehordenID() {
        return BehordenID.get();
    }

    public IntegerProperty behordenIDProperty() {
        return BehordenID;
    }

    public void setBehordenID(int behordenID) {
        this.BehordenID.set(behordenID);
    }

    public String getBehordenName() {
        return BehordenName.get();
    }

    public StringProperty behordenNameProperty() {
        return BehordenName;
    }

    public void setBehordenName(String behordenName) {
        this.BehordenName.set(behordenName);
    }

    public String getVonDatum() {
        return VonDatum.get();
    }

    public StringProperty vonDatumProperty() {
        return VonDatum;
    }

    public void setVonDatum(String vonDatum) {
        this.VonDatum.set(vonDatum);
    }

    public String getBisDatum() {
        return BisDatum.get();
    }

    public StringProperty bisDatumProperty() {
        return BisDatum;
    }

    public void setBisDatum(String bisDatum) {
        this.BisDatum.set(bisDatum);
    }
}
