package br.edu.unisa.clinica.app;

import br.edu.unisa.clinica.conflito.DetectorPorArvore;
import br.edu.unisa.clinica.model.Consulta;
import br.edu.unisa.clinica.model.Dentista;
import br.edu.unisa.clinica.model.Expediente;
import br.edu.unisa.clinica.model.Paciente;
import br.edu.unisa.clinica.model.Procedimento;
import br.edu.unisa.clinica.repository.ConsultaRepositoryMemoria;
import br.edu.unisa.clinica.service.AgendamentoService;
import br.edu.unisa.clinica.service.RegraNegocioException;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

/** Demonstração no console usando o cenário simulado (dados fictícios). */
public class DemoConsole {

    public static void main(String[] args) {
        AgendamentoService servico =
                new AgendamentoService(new ConsultaRepositoryMemoria(), new DetectorPorArvore());

        Dentista dentista = new Dentista(1, "Dentista Exemplo A", "CRO-0001", "Clínica geral",
                "(11) 90000-0001", "dentista.a@exemplo.com");
        for (DayOfWeek dia : new DayOfWeek[] {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}) {
            dentista.adicionarExpediente(new Expediente(dia, LocalTime.of(8, 0), LocalTime.of(12, 0)));
            dentista.adicionarExpediente(new Expediente(dia, LocalTime.of(13, 0), LocalTime.of(18, 0)));
        }

        Procedimento restauracao = new Procedimento(1, "Restauração", 60, new BigDecimal("150.00"));
        Paciente ana = new Paciente(1, "Paciente Fictício 1", "000.000.000-01", "(11) 90000-1001",
                "paciente1@exemplo.com", LocalDate.of(1990, 5, 10));
        Paciente bruno = new Paciente(2, "Paciente Fictício 2", "000.000.000-02", "(11) 90000-1002",
                "paciente2@exemplo.com", LocalDate.of(1985, 8, 22));

        LocalDate segunda = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDateTime nove = segunda.atTime(9, 0);

        Consulta c1 = servico.agendar(ana, dentista, restauracao, nove);
        System.out.println("Agendada: consulta " + c1.getId() + " em " + c1.getInicio() + " até " + c1.getFim());

        try {
            servico.agendar(bruno, dentista, restauracao, nove.plusMinutes(30));
        } catch (RegraNegocioException e) {
            System.out.println("Rejeitada: " + e.getMessage());
            System.out.println("Horários livres sugeridos: "
                    + servico.horariosLivres(dentista, segunda, restauracao, 30));
        }

        Consulta c2 = servico.agendar(bruno, dentista, restauracao, nove.plusMinutes(60));
        System.out.println("Agendada: consulta " + c2.getId() + " em " + c2.getInicio() + " (consecutiva, sem conflito)");
    }
}
