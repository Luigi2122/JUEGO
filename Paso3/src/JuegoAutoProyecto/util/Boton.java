package JuegoAutoProyecto.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;

/**
 * Botón dibujado a mano, que reacciona al mouse.
 *
 * <p>No usamos {@code JButton} porque queremos que el botón combine con el estilo
 * del juego. Cuando el mouse pasa por encima, el botón crece un poco y brilla
 * (animación de "hover").</p>
 */
public class Boton {

    private final String texto;
    private final String atajo;      // Tecla de atajo que se muestra a la derecha (puede ser null)
    private final Rectangle area;    // Zona clickeable
    private final Color color;
    private boolean encima;          // ¿El mouse está encima?
    private double animacion;        // 0 = normal, 1 = totalmente resaltado (se anima suavemente)

    /**
     * @param texto texto del botón
     * @param atajo tecla de atajo a mostrar (por ejemplo "R"), o {@code null}
     * @param x     posición X
     * @param y     posición Y
     * @param ancho ancho en píxeles
     * @param alto  alto en píxeles
     * @param color color principal del botón
     */
    public Boton(String texto, String atajo, int x, int y, int ancho, int alto, Color color) {
        this.texto = texto;
        this.atajo = atajo;
        this.area = new Rectangle(x, y, ancho, alto);
        this.color = color;
    }

    /** Actualiza si el mouse está encima del botón. */
    public void actualizarMouse(Point mouse) {
        encima = mouse != null && area.contains(mouse);
    }

    /** @return {@code true} si el punto está dentro del botón */
    public boolean contiene(Point p) {
        return p != null && area.contains(p);
    }

    public boolean isEncima() { return encima; }

    /** Dibuja el botón. */
    public void dibujar(Graphics2D g2) {
        animacion += ((encima ? 1 : 0) - animacion) * 0.25;      // Animación suave del hover

        double crecer = 3 * animacion;                           // Crece hasta 3 px por lado
        double x = area.x - crecer, y = area.y - crecer;
        double w = area.width + crecer * 2, h = area.height + crecer * 2;
        RoundRectangle2D forma = new RoundRectangle2D.Double(x, y, w, h, h * 0.55, h * 0.55);

        // Resplandor cuando el mouse está encima
        if (animacion > 0.02) {
            for (int i = 3; i >= 1; i--) {
                g2.setColor(Estilo.conAlfa(color, (int) (28 * animacion / i)));
                g2.fill(new RoundRectangle2D.Double(x - i * 4, y - i * 4, w + i * 8, h + i * 8,
                        h * 0.55 + i * 8, h * 0.55 + i * 8));
            }
        }
        // Sombra
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fill(new RoundRectangle2D.Double(x, y + 5, w, h, h * 0.55, h * 0.55));
        // Relleno con degradado vertical (más claro arriba)
        Color claro = Estilo.mezclar(color, Color.WHITE, 0.18 + 0.12 * animacion);
        Color oscuro = Estilo.mezclar(color, Color.BLACK, 0.25);
        g2.setPaint(new GradientPaint(0, (float) y, claro, 0, (float) (y + h), oscuro));
        g2.fill(forma);
        // Brillo de "vidrio" en la mitad superior
        g2.setPaint(new GradientPaint(0, (float) y, new Color(255, 255, 255, 70),
                0, (float) (y + h / 2), new Color(255, 255, 255, 0)));
        g2.fill(new RoundRectangle2D.Double(x + 2, y + 2, w - 4, h / 2, h * 0.5, h * 0.5));
        // Borde
        g2.setColor(new Color(255, 255, 255, 70));
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(forma);

        // Texto (y tecla de atajo si la tiene)
        g2.setFont(Estilo.negrita((float) Math.max(13, h * 0.36)));
        int anchoTexto = Estilo.anchoTexto(g2, texto);
        int anchoAtajo = 0;
        if (atajo != null) {
            g2.setFont(Estilo.negrita(11));
            anchoAtajo = Estilo.anchoTexto(g2, atajo) + 16;
            g2.setFont(Estilo.negrita((float) Math.max(13, h * 0.36)));
        }
        double total = anchoTexto + (atajo != null ? anchoAtajo + 8 : 0);
        double tx = x + (w - total) / 2;
        double baseY = y + h / 2 + g2.getFontMetrics().getAscent() * 0.36;
        Estilo.textoConSombra(g2, texto, tx, baseY, Color.WHITE);

        if (atajo != null) {
            double ax = tx + anchoTexto + 8, ay = y + h / 2 - 10;
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fill(new RoundRectangle2D.Double(ax, ay, anchoAtajo, 20, 8, 8));
            g2.setColor(new Color(255, 255, 255, 120));
            g2.draw(new RoundRectangle2D.Double(ax, ay, anchoAtajo, 20, 8, 8));
            g2.setFont(Estilo.negrita(11));
            g2.setColor(Color.WHITE);
            Estilo.textoCentrado(g2, atajo, ax + anchoAtajo / 2.0, ay + 14.5);
        }
    }
}
