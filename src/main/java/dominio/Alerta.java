package dominio;

import excecao.DadosInvalidosException;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Um alerta emitido. Sempre carrega o porquê: qual sensor, qual valor
 * (atual ou previsto) e qual limite foi estourado.
 */
public class Alerta {

    private final NivelAlerta nivel;
    private final TipoSensor tipoSensor;
    private final double valorReferencia;
    private final double limiteEstourado;
    private final boolean previsto;
    private final String mensagem;
    private final LocalDateTime dataHora;

    /** Alerta novo: a mensagem explicativa é montada automaticamente. */
    public Alerta(NivelAlerta nivel, TipoSensor tipoSensor, double valorReferencia,
                  double limiteEstourado, boolean previsto) {
        this(nivel, tipoSensor, valorReferencia, limiteEstourado, previsto,
                montarMensagem(nivel, tipoSensor, valorReferencia, limiteEstourado, previsto),
                LocalDateTime.now());
    }

    /** Reconstrução (ex.: lido do banco). */
    public Alerta(NivelAlerta nivel, TipoSensor tipoSensor, double valorReferencia,
                  double limiteEstourado, boolean previsto, String mensagem, LocalDateTime dataHora) {
        if (nivel == null || tipoSensor == null) {
            throw new DadosInvalidosException("Alerta sem nível ou sem tipo de sensor.");
        }
        if (mensagem == null || mensagem.isBlank()) {
            throw new DadosInvalidosException("Alerta sem mensagem explicativa.");
        }
        if (dataHora == null) {
            throw new DadosInvalidosException("Alerta sem data/hora.");
        }
        this.nivel = nivel;
        this.tipoSensor = tipoSensor;
        this.valorReferencia = valorReferencia;
        this.limiteEstourado = limiteEstourado;
        this.previsto = previsto;
        this.mensagem = mensagem;
        this.dataHora = dataHora;
    }

    private static String montarMensagem(NivelAlerta nivel, TipoSensor tipo, double valor,
                                         double limite, boolean previsto) {
        if (nivel == null || tipo == null) {
            throw new DadosInvalidosException("Alerta sem nível ou sem tipo de sensor.");
        }
        String quando = previsto ? "valor previsto" : "valor atual";
        return String.format(Locale.forLanguageTag("pt-BR"),
                "[%s] %s: %s de %.1f %s ultrapassa o limite de %s (%.1f %s). "
                        + "Recomenda-se que o operador avalie as medidas cabíveis.",
                nivel.getRotulo(), tipo.getRotulo(), quando, valor, tipo.getUnidade(),
                nivel.getRotulo(), limite, tipo.getUnidade());
    }

    public NivelAlerta getNivel() {
        return nivel;
    }

    public TipoSensor getTipoSensor() {
        return tipoSensor;
    }

    public double getValorReferencia() {
        return valorReferencia;
    }

    public double getLimiteEstourado() {
        return limiteEstourado;
    }

    public boolean isPrevisto() {
        return previsto;
    }

    public String getMensagem() {
        return mensagem;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    @Override
    public String toString() {
        return dataHora.withNano(0) + " " + mensagem;
    }
}
