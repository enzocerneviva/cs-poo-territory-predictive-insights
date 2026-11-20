package excecao;

/**
 * Lançada quando uma fonte de dados falha: sensor desligado, gerador de
 * leituras indisponível etc.
 *
 * É checked (Exception) porque a falha é esperada e recuperável: o chamador
 * é obrigado a decidir o que fazer (pular o sensor, avisar o operador).
 */
public class FonteIndisponivelException extends Exception {

    public FonteIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public FonteIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
