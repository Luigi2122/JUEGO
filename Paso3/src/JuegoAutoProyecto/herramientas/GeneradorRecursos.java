package JuegoAutoProyecto.herramientas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * <b>Herramienta (no es parte del juego).</b> Dibuja las imágenes PNG de la carpeta
 * {@code recursos/}: los autos vistos desde arriba, la moneda y los árboles.
 *
 * <p>Las imágenes ya vienen generadas en el repositorio, así que <b>no hace falta
 * ejecutar esto para jugar</b>. Sirve si quieres cambiar colores o diseños: modifica
 * este archivo, ejecútalo (botón ▶ en IntelliJ) y las imágenes se vuelven a crear.</p>
 *
 * <p>Las imágenes se dibujan al doble de tamaño del que se usan en el juego para
 * que se vean nítidas.</p>
 */
public class GeneradorRecursos {

    private static final int W = 96, H = 172;   // Tamaño de los autos (el juego los dibuja a 48 x 86)

    public static void main(String[] args) throws IOException {
        File carpeta = new File(args.length > 0 ? args[0] : "src/JuegoAutoProyecto/recursos");
        if (!carpeta.isDirectory()) carpeta = new File("Paso3/src/JuegoAutoProyecto/recursos");
        if (!carpeta.isDirectory()) throw new IOException("No encuentro la carpeta recursos/");

        guardar(auto(new Color(225, 29, 72), Estilo.DEPORTIVO), carpeta, "auto_jugador.png");
        guardar(auto(new Color(37, 99, 235), Estilo.SEDAN), carpeta, "rival_azul.png");
        guardar(auto(new Color(5, 150, 105), Estilo.SEDAN), carpeta, "rival_verde.png");
        guardar(auto(new Color(147, 51, 234), Estilo.SEDAN), carpeta, "rival_morado.png");
        guardar(auto(new Color(234, 88, 12), Estilo.SEDAN), carpeta, "rival_naranja.png");
        guardar(auto(new Color(250, 204, 21), Estilo.TAXI), carpeta, "rival_taxi.png");
        guardar(moneda(), carpeta, "moneda.png");
        guardar(arbol(), carpeta, "arbol.png");
        guardar(pino(), carpeta, "pino.png");
        guardar(arbusto(), carpeta, "arbusto.png");
        guardar(roca(), carpeta, "roca.png");
        System.out.println("Imágenes generadas en: " + carpeta.getAbsolutePath());
    }

    private enum Estilo { DEPORTIVO, SEDAN, TAXI }

    private static void guardar(BufferedImage img, File carpeta, String nombre) throws IOException {
        ImageIO.write(img, "png", new File(carpeta, nombre));
        System.out.println("  OK " + nombre);
    }

