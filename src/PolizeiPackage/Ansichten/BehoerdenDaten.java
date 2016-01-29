package PolizeiPackage.Ansichten;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BehoerdenDaten {
    private IntegerProperty BehoerdenID;
    private StringProperty Name;
    private StringProperty Typ;
    private IntegerProperty verantwortlichBezirksID;
    private StringProperty BezirkName;

    public BehoerdenDaten(int ID, String Na, String Ty, int BeID, String BeNa) {
        BehoerdenID = new SimpleIntegerProperty(ID);
        Name = new SimpleStringProperty(Na);
        Typ = new SimpleStringProperty(Ty);
        verantwortlichBezirksID = new SimpleIntegerProperty(BeID);
        BezirkName = new SimpleStringProperty(BeNa);
    }

    public int getBehoerdenID() {
        return BehoerdenID.get();
    }

    public IntegerProperty behoerdenIDProperty() {
        return BehoerdenID;
    }

    public void setBehoerdenID(int behoerdenID) {
        this.BehoerdenID.set(behoerdenID);
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

    public String getTyp() {
        return Typ.get();
    }

    public StringProperty typProperty() {
        return Typ;
    }

    public void setTyp(String typ) {
        this.Typ.set(typ);
    }

    public int getVerantwortlichBezirksID() {
        return verantwortlichBezirksID.get();
    }

    public IntegerProperty verantwortlichBezirksIDProperty() {
        return verantwortlichBezirksID;
    }

    public void setVerantwortlichBezirksID(int verantwortlichBezirksID) {
        this.verantwortlichBezirksID.set(verantwortlichBezirksID);
    }

    public String getBezirkName() {
        return BezirkName.get();
    }

    public StringProperty bezirkNameProperty() {
        return BezirkName;
    }

    public void setBezirkName(String bezirkName) {
        this.BezirkName.set(bezirkName);
    }
}
