package br.edu.unisa.clinica.service;

/** Lançada quando uma operação viola uma regra de negócio do sistema. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
