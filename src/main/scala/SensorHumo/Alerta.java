package SensorHumo;

import java.io.Serializable;

public class Alerta implements Serializable {

    public boolean emergencia;

    public Alerta() {};

    public void setEmergencia(boolean emergencia) {this.emergencia = emergencia;}

    public boolean getEmergencia() { return this.emergencia;}
}
