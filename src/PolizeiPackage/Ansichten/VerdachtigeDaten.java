package PolizeiPackage.Ansichten;

import javafx.beans.property.*;

public class VerdachtigeDaten {
    private IntegerProperty PersonenID;
    private StringProperty PersonenName;
    private IntegerProperty VerbrechensID;
    private StringProperty VerbrechensName;
    private BooleanProperty Uberfuhrt;
    private StringProperty UberfuhrtString;

    public VerdachtigeDaten(int PID, String PNa, int VID, String VNa, boolean Ub) {
        PersonenID = new SimpleIntegerProperty(PID);
        PersonenName = new SimpleStringProperty(PNa);
        VerbrechensID = new SimpleIntegerProperty(VID);
        VerbrechensName = new SimpleStringProperty(VNa);
        Uberfuhrt = new SimpleBooleanProperty(Ub);
        if (Ub) {
            UberfuhrtString = new SimpleStringProperty("Ja");
        } else {
            UberfuhrtString = new SimpleStringProperty("Nein");
        }
    }

    public String getUberfuhrtString() {
        return UberfuhrtString.get();
    }

    public StringProperty uberfuhrtStringProperty() {
        return UberfuhrtString;
    }

    public void setUberfuhrtString(String uberfuhrtString) {
        this.UberfuhrtString.set(uberfuhrtString);
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

    public String getVerbrechensName() {
        return VerbrechensName.get();
    }

    public StringProperty verbrechensNameProperty() {
        return VerbrechensName;
    }

    public void setVerbrechensName(String verbrechensName) {
        this.VerbrechensName.set(verbrechensName);
    }

    public boolean getUberfuhrt() {
        return Uberfuhrt.get();
    }

    public BooleanProperty uberfuhrtProperty() {
        return Uberfuhrt;
    }

    public void setUberfuhrt(boolean uberfuhrt) {
        this.Uberfuhrt.set(uberfuhrt);
    }
}
