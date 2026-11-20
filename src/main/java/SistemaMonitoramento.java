import dominio.Alerta;
import dominio.DadosTerritorio;
import dominio.Limiares;
import dominio.Medicao;
import dominio.PontoMonitoramento;
import dominio.SensorNivelRio;
import dominio.SensorPluviometro;
import dominio.SensorSimulado;
import dominio.SensorUmidadeSolo;
import dominio.TipoSensor;
import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;
import persistencia.AlertaDAO;
import persistencia.ConexaoBanco;
import persistencia.MedicaoDAO;
import servico.ConfiguracaoSistema;
import servico.FabricaPrevisao;
import servico.ResultadoPrevisao;
import servico.ServicoMonitoramento;
import servico.TipoPrevisao;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

/** Menu principal (console). Só conversa com o usuário; a regra de negócio fica no serviço. */
public class SistemaMonitoramento {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final int MAX_LEITURAS_POR_VEZ = 20;
    private static final int MAX_HISTORICO_EXIBIDO = 30;

    private final Scanner scanner = new Scanner(System.in);
    private final ServicoMonitoramento servico;
    private final DadosTerritorio dados;
    private final ConfiguracaoSistema configuracao;
    private final PontoMonitoramento ponto =
            new PontoMonitoramento(1, "Ponte do Rio", "Lajeado - Vale do Taquari (RS)");
    private final List<SensorSimulado> sensores =
            List.of(new SensorPluviometro(), new SensorNivelRio(), new SensorUmidadeSolo());

    public SistemaMonitoramento(ServicoMonitoramento servico, DadosTerritorio dados,
                                ConfiguracaoSistema configuracao) {
        this.servico = servico;
        this.dados = dados;
        this.configuracao = configuracao;
        sensores.forEach(ponto::adicionarSensor);
    }

    public static void main(String[] args) {
        ConexaoBanco banco = new ConexaoBanco();
        try {
            banco.inicializarTabelas();
        } catch (FonteIndisponivelException e) {
            System.out.println("Não foi possível iniciar o sistema: " + e.getMessage());
            return;
        }
        DadosTerritorio dados = new DadosTerritorio(5000);
        ConfiguracaoSistema configuracao = ConfiguracaoSistema.getInstance();
        ServicoMonitoramento servico = new ServicoMonitoramento(
                new MedicaoDAO(banco), new AlertaDAO(banco), dados, configuracao);
        new SistemaMonitoramento(servico, dados, configuracao).executar();
    }

    // ------------------------------------------------------------------ loop

    private void executar() {
        System.out.println("=== Territory Predictive Insights - Monitoramento de chuva e alagamento ===");
        boolean sair = false;
        while (!sair) {
            imprimirMenu();
            if (!scanner.hasNextLine()) {
                break;
            }
            String opcao = scanner.nextLine().trim();
            try {
                switch (opcao) {
                    case "1" -> verificarSensores();
                    case "2" -> rodarPrevisoes();
                    case "3" -> verHistorico();
                    case "4" -> gerenciarDados();
                    case "5" -> statusCompleto();
                    case "0" -> sair = true;
                    default -> System.out.println("Opção inválida: '" + opcao + "'. Escolha um número do menu.");
                }
            } catch (DadosInvalidosException e) {
                System.out.println("Dado inválido: " + e.getMessage());
            } catch (FonteIndisponivelException e) {
                System.out.println("Fonte de dados indisponível: " + e.getMessage());
            }
        }
        System.out.println("Encerrando. Os dados ficam salvos no banco.");
    }

    private void imprimirMenu() {
        System.out.println();
        System.out.println("--- MENU ---");
        System.out.println("1 - Verificar sensores (ler e gravar)");
        System.out.println("2 - Rodar previsões e alertas");
        System.out.println("3 - Ver histórico (medições e alertas)");
        System.out.println("4 - Gerenciar dados do território");
        System.out.println("5 - Status completo");
        System.out.println("0 - Sair");
        System.out.print("Opção: ");
    }

    // ------------------------------------------------------------- opção 1

