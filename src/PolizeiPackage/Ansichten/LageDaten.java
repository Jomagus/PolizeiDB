package PolizeiPackage.Ansichten;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LageDaten {
    private IntegerProperty InnenID;
    private StringProperty InnenName;
    private IntegerProperty AussenID;
    private StringProperty AussenName;

    public int getInnenID() {
        return InnenID.get();
    }

    public IntegerProperty innenIDProperty() {
        return InnenID;
    }

    public void setInnenID(int innenID) {
        this.InnenID.set(innenID);
    }

    public String getInnenName() {
        return InnenName.get();
    }

    public StringProperty innenNameProperty() {
        return InnenName;
    }

    public void setInnenName(String innenName) {
        this.InnenName.set(innenName);
    }

    public int getAussenID() {
        return AussenID.get();
    }

    public IntegerProperty aussenIDProperty() {
        return AussenID;
    }

    public void setAussenID(int aussenID) {
        this.AussenID.set(aussenID);
    }

    public String getAussenName() {
        return AussenName.get();
    }

    public StringProperty aussenNameProperty() {
        return AussenName;
    }

    public void setAussenName(String aussenName) {
        this.AussenName.set(aussenName);
    }

    public LageDaten(int IID, String INa, int AID, String ANa) {
        InnenID = new SimpleIntegerProperty(IID);
        InnenName = new SimpleStringProperty(INa);
        AussenID = new SimpleIntegerProperty(AID);
        AussenName = new SimpleStringProperty(ANa);


    }
}
