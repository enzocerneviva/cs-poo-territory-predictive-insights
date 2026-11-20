package servico;

import dominio.EstrategiaPrevisao;
import dominio.PrevisaoComparacao;
import dominio.PrevisaoMediaMovel;
import excecao.DadosInvalidosException;

/**
 * Factory: concentra a criação das estratégias de previsão. O resto do código
 * pede "uma estratégia do tipo X" e nunca usa new PrevisaoXxx() nem if/instanceof.
 * Para criar uma nova estratégia, só esta classe (e o enum) mudam.
 */
public final class FabricaPrevisao {

    private FabricaPrevisao() {
    }

    public static EstrategiaPrevisao criar(TipoPrevisao tipo, int janela) {
        if (tipo == null) {
            throw new DadosInvalidosException("Tipo de previsão não informado.");
        }
        return switch (tipo) {
            case MEDIA_MOVEL -> new PrevisaoMediaMovel(janela);
            case COMPARACAO -> new PrevisaoComparacao(janela);
        };
    }

    /** Cria a estratégia ativa segundo a configuração do sistema. */
    public static EstrategiaPrevisao criar(ConfiguracaoSistema configuracao) {
        return criar(configuracao.getTipoPrevisao(), configuracao.getJanela());
    }
}