    private void verificarSensores() {
        int quantidade = lerInteiro("Quantas leituras por sensor (1 a " + MAX_LEITURAS_POR_VEZ + ")? ");
        if (quantidade < 1 || quantidade > MAX_LEITURAS_POR_VEZ) {
            throw new DadosInvalidosException("Informe um número entre 1 e " + MAX_LEITURAS_POR_VEZ + ".");
        }
        int gravadas = 0;
        Set<TipoSensor> comFalha = new HashSet<>();
        for (int i = 1; i <= quantidade; i++) {
            for (SensorSimulado sensor : sensores) {
                if (comFalha.contains(sensor.getTipo())) {
                    continue;
                }
                try {
                    Medicao medicao = servico.lerEGravar(sensor);
                    gravadas++;
                    if (quantidade <= 3) {
                        System.out.println("  " + medicao);
                    }
                } catch (FonteIndisponivelException e) {
                    comFalha.add(sensor.getTipo());
                    System.out.println("  ! " + e.getMessage());
                }
            }
        }
        System.out.println(gravadas + " medição(ões) gravada(s) no banco.");
    }

    // ------------------------------------------------------------- opção 2

    private void rodarPrevisoes() throws FonteIndisponivelException {
        System.out.println("Estratégia que gera os alertas: "
                + FabricaPrevisao.criar(configuracao).getNome());
        for (TipoSensor tipo : TipoSensor.values()) {
            System.out.println();
            System.out.println("# " + tipo.getRotulo() + " (" + tipo.getUnidade() + ")");
            for (TipoPrevisao tipoPrevisao : TipoPrevisao.values()) {
                ResultadoPrevisao resultado = servico.prever(tipo,
                        FabricaPrevisao.criar(tipoPrevisao, configuracao.getJanela()));
                imprimirResultado(resultado);
            }
            Alerta alerta = servico.avaliarEAlertar(tipo);
            if (alerta != null) {
                System.out.println("  >>> " + alerta.getMensagem());
            } else {
                System.out.println("  Nenhum alerta emitido para este sensor.");
            }
        }
    }

    private void imprimirResultado(ResultadoPrevisao r) {
        if (!r.suficiente()) {
            System.out.println(String.format(PT_BR,
                    "  [%s] Histórico insuficiente: %d leitura(s), mínimo %d. "
                            + "Previsão com tão pouco histórico não é confiável, então o sistema não alerta.",
                    r.estrategia(), r.leituras(), r.minimoExigido()));
            return;
        }
        System.out.println(String.format(PT_BR, "  [%s] atual %.1f -> previsto %.1f %s",
                r.estrategia(), r.valorAtual(), r.valorPrevisto(), r.tipo().getUnidade()));
        System.out.println("      " + r.descricao());
    }

    // ------------------------------------------------------------- opção 3

    private void verHistorico() throws FonteIndisponivelException {
        System.out.println("1 - Medições de um sensor");
        System.out.println("2 - Alertas já emitidos");
        int opcao = lerInteiro("Opção: ");
        switch (opcao) {
            case 1 -> {
                TipoSensor tipo = escolherSensor();
                List<Medicao> medicoes = servico.historico(tipo);
                if (medicoes.isEmpty()) {
                    System.out.println("Nenhuma medição gravada para " + tipo.getRotulo() + ".");
                    return;
                }
                int inicio = Math.max(0, medicoes.size() - MAX_HISTORICO_EXIBIDO);
                System.out.println(medicoes.size() + " medição(ões); exibindo as " + (medicoes.size() - inicio) + " mais recentes:");
                medicoes.subList(inicio, medicoes.size()).forEach(m -> System.out.println("  " + m));
            }
            case 2 -> {
                List<Alerta> alertas = servico.alertasEmitidos();
                if (alertas.isEmpty()) {
                    System.out.println("Nenhum alerta emitido até agora.");
                    return;
                }
                System.out.println(alertas.size() + " alerta(s), do mais recente ao mais antigo:");
                alertas.stream().limit(MAX_HISTORICO_EXIBIDO).forEach(a -> System.out.println("  " + a));
            }
            default -> throw new DadosInvalidosException("Opção inexistente: " + opcao);
        }
    }

    // ------------------------------------------------------------- opção 4

