package Model;
/**
 * Ekvivalent SerialPortMessageListener. Chtěl jsem původně vyvolávat falešné SerialEventy, ale ty musí být vázané na
 * SerialPort, který nesmí být null, a ten neseženu, když žádný není připojený...past vedle pasti...
 * */
public interface TestPortMessageListener {
    void testDataEvent(String csv);
}
