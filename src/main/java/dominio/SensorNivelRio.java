package dominio;

import java.util.Random;

/** Mede o nível do rio (cm). Simula de 100 a 650 cm. */
public class SensorNivelRio extends SensorSimulado {

    public SensorNivelRio() {
        this(new Random());
    }

    public SensorNivelRio(Random random) {
        super(TipoSensor.NIVEL_RIO, 100, 650, random);
    }
}
