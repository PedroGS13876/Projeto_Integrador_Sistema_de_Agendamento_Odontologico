package br.edu.unisa.clinica.service;

import br.edu.unisa.clinica.model.Dentista;
import br.edu.unisa.clinica.model.Expediente;
import br.edu.unisa.clinica.model.Paciente;
import br.edu.unisa.clinica.model.Procedimento;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Cadastro de pacientes, dentistas e procedimentos (RF01 a RF03). */
public class Cadastro {

    private final Map<Integer, Paciente> pacientes = new LinkedHashMap<>();
    private final Map<Integer, Dentista> dentistas = new LinkedHashMap<>();
    private final Map<Integer, Procedimento> procedimentos = new LinkedHashMap<>();

    public Paciente cadastrarPaciente(String nome, String cpf, String telefone,
                                      String email, LocalDate nascimento) {
        exigirTexto(nome, "nome");
        Paciente p = new Paciente(proximo(pacientes.keySet()), nome.trim(), cpf, telefone, email, nascimento);
        pacientes.put(p.getId(), p);
        return p;
    }

    /** Cadastra o dentista com o expediente padrão: segunda a sexta, 8h–12h e 13h–18h. */
    public Dentista cadastrarDentista(String nome, String cro, String especialidade,
                                      String telefone, String email) {
        exigirTexto(nome, "nome");
        Dentista d = new Dentista(proximo(dentistas.keySet()), nome.trim(), cro, especialidade, telefone, email);
        for (DayOfWeek dia : new DayOfWeek[] {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}) {
            d.adicionarExpediente(new Expediente(dia, LocalTime.of(8, 0), LocalTime.of(12, 0)));
            d.adicionarExpediente(new Expediente(dia, LocalTime.of(13, 0), LocalTime.of(18, 0)));
        }
        dentistas.put(d.getId(), d);
        return d;
    }

    public Procedimento cadastrarProcedimento(String nome, int duracaoMinutos, BigDecimal valor) {
        exigirTexto(nome, "nome");
        Procedimento p = new Procedimento(proximo(procedimentos.keySet()), nome.trim(), duracaoMinutos, valor);
        procedimentos.put(p.getId(), p);
        return p;
    }

    /** Inclui um objeto lido do armazenamento, mantendo seu identificador. */
    public void restaurar(Paciente p) { pacientes.put(p.getId(), p); }
    public void restaurar(Dentista d) { dentistas.put(d.getId(), d); }
    public void restaurar(Procedimento p) { procedimentos.put(p.getId(), p); }

    public Optional<Paciente> buscarPaciente(int id) { return Optional.ofNullable(pacientes.get(id)); }
    public Optional<Dentista> buscarDentista(int id) { return Optional.ofNullable(dentistas.get(id)); }
    public Optional<Procedimento> buscarProcedimento(int id) { return Optional.ofNullable(procedimentos.get(id)); }

    public List<Paciente> listarPacientes() { return new ArrayList<>(pacientes.values()); }
    public List<Dentista> listarDentistas() { return new ArrayList<>(dentistas.values()); }
    public List<Procedimento> listarProcedimentos() { return new ArrayList<>(procedimentos.values()); }

    private static int proximo(java.util.Set<Integer> ids) {
        int max = 0;
        for (int id : ids) {
            max = Math.max(max, id);
        }
        return max + 1;
    }

    private static void exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException("Informe o campo: " + campo + ".");
        }
    }
}
