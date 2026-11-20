package dominio;

import excecao.DadosInvalidosException;

import java.util.List;

/**
 * Compara a média das leituras recentes com a do período anterior e projeta
 * a tendência: previsão = média recente + (média recente - média anterior).
 * Detecta subida ou queda antes da média móvel.
 */
public class PrevisaoComparacao extends EstrategiaPrevisao {

    private final int janela;

    public PrevisaoComparacao(int janela) {
        if (janela < 1) {
            throw new DadosInvalidosException("A janela da comparação deve ser pelo menos 1 (recebido: " + janela + ").");
        }
        this.janela = janela;
    }

    @Override
    public double prever(List<Medicao> historico) {
        double ultima = super.prever(historico); // valida e já dá a última leitura
        int n = historico.size();
        int w = Math.min(janela, n / 2); // com poucos dados, usa a metade disponível
        if (w < 1) {
            return ultima; // só 1 leitura: sem como estimar tendência
        }
        double recente = media(historico, n - w, n);
        double anterior = media(historico, n - 2 * w, n - w);
        double projecao = recente + (recente - anterior);

        // Mantém o resultado dentro da faixa física do sensor.
        TipoSensor tipo = historico.get(0).getTipoSensor();
        return Math.max(tipo.getMin(), Math.min(tipo.getMax(), projecao));
    }

    private double media(List<Medicao> historico, int inicio, int fim) {
        double soma = 0;
        for (int i = inicio; i < fim; i++) {
            soma += historico.get(i).getValor();
        }
        return soma / (fim - inicio);
    }

    @Override
    public String getNome() {
        return "Comparação de períodos (" + janela + " leituras)";
    }

    @Override
    public String getDescricao() {
        return "Projeta a tendência: compara a média recente com a do período anterior e estende a variação.";
    }
}
