package br.edu.unisa.clinica.conflito;

import br.edu.unisa.clinica.model.Consulta;
import br.edu.unisa.clinica.model.Dentista;
import br.edu.unisa.clinica.model.Paciente;
import br.edu.unisa.clinica.model.Procedimento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste de equivalência: as duas estratégias devem responder exatamente o mesmo
 * para qualquer consulta, inclusive com durações variadas e remoções.
 */
class DetectoresEquivalenciaTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2030, 1, 1, 8, 0);

    @Test
    void estrategiasConcordamEmMilharesDeCasosAleatorios() {
        Random rnd = new Random(2026);
        Dentista dentista = new Dentista(1, "Dentista Fictício", "CRO-0001", "Clínica geral", "", "");
        Paciente paciente = new Paciente(1, "Paciente Fictício", "000", "", "", LocalDate.of(1990, 1, 1));
        int[] duracoes = {15, 30, 45, 60, 90};

        DetectorConflitos varredura = new DetectorPorVarredura();
        DetectorConflitos arvore = new DetectorPorArvore();
        java.util.List<Consulta> registradas = new java.util.ArrayList<>();
        int aceitas = 0;

        for (int i = 0; i < 4000; i++) {
            Procedimento proc = new Procedimento(1, "Proc", duracoes[rnd.nextInt(duracoes.length)], BigDecimal.TEN);
            LocalDateTime inicio = BASE.plusMinutes(rnd.nextInt(3000) * 5L);
            LocalDateTime fim = inicio.plusMinutes(proc.getDuracaoMinutos());

            boolean a = varredura.existeConflito(dentista, inicio, fim);
            boolean b = arvore.existeConflito(dentista, inicio, fim);
            assertEquals(a, b);

            if (!a) {                                   // registra nas duas, mantendo a agenda sem sobreposição
                Consulta c = new Consulta(i, paciente, dentista, proc, inicio);
                varredura.registrar(c);
                arvore.registrar(c);
                registradas.add(c);
                aceitas++;
            }
            if (rnd.nextInt(10) == 0 && !registradas.isEmpty()) {   // remove uma consulta aleatória
                Consulta removida = registradas.remove(rnd.nextInt(registradas.size()));
                varredura.remover(removida);
                arvore.remover(removida);
            }
        }
        assertTrue(aceitas > 100);
    }
}
