package br.edu.unisa.clinica.repository;

import br.edu.unisa.clinica.model.Consulta;
import br.edu.unisa.clinica.model.Dentista;
import br.edu.unisa.clinica.model.EntradaListaEspera;
import br.edu.unisa.clinica.model.Expediente;
import br.edu.unisa.clinica.model.Paciente;
import br.edu.unisa.clinica.model.Procedimento;
import br.edu.unisa.clinica.model.StatusConsulta;
import br.edu.unisa.clinica.service.AgendamentoService;
import br.edu.unisa.clinica.service.Cadastro;
import br.edu.unisa.clinica.service.ListaEspera;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistência em arquivos CSV (separador ponto e vírgula, codificação UTF-8).
 * Cada arquivo corresponde a uma tabela do modelo de dados: pacientes,
 * dentistas, expedientes, procedimentos, consultas e lista de espera.
 */
public class ArmazenamentoCsv {

    private final Path pasta;

    public ArmazenamentoCsv(Path pasta) {
        this.pasta = pasta;
    }

    // ------------------------------------------------------------- gravação

    public void salvar(Cadastro cadastro, ConsultaRepository consultas, ListaEspera espera)
            throws IOException {
        Files.createDirectories(pasta);

        List<String> pacientes = cabecalho("id;nome;cpf;telefone;email;data_nascimento;ativo");
        for (Paciente p : cadastro.listarPacientes()) {
            pacientes.add(String.join(";", String.valueOf(p.getId()), t(p.getNome()), t(p.getCpf()),
                    t(p.getTelefone()), t(p.getEmail()), String.valueOf(p.getDataNascimento()),
                    String.valueOf(p.isAtivo())));
        }
        escrever("pacientes.csv", pacientes);

        List<String> dentistas = cabecalho("id;nome;cro;especialidade;telefone;email;ativo");
        List<String> expedientes = cabecalho("dentista_id;dia_semana;hora_inicio;hora_fim");
        for (Dentista d : cadastro.listarDentistas()) {
            dentistas.add(String.join(";", String.valueOf(d.getId()), t(d.getNome()), t(d.getCro()),
                    t(d.getEspecialidade()), t(d.getTelefone()), t(d.getEmail()), String.valueOf(d.isAtivo())));
            for (Expediente e : d.getExpedientes()) {
                expedientes.add(String.join(";", String.valueOf(d.getId()), e.getDiaSemana().name(),
                        e.getHoraInicio().toString(), e.getHoraFim().toString()));
            }
        }
        escrever("dentistas.csv", dentistas);
        escrever("expedientes.csv", expedientes);

        List<String> procedimentos = cabecalho("id;nome;duracao_minutos;valor");
        for (Procedimento p : cadastro.listarProcedimentos()) {
            procedimentos.add(String.join(";", String.valueOf(p.getId()), t(p.getNome()),
                    String.valueOf(p.getDuracaoMinutos()), p.getValor().toPlainString()));
        }
        escrever("procedimentos.csv", procedimentos);

        List<String> linhasConsultas = cabecalho(
                "id;paciente_id;dentista_id;procedimento_id;data_hora_inicio;status;motivo_cancelamento");
        for (Consulta c : consultas.listarTodas()) {
            linhasConsultas.add(String.join(";", String.valueOf(c.getId()),
                    String.valueOf(c.getPaciente().getId()), String.valueOf(c.getDentista().getId()),
                    String.valueOf(c.getProcedimento().getId()), c.getInicio().toString(),
                    c.getStatus().name(), t(c.getMotivoCancelamento())));
        }
        escrever("consultas.csv", linhasConsultas);

        List<String> linhasEspera = cabecalho("id;paciente_id;procedimento_id;data_desejada;data_registro");
        for (EntradaListaEspera e : espera.listar()) {
            linhasEspera.add(String.join(";", String.valueOf(e.getId()),
                    String.valueOf(e.getPaciente().getId()), String.valueOf(e.getProcedimento().getId()),
                    e.getDataDesejada() == null ? "" : e.getDataDesejada().toString(),
                    e.getDataRegistro().toString()));
        }
        escrever("lista_espera.csv", linhasEspera);
    }