    private void gerenciarDados() {
        System.out.println("1 - Alterar população exposta");
        System.out.println("2 - Alterar limiares de um sensor");
        System.out.println("3 - Escolher estratégia e janela de previsão");
        System.out.println("4 - Ligar/desligar sensor (simular falha)");
        int opcao = lerInteiro("Opção: ");
        switch (opcao) {
            case 1 -> {
                dados.setPopulacaoExposta(lerInteiro("Nova população exposta: "));
                System.out.println("População exposta atualizada: " + dados.getPopulacaoExposta());
            }
            case 2 -> {
                TipoSensor tipo = escolherSensor();
                System.out.println("Limiares atuais: " + formatarLimiares(tipo));
                double atencao = lerDecimal("Novo limiar de ATENÇÃO: ");
                double alerta = lerDecimal("Novo limiar de ALERTA: ");
                double critico = lerDecimal("Novo limiar CRÍTICO: ");
                dados.definirLimiares(tipo, atencao, alerta, critico);
                System.out.println("Limiares atualizados: " + formatarLimiares(tipo));
            }
            case 3 -> {
                TipoPrevisao[] tipos = TipoPrevisao.values();
                for (int i = 0; i < tipos.length; i++) {
                    System.out.println((i + 1) + " - " + tipos[i].getRotulo());
                }
                int escolha = lerInteiro("Estratégia: ");
                if (escolha < 1 || escolha > tipos.length) {
                    throw new DadosInvalidosException("Estratégia inexistente: " + escolha);
                }
                int janela = lerInteiro("Tamanho da janela (leituras): ");
                configuracao.setJanela(janela);
                configuracao.setTipoPrevisao(tipos[escolha - 1]);
                System.out.println("Configuração atualizada: " + tipos[escolha - 1].getRotulo() + ", janela " + janela + ".");
            }
            case 4 -> {
                TipoSensor tipo = escolherSensor();
                for (SensorSimulado sensor : sensores) {
                    if (sensor.getTipo() == tipo) {
                        sensor.setFuncionando(!sensor.estaFuncionando());
                        System.out.println("Sensor de " + tipo.getRotulo().toLowerCase() + " agora está "
                                + (sensor.estaFuncionando() ? "LIGADO" : "DESLIGADO") + ".");
                    }
                }
            }
            default -> throw new DadosInvalidosException("Opção inexistente: " + opcao);
        }
    }

    // ------------------------------------------------------------- opção 5

    private void statusCompleto() throws FonteIndisponivelException {
        ponto.verificarStatus();
        System.out.println("Ponto: " + ponto);
        System.out.println("População exposta: " + dados.getPopulacaoExposta());
        System.out.println("Previsão: " + FabricaPrevisao.criar(configuracao).getNome()
                + " | mínimo de leituras: " + configuracao.getMinimoLeituras());
        for (SensorSimulado sensor : sensores) {
            TipoSensor tipo = sensor.getTipo();
            List<Medicao> ultima = servico.ultimasMedicoes(tipo, 1);
            String leitura = ultima.isEmpty() ? "sem leituras" : ultima.get(0).toString();
            System.out.println("  - " + tipo.getRotulo() + ": " + (sensor.estaFuncionando() ? "ligado" : "DESLIGADO")
                    + " | limiares " + formatarLimiares(tipo) + " | última: " + leitura);
        }
        System.out.println("Alertas emitidos até agora: " + servico.alertasEmitidos().size());
    }

    // ------------------------------------------------------------- apoio

    private String formatarLimiares(TipoSensor tipo) {
        Limiares l = dados.getLimiares(tipo);
        return String.format(PT_BR, "atenção %.1f / alerta %.1f / crítico %.1f %s",
                l.atencao(), l.alerta(), l.critico(), tipo.getUnidade());
    }

    private TipoSensor escolherSensor() {
        TipoSensor[] tipos = TipoSensor.values();
        for (int i = 0; i < tipos.length; i++) {
            System.out.println((i + 1) + " - " + tipos[i].getRotulo());
        }
        int opcao = lerInteiro("Sensor: ");
        if (opcao < 1 || opcao > tipos.length) {
            throw new DadosInvalidosException("Sensor inexistente: " + opcao);
        }
        return tipos[opcao - 1];
    }

    private int lerInteiro(String mensagem) {
        System.out.print(mensagem);
        String texto = scanner.nextLine().trim();
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new DadosInvalidosException("'" + texto + "' não é um número inteiro válido.");
        }
    }

    private double lerDecimal(String mensagem) {
        System.out.print(mensagem);
        String texto = scanner.nextLine().trim();
        try {
            return Double.parseDouble(texto.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new DadosInvalidosException("'" + texto + "' não é um número válido.");
        }
    }
}
