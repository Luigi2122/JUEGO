package JuegoAutoProyecto.pantallas;

import JuegoAutoProyecto.util.Boton;
import JuegoAutoProyecto.util.Config;
import JuegoAutoProyecto.util.Estilo;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Clase base de todas las pantallas del juego (inicio, carrera e historial).
 *
 * <p>Se encarga de lo que todas tienen en común, para no repetir código:</p>
 * <ul>
 *   <li>El <b>bucle de animación</b>: un {@link Timer} que cada ~16 ms llama a
 *       {@link #actualizar()} (mover cosas) y luego a {@code repaint()} (que termina
 *       llamando a {@link #dibujar(Graphics2D)}).</li>
 *   <li>Recibir el <b>teclado</b> y el <b>mouse</b> y pasárselos a métodos fáciles de
 *       sobrescribir ({@link #teclaPresionada}, {@link #click}, etc.).</li>
 *   <li>Encender el timer solo cuando la pantalla está visible, para no gastar CPU.</li>
 * </ul>
 */
public abstract class Pantalla extends JPanel {

    protected final VentanaJuego ventana;    // Para poder cambiar de pantalla
    protected long ticks;                    // Fotogramas desde que se mostró esta pantalla
    protected Point mouse = new Point(-100, -100);
    private final Timer timer;

    protected Pantalla(VentanaJuego ventana) {
        this.ventana = ventana;
        setPreferredSize(new Dimension(Config.ANCHO, Config.ALTO));
        setFocusable(true);
        setDoubleBuffered(true);              // Evita parpadeos al redibujar

        // Bucle del juego: actualizar -> repintar, unas 60 veces por segundo
        timer = new Timer(Config.MS_POR_FRAME, e -> {
            ticks++;
            actualizar();
            repaint();
            Toolkit.getDefaultToolkit().sync();   // Hace la animación más fluida en Linux/Mac
        });

        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { teclaPresionada(e); }
            @Override public void keyReleased(KeyEvent e) { teclaSoltada(e); }
            @Override public void keyTyped(KeyEvent e) { caracterEscrito(e.getKeyChar()); }
        });

        MouseAdapter manejadorMouse = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { requestFocusInWindow(); click(e.getPoint()); }
            @Override public void mouseMoved(MouseEvent e) { mouse = e.getPoint(); }
            @Override public void mouseDragged(MouseEvent e) { mouse = e.getPoint(); }
            @Override public void mouseExited(MouseEvent e) { mouse = new Point(-100, -100); }
            @Override public void mouseWheelMoved(MouseWheelEvent e) { rueda(e.getWheelRotation()); }
        };
        addMouseListener(manejadorMouse);
        addMouseMotionListener(manejadorMouse);
        addMouseWheelListener(manejadorMouse);
    }

    /** La ventana llama a este método cuando la pantalla aparece. */
    public final void mostrar() {
        ticks = 0;
        alMostrar();
        timer.start();
        SwingUtilities.invokeLater(this::requestFocusInWindow);   // Para recibir el teclado
    }

    /** La ventana llama a este método cuando la pantalla se oculta. */
    public final void ocultar() {
        timer.stop();
        alOcultar();
    }

    @Override
    protected final void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();   // Copia, para no ensuciar el Graphics original
        Estilo.activarCalidad(g2);
        dibujar(g2);
        g2.dispose();
    }

    /** Pone la "manito" del mouse si está sobre algún botón, y la flecha normal si no. */
    protected void actualizarBotones(Boton... botones) {
        boolean sobreAlguno = false;
        for (Boton b : botones) {
            b.actualizarMouse(mouse);
            sobreAlguno |= b.isEncima();
        }
        setCursor(Cursor.getPredefinedCursor(sobreAlguno ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    // ------------------------------------------------ Métodos que cada pantalla implementa

    /** Lógica de un fotograma (mover objetos, revisar colisiones, animaciones...). */
    protected abstract void actualizar();

    /** Dibuja un fotograma completo. */
    protected abstract void dibujar(Graphics2D g2);

    // ------------------------------------------------ Métodos opcionales (por defecto no hacen nada)

    protected void alMostrar() { }
    protected void alOcultar() { }
    protected void teclaPresionada(KeyEvent e) { }
    protected void teclaSoltada(KeyEvent e) { }
    protected void caracterEscrito(char c) { }
    protected void click(Point p) { }
    protected void rueda(int pasos) { }
}
