package br.edu.unisa.clinica.service;

import br.edu.unisa.clinica.conflito.DetectorConflitos;
import br.edu.unisa.clinica.model.Consulta;
import br.edu.unisa.clinica.model.Dentista;
import br.edu.unisa.clinica.model.Expediente;
import br.edu.unisa.clinica.model.Paciente;
import br.edu.unisa.clinica.model.Procedimento;
import br.edu.unisa.clinica.model.StatusConsulta;
import br.edu.unisa.clinica.repository.ConsultaRepositoryMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Casos de teste das regras de agendamento. A classe é abstrata para que os
 * mesmos testes sejam executados com as duas estratégias de detecção de conflitos.
 * "Agora" é fixado em segunda-feira, 23/11/2026, 07:00; os agendamentos usam terça-feira, 24/11/2026.
 */
public abstract class AgendamentoServiceTestBase {

    protected abstract DetectorConflitos criarDetector();

    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate TERCA = LocalDate.of(2026, 11, 24);

    private AgendamentoService servico;
    private Dentista dentista;
    private Dentista outroDentista;
    private Paciente paciente;
    private Paciente outroPaciente;
    private Procedimento restauracao;   // 60 min

    @BeforeEach
    void preparar() {
        Clock relogio = Clock.fixed(
                LocalDateTime.of(2026, 11, 23, 7, 0).atZone(ZONA).toInstant(), ZONA);
        servico = new AgendamentoService(new ConsultaRepositoryMemoria(), criarDetector(), relogio);

        dentista = novoDentista(1);
        outroDentista = novoDentista(2);
        paciente = new Paciente(1, "Paciente Fictício 1", "000.000.000-01", "", "", LocalDate.of(1990, 1, 1));
        outroPaciente = new Paciente(2, "Paciente Fictício 2", "000.000.000-02", "", "", LocalDate.of(1985, 1, 1));
        restauracao = new Procedimento(1, "Restauração", 60, new BigDecimal("150.00"));
    }

    private static Dentista novoDentista(int id) {
        Dentista d = new Dentista(id, "Dentista Fictício " + id, "CRO-000" + id, "Clínica geral", "", "");
        for (DayOfWeek dia : DayOfWeek.values()) {
            if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
                continue;
            }
            d.adicionarExpediente(new Expediente(dia, LocalTime.of(8, 0), LocalTime.of(12, 0)));
            d.adicionarExpediente(new Expediente(dia, LocalTime.of(13, 0), LocalTime.of(18, 0)));
        }
        return d;
    }

    private LocalDateTime terca(int hora, int minuto) {
        return TERCA.atTime(hora, minuto);
    }

    @Test
    void ct01_agendaHorarioLivre() {
        Consulta c = servico.agendar(paciente, dentista, restauracao, terca(9, 0));
        assertEquals(StatusConsulta.AGENDADA, c.getStatus());
        assertEquals(terca(10, 0), c.getFim());
    }

    @Test
    void ct02_rejeitaMesmoHorarioDoMesmoDentista() {
        servico.agendar(paciente, dentista, restauracao, terca(9, 0));
        assertThrows(RegraNegocioException.class,
                () -> servico.agendar(outroPaciente, dentista, restauracao, terca(9, 0)));
    }

    @Test
    void ct03_rejeitaSobreposicaoParcial() {
        servico.agendar(paciente, dentista, restauracao, terca(9, 0));
        assertThrows(RegraNegocioException.class,
                () -> servico.agendar(outroPaciente, dentista, restauracao, terca(9, 30)));
        assertThrows(RegraNegocioException.class,
                () -> servico.agendar(outroPaciente, dentista, restauracao, terca(8, 30)));
    }

    @Test
    void ct04_aceitaConsultasConsecutivas() {
        servico.agendar(paciente, dentista, restauracao, terca(9, 0));
        servico.agendar(outroPaciente, dentista, restauracao, terca(10, 0));
        servico.agendar(outroPaciente, dentista, restauracao, terca(8, 0));
    }

    @Test
    void ct05_rejeitaDataNoPassado() {
        assertThrows(RegraNegocioException.class,
                () -> servico.agendar(paciente, dentista, restauracao, LocalDateTime.of(2026, 11, 23, 6, 0)));
    }

    @Test
    void ct06_rejeitaForaDoExpediente() {
        assertThrows(RegraNegocioException.class,
                () -> servico.agendar(paciente, dentista, restauracao, terca(7, 30)));   // antes de abrir
        assertThrows(RegraNegocioException.class,
                () -> servico.agendar(paciente, dentista, restauracao, terca(12, 0)));   // intervalo de almoço
        assertThrows(RegraNegocioException.class,
                () -> servico.agendar(paciente, dentista, restauracao, terca(17, 30)));  // termina após o fechamento
    }

    @Test
    void ct07_rejeitaPacienteComConsultaSimultaneaEmOutroDentista() {
        servico.agendar(paciente, dentista, restauracao, terca(9, 0));
        assertThrows(RegraNegocioException.class,
                () -> servico.agendar(paciente, outroDentista, restauracao, terca(9, 30)));
    }

    @Test
    void ct08_cancelaEReagendaNoMesmoHorario() {
        Consulta c = servico.agendar(paciente, dentista, restauracao, terca(9, 0));
        servico.cancelar(c.getId(), "Paciente desistiu");
        assertEquals(StatusConsulta.CANCELADA, c.getStatus());
        servico.agendar(outroPaciente, dentista, restauracao, terca(9, 0));
    }

    @Test
    void ct09_remarcarParaHorarioOcupadoMantemOriginal() {
        Consulta primeira = servico.agendar(paciente, dentista, restauracao, terca(9, 0));
        servico.agendar(outroPaciente, dentista, restauracao, terca(11, 0));

        assertThrows(RegraNegocioException.class, () -> servico.remarcar(primeira.getId(), terca(11, 30)));
        assertEquals(terca(9, 0), primeira.getInicio());
        // o horário original continua bloqueado
        assertThrows(RegraNegocioException.class,
                () -> servico.agendar(outroPaciente, dentista, restauracao, terca(9, 0)));
    }

    @Test
    void ct09b_remarcarParaHorarioLivreLiberaOAntigo() {
        Consulta c = servico.agendar(paciente, dentista, restauracao, terca(9, 0));
        servico.remarcar(c.getId(), terca(14, 0));
        assertEquals(terca(14, 0), c.getInicio());
        servico.agendar(outroPaciente, dentista, restauracao, terca(9, 0));   // horário antigo ficou livre
    }

    @Test
    void ct10_sugereHorariosLivresSemOsOcupados() {
        servico.agendar(paciente, dentista, restauracao, terca(9, 0));
        List<LocalDateTime> livres = servico.horariosLivres(dentista, TERCA, restauracao, 30);
        assertTrue(livres.contains(terca(8, 0)));
        assertFalse(livres.contains(terca(8, 30)));   // terminaria às 9h30, dentro da consulta das 9h
        assertFalse(livres.contains(terca(9, 0)));
        assertFalse(livres.contains(terca(9, 30)));
        assertTrue(livres.contains(terca(10, 0)));
    }

    @Test
    void ct11_rejeitaPacienteOuDentistaInativo() {
        paciente.setAtivo(false);
        assertThrows(RegraNegocioException.class,
                () -> servico.agendar(paciente, dentista, restauracao, terca(9, 0)));
        paciente.setAtivo(true);
        dentista.setAtivo(false);
        assertThrows(RegraNegocioException.class,
                () -> servico.agendar(paciente, dentista, restauracao, terca(9, 0)));
    }
}
