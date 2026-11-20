package servico;

import dominio.Alerta;
import dominio.DadosTerritorio;
import dominio.Medicao;
import dominio.NivelAlerta;
import dominio.Sensor;
import dominio.TipoSensor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import persistencia.AlertaDAO;
import persistencia.MedicaoDAO;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Testa a lógica de alerta sem banco: os DAOs são mocks (Mockito). */
@ExtendWith(MockitoExtension.class)
class ServicoMonitoramentoTest {

    @Mock
    private MedicaoDAO medicaoDAO;
    @Mock
    private AlertaDAO alertaDAO;

    private ServicoMonitoramento servico;

    @BeforeEach
    void preparar() {
        // O Singleton é compartilhado entre testes: volta ao padrão antes de cada um.
        ConfiguracaoSistema configuracao = ConfiguracaoSistema.getInstance();
        configuracao.restaurarPadrao();
        servico = new ServicoMonitoramento(medicaoDAO, alertaDAO, new DadosTerritorio(1000), configuracao);
    }

    private List<Medicao> historico(TipoSensor tipo, double... valores) {
        List<Medicao> lista = new ArrayList<>();
        for (double v : valores) {
            lista.add(new Medicao(tipo, v));
        }
        return lista;
    }

    @Test
    void poucasLeiturasNaoGeramAlerta() throws Exception {
        // Arrange: só 3 leituras, mesmo altas, mínimo é 5
        when(medicaoDAO.buscarUltimas(eq(TipoSensor.CHUVA), anyInt()))
                .thenReturn(historico(TipoSensor.CHUVA, 90, 95, 100));

        // Act
        Alerta alerta = servico.avaliarEAlertar(TipoSensor.CHUVA);

        // Assert
        assertNull(alerta);
        verify(alertaDAO, never()).salvar(any());
    }

    @Test
    void valorAtualAcimaDoLimiteCriticoEmiteAlertaCritico() throws Exception {
        // Arrange: chuva constante em 85 mm/h (crítico = 80)
        when(medicaoDAO.buscarUltimas(eq(TipoSensor.CHUVA), anyInt()))
                .thenReturn(historico(TipoSensor.CHUVA, 85, 85, 85, 85, 85));

        // Act
        Alerta alerta = servico.avaliarEAlertar(TipoSensor.CHUVA);

        // Assert
        assertNotNull(alerta);
        assertEquals(NivelAlerta.CRITICO, alerta.getNivel());
        assertFalse(alerta.isPrevisto());
        verify(alertaDAO).salvar(alerta);
    }

    @Test
    void tendenciaDeSubidaEmiteAlertaPorPrevisao() throws Exception {
        // Arrange: nível do rio subindo; o atual (440) é só ATENÇÃO, mas a tendência passa de 450
        when(medicaoDAO.buscarUltimas(eq(TipoSensor.NIVEL_RIO), anyInt()))
                .thenReturn(historico(TipoSensor.NIVEL_RIO, 300, 340, 380, 420, 440));

        // Act
        Alerta alerta = servico.avaliarEAlertar(TipoSensor.NIVEL_RIO);

        // Assert
        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaDAO).salvar(captor.capture());
        Alerta gravado = captor.getValue();
        assertEquals(NivelAlerta.ALERTA, gravado.getNivel());
        assertTrue(gravado.isPrevisto());
        assertEquals(450.0, gravado.getLimiteEstourado(), 0.0001);
        assertEquals(alerta, gravado);
    }

    @Test
    void valoresBaixosNaoEmitemAlerta() throws Exception {
        when(medicaoDAO.buscarUltimas(eq(TipoSensor.CHUVA), anyInt()))
                .thenReturn(historico(TipoSensor.CHUVA, 5, 5, 5, 5, 5));

        Alerta alerta = servico.avaliarEAlertar(TipoSensor.CHUVA);

        assertNull(alerta);
        verify(alertaDAO, never()).salvar(any());
    }

    @Test
    void lerEGravarSalvaAMedicaoDoSensor() throws Exception {
        // Arrange
        Sensor sensor = new Sensor() {
            public double lerValor() { return 12.5; }
            public boolean estaFuncionando() { return true; }
            public TipoSensor getTipo() { return TipoSensor.CHUVA; }
        };

        // Act
        Medicao medicao = servico.lerEGravar(sensor);

        // Assert
        ArgumentCaptor<Medicao> captor = ArgumentCaptor.forClass(Medicao.class);
        verify(medicaoDAO).salvar(captor.capture());
        assertEquals(12.5, captor.getValue().getValor(), 0.0001);
        assertEquals(medicao, captor.getValue());
    }
}
