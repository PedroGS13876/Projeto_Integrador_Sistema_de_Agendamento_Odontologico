package br.edu.unisa.clinica.model;

import java.time.LocalDate;

/** Paciente da clínica. Todos os dados usados no projeto são fictícios. */
public class Paciente extends Pessoa {

    private final String cpf;
    private final LocalDate dataNascimento;

    public Paciente(int id, String nome, String cpf, String telefone,
                    String email, LocalDate dataNascimento) {
        super(id, nome, telefone, email);
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
    }

    public String getCpf() { return cpf; }
    public LocalDate getDataNascimento() { return dataNascimento; }
}
