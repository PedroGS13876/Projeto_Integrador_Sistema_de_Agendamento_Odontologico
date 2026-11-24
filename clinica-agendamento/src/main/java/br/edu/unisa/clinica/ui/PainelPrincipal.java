package br.edu.unisa.clinica.ui;

import br.edu.unisa.clinica.model.Consulta;
import br.edu.unisa.clinica.model.Dentista;
import br.edu.unisa.clinica.model.EntradaListaEspera;
import br.edu.unisa.clinica.model.Paciente;
import br.edu.unisa.clinica.model.Pessoa;
import br.edu.unisa.clinica.model.Procedimento;
import br.edu.unisa.clinica.model.StatusConsulta;
import br.edu.unisa.clinica.service.Clinica;
import br.edu.unisa.clinica.service.RegraNegocioException;
import br.edu.unisa.clinica.service.Relatorios;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Tela principal do sistema, organizada em abas (agenda, cadastros, lista de espera e relatórios). */
public class PainelPrincipal extends JPanel {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Clinica clinica;
    private final JTabbedPane abas = new JTabbedPane();

    // Agenda
    private final JComboBox<Dentista> cbDentistaAgenda = new JComboBox<>();
    private final JTextField tfDataAgenda = new JTextField(9);
    private final JComboBox<Paciente> cbPacienteAgenda = new JComboBox<>();
    private final JComboBox<Procedimento> cbProcedimentoAgenda = new JComboBox<>();
    private final JTextField tfHoraAgenda = new JTextField("09:00", 5);
    private final DefaultTableModel modeloAgenda = modelo("Início", "Fim", "Paciente", "Procedimento", "Status");
    private final JTable tabelaAgenda = new JTable(modeloAgenda);
    private List<Consulta> agendaAtual = List.of();

    // Cadastros
    private final DefaultTableModel modeloPacientes =
            modelo("ID", "Nome", "CPF", "Telefone", "E-mail", "Nascimento", "Situação");
    private final JTable tabelaPacientes = new JTable(modeloPacientes);
    private final JTextField tfPNome = new JTextField(14), tfPCpf = new JTextField(10),
            tfPTelefone = new JTextField(10), tfPEmail = new JTextField(14), tfPNasc = new JTextField(8);

    private final DefaultTableModel modeloDentistas =
            modelo("ID", "Nome", "CRO", "Especialidade", "Telefone", "Situação");
    private final JTable tabelaDentistas = new JTable(modeloDentistas);
    private final JTextField tfDNome = new JTextField(14), tfDCro = new JTextField(8),
            tfDEspecialidade = new JTextField(12), tfDTelefone = new JTextField(10), tfDEmail = new JTextField(14);

    private final DefaultTableModel modeloProcedimentos = modelo("ID", "Procedimento", "Duração (min)", "Valor (R$)");
    private final JTable tabelaProcedimentos = new JTable(modeloProcedimentos);
    private final JTextField tfProcNome = new JTextField(16), tfProcDuracao = new JTextField(5),
            tfProcValor = new JTextField(7);

    // Lista de espera
    private final DefaultTableModel modeloEspera =
            modelo("Posição", "Paciente", "Procedimento", "Data desejada", "Registrado em");
    private final JTable tabelaEspera = new JTable(modeloEspera);
    private final JComboBox<Paciente> cbPacienteEspera = new JComboBox<>();
    private final JComboBox<Procedimento> cbProcedimentoEspera = new JComboBox<>();
    private final JTextField tfDataEspera = new JTextField(9);

    // Relatórios
    private final JTextField tfDe = new JTextField(9), tfAte = new JTextField(9);
    private final JTextArea areaRelatorio = new JTextArea();

