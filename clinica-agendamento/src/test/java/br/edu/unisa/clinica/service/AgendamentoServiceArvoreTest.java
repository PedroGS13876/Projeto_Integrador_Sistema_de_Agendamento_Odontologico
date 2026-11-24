package br.edu.unisa.clinica.service;

import br.edu.unisa.clinica.conflito.DetectorConflitos;
import br.edu.unisa.clinica.conflito.DetectorPorArvore;

/** Executa os mesmos casos de teste com a detecção por árvore ordenada. */
class AgendamentoServiceArvoreTest extends AgendamentoServiceTestBase {
    @Override
    protected DetectorConflitos criarDetector() {
        return new DetectorPorArvore();
    }
}
