package br.edu.unisa.clinica.repository;

import br.edu.unisa.clinica.model.Consulta;
import br.edu.unisa.clinica.model.StatusConsulta;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementação em memória, usada nos testes e na demonstração. A versão com
 * banco de dados (JDBC) implementa a mesma interface.
 */
public class ConsultaRepositoryMemoria implements ConsultaRepository {

    private final Map<Integer, Consulta> consultas = new LinkedHashMap<>();
    private int ultimoId = 0;

    @Override
    public int proximoId() {
        return ++ultimoId;
    }

    @Override
    public Consulta salvar(Consulta consulta) {
        consultas.put(consulta.getId(), consulta);
        ultimoId = Math.max(ultimoId, consulta.getId());
        return consulta;
    }

    @Override
    public List<Consulta> listarTodas() {
        return new ArrayList<>(consultas.values());
    }

    @Override
    public Optional<Consulta> buscarPorId(int id) {
        return Optional.ofNullable(consultas.get(id));
    }

    @Override
    public List<Consulta> buscarAgendadasDoPaciente(int pacienteId) {
        List<Consulta> resultado = new ArrayList<>();
        for (Consulta c : consultas.values()) {
            if (c.getPaciente().getId() == pacienteId && c.getStatus() == StatusConsulta.AGENDADA) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    @Override
    public List<Consulta> buscarPorDentistaEDia(int dentistaId, LocalDate dia) {
        List<Consulta> resultado = new ArrayList<>();
        for (Consulta c : consultas.values()) {
            if (c.getDentista().getId() == dentistaId && c.getInicio().toLocalDate().equals(dia)) {
                resultado.add(c);
            }
        }
        return resultado;
    }
}
