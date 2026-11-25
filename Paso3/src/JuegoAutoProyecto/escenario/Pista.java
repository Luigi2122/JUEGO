package JuegoAutoProyecto.escenario;

import JuegoAutoProyecto.entidades.Decoracion;
import JuegoAutoProyecto.util.Config;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * El escenario: pasto, bordes rojo/blanco, asfalto, líneas de carril y árboles.
 *
 * <p><b>¿Cómo se logra la sensación de movimiento?</b> El auto del jugador casi no
 * se mueve en vertical; lo que se mueve es el escenario. Guardamos un
 * {@code desplazamiento} que aumenta con la velocidad y lo usamos para correr
 * hacia abajo las texturas, las líneas y los árboles.</p>
 *
 * <p><b>Optimización:</b> las texturas (ruido del asfalto, franjas del pasto y la
 * viñeta oscura de los bordes) se dibujan <i>una sola vez</i> en imágenes en memoria
 * ({@link BufferedImage}) y en cada fotograma solo se copian. Así el juego corre fluido.</p>
 */
public class Pista {

    private static final int ALTO_TEXTURA = 256;   // Las texturas se repiten cada 256 px
    private static final int LARGO_LINEA = 34;     // Largo de cada línea blanca de carril
    private static final int ESPACIO_LINEA = 70;   // Distancia entre el inicio de una línea y la siguiente
    private static final int ALTO_BORDILLO = 24;   // Alto de cada bloque rojo/blanco del borde

    private final BufferedImage texturaPasto;
    private final BufferedImage texturaAsfalto;
    private final BufferedImage vineta;
    private final List<Decoracion> decoraciones = new ArrayList<>();
    private final Random azar = new Random();

    private double desplazamiento;          // Cuántos píxeles ha avanzado la pista en total
    private double distanciaSiguienteArbol;  // Cuánto falta para poner el próximo árbol

    public Pista() {
        texturaPasto = crearTexturaPasto();
        texturaAsfalto = crearTexturaAsfalto();
        vineta = crearVineta();
        // Llenamos la pantalla con árboles desde el principio para que no empiece vacía
        for (int y = -80; y < Config.ALTO; y += 55 + azar.nextInt(40)) {
            agregarDecoracion(y);
        }
    }

    /**
     * Avanza el escenario.
     *
     * @param velocidad píxeles que avanza en este fotograma
     */
    public void avanzar(double velocidad) {
        desplazamiento += velocidad;

        Iterator<Decoracion> it = decoraciones.iterator();
        while (it.hasNext()) {
            Decoracion d = it.next();
            d.actualizar(velocidad);
            if (d.salioDePantalla()) it.remove();
        }
        distanciaSiguienteArbol -= velocidad;
        if (distanciaSiguienteArbol <= 0) {
            agregarDecoracion(-90);
            distanciaSiguienteArbol = 45 + azar.nextInt(60);
        }
    }

    /** Dibuja todo el escenario (va antes que los autos). */
    public void dibujar(Graphics2D g2) {
        int ancho = Config.ANCHO, alto = Config.ALTO;
        int pistaX = Config.PISTA_X, pistaAncho = Config.PISTA_ANCHO;

        // 1) Pasto: la textura se repite verticalmente y se desplaza hacia abajo
        int corrimiento = (int) (desplazamiento % ALTO_TEXTURA);
        for (int y = corrimiento - ALTO_TEXTURA; y < alto; y += ALTO_TEXTURA) {
            g2.drawImage(texturaPasto, 0, y, null);
        }

        // 2) Bordillos rojo y blanco a ambos lados del asfalto
        int corrimientoBordillo = (int) (desplazamiento % (ALTO_BORDILLO * 2));
        for (int y = corrimientoBordillo - ALTO_BORDILLO * 2, i = 0; y < alto; y += ALTO_BORDILLO, i++) {
            g2.setColor(i % 2 == 0 ? new Color(220, 38, 38) : new Color(241, 245, 249));
            g2.fillRect(pistaX - 12, y, 12, ALTO_BORDILLO);
            g2.fillRect(pistaX + pistaAncho, y, 12, ALTO_BORDILLO);
        }
        g2.setColor(new Color(0, 0, 0, 50));   // Sombrita del bordillo sobre el pasto
        g2.fillRect(pistaX - 15, 0, 3, alto);
        g2.fillRect(pistaX + pistaAncho + 12, 0, 3, alto);

        // 3) Asfalto
        for (int y = corrimiento - ALTO_TEXTURA; y < alto; y += ALTO_TEXTURA) {
            g2.drawImage(texturaAsfalto, pistaX, y, null);
        }

        // 4) Líneas blancas continuas de los bordes
        g2.setColor(new Color(248, 250, 252, 230));
        g2.fillRect(pistaX + 6, 0, 4, alto);
        g2.fillRect(pistaX + pistaAncho - 10, 0, 4, alto);

        // 5) Líneas discontinuas entre carriles (se desplazan para dar movimiento)
        int corrimientoLinea = (int) (desplazamiento % ESPACIO_LINEA);
        for (int carril = 1; carril < Config.NUM_CARRILES; carril++) {
            int lx = pistaX + Config.CARRIL_ANCHO * carril - 2;
            for (int y = corrimientoLinea - ESPACIO_LINEA; y < alto; y += ESPACIO_LINEA) {
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRect(lx + 1, y + 2, 4, LARGO_LINEA);   // sombra
                g2.setColor(new Color(241, 245, 249, 220));
                g2.fillRect(lx, y, 4, LARGO_LINEA);
            }
        }

        // 6) Árboles y demás decoración
        for (Decoracion d : decoraciones) d.dibujar(g2);
    }

