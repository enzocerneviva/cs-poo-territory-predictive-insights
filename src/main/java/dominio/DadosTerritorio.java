package dominio;

import excecao.DadosInvalidosException;

import java.util.EnumMap;
import java.util.Map;

/**
 * Dados configuráveis do território: população exposta e limiares por sensor.
 * Atributos privados; a única forma de alterá-los é pelos métodos abaixo,
 * que validam e lançam DadosInvalidosException.
 */
public class DadosTerritorio {

    private int populacaoExposta;
    private final Map<TipoSensor, Limiares> limiares = new EnumMap<>(TipoSensor.class);

    public DadosTerritorio(int populacaoExposta) {
        setPopulacaoExposta(populacaoExposta);
        // Valores padrão para o sistema funcionar sem configuração prévia.
        limiares.put(TipoSensor.CHUVA, new Limiares(20, 50, 80));
        limiares.put(TipoSensor.NIVEL_RIO, new Limiares(300, 450, 600));
        limiares.put(TipoSensor.UMIDADE_SOLO, new Limiares(70, 85, 95));
    }

    public int getPopulacaoExposta() {
        return populacaoExposta;
    }

    public void setPopulacaoExposta(int populacaoExposta) {
        if (populacaoExposta < 0) {
            throw new DadosInvalidosException(
                    "População exposta não pode ser negativa (recebido: " + populacaoExposta + ").");
        }
        this.populacaoExposta = populacaoExposta;
    }

    public Limiares getLimiares(TipoSensor tipo) {
        if (tipo == null) {
            throw new DadosInvalidosException("Tipo de sensor não informado.");
        }
        return limiares.get(tipo);
    }

    /** Altera os limiares de um sensor, validando a ordem e a faixa física do sensor. */
    public void definirLimiares(TipoSensor tipo, double atencao, double alerta, double critico) {
        if (tipo == null) {
            throw new DadosInvalidosException("Tipo de sensor não informado.");
        }
        Limiares novos = new Limiares(atencao, alerta, critico); // valida a ordem
        if (novos.critico() > tipo.getMax()) {
            throw new DadosInvalidosException(String.format(
                    "Limiar crítico (%.1f) acima do máximo possível do sensor de %s (%.0f %s).",
                    novos.critico(), tipo.getRotulo().toLowerCase(), tipo.getMax(), tipo.getUnidade()));
        }
        limiares.put(tipo, novos);
    }
}