    public PainelPrincipal(Clinica clinica) {
        super(new BorderLayout());
        this.clinica = clinica;
        abas.addTab("Agenda", criarAbaAgenda());
        abas.addTab("Pacientes", criarAbaPacientes());
        abas.addTab("Dentistas", criarAbaDentistas());
        abas.addTab("Procedimentos", criarAbaProcedimentos());
        abas.addTab("Lista de espera", criarAbaEspera());
        abas.addTab("Relatórios", criarAbaRelatorios());
        add(abas, BorderLayout.CENTER);

        LocalDate hoje = LocalDate.now(clinica.relogio());
        tfDataAgenda.setText(hoje.format(DATA));
        tfDe.setText(hoje.withDayOfMonth(1).format(DATA));
        tfAte.setText(hoje.withDayOfMonth(hoje.lengthOfMonth()).format(DATA));
        atualizarTudo();
    }

    public void selecionarAba(int indice) { abas.setSelectedIndex(indice); }

    // ------------------------------------------------------------------ Agenda

    private JComponent criarAbaAgenda() {
        JButton atualizar = new JButton("Atualizar");
        atualizar.addActionListener(e -> atualizarAgenda());
        cbDentistaAgenda.addActionListener(e -> atualizarAgenda());

        JPanel topo = linha(new JLabel("Dentista:"), cbDentistaAgenda, new JLabel("Data (dd/mm/aaaa):"),
                tfDataAgenda, atualizar);

        JButton agendar = new JButton("Agendar");
        agendar.addActionListener(e -> agendar());
        JButton cancelar = new JButton("Cancelar consulta");
        cancelar.addActionListener(e -> cancelar());
        JButton remarcar = new JButton("Remarcar");
        remarcar.addActionListener(e -> remarcar());
        JButton concluir = new JButton("Marcar concluída");
        concluir.addActionListener(e -> desfecho(StatusConsulta.CONCLUIDA));
        JButton falta = new JButton("Registrar falta");
        falta.addActionListener(e -> desfecho(StatusConsulta.FALTA));
        JButton livres = new JButton("Horários livres");
        livres.addActionListener(e -> mostrarHorariosLivres());

        JPanel novo = linha(new JLabel("Novo agendamento  Paciente:"), cbPacienteAgenda,
                new JLabel("Procedimento:"), cbProcedimentoAgenda, new JLabel("Hora:"), tfHoraAgenda, agendar);
        JPanel acoes = linha(cancelar, remarcar, concluir, falta, livres);

        JPanel sul = new JPanel();
        sul.setLayout(new BoxLayout(sul, BoxLayout.Y_AXIS));
        sul.add(novo);
        sul.add(acoes);

        JPanel aba = new JPanel(new BorderLayout());
        aba.add(topo, BorderLayout.NORTH);
        aba.add(rolagem(tabelaAgenda), BorderLayout.CENTER);
        aba.add(sul, BorderLayout.SOUTH);
        return aba;
    }

    private void atualizarAgenda() {
        modeloAgenda.setRowCount(0);
        Dentista dentista = (Dentista) cbDentistaAgenda.getSelectedItem();
        LocalDate dia = lerData(tfDataAgenda.getText(), false);
        if (dentista == null || dia == null) {
            agendaAtual = List.of();
            return;
        }
        List<Consulta> doDia = new java.util.ArrayList<>(
                clinica.consultas().buscarPorDentistaEDia(dentista.getId(), dia));
        doDia.sort((x, y) -> x.getInicio().compareTo(y.getInicio()));
        agendaAtual = doDia;
        for (Consulta c : agendaAtual) {
            modeloAgenda.addRow(new Object[] {c.getInicio().format(HORA), c.getFim().format(HORA),
                    c.getPaciente().getNome(), c.getProcedimento().getNome(), rotulo(c.getStatus())});
        }
    }

    private void agendar() {
        Dentista dentista = (Dentista) cbDentistaAgenda.getSelectedItem();
        Paciente paciente = (Paciente) cbPacienteAgenda.getSelectedItem();
        Procedimento procedimento = (Procedimento) cbProcedimentoAgenda.getSelectedItem();
        LocalDate dia = lerData(tfDataAgenda.getText(), true);
        LocalTime hora = lerHora(tfHoraAgenda.getText());
        if (dentista == null || paciente == null || procedimento == null || dia == null || hora == null) {
            aviso("Selecione dentista, paciente e procedimento e informe data e hora válidas.");
            return;
        }
        try {
            Consulta c = clinica.agendamento().agendar(paciente, dentista, procedimento, dia.atTime(hora));
            clinica.salvar();
            atualizarTudo();
            info("Consulta agendada: " + c.getInicio().format(DATA_HORA) + " às " + c.getFim().format(HORA) + ".");
        } catch (RegraNegocioException ex) {
            String sugestao = sugerir(dentista, dia, procedimento);
            aviso(ex.getMessage() + sugestao);
        }
    }

