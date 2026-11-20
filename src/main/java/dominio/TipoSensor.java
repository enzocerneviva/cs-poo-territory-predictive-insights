package dominio;

/**
 * Tipos de sensor do ponto de monitoramento, cada um com sua faixa válida.
 * A faixa fica aqui para a validação das medições ficar em um lugar só.
 */
public enum TipoSensor {
    CHUVA("Chuva", "mm/h", 0, 150),
    NIVEL_RIO("Nível do rio", "cm", 0, 1000),
    UMIDADE_SOLO("Umidade do solo", "%", 0, 100);

    private final String rotulo;
    private final String unidade;
    private final double min;
    private final double max;

    TipoSensor(String rotulo, String unidade, double min, double max) {
        this.rotulo = rotulo;
        this.unidade = unidade;
        this.min = min;
        this.max = max;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getUnidade() {
        return unidade;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    /** Diz se um valor está dentro da faixa física válida deste sensor. */
    public boolean aceita(double valor) {
        return !Double.isNaN(valor) && valor >= min && valor <= max;
    }
}
