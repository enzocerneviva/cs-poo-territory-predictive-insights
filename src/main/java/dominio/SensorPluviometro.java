package dominio;

import java.util.Random;

/** Mede chuva (mm/h). Simula de 0 a 90 mm/h. */
public class SensorPluviometro extends SensorSimulado {

    public SensorPluviometro() {
        this(new Random());
    }

    public SensorPluviometro(Random random) {
        super(TipoSensor.CHUVA, 0, 90, random);
    }
}
