package br.edu.unisa.clinica.service;

import br.edu.unisa.clinica.conflito.DetectorConflitos;
import br.edu.unisa.clinica.conflito.DetectorPorVarredura;

/** Executa os casos de teste com a detecção por varredura da lista. */
class AgendamentoServiceVarreduraTest extends AgendamentoServiceTestBase {
    @Override
    protected DetectorConflitos criarDetector() {
        return new DetectorPorVarredura();
    }
}
