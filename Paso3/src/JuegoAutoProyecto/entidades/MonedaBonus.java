package JuegoAutoProyecto.entidades;

import JuegoAutoProyecto.util.Config;
import JuegoAutoProyecto.util.Recursos;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * Moneda que da puntos extra y recarga el turbo.
 *
 * <p>Animación de <b>giro</b>: el ancho del dibujo se calcula con
 * {@code |cos(fase)|}, así la moneda parece rotar sobre su eje. Además tiene un
 * resplandor dorado que "late".</p>
 */
public class MonedaBonus extends EntidadJuego {

    public static final int TAMANO = 30;

    private static final BufferedImage SPRITE = Recursos.imagen("moneda.png");
    private double fase;   // Ángulo de giro actual

    /**
     * @param carril carril donde aparece (0 a 3)
     * @param y      posición vertical inicial
     */
    public MonedaBonus(int carril, double y) {
        super(Config.centroCarril(carril) - TAMANO / 2.0, y, TAMANO, TAMANO);
        this.fase = Math.random() * Math.PI;
    }

    @Override
    public void actualizar(double velocidadPista) {
        y += velocidadPista * 0.8;   // Misma velocidad que los rivales: nunca se encima con ellos
        fase += 0.09;
    }

    @Override
    public void dibujar(Graphics2D g2) {
        double cx = getCentroX(), cy = getCentroY();

        // Resplandor que late
        float radio = (float) (26 + 5 * Math.sin(fase * 2));
        g2.setPaint(new RadialGradientPaint(new Point2D.Double(cx, cy), radio, new float[]{0f, 1f},
                new Color[]{new Color(253, 224, 71, 120), new Color(253, 224, 71, 0)}));
        g2.fill(new Ellipse2D.Double(cx - radio, cy - radio, radio * 2, radio * 2));

        // Sombra en el piso
        g2.setColor(new Color(0, 0, 0, 55));
        g2.fill(new Ellipse2D.Double(cx - 12, cy + 12, 24, 7));

        // Moneda girando: el ancho cambia con el coseno
        double anchoVisible = Math.max(0.12, Math.abs(Math.cos(fase))) * ancho;
        double flotar = Math.sin(fase * 1.5) * 2;   // Sube y baja un poquito
        if (SPRITE != null) {
            g2.drawImage(SPRITE, (int) (cx - anchoVisible / 2), (int) (y + flotar),
                    (int) Math.max(2, anchoVisible), alto, null);
        } else {
            g2.setColor(new Color(250, 204, 21));
            g2.fill(new Ellipse2D.Double(cx - anchoVisible / 2, y + flotar, anchoVisible, alto));
            g2.setColor(new Color(161, 98, 7));
            g2.setStroke(new BasicStroke(2));
            g2.draw(new Ellipse2D.Double(cx - anchoVisible / 2, y + flotar, anchoVisible, alto));
        }
    }
}
