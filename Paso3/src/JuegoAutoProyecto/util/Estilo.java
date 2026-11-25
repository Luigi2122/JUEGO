package JuegoAutoProyecto.util;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Colores, tipografías y funciones de dibujo que se repiten en todas las pantallas.
 *
 * <p>Centralizar el estilo aquí logra que todo el juego se vea coherente: si cambias
 * un color en esta clase, cambia en todas las pantallas a la vez.</p>
 */
public final class Estilo {

    private Estilo() { }

    // ------------------------------------------------------------------ Colores
    public static final Color FONDO = new Color(9, 12, 28);
    public static final Color VIDRIO = new Color(13, 20, 40, 190);
    public static final Color VIDRIO_BORDE = new Color(255, 255, 255, 45);
    public static final Color AMARILLO = new Color(250, 204, 21);
    public static final Color NARANJA = new Color(249, 115, 22);
    public static final Color ROJO = new Color(239, 68, 68);
    public static final Color ROSA = new Color(244, 63, 94);
    public static final Color CIAN = new Color(56, 189, 248);
    public static final Color VERDE = new Color(34, 197, 94);
    public static final Color MORADO = new Color(168, 85, 247);
    public static final Color TEXTO = Color.WHITE;
    public static final Color TEXTO_SUAVE = new Color(203, 213, 225);
    public static final Color TEXTO_APAGADO = new Color(148, 163, 184);

    // ------------------------------------------------------------------ Fuentes
    private static final String FUENTE_TITULO = "Anton-Regular.ttf";
    private static final String FUENTE_NEGRITA = "Poppins-Bold.ttf";
    private static final String FUENTE_NORMAL = "Poppins-Regular.ttf";

    /** Fuente grande y condensada para títulos y números (Anton). */
    public static Font titulo(float tamano) { return Recursos.fuente(FUENTE_TITULO, tamano); }

    /** Fuente en negrita para botones y etiquetas (Poppins Bold). */
    public static Font negrita(float tamano) { return Recursos.fuente(FUENTE_NEGRITA, tamano); }

    /** Fuente normal para textos (Poppins Regular). */
    public static Font normal(float tamano) { return Recursos.fuente(FUENTE_NORMAL, tamano); }

    // ------------------------------------------------------------------ Calidad
    /** Activa el suavizado (antialiasing) para que bordes y textos no se vean "pixelados". */
    public static void activarCalidad(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    // ------------------------------------------------------------------ Texto
    /** Ancho en píxeles que ocupa un texto con la fuente actual. */
    public static int anchoTexto(Graphics2D g2, String texto) {
        return g2.getFontMetrics().stringWidth(texto);
    }

    /** Corta un texto con "…" al final si no entra en el ancho indicado (con la fuente actual). */
    public static String recortar(Graphics2D g2, String texto, int anchoMaximo) {
        if (anchoTexto(g2, texto) <= anchoMaximo) return texto;
        String recortado = texto;
        while (recortado.length() > 1 && anchoTexto(g2, recortado + "…") > anchoMaximo) {
            recortado = recortado.substring(0, recortado.length() - 1);
        }
        return recortado + "…";
    }

    /** Dibuja un texto centrado horizontalmente en {@code cx}. */
    public static void textoCentrado(Graphics2D g2, String texto, double cx, double yBase) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(texto, (float) (cx - fm.stringWidth(texto) / 2.0), (float) yBase);
    }

    /** Dibuja un texto con una sombra suave debajo (mejora la lectura sobre fondos claros). */
    public static void textoConSombra(Graphics2D g2, String texto, double x, double yBase, Color color) {
        g2.setColor(new Color(0, 0, 0, 140));
        g2.drawString(texto, (float) x + 2, (float) yBase + 3);
        g2.setColor(color);
        g2.drawString(texto, (float) x, (float) yBase);
    }

    /** Igual que {@link #textoConSombra} pero centrado en {@code cx}. */
    public static void textoCentradoConSombra(Graphics2D g2, String texto, double cx, double yBase, Color color) {
        double x = cx - anchoTexto(g2, texto) / 2.0;
        textoConSombra(g2, texto, x, yBase, color);
    }

