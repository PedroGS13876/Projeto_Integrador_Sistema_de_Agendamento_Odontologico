package br.edu.unisa.clinica.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Registro de um paciente que aguarda um horário para determinado procedimento. */
public class EntradaListaEspera {

    private final int id;
    private final Paciente paciente;
    private final Procedimento procedimento;
    private final LocalDate dataDesejada;
    private final LocalDateTime dataRegistro;

    public EntradaListaEspera(int id, Paciente paciente, Procedimento procedimento,
                              LocalDate dataDesejada, LocalDateTime dataRegistro) {
        this.id = id;
        this.paciente = paciente;
        this.procedimento = procedimento;
        this.dataDesejada = dataDesejada;
        this.dataRegistro = dataRegistro;
    }

    public int getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public Procedimento getProcedimento() { return procedimento; }
    public LocalDate getDataDesejada() { return dataDesejada; }
    public LocalDateTime getDataRegistro() { return dataRegistro; }
}