    private String sugerir(Dentista dentista, LocalDate dia, Procedimento procedimento) {
        List<LocalDateTime> livres = clinica.agendamento().horariosLivres(dentista, dia, procedimento, 30);
        if (livres.isEmpty()) {
            return "\nNão há horários livres nesse dia para o procedimento.";
        }
        StringBuilder sb = new StringBuilder("\nHorários livres sugeridos: ");
        for (int i = 0; i < Math.min(6, livres.size()); i++) {
            sb.append(i == 0 ? "" : ", ").append(livres.get(i).format(HORA));
        }
        return sb.toString();
    }

    private void cancelar() {
        Consulta c = consultaSelecionada();
        if (c == null) { return; }
        String motivo = JOptionPane.showInputDialog(this, "Motivo do cancelamento:");
        if (motivo == null) { return; }
        try {
            clinica.agendamento().cancelar(c.getId(), motivo);
            clinica.salvar();
            atualizarTudo();
            Optional<EntradaListaEspera> candidato = clinica.candidatoAoHorarioLiberado(c);
            if (candidato.isPresent()) {
                info("Horário liberado. Primeiro da lista de espera para este procedimento: "
                        + candidato.get().getPaciente().getNome() + ".");
            }
        } catch (RegraNegocioException ex) {
            aviso(ex.getMessage());
        }
    }

    private void remarcar() {
        Consulta c = consultaSelecionada();
        if (c == null) { return; }
        String texto = JOptionPane.showInputDialog(this, "Novo horário (dd/mm/aaaa hh:mm):",
                c.getInicio().format(DATA_HORA));
        if (texto == null) { return; }
        try {
            LocalDateTime novo = LocalDateTime.parse(texto.trim(), DATA_HORA);
            clinica.agendamento().remarcar(c.getId(), novo);
            clinica.salvar();
            atualizarTudo();
        } catch (DateTimeParseException ex) {
            aviso("Horário inválido. Use o formato dd/mm/aaaa hh:mm.");
        } catch (RegraNegocioException ex) {
            aviso(ex.getMessage());
        }
    }

    private void desfecho(StatusConsulta status) {
        Consulta c = consultaSelecionada();
        if (c == null) { return; }
        try {
            clinica.agendamento().registrarDesfecho(c.getId(), status);
            clinica.salvar();
            atualizarTudo();
        } catch (RegraNegocioException ex) {
            aviso(ex.getMessage());
        }
    }

    private void mostrarHorariosLivres() {
        Dentista dentista = (Dentista) cbDentistaAgenda.getSelectedItem();
        Procedimento procedimento = (Procedimento) cbProcedimentoAgenda.getSelectedItem();
        LocalDate dia = lerData(tfDataAgenda.getText(), true);
        if (dentista == null || procedimento == null || dia == null) {
            aviso("Selecione dentista e procedimento e informe uma data válida.");
            return;
        }
        List<LocalDateTime> livres = clinica.agendamento().horariosLivres(dentista, dia, procedimento, 30);
        DefaultComboBoxModel<String> modelo = new DefaultComboBoxModel<>();
        for (LocalDateTime l : livres) {
            modelo.addElement(l.format(HORA));
        }
        JList<String> lista = new JList<>(modelo);
        JScrollPane painel = new JScrollPane(lista);
        painel.setPreferredSize(new Dimension(120, 180));
        JOptionPane.showMessageDialog(this, painel, "Horários livres", JOptionPane.PLAIN_MESSAGE);
    }

