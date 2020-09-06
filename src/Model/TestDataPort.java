package Model;

import com.fazecast.jSerialComm.SerialPortMessageListener;

import java.util.Arrays;

/**
 * Zjednodušená simulace sériového portu. Implementuje jen funkcionalitu použitou v programu, určitě né celý port.
 * Nechce se po ném nic jiného, než aby vracel nějaká částečně smysluplná data, se kterými by alikace mohla pracovat.
 * Tak se i člověk bez reálného hardwaru může přesvédčit, že aplikace funguje. Test nereflektuje reálné průběhy teplot
 * a ani to není jeho cílem.
 **/
public class TestDataPort implements DataPort {
    private TestPortMessageListener listener;
    private double ambient = 30;                     //Taplota okolí
    private double[] maxTemperatures = {280,42,37};  //Teplota při 100% výkonu (Parametr maximální výkon heateru je zanedbán)
    private double[] tempRises = {5,0.4,0.2};        //Rychlost oteplování
    private double[] setTemperatures = {ambient,ambient,ambient};   //nastavené teploty
    private double[] actualTemperatures = {ambient,ambient,ambient};    //aktuální teploty
    private PortTask task;

    TestDataPort(){
        task = new PortTask();
    }

    @Override
    public void openPort() {
        task.start();
    }

    @Override
    public void closePort() {
        task.stop();
    }

    @Override
    public void addDataListener(SerialPortMessageListener s) {
        listener = (TestPortMessageListener)s; //tohle není hezké
    }

    @Override
    public void writeBytes(byte[] data, int len) {
        calculateTemps(data[1]);
    }

    @Override
    public String getName() {
        return "Virtual test device";
    }

    @Override
    public void removeDataListener() {
        listener = null;
    }

    private void calculateTemps(byte b){
        double power = (double)b/255.0;
        setTemperatures = Arrays.stream(maxTemperatures).map(d -> ((d-ambient)*power)+ambient).toArray();
    }

    private class PortTask extends Thread{
        @Override
        public void run() {
            while(true) {
                for (int i = 0; i < actualTemperatures.length; i++) {
                    if (actualTemperatures[i] > setTemperatures[i] + tempRises[i])
                        actualTemperatures[i] -= tempRises[i];
                    else if (actualTemperatures[i] < setTemperatures[i] - tempRises[i])
                        actualTemperatures[i] += tempRises[i];
                    else
                        actualTemperatures[i] = setTemperatures[i];
                }

                listener.testDataEvent(ambient + "|" + actualTemperatures[0] + "|" + actualTemperatures[1] + "|" + actualTemperatures[2]);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
