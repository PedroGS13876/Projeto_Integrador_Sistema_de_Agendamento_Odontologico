package br.edu.unisa.clinica.app;

import br.edu.unisa.clinica.model.Consulta;
import br.edu.unisa.clinica.model.Dentista;
import br.edu.unisa.clinica.model.Paciente;
import br.edu.unisa.clinica.model.Procedimento;
import br.edu.unisa.clinica.model.StatusConsulta;
import br.edu.unisa.clinica.service.Cadastro;
import br.edu.unisa.clinica.service.Clinica;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Popula a clínica com o cenário simulado: todos os nomes e dados são fictícios. */
public final class DadosDemonstracao {

    private DadosDemonstracao() { }

    /** Cria cadastros e algumas consultas na semana que começa em {@code segunda}. */
    public static void popular(Clinica clinica, LocalDate segunda) {
        Cadastro cad = clinica.cadastro();

        Dentista geral = cad.cadastrarDentista("Dentista Exemplo A", "CRO-0001", "Clínica geral", "(11) 90000-0001", "dentista.a@exemplo.com");
        Dentista orto = cad.cadastrarDentista("Dentista Exemplo B", "CRO-0002", "Ortodontia", "(11) 90000-0002", "dentista.b@exemplo.com");
        Dentista endo = cad.cadastrarDentista("Dentista Exemplo C", "CRO-0003", "Endodontia", "(11) 90000-0003", "dentista.c@exemplo.com");

        Procedimento avaliacao = cad.cadastrarProcedimento("Avaliação inicial", 30, new BigDecimal("80.00"));
        Procedimento profilaxia = cad.cadastrarProcedimento("Profilaxia", 45, new BigDecimal("120.00"));
        Procedimento restauracao = cad.cadastrarProcedimento("Restauração", 60, new BigDecimal("180.00"));
        Procedimento canal = cad.cadastrarProcedimento("Tratamento de canal", 90, new BigDecimal("600.00"));
        Procedimento manutencao = cad.cadastrarProcedimento("Manutenção ortodôntica", 30, new BigDecimal("150.00"));

        String[] nomes = {"Paciente Fictício 1", "Paciente Fictício 2", "Paciente Fictício 3",
                "Paciente Fictício 4", "Paciente Fictício 5", "Paciente Fictício 6"};
        Paciente[] pacientes = new Paciente[nomes.length];
        for (int i = 0; i < nomes.length; i++) {
            pacientes[i] = cad.cadastrarPaciente(nomes[i], String.format("000.000.000-%02d", i + 1),
                    String.format("(11) 90000-10%02d", i + 1), "paciente" + (i + 1) + "@exemplo.com",
                    LocalDate.of(1980 + i * 3, 1 + i, 10 + i));
        }

        var s = clinica.agendamento();
        Consulta c1 = s.agendar(pacientes[0], geral, avaliacao, segunda.atTime(8, 30));
        s.agendar(pacientes[1], geral, restauracao, segunda.atTime(9, 0));
        Consulta c3 = s.agendar(pacientes[2], geral, profilaxia, segunda.atTime(10, 30));
        Consulta c4 = s.agendar(pacientes[3], geral, restauracao, segunda.atTime(14, 0));
        s.agendar(pacientes[4], orto, manutencao, segunda.atTime(9, 0));
        s.agendar(pacientes[5], endo, canal, segunda.atTime(10, 0));
        s.agendar(pacientes[0], geral, profilaxia, segunda.plusDays(1).atTime(9, 0));

        s.registrarDesfecho(c1.getId(), StatusConsulta.CONCLUIDA);
        s.registrarDesfecho(c3.getId(), StatusConsulta.FALTA);
        s.cancelar(c4.getId(), "Paciente desistiu");

        clinica.listaEspera().adicionar(pacientes[2], restauracao, segunda,
                segunda.minusDays(2).atTime(15, 0));
        clinica.listaEspera().adicionar(pacientes[4], profilaxia, null,
                segunda.minusDays(1).atTime(9, 30));
    }
}