    private Consulta consultaSelecionada() {
        int linha = tabelaAgenda.getSelectedRow();
        if (linha < 0 || linha >= agendaAtual.size()) {
            aviso("Selecione uma consulta na tabela.");
            return null;
        }
        return agendaAtual.get(linha);
    }

    // --------------------------------------------------------------- Cadastros

    private JComponent criarAbaPacientes() {
        JButton cadastrar = new JButton("Cadastrar");
        cadastrar.addActionListener(e -> {
            try {
                LocalDate nasc = lerData(tfPNasc.getText(), true);
                if (nasc == null) { aviso("Data de nascimento inválida (dd/mm/aaaa)."); return; }
                clinica.cadastro().cadastrarPaciente(tfPNome.getText(), tfPCpf.getText(),
                        tfPTelefone.getText(), tfPEmail.getText(), nasc);
                limpar(tfPNome, tfPCpf, tfPTelefone, tfPEmail, tfPNasc);
                clinica.salvar();
                atualizarTudo();
            } catch (RegraNegocioException ex) {
                aviso(ex.getMessage());
            }
        });
        JButton alternar = new JButton("Ativar/inativar selecionado");
        alternar.addActionListener(e -> {
            int l = tabelaPacientes.getSelectedRow();
            if (l >= 0) {
                Paciente p = clinica.cadastro().listarPacientes().get(l);
                p.setAtivo(!p.isAtivo());
                clinica.salvar();
                atualizarTudo();
            }
        });
        return abaDeCadastro(tabelaPacientes,
                linha(new JLabel("Nome:"), tfPNome, new JLabel("CPF:"), tfPCpf, new JLabel("Telefone:"), tfPTelefone),
                linha(new JLabel("E-mail:"), tfPEmail, new JLabel("Nascimento:"), tfPNasc, cadastrar, alternar));
    }

    private JComponent criarAbaDentistas() {
        JButton cadastrar = new JButton("Cadastrar");
        cadastrar.addActionListener(e -> {
            try {
                clinica.cadastro().cadastrarDentista(tfDNome.getText(), tfDCro.getText(),
                        tfDEspecialidade.getText(), tfDTelefone.getText(), tfDEmail.getText());
                limpar(tfDNome, tfDCro, tfDEspecialidade, tfDTelefone, tfDEmail);
                clinica.salvar();
                atualizarTudo();
            } catch (RegraNegocioException ex) {
                aviso(ex.getMessage());
            }
        });
        JButton alternar = new JButton("Ativar/inativar selecionado");
        alternar.addActionListener(e -> {
            int l = tabelaDentistas.getSelectedRow();
            if (l >= 0) {
                Dentista d = clinica.cadastro().listarDentistas().get(l);
                d.setAtivo(!d.isAtivo());
                clinica.salvar();
                atualizarTudo();
            }
        });
        return abaDeCadastro(tabelaDentistas,
                linha(new JLabel("Nome:"), tfDNome, new JLabel("CRO:"), tfDCro,
                        new JLabel("Especialidade:"), tfDEspecialidade),
                linha(new JLabel("Telefone:"), tfDTelefone, new JLabel("E-mail:"), tfDEmail, cadastrar, alternar));
    }

    private JComponent criarAbaProcedimentos() {
        JButton cadastrar = new JButton("Cadastrar");
        cadastrar.addActionListener(e -> {
            try {
                int duracao = Integer.parseInt(tfProcDuracao.getText().trim());
                BigDecimal valor = new BigDecimal(tfProcValor.getText().trim().replace(',', '.'));
                clinica.cadastro().cadastrarProcedimento(tfProcNome.getText(), duracao, valor);
                limpar(tfProcNome, tfProcDuracao, tfProcValor);
                clinica.salvar();
                atualizarTudo();
            } catch (NumberFormatException ex) {
                aviso("Informe a duração (minutos) e o valor com números válidos.");
            } catch (RegraNegocioException | IllegalArgumentException ex) {
                aviso(ex.getMessage());
            }
        });
        return abaDeCadastro(tabelaProcedimentos,
                linha(new JLabel("Nome:"), tfProcNome, new JLabel("Duração (min):"), tfProcDuracao,
                        new JLabel("Valor (R$):"), tfProcValor, cadastrar));
    }

