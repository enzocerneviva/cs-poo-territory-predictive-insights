package persistencia;

import dominio.Alerta;
import dominio.NivelAlerta;
import dominio.TipoSensor;
import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Acesso à tabela alerta. Todo SQL usa PreparedStatement com parâmetros (?). */
public class AlertaDAO {

    private static final String INSERIR =
            "INSERT INTO alerta (nivel, sensor_tipo, valor_referencia, limite, previsto, mensagem, data_hora) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String LISTAR_TODOS =
            "SELECT nivel, sensor_tipo, valor_referencia, limite, previsto, mensagem, data_hora "
                    + "FROM alerta ORDER BY data_hora DESC, id DESC";

    private final ConexaoBanco banco;

    public AlertaDAO(ConexaoBanco banco) {
        this.banco = banco;
    }

    public void salvar(Alerta alerta) throws FonteIndisponivelException {
        try (Connection conexao = banco.abrir();
             PreparedStatement ps = conexao.prepareStatement(INSERIR)) {
            ps.setString(1, alerta.getNivel().name());
            ps.setString(2, alerta.getTipoSensor().name());
            ps.setDouble(3, alerta.getValorReferencia());
            ps.setDouble(4, alerta.getLimiteEstourado());
            ps.setBoolean(5, alerta.isPrevisto());
            ps.setString(6, alerta.getMensagem());
            ps.setObject(7, alerta.getDataHora());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao gravar o alerta no banco: " + e.getMessage(), e);
        }
    }

    /** Todos os alertas já emitidos, do mais recente para o mais antigo. */
    public List<Alerta> listarTodos() throws FonteIndisponivelException {
        List<Alerta> alertas = new ArrayList<>();
        try (Connection conexao = banco.abrir();
             PreparedStatement ps = conexao.prepareStatement(LISTAR_TODOS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                alertas.add(montarAlerta(rs));
            }
            return alertas;
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao consultar os alertas no banco: " + e.getMessage(), e);
        }
    }

    private Alerta montarAlerta(ResultSet rs) throws SQLException, FonteIndisponivelException {
        try {
            return new Alerta(
                    NivelAlerta.valueOf(rs.getString("nivel")),
                    TipoSensor.valueOf(rs.getString("sensor_tipo")),
                    rs.getDouble("valor_referencia"),
                    rs.getDouble("limite"),
                    rs.getBoolean("previsto"),
                    rs.getString("mensagem"),
                    rs.getObject("data_hora", LocalDateTime.class));
        } catch (IllegalArgumentException | DadosInvalidosException e) {
            throw new FonteIndisponivelException("Registro de alerta inválido no banco: " + e.getMessage(), e);
        }
    }
}
