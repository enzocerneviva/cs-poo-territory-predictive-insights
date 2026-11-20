package dominio;

import excecao.DadosInvalidosException;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Uma leitura de sensor em um instante. Imutável: depois de criada não muda.
 * A validação fica no construtor, então é impossível existir uma Medicao
 * inválida, venha ela do sensor ou do banco de dados.
 */
public class Medicao {

    private final TipoSensor tipoSensor;
    private final double valor;
    private final LocalDateTime dataHora;

    /** Leitura nova: a data/hora é o momento atual. */
    public Medicao(TipoSensor tipoSensor, double valor) {
        this(tipoSensor, valor, LocalDateTime.now());
    }

    /** Reconstrução (ex.: lida do banco): preserva a data/hora original. */
    public Medicao(TipoSensor tipoSensor, double valor, LocalDateTime dataHora) {
        if (tipoSensor == null) {
            throw new DadosInvalidosException("Medição sem tipo de sensor.");
        }
        if (dataHora == null) {
            throw new DadosInvalidosException("Medição sem data/hora.");
        }
        if (!tipoSensor.aceita(valor)) {
            throw new DadosInvalidosException(String.format(Locale.forLanguageTag("pt-BR"),
                    "Medição de %s fora da faixa: %.1f %s (válido: %.0f a %.0f).",
                    tipoSensor.getRotulo().toLowerCase(), valor, tipoSensor.getUnidade(),
                    tipoSensor.getMin(), tipoSensor.getMax()));
        }
        this.tipoSensor = tipoSensor;
        this.valor = valor;
        this.dataHora = dataHora;
    }

    public TipoSensor getTipoSensor() {
        return tipoSensor;
    }

    public double getValor() {
        return valor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    @Override
    public String toString() {
        return String.format(Locale.forLanguageTag("pt-BR"), "%s | %s: %.1f %s",
                dataHora.withNano(0), tipoSensor.getRotulo(), valor, tipoSensor.getUnidade());
    }
}
