package br.edu.unisa.clinica.service;

import br.edu.unisa.clinica.model.Consulta;
import br.edu.unisa.clinica.model.StatusConsulta;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Relatórios gerenciais sobre as consultas de um período (RF12). */
public class Relatorios {

    private Relatorios() { }

    /** Consultas cujo início está no período [de, ate], inclusive. */
    public static List<Consulta> doPeriodo(List<Consulta> todas, LocalDate de, LocalDate ate) {
        List<Consulta> resultado = new ArrayList<>();
        for (Consulta c : todas) {
            LocalDate dia = c.getInicio().toLocalDate();
            if (!dia.isBefore(de) && !dia.isAfter(ate)) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    /** Quantidade de consultas por status. */
    public static Map<StatusConsulta, Integer> porStatus(List<Consulta> consultas) {
        Map<StatusConsulta, Integer> contagem = new EnumMap<>(StatusConsulta.class);
        for (StatusConsulta s : StatusConsulta.values()) {
            contagem.put(s, 0);
        }
        for (Consulta c : consultas) {
            contagem.merge(c.getStatus(), 1, Integer::sum);
        }
        return contagem;
    }

    /**
     * Taxa de faltas entre as consultas já decididas (concluídas ou faltas),
     * de 0 a 1. Retorna 0 quando não há consultas decididas.
     */
    public static double taxaDeFaltas(List<Consulta> consultas) {
        Map<StatusConsulta, Integer> s = porStatus(consultas);
        int decididas = s.get(StatusConsulta.CONCLUIDA) + s.get(StatusConsulta.FALTA);
        return decididas == 0 ? 0.0 : (double) s.get(StatusConsulta.FALTA) / decididas;
    }

    /** Procedimentos em ordem decrescente de frequência (consultas não canceladas). */
    public static List<Map.Entry<String, Integer>> procedimentosMaisFrequentes(List<Consulta> consultas) {
        Map<String, Integer> contagem = new HashMap<>();
        for (Consulta c : consultas) {
            if (c.getStatus() != StatusConsulta.CANCELADA) {
                contagem.merge(c.getProcedimento().getNome(), 1, Integer::sum);
            }
        }
        List<Map.Entry<String, Integer>> lista = new ArrayList<>(contagem.entrySet());
        lista.sort((a, b) -> b.getValue() != a.getValue().intValue()
                ? Integer.compare(b.getValue(), a.getValue())
                : a.getKey().compareTo(b.getKey()));
        return lista;
    }
}
