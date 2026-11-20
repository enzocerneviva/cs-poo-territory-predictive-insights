package excecao;

/**
 * Lançada quando um dado viola uma regra do domínio: medição fora da faixa,
 * população negativa, limiar inválido etc.
 *
 * É unchecked (RuntimeException) porque indica dado inválido na origem
 * (erro de quem chamou) e precisa ser lançada de construtores e records,
 * que não podem declarar "throws" checked sem poluir toda a cadeia.
 */
public class DadosInvalidosException extends RuntimeException {

    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}
