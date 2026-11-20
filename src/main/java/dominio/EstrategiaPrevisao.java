package dominio;

import excecao.DadosInvalidosException;

import java.util.List;

/**
 * Base das estratégias de previsão. O histórico deve vir em ordem
 * cronológica (da medição mais antiga para a mais recente).
 *
 * A implementação base valida o histórico e devolve a última medição
 * (previsão "de persistência"). As filhas sobrescrevem prever(), chamam
 * super.prever() para reaproveitar a validação e calculam a sua própria regra.
 */
public abstract class EstrategiaPrevisao {

    public double prever(List<Medicao> historico) {
        if (historico == null || historico.isEmpty()) {
            throw new DadosInvalidosException("Histórico vazio: não é possível prever.");
        }
        return historico.get(historico.size() - 1).getValor();
    }

    /** Nome curto da estratégia, para exibir ao usuário. */
    public abstract String getNome();

    /** O que o resultado significa, em linguagem simples. */
    public abstract String getDescricao();
}