    /**
     * Dibuja un texto centrado con un "brillo" (resplandor) de color alrededor.
     * El brillo se logra dibujando el texto varias veces, desplazado y muy transparente.
     */
    public static void textoBrillante(Graphics2D g2, String texto, double cx, double yBase,
                                      Color color, Color brillo, int radio) {
        double x = cx - anchoTexto(g2, texto) / 2.0;
        Composite original = g2.getComposite();
        g2.setColor(brillo);
        for (int r = radio; r >= 1; r -= 2) {
            float alfa = 0.10f * (1f - (float) r / (radio + 1));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.02f, alfa)));
            for (int ang = 0; ang < 360; ang += 45) {
                double rad = Math.toRadians(ang);
                g2.drawString(texto, (float) (x + Math.cos(rad) * r), (float) (yBase + Math.sin(rad) * r));
            }
        }
        g2.setComposite(original);
        textoConSombra(g2, texto, x, yBase, color);
    }

    // ------------------------------------------------------------------ Paneles
    /**
     * Dibuja un panel tipo "vidrio esmerilado": fondo oscuro semitransparente,
     * un brillo suave arriba y un borde fino.
     */
    public static void panelVidrio(Graphics2D g2, double x, double y, double w, double h, double radio) {
        RoundRectangle2D forma = new RoundRectangle2D.Double(x, y, w, h, radio, radio);
        g2.setColor(new Color(0, 0, 0, 70));                       // Sombra del panel
        g2.fill(new RoundRectangle2D.Double(x + 3, y + 6, w, h, radio, radio));
        g2.setColor(VIDRIO);                                       // Fondo
        g2.fill(forma);
        g2.setPaint(new GradientPaint(0, (float) y, new Color(255, 255, 255, 38),
                0, (float) (y + h * 0.5), new Color(255, 255, 255, 0)));
        g2.fill(forma);                                            // Brillo superior
        g2.setColor(VIDRIO_BORDE);
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(forma);                                            // Borde
    }

    /** Dibuja una "tecla" de teclado con una letra o símbolo (para las instrucciones). */
    public static int tecla(Graphics2D g2, String texto, double x, double y) {
        Font fuente = negrita(12);
        if (fuente.canDisplayUpTo(texto) != -1) {           // Si la fuente no tiene el símbolo (ej: flechas)...
            fuente = new Font("Dialog", Font.BOLD, 13);      // ...usamos una fuente del sistema que sí lo tiene
        }
        g2.setFont(fuente);
        int w = Math.max(26, anchoTexto(g2, texto) + 14);
        RoundRectangle2D forma = new RoundRectangle2D.Double(x, y, w, 24, 8, 8);
        g2.setColor(new Color(15, 23, 42, 230));
        g2.fill(new RoundRectangle2D.Double(x, y + 3, w, 24, 8, 8));
        g2.setPaint(new GradientPaint(0, (float) y, new Color(71, 85, 105), 0, (float) y + 24, new Color(51, 65, 85)));
        g2.fill(forma);
        g2.setColor(new Color(255, 255, 255, 60));
        g2.draw(forma);
        g2.setColor(Color.WHITE);
        textoCentrado(g2, texto, x + w / 2.0, y + 17);
        return w;
    }

    // ------------------------------------------------------------------ Íconos
    /** Dibuja un trofeo dorado (para récords) con su esquina superior izquierda en (x, y). */
    public static void trofeo(Graphics2D g2, double x, double y, double tam) {
        double s = tam / 20.0;
        g2.setPaint(new GradientPaint((float) x, (float) y, new Color(253, 230, 138),
                (float) (x + tam), (float) (y + tam), new Color(202, 138, 4)));
        Path2D copa = new Path2D.Double();
        copa.moveTo(x + 4 * s, y + 1 * s);
        copa.lineTo(x + 16 * s, y + 1 * s);
        copa.curveTo(x + 16 * s, y + 9 * s, x + 13 * s, y + 12 * s, x + 10 * s, y + 12 * s);
        copa.curveTo(x + 7 * s, y + 12 * s, x + 4 * s, y + 9 * s, x + 4 * s, y + 1 * s);
        copa.closePath();
        g2.fill(copa);
        g2.setStroke(new BasicStroke((float) (1.6 * s)));
        g2.draw(new Ellipse2D.Double(x + 0.5 * s, y + 2.5 * s, 5 * s, 5 * s));   // Asa izquierda
        g2.draw(new Ellipse2D.Double(x + 14.5 * s, y + 2.5 * s, 5 * s, 5 * s));  // Asa derecha
        g2.fill(new RoundRectangle2D.Double(x + 8.5 * s, y + 11 * s, 3 * s, 4 * s, s, s));   // Tallo
        g2.fill(new RoundRectangle2D.Double(x + 5 * s, y + 15 * s, 10 * s, 4 * s, 2 * s, 2 * s)); // Base
    }

    /** Dibuja una pequeña moneda dorada con su esquina superior izquierda en (x, y). */
    public static void iconoMoneda(Graphics2D g2, double x, double y, double tam) {
        g2.setPaint(new GradientPaint((float) x, (float) y, new Color(254, 240, 138),
                (float) (x + tam), (float) (y + tam), new Color(202, 138, 4)));
        g2.fill(new Ellipse2D.Double(x, y, tam, tam));
        g2.setColor(new Color(161, 98, 7));
        g2.setStroke(new BasicStroke((float) Math.max(1, tam / 12)));
        g2.draw(new Ellipse2D.Double(x + tam * 0.18, y + tam * 0.18, tam * 0.64, tam * 0.64));
    }

    // ------------------------------------------------------------------ Matemática de animación
    /** Mezcla dos colores: t = 0 devuelve a, t = 1 devuelve b. */
    public static Color mezclar(Color a, Color b, double t) {
        t = Math.max(0, Math.min(1, t));
        return new Color(
                (int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t),
                (int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
    }

    /** Devuelve el mismo color con otra transparencia (0 = invisible, 255 = sólido). */
    public static Color conAlfa(Color c, int alfa) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, alfa)));
    }

    /**
     * Curva de animación "ease out back": avanza rápido, se pasa un poquito y vuelve.
     * Da un efecto de "rebote" muy agradable al aparecer paneles.
     *
     * @param t progreso entre 0 y 1
     */
    public static double easeOutBack(double t) {
        t = Math.max(0, Math.min(1, t));
        double c1 = 1.70158, c3 = c1 + 1;
        return 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
    }

    /** Curva "ease out cubic": empieza rápido y frena suavemente al final. */
    public static double easeOutCubic(double t) {
        t = Math.max(0, Math.min(1, t));
        return 1 - Math.pow(1 - t, 3);
    }
}
