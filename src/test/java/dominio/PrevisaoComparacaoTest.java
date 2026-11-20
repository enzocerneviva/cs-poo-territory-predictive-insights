package dominio;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrevisaoComparacaoTest {

    private List<Medicao> historico(double... valores) {
        List<Medicao> lista = new ArrayList<>();
        for (double v : valores) {
            lista.add(new Medicao(TipoSensor.CHUVA, v));
        }
        return lista;
    }

    @Test
    void tendenciaDeAltaPrevMaisQueAUltimaLeitura() {
        // Arrange
        PrevisaoComparacao previsao = new PrevisaoComparacao(3);

        // Act: com 3 leituras a janela efetiva é 1 -> recente 30, anterior 20
        double resultado = previsao.prever(historico(10, 20, 30));

        // Assert: 30 + (30 - 20) = 40
        assertEquals(40.0, resultado, 0.0001);
    }

    @Test
    void valoresEstaveisMantemOPatamar() {
        PrevisaoComparacao previsao = new PrevisaoComparacao(2);

        double resultado = previsao.prever(historico(10, 10, 10, 10));

        assertEquals(10.0, resultado, 0.0001);
    }

    @Test
    void tendenciaDeQuedaNaoPrevValorNegativo() {
        PrevisaoComparacao previsao = new PrevisaoComparacao(1);

        double resultado = previsao.prever(historico(30, 10));

        // 10 + (10 - 30) = -10 -> limitado ao mínimo da faixa do sensor (0)
        assertEquals(0.0, resultado, 0.0001);
    }

    @Test
    void previsaoNaoUltrapassaOMaximoDoSensor() {
        PrevisaoComparacao previsao = new PrevisaoComparacao(1);

        double resultado = previsao.prever(historico(120, 150));

        assertTrue(resultado <= TipoSensor.CHUVA.getMax());
    }
}
