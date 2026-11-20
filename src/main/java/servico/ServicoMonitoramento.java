package servico;

import dominio.Alerta;
import dominio.DadosTerritorio;
import dominio.EstrategiaPrevisao;
import dominio.Limiares;
import dominio.Medicao;
import dominio.NivelAlerta;
import dominio.Sensor;
import dominio.TipoSensor;
import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;
import persistencia.AlertaDAO;
import persistencia.MedicaoDAO;

import java.util.List;

/**
 * Regras de negócio: ler sensores, prever e emitir alertas.
 * Recebe os DAOs pelo construtor, então nos testes dá para trocá-los por mocks.
 */
public class ServicoMonitoramento {

    private final MedicaoDAO medicaoDAO;
    private final AlertaDAO alertaDAO;
    private final DadosTerritorio dados;
    private final ConfiguracaoSistema configuracao;

    public ServicoMonitoramento(MedicaoDAO medicaoDAO, AlertaDAO alertaDAO,
                                DadosTerritorio dados, ConfiguracaoSistema configuracao) {
        if (medicaoDAO == null || alertaDAO == null || dados == null || configuracao == null) {
            throw new DadosInvalidosException("ServicoMonitoramento criado com dependência nula.");
        }
        this.medicaoDAO = medicaoDAO;
        this.alertaDAO = alertaDAO;
        this.dados = dados;
        this.configuracao = configuracao;
    }

    /** Lê o sensor, valida (Medicao) e grava a medição no banco. */
    public Medicao lerEGravar(Sensor sensor) throws FonteIndisponivelException {
        double valor = sensor.lerValor();
        Medicao medicao = new Medicao(sensor.getTipo(), valor);
        medicaoDAO.salvar(medicao);
        return medicao;
    }

    /**
     * Prevê o próximo valor do sensor com a estratégia dada e classifica o
     * resultado. Não grava nada. Com menos leituras que o mínimo configurado,
     * o sistema se cala (resultado insuficiente).
     */
    public ResultadoPrevisao prever(TipoSensor tipo, EstrategiaPrevisao estrategia) throws FonteIndisponivelException {
        int minimo = configuracao.getMinimoLeituras();
        int quantidade = Math.max(minimo, configuracao.getJanela() * 2);
        List<Medicao> historico = medicaoDAO.buscarUltimas(tipo, quantidade);

        if (historico.size() < minimo) {
            return ResultadoPrevisao.insuficiente(tipo, estrategia.getNome(), estrategia.getDescricao(),
                    historico.size(), minimo);
        }

        double atual = historico.get(historico.size() - 1).getValor();
        double previsto = estrategia.prever(historico);

        Limiares limiares = dados.getLimiares(tipo);
        NivelAlerta nivelAtual = limiares.classificar(atual);
        NivelAlerta nivelPrevisto = limiares.classificar(previsto);
        NivelAlerta nivel = NivelAlerta.maior(nivelAtual, nivelPrevisto);

        boolean porPrevisao = nivel != null && nivel != nivelAtual;
        double limite = nivel == null ? Double.NaN : limiares.limiteDe(nivel);

        return new ResultadoPrevisao(tipo, estrategia.getNome(), estrategia.getDescricao(),
                historico.size(), minimo, atual, previsto, nivel, limite, porPrevisao);
    }

    /** Emite e grava o alerta de um resultado. Devolve null se não há o que alertar. */
    public Alerta emitirAlerta(ResultadoPrevisao resultado) throws FonteIndisponivelException {
        if (!resultado.suficiente() || !resultado.temAlerta()) {
            return null;
        }
        Alerta alerta = new Alerta(resultado.nivel(), resultado.tipo(), resultado.valorReferencia(),
                resultado.limite(), resultado.porPrevisao());
        alertaDAO.salvar(alerta);
        return alerta;
    }

    /** Previsão com a estratégia ativa da configuração + emissão do alerta. */
    public Alerta avaliarEAlertar(TipoSensor tipo) throws FonteIndisponivelException {
        ResultadoPrevisao resultado = prever(tipo, FabricaPrevisao.criar(configuracao));
        return emitirAlerta(resultado);
    }

    public List<Medicao> historico(TipoSensor tipo) throws FonteIndisponivelException {
        return medicaoDAO.buscarPorSensor(tipo);
    }

    public List<Medicao> ultimasMedicoes(TipoSensor tipo, int quantidade) throws FonteIndisponivelException {
        return medicaoDAO.buscarUltimas(tipo, quantidade);
    }

    public List<Alerta> alertasEmitidos() throws FonteIndisponivelException {
        return alertaDAO.listarTodos();
    }
}
