package Model;

public enum ProcedureState {
    WAITING1, //V první fázi se trochu počká, termistor má trochu spoždění, algoritmus by jej mohl vyhodnotit jako ustálení teploty
    HEATING,  //V druhé fázi je aktivně testováno, zda se teplota chladiče již neustálila
    WAITING2, //Opět je trochu počkáno, aby se teplota opravdu ustálila. Dojde k výpočtu tepelného odporu pro tento výkon a proces se opakuje
    HALT
}
