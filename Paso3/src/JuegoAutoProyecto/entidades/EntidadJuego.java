package JuegoAutoProyecto.entidades;

import JuegoAutoProyecto.util.Config;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;

/**
 * Clase base (abstracta) de todo lo que se mueve en la pista.
 *
 * <p><b>Abstracta</b> significa que no se crean objetos {@code EntidadJuego} directamente:
 * sirve de "molde" para {@link AutoJugador}, {@link AutoRival}, {@link MonedaBonus} y
 * {@link Decoracion}. Todas comparten posición, tamaño y caja de colisión, pero cada
 * una decide <i>cómo se mueve</i> ({@link #actualizar}) y <i>cómo se dibuja</i>
 * ({@link #dibujar}). Eso es <b>polimorfismo</b>.</p>
 */
public abstract class EntidadJuego {

    protected double x, y;          // Esquina superior izquierda (double para movimiento suave)
    protected final int ancho, alto; // Tamaño en píxeles

    public EntidadJuego(double x, double y, int ancho, int alto) {
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.alto = alto;
    }

    /**
     * Mueve la entidad un fotograma.
     *
     * @param velocidadPista a qué velocidad avanza la pista en este momento
     */
    public abstract void actualizar(double velocidadPista);

    /** Dibuja la entidad en pantalla. */
    public abstract void dibujar(Graphics2D g2);

    /**
     * Caja de colisión: un rectángulo un poquito más chico que el dibujo, para que
     * los choques se sientan "justos" (no chocas por rozar una esquina redondeada).
     */
    public Rectangle getHitbox() {
        return new Rectangle((int) x + 4, (int) y + 6, ancho - 8, alto - 12);
    }

    /** @return {@code true} si la entidad toca a otra */
    public boolean colisionaCon(EntidadJuego otra) {
        return getHitbox().intersects(otra.getHitbox());
    }

    /** @return {@code true} cuando la entidad ya pasó por debajo de la pantalla */
    public boolean salioDePantalla() {
        return y > Config.ALTO + 20;
    }

    /** Dibuja una sombra suave debajo del objeto (le da sensación de profundidad). */
    protected void dibujarSombra(Graphics2D g2, double desplazamientoX, double desplazamientoY) {
        g2.setColor(new Color(0, 0, 0, 45));
        g2.fill(new RoundRectangle2D.Double(x + desplazamientoX - 2, y + desplazamientoY - 2,
                ancho + 4, alto + 4, 22, 22));
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fill(new RoundRectangle2D.Double(x + desplazamientoX, y + desplazamientoY, ancho, alto, 18, 18));
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getAncho() { return ancho; }
    public int getAlto() { return alto; }
    public double getCentroX() { return x + ancho / 2.0; }
    public double getCentroY() { return y + alto / 2.0; }
}
