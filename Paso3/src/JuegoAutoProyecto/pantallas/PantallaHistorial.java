package JuegoAutoProyecto.pantallas;

import JuegoAutoProyecto.escenario.Pista;
import JuegoAutoProyecto.historial.HistorialPartidas;
import JuegoAutoProyecto.historial.Partida;
import JuegoAutoProyecto.util.Boton;
import JuegoAutoProyecto.util.Config;
import JuegoAutoProyecto.util.Estilo;
import JuegoAutoProyecto.util.Recursos;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Muestra el historial de todas las partidas jugadas mientras el juego está abierto.
 *
 * <p>Arriba hay un resumen (partidas, récord y monedas totales) y abajo una tabla
 * que se puede ordenar por "más recientes" o por "ranking" (mayor puntaje). Si hay
 * muchas partidas, la tabla se desplaza con la rueda del mouse o las flechas.</p>
 */
public class PantallaHistorial extends Pantalla {

    private static final int TABLA_X = 20, TABLA_Y = 212, TABLA_W = 480, TABLA_H = 372;
    private static final int ALTO_FILA = 38;
    private static final int INICIO_FILAS = TABLA_Y + 48;              // Debajo del encabezado
    private static final int ALTO_VISIBLE = TABLA_H - 48 - 12;          // Espacio para las filas

    private final Pista pista = new Pista();
    private final Boton botonVolver;
    private final Boton botonVerRanking;
    private final Boton botonVerRecientes;
    private final BufferedImage imagenAuto = Recursos.imagen("auto_jugador.png");

    private boolean ordenarPorPuntaje = false;
    private double scrollActual;      // Desplazamiento que se ve (se anima)
    private double scrollDeseado;     // Desplazamiento al que queremos llegar

    public PantallaHistorial(VentanaJuego ventana) {
        super(ventana);
        botonVolver = new Boton("Volver", "ESC", 40, 602, 200, 50, Estilo.ROSA);
        botonVerRanking = new Boton("Ver ranking", "O", 280, 602, 200, 50, Estilo.CIAN);
        botonVerRecientes = new Boton("Ver recientes", "O", 280, 602, 200, 50, Estilo.CIAN);
    }

    @Override
    protected void alMostrar() {
        scrollActual = scrollDeseado = 0;
    }

    private Boton botonOrden() {
        return ordenarPorPuntaje ? botonVerRecientes : botonVerRanking;
    }

    private List<Partida> partidasOrdenadas() {
        HistorialPartidas h = ventana.getHistorial();
        return ordenarPorPuntaje ? h.getPorPuntaje() : h.getRecientes();
    }

    // =================================================================== Lógica

    @Override
    protected void actualizar() {
        pista.avanzar(1.5);
        double maximo = Math.max(0, ventana.getHistorial().cantidad() * ALTO_FILA - ALTO_VISIBLE);
        scrollDeseado = Math.max(0, Math.min(maximo, scrollDeseado));
        scrollActual += (scrollDeseado - scrollActual) * 0.25;   // Desplazamiento suave
        actualizarBotones(botonVolver, botonOrden());
    }

    private void cambiarOrden() {
        ordenarPorPuntaje = !ordenarPorPuntaje;
        scrollActual = scrollDeseado = 0;
        ticks = 0;   // Repite la animación de entrada de las filas
    }

