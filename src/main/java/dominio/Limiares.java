package dominio;

import excecao.DadosInvalidosException;

/**
 * Os três limites de um sensor. Regra: 0 < atenção < alerta < crítico.
 * Como é um record imutável, um Limiares inválido nunca chega a existir.
 */
public record Limiares(double atencao, double alerta, double critico) {

    public Limiares {
        // "!(...)" também rejeita NaN, que faria qualquer comparação dar false.
        if (!(atencao > 0 && atencao < alerta && alerta < critico)) {
            throw new DadosInvalidosException(
                    "Limiares inválidos: é preciso 0 < atenção < alerta < crítico (recebido: "
                            + atencao + " / " + alerta + " / " + critico + ").");
        }
    }

    /**
     * Classifica um valor segundo estes limites.
     * @return o nível atingido, ou null se o valor está abaixo de ATENÇÃO (sem alerta).
     */
    public NivelAlerta classificar(double valor) {
        if (valor >= critico) return NivelAlerta.CRITICO;
        if (valor >= alerta) return NivelAlerta.ALERTA;
        if (valor >= atencao) return NivelAlerta.ATENCAO;
        return null;
    }

    /** Valor do limite correspondente a um nível. */
    public double limiteDe(NivelAlerta nivel) {
        return switch (nivel) {
            case ATENCAO -> atencao;
            case ALERTA -> alerta;
            case CRITICO -> critico;
        };
    }
}