    /** Oscurece suavemente los bordes de la pantalla (efecto cinematográfico). Va al final. */
    public void dibujarVineta(Graphics2D g2) {
        g2.drawImage(vineta, 0, 0, null);
    }

    public double getDesplazamiento() { return desplazamiento; }

    // =================================================================== Creación de texturas

    /** Coloca un árbol/arbusto/roca al azar en el pasto izquierdo o derecho. */
    private void agregarDecoracion(double y) {
        Decoracion.Tipo[] tipos = Decoracion.Tipo.values();
        // Más árboles que rocas: elegimos con "pesos"
        int r = azar.nextInt(10);
        Decoracion.Tipo tipo = r < 4 ? tipos[0] : r < 7 ? tipos[1] : r < 9 ? tipos[2] : tipos[3];
        int tam = tipo.getTamano();
        boolean izquierda = azar.nextBoolean();
        double x;
        if (izquierda) {
            x = -tam * 0.3 + azar.nextInt(Math.max(1, Config.PISTA_X - 18 - (int) (tam * 0.7)));
        } else {
            int inicio = Config.PISTA_X + Config.PISTA_ANCHO + 18;
            x = inicio + azar.nextInt(Math.max(1, Config.ANCHO - inicio - (int) (tam * 0.7)));
        }
        decoraciones.add(new Decoracion(tipo, x, y));
    }

    /** Pasto con franjas (como un césped recién cortado) y algo de "ruido" para que no se vea plano. */
    private BufferedImage crearTexturaPasto() {
        BufferedImage img = new BufferedImage(Config.ANCHO, ALTO_TEXTURA, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        Color franjaA = new Color(34, 139, 58), franjaB = new Color(40, 155, 66);
        for (int y = 0; y < ALTO_TEXTURA; y += 64) {
            g.setColor((y / 64) % 2 == 0 ? franjaA : franjaB);
            g.fillRect(0, y, Config.ANCHO, 64);
        }
        Random r = new Random(7);
        for (int i = 0; i < 2600; i++) {   // Puntitos claros y oscuros: briznas de pasto
            int px = r.nextInt(Config.ANCHO), py = r.nextInt(ALTO_TEXTURA);
            g.setColor(r.nextBoolean() ? new Color(74, 190, 92, 120) : new Color(20, 100, 40, 120));
            g.fillRect(px, py, 2, 2 + r.nextInt(3));
        }
        g.dispose();
        return img;
    }

    /** Asfalto gris oscuro con granitos y bordes un poco más oscuros. */
    private BufferedImage crearTexturaAsfalto() {
        int w = Config.PISTA_ANCHO;
        BufferedImage img = new BufferedImage(w, ALTO_TEXTURA, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(55, 62, 76));
        g.fillRect(0, 0, w, ALTO_TEXTURA);
        Random r = new Random(11);
        for (int i = 0; i < 5000; i++) {   // Granitos del asfalto
            int gris = 40 + r.nextInt(50);
            g.setColor(new Color(gris, gris + 4, gris + 12, 110));
            g.fillRect(r.nextInt(w), r.nextInt(ALTO_TEXTURA), 1 + r.nextInt(2), 1 + r.nextInt(2));
        }
        // Marcas de llantas: franjas un poco más oscuras en el centro de cada carril
        for (int c = 0; c < Config.NUM_CARRILES; c++) {
            int centro = Config.CARRIL_ANCHO * c + Config.CARRIL_ANCHO / 2;
            g.setColor(new Color(0, 0, 0, 22));
            g.fillRect(centro - 18, 0, 8, ALTO_TEXTURA);
            g.fillRect(centro + 10, 0, 8, ALTO_TEXTURA);
        }
        // Bordes más oscuros (sombra hacia los costados)
        g.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 90), 26, 0, new Color(0, 0, 0, 0)));
        g.fillRect(0, 0, 26, ALTO_TEXTURA);
        g.setPaint(new GradientPaint(w - 26, 0, new Color(0, 0, 0, 0), w, 0, new Color(0, 0, 0, 90)));
        g.fillRect(w - 26, 0, 26, ALTO_TEXTURA);
        g.dispose();
        return img;
    }

    /** Degradado radial: transparente en el centro y oscuro en las esquinas. */
    private BufferedImage crearVineta() {
        BufferedImage img = new BufferedImage(Config.ANCHO, Config.ALTO, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        float radio = (float) Math.hypot(Config.ANCHO, Config.ALTO) * 0.62f;
        g.setPaint(new RadialGradientPaint(new Point2D.Double(Config.ANCHO / 2.0, Config.ALTO / 2.0),
                radio, new float[]{0.55f, 1f}, new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 10, 150)}));
        g.fillRect(0, 0, Config.ANCHO, Config.ALTO);
        g.dispose();
        return img;
    }
}
