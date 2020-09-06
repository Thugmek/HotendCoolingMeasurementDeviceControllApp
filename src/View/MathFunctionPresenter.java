package View;

import Model.MathFunction;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.paint.Paint;


public class MathFunctionPresenter {
    MathFunction function;
    GraphicsContext graph;
    TextField[] displays;
    int timeFrame = 50; //Délka vykreslného úseku v sekundách
    int timeindex = 6;  //Délka vykresleného úseku v krocích
    private int[] niceValues = {1,2,5}; //Násobky těchot hodnot budou pěkné
    double maxTemp = 20;
    int width = 400; //tohle by se nemělo být zadané natvrdo, ale...
    int height = 200;

    Paint[] paints = {Paint.valueOf("blue"),Paint.valueOf("red"),Paint.valueOf("green"),Paint.valueOf("magenta")};

    public MathFunctionPresenter(GraphicsContext graph, TextField[] displays) {
        this.graph = graph;
        this.displays = displays;
    }

    public void update(){
        double maxT = 0;

        int dataCount = function.getValues().size();
        double tempZoom = height/ maxTemp;

        //clear
        graph.setFill(Paint.valueOf("#AAAAAA"));
        graph.fillRect(0,0,width,height);

        int from = dataCount-1;
        int to = Math.max(dataCount- timeFrame,0);
        for(int i = from;i>to;i--){
            double[] data2 = function.getValues().get(i);
            double[] data1 = function.getValues().get(i-1);
            for(int j = 0;j<data1.length;j++){
                graph.setStroke(paints[j%4]);
                graph.strokeLine(
                        map(i,dataCount- timeFrame,from,0,width), //x1 - horizontální osa
                        height-data2[j]*tempZoom,//y1 - vertikální osa
                        map(i-1,dataCount- timeFrame,from,0,width), //x2
                        height-data1[j]*tempZoom //y2
                );
                maxT = Math.max(maxT,data2[j]);
            }
        }

        maxTemp = maxT + 10;

        double[] data = function.getValues().get(dataCount-1);
        for(int i = 0;i<data.length;i++){
            //displays[i].setText(data[i] + " °C");
            displays[i].setText(String.format("%.2f °C",data[i]));
        }

    }

    //Přemapuje hodnotu z jednoho rozsau do jiného rozsahu
    private double map(double n, double fromMin,double fromMax,double toMin, double toMax){
        return (((n-fromMin)/(fromMax-fromMin))*(toMax-toMin))+toMin;
    }

    public MathFunction getFunction() {
        return function;
    }

    public void setFunction(MathFunction function) {
        this.function = function;
        function.getConsumers().add(e -> update());
    }

    public int getTimeFrame() {
        return timeFrame;
    }

    public void zoomIn(){
        timeindex--;
        updateTime();
    }
    public void zoomOut(){
        timeindex++;
        updateTime();
    }
    private void updateTime(){
        timeFrame = niceValues[timeindex%3] * (int)Math.pow(10,timeindex/3);
    }
}
