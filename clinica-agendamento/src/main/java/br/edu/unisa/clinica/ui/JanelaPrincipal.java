package br.edu.unisa.clinica.ui;

import br.edu.unisa.clinica.service.Clinica;

import javax.swing.JFrame;
import javax.swing.UIManager;

/** Janela do sistema. */
public class JanelaPrincipal extends JFrame {

    public JanelaPrincipal(Clinica clinica) {
        super("Sistema de agendamento – Clínica odontológica (cenário simulado)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(new PainelPrincipal(clinica));
        setSize(1000, 620);
        setLocationRelativeTo(null);
    }

    /** Tenta usar o visual Nimbus; se não estiver disponível, mantém o padrão. */
    public static void usarVisualNimbus() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (Exception ignorado) {
            // mantém o visual padrão
        }
    }
}
