package br.edu.unisa.clinica.service;

import br.edu.unisa.clinica.model.EntradaListaEspera;
import br.edu.unisa.clinica.model.Paciente;
import br.edu.unisa.clinica.model.Procedimento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Lista de espera organizada como fila (FIFO): quem entrou primeiro é o
 * primeiro a ser lembrado quando um horário é liberado (RF11).
 */
public class ListaEspera {

    private final LinkedList<EntradaListaEspera> fila = new LinkedList<>();
    private int ultimoId = 0;

    /** Coloca um paciente no final da fila. */
    public EntradaListaEspera adicionar(Paciente paciente, Procedimento procedimento,
                                        LocalDate dataDesejada, LocalDateTime agora) {
        EntradaListaEspera entrada =
                new EntradaListaEspera(++ultimoId, paciente, procedimento, dataDesejada, agora);
        fila.addLast(entrada);
        return entrada;
    }

    /** Recoloca na fila uma entrada lida do armazenamento. */
    public void restaurar(EntradaListaEspera entrada) {
        fila.addLast(entrada);
        ultimoId = Math.max(ultimoId, entrada.getId());
    }

    /**
     * Primeiro da fila interessado no procedimento e na data informados
     * (ou sem data definida), com paciente ativo.
     */
    public Optional<EntradaListaEspera> proximo(Procedimento procedimento, LocalDate dia) {
        for (EntradaListaEspera e : fila) {
            boolean mesmoProcedimento = e.getProcedimento().getId() == procedimento.getId();
            boolean mesmaData = e.getDataDesejada() == null || e.getDataDesejada().equals(dia);
            if (mesmoProcedimento && mesmaData && e.getPaciente().isAtivo()) {
                return Optional.of(e);
            }
        }
        return Optional.empty();
    }

    public boolean remover(int idEntrada) {
        return fila.removeIf(e -> e.getId() == idEntrada);
    }

    public List<EntradaListaEspera> listar() {
        return new ArrayList<>(fila);
    }
}
