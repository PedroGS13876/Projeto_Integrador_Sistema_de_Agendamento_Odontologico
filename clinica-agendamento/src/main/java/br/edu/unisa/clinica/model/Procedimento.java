package br.edu.unisa.clinica.model;

import java.math.BigDecimal;

/** Procedimento odontológico, com duração usada para calcular o fim da consulta. */
public class Procedimento {

    private final int id;
    private final String nome;
    private final int duracaoMinutos;
    private final BigDecimal valor;

    public Procedimento(int id, String nome, int duracaoMinutos, BigDecimal valor) {
        if (duracaoMinutos <= 0) {
            throw new IllegalArgumentException("A duração deve ser maior que zero.");
        }
        this.id = id;
        this.nome = nome;
        this.duracaoMinutos = duracaoMinutos;
        this.valor = valor;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public int getDuracaoMinutos() { return duracaoMinutos; }
    public BigDecimal getValor() { return valor; }

    @Override
    public String toString() { return nome + " (" + duracaoMinutos + " min)"; }
}
