package br.edu.unisa.clinica.repository;

import br.edu.unisa.clinica.app.DadosDemonstracao;
import br.edu.unisa.clinica.conflito.DetectorPorArvore;
import br.edu.unisa.clinica.model.Consulta;
import br.edu.unisa.clinica.model.Dentista;
import br.edu.unisa.clinica.model.Paciente;
import br.edu.unisa.clinica.model.Procedimento;
import br.edu.unisa.clinica.model.StatusConsulta;
import br.edu.unisa.clinica.service.Clinica;
import br.edu.unisa.clinica.service.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArmazenamentoCsvTest {

    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate SEGUNDA = LocalDate.of(2026, 11, 23);

    private static Clock relogio() {
        return Clock.fixed(LocalDateTime.of(2026, 11, 22, 12, 0).atZone(ZONA).toInstant(), ZONA);
    }

    @Test
    void dadosGravadosSaoRecuperadosIguais() throws IOException {
        Path pasta = Files.createTempDirectory("clinica-teste");
        Clinica original = Clinica.abrir(pasta, new DetectorPorArvore(), relogio());
        DadosDemonstracao.popular(original, SEGUNDA);
        original.salvar();

        Clinica recarregada = Clinica.abrir(pasta, new DetectorPorArvore(), relogio());

        assertEquals(original.cadastro().listarPacientes().size(), recarregada.cadastro().listarPacientes().size());
        assertEquals(original.cadastro().listarDentistas().size(), recarregada.cadastro().listarDentistas().size());
        assertEquals(original.cadastro().listarProcedimentos().size(), recarregada.cadastro().listarProcedimentos().size());
        assertEquals(original.listaEspera().listar().size(), recarregada.listaEspera().listar().size());
        assertEquals(original.consultas().listarTodas().size(), recarregada.consultas().listarTodas().size());

        for (Consulta c : original.consultas().listarTodas()) {
            Consulta r = recarregada.consultas().buscarPorId(c.getId()).orElseThrow();
            assertEquals(c.getInicio(), r.getInicio());
            assertEquals(c.getFim(), r.getFim());
            assertEquals(c.getStatus(), r.getStatus());
            assertEquals(c.getPaciente().getNome(), r.getPaciente().getNome());
        }
        Dentista d = recarregada.cadastro().listarDentistas().get(0);
        assertEquals(10, d.getExpedientes().size());
    }

    @Test
    void consultasAgendadasContinuamBloqueandoOHorarioAposRecarregar() throws IOException {
        Path pasta = Files.createTempDirectory("clinica-teste");
        Clinica original = Clinica.abrir(pasta, new DetectorPorArvore(), relogio());
        DadosDemonstracao.popular(original, SEGUNDA);
        original.salvar();

        Clinica recarregada = Clinica.abrir(pasta, new DetectorPorArvore(), relogio());
        Dentista geral = recarregada.cadastro().listarDentistas().get(0);
        Paciente outro = recarregada.cadastro().listarPacientes().get(5);
        Procedimento restauracao = recarregada.cadastro().listarProcedimentos().get(2);

        // 9h na segunda já tem uma restauração agendada para o dentista geral
        assertThrows(RegraNegocioException.class,
                () -> recarregada.agendamento().agendar(outro, geral, restauracao, SEGUNDA.atTime(9, 0)));
        // o horário da consulta cancelada (14h) voltou a ficar livre
        recarregada.agendamento().agendar(outro, geral, restauracao, SEGUNDA.atTime(14, 0));
        assertTrue(recarregada.consultas().listarTodas().stream()
                .anyMatch(c -> c.getStatus() == StatusConsulta.CANCELADA));
    }

    @Test
    void textosComPontoEVirgulaNaoCorrompemOArquivo() throws IOException {
        Path pasta = Files.createTempDirectory("clinica-teste");
        Clinica clinica = Clinica.abrir(pasta, new DetectorPorArvore(), relogio());
        clinica.cadastro().cadastrarPaciente("Nome; com ponto e vírgula", "000", "", "", LocalDate.of(1990, 1, 1));
        clinica.salvar();

        Clinica recarregada = Clinica.abrir(pasta, new DetectorPorArvore(), relogio());
        assertEquals(1, recarregada.cadastro().listarPacientes().size());
        assertEquals("Nome, com ponto e vírgula", recarregada.cadastro().listarPacientes().get(0).getNome());
    }
}
