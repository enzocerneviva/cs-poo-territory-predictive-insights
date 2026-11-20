package dominio;

import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PrevisaoMediaMovelTest {

    private List<Medicao> historico(double... valores) {
        List<Medicao> lista = new ArrayList<>();
        for (double v : valores) {
            lista.add(new Medicao(TipoSensor.CHUVA, v));
        }
        return lista;
    }

    @Test
    void mediaDasUltimasTresLeituras() {
        // Arrange
        PrevisaoMediaMovel previsao = new PrevisaoMediaMovel(3);

        // Act
        double resultado = previsao.prever(historico(5, 10, 20, 30));

        // Assert: média de 10, 20, 30
        assertEquals(20.0, resultado, 0.0001);
    }

    @Test
    void janelaMaiorQueOHistoricoUsaTudoQueExiste() {
        PrevisaoMediaMovel previsao = new PrevisaoMediaMovel(10);

        double resultado = previsao.prever(historico(10, 20));

        assertEquals(15.0, resultado, 0.0001);
    }

    @Test
    void historicoVazioLancaDadosInvalidosException() {
        PrevisaoMediaMovel previsao = new PrevisaoMediaMovel(3);

        assertThrows(DadosInvalidosException.class, () -> previsao.prever(new ArrayList<>()));
    }

    @Test
    void janelaInvalidaLancaDadosInvalidosException() {
        assertThrows(DadosInvalidosException.class, () -> new PrevisaoMediaMovel(0));
    }
}
