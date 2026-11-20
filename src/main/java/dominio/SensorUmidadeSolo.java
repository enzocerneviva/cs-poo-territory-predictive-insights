package dominio;

import java.util.Random;

/** Mede a umidade do solo (%). Simula de 30 a 100%. */
public class SensorUmidadeSolo extends SensorSimulado {

    public SensorUmidadeSolo() {
        this(new Random());
    }

    public SensorUmidadeSolo(Random random) {
        super(TipoSensor.UMIDADE_SOLO, 30, 100, random);
    }
}
