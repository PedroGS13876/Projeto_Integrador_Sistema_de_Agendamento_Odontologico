package br.edu.unisa.clinica.model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Faixa de atendimento de um dentista em um dia da semana
 * (por exemplo, segunda-feira das 8h às 12h).
 */
public class Expediente {

    private final DayOfWeek diaSemana;
    private final LocalTime horaInicio;
    private final LocalTime horaFim;

    public Expediente(DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFim) {
        if (!horaInicio.isBefore(horaFim)) {
            throw new IllegalArgumentException("A hora de início deve ser anterior à hora de fim.");
        }
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    public DayOfWeek getDiaSemana() { return diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }

    /** Indica se o intervalo [inicio, fim] cabe inteiramente nesta faixa de atendimento. */
    public boolean cobre(LocalDateTime inicio, LocalDateTime fim) {
        return inicio.getDayOfWeek() == diaSemana
                && inicio.toLocalDate().equals(fim.toLocalDate())
                && !inicio.toLocalTime().isBefore(horaInicio)
                && !fim.toLocalTime().isAfter(horaFim);
    }
}
