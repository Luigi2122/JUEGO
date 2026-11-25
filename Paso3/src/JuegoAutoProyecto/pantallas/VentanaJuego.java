package JuegoAutoProyecto.pantallas;

import JuegoAutoProyecto.historial.HistorialPartidas;
import JuegoAutoProyecto.util.Config;
import JuegoAutoProyecto.util.Recursos;
import java.awt.CardLayout;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * La ventana principal. Contiene las tres pantallas del juego y decide cuál se ve.
 *
 * <p>Usa un {@link CardLayout}: funciona como un mazo de cartas donde solo la carta
 * de arriba es visible. Cambiar de pantalla = mostrar otra carta.</p>
 *
 * <pre>
 *   PantallaInicio  --(escribe su nombre)-->  PantallaCarrera
 *        ^                                           |
 *        +-----------(N: nuevo jugador)--------------+
 *   PantallaHistorial  <--(botón / tecla H)-- ambas pantallas
 * </pre>
 *
 * <p>También guarda el {@link HistorialPartidas}, que es compartido por todas las pantallas.</p>
 */
public class VentanaJuego extends JFrame {

    private static final String INICIO = "inicio";
    private static final String CARRERA = "carrera";
    private static final String HISTORIAL = "historial";

    private final CardLayout mazo = new CardLayout();
    private final JPanel contenedor = new JPanel(mazo);
    private final HistorialPartidas historial = new HistorialPartidas();

    private final PantallaInicio pantallaInicio;
    private final PantallaCarrera pantallaCarrera;
    private final PantallaHistorial pantallaHistorial;

    private Pantalla pantallaActual;
    private Pantalla pantallaAnterior;   // Para saber a dónde volver desde el historial
    private String ultimoJugador = "";   // Para no tener que escribir el nombre otra vez

    public VentanaJuego() {
        setTitle(Config.TITULO);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);                                  // Así la pista no se deforma
        BufferedImage icono = Recursos.imagen("auto_jugador.png");
        if (icono != null) setIconImage(icono);               // Ícono de la ventana

        pantallaInicio = new PantallaInicio(this);
        pantallaCarrera = new PantallaCarrera(this);
        pantallaHistorial = new PantallaHistorial(this);

        contenedor.add(pantallaInicio, INICIO);
        contenedor.add(pantallaCarrera, CARRERA);
        contenedor.add(pantallaHistorial, HISTORIAL);
        add(contenedor);

        pack();                          // Ajusta la ventana al tamaño de las pantallas
        setLocationRelativeTo(null);     // Centra la ventana en el monitor
        cambiarA(pantallaInicio, INICIO);
    }

    /** Muestra la pantalla de inicio (para escribir el nombre). */
    public void irAInicio() {
        cambiarA(pantallaInicio, INICIO);
    }

    /** Empieza una carrera nueva con el nombre indicado. */
    public void iniciarCarrera(String nombreJugador) {
        ultimoJugador = nombreJugador;
        pantallaCarrera.prepararPartida(nombreJugador);
        cambiarA(pantallaCarrera, CARRERA);
    }

    /** Muestra el historial de partidas. */
    public void irAHistorial() {
        cambiarA(pantallaHistorial, HISTORIAL);
    }

    /** Vuelve desde el historial a la pantalla donde estaba el jugador. */
    public void volverDelHistorial() {
        if (pantallaAnterior == pantallaCarrera) {
            cambiarA(pantallaCarrera, CARRERA);
        } else {
            cambiarA(pantallaInicio, INICIO);
        }
    }

    public HistorialPartidas getHistorial() { return historial; }
    public String getUltimoJugador() { return ultimoJugador; }

    /** Oculta la pantalla actual y muestra la nueva. */
    private void cambiarA(Pantalla nueva, String nombreCarta) {
        if (pantallaActual != null) pantallaActual.ocultar();
        pantallaAnterior = pantallaActual;
        pantallaActual = nueva;
        mazo.show(contenedor, nombreCarta);
        nueva.mostrar();
    }
}
