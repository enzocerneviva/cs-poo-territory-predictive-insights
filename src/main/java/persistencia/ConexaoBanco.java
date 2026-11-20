package persistencia;

import excecao.FonteIndisponivelException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Ponto único de acesso ao banco H2. Usa o modo ARQUIVO (./data/territorio),
 * então medições e alertas sobrevivem ao reiniciar o programa.
 * Quem pede a conexão (os DAOs) é responsável por fechá-la (try-with-resources).
 */
public class ConexaoBanco {

    public static final String URL_PADRAO = "jdbc:h2:./data/territorio";

    private static final String CRIAR_TABELA_MEDICAO = """
            CREATE TABLE IF NOT EXISTS medicao (
                id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                sensor_tipo VARCHAR(30) NOT NULL,
                valor       DOUBLE      NOT NULL,
                data_hora   TIMESTAMP   NOT NULL
            )""";

    private static final String CRIAR_INDICE_MEDICAO = """
            CREATE INDEX IF NOT EXISTS idx_medicao_sensor
            ON medicao (sensor_tipo, data_hora)""";

    private static final String CRIAR_TABELA_ALERTA = """
            CREATE TABLE IF NOT EXISTS alerta (
                id               BIGINT AUTO_INCREMENT PRIMARY KEY,
                nivel            VARCHAR(10)  NOT NULL,
                sensor_tipo      VARCHAR(30)  NOT NULL,
                valor_referencia DOUBLE       NOT NULL,
                limite           DOUBLE       NOT NULL,
                previsto         BOOLEAN      NOT NULL,
                mensagem         VARCHAR(500) NOT NULL,
                data_hora        TIMESTAMP    NOT NULL
            )""";

    private final String url;

    public ConexaoBanco() {
        this(URL_PADRAO);
    }

    /** Permite apontar para outro banco (ex.: em memória, em testes). */
    public ConexaoBanco(String url) {
        this.url = url;
    }

    /** Abre uma nova conexão. Quem chamar deve fechar. */
    public Connection abrir() throws FonteIndisponivelException {
        try {
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Não foi possível conectar ao banco de dados: " + e.getMessage(), e);
        }
    }

    /** Cria as tabelas se ainda não existirem. Seguro chamar a cada execução. */
    public void inicializarTabelas() throws FonteIndisponivelException {
        try (Connection conexao = abrir();
             Statement comando = conexao.createStatement()) {
            comando.execute(CRIAR_TABELA_MEDICAO);
            comando.execute(CRIAR_INDICE_MEDICAO);
            comando.execute(CRIAR_TABELA_ALERTA);
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao criar as tabelas do banco: " + e.getMessage(), e);
        }
    }
}
