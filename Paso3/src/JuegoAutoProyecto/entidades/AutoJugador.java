package JuegoAutoProyecto.entidades;

import JuegoAutoProyecto.util.Config;
import JuegoAutoProyecto.util.Recursos;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * El auto que controla el jugador.
 *
 * <p>En vez de moverse "a saltos", tiene <b>aceleración</b>: al presionar una flecha
 * gana velocidad poco a poco y al soltarla frena suavemente. Además se
 * <b>inclina</b> hacia el lado al que gira, lo que da una animación muy natural.</p>
 */
public class AutoJugador extends EntidadJuego {

    public static final int ANCHO = 48;
    public static final int ALTO = 86;

    private static final double ACELERACION = 0.95;   // Cuánto acelera por fotograma
    private static final double VELOCIDAD_LATERAL_MAX = 7.5;
    private static final double FRENADO = 0.80;       // Al soltar la tecla, la velocidad se multiplica por esto

    private final BufferedImage sprite = Recursos.imagen("auto_jugador.png");
    private boolean moverIzquierda;
    private boolean moverDerecha;
    private boolean turbo;
    private double velocidadLateral;   // Velocidad horizontal actual
    private double inclinacion;        // Ángulo de inclinación en radianes
    private double giroChoque;         // Velocidad de giro cuando choca (efecto trompo)
    private int parpadeoFaros;         // Contador para animar el brillo de los faros

    /**
     * Crea el auto en el segundo carril, cerca de la parte de abajo de la pantalla.
     */
    public AutoJugador() {
        super(Config.centroCarril(1) - ANCHO / 2.0, Config.ALTO - 160, ANCHO, ALTO);
    }

    public void setMoverIzquierda(boolean valor) { moverIzquierda = valor; }
    public void setMoverDerecha(boolean valor) { moverDerecha = valor; }
    public void setTurbo(boolean valor) { turbo = valor; }

    /** Mueve el auto según las teclas presionadas (la velocidad de la pista no le afecta). */
    @Override
    public void actualizar(double velocidadPista) {
        if (moverIzquierda) velocidadLateral -= ACELERACION;
        if (moverDerecha) velocidadLateral += ACELERACION;
        if (moverIzquierda == moverDerecha) velocidadLateral *= FRENADO;   // Ninguna (o ambas): frena
        velocidadLateral = Math.max(-VELOCIDAD_LATERAL_MAX, Math.min(VELOCIDAD_LATERAL_MAX, velocidadLateral));

        x += velocidadLateral;

        // Límites: el auto no puede salir del asfalto
        double minX = Config.PISTA_X + 8;
        double maxX = Config.PISTA_X + Config.PISTA_ANCHO - ancho - 8;
        if (x < minX) { x = minX; velocidadLateral = 0; }
        if (x > maxX) { x = maxX; velocidadLateral = 0; }

        // La inclinación sigue a la velocidad lateral de forma suave
        double inclinacionDeseada = velocidadLateral * 0.035;
        inclinacion += (inclinacionDeseada - inclinacion) * 0.3;
        parpadeoFaros++;
    }

    /** Inicia el efecto "trompo" después de un choque. */
    public void chocar() {
        giroChoque = (velocidadLateral >= 0 ? 1 : -1) * 0.28;
        moverIzquierda = moverDerecha = turbo = false;
    }

    /** Animación después del choque: el auto gira cada vez más lento hasta detenerse. */
    public void actualizarChoque() {
        inclinacion += giroChoque;
        giroChoque *= 0.94;
        y += 0.6;
    }

    @Override
    public Rectangle getHitbox() {
        return new Rectangle((int) x + 5, (int) y + 8, ancho - 10, alto - 14);
    }

    /** Punto desde donde sale el humo del escape (parte trasera del auto). */
    public double getEscapeX() { return getCentroX() - Math.sin(inclinacion) * alto / 2; }
    public double getEscapeY() { return y + alto - 2; }

    @Override
    public void dibujar(Graphics2D g2) {
        dibujarSombra(g2, 5, 8);
        AffineTransform original = g2.getTransform();
        g2.rotate(inclinacion, getCentroX(), getCentroY());   // Inclina todo el dibujo del auto

        dibujarLucesFaros(g2);
        if (sprite != null) {
            g2.drawImage(sprite, (int) x, (int) y, ancho, alto, null);
        } else {
            dibujarRespaldo(g2);
        }
        g2.setTransform(original);
    }

    /** Conos de luz amarilla delante de los faros (más intensos con turbo). */
    private void dibujarLucesFaros(Graphics2D g2) {
        int intensidad = turbo ? 95 : 55 + (int) (8 * Math.sin(parpadeoFaros * 0.1));
        for (int lado = 0; lado < 2; lado++) {
            int fx = (int) (x + (lado == 0 ? 11 : ancho - 11));
            int fy = (int) y + 4;
            Polygon cono = new Polygon(
                    new int[]{fx - 4, fx + 4, fx + 22, fx - 22},
                    new int[]{fy, fy, fy - 110, fy - 110}, 4);
            g2.setPaint(new RadialGradientPaint(new Point2D.Double(fx, fy), 115f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(255, 247, 200, intensidad), new Color(255, 247, 200, 0)}));
            g2.fill(cono);
        }
    }

    /** Dibujo simple por si falta la imagen auto_jugador.png. */
    private void dibujarRespaldo(Graphics2D g2) {
        g2.setColor(new Color(220, 38, 38));
        g2.fill(new RoundRectangle2D.Double(x, y, ancho, alto, 16, 16));
        g2.setColor(new Color(186, 230, 253));
        g2.fill(new RoundRectangle2D.Double(x + 7, y + 22, ancho - 14, 18, 6, 6));
        g2.setColor(Color.WHITE);
        g2.fillRect((int) x + ancho / 2 - 3, (int) y + 2, 6, alto - 4);
    }
}
