package JuegoAutoProyecto.entidades;

import JuegoAutoProyecto.util.Recursos;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

/**
 * Árboles, pinos, arbustos y rocas que pasan al costado de la pista.
 *
 * <p>No chocan con nada: solo están para que el escenario se sienta vivo y
 * para que se note la velocidad. Se mueven exactamente a la velocidad de la pista
 * porque están "pegados al suelo".</p>
 */
public class Decoracion extends EntidadJuego {

    /** Tipos de decoración disponibles. */
    public enum Tipo {
        ARBOL("arbol.png", 74),
        PINO("pino.png", 64),
        ARBUSTO("arbusto.png", 42),
        ROCA("roca.png", 34);

        final String imagen;
        final int tamano;

        Tipo(String imagen, int tamano) {
            this.imagen = imagen;
            this.tamano = tamano;
        }

        public int getTamano() { return tamano; }
    }

    private final Tipo tipo;
    private final BufferedImage sprite;

    public Decoracion(Tipo tipo, double x, double y) {
        super(x, y, tipo.tamano, tipo.tamano);
        this.tipo = tipo;
        this.sprite = Recursos.imagen(tipo.imagen);
    }

    @Override
    public void actualizar(double velocidadPista) {
        y += velocidadPista;
    }

    @Override
    public void dibujar(Graphics2D g2) {
        // Sombra ovalada hacia abajo a la derecha (el "sol" está arriba a la izquierda)
        g2.setColor(new Color(0, 0, 0, 55));
        g2.fill(new Ellipse2D.Double(x + ancho * 0.18, y + alto * 0.25, ancho * 0.95, alto * 0.9));
        if (sprite != null) {
            g2.drawImage(sprite, (int) x, (int) y, ancho, alto, null);
        } else {
            g2.setColor(tipo == Tipo.ROCA ? new Color(120, 113, 108) : new Color(21, 128, 61));
            g2.fill(new Ellipse2D.Double(x, y, ancho, alto));
        }
    }
}
