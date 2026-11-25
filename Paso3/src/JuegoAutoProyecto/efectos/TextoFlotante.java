package JuegoAutoProyecto.efectos;

import JuegoAutoProyecto.util.Estilo;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
 * Texto que aparece con un pequeño "salto", sube y se desvanece.
 * Se usa para mostrar "+25" al recoger una moneda o "¡NIVEL 3!" al subir de nivel.
 */
public class TextoFlotante {

    private final String texto;
    private final double x;
    private double y;
    private final Color color;
    private final float tamano;
    private final int vidaMaxima;
    private int vida;
    private final double velocidadSubida;

    /**
     * @param texto  lo que se muestra
     * @param x      centro horizontal
     * @param y      posición vertical inicial
     * @param color  color del texto
     * @param tamano tamaño de la fuente
     * @param vida   cuántos fotogramas dura
     */
    public TextoFlotante(String texto, double x, double y, Color color, float tamano, int vida) {
        this.texto = texto;
        this.x = x;
        this.y = y;
        this.color = color;
        this.tamano = tamano;
        this.vida = vida;
        this.vidaMaxima = vida;
        this.velocidadSubida = tamano > 30 ? 0.4 : 1.1;   // Los textos grandes suben más lento
    }

    public void actualizar() {
        y -= velocidadSubida;
        vida--;
    }

    public boolean estaMuerto() {
        return vida <= 0;
    }

    public void dibujar(Graphics2D g2) {
        double edad = vidaMaxima - vida;
        // Escala: empieza en 0.4, se pasa a ~1.15 y vuelve a 1 (efecto "pop")
        double escala = 0.4 + 0.6 * Estilo.easeOutBack(Math.min(1, edad / 12.0));
        // Transparencia: se desvanece durante el último tercio de su vida
        float alfa = (float) Math.min(1.0, vida / (vidaMaxima / 3.0));

        Composite original = g2.getComposite();
        AffineTransform transformacion = g2.getTransform();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, alfa)));
        g2.translate(x, y);
        g2.scale(escala, escala);
        g2.setFont(Estilo.titulo(tamano));
        if (tamano > 30) {
            Estilo.textoBrillante(g2, texto, 0, 0, color, color, 10);
        } else {
            Estilo.textoCentradoConSombra(g2, texto, 0, 0, color);
        }
        g2.setTransform(transformacion);
        g2.setComposite(original);
    }
}
