package servico;

import excecao.DadosInvalidosException;

/**
 * Configuração global do sistema (Singleton): uma única instância guarda a
 * estratégia ativa, o tamanho da janela e o mínimo de leituras exigido.
 * O construtor é privado: ninguém além da própria classe cria instâncias.
 */
public class ConfiguracaoSistema {

    private static final int JANELA_PADRAO = 3;
    private static final int MINIMO_LEITURAS_PADRAO = 5;
    private static final TipoPrevisao TIPO_PADRAO = TipoPrevisao.COMPARACAO;

    private static final ConfiguracaoSistema INSTANCIA = new ConfiguracaoSistema();

    private int janela = JANELA_PADRAO;
    private int minimoLeituras = MINIMO_LEITURAS_PADRAO;
    private TipoPrevisao tipoPrevisao = TIPO_PADRAO;

    private ConfiguracaoSistema() {
    }

    public static ConfiguracaoSistema getInstance() {
        return INSTANCIA;
    }

    public int getJanela() {
        return janela;
    }

    public void setJanela(int janela) {
        if (janela < 1) {
            throw new DadosInvalidosException("A janela de previsão deve ser pelo menos 1 (recebido: " + janela + ").");
        }
        this.janela = janela;
    }

    public int getMinimoLeituras() {
        return minimoLeituras;
    }

    public void setMinimoLeituras(int minimoLeituras) {
        if (minimoLeituras < 1) {
            throw new DadosInvalidosException("O mínimo de leituras deve ser pelo menos 1 (recebido: " + minimoLeituras + ").");
        }
        this.minimoLeituras = minimoLeituras;
    }

    public TipoPrevisao getTipoPrevisao() {
        return tipoPrevisao;
    }

    public void setTipoPrevisao(TipoPrevisao tipoPrevisao) {
        if (tipoPrevisao == null) {
            throw new DadosInvalidosException("Tipo de previsão não informado.");
        }
        this.tipoPrevisao = tipoPrevisao;
    }

    /** Volta aos valores padrão. Útil em testes, já que a instância é compartilhada. */
    public void restaurarPadrao() {
        janela = JANELA_PADRAO;
        minimoLeituras = MINIMO_LEITURAS_PADRAO;
        tipoPrevisao = TIPO_PADRAO;
    }
}
