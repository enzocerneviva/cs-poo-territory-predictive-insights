# Territory Predictive Insights: alerta de chuva e alagamento

Global Solution 2026.2 - Programação Orientada a Objetos (FIAP) - Tema **CitizenTech**

Sistema de console em Java que lê sensores de um ponto de monitoramento (chuva, nível do rio e umidade do solo),
guarda o histórico em banco de dados, **prevê** o próximo valor e **emite alertas explicados** (ATENÇÃO, ALERTA, CRÍTICO).

## Público beneficiado e decisão apoiada

- **Quem sofre o problema:** moradores de áreas sujeitas a alagamento (recorte de exemplo: Lajeado, Vale do Taquari, RS).
- **Quem usa o sistema:** o operador da Defesa Civil / prefeitura.
- **Decisão que o sistema apoia:** "devo avisar a população e preparar abrigos agora?". O sistema **não decide**:
  ele mostra o valor atual, o valor previsto, o limite estourado e recomenda que o operador avalie as medidas.

> Os dados dos sensores são **simulados** (`Random` em faixas realistas). Não há dados pessoais: o território é
> descrito por região e equipamento, nunca por indivíduos.

## Como rodar

**Requisitos:** Java 21 e Maven (ou IntelliJ IDEA, que já traz o Maven).

**Pelo IntelliJ (mais simples):** abra a pasta do projeto, aguarde o import do Maven, abra
`src/main/java/SistemaMonitoramento.java` e clique no botão verde ao lado do `main`.
Para os testes: aba **Maven → Lifecycle → test**.

**Pelo terminal (com Maven instalado):**

```bash
mvn test                       # roda os 21 testes
mvn -q compile exec:java       # abre o menu de console
```

**Banco de dados:** H2 em **modo arquivo** (`./data/territorio`), criado automaticamente na primeira execução,
sem configuração. Os dados sobrevivem ao reiniciar. Para recomeçar do zero, apague a pasta `data/`.

**Roteiro de demonstração (2 min):**
1. Opção `1` -> informe `6` leituras por sensor (o sistema exige no mínimo 5 leituras para prever).
2. Opção `2` -> veja as duas previsões por sensor e os alertas emitidos.
3. Opção `3` -> consulte o histórico de um sensor e os alertas gravados.
4. Opção `4` -> altere limiares (tente um valor inválido para ver a validação) ou desligue um sensor.
5. Saia, abra de novo e consulte a opção `3`: o histórico continua lá.

## Estrutura

```
src/main/java/
  dominio/       Medicao, Alerta, Limiares, DadosTerritorio, TipoSensor, NivelAlerta, StatusComponente,
                 Sensor (+ SensorSimulado, SensorPluviometro, SensorNivelRio, SensorUmidadeSolo),
                 ComponenteTerritorial (+ PontoMonitoramento),
                 EstrategiaPrevisao (+ PrevisaoMediaMovel, PrevisaoComparacao)
  excecao/       DadosInvalidosException, FonteIndisponivelException
  persistencia/  ConexaoBanco, MedicaoDAO, AlertaDAO   (JDBC + H2)
  servico/       ServicoMonitoramento, FabricaPrevisao, ConfiguracaoSistema, TipoPrevisao, ResultadoPrevisao
  SistemaMonitoramento.java   (menu de console)
src/test/java/   MedicaoTest, DadosTerritorioTest, PrevisaoMediaMovelTest, PrevisaoComparacaoTest, ServicoMonitoramentoTest
```

## Decisões técnicas

- **JDBC puro (DAO) em vez de Spring Boot/JPA:** menos camadas e menos risco de não rodar na correção; todo SQL usa
  `PreparedStatement` com parâmetros e `try-with-resources`.
- **H2 em arquivo:** o enunciado exige que o histórico sobreviva ao reiniciar, o que o modo em memória não permite.
- **Validação no construtor:** `Medicao`, `Alerta` e `Limiares` são imutáveis e validam ao nascer; assim nenhum
  objeto inválido existe, nem quando reconstruído a partir do banco.
- **Duas exceções com tipos diferentes:** `DadosInvalidosException` é *unchecked* (erro de dado na origem, lançada de
  construtores e do `record Limiares`); `FonteIndisponivelException` é *checked* (falha esperada de sensor ou banco,
  o chamador é obrigado a tratar). O menu captura as duas com mensagem clara, sem `catch` vazio.
