package Controller;

import Model.Hardware;
import Model.MathFunction;
import Model.Physics;
import Model.TestProcedure;
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


    private ObservableList<SerialPort> ports;
    private Hardware hardware;

    @FXML
    void initialize(){

        ports = FXCollections.observableList(new ArrayList<SerialPort>());
        Arrays.stream(SerialPort.getCommPorts()).forEach(port -> ports.add(port));

        hardware = new Hardware();

        cmbSerialPort.setConverter(new StringConverter<SerialPort>() {
            @Override
            public String toString(SerialPort o) {
                if(o != null)
                return o.getDescriptivePortName();
                else return "";
            }

            @Override
            public SerialPort fromString(String s) {
                return null;
            }
        });

        cmbSerialPort.setItems(ports);

        canTemps.getGraphicsContext2D().setFill(Paint.valueOf("#AAAAAA"));
        canTemps.getGraphicsContext2D().fillRect(0,0,800,200);
        //canTemps.getGraphicsContext2D().strokeLine(10,0,20,0);

        TextField[] tempDisplays = {txfAmbientTemp,txfHeatblockTemp,txfHeatSink1Temp,txfHeatSink2Temp};
        MathFunctionPresenter mfp = new MathFunctionPresenter(canTemps.getGraphicsContext2D(),tempDisplays);
        MathFunction mf = new MathFunction();
        mfp.setFunction(mf);
        hardware.setMf(mf);
        txfZoom.setText(mfp.getTime() + " sec");


        Alert infobox = new Alert(Alert.AlertType.INFORMATION);
        TestProcedure test = new TestProcedure(mf,hardware, e -> {
            Platform.runLater(() -> {
                lblStatus.setText("Test dokončen Rth: " + String.format("%.4f",e.getResult()) + "K/W");
                btnStartTest.setDisable(false);
                infobox.setTitle("Měření dokončeno");
                infobox.setHeaderText("Měření dokončeno");
                infobox.setContentText("Naměřené Rth: " + String.format("%.4f",e.getResult()) + "K/W");
                infobox.show();
            });
        });

        //Event hadlers--------------------------------------------------------------------------------------
        txfHeatbreakCrossSection.addEventHandler(KeyEvent.KEY_RELEASED,
                e -> {
                    System.out.println(txfHeatbreakCrossSection.getText());
                }
        );

        cmbSerialPort.addEventHandler(MouseEvent.MOUSE_CLICKED,
                e -> {
                    ports.clear();
                    Arrays.stream(SerialPort.getCommPorts()).forEach(port -> ports.add(port));
                }
        );

        cmbSerialPort.addEventHandler(ActionEvent.ACTION,
                e -> {
                    hardware.setSerial((SerialPort) cmbSerialPort.getSelectionModel().getSelectedItem());
                    if(cmbSerialPort.getSelectionModel().getSelectedItem() != null) {
                        btnConnect.setDisable(false);
                        lblStatus.setText("Připojte se k zařízení");
                    }
                    else {
                        lblStatus.setText("Vyberte sériový port");
                        btnConnect.setDisable(true);
                    }
                }
        );

        btnZoomIn.setOnAction(e -> {
            //mfp.setTime(mfp.getTime()-1);
            mfp.zoomIn();
            txfZoom.setText(mfp.getTime() + " sec");
            mfp.update();
        });
        btnZoomOut.setOnAction(e -> {
            //mfp.setTime(mfp.getTime()+1);
            mfp.zoomOut();
            txfZoom.setText(mfp.getTime() + " sec");
            mfp.update();
        });
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

        EventHandler<KeyEvent> handler = e ->{
            lblThermalConductivity.setText(Physics.thermalConductivity(
                    Double.parseDouble(txfHeatbreakCrossSection.getText()),
                    Double.parseDouble(txfHeatbreakLength.getText()),
                    Double.parseDouble(txfHeatbrekThermalConductivity.getText())
            ) + "");
        };

        txfHeatbreakCrossSection.addEventHandler(KeyEvent.KEY_RELEASED,handler);
        txfHeatbreakLength.addEventHandler(KeyEvent.KEY_RELEASED,handler);
        txfHeatbrekThermalConductivity.addEventHandler(KeyEvent.KEY_RELEASED,handler);

        btnStartTest.setOnAction(e -> {
            lblStatus.setText("Test běží...");
            test.setParams(
                    Integer.parseInt(txfTestStartPower.getText()),
                    Integer.parseInt(txfTestEndPower.getText()),
                    Integer.parseInt(txfTestPowerStep.getText()),
                    Integer.parseInt(txfMaxPower.getText()),
                    Double.parseDouble(lblThermalConductivity.getText())
            );
            test.start();
        });

    }

    public void close(){

    }
}
