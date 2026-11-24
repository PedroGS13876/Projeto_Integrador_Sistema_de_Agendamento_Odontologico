package br.edu.unisa.clinica.conflito;

import br.edu.unisa.clinica.model.Consulta;
import br.edu.unisa.clinica.model.Dentista;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Detecta conflitos mantendo, para cada dentista, as consultas ordenadas pelo
 * horário de início em um {@link TreeMap}. Como as consultas já registradas de
 * um mesmo dentista nunca se sobrepõem, basta examinar a consulta imediatamente
 * anterior e a imediatamente posterior ao horário desejado. Custo proporcional
 * ao logaritmo do número de consultas (O(log n)).
 */
public class DetectorPorArvore implements DetectorConflitos {

    private final Map<Integer, TreeMap<LocalDateTime, Consulta>> agendas = new HashMap<>();

    @Override
    public boolean existeConflito(Dentista dentista, LocalDateTime inicio, LocalDateTime fim) {
        TreeMap<LocalDateTime, Consulta> agenda = agendas.get(dentista.getId());
        if (agenda == null) {
            return false;
        }
        // Consulta que começa antes (ou junto): conflita se terminar após o início desejado.
        Map.Entry<LocalDateTime, Consulta> anterior = agenda.floorEntry(inicio);
        if (anterior != null && anterior.getValue().getFim().isAfter(inicio)) {
            return true;
        }
        // Consulta que começa depois (ou junto): conflita se começar antes do fim desejado.
        Map.Entry<LocalDateTime, Consulta> posterior = agenda.ceilingEntry(inicio);
        return posterior != null && posterior.getKey().isBefore(fim);
    }

    @Override
    public void registrar(Consulta consulta) {
        agendas.computeIfAbsent(consulta.getDentista().getId(), k -> new TreeMap<>())
               .put(consulta.getInicio(), consulta);
    }

    @Override
    public void remover(Consulta consulta) {
        TreeMap<LocalDateTime, Consulta> agenda = agendas.get(consulta.getDentista().getId());
        if (agenda != null) {
            agenda.remove(consulta.getInicio(), consulta);
        }
    }
}