    @Override
    protected void teclaPresionada(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
            case KeyEvent.VK_BACK_SPACE:
                ventana.volverDelHistorial();
                break;
            case KeyEvent.VK_O:
                cambiarOrden();
                break;
            case KeyEvent.VK_DOWN:
                scrollDeseado += ALTO_FILA;
                break;
            case KeyEvent.VK_UP:
                scrollDeseado -= ALTO_FILA;
                break;
            default:
                break;
        }
    }

    @Override
    protected void rueda(int pasos) {
        scrollDeseado += pasos * ALTO_FILA;
    }

    @Override
    protected void click(Point p) {
        if (botonVolver.contiene(p)) ventana.volverDelHistorial();
        else if (botonOrden().contiene(p)) cambiarOrden();
    }

    // =================================================================== Dibujo

    @Override
    protected void dibujar(Graphics2D g2) {
        pista.dibujar(g2);
        pista.dibujarVineta(g2);
        g2.setColor(new Color(9, 12, 28, 200));
        g2.fillRect(0, 0, Config.ANCHO, Config.ALTO);

        double entrada = Estilo.easeOutBack(Math.min(1, ticks / 30.0));
        g2.setFont(Estilo.titulo(56));
        Estilo.textoBrillante(g2, "HISTORIAL", Config.ANCHO / 2.0, 20 + 58 * entrada, Estilo.AMARILLO, Estilo.MORADO, 12);
        g2.setFont(Estilo.normal(12));
        g2.setColor(Estilo.TEXTO_SUAVE);
        Estilo.textoCentrado(g2, "Partidas de esta sesión  ·  se guardan mientras el juego esté abierto",
                Config.ANCHO / 2.0, 104);

        dibujarResumen(g2);
        dibujarTabla(g2);

        botonVolver.dibujar(g2);
        botonOrden().dibujar(g2);
        g2.setFont(Estilo.normal(11));
        g2.setColor(Estilo.TEXTO_APAGADO);
        Estilo.textoCentrado(g2, "Usa la rueda del mouse o las flechas arriba/abajo para desplazarte",
                Config.ANCHO / 2.0, 672);
    }

    /** Tres tarjetas con los totales de la sesión. */
    private void dibujarResumen(Graphics2D g2) {
        HistorialPartidas h = ventana.getHistorial();
        Partida mejor = h.getMejor();
        String[] titulos = {"PARTIDAS", "RÉCORD", "MONEDAS"};
        String[] valores = {String.valueOf(h.cantidad()), mejor == null ? "-" : String.valueOf(mejor.getPuntaje()),
                String.valueOf(h.totalMonedas())};
        String[] detalles = {"en esta sesión", mejor == null ? "sin récord aún" : mejor.getNombreJugador(), "recogidas en total"};
        Color[] colores = {Estilo.CIAN, Estilo.AMARILLO, Estilo.VERDE};

        for (int i = 0; i < 3; i++) {
            double aparicion = Estilo.easeOutCubic((ticks - 5 - i * 5) / 20.0);
            double x = 25 + i * 160, y = 124 + (1 - aparicion) * 20;
            Composite original = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) Math.max(0, aparicion)));
            Estilo.panelVidrio(g2, x, y, 150, 74, 20);
            g2.setColor(colores[i]);
            g2.fill(new RoundRectangle2D.Double(x + 14, y + 12, 4, 50, 4, 4));
            g2.setFont(Estilo.negrita(9));
            g2.setColor(Estilo.TEXTO_APAGADO);
            g2.drawString(titulos[i], (float) x + 28, (float) y + 24);
            if (i == 1 && mejor != null) Estilo.trofeo(g2, x + 122, y + 12, 16);
            g2.setFont(Estilo.titulo(26));
            Estilo.textoConSombra(g2, valores[i], x + 28, y + 52, colores[i]);
            g2.setFont(Estilo.normal(10));
            g2.setColor(Estilo.TEXTO_SUAVE);
            g2.drawString(Estilo.recortar(g2, detalles[i], 112), (float) x + 28, (float) y + 66);
            g2.setComposite(original);
        }
    }

    /** Tabla con una fila por partida. */
    private void dibujarTabla(Graphics2D g2) {
        Estilo.panelVidrio(g2, TABLA_X, TABLA_Y, TABLA_W, TABLA_H, 24);

        // Encabezado
        int[] columnas = {47, 70, 254, 320, 384, 452};   // Posiciones X de cada columna
        g2.setFont(Estilo.negrita(10));
        g2.setColor(Estilo.TEXTO_APAGADO);
        Estilo.textoCentrado(g2, ordenarPorPuntaje ? "POS." : "#", columnas[0], TABLA_Y + 30);
        g2.drawString("PILOTO", columnas[1], TABLA_Y + 30);
        Estilo.textoCentrado(g2, "PUNTOS", columnas[2], TABLA_Y + 30);
        Estilo.textoCentrado(g2, "MONEDAS", columnas[3], TABLA_Y + 30);
        Estilo.textoCentrado(g2, "TIEMPO", columnas[4], TABLA_Y + 30);
        Estilo.textoCentrado(g2, "HORA", columnas[5], TABLA_Y + 30);
        g2.setColor(new Color(255, 255, 255, 30));
        g2.fillRect(TABLA_X + 14, TABLA_Y + 40, TABLA_W - 28, 1);

        List<Partida> lista = partidasOrdenadas();
        if (lista.isEmpty()) {
            dibujarTablaVacia(g2);
            return;
        }

        HistorialPartidas h = ventana.getHistorial();
        Partida mejor = h.getMejor();
        int numeroUltima = h.cantidad();

        // Recorte: lo que salga de esta zona no se dibuja (así funciona el scroll)
        Shape recorteOriginal = g2.getClip();
        g2.clipRect(TABLA_X + 4, INICIO_FILAS - 4, TABLA_W - 8, ALTO_VISIBLE + 8);

        for (int i = 0; i < lista.size(); i++) {
            Partida p = lista.get(i);
            double y = INICIO_FILAS + i * ALTO_FILA - scrollActual;
            if (y < INICIO_FILAS - ALTO_FILA || y > INICIO_FILAS + ALTO_VISIBLE) continue;   // Fuera de vista

            // Animación de entrada: cada fila llega desde la derecha, un poquito después que la anterior
            double llegada = Estilo.easeOutCubic((ticks - 8 - i * 3) / 18.0);
            Composite original = g2.getComposite();
            AffineTransform t = g2.getTransform();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) Math.max(0, llegada)));
            g2.translate((1 - llegada) * 140, 0);

            boolean esMejor = p == mejor;
            boolean esUltima = p.getNumero() == numeroUltima;
            RoundRectangle2D fondo = new RoundRectangle2D.Double(TABLA_X + 10, y, TABLA_W - 26, ALTO_FILA - 6, 14, 14);
            if (esMejor) {
                g2.setPaint(new GradientPaint(TABLA_X, 0, new Color(250, 204, 21, 70),
                        TABLA_X + TABLA_W, 0, new Color(250, 204, 21, 10)));
            } else {
                g2.setColor(new Color(255, 255, 255, i % 2 == 0 ? 14 : 6));
            }
            g2.fill(fondo);
            if (esUltima) {                                                  // Barrita cian = última partida
                g2.setColor(Estilo.CIAN);
                g2.fill(new RoundRectangle2D.Double(TABLA_X + 10, y + 6, 4, ALTO_FILA - 18, 4, 4));
            }

            double base = y + ALTO_FILA / 2.0 + 2;
            // Columna 1: posición en el ranking (medallas) o número de partida
            if (ordenarPorPuntaje && i < 3) {
                Color[] medallas = {new Color(250, 204, 21), new Color(203, 213, 225), new Color(217, 119, 6)};
                g2.setColor(medallas[i]);
                g2.fill(new Ellipse2D.Double(columnas[0] - 11, base - 15, 22, 22));
                g2.setFont(Estilo.negrita(12));
                g2.setColor(new Color(30, 20, 5));
                Estilo.textoCentrado(g2, String.valueOf(i + 1), columnas[0], base);
            } else if (esMejor) {
                Estilo.trofeo(g2, columnas[0] - 9, base - 14, 18);
            } else {
                g2.setFont(Estilo.negrita(12));
                g2.setColor(Estilo.TEXTO_APAGADO);
                Estilo.textoCentrado(g2, ordenarPorPuntaje ? String.valueOf(i + 1) : "#" + p.getNumero(), columnas[0], base);
            }
            // Resto de columnas
            g2.setFont(Estilo.negrita(13));
            g2.setColor(Color.WHITE);
            g2.drawString(Estilo.recortar(g2, p.getNombreJugador(), 140), columnas[1], (float) base);
            g2.setFont(Estilo.titulo(18));
            g2.setColor(esMejor ? Estilo.AMARILLO : Color.WHITE);
            Estilo.textoCentrado(g2, String.valueOf(p.getPuntaje()), columnas[2], base + 1);
            g2.setFont(Estilo.negrita(12));
            Estilo.iconoMoneda(g2, columnas[3] - 20, base - 11, 12);
            g2.setColor(Estilo.TEXTO_SUAVE);
            g2.drawString(String.valueOf(p.getMonedas()), columnas[3] - 4, (float) base);
            g2.setFont(Estilo.normal(12));
            Estilo.textoCentrado(g2, p.getDuracionTexto(), columnas[4], base);
            g2.setColor(Estilo.TEXTO_APAGADO);
            Estilo.textoCentrado(g2, p.getHoraTexto(), columnas[5], base);

            g2.setTransform(t);
            g2.setComposite(original);
        }
        g2.setClip(recorteOriginal);
        dibujarBarraScroll(g2, lista.size());
    }

    /** Barrita a la derecha que indica qué parte de la lista se está viendo. */
    private void dibujarBarraScroll(Graphics2D g2, int filas) {
        double total = filas * ALTO_FILA;
        if (total <= ALTO_VISIBLE) return;
        double altoBarra = Math.max(30, ALTO_VISIBLE * ALTO_VISIBLE / total);
        double yBarra = INICIO_FILAS + (ALTO_VISIBLE - altoBarra) * (scrollActual / (total - ALTO_VISIBLE));
        g2.setColor(new Color(255, 255, 255, 25));
        g2.fill(new RoundRectangle2D.Double(TABLA_X + TABLA_W - 12, INICIO_FILAS, 5, ALTO_VISIBLE, 5, 5));
        g2.setColor(new Color(255, 255, 255, 110));
        g2.fill(new RoundRectangle2D.Double(TABLA_X + TABLA_W - 12, yBarra, 5, altoBarra, 5, 5));
    }

    /** Mensaje cuando todavía no se jugó ninguna partida. */
    private void dibujarTablaVacia(Graphics2D g2) {
        double cx = Config.ANCHO / 2.0, cy = TABLA_Y + TABLA_H / 2.0;
        if (imagenAuto != null) {
            AffineTransform t = g2.getTransform();
            g2.rotate(Math.sin(ticks * 0.05) * 0.12, cx, cy - 40);
            g2.drawImage(imagenAuto, (int) cx - 24, (int) cy - 83, 48, 86, null);
            g2.setTransform(t);
        }
        g2.setFont(Estilo.negrita(17));
        Estilo.textoCentradoConSombra(g2, "Todavía no hay partidas", cx, cy + 34, Color.WHITE);
        g2.setFont(Estilo.normal(12));
        g2.setColor(Estilo.TEXTO_SUAVE);
        Estilo.textoCentrado(g2, "Juega una carrera y aparecerá aquí.", cx, cy + 56);
    }
}
