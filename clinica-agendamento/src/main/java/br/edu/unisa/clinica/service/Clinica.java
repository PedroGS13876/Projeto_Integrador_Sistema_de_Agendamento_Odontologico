package br.edu.unisa.clinica.service;

import br.edu.unisa.clinica.conflito.DetectorConflitos;
import br.edu.unisa.clinica.conflito.DetectorPorArvore;
import br.edu.unisa.clinica.model.Consulta;
import br.edu.unisa.clinica.model.EntradaListaEspera;
import br.edu.unisa.clinica.repository.ArmazenamentoCsv;
import br.edu.unisa.clinica.repository.ConsultaRepository;
import br.edu.unisa.clinica.repository.ConsultaRepositoryMemoria;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Optional;

/**
 * Ponto único de acesso ao sistema: reúne cadastros, agendamento, lista de
 * espera e armazenamento. A interface gráfica conversa apenas com esta classe.
 */
public class Clinica {

    private final Cadastro cadastro = new Cadastro();
    private final ConsultaRepository consultas = new ConsultaRepositoryMemoria();
    private final ListaEspera listaEspera = new ListaEspera();
    private final AgendamentoService agendamento;
    private final ArmazenamentoCsv armazenamento;
    private final Clock relogio;

    private Clinica(Path pasta, DetectorConflitos detector, Clock relogio) {
        this.relogio = relogio;
        this.agendamento = new AgendamentoService(consultas, detector, relogio);
        this.armazenamento = pasta == null ? null : new ArmazenamentoCsv(pasta);
    }

    /** Abre a clínica carregando os arquivos da pasta (se existirem). Com pasta nula, funciona só em memória. */
    public static Clinica abrir(Path pasta, DetectorConflitos detector, Clock relogio) {
        Clinica clinica = new Clinica(pasta, detector, relogio);
        if (clinica.armazenamento != null) {
            try {
                clinica.armazenamento.carregar(clinica.cadastro, clinica.agendamento, clinica.listaEspera);
            } catch (IOException e) {
                throw new UncheckedIOException("Não foi possível ler os dados em " + pasta, e);
            }
        }
        return clinica;
    }

    public static Clinica abrir(Path pasta) {
        return abrir(pasta, new DetectorPorArvore(), Clock.systemDefaultZone());
    }

    /** Grava todos os dados nos arquivos. Chamado após cada alteração. */
    public void salvar() {
        if (armazenamento == null) {
            return;
        }
        try {
            armazenamento.salvar(cadastro, consultas, listaEspera);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível gravar os dados.", e);
        }
    }

    public Cadastro cadastro() { return cadastro; }
    public AgendamentoService agendamento() { return agendamento; }
    public ListaEspera listaEspera() { return listaEspera; }
    public ConsultaRepository consultas() { return consultas; }
    public Clock relogio() { return relogio; }

    /** Após um cancelamento, indica quem está na frente da fila para o horário liberado (RF11). */
    public Optional<EntradaListaEspera> candidatoAoHorarioLiberado(Consulta cancelada) {
        return listaEspera.proximo(cancelada.getProcedimento(), cancelada.getInicio().toLocalDate());
    }
}