- **Alerta usa valor atual e previsto:** `ServicoMonitoramento` classifica os dois e emite o **maior** nível,
  registrando se o motivo foi o valor já medido ou a tendência.
- **Polimorfismo sem `if`:** o serviço trabalha com `Sensor` e `EstrategiaPrevisao`, nunca com `instanceof`.

## Padrão criacional: Factory (`FabricaPrevisao`) e Singleton (`ConfiguracaoSistema`)

- **Factory:** `FabricaPrevisao.criar(tipo, janela)` é o único lugar que conhece as classes concretas
  `PrevisaoMediaMovel` e `PrevisaoComparacao`. O menu e o serviço pedem "a estratégia configurada" e não
  duplicam `new` nem `if tipo ==`. Para adicionar uma estratégia nova, mexe-se só na fábrica e no enum.
  Foi aplicada ali, e não em `Medicao` ou `Alerta`, porque é onde há **variação de implementação** a escolher
  em tempo de execução; as demais classes têm um único jeito de ser criadas.
- **Singleton:** `ConfiguracaoSistema` (construtor privado) guarda estratégia ativa, janela e mínimo de leituras,
  que precisam ser os mesmos em todo o sistema. Como o estado é compartilhado, os testes chamam `restaurarPadrao()`
  antes de cada caso.

## Testes (21, JUnit 5 + Mockito)

- Padrão AAA em todos; `assertThrows` para `DadosInvalidosException` (medição fora da faixa, população negativa,
  limiares fora de ordem, janela inválida, histórico vazio).
- `ServicoMonitoramentoTest` usa **Mockito** nos DAOs: testa a lógica de alerta (poucas leituras, valor atual crítico,
  alerta por tendência, valores baixos) **sem banco**.

## Reflexão crítica

**Quando o sistema deve se calar?** Com menos de 5 leituras no histórico ele não prevê nem alerta. Cinco é o menor
número em que a comparação de períodos consegue usar duas leituras por período; com menos, uma única leitura atípica
dominaria o resultado. O custo é real: numa enchente repentina, com o banco ainda vazio, o sistema ficaria calado
mesmo com o nível do rio já alto. É uma limitação assumida; num sistema de produção, o valor **atual** acima do
limite crítico deveria alertar independentemente do tamanho do histórico, e um sensor desligado deveria gerar
um aviso próprio em vez de silêncio.

**O que os dados não representam?** Os valores são simulados e os limiares padrão não foram calibrados com dados
reais. Um sensor por ponto diz pouco sobre ruas, bairros e cotas de terreno diferentes; a média móvel e a
projeção linear ignoram fatores como chuva acumulada, saturação do solo e previsão meteorológica. A previsão
é uma estimativa simples de tendência, não um modelo hidrológico.

**Por que o alerta apoia a decisão em vez de decidir?** Evacuar uma área tem custo e risco para as pessoas, e
só quem conhece o território (e pode confirmar em campo) sabe ponderar falso alarme contra risco real. Por isso
o alerta sempre **explica** o porquê (sensor, valor, limite, se é atual ou previsto) e usa linguagem de
recomendação, mantendo a decisão e a responsabilidade com o operador humano.

## Uso de IA

- **Onde foi usada:** o assistente Claude foi usado como um acelerador de desenvolvimento na maior parte do projeto (código e documentação).
- **Como foi revisado:** a revisão dos arquivos escritos por IA foi feita a partir da estrutura de arquivos pré-definida, regras de negócio e testes. Para garantir que 
o desenvolvimento esteja seguido o projeto corretamente de acordo com que foi solicitado.

## Divisão de tarefas

| Integrante                              | Responsabilidade             |
|-----------------------------------------|------------------------------|
| _Enzo Cardilli Cerneviva — RM 563480_   | _Desenvolvimento_            |
| _Matheus Lara Carneiro — RM 564049_     | _Participação na discussão da arquitetura e da estrutura do projeto_ |
| _Victor Hugo Almeida Bahia — RM 564633_ | _Gestão das tarefas no Trello e ideia do recorte de chuva e alagamento_ |
