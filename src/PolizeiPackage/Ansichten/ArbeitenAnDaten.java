package PolizeiPackage.Ansichten;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ArbeitenAnDaten {
    private IntegerProperty PersonenID;
    private StringProperty PersonenName;
    private IntegerProperty FallID;
    private StringProperty FallName;
    private StringProperty VonDatum;
    private StringProperty BisDatum;

    public ArbeitenAnDaten(int PID, String PNa, int BID, String BNa, String VDa, String BDa) {
        PersonenID = new SimpleIntegerProperty(PID);
        PersonenName = new SimpleStringProperty(PNa);
        FallID = new SimpleIntegerProperty(BID);
        FallName = new SimpleStringProperty(BNa);
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

    public int getFallID() {
        return FallID.get();
    }

    public IntegerProperty fallIDProperty() {
        return FallID;
    }

    public void setFallID(int fallID) {
        this.FallID.set(fallID);
    }

    public String getFallName() {
        return FallName.get();
    }

    public StringProperty fallNameProperty() {
        return FallName;
    }

    public void setFallName(String fallName) {
        this.FallName.set(fallName);
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
