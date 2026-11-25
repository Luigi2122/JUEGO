package JuegoAutoProyecto.entidades;

import JuegoAutoProyecto.util.Config;
import JuegoAutoProyecto.util.Recursos;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Auto del tráfico que el jugador debe esquivar.
 *
 * <p>Los rivales van más lento que el jugador, por eso en pantalla se ven "bajar".
 * Cada uno usa una imagen al azar (azul, verde, morado, naranja o taxi).</p>
 */
public class AutoRival extends EntidadJuego {

    public static final int ANCHO = 48;
    public static final int ALTO = 86;

    /** Qué tan rápido baja respecto a la pista (0.8 = un 20 % más lento que el asfalto). */
    private static final double FACTOR_VELOCIDAD = 0.8;

    /** Imágenes disponibles para los rivales (carpeta recursos/). */
    private static final String[] IMAGENES = {
            "rival_azul.png", "rival_verde.png", "rival_morado.png", "rival_naranja.png", "rival_taxi.png"
    };
    /** Colores de respaldo por si falta alguna imagen (mismo orden que IMAGENES). */
    private static final Color[] COLORES_RESPALDO = {
            new Color(59, 130, 246), new Color(16, 185, 129), new Color(168, 85, 247),
            new Color(249, 115, 22), new Color(250, 204, 21)
    };

    private final BufferedImage sprite;
    private final Color colorRespaldo;
    private final int carril;
    private boolean esquivado;   // ¿Ya se contaron los puntos por esquivarlo?
    private int animacion;       // Para el parpadeo de las luces de freno

    /**
     * @param carril carril donde aparece (0 a 3)
     * @param y      posición vertical inicial (normalmente arriba de la pantalla)
     * @param azar   generador de números aleatorios para elegir la imagen
     */
    public AutoRival(int carril, double y, Random azar) {
        super(Config.centroCarril(carril) - ANCHO / 2.0, y, ANCHO, ALTO);
        this.carril = carril;
        int indice = azar.nextInt(IMAGENES.length);
        this.sprite = Recursos.imagen(IMAGENES[indice]);
        this.colorRespaldo = COLORES_RESPALDO[indice];
        this.animacion = azar.nextInt(100);
    }

    @Override
    public void actualizar(double velocidadPista) {
        y += velocidadPista * FACTOR_VELOCIDAD;   // En Swing, la Y crece hacia abajo
        animacion++;
    }

    @Override
    public void dibujar(Graphics2D g2) {
        dibujarSombra(g2, 4, 7);
        if (sprite != null) {
            g2.drawImage(sprite, (int) x, (int) y, ancho, alto, null);
        } else {
            g2.setColor(colorRespaldo);
            g2.fill(new RoundRectangle2D.Double(x, y, ancho, alto, 14, 14));
            g2.setColor(new Color(30, 41, 59));
            g2.fill(new RoundRectangle2D.Double(x + 7, y + 52, ancho - 14, 14, 6, 6));
        }
        // Resplandor rojo de las luces traseras (parpadea suavemente, como frenando)
        int brillo = 70 + (int) (50 * Math.max(0, Math.sin(animacion * 0.08)));
        for (int lado = 0; lado < 2; lado++) {
            double lx = x + (lado == 0 ? 10 : ancho - 10);
            double ly = y + alto - 4;
            g2.setPaint(new RadialGradientPaint(new Point2D.Double(lx, ly), 16f, new float[]{0f, 1f},
                    new Color[]{new Color(255, 40, 40, brillo), new Color(255, 40, 40, 0)}));
            g2.fill(new Ellipse2D.Double(lx - 16, ly - 16, 32, 32));
        }
    }

    public int getCarril() { return carril; }
    public boolean isEsquivado() { return esquivado; }
    public void marcarEsquivado() { esquivado = true; }
}
