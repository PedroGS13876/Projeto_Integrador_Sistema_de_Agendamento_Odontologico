package br.edu.unisa.clinica.repository;

import br.edu.unisa.clinica.model.Consulta;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Acesso aos dados das consultas. A camada de serviço depende apenas desta interface. */
public interface ConsultaRepository {

    /** Gera o próximo identificador disponível. */
    int proximoId();

    /** Grava ou atualiza uma consulta. */
    Consulta salvar(Consulta consulta);

    Optional<Consulta> buscarPorId(int id);

    /** Todas as consultas registradas, em ordem de cadastro. */
    List<Consulta> listarTodas();

    /** Consultas com status AGENDADA do paciente. */
    List<Consulta> buscarAgendadasDoPaciente(int pacienteId);

    /** Consultas do dentista que iniciam no dia informado, em qualquer status. */
    List<Consulta> buscarPorDentistaEDia(int dentistaId, LocalDate dia);
}
