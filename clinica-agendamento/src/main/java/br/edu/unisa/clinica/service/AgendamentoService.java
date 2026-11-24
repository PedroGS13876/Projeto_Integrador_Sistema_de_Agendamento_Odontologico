package br.edu.unisa.clinica.service;

import br.edu.unisa.clinica.conflito.DetectorConflitos;
import br.edu.unisa.clinica.model.Consulta;
import br.edu.unisa.clinica.model.Dentista;
import br.edu.unisa.clinica.model.Expediente;
import br.edu.unisa.clinica.model.Paciente;
import br.edu.unisa.clinica.model.Procedimento;
import br.edu.unisa.clinica.model.StatusConsulta;
import br.edu.unisa.clinica.repository.ConsultaRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Regras de negócio do agendamento (RN01 a RN07). A estratégia de detecção de
 * conflitos é injetada, o que permite comparar as duas implementações.
 */
public class AgendamentoService {

    private final ConsultaRepository repositorio;
    private final DetectorConflitos detector;
    private final Clock relogio;

    public AgendamentoService(ConsultaRepository repositorio, DetectorConflitos detector, Clock relogio) {
        this.repositorio = repositorio;
        this.detector = detector;
        this.relogio = relogio;
    }

    public AgendamentoService(ConsultaRepository repositorio, DetectorConflitos detector) {
        this(repositorio, detector, Clock.systemDefaultZone());
    }

    /** Agenda uma consulta após validar todas as regras (UC04). */
    public Consulta agendar(Paciente paciente, Dentista dentista,
                            Procedimento procedimento, LocalDateTime inicio) {
        LocalDateTime fim = inicio.plusMinutes(procedimento.getDuracaoMinutos());
        validar(paciente, dentista, inicio, fim, null);

        Consulta consulta = new Consulta(repositorio.proximoId(), paciente, dentista, procedimento, inicio);
        repositorio.salvar(consulta);
        detector.registrar(consulta);
        return consulta;
    }

    /**
     * Registra uma consulta lida do armazenamento, sem revalidar as regras
     * (ela já foi validada quando foi criada). Consultas agendadas voltam a
     * ocupar a agenda do dentista.
     */
    public void registrarExistente(Consulta consulta) {
        repositorio.salvar(consulta);
        if (consulta.getStatus() == StatusConsulta.AGENDADA) {
            detector.registrar(consulta);
        }
    }

    /** Cancela uma consulta agendada, liberando o horário (UC05). */
    public void cancelar(int idConsulta, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new RegraNegocioException("Informe o motivo do cancelamento.");
        }
        Consulta consulta = buscarAgendada(idConsulta);
        detector.remover(consulta);
        consulta.cancelar(motivo);
        repositorio.salvar(consulta);
    }

    /**
     * Remarca uma consulta (UC06). Se o novo horário violar alguma regra, a
     * consulta original é mantida sem alterações.
     */
    public Consulta remarcar(int idConsulta, LocalDateTime novoInicio) {
        Consulta consulta = buscarAgendada(idConsulta);
        int duracao = consulta.getProcedimento().getDuracaoMinutos();
        LocalDateTime novoFim = novoInicio.plusMinutes(duracao);

        detector.remover(consulta);   // a consulta não pode conflitar consigo mesma
        try {
            validar(consulta.getPaciente(), consulta.getDentista(),
                    novoInicio, novoFim, consulta);
        } catch (RegraNegocioException e) {
            detector.registrar(consulta);   // restaura o horário original
            throw e;
        }
        consulta.reagendar(novoInicio);
        detector.registrar(consulta);
        repositorio.salvar(consulta);
        return consulta;
    }

    /** Registra o desfecho de uma consulta: CONCLUIDA ou FALTA (UC08). */
    public void registrarDesfecho(int idConsulta, StatusConsulta desfecho) {
        Consulta consulta = buscarAgendada(idConsulta);
        if (desfecho == StatusConsulta.CONCLUIDA) {
            consulta.concluir();
        } else if (desfecho == StatusConsulta.FALTA) {
            consulta.registrarFalta();
        } else {
            throw new RegraNegocioException("Desfecho inválido: " + desfecho);
        }
        detector.remover(consulta);
        repositorio.salvar(consulta);
    }

    /**
     * Lista os horários livres de um dentista em um dia para determinado
     * procedimento, a cada {@code passoMinutos} minutos. Usada para sugerir
     * alternativas quando há conflito (fluxo alternativo do UC04).
     */
    public List<LocalDateTime> horariosLivres(Dentista dentista, LocalDate dia,
                                              Procedimento procedimento, int passoMinutos) {
        List<LocalDateTime> livres = new ArrayList<>();
        LocalDateTime agora = LocalDateTime.now(relogio);

        for (Expediente e : dentista.getExpedientes()) {
            if (e.getDiaSemana() != dia.getDayOfWeek()) {
                continue;
            }
            LocalTime hora = e.getHoraInicio();
            while (!hora.plusMinutes(procedimento.getDuracaoMinutos()).isAfter(e.getHoraFim())) {
                LocalDateTime inicio = dia.atTime(hora);
                LocalDateTime fim = inicio.plusMinutes(procedimento.getDuracaoMinutos());
                if (!inicio.isBefore(agora) && !detector.existeConflito(dentista, inicio, fim)) {
                    livres.add(inicio);
                }
                hora = hora.plusMinutes(passoMinutos);
            }
        }
        livres.sort(null);
        return livres;
    }

    /** Aplica as regras RN02 a RN05 e RN07. {@code ignorada} é a consulta em remarcação, se houver. */
    private void validar(Paciente paciente, Dentista dentista,
                         LocalDateTime inicio, LocalDateTime fim, Consulta ignorada) {
        // RN07: pacientes e dentistas inativos não recebem agendamentos
        if (!paciente.isAtivo() || !dentista.isAtivo()) {
            throw new RegraNegocioException("Paciente ou dentista inativo.");
        }
        // RN04: não agendar no passado
        if (inicio.isBefore(LocalDateTime.now(relogio))) {
            throw new RegraNegocioException("Não é possível agendar no passado.");
        }
        // RN05: somente dentro do expediente do dentista
        if (!dentista.atendeEm(inicio, fim)) {
            throw new RegraNegocioException("Horário fora do expediente.");
        }
        // RN02: sem sobreposição na agenda do dentista
        if (detector.existeConflito(dentista, inicio, fim)) {
            throw new RegraNegocioException("Conflito na agenda do dentista.");
        }
        // RN03: sem sobreposição na agenda do paciente
        for (Consulta outra : repositorio.buscarAgendadasDoPaciente(paciente.getId())) {
            boolean sobrepoe = inicio.isBefore(outra.getFim())
                    && outra.getInicio().isBefore(fim);
            if (outra != ignorada && sobrepoe) {
                throw new RegraNegocioException("O paciente já tem consulta nesse horário.");
            }
        }
    }

    private Consulta buscarAgendada(int idConsulta) {
        Consulta consulta = repositorio.buscarPorId(idConsulta)
                .orElseThrow(() -> new RegraNegocioException("Consulta não encontrada: " + idConsulta));
        if (consulta.getStatus() != StatusConsulta.AGENDADA) {
            throw new RegraNegocioException("A consulta não está agendada: " + consulta.getStatus());
        }
        return consulta;
    }
}
