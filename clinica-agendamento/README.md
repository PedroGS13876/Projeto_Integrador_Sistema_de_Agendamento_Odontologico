# Sistema de agendamento para clínica odontológica

Projeto Integrador – Laboratório de Programação (UNISA).
Cenário **simulado**: todos os dados (clínica, dentistas e pacientes) são fictícios.

## Funcionalidades

- Cadastro de pacientes, dentistas (com expediente padrão de segunda a sexta, 8h–12h e 13h–18h) e procedimentos
- Agendamento com validação de conflitos, expediente, data passada e simultaneidade do paciente
- Cancelamento (com motivo), remarcação, registro de conclusão e de falta
- Sugestão de horários livres quando há conflito
- Lista de espera (fila FIFO) com indicação do primeiro da fila quando um horário é liberado
- Relatórios por período (consultas por status, taxa de faltas, procedimentos mais frequentes)
- Persistência em arquivos CSV (pasta `dados/`), um arquivo por tabela do modelo de dados
- Duas estratégias de detecção de conflitos: varredura da lista e árvore ordenada (`TreeMap`)

## Estrutura

```
src/main/java/br/edu/unisa/clinica
├── model        Pessoa, Paciente, Dentista, Expediente, Procedimento, Consulta,
│                StatusConsulta, EntradaListaEspera
├── conflito     DetectorConflitos (interface), DetectorPorVarredura, DetectorPorArvore
├── repository   ConsultaRepository (interface), ConsultaRepositoryMemoria, ArmazenamentoCsv
├── service      AgendamentoService, Cadastro, ListaEspera, Relatorios, Clinica, RegraNegocioException
├── ui           JanelaPrincipal, PainelPrincipal (Swing)
└── app          Main (interface gráfica), DemoConsole, DadosDemonstracao, Benchmark
src/test/java/...   testes JUnit 5
```

## Como executar

Requisitos: JDK 17 ou superior. Maven é opcional.

**Com Maven**
```
mvn test                                                          # executa todos os testes
mvn compile exec:java -Dexec.mainClass=br.edu.unisa.clinica.app.Main -Dexec.args="--demo"
mvn compile exec:java -Dexec.mainClass=br.edu.unisa.clinica.app.Benchmark
```

**Somente com o JDK** (a partir da pasta do projeto)
```
javac -encoding UTF-8 -d out $(find src/main -name "*.java")     # Linux/macOS
java -cp out br.edu.unisa.clinica.app.Main --demo                # abre a interface com dados fictícios
java -cp out br.edu.unisa.clinica.app.Benchmark                  # compara as duas estratégias
java -cp out br.edu.unisa.clinica.app.DemoConsole                # demonstração no console
```
No Windows (PowerShell), liste os arquivos com
`Get-ChildItem -Recurse src/main -Filter *.java | % FullName > fontes.txt` e use `javac -encoding UTF-8 -d out @fontes.txt`.

Sem `--demo`, o sistema abre vazio e os dados ficam na pasta `dados/`. Para usar outra pasta, informe
o caminho como argumento.

## Regras de negócio implementadas

- RN01 – a consulta ocupa [início, início + duração do procedimento)
- RN02 – sem sobreposição na agenda do mesmo dentista (consultas consecutivas são permitidas)
- RN03 – o paciente não pode ter duas consultas sobrepostas
- RN04 – não agendar no passado
- RN05 – somente dentro do expediente do dentista
- RN06 – o cancelamento libera o horário e indica o primeiro da lista de espera
- RN07 – pacientes e dentistas inativos não recebem agendamentos
