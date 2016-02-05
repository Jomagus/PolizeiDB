package PolizeiPackage;

import java.sql.*;

/**
 * Verwaltet die Datenbankzugriffe fuer die PolizeiDB
 */
public class DatenbankHandler {
    static int SQL_TIMEOUT_TIME = 60;

    private Connection Verbindung;
    private Statement AnfrageObjekt;
    private InfoErrorManager IEM;

    public DatenbankHandler(InfoErrorManager FLInfo) {
        this.IEM = FLInfo;
        // Wir versuchen eine Verbindung zur Datenbank herzustellen.
        Verbindung = null;
        VerbindeDatenbank();
        if (Verbindung == null) {
            IEM.setErrorText("Konnte keine Verbindung zur Datenbank herstellen. Starten sie das Programm bitte neu.");
        }
    }

    /**
     * Stellt eine Verbindung mit der Datenbank her.
     */
    private void VerbindeDatenbank() {
        try {
            Verbindung = DriverManager.getConnection("jdbc:sqlite:Polizei.db");
            Verbindung.setAutoCommit(true);
            AnfrageObjekt = Verbindung.createStatement();
            AnfrageObjekt.setQueryTimeout(SQL_TIMEOUT_TIME);
        } catch (SQLException e) {
            IEM.setErrorText(e.getMessage());
            return;
        }
        IEM.setInfoText("Verbindung zur Datenbank hergestellt");
    }

