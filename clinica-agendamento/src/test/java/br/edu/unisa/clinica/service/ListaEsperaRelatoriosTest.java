package br.edu.unisa.clinica.service;

import br.edu.unisa.clinica.model.Consulta;
import br.edu.unisa.clinica.model.Dentista;
import br.edu.unisa.clinica.model.EntradaListaEspera;
import br.edu.unisa.clinica.model.Paciente;
import br.edu.unisa.clinica.model.Procedimento;
import br.edu.unisa.clinica.model.StatusConsulta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListaEsperaRelatoriosTest {

    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate TERCA = LocalDate.of(2026, 11, 24);

    private Clinica clinica;
    private Dentista dentista;
    private Paciente ana;
    private Paciente bruno;
    private Procedimento restauracao;
    private Procedimento profilaxia;

    @BeforeEach
    void preparar() {
        Clock relogio = Clock.fixed(LocalDateTime.of(2026, 11, 23, 7, 0).atZone(ZONA).toInstant(), ZONA);
        clinica = Clinica.abrir(null, new br.edu.unisa.clinica.conflito.DetectorPorArvore(), relogio);
        dentista = clinica.cadastro().cadastrarDentista("Dentista Fictício", "CRO-0001", "Clínica geral", "", "");
        ana = clinica.cadastro().cadastrarPaciente("Ana Fictícia", "000", "", "", LocalDate.of(1990, 1, 1));
        bruno = clinica.cadastro().cadastrarPaciente("Bruno Fictício", "001", "", "", LocalDate.of(1985, 1, 1));
        restauracao = clinica.cadastro().cadastrarProcedimento("Restauração", 60, new BigDecimal("180.00"));
        profilaxia = clinica.cadastro().cadastrarProcedimento("Profilaxia", 45, new BigDecimal("120.00"));
    }

    @Test
    void filaRespeitaAOrdemDeChegadaEOProcedimento() {
        LocalDateTime agora = LocalDateTime.of(2026, 11, 23, 7, 0);
        EntradaListaEspera primeiro = clinica.listaEspera().adicionar(ana, restauracao, TERCA, agora);
        clinica.listaEspera().adicionar(bruno, restauracao, TERCA, agora.plusMinutes(5));
        clinica.listaEspera().adicionar(bruno, profilaxia, null, agora.plusMinutes(10));

        assertEquals(primeiro.getId(), clinica.listaEspera().proximo(restauracao, TERCA).get().getId());
        assertEquals(bruno.getId(), clinica.listaEspera().proximo(profilaxia, TERCA).get().getPaciente().getId());
        assertFalse(clinica.listaEspera().proximo(restauracao, TERCA.plusDays(1)).isPresent());
    }

    @Test
    void cancelamentoIndicaOPrimeiroDaFila() {
        Consulta c = clinica.agendamento().agendar(ana, dentista, restauracao, TERCA.atTime(9, 0));
        clinica.listaEspera().adicionar(bruno, restauracao, TERCA, LocalDateTime.of(2026, 11, 23, 7, 0));
        clinica.agendamento().cancelar(c.getId(), "teste");
        assertEquals(bruno.getId(), clinica.candidatoAoHorarioLiberado(c).get().getPaciente().getId());
    }

    @Test
    void relatorioContaStatusEFaltas() {
        Consulta c1 = clinica.agendamento().agendar(ana, dentista, restauracao, TERCA.atTime(9, 0));
        Consulta c2 = clinica.agendamento().agendar(bruno, dentista, profilaxia, TERCA.atTime(10, 30));
        Consulta c3 = clinica.agendamento().agendar(ana, dentista, restauracao, TERCA.atTime(14, 0));
        Consulta c4 = clinica.agendamento().agendar(bruno, dentista, restauracao, TERCA.atTime(16, 0));
        clinica.agendamento().registrarDesfecho(c1.getId(), StatusConsulta.CONCLUIDA);
        clinica.agendamento().registrarDesfecho(c2.getId(), StatusConsulta.FALTA);
        clinica.agendamento().registrarDesfecho(c3.getId(), StatusConsulta.CONCLUIDA);
        clinica.agendamento().cancelar(c4.getId(), "teste");

        List<Consulta> periodo = Relatorios.doPeriodo(clinica.consultas().listarTodas(), TERCA, TERCA);
        assertEquals(4, periodo.size());
        assertEquals(2, Relatorios.porStatus(periodo).get(StatusConsulta.CONCLUIDA));
        assertEquals(1, Relatorios.porStatus(periodo).get(StatusConsulta.CANCELADA));
        assertTrue(Math.abs(Relatorios.taxaDeFaltas(periodo) - 1.0 / 3.0) < 1e-9);
        assertEquals("Restauração", Relatorios.procedimentosMaisFrequentes(periodo).get(0).getKey());
        assertEquals(0, Relatorios.doPeriodo(clinica.consultas().listarTodas(), TERCA.plusDays(1), TERCA.plusDays(2)).size());
    }
}
