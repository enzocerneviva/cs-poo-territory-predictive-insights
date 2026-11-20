package servico;

/** Estratégias de previsão disponíveis; usado pela configuração e pela fábrica. */
public enum TipoPrevisao {
    MEDIA_MOVEL("Média móvel"),
    COMPARACAO("Comparação de períodos");

    private final String rotulo;

    TipoPrevisao(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
