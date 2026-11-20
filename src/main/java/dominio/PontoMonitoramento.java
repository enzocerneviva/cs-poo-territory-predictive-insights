package dominio;

import excecao.DadosInvalidosException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Um ponto físico com sensores, por exemplo "Ponte do Rio" em Lajeado. */
public class PontoMonitoramento extends ComponenteTerritorial {

    private final List<Sensor> sensores = new ArrayList<>();

    public PontoMonitoramento(int id, String nome, String regiao) {
        super(id, nome, regiao);
    }

    public void adicionarSensor(Sensor sensor) {
        if (sensor == null) {
            throw new DadosInvalidosException("Sensor não informado.");
        }
        sensores.add(sensor);
    }

    public List<Sensor> getSensores() {
        return Collections.unmodifiableList(sensores);
    }

    /** INOPERANTE sem sensores ou com todos parados; DEGRADADO se só alguns falham. */
    @Override
    public StatusComponente verificarStatus() {
        long ativos = sensores.stream().filter(Sensor::estaFuncionando).count();
        StatusComponente novo;
        if (sensores.isEmpty() || ativos == 0) {
            novo = StatusComponente.INOPERANTE;
        } else if (ativos < sensores.size()) {
            novo = StatusComponente.DEGRADADO;
        } else {
            novo = StatusComponente.OPERANTE;
        }
        setStatus(novo);
        return novo;
    }
}
