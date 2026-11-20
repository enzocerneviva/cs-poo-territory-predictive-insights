package dominio;

import excecao.DadosInvalidosException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DadosTerritorioTest {

    private DadosTerritorio dados;

    @BeforeEach
    void preparar() {
        dados = new DadosTerritorio(1000);
    }

    @Test
    void populacaoNegativaLancaDadosInvalidosException() {
        assertThrows(DadosInvalidosException.class, () -> dados.setPopulacaoExposta(-1));
        assertEquals(1000, dados.getPopulacaoExposta()); // valor antigo preservado
    }

    @Test
    void limiaresForaDeOrdemLancamDadosInvalidosException() {
        assertThrows(DadosInvalidosException.class,
                () -> dados.definirLimiares(TipoSensor.CHUVA, 50, 20, 80));
    }

    @Test
    void limiarCriticoAcimaDoMaximoDoSensorLancaDadosInvalidosException() {
        assertThrows(DadosInvalidosException.class,
                () -> dados.definirLimiares(TipoSensor.UMIDADE_SOLO, 50, 80, 150));
    }

    @Test
    void limiaresValidosSaoAplicados() {
        // Act
        dados.definirLimiares(TipoSensor.CHUVA, 10, 30, 60);

        // Assert
        assertEquals(30, dados.getLimiares(TipoSensor.CHUVA).alerta(), 0.0001);
    }
}
