package PolizeiPackage.Ansichten;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class IndizDaten {
    private IntegerProperty IndizID;
    private StringProperty Datum;
    private byte[] Bild;
    private StringProperty Text;
    private IntegerProperty PersonenID;
    private IntegerProperty FallID;
    private StringProperty PersonenName;
    private StringProperty FallName;

    public IndizDaten(int NID, String Da, byte[] Bi, String Te, int PID, int FID, String PNa, String FNa) {
        IndizID = new SimpleIntegerProperty(NID);
        Datum = new SimpleStringProperty(Da);
        Bild = Bi;
        Text = new SimpleStringProperty(Te);
        PersonenID = new SimpleIntegerProperty(PID);
        FallID = new SimpleIntegerProperty(FID);
        PersonenName = new SimpleStringProperty(PNa);
        FallName = new SimpleStringProperty(FNa);
    }

    public int getIndizID() {
        return IndizID.get();
    }

    public IntegerProperty indizIDProperty() {
        return IndizID;
    }

    public void setIndizID(int indizID) {
        this.IndizID.set(indizID);
    }

    public String getDatum() {
        return Datum.get();
    }

    public StringProperty datumProperty() {
        return Datum;
    }

    public void setDatum(String datum) {
        this.Datum.set(datum);
    }

    public byte[] getBild() {
        return Bild;
    }

    public void setBild(byte[] bild) {
        Bild = bild;
    }

    public String getText() {
        return Text.get();
    }

    public StringProperty textProperty() {
        return Text;
    }

    public void setText(String text) {
        this.Text.set(text);
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

    public int getFallID() {
        return FallID.get();
    }

    public IntegerProperty fallIDProperty() {
        return FallID;
    }

    public void setFallID(int fallID) {
        this.FallID.set(fallID);
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

    public String getFallName() {
        return FallName.get();
    }

    public StringProperty fallNameProperty() {
        return FallName;
    }

    public void setFallName(String fallName) {
        this.FallName.set(fallName);
    }
}
