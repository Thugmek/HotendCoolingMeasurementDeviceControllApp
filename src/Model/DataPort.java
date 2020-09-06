package Model;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortMessageListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

//Tohle v podstatě vzniklo jen kvůli testovacím datům
public interface DataPort {
    void openPort();
    void closePort();
    void addDataListener(SerialPortMessageListener s);
    void writeBytes(byte data[], int len);
    String getName();
    void removeDataListener();

    static DataPort[] getPorts(){
        ArrayList<DataPort> ports = new ArrayList<>();
        Stream.concat(
                Arrays.stream(SerialPort.getCommPorts()).map(port -> new SerialDataPort(port)),
                Stream.of(new TestDataPort())
        ).forEach(port -> ports.add((DataPort) port));
        DataPort[] resAr = new DataPort[ports.size()];
        return ports.toArray(resAr);
    }
}
