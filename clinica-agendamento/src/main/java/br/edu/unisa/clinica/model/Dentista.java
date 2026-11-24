package br.edu.unisa.clinica.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Dentista da clínica, com sua especialidade e seus horários de atendimento. */
public class Dentista extends Pessoa {

    private final String cro;
    private final String especialidade;
    private final List<Expediente> expedientes = new ArrayList<>();

    public Dentista(int id, String nome, String cro, String especialidade,
                    String telefone, String email) {
        super(id, nome, telefone, email);
        this.cro = cro;
        this.especialidade = especialidade;
    }

    public String getCro() { return cro; }
    public String getEspecialidade() { return especialidade; }

    public List<Expediente> getExpedientes() {
        return Collections.unmodifiableList(expedientes);
    }

    public void adicionarExpediente(Expediente expediente) {
        expedientes.add(expediente);
    }

    /** Verifica se o intervalo [inicio, fim] está dentro de alguma faixa de atendimento (RN05). */
    public boolean atendeEm(LocalDateTime inicio, LocalDateTime fim) {
        for (Expediente e : expedientes) {
            if (e.cobre(inicio, fim)) {
                return true;
            }
        }
        return false;
    }
}
