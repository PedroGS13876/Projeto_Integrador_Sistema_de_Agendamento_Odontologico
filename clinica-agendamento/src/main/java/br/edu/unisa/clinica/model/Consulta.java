package br.edu.unisa.clinica.model;

import java.time.LocalDateTime;

/**
 * Consulta agendada. Ocupa o intervalo entre o início e o fim, sendo o fim
 * calculado a partir da duração do procedimento (RN01).
 */
public class Consulta {

    private final int id;
    private final Paciente paciente;
    private final Dentista dentista;
    private final Procedimento procedimento;
    private LocalDateTime inicio;
    private LocalDateTime fim;
    private StatusConsulta status = StatusConsulta.AGENDADA;
    private String motivoCancelamento;

    public Consulta(int id, Paciente paciente, Dentista dentista,
                    Procedimento procedimento, LocalDateTime inicio) {
        this.id = id;
        this.paciente = paciente;
        this.dentista = dentista;
        this.procedimento = procedimento;
        definirHorario(inicio);
    }

    private void definirHorario(LocalDateTime novoInicio) {
        this.inicio = novoInicio;
        this.fim = novoInicio.plusMinutes(procedimento.getDuracaoMinutos());
    }

    public int getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public Dentista getDentista() { return dentista; }
    public Procedimento getProcedimento() { return procedimento; }
    public LocalDateTime getInicio() { return inicio; }
    public LocalDateTime getFim() { return fim; }
    public StatusConsulta getStatus() { return status; }
    public String getMotivoCancelamento() { return motivoCancelamento; }

    /**
     * Verifica se esta consulta se sobrepõe a outra (RN02).
     * Há conflito quando cada início ocorre antes do fim da outra; consultas
     * consecutivas (fim de uma igual ao início da outra) não conflitam.
     */
    public boolean sobrepoe(Consulta outra) {
        return this.inicio.isBefore(outra.fim) && outra.inicio.isBefore(this.fim);
    }

    /** Move a consulta para um novo início, recalculando o fim. */
    public void reagendar(LocalDateTime novoInicio) {
        exigirAgendada();
        definirHorario(novoInicio);
    }

    public void cancelar(String motivo) {
        exigirAgendada();
        this.status = StatusConsulta.CANCELADA;
        this.motivoCancelamento = motivo;
    }

    public void concluir() {
        exigirAgendada();
        this.status = StatusConsulta.CONCLUIDA;
    }

    public void registrarFalta() {
        exigirAgendada();
        this.status = StatusConsulta.FALTA;
    }

    /** Restaura o estado gravado em arquivo. Uso exclusivo da camada de armazenamento. */
    public void restaurar(StatusConsulta statusGravado, String motivo) {
        this.status = statusGravado;
        this.motivoCancelamento = motivo;
    }

    private void exigirAgendada() {
        if (status != StatusConsulta.AGENDADA) {
            throw new IllegalStateException("A consulta não está mais agendada: " + status);
        }
    }
}