    /**
     * Löscht (!!!) die Datenbank und erstellt eine neue.
     */
    public void RebuildDatabase() {
        try {
            AnfrageObjekt.executeUpdate("DROP TABLE IF EXISTS ARBEITEN;");
            AnfrageObjekt.executeUpdate("DROP TABLE IF EXISTS ARBEITEN_AN;");
            AnfrageObjekt.executeUpdate("DROP TABLE IF EXISTS ART;");
            AnfrageObjekt.executeUpdate("DROP TABLE IF EXISTS BEHÖRDE;");
            AnfrageObjekt.executeUpdate("DROP TABLE IF EXISTS BEZIRK;");
            AnfrageObjekt.executeUpdate("DROP TABLE IF EXISTS FALL;");
            AnfrageObjekt.executeUpdate("DROP TABLE IF EXISTS INDIZ;");
            AnfrageObjekt.executeUpdate("DROP TABLE IF EXISTS LIEGT_IN;");
            AnfrageObjekt.executeUpdate("DROP TABLE IF EXISTS NOTIZ;");
            AnfrageObjekt.executeUpdate("DROP TABLE IF EXISTS PERSON;");
            AnfrageObjekt.executeUpdate("DROP TABLE IF EXISTS POLIZIST;");
            AnfrageObjekt.executeUpdate("DROP TABLE IF EXISTS SIND_OPFER;");
            AnfrageObjekt.executeUpdate("DROP TABLE IF EXISTS SIND_VERDÄCHTIGE;");
            AnfrageObjekt.executeUpdate("DROP TABLE IF EXISTS VERBRECHEN;");

            AnfrageObjekt.executeUpdate("CREATE TABLE PERSON(\n" +
                    "  PersonenID INTEGER PRIMARY KEY,\n" +
                    "  Name TEXT NOT NULL,\n" +
                    "  Geburtsdatum TEXT NOT NULL,\n" +
                    "  Nationalität TEXT NOT NULL,\n" +
                    "  Geschlecht TEXT NOT NULL,\n" +
                    "  Todesdatum TEXT CHECK (julianday(Todesdatum) > julianday(Geburtsdatum)) -- wir gehen das Risiko ein, bei illegalen Abtreibungen Probleme hier zu bekommen, fuer mehr Sicherheit\n" +
                    ");");
            AnfrageObjekt.executeUpdate("CREATE TABLE POLIZIST(\n" +
                    "  PersonenID INTEGER PRIMARY KEY,\n" +
                    "  Dienstgrad TEXT NOT NULL,\n" +
                    "  FOREIGN KEY (PersonenID) REFERENCES PERSON(PersonenID) ON UPDATE CASCADE ON DELETE CASCADE\n" +
                    ");");
            AnfrageObjekt.executeUpdate("CREATE TABLE ART(\n" +
                    "  ArtID INTEGER PRIMARY KEY ,\n" +
                    "  Name TEXT NOT NULL,\n" +
                    "  Beschreibung TEXT NOT NULL\n" +
                    ");");
            AnfrageObjekt.executeUpdate("CREATE TABLE BEZIRK(\n" +
                    "  BezirksID INTEGER PRIMARY KEY,\n" +
                    "  Name TEXT NOT NULL\n" +
                    ");");
            AnfrageObjekt.executeUpdate("CREATE TABLE BEHÖRDE(\n" +
                    "  BehördenID INTEGER PRIMARY KEY,\n" +
                    "  Name TEXT NOT NULL,\n" +
                    "  Typ TEXT NOT NULL,\n" +
                    "  verantwortlich_für_BezirksID INTEGER NOT NULL,\n" +
                    "  FOREIGN KEY (verantwortlich_für_BezirksID) REFERENCES BEZIRK(BezirksID) ON UPDATE CASCADE ON DELETE RESTRICT\n" +
                    ");");
            AnfrageObjekt.executeUpdate("CREATE TABLE FALL(\n" +
                    "  FallID INTEGER PRIMARY KEY ,\n" +
                    "  Name TEXT NOT NULL ,\n" +
                    "  Eröffnungsdatum TEXT NOT NULL , -- SQLites Date-Funktion(en) agieren auf Datumsstrings\n" +
                    "  Enddatum TEXT\n" +
                    ");");
            AnfrageObjekt.executeUpdate("CREATE TABLE VERBRECHEN(\n" +
                    "  VerbrechensID INTEGER PRIMARY KEY ,\n" +
                    "  Name TEXT NOT NULL ,\n" +
                    "  Datum TEXT NOT NULL ,\n" +
                    "  geschieht_in_BezirksID INTEGER NOT NULL ,\n" +
                    "  gehört_zu_FallID INTEGER NOT NULL ,\n" +
                    "  gehört_zu_ArtID TEXT NOT NULL ,\n" +
                    "  FOREIGN KEY (geschieht_in_BezirksID) REFERENCES BEZIRK(BezirksID) ON UPDATE CASCADE ON DELETE RESTRICT,\n" +
                    "  FOREIGN KEY (gehört_zu_FallID) REFERENCES FALL(FallID) ON UPDATE CASCADE ON DELETE RESTRICT,\n" +
                    "  FOREIGN KEY (gehört_zu_ArtID) REFERENCES ART(ArtID) ON UPDATE CASCADE ON DELETE RESTRICT\n" +
                    ");");
            AnfrageObjekt.executeUpdate("CREATE TABLE INDIZ(\n" +
                    "  IndizID INTEGER PRIMARY KEY ,\n" +
                    "  Datum TEXT NOT NULL ,\n" +
                    "  Bild BLOB NOT NULL ,\n" +
                    "  Text TEXT ,\n" +
                    "  angelegt_von_PersonenID INTEGER NOT NULL ,\n" +
                    "  angelegt_zu_FallID INTEGER NOT NULL ,\n" +
                    "  FOREIGN KEY (angelegt_von_PersonenID) REFERENCES POLIZIST(PersonenID) ON UPDATE CASCADE ON DELETE RESTRICT,\n" +
                    "  FOREIGN KEY (angelegt_zu_FallID) REFERENCES FALL(FallID) ON UPDATE CASCADE ON DELETE RESTRICT\n" +
                    ");");
            AnfrageObjekt.executeUpdate("CREATE TABLE NOTIZ(\n" +
                    "  NotizID INTEGER PRIMARY KEY ,\n" +
                    "  Datum TEXT NOT NULL ,\n" +
                    "  Text TEXT NOT NULL ,\n" +
                    "  angelegt_von_PersonenID INTEGER NOT NULL ,\n" +
                    "  angelegt_zu_FallID INTEGER NOT NULL ,\n" +
                    "  FOREIGN KEY (angelegt_von_PersonenID) REFERENCES POLIZIST(PersonenID) ON UPDATE CASCADE ON DELETE RESTRICT,\n" +
                    "  FOREIGN KEY (angelegt_zu_FallID) REFERENCES FALL(FallID) ON UPDATE CASCADE ON DELETE RESTRICT\n" +
                    ");");
            AnfrageObjekt.executeUpdate("CREATE TABLE SIND_OPFER(\n" +
                    "  PersonenID INTEGER NOT NULL ,\n" +
                    "  VerbrechensID INTEGER NOT NULL ,\n" +
                    "  PRIMARY KEY (PersonenID, VerbrechensID),\n" +
                    "  FOREIGN KEY (PersonenID) REFERENCES PERSON(PersonenID) ON UPDATE CASCADE ON DELETE RESTRICT,\n" +
                    "  FOREIGN KEY (VerbrechensID) REFERENCES VERBRECHEN(VerbrechensID) ON UPDATE CASCADE ON DELETE RESTRICT\n" +
                    ");");
            AnfrageObjekt.executeUpdate("CREATE TABLE SIND_VERDÄCHTIGE(\n" +
                    "  PersonenID INTEGER NOT NULL ,\n" +
                    "  VerbrechensID INTEGER NOT NULL ,\n" +
                    "  Überführt INTEGER NOT NULL ,    -- wir nutzen 0 und 1 fuer boolsche Werte, 0 false, sonst true, wie in C\n" +
                    "  PRIMARY KEY (PersonenID, VerbrechensID),\n" +
                    "  FOREIGN KEY (PersonenID) REFERENCES PERSON(PersonenID) ON UPDATE CASCADE ON DELETE RESTRICT,\n" +
                    "  FOREIGN KEY (VerbrechensID) REFERENCES VERBRECHEN(VerbrechensID) ON UPDATE CASCADE ON DELETE RESTRICT\n" +
                    ");");
            AnfrageObjekt.executeUpdate("CREATE TABLE LIEGT_IN(\n" +
                    "  innere_BezirksID INTEGER PRIMARY KEY ,\n" +
                    "  äußere_BezirksID INTEGER NOT NULL ,\n" +
                    "  FOREIGN KEY (innere_BezirksID) REFERENCES BEZIRK(BezirksID) ON UPDATE CASCADE ON DELETE RESTRICT,\n" +
                    "  FOREIGN KEY (äußere_BezirksID) REFERENCES BEZIRK(BezirksID) ON UPDATE CASCADE ON DELETE RESTRICT\n" +
                    ");");
            AnfrageObjekt.executeUpdate("CREATE TABLE ARBEITEN(\n" +
                    "  PersonenID INTEGER NOT NULL ,\n" +
                    "  BehördenID INTEGER NOT NULL ,\n" +
                    "  von TEXT NOT NULL ,\n" +
                    "  bis TEXT NOT NULL CHECK (julianday(bis) > julianday(von)), -- check ob Zeit sinnvoll\n" +
                    "  PRIMARY KEY (PersonenID, BehördenID, von, bis),\n" +
                    "  FOREIGN KEY (PersonenID) REFERENCES POLIZIST(PersonenID) ON UPDATE CASCADE ON DELETE RESTRICT,\n" +
                    "  FOREIGN KEY (BehördenID) REFERENCES BEHÖRDE(BehördenID) ON UPDATE CASCADE ON DELETE RESTRICT\n" +
                    ");");
            AnfrageObjekt.executeUpdate("CREATE TABLE ARBEITEN_AN(\n" +
                    "  PersonenID INTEGER NOT NULL ,\n" +
                    "  FallID INTEGER NOT NULL ,\n" +
                    "  von TEXT NOT NULL ,\n" +
                    "  bis TEXT CHECK (julianday(bis) > julianday(von)),\n" +
                    "  PRIMARY KEY (PersonenID, FallID),\n" +
                    "  FOREIGN KEY (PersonenID) REFERENCES POLIZIST(PersonenID) ON UPDATE CASCADE ON DELETE RESTRICT,\n" +
                    "  FOREIGN KEY (FallID) REFERENCES FALL(FallID)  ON UPDATE CASCADE ON DELETE RESTRICT\n" +
                    ");");
        } catch (SQLException e) {
            IEM.setErrorText("Konnte Datenbank nicht korregt löschen. Schließen sie das Programm und löschen sie manuell.", e);
        }
        IEM.setInfoText("Datenbank wurde neu erstellt");
    }

    public Statement getAnfrageObjekt() {
        return AnfrageObjekt;
    }

    public Connection getVerbindung() {
        return Verbindung;
    }

    public PreparedStatement prepareStatement(String SQLAnfrage) throws SQLException {
        return Verbindung.prepareStatement(SQLAnfrage);
    }


    /**
     * Schliest und oeffnet die Verbindung zur Datenbank wieder.
     */
    public void verbindeDatenbankNeu() {
        BeendeDatenbankAnbindung();
        VerbindeDatenbank();
    }

    /**
     * Zwingt die Datenbankanbindung beendet zu werden.
     */
    public void BeendeDatenbankAnbindung() {
        try {
            if (Verbindung != null) {
                Verbindung.close();
            }
            Verbindung = null;
            AnfrageObjekt = null;
        } catch (SQLException e) {
        }
    }
}
