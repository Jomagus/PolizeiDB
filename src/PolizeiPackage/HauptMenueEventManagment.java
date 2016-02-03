package PolizeiPackage;

import PolizeiPackage.Ansichten.PolizistAnsichtManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Verwaltet Hauptmenueevents
 */
public class HauptMenueEventManagment {
    private Stage PrimaereStage;
    private PolizistAnsichtManager PolizistAM;
    private DatenbankHandler DH;
    private InfoErrorManager IM;

    /**
     * Initialisiert einen EventManager fuers Hauptmenue
     *
     * @param VaterStage Die Stage mit dem Hauptmenue
     */
    public HauptMenueEventManagment(Stage VaterStage, DatenbankHandler DBH, InfoErrorManager IEM, PolizistAnsichtManager PAM) {
        this.PrimaereStage = VaterStage;
        DH = DBH;
        PolizistAM = PAM;
        IM = IEM;
    }

    public void FilterSubMenue() {

    }

    public void SuchenPolizisten() {
        Stage PopUp = new Stage();
        PopUp.initModality(Modality.APPLICATION_MODAL);
        PopUp.setTitle("Suche Polizisten");
        PopUp.setAlwaysOnTop(true);
        PopUp.setResizable(false);

        GridPane Gitter = new GridPane();
        Gitter.setHgap(10);
        Gitter.setVgap(10);

        Label LabelTop = new Label("Freigelassene Felder gelten als Wildcard (%)");

        Label LabelA = new Label("PersonenID");
        TextField LabelAWert = new TextField();

        Label LabelB = new Label("Name");
        TextField LabelBWert = new TextField();

        Label LabelC = new Label("Geburtsdatum");
        DatePicker LabelCWert = new DatePicker();

        Label LabelD = new Label("Nationalität");
        TextField LabelDWert = new TextField();

        Label LabelE = new Label("Geschlecht");
        ComboBox LabelEWert = new ComboBox();

        Label LabelF = new Label("Todesdatum");
        DatePicker LabelFWert = new DatePicker();

        Label LabelG = new Label("Dienstgrad");
        TextField LabelGWert = new TextField();

        final Callback<DatePicker, DateCell> TagesZellenFabtrik = new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(final DatePicker DP) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item.isBefore(LabelCWert.getValue().plusDays(1))) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }
                    }
                };
            }
        };
        LabelFWert.setDayCellFactory(TagesZellenFabtrik);

        LabelEWert.getItems().addAll("","m", "w");
        LabelEWert.setValue("");

        Button ButtonFort = new Button("Fortfahren");
        Button ButtonAbb = new Button("Abbrechen");

        ButtonFort.defaultButtonProperty();
        ButtonAbb.cancelButtonProperty();

        ButtonFort.setMaxWidth(Double.MAX_VALUE);
        ButtonAbb.setMaxWidth(Double.MAX_VALUE);

        Gitter.addColumn(0, LabelA, LabelB, LabelC, LabelD, LabelE, LabelF, LabelG);
        Gitter.addColumn(1, LabelAWert, LabelBWert, LabelCWert, LabelDWert, LabelEWert, LabelFWert, LabelGWert);

        VBox AussenBox = new VBox(10);
        HBox InnenBox = new HBox();

        AussenBox.setSpacing(10);
        AussenBox.setPadding(new Insets(10));
        InnenBox.setSpacing(10);

        AussenBox.setAlignment(Pos.CENTER);
        InnenBox.setAlignment(Pos.BOTTOM_CENTER);

        AussenBox.getChildren().addAll(LabelTop, Gitter, InnenBox);
        InnenBox.getChildren().addAll(ButtonFort, ButtonAbb);

        ButtonAbb.setOnAction(event -> PopUp.close());
        ButtonFort.setOnAction(event -> {
            String SQLString;
            ResultSet Results = null;

            String PersonenID = LabelAWert.getText().isEmpty() ? "%" : LabelAWert.getText();
            String Name = LabelBWert.getText().isEmpty() ? "%" : LabelBWert.getText();
            String Geburtsdatum = LabelCWert.getValue() == null ? "%" : LabelCWert.getValue().toString();
            String Nationalitaet  = LabelDWert.getText().isEmpty() ? "%" : LabelDWert.getText();
            String Geschlecht = LabelEWert.getValue().toString().isEmpty() ? "%" : LabelEWert.getValue().toString();
            String Dienstgrad = LabelGWert.getText().isEmpty() ? "%" : LabelGWert.getText();
            if (LabelFWert.getValue() != null) {
                SQLString = "SELECT PERSON.PersonenID, PERSON.Name, PERSON.Geburtsdatum, PERSON.Nationalität, PERSON.Geschlecht, PERSON.Todesdatum, POLIZIST.Dienstgrad " +
                        "FROM PERSON, POLIZIST WHERE PERSON.PersonenID = POLIZIST.PersonenID " +
                        "AND PERSON.PersonenID LIKE ? AND PERSON.Name LIKE ? AND PERSON.Geburtsdatum LIKE ? " +
                        "AND PERSON.Nationalität LIKE ? AND PERSON.Geschlecht LIKE ? AND POLIZIST.Dienstgrad LIKE ? AND PERSON.Todesdatum LIKE ?";
            } else {
                SQLString = "SELECT PERSON.PersonenID, PERSON.Name, PERSON.Geburtsdatum, PERSON.Nationalität, PERSON.Geschlecht, PERSON.Todesdatum, POLIZIST.Dienstgrad " +
                        "FROM PERSON, POLIZIST WHERE PERSON.PersonenID = POLIZIST.PersonenID AND " +
                        "PERSON.PersonenID LIKE ? AND PERSON.Name LIKE ? AND PERSON.Geburtsdatum LIKE ? AND " +
                        "PERSON.Nationalität LIKE ? AND PERSON.Geschlecht LIKE ? AND POLIZIST.Dienstgrad LIKE ?";
            }

            try {
                PreparedStatement InsertStatement = DH.prepareStatement(SQLString);
                InsertStatement.setString(1, PersonenID);
                InsertStatement.setString(2, Name);
                InsertStatement.setString(3, Geburtsdatum);
                InsertStatement.setString(4, Nationalitaet);
                InsertStatement.setString(5, Geschlecht);
                InsertStatement.setString(6, Dienstgrad);
                if (LabelFWert.getValue() != null) {
                    InsertStatement.setString(7, LabelFWert.getValue().toString());
                }
                Results = InsertStatement.executeQuery();
                IM.setInfoText("Suchanfrage durchgeführt");
            } catch (SQLException e) {
                IM.setErrorText("Suchanfrage Fehlgeschlagen", e);
            }
            PolizistAM.ZeigeSuchResultate(Results);
            PopUp.close();
        });

        PopUp.setScene(new Scene(AussenBox));
        PopUp.showAndWait();
    }

    public void SuchenOpfer() {

    }

    public void SuchenVerdaechtige() {

    }
}
