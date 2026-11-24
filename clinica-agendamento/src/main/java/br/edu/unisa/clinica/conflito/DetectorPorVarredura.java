package br.edu.unisa.clinica.conflito;

import br.edu.unisa.clinica.model.Consulta;
import br.edu.unisa.clinica.model.Dentista;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Detecta conflitos percorrendo todas as consultas registradas do dentista
 * e aplicando a condição de sobreposição a cada uma. Custo proporcional ao
 * número de consultas do dentista (O(n)).
 */
public class DetectorPorVarredura implements DetectorConflitos {

    private final Map<Integer, List<Consulta>> agendas = new HashMap<>();

    @Override
    public boolean existeConflito(Dentista dentista, LocalDateTime inicio, LocalDateTime fim) {
        List<Consulta> agenda = agendas.get(dentista.getId());
        if (agenda == null) {
            return false;
        }
        for (Consulta existente : agenda) {
            boolean sobrepoe = inicio.isBefore(existente.getFim())
                    && existente.getInicio().isBefore(fim);
            if (sobrepoe) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void registrar(Consulta consulta) {
        agendas.computeIfAbsent(consulta.getDentista().getId(), k -> new ArrayList<>())
               .add(consulta);
    }

    @Override
    public void remover(Consulta consulta) {
        List<Consulta> agenda = agendas.get(consulta.getDentista().getId());
        if (agenda != null) {
            agenda.remove(consulta);
        }
    }
}
