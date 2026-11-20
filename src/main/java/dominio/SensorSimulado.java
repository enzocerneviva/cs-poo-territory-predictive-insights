package dominio;

import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;

import java.util.Random;

/**
 * Base dos sensores simulados: gera valores aleatórios dentro de uma faixa
 * realista (subconjunto da faixa física do tipo). Cada sensor concreto só
 * define seu tipo e sua faixa.
 */
public abstract class SensorSimulado implements Sensor {

    private final TipoSensor tipo;
    private final double minSimulado;
    private final double maxSimulado;
    private final Random random;
    private boolean funcionando = true;

    protected SensorSimulado(TipoSensor tipo, double minSimulado, double maxSimulado, Random random) {
        if (tipo == null || random == null) {
            throw new DadosInvalidosException("Sensor sem tipo ou sem gerador de números.");
        }
        if (minSimulado < tipo.getMin() || maxSimulado > tipo.getMax() || minSimulado >= maxSimulado) {
            throw new DadosInvalidosException("Faixa de simulação inválida para o sensor de "
                    + tipo.getRotulo().toLowerCase() + ".");
        }
        this.tipo = tipo;
        this.minSimulado = minSimulado;
        this.maxSimulado = maxSimulado;
        this.random = random;
    }

    @Override
    public double lerValor() throws FonteIndisponivelException {
        if (!funcionando) {
            throw new FonteIndisponivelException("Sensor de " + tipo.getRotulo().toLowerCase()
                    + " indisponível (desligado ou com falha).");
        }
        double valor = minSimulado + random.nextDouble() * (maxSimulado - minSimulado);
        return Math.round(valor * 10.0) / 10.0; // 1 casa decimal
    }

    @Override
    public boolean estaFuncionando() {
        return funcionando;
    }

    /** Permite simular falha do sensor (para demonstrar FonteIndisponivelException). */
    public void setFuncionando(boolean funcionando) {
        this.funcionando = funcionando;
    }

    @Override
    public TipoSensor getTipo() {
        return tipo;
    }
}
