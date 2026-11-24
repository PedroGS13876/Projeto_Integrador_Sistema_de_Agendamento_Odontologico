package br.edu.unisa.clinica.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsultaTest {

    private final Dentista dentista = new Dentista(1, "Dentista Fictício", "CRO-0001", "Clínica geral", "", "");
    private final Paciente paciente = new Paciente(1, "Paciente Fictício", "000", "", "", LocalDate.of(1990, 1, 1));
    private final Procedimento hora = new Procedimento(1, "Restauração", 60, BigDecimal.TEN);

    private Consulta em(int h, int m) {
        return new Consulta(1, paciente, dentista, hora, LocalDateTime.of(2026, 11, 24, h, m));
    }

    @Test
    void fimEhInicioMaisDuracaoDoProcedimento() {
        assertEquals(LocalDateTime.of(2026, 11, 24, 10, 0), em(9, 0).getFim());
    }

    @Test
    void sobreposicaoParcialTotalEIdenticaConflitam() {
        assertTrue(em(9, 0).sobrepoe(em(9, 30)));
        assertTrue(em(9, 30).sobrepoe(em(9, 0)));
        assertTrue(em(9, 0).sobrepoe(em(9, 0)));
    }

    @Test
    void consultasConsecutivasNaoConflitam() {
        assertFalse(em(9, 0).sobrepoe(em(10, 0)));
        assertFalse(em(10, 0).sobrepoe(em(9, 0)));
    }

    @Test
    void naoPermiteAlterarConsultaJaCancelada() {
        Consulta c = em(9, 0);
        c.cancelar("teste");
        assertThrows(IllegalStateException.class, () -> c.reagendar(LocalDateTime.of(2026, 11, 25, 9, 0)));
    }

    @Test
    void procedimentoComDuracaoInvalidaEhRejeitado() {
        assertThrows(IllegalArgumentException.class, () -> new Procedimento(2, "Inválido", 0, BigDecimal.ONE));
    }
}
