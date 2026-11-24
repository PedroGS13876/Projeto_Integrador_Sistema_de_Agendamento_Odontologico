package br.edu.unisa.clinica.app;

import br.edu.unisa.clinica.service.Clinica;
import br.edu.unisa.clinica.ui.JanelaPrincipal;

import javax.swing.SwingUtilities;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * Inicia o sistema com interface gráfica.
 *
 * <p>Uso: {@code java -cp ... br.edu.unisa.clinica.app.Main [pasta-de-dados] [--demo]}.
 * Sem argumentos, os dados ficam na pasta "dados". Com {@code --demo}, se a pasta
 * ainda estiver vazia, é carregado o cenário simulado com dados fictícios.
 */
public class Main {

    public static void main(String[] args) {
        Path pasta = Path.of("dados");
        boolean demo = false;
        for (String arg : args) {
            if ("--demo".equals(arg)) {
                demo = true;
            } else {
                pasta = Path.of(arg);
            }
        }

        Clinica clinica = Clinica.abrir(pasta);
        if (demo && clinica.cadastro().listarPacientes().isEmpty()) {
            LocalDate segunda = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            DadosDemonstracao.popular(clinica, segunda);
            clinica.salvar();
        }

        JanelaPrincipal.usarVisualNimbus();
        SwingUtilities.invokeLater(() -> new JanelaPrincipal(clinica).setVisible(true));
    }
}
