package Controller;

import Model.*;
import View.MathFunctionPresenter;
import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.util.StringConverter;

import java.util.*;


public class Controller {
    @FXML
    public TextField txfHeatbreakCrossSection;
    @FXML
    public TextField txfHeatbreakLength;
    @FXML
    TextField txfHeatbrekThermalConductivity;
    @FXML
    ComboBox cmbSerialPort;
    @FXML
    Canvas canTemps;
    @FXML
    Button btnStartTest;
    @FXML
    Button btnZoomOut;
    @FXML
    Button btnZoomIn;
    @FXML
    TextField txfZoom;
    @FXML
    TextField txfAmbientTemp;
    @FXML
    TextField txfHeatblockTemp;
    @FXML
    TextField txfHeatSink1Temp;
    @FXML
    TextField txfHeatSink2Temp;
    @FXML
    Button btnConnect;
    @FXML
    Button btnDisconnect;
    @FXML
    Label lblThermalConductivity;
    @FXML
    TextField txfTestStartPower;
    @FXML
    TextField txfTestEndPower;
    @FXML
    TextField txfTestPowerStep;
    @FXML
    TextField txfMaxPower;
    @FXML
    Label lblStatus;

    private ObservableList<DataPort> ports;

    @FXML
    void initialize(){
        //načtení dostupných sériových portú
        ports = FXCollections.observableList(new ArrayList<DataPort>());
        Arrays.stream(DataPort.getPorts()).forEach(port -> ports.add(port));

        //Logika převodu objektů SerialPort na String
        cmbSerialPort.setConverter(new StringConverter<DataPort>() {
            @Override
            public String toString(DataPort o) {
                if(o != null)
                    return o.getName();
                else return "";
            }

            @Override
            public DataPort fromString(String s) {
                return null;
            }
        });

        cmbSerialPort.setItems(ports);

        //canvas do kterého se bude vykreslovat graf teplot
        canTemps.getGraphicsContext2D().setFill(Paint.valueOf("#AAAAAA"));
        canTemps.getGraphicsContext2D().fillRect(0,0,800,200);

        //pole k zobrazení teplot
        TextField[] tempDisplays = {txfAmbientTemp,txfHeatblockTemp,txfHeatSink1Temp,txfHeatSink2Temp};
        //zobrazovací logika matematické funkce
        MathFunctionPresenter mfp = new MathFunctionPresenter(canTemps.getGraphicsContext2D(),tempDisplays);
        //matematická funkce
        MathFunction mf = new MathFunction();
        mfp.setFunction(mf);
        txfZoom.setText(mfp.getTimeFrame() + " sec");

        //instanciance rozhraní hardwarového přípravku
        Hardware hardware = new Hardware();
        hardware.setMathFunction(mf);


        //instance testovací procedury
        Alert infobox = new Alert(Alert.AlertType.INFORMATION);
        TestProcedure test = new TestProcedure(mf,hardware, e -> {
            Platform.runLater(() -> {
                lblStatus.setText("Test dokončen. Rth: " + String.format("%.4f",e.getResult()) + "K/W");
                btnStartTest.setDisable(false);
                infobox.setTitle("Měření dokončeno");
                infobox.setHeaderText("Měření dokončeno");
                infobox.setContentText("Naměřené Rth: " + String.format("%.4f",e.getResult()) + "K/W");
                infobox.show();
            });
        });

        //Event hadlers--------------------------------------------------------------------------------------
        //Aktualizace sériových portů po kliknutí na combobox
        cmbSerialPort.addEventHandler(MouseEvent.MOUSE_CLICKED,
                e -> {
                    ports.clear();
                    Arrays.stream(DataPort.getPorts()).forEach(port -> ports.add(port));
                }
        );
        //po vybrání sériového portu z comboboxu...
        cmbSerialPort.addEventHandler(ActionEvent.ACTION,
                e -> {
                    hardware.setSerial((DataPort) cmbSerialPort.getSelectionModel().getSelectedItem());
                    if(cmbSerialPort.getSelectionModel().getSelectedItem() != null) {
                        btnConnect.setDisable(false);
                        btnZoomIn.setDisable(false);
                        btnZoomOut.setDisable(false);
                        lblStatus.setText("Připojte se k zařízení");
                    }
                    else {
                        lblStatus.setText("Vyberte sériový port");
                        btnConnect.setDisable(true);
                        btnZoomIn.setDisable(true);
                        btnZoomOut.setDisable(true);
                    }
                }
        );
        //přiblížení/oddálení grafu
        btnZoomIn.setOnAction(e -> {
            //mfp.setTime(mfp.getTime()-1);
            mfp.zoomIn();
            txfZoom.setText(mfp.getTimeFrame() + " sec");
            mfp.update();
        });
        btnZoomOut.setOnAction(e -> {
            //mfp.setTime(mfp.getTime()+1);
            mfp.zoomOut();
            txfZoom.setText(mfp.getTimeFrame() + " sec");
            mfp.update();
        });
        //připojení k/odpojení od zařízení
        btnConnect.setOnAction(e -> {
            hardware.connect();
            btnConnect.setDisable(true);
            btnDisconnect.setDisable(false);
            btnStartTest.setDisable(false);
            lblStatus.setText("Zařízení připojeno");
        });
        btnDisconnect.setOnAction(e -> {
            hardware.disconect();
            btnConnect.setDisable(false);
            btnDisconnect.setDisable(true);
            btnStartTest.setDisable(true);
            lblStatus.setText("Připojte se k zařízení");
        });

        //reakce na změnu polí v sekci "heatbreak"
        EventHandler<KeyEvent> handler = e ->{
            if(validateNumberInput(((TextField)e.getSource()).getText())){
                ((TextField)e.getSource()).setStyle("");

                lblThermalConductivity.setText(String.format(
                        "%.4f",
                        Physics.thermalConductivity(
                                Double.parseDouble(txfHeatbreakCrossSection.getText()),
                                Double.parseDouble(txfHeatbreakLength.getText()),
                                Double.parseDouble(txfHeatbrekThermalConductivity.getText())
                        )));
            }else{
                ((TextField)e.getSource()).setStyle("-fx-background-color:red;");
                lblThermalConductivity.setText("NaN");
            }
        };
        txfHeatbreakCrossSection.addEventHandler(KeyEvent.KEY_RELEASED,handler);
        txfHeatbreakLength.addEventHandler(KeyEvent.KEY_RELEASED,handler);
        txfHeatbrekThermalConductivity.addEventHandler(KeyEvent.KEY_RELEASED,handler);

        //reakce na změnu ostatních polí"
        EventHandler<KeyEvent> validate = e ->{
            if(validateNumberInput(((TextField)e.getSource()).getText()))
                ((TextField)e.getSource()).setStyle("");
            else
                ((TextField)e.getSource()).setStyle("-fx-background-color:red;");
        };
        txfMaxPower.addEventHandler(KeyEvent.KEY_RELEASED,validate);
        txfTestPowerStep.addEventHandler(KeyEvent.KEY_RELEASED,validate);
        txfTestEndPower.addEventHandler(KeyEvent.KEY_RELEASED,validate);
        txfTestStartPower.addEventHandler(KeyEvent.KEY_RELEASED,validate);

        //po kliknutí na tlačítko start test
        btnStartTest.setOnAction(e -> {
            boolean dataOk = true;
            dataOk = dataOk && validateNumberInput(txfTestStartPower.getText());
            dataOk = dataOk && validateNumberInput(txfTestEndPower.getText());
            dataOk = dataOk && validateNumberInput(txfTestPowerStep.getText());
            dataOk = dataOk && validateNumberInput(txfMaxPower.getText());
            dataOk = dataOk && validateNumberInput(txfHeatbreakCrossSection.getText());
            dataOk = dataOk && validateNumberInput(txfHeatbreakLength.getText());
            dataOk = dataOk && validateNumberInput(txfHeatbrekThermalConductivity.getText());
            if(dataOk) {
                lblStatus.setText("Test běží...");
                test.setParams(
                        Integer.parseInt(txfTestStartPower.getText()),
                        Integer.parseInt(txfTestEndPower.getText()),
                        Integer.parseInt(txfTestPowerStep.getText()),
                        Integer.parseInt(txfMaxPower.getText()),
                        Physics.thermalConductivity(
                                Double.parseDouble(txfHeatbreakCrossSection.getText()),
                                Double.parseDouble(txfHeatbreakLength.getText()),
                                Double.parseDouble(txfHeatbrekThermalConductivity.getText())
                        )
                );
                test.start();
            }else{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Chyba");
                alert.setHeaderText("Neplatný vstup");
                alert.setContentText("Zadejte platné vstupní hodnoty");
                alert.show();

            }
        });

    }

    private boolean validateNumberInput(String s){
        return s.matches("^\\d*\\.{0,1}\\d*$");
    }
}
