package Model;
//ekvivlanet třídy Math, ale pro fyzikální výpočty
public class Physics {

    private Physics(){}; //žádné instance této třídy

    /**vrací tepelnou vodivost, očekává rozměry v milimetrech
    *(Ano, lehce nefyzikální, ale pro rozměry se kterými pracuji je to praktičtější)*/
    public static double thermalConductivity(double crossSecton,double length,double thermalConstant){
        return (thermalConstant*(crossSecton/1000000))/(length/1000);
    }

    /**Vrací tepelný výkon přecházející z jednoho tělesa do druhého
    *Očekává teploty ve stupních Celsia, nebo v Kelvinech (vyjde to stejně)*/
    public static double heatPower(double t1,double t2, double thermalConductivity){
        return (t1-t2)*thermalConductivity;
    }
}
