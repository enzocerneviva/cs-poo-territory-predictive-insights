package dominio;

/**
 * Níveis de alerta em ordem crescente de gravidade.
 * A ordem de declaração importa: ATENCAO < ALERTA < CRITICO (usa ordinal()).
 */
public enum NivelAlerta {
    ATENCAO("ATENÇÃO"),
    ALERTA("ALERTA"),
    CRITICO("CRÍTICO");

    private final String rotulo;

    NivelAlerta(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    /**
     * Devolve o mais grave entre dois níveis. Null significa "sem alerta".
     * Útil para combinar o nível do valor atual com o do valor previsto.
     */
    public static NivelAlerta maior(NivelAlerta a, NivelAlerta b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.ordinal() >= b.ordinal() ? a : b;
    }
}
