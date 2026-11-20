package dominio;

import excecao.DadosInvalidosException;

import java.util.List;

/** Prevê o próximo valor como a média das últimas N medições. */
public class PrevisaoMediaMovel extends EstrategiaPrevisao {

    private final int janela;

    public PrevisaoMediaMovel(int janela) {
        if (janela < 1) {
            throw new DadosInvalidosException("A janela da média móvel deve ser pelo menos 1 (recebido: " + janela + ").");
        }
        this.janela = janela;
    }

    @Override
    public double prever(List<Medicao> historico) {
        super.prever(historico); // valida o histórico
        int inicio = Math.max(0, historico.size() - janela);
        double soma = 0;
        for (int i = inicio; i < historico.size(); i++) {
            soma += historico.get(i).getValor();
        }
        return soma / (historico.size() - inicio);
    }

    @Override
    public String getNome() {
        return "Média móvel (" + janela + " leituras)";
    }

    @Override
    public String getDescricao() {
        return "Média das últimas " + janela + " leituras: indica o patamar recente, sem considerar a tendência.";
    }
}
