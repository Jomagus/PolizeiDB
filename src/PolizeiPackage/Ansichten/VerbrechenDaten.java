package PolizeiPackage.Ansichten;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class VerbrechenDaten {
    private IntegerProperty VerbrechensID;
    private StringProperty Name;
    private StringProperty Datum;
    private IntegerProperty BezirksID;
    private IntegerProperty FallID;
    private IntegerProperty ArtID;
    private StringProperty BezirkName;
    private StringProperty FallName;
    private StringProperty ArtName;

    public VerbrechenDaten(int VID, String Na, String Da, int BID, int FID, int AID, String BN, String FN, String AN) {
        VerbrechensID = new SimpleIntegerProperty(VID);
        Name = new SimpleStringProperty(Na);
        Datum = new SimpleStringProperty(Da);
        BezirksID = new SimpleIntegerProperty(BID);
        FallID = new SimpleIntegerProperty(FID);
        ArtID = new SimpleIntegerProperty(AID);
        BezirkName = new SimpleStringProperty(BN);
        FallName = new SimpleStringProperty(FN);
        ArtName = new SimpleStringProperty(AN);
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

    public String getName() {
        return Name.get();
    }

    public StringProperty nameProperty() {
        return Name;
    }

    public void setName(String name) {
        this.Name.set(name);
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

    public int getBezirksID() {
        return BezirksID.get();
    }

    public IntegerProperty bezirksIDProperty() {
        return BezirksID;
    }

    public void setBezirksID(int bezirksID) {
        this.BezirksID.set(bezirksID);
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

    public int getArtID() {
        return ArtID.get();
    }

    public IntegerProperty artIDProperty() {
        return ArtID;
    }

    public void setArtID(int artID) {
        this.ArtID.set(artID);
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

    public String getFallName() {
        return FallName.get();
    }

    public StringProperty fallNameProperty() {
        return FallName;
    }

    public void setFallName(String fallName) {
        this.FallName.set(fallName);
    }

    public String getArtName() {
        return ArtName.get();
    }

    public StringProperty artNameProperty() {
        return ArtName;
    }

    public void setArtName(String artName) {
        this.ArtName.set(artName);
    }
}
