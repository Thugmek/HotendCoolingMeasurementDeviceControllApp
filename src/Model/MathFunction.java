package Model;

import java.util.ArrayList;
import java.util.function.Consumer;

public class MathFunction {
    private ArrayList<double[]> values;
    private ArrayList<Consumer<MathFunction>> consumers;

    public MathFunction(){
        values = new ArrayList<double[]>();
        consumers = new ArrayList<>();
    }

    //Přidá do funkce hodnoty z CSV
    public void addCSV(String csv){
        String str[] = csv.split("\\|");
        double[] data = new double[str.length];
        for (int i = 0;i<str.length;i++) {
            data[i] = Double.parseDouble(str[i]);
        }
        values.add(data);
        callUpdate();
    }

    public void add(double[] data){
        values.add(data);
        callUpdate();
    }

    private void callUpdate(){
        for (Consumer<MathFunction> c: consumers) {
            c.accept(this);
        }
    }

    public ArrayList<Consumer<MathFunction>> getConsumers(){
        return consumers;
    }

    public ArrayList<double[]> getValues() {
        return values;
    }
}

