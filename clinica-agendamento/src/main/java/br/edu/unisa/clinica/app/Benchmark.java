package br.edu.unisa.clinica.app;

import br.edu.unisa.clinica.conflito.DetectorConflitos;
import br.edu.unisa.clinica.conflito.DetectorPorArvore;
import br.edu.unisa.clinica.conflito.DetectorPorVarredura;
import br.edu.unisa.clinica.model.Consulta;
import br.edu.unisa.clinica.model.Dentista;
import br.edu.unisa.clinica.model.Paciente;
import br.edu.unisa.clinica.model.Procedimento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Random;

/**
 * Compara o tempo médio de uma verificação de conflito nas duas estratégias,
 * variando o número de consultas já registradas para um dentista.
 *
 * <p>Uso: {@code java -cp ... br.edu.unisa.clinica.app.Benchmark}. Os tempos variam
 * conforme o computador, por isso o programa informa o ambiente de execução.
 */
public class Benchmark {

    private static final int[] TAMANHOS = {100, 1_000, 10_000, 100_000};
    private static final int CONSULTAS_POR_MEDICAO = 20_000;
    private static final int REPETICOES = 7;

    public static void main(String[] args) {
        System.out.println("Java: " + System.getProperty("java.version") + " | SO: "
                + System.getProperty("os.name") + " | processadores: "
                + Runtime.getRuntime().availableProcessors());
        System.out.println("Cada medição: " + CONSULTAS_POR_MEDICAO + " verificações; mediana de "
                + REPETICOES + " repetições (após aquecimento).");
        System.out.println();
        System.out.printf("%-12s %-22s %-22s %-10s%n", "Consultas", "Varredura (ns/verif.)",
                "Árvore (ns/verif.)", "Razão");

        for (int n : TAMANHOS) {
            Dentista dentista = new Dentista(1, "Dentista Fictício", "CRO-0001", "Clínica geral", "", "");
            Paciente paciente = new Paciente(1, "Paciente Fictício", "000", "", "", LocalDate.of(1990, 1, 1));
            Procedimento proc = new Procedimento(1, "Consulta", 45, BigDecimal.TEN);

            DetectorConflitos varredura = new DetectorPorVarredura();
            DetectorConflitos arvore = new DetectorPorArvore();
            LocalDateTime base = LocalDateTime.of(2030, 1, 1, 8, 0);
            for (int i = 0; i < n; i++) {
                // consultas de 45 min a cada 60 min: 15 min livres entre elas
                Consulta c = new Consulta(i + 1, paciente, dentista, proc, base.plusMinutes(60L * i));
                varredura.registrar(c);
                arvore.registrar(c);
            }

            long[] candidatos = candidatos(n, base);
            double tv = medir(varredura, dentista, candidatos);
            double ta = medir(arvore, dentista, candidatos);
            System.out.printf("%-12d %-22.1f %-22.1f %-10.1f%n", n, tv, ta, tv / ta);
        }
    }

    /**
     * Minutos (a partir de {@code base}) dos horários testados, para consultas de 15 minutos: metade
     * cai na folga de 15 minutos entre duas consultas (sem conflito) e metade sobre uma consulta (conflito).
     */
    private static long[] candidatos(int n, LocalDateTime base) {
        Random rnd = new Random(42);
        long[] minutos = new long[CONSULTAS_POR_MEDICAO];
        for (int i = 0; i < minutos.length; i++) {
            minutos[i] = (long) rnd.nextInt(n) * 60 + (rnd.nextBoolean() ? 45 : 10);
        }
        return minutos;
    }

    private static double medir(DetectorConflitos detector, Dentista dentista, long[] minutos) {
        LocalDateTime base = LocalDateTime.of(2030, 1, 1, 8, 0);
        int conflitos = 0;
        for (int i = 0; i < 3; i++) {                       // aquecimento da JVM
            conflitos += rodar(detector, dentista, minutos, base);
        }
        double[] tempos = new double[REPETICOES];
        for (int r = 0; r < REPETICOES; r++) {
            long inicio = System.nanoTime();
            conflitos += rodar(detector, dentista, minutos, base);
            tempos[r] = (System.nanoTime() - inicio) / (double) minutos.length;
        }
        if (conflitos < 0) {
            System.out.println();                          // evita que a JVM elimine o cálculo
        }
        Arrays.sort(tempos);
        return tempos[REPETICOES / 2];
    }

    private static int rodar(DetectorConflitos detector, Dentista dentista, long[] minutos, LocalDateTime base) {
        int conflitos = 0;
        for (long m : minutos) {
            LocalDateTime inicio = base.plusMinutes(m);
            if (detector.existeConflito(dentista, inicio, inicio.plusMinutes(15))) {
                conflitos++;
            }
        }
        return conflitos;
    }
}
