package PolizeiPackage.Ansichten;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BezirkDaten {
    private IntegerProperty BezirksID;
    private StringProperty Name;

    public BezirkDaten(int BID, String Nam) {
        BezirksID = new SimpleIntegerProperty(BID);
        Name = new SimpleStringProperty(Nam);
    }

    public int getBezirksID() {
        return BezirksID.get();
    }

    public IntegerProperty bezirksIDProperty() {
        return BezirksID;
    }

    public void setBezirksID(int bezirksID) {
        this.BezirksID.set(bezirksID);
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
}
