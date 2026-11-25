package JuegoAutoProyecto;

import JuegoAutoProyecto.pantallas.VentanaJuego;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada del juego "Carrera de Autos".
 *
 * <p>Para jugar: abre este archivo en IntelliJ y presiona el botón ▶ (Run) que
 * aparece al lado de {@code main}.</p>
 */
public class Main {

    public static void main(String[] args) {
        // Swing exige crear las ventanas en su propio hilo (el "Event Dispatch Thread").
        // invokeLater se encarga de eso.
        SwingUtilities.invokeLater(() -> {
            VentanaJuego ventana = new VentanaJuego();
            ventana.setVisible(true);
        });
    }
}
