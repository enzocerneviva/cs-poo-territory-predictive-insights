package dominio;

import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MedicaoTest {

    @Test
    void medicaoValidaGuardaOsDados() {
        // Arrange
        LocalDateTime momento = LocalDateTime.of(2026, 11, 19, 22, 0);

        // Act
        Medicao medicao = new Medicao(TipoSensor.CHUVA, 42.5, momento);

        // Assert
        assertEquals(TipoSensor.CHUVA, medicao.getTipoSensor());
        assertEquals(42.5, medicao.getValor(), 0.0001);
        assertEquals(momento, medicao.getDataHora());
    }

    @Test
    void chuvaNegativaLancaDadosInvalidosException() {
        // Arrange + Act
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> new Medicao(TipoSensor.CHUVA, -5));

        // Assert
        assertTrue(erro.getMessage().contains("fora da faixa"));
    }

    @Test
    void valorAcimaDoMaximoDoSensorLancaDadosInvalidosException() {
        assertThrows(DadosInvalidosException.class, () -> new Medicao(TipoSensor.UMIDADE_SOLO, 101));
    }

    @Test
    void valorNaNLancaDadosInvalidosException() {
        assertThrows(DadosInvalidosException.class, () -> new Medicao(TipoSensor.NIVEL_RIO, Double.NaN));
    }
}
