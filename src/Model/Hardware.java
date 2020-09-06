package Model;
import com.fazecast.jSerialComm.*;
/**
 * Rozhraní pro komunikaci s hardwarem
**/
public class Hardware {
    private SerialPort serial;
    private MathFunction mathFunction;

    public Hardware(){
    }

    public void connect(){
        serial.openPort();
        MessageListener listener = new MessageListener();
        serial.addDataListener(listener);
    }

    public void disconect(){
        serial.removeDataListener();
        serial.closePort();
    }

    public void setHeaterPower(int power, int maxPower){
        double pow = power/maxPower;
        pow = pow * 255;
        byte p = (byte)pow;
        byte[] msg = {'s',p};
        serial.writeBytes(msg,2);
    }

    public SerialPort getSerial() {
        return serial;
    }

    public void setSerial(SerialPort serial) {
        this.serial = serial;
    }

    public MathFunction getMathFunction() {
        return mathFunction;
    }

    public void setMathFunction(MathFunction mathFunction) {
        this.mathFunction = mathFunction;
    }

    private final class MessageListener implements SerialPortMessageListener
    {
        @Override
        public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_RECEIVED; }

        @Override
        public byte[] getMessageDelimiter() { return new byte[] { '\n' }; }

        @Override
        public boolean delimiterIndicatesEndOfMessage() { return true; }

        @Override
        public void serialEvent(SerialPortEvent event)
        {
            byte[] delimitedMessage = event.getReceivedData();
            String csv = "";
            for (byte b:delimitedMessage) {
                csv += (char)b;
            }
            mathFunction.addCSV(csv);
        }
    }
}