    private static Graphics2D lienzo(BufferedImage img) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g;
    }

    private static Color oscurecer(Color c, double f) {
        return new Color((int) (c.getRed() * f), (int) (c.getGreen() * f), (int) (c.getBlue() * f));
    }

    private static Color aclarar(Color c, double f) {
        return new Color(c.getRed() + (int) ((255 - c.getRed()) * f), c.getGreen() + (int) ((255 - c.getGreen()) * f),
                c.getBlue() + (int) ((255 - c.getBlue()) * f));
    }

    // ===================================================================== AUTO (vista desde arriba)

    /** Dibuja un auto visto desde arriba con el frente hacia ARRIBA. */
    private static BufferedImage auto(Color color, Estilo estilo) {
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = lienzo(img);
        double l = 8, r = W - 8, t = 4, b = H - 4;      // Bordes de la carrocería

        // --- Ruedas (asoman a los costados)
        for (double ry : new double[]{26, H - 62}) {
            for (double rx : new double[]{l - 5, r - 9}) {
                g.setColor(new Color(15, 15, 18));
                g.fill(new RoundRectangle2D.Double(rx, ry, 14, 36, 8, 8));
                g.setColor(new Color(70, 70, 78));
                g.fill(new RoundRectangle2D.Double(rx + 4, ry + 4, 6, 28, 4, 4));
            }
        }

        // --- Carrocería: forma con frente y cola redondeados y "cintura" un poco más angosta
        Path2D cuerpo = new Path2D.Double();
        cuerpo.moveTo(l + 16, t);
        cuerpo.lineTo(r - 16, t);
        cuerpo.curveTo(r - 2, t, r, t + 10, r, t + 28);
        cuerpo.curveTo(r - 2, H * 0.45, r - 2, H * 0.55, r, b - 28);
        cuerpo.curveTo(r, b - 8, r - 4, b, r - 18, b);
        cuerpo.lineTo(l + 18, b);
        cuerpo.curveTo(l + 4, b, l, b - 8, l, b - 28);
        cuerpo.curveTo(l + 2, H * 0.55, l + 2, H * 0.45, l, t + 28);
        cuerpo.curveTo(l, t + 10, l + 2, t, l + 16, t);
        cuerpo.closePath();

        // Degradado de lado a lado: bordes oscuros y centro brillante = sensación de volumen
        g.setPaint(new LinearGradientPaint((float) l, 0, (float) r, 0,
                new float[]{0f, 0.2f, 0.5f, 0.8f, 1f},
                new Color[]{oscurecer(color, 0.5), color, aclarar(color, 0.28), color, oscurecer(color, 0.5)}));
        g.fill(cuerpo);
        // Brillo vertical (luz que viene de arriba)
        g.setPaint(new GradientPaint(0, (float) t, new Color(255, 255, 255, 60), 0, (float) (H * 0.35), new Color(255, 255, 255, 0)));
        g.fill(cuerpo);
        g.setStroke(new BasicStroke(2.2f));
        g.setColor(oscurecer(color, 0.38));
        g.draw(cuerpo);

        // --- Franjas deportivas (solo el auto del jugador)
        if (estilo == Estilo.DEPORTIVO) {
            g.setColor(new Color(255, 255, 255, 235));
            g.fill(new RoundRectangle2D.Double(W / 2.0 - 11, t + 2, 7, b - t - 4, 4, 4));
            g.fill(new RoundRectangle2D.Double(W / 2.0 + 4, t + 2, 7, b - t - 4, 4, 4));
        }

        // --- Líneas del capó
        g.setColor(new Color(0, 0, 0, 50));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(new java.awt.geom.QuadCurve2D.Double(l + 14, t + 40, W / 2.0, t + 30, r - 14, t + 40));

        // --- Parabrisas (más ancho adelante, más angosto hacia el techo)
        Polygon parabrisas = new Polygon(
                new int[]{(int) l + 9, (int) r - 9, (int) r - 15, (int) l + 15},
                new int[]{48, 48, 74, 74}, 4);
        g.setPaint(new GradientPaint(0, 48, new Color(30, 58, 95), 0, 74, new Color(12, 22, 40)));
        g.fill(parabrisas);
        g.setColor(new Color(255, 255, 255, 80));   // Reflejo diagonal en el vidrio
        g.fill(new Polygon(new int[]{(int) l + 16, (int) l + 30, (int) l + 24, (int) l + 13},
                new int[]{50, 50, 72, 72}, 4));
        g.setColor(oscurecer(color, 0.35));
        g.setStroke(new BasicStroke(1.6f));
        g.draw(parabrisas);

        // --- Techo
        RoundRectangle2D techo = new RoundRectangle2D.Double(l + 15, 75, r - l - 30, 38, 10, 10);
        g.setPaint(new LinearGradientPaint((float) (l + 15), 0, (float) (r - 15), 0, new float[]{0f, 0.5f, 1f},
                new Color[]{oscurecer(color, 0.75), aclarar(color, 0.15), oscurecer(color, 0.75)}));
        g.fill(techo);
        if (estilo == Estilo.DEPORTIVO) {
            g.setColor(new Color(255, 255, 255, 235));
            g.fill(new RoundRectangle2D.Double(W / 2.0 - 11, 75, 7, 38, 3, 3));
            g.fill(new RoundRectangle2D.Double(W / 2.0 + 4, 75, 7, 38, 3, 3));
        }
        if (estilo == Estilo.TAXI) {                                   // Letrero TAXI en el techo
            g.setColor(new Color(20, 20, 20));
            g.fill(new RoundRectangle2D.Double(W / 2.0 - 18, 85, 36, 16, 6, 6));
            g.setColor(new Color(253, 224, 71));
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            g.drawString("TAXI", (float) (W / 2.0 - 13), 97f);
            g.setColor(new Color(20, 20, 20));                           // Franja a cuadros en los costados
            for (int i = 0; i < 7; i++) {
                g.fillRect((int) l + 1, 60 + i * 8 + (i % 2) * 4, 4, 4);
                g.fillRect((int) r - 5, 60 + i * 8 + (i % 2) * 4, 4, 4);
            }
        }
        g.setColor(oscurecer(color, 0.4));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(techo);

        // --- Luneta trasera
        Polygon luneta = new Polygon(
                new int[]{(int) l + 15, (int) r - 15, (int) r - 11, (int) l + 11},
                new int[]{115, 115, 132, 132}, 4);
        g.setPaint(new GradientPaint(0, 115, new Color(12, 22, 40), 0, 132, new Color(35, 60, 95)));
        g.fill(luneta);
        g.setColor(oscurecer(color, 0.35));
        g.draw(luneta);

        // --- Espejos laterales
        g.setColor(oscurecer(color, 0.8));
        g.fill(new Ellipse2D.Double(l - 6, 56, 10, 7));
        g.fill(new Ellipse2D.Double(r - 4, 56, 10, 7));

        // --- Faros delanteros con brillo
        for (double fx : new double[]{l + 6, r - 22}) {
            g.setPaint(new RadialGradientPaint(new Point2D.Double(fx + 8, t + 7), 14f, new float[]{0f, 1f},
                    new Color[]{new Color(255, 251, 220, 200), new Color(255, 251, 220, 0)}));
            g.fill(new Ellipse2D.Double(fx - 6, t - 6, 28, 26));
            g.setColor(new Color(255, 253, 235));
            g.fill(new RoundRectangle2D.Double(fx, t + 3, 16, 8, 6, 6));
        }

        // --- Luces traseras
        for (double fx : new double[]{l + 6, r - 24}) {
            g.setColor(new Color(127, 29, 29));
            g.fill(new RoundRectangle2D.Double(fx, b - 10, 18, 7, 4, 4));
            g.setColor(new Color(248, 113, 113));
            g.fill(new RoundRectangle2D.Double(fx + 2, b - 9, 14, 4, 3, 3));
        }

        g.dispose();
        return img;
    }

    // ===================================================================== MONEDA

    private static BufferedImage moneda() {
        int s = 64;
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = lienzo(img);
        g.setPaint(new RadialGradientPaint(new Point2D.Double(s * 0.38, s * 0.32), s * 0.7f,
                new float[]{0f, 0.6f, 1f},
                new Color[]{new Color(254, 249, 195), new Color(250, 204, 21), new Color(180, 110, 5)}));
        g.fill(new Ellipse2D.Double(1, 1, s - 2, s - 2));
        g.setColor(new Color(146, 84, 6));
        g.setStroke(new BasicStroke(3f));
        g.draw(new Ellipse2D.Double(2, 2, s - 4, s - 4));
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(202, 138, 4));
        g.draw(new Ellipse2D.Double(9, 9, s - 18, s - 18));

        // Estrella en relieve
        Path2D estrella = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double ang = -Math.PI / 2 + i * Math.PI / 5;
            double rad = i % 2 == 0 ? 17 : 7.5;
            double x = s / 2.0 + Math.cos(ang) * rad, y = s / 2.0 + 1 + Math.sin(ang) * rad;
            if (i == 0) estrella.moveTo(x, y); else estrella.lineTo(x, y);
        }
        estrella.closePath();
        g.setColor(new Color(161, 98, 7));
        g.translate(1, 2);
        g.fill(estrella);
        g.translate(-1, -2);
        g.setPaint(new GradientPaint(0, 14, new Color(255, 250, 210), 0, 50, new Color(234, 179, 8)));
        g.fill(estrella);

        g.setColor(new Color(255, 255, 255, 150));   // Brillo
        g.fill(new Ellipse2D.Double(14, 9, 16, 9));
        g.dispose();
        return img;
    }

    // ===================================================================== ÁRBOLES Y DECORACIÓN

    /** Árbol frondoso visto desde arriba: varios círculos verdes con luz y sombra. */
    private static BufferedImage arbol() {
        int s = 148;
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = lienzo(img);
        Random r = new Random(3);
        double[][] hojas = {{74, 74, 54}, {48, 56, 34}, {100, 52, 32}, {50, 100, 32}, {102, 98, 34}, {74, 40, 30}, {74, 108, 30}};
        for (double[] h : hojas) {
            g.setPaint(new RadialGradientPaint(new Point2D.Double(h[0] - h[2] * 0.35, h[1] - h[2] * 0.35), (float) (h[2] * 1.4),
                    new float[]{0f, 0.55f, 1f},
                    new Color[]{new Color(134, 239, 172), new Color(34, 160, 70), new Color(16, 90, 40)}));
            g.fill(new Ellipse2D.Double(h[0] - h[2], h[1] - h[2], h[2] * 2, h[2] * 2));
        }
        for (int i = 0; i < 40; i++) {                // Destellos de luz en las hojas
            double x = 30 + r.nextDouble() * 80, y = 25 + r.nextDouble() * 80;
            g.setColor(new Color(187, 247, 208, 90));
            g.fill(new Ellipse2D.Double(x, y, 4 + r.nextInt(5), 3 + r.nextInt(4)));
        }
        g.dispose();
        return img;
    }

    /** Pino visto desde arriba: estrellas superpuestas en distintos tonos de verde. */
    private static BufferedImage pino() {
        int s = 128;
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = lienzo(img);
        Color[] tonos = {new Color(6, 78, 59), new Color(4, 120, 87), new Color(16, 160, 110), new Color(110, 231, 183)};
        double[] radios = {62, 46, 30, 12};
        for (int capa = 0; capa < 4; capa++) {
            Path2D estrella = new Path2D.Double();
            int puntas = 9;
            for (int i = 0; i < puntas * 2; i++) {
                double ang = i * Math.PI / puntas + capa * 0.3;
                double rad = i % 2 == 0 ? radios[capa] : radios[capa] * 0.68;
                double x = s / 2.0 + Math.cos(ang) * rad, y = s / 2.0 + Math.sin(ang) * rad;
                if (i == 0) estrella.moveTo(x, y); else estrella.lineTo(x, y);
            }
            estrella.closePath();
            g.setColor(tonos[capa]);
            g.fill(estrella);
        }
        g.dispose();
        return img;
    }

    /** Arbusto con florcitas rosadas. */
    private static BufferedImage arbusto() {
        int s = 84;
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = lienzo(img);
        double[][] partes = {{30, 44, 24}, {54, 40, 24}, {42, 28, 20}, {44, 56, 20}};
        for (double[] p : partes) {
            g.setPaint(new RadialGradientPaint(new Point2D.Double(p[0] - 6, p[1] - 6), (float) (p[2] * 1.3),
                    new float[]{0f, 1f}, new Color[]{new Color(132, 204, 22), new Color(54, 110, 20)}));
            g.fill(new Ellipse2D.Double(p[0] - p[2], p[1] - p[2], p[2] * 2, p[2] * 2));
        }
        Random r = new Random(5);
        for (int i = 0; i < 9; i++) {
            double x = 20 + r.nextDouble() * 44, y = 18 + r.nextDouble() * 46;
            g.setColor(new Color(244, 114, 182));
            g.fill(new Ellipse2D.Double(x, y, 7, 7));
            g.setColor(new Color(254, 240, 138));
            g.fill(new Ellipse2D.Double(x + 2.2, y + 2.2, 2.6, 2.6));
        }
        g.dispose();
        return img;
    }

    /** Roca gris con caras iluminadas y en sombra. */
    private static BufferedImage roca() {
        int s = 68;
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = lienzo(img);
        Polygon contorno = new Polygon(new int[]{14, 34, 56, 64, 52, 24, 6}, new int[]{14, 4, 12, 36, 60, 64, 40}, 7);
        g.setPaint(new GradientPaint(10, 8, new Color(214, 211, 209), 60, 60, new Color(87, 83, 78)));
        g.fill(contorno);
        g.setColor(new Color(255, 255, 255, 60));
        g.fill(new Polygon(new int[]{14, 34, 36, 18}, new int[]{14, 4, 24, 30}, 4));
        g.setColor(new Color(41, 37, 36, 120));
        g.setStroke(new BasicStroke(2f));
        g.draw(contorno);
        g.dispose();
        return img;
    }
}