    private JComponent abaDeCadastro(JTable tabela, JComponent... linhas) {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        for (JComponent l : linhas) { form.add(l); }
        JPanel aba = new JPanel(new BorderLayout());
        aba.add(rolagem(tabela), BorderLayout.CENTER);
        aba.add(form, BorderLayout.SOUTH);
        return aba;
    }

    // ---------------------------------------------------------- Lista de espera

    private JComponent criarAbaEspera() {
        JButton adicionar = new JButton("Adicionar à lista");
        adicionar.addActionListener(e -> {
            Paciente p = (Paciente) cbPacienteEspera.getSelectedItem();
            Procedimento proc = (Procedimento) cbProcedimentoEspera.getSelectedItem();
            LocalDate data = tfDataEspera.getText().isBlank() ? null : lerData(tfDataEspera.getText(), true);
            if (p == null || proc == null || (data == null && !tfDataEspera.getText().isBlank())) {
                aviso("Selecione paciente e procedimento. A data desejada é opcional (dd/mm/aaaa).");
                return;
            }
            clinica.listaEspera().adicionar(p, proc, data, LocalDateTime.now(clinica.relogio()));
            tfDataEspera.setText("");
            clinica.salvar();
            atualizarTudo();
        });
        JButton remover = new JButton("Remover selecionado");
        remover.addActionListener(e -> {
            int l = tabelaEspera.getSelectedRow();
            if (l >= 0) {
                clinica.listaEspera().remover(clinica.listaEspera().listar().get(l).getId());
                clinica.salvar();
                atualizarTudo();
            }
        });
        return abaDeCadastro(tabelaEspera,
                linha(new JLabel("Paciente:"), cbPacienteEspera, new JLabel("Procedimento:"), cbProcedimentoEspera,
                        new JLabel("Data desejada (opcional):"), tfDataEspera, adicionar, remover));
    }

    // -------------------------------------------------------------- Relatórios

    private JComponent criarAbaRelatorios() {
        JButton gerar = new JButton("Gerar relatório");
        gerar.addActionListener(e -> gerarRelatorio());
        areaRelatorio.setEditable(false);
        areaRelatorio.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        areaRelatorio.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel aba = new JPanel(new BorderLayout());
        aba.add(linha(new JLabel("De:"), tfDe, new JLabel("Até:"), tfAte, gerar), BorderLayout.NORTH);
        aba.add(new JScrollPane(areaRelatorio), BorderLayout.CENTER);
        return aba;
    }

    private void gerarRelatorio() {
        LocalDate de = lerData(tfDe.getText(), true);
        LocalDate ate = lerData(tfAte.getText(), true);
        if (de == null || ate == null || ate.isBefore(de)) {
            aviso("Informe um período válido (dd/mm/aaaa).");
            return;
        }
        List<Consulta> doPeriodo = Relatorios.doPeriodo(clinica.consultas().listarTodas(), de, ate);
        Map<StatusConsulta, Integer> porStatus = Relatorios.porStatus(doPeriodo);
        StringBuilder sb = new StringBuilder();
        sb.append("Período: ").append(de.format(DATA)).append(" a ").append(ate.format(DATA)).append("\n\n");
        sb.append("Total de consultas: ").append(doPeriodo.size()).append("\n");
        for (StatusConsulta s : StatusConsulta.values()) {
            sb.append(String.format("  %-10s %d%n", rotulo(s), porStatus.get(s)));
        }
        sb.append(String.format("%nTaxa de faltas (concluídas e faltas): %.1f%%%n",
                Relatorios.taxaDeFaltas(doPeriodo) * 100));
        sb.append("\nProcedimentos mais frequentes (sem canceladas):\n");
        for (Map.Entry<String, Integer> e : Relatorios.procedimentosMaisFrequentes(doPeriodo)) {
            sb.append(String.format("  %-28s %d%n", e.getKey(), e.getValue()));
        }
        areaRelatorio.setText(sb.toString());
        areaRelatorio.setCaretPosition(0);
    }

    // ------------------------------------------------------------- Atualização

