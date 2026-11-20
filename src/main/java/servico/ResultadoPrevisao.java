package servico;

import dominio.NivelAlerta;
import dominio.TipoSensor;

/**
 * Resultado de uma previsão para um sensor: valor atual, valor previsto e
 * o nível de alerta que isso implica (null = dentro dos limites).
 * Se o histórico for insuficiente, o sistema "se cala": suficiente() é false.
 */
public record ResultadoPrevisao(TipoSensor tipo, String estrategia, String descricao,
                                int leituras, int minimoExigido,
                                double valorAtual, double valorPrevisto,
                                NivelAlerta nivel, double limite, boolean porPrevisao) {

    public static ResultadoPrevisao insuficiente(TipoSensor tipo, String estrategia, String descricao,
                                                 int leituras, int minimoExigido) {
        return new ResultadoPrevisao(tipo, estrategia, descricao, leituras, minimoExigido,
                Double.NaN, Double.NaN, null, Double.NaN, false);
    }

    public boolean suficiente() {
        return leituras >= minimoExigido;
    }

    public boolean temAlerta() {
        return nivel != null;
    }

    /** Valor que motivou o alerta: o previsto (se o alerta é por tendência) ou o atual. */
    public double valorReferencia() {
        return porPrevisao ? valorPrevisto : valorAtual;
    }
}
