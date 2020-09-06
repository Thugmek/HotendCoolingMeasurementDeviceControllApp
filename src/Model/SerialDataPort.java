package Model;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortMessageListener;

public class SerialDataPort implements DataPort {

    SerialPort port;

    public SerialDataPort(SerialPort port){
        this.port = port;
    }

    @Override
    public void openPort() {
        port.openPort();
    }

    @Override
    public void closePort() {
        port.closePort();
    }

    @Override
    public void addDataListener(SerialPortMessageListener s) {
        port.addDataListener(s);
    }

    @Override
    public void writeBytes(byte[] data, int len) {
        port.writeBytes(data,len);
    }

    @Override
    public String getName() {
        return port.getDescriptivePortName();
    }

    @Override
    public void removeDataListener() {
        port.removeDataListener();
    }
}