    // -------------------------------------------------------------- leitura

    /** Carrega os arquivos existentes. Se a pasta estiver vazia, nada é alterado. */
    public void carregar(Cadastro cadastro, AgendamentoService servico, ListaEspera espera)
            throws IOException {
        for (String[] c : ler("pacientes.csv")) {
            Paciente p = new Paciente(Integer.parseInt(c[0]), c[1], c[2], c[3], c[4],
                    LocalDate.parse(c[5]));
            p.setAtivo(Boolean.parseBoolean(c[6]));
            cadastro.restaurar(p);
        }
        for (String[] c : ler("dentistas.csv")) {
            Dentista d = new Dentista(Integer.parseInt(c[0]), c[1], c[2], c[3], c[4], c[5]);
            d.setAtivo(Boolean.parseBoolean(c[6]));
            cadastro.restaurar(d);
        }
        for (String[] c : ler("expedientes.csv")) {
            Dentista d = cadastro.buscarDentista(Integer.parseInt(c[0])).orElseThrow();
            d.adicionarExpediente(new Expediente(DayOfWeek.valueOf(c[1]),
                    LocalTime.parse(c[2]), LocalTime.parse(c[3])));
        }
        for (String[] c : ler("procedimentos.csv")) {
            cadastro.restaurar(new Procedimento(Integer.parseInt(c[0]), c[1],
                    Integer.parseInt(c[2]), new BigDecimal(c[3])));
        }
        for (String[] c : ler("consultas.csv")) {
            Consulta consulta = new Consulta(Integer.parseInt(c[0]),
                    cadastro.buscarPaciente(Integer.parseInt(c[1])).orElseThrow(),
                    cadastro.buscarDentista(Integer.parseInt(c[2])).orElseThrow(),
                    cadastro.buscarProcedimento(Integer.parseInt(c[3])).orElseThrow(),
                    LocalDateTime.parse(c[4]));
            consulta.restaurar(StatusConsulta.valueOf(c[5]), c.length > 6 ? c[6] : "");
            servico.registrarExistente(consulta);
        }
        for (String[] c : ler("lista_espera.csv")) {
            espera.restaurar(new EntradaListaEspera(Integer.parseInt(c[0]),
                    cadastro.buscarPaciente(Integer.parseInt(c[1])).orElseThrow(),
                    cadastro.buscarProcedimento(Integer.parseInt(c[2])).orElseThrow(),
                    c[3].isEmpty() ? null : LocalDate.parse(c[3]),
                    LocalDateTime.parse(c[4])));
        }
    }

    // ------------------------------------------------------------ utilitários

    private static List<String> cabecalho(String linha) {
        List<String> linhas = new ArrayList<>();
        linhas.add(linha);
        return linhas;
    }

    /** Remove separadores e quebras de linha que corromperiam o arquivo. */
    private static String t(String texto) {
        return texto == null ? "" : texto.replace(';', ',').replace('\r', ' ').replace('\n', ' ');
    }

    private void escrever(String nome, List<String> linhas) throws IOException {
        Files.write(pasta.resolve(nome), linhas, StandardCharsets.UTF_8);
    }

    private List<String[]> ler(String nome) throws IOException {
        Path arquivo = pasta.resolve(nome);
        List<String[]> registros = new ArrayList<>();
        if (!Files.exists(arquivo)) {
            return registros;
        }
        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        for (int i = 1; i < linhas.size(); i++) {      // a linha 0 é o cabeçalho
            if (!linhas.get(i).isBlank()) {
                registros.add(linhas.get(i).split(";", -1));
            }
        }
        return registros;
    }
}
