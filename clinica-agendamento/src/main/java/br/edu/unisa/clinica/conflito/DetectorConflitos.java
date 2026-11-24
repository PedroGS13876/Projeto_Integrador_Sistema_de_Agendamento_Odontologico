package br.edu.unisa.clinica.conflito;

import br.edu.unisa.clinica.model.Consulta;
import br.edu.unisa.clinica.model.Dentista;

import java.time.LocalDateTime;

/**
 * Estratégia de detecção de conflitos de horário na agenda dos dentistas.
 * Existem duas implementações, comparadas na etapa de testes: varredura da
 * lista do dentista e consulta em árvore ordenada.
 */
public interface DetectorConflitos {

    /** Informa se o intervalo [inicio, fim) conflita com alguma consulta registrada do dentista. */
    boolean existeConflito(Dentista dentista, LocalDateTime inicio, LocalDateTime fim);

    /** Registra uma consulta agendada na agenda do seu dentista. */
    void registrar(Consulta consulta);

    /** Remove uma consulta da agenda do seu dentista (cancelamento, remarcação ou conclusão). */
    void remover(Consulta consulta);
}
