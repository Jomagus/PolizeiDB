package PolizeiPackage.Ansichten;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class OpferDaten {
    private IntegerProperty PersonenID;
    private StringProperty PersonenName;
    private IntegerProperty VerbrechensID;
    private StringProperty VerbrechenName;

    public OpferDaten(int PID, String PNa, int VID, String VNa) {
        PersonenID = new SimpleIntegerProperty(PID);
        PersonenName = new SimpleStringProperty(PNa);
        VerbrechensID = new SimpleIntegerProperty(VID);
        VerbrechenName = new SimpleStringProperty(VNa);
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

    public int getVerbrechensID() {
        return VerbrechensID.get();
    }

    public IntegerProperty verbrechensIDProperty() {
        return VerbrechensID;
    }

    public void setVerbrechensID(int verbrechensID) {
        this.VerbrechensID.set(verbrechensID);
    }

    public String getVerbrechenName() {
        return VerbrechenName.get();
    }

    public StringProperty verbrechenNameProperty() {
        return VerbrechenName;
    }

    public void setVerbrechenName(String verbrechenName) {
        this.VerbrechenName.set(verbrechenName);
    }
}