    /** Recarrega combos e tabelas a partir dos dados da clínica. */
    public void atualizarTudo() {
        List<Dentista> dentistasAtivos = clinica.cadastro().listarDentistas().stream()
                .filter(Pessoa::isAtivo).toList();
        List<Paciente> pacientesAtivos = clinica.cadastro().listarPacientes().stream()
                .filter(Pessoa::isAtivo).toList();
        List<Procedimento> procedimentos = clinica.cadastro().listarProcedimentos();

        Object dentistaSel = cbDentistaAgenda.getSelectedItem();
        cbDentistaAgenda.setModel(new DefaultComboBoxModel<>(dentistasAtivos.toArray(new Dentista[0])));
        if (dentistaSel != null && dentistasAtivos.contains(dentistaSel)) {
            cbDentistaAgenda.setSelectedItem(dentistaSel);
        }
        cbPacienteAgenda.setModel(new DefaultComboBoxModel<>(pacientesAtivos.toArray(new Paciente[0])));
        cbProcedimentoAgenda.setModel(new DefaultComboBoxModel<>(procedimentos.toArray(new Procedimento[0])));
        cbPacienteEspera.setModel(new DefaultComboBoxModel<>(pacientesAtivos.toArray(new Paciente[0])));
        cbProcedimentoEspera.setModel(new DefaultComboBoxModel<>(procedimentos.toArray(new Procedimento[0])));

        modeloPacientes.setRowCount(0);
        for (Paciente p : clinica.cadastro().listarPacientes()) {
            modeloPacientes.addRow(new Object[] {p.getId(), p.getNome(), p.getCpf(), p.getTelefone(), p.getEmail(),
                    p.getDataNascimento().format(DATA), p.isAtivo() ? "Ativo" : "Inativo"});
        }
        modeloDentistas.setRowCount(0);
        for (Dentista d : clinica.cadastro().listarDentistas()) {
            modeloDentistas.addRow(new Object[] {d.getId(), d.getNome(), d.getCro(), d.getEspecialidade(),
                    d.getTelefone(), d.isAtivo() ? "Ativo" : "Inativo"});
        }
        modeloProcedimentos.setRowCount(0);
        for (Procedimento p : procedimentos) {
            modeloProcedimentos.addRow(new Object[] {p.getId(), p.getNome(), p.getDuracaoMinutos(),
                    p.getValor().toPlainString()});
        }
        modeloEspera.setRowCount(0);
        int posicao = 1;
        for (EntradaListaEspera e : clinica.listaEspera().listar()) {
            modeloEspera.addRow(new Object[] {posicao++, e.getPaciente().getNome(), e.getProcedimento().getNome(),
                    e.getDataDesejada() == null ? "—" : e.getDataDesejada().format(DATA),
                    e.getDataRegistro().format(DATA_HORA)});
        }
        atualizarAgenda();
    }

    // --------------------------------------------------------------- Utilitários

    /** Cria a área de rolagem da tabela, garantindo que o cabeçalho das colunas apareça. */
    private static JScrollPane rolagem(JTable tabela) {
        tabela.setRowHeight(24);
        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setColumnHeaderView(tabela.getTableHeader());
        return rolagem;
    }

    private static DefaultTableModel modelo(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private static JPanel linha(JComponent... componentes) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        for (JComponent c : componentes) { p.add(c); }
        return p;
    }

    private static void limpar(JTextField... campos) {
        for (JTextField c : campos) { c.setText(""); }
    }

    private LocalDate lerData(String texto, boolean avisar) {
        try {
            return LocalDate.parse(texto.trim(), DATA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private LocalTime lerHora(String texto) {
        try {
            return LocalTime.parse(texto.trim(), HORA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String rotulo(StatusConsulta s) {
        return switch (s) {
            case AGENDADA -> "Agendada";
            case CONCLUIDA -> "Concluída";
            case CANCELADA -> "Cancelada";
            case FALTA -> "Falta";
        };
    }

    private void aviso(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    private void info(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Informação", JOptionPane.INFORMATION_MESSAGE);
    }
}
