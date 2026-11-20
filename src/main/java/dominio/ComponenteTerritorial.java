package dominio;

import excecao.DadosInvalidosException;

/**
 * Qualquer elemento monitorado do território (ponto de monitoramento, estação etc.).
 * Cada filha decide como verificar o próprio status.
 */
public abstract class ComponenteTerritorial {

    private final int id;
    private final String nome;
    private final String regiao;
    private StatusComponente status = StatusComponente.OPERANTE;

    protected ComponenteTerritorial(int id, String nome, String regiao) {
        if (nome == null || nome.isBlank()) {
            throw new DadosInvalidosException("Componente territorial sem nome.");
        }
        if (regiao == null || regiao.isBlank()) {
            throw new DadosInvalidosException("Componente territorial sem região.");
        }
        this.id = id;
        this.nome = nome;
        this.regiao = regiao;
    }

    /** Recalcula e devolve o status atual do componente. */
    public abstract StatusComponente verificarStatus();

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getRegiao() {
        return regiao;
    }

    public StatusComponente getStatus() {
        return status;
    }

    protected void setStatus(StatusComponente status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return nome + " (" + regiao + ") - " + status;
    }
}
