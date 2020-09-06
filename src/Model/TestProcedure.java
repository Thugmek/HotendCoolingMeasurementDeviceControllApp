package Model;

import com.fazecast.jSerialComm.SerialPort;

import java.util.ArrayList;
import java.util.function.Consumer;

public class TestProcedure {

    MathFunction mathFunction;
    Hardware hw;
    int startPower;
    int endPower;
    int powerStep;
    int maxPower;
    double hbThermCond;
    Consumer<TestProcedure> callback;
    ProcedureState state;
    private int couter;
    private int power;
    private ArrayList<Double> results;

    public TestProcedure(MathFunction mathFunction, Hardware hw, Consumer<TestProcedure> callback) {
        this.mathFunction = mathFunction;
        this.hw = hw;
        this.callback = callback;
        state = ProcedureState.HALT;
        mathFunction.getConsumers().add(e -> {
            calculate();
        });
    }

    public void setParams(int startPower, int endPower, int powerStep, int maxPower, double hbThermCond){
        this.startPower = startPower;
        this.endPower = endPower;
        this.powerStep = powerStep;
        power = startPower;
        this.maxPower = maxPower;
        this.hbThermCond = hbThermCond;
    }

    public void start(){
        results = new ArrayList<>();
        power = startPower;
        couter = 3; //10
        state = ProcedureState.WAITING1;
    }

    //Tohle se zavolá pokaždé, když přijdou data z přípravku.
    private void calculate(){
        switch (state){
            //V první fázi se trochu počká, termistor má trochu spoždění, algoritmus by jej mohl vyhodnotit jako ustálení teploty
            case WAITING1:
                couter--;
                if(couter < 1) state = ProcedureState.HEATING;
                break;
            //V druhé fázi je aktivně testováno, zda se teplota chladiče již neustálila
            case HEATING:
                if(range(10) < 1){
                    couter = 3; //30
                    state = ProcedureState.WAITING2;
                }
                break;
            //Opět je trochu počkáno, aby se teplota opravdu ustálila. Dojde k výpočtu tepelného odporu pro tento výkon a proces se opakuje
            case WAITING2:
                couter--;
                if(couter < 1){
                    results.add(calculateThermalResistance());
                    power += powerStep;
                    if(power <= endPower){
                        hw.setHeaterPower(power,maxPower);
                        state = ProcedureState.WAITING1;
                    }else {
                        hw.setHeaterPower(0,maxPower);
                        state = ProcedureState.HALT;
                        System.out.println("Callback");
                        callback.accept(this);
                    }
                }
                break;
            //test doběhl do konce, zastaveno
            case HALT:
                break;
        }
    }

    private double calculateThermalResistance(){
        double temps[] = mathFunction.getValues().get(mathFunction.getValues().size()-1);
        double ambient = temps[0];
        double heatblockTemp = temps[1];
        double heatsinkTemp = temps[2];
        double thermalPower = Physics.heatPower(heatblockTemp,heatsinkTemp,hbThermCond);
        double thermalDifference = heatsinkTemp-ambient;
        return thermalPower/thermalDifference;
    }

    public double getResult(){
        double res = 0;
        for (Double d:results) {
            res += d.doubleValue();
        }
        res /= results.size();
        return res;
    }

    //Vrátí rozpětí posledních hodnot. Slouźí k určení, zda se teplota již ustálila.
    private double range(int n){
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        for(int i = 0;i<n;i++){
            //pokud se ještě některá z hodnot neustálila, neustálil se ani jejich součet.
            double val = mathFunction.getValues().get(mathFunction.getValues().size()-1)[1];
            val += mathFunction.getValues().get(mathFunction.getValues().size()-1)[2];
            max = Math.max(max,val);
            min = Math.min(min,val);
        }

        return max-min;
    }
}
