package persistencia;

import dominio.Medicao;
import dominio.TipoSensor;
import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Acesso à tabela medicao. Todo SQL usa PreparedStatement com parâmetros (?). */
public class MedicaoDAO {

    private static final String INSERIR =
            "INSERT INTO medicao (sensor_tipo, valor, data_hora) VALUES (?, ?, ?)";

    private static final String BUSCAR_POR_SENSOR =
            "SELECT sensor_tipo, valor, data_hora FROM medicao "
                    + "WHERE sensor_tipo = ? ORDER BY data_hora, id";

    private static final String BUSCAR_ULTIMAS =
            "SELECT sensor_tipo, valor, data_hora FROM medicao "
                    + "WHERE sensor_tipo = ? ORDER BY data_hora DESC, id DESC LIMIT ?";

    private final ConexaoBanco banco;

    public MedicaoDAO(ConexaoBanco banco) {
        this.banco = banco;
    }

    public void salvar(Medicao medicao) throws FonteIndisponivelException {
        try (Connection conexao = banco.abrir();
             PreparedStatement ps = conexao.prepareStatement(INSERIR)) {
            ps.setString(1, medicao.getTipoSensor().name());
            ps.setDouble(2, medicao.getValor());
            ps.setObject(3, medicao.getDataHora());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao gravar a medição no banco: " + e.getMessage(), e);
        }
    }

    /** Todo o histórico de um sensor, da medição mais antiga para a mais recente. */
    public List<Medicao> buscarPorSensor(TipoSensor tipo) throws FonteIndisponivelException {
        try (Connection conexao = banco.abrir();
             PreparedStatement ps = conexao.prepareStatement(BUSCAR_POR_SENSOR)) {
            ps.setString(1, tipo.name());
            return lerMedicoes(ps);
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao consultar o histórico no banco: " + e.getMessage(), e);
        }
    }

    /**
     * As últimas N medições de um sensor, já em ordem cronológica
     * (mais antiga primeiro), pronta para as estratégias de previsão.
     */
    public List<Medicao> buscarUltimas(TipoSensor tipo, int quantidade) throws FonteIndisponivelException {
        try (Connection conexao = banco.abrir();
             PreparedStatement ps = conexao.prepareStatement(BUSCAR_ULTIMAS)) {
            ps.setString(1, tipo.name());
            ps.setInt(2, quantidade);
            List<Medicao> maisRecentesPrimeiro = lerMedicoes(ps);
            Collections.reverse(maisRecentesPrimeiro);
            return maisRecentesPrimeiro;
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao consultar as últimas medições no banco: " + e.getMessage(), e);
        }
    }

    private List<Medicao> lerMedicoes(PreparedStatement ps) throws SQLException, FonteIndisponivelException {
        List<Medicao> medicoes = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                medicoes.add(montarMedicao(rs));
            }
        }
        return medicoes;
    }

    private Medicao montarMedicao(ResultSet rs) throws SQLException, FonteIndisponivelException {
        try {
            TipoSensor tipo = TipoSensor.valueOf(rs.getString("sensor_tipo"));
            double valor = rs.getDouble("valor");
            LocalDateTime dataHora = rs.getObject("data_hora", LocalDateTime.class);
            return new Medicao(tipo, valor, dataHora);
        } catch (IllegalArgumentException | DadosInvalidosException e) {
            // Registro corrompido no banco: tipo desconhecido ou valor fora da faixa.
            throw new FonteIndisponivelException("Registro de medição inválido no banco: " + e.getMessage(), e);
        }
    }
}
