package JuegoAutoProyecto.efectos;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

/**
 * Una partícula es un circulito que vive unos instantes: se mueve, cambia de tamaño
 * y se desvanece. Con cientos de ellas se arman efectos como humo, fuego,
 * chispas o una explosión.
 */
public class Particula {

    private double x, y;          // Posición del centro
    private double vx, vy;        // Velocidad (píxeles por fotograma)
    private double tamano;        // Diámetro actual
    private final double crecimiento; // Cuánto cambia el tamaño por fotograma (+ crece, - se achica)
    private final double friccion;    // 1 = no frena, 0.9 = frena rápido
    private final double gravedad;    // Aceleración vertical
    private final int vidaMaxima;
    private int vida;                 // Fotogramas que le quedan
    private final Color colorInicial;
    private final Color colorFinal;

    public Particula(double x, double y, double vx, double vy, double tamano, double crecimiento,
                     double friccion, double gravedad, int vida, Color colorInicial, Color colorFinal) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.tamano = tamano;
        this.crecimiento = crecimiento;
        this.friccion = friccion;
        this.gravedad = gravedad;
        this.vida = vida;
        this.vidaMaxima = vida;
        this.colorInicial = colorInicial;
        this.colorFinal = colorFinal;
    }

    /** Mueve la partícula un fotograma. */
    public void actualizar() {
        vx *= friccion;
        vy = vy * friccion + gravedad;
        x += vx;
        y += vy;
        tamano = Math.max(0.5, tamano + crecimiento);
        vida--;
    }

    /** @return {@code true} cuando la partícula ya terminó su vida y debe borrarse */
    public boolean estaMuerta() {
        return vida <= 0;
    }

    /** Dibuja la partícula: su color cambia y se vuelve transparente a medida que envejece. */
    public void dibujar(Graphics2D g2) {
        double progreso = 1.0 - (double) vida / vidaMaxima;     // 0 al nacer, 1 al morir
        int r = (int) (colorInicial.getRed() + (colorFinal.getRed() - colorInicial.getRed()) * progreso);
        int g = (int) (colorInicial.getGreen() + (colorFinal.getGreen() - colorInicial.getGreen()) * progreso);
        int b = (int) (colorInicial.getBlue() + (colorFinal.getBlue() - colorInicial.getBlue()) * progreso);
        int alfa = (int) (colorInicial.getAlpha() * (1.0 - progreso));
        g2.setColor(new Color(r, g, b, Math.max(0, Math.min(255, alfa))));
        g2.fill(new Ellipse2D.Double(x - tamano / 2, y - tamano / 2, tamano, tamano));
    }
}
