package Proyecto1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class CarreraAutosJava extends JFrame {

    public CarreraAutosJava() {
        setTitle("Programación 2 - Carrera de Autos (Java)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        add(new PanelCarrera());
        pack();
        setLocationRelativeTo(null);
    }

    // --- Clase base POO para objetos en pantalla ---
    private static abstract class EntidadJuego {
        protected int x, y, ancho, alto, velocidad;

        public EntidadJuego(int x, int y, int ancho, int alto, int velocidad) {
            this.x = x;
            this.y = y;
            this.ancho = ancho;
            this.alto = alto;
            this.velocidad = velocidad;
        }

        public abstract void actualizar();
        public abstract void dibujar(Graphics2D g2);

        public Rectangle getHitbox() {
            return new Rectangle(x + 4, y + 4, ancho - 8, alto - 8);
        }
    }

    // --- Auto del Jugador ---
    private static class AutoJugador extends EntidadJuego {
        private final Image sprite;
        private boolean movIzq, movDer;

        public AutoJugador(int x, int y, String rutaImagen) {
            super(x, y, 48, 84, 7);
            Image img = new ImageIcon(rutaImagen).getImage();
            this.sprite = (img.getWidth(null) > 0) ? img : null;
        }

        @Override
        public void actualizar() {
            if (movIzq) x -= velocidad;
            if (movDer) x += velocidad;
            // Limitar dentro de los bordes de la carretera (x entre 95 y 405)
            x = Math.max(95, Math.min(405 - ancho, x));
        }

        @Override
        public void dibujar(Graphics2D g2) {
            if (sprite != null) {
                g2.drawImage(sprite, x, y, ancho, alto, null);
            } else {
                // Chasis
                g2.setColor(new Color(220, 38, 38));
                g2.fillRoundRect(x, y, ancho, alto, 14, 14);
                // Parabrisas
                g2.setColor(new Color(186, 230, 253));
                g2.fillRoundRect(x + 7, y + 18, ancho - 14, 20, 6, 6);
                // Franja deportiva
                g2.setColor(Color.WHITE);
                g2.fillRect(x + ancho / 2 - 3, y + 2, 6, alto - 4);
                // Llantas
                g2.setColor(Color.BLACK);
                g2.fillRect(x - 4, y + 12, 4, 18);
                g2.fillRect(x + ancho, y + 12, 4, 18);
                g2.fillRect(x - 4, y + alto - 30, 4, 18);
                g2.fillRect(x + ancho, y + alto - 30, 4, 18);
            }
        }
    }

    // --- Autos Rivales (Obstáculos) ---
    private static class AutoRival extends EntidadJuego {
        private final Color color;

        public AutoRival(int x, int y, int velocidad, Color color) {
            super(x, y, 46, 80, velocidad);
            this.color = color;
        }

        @Override
        public void actualizar() {
            y += velocidad;
        }

        @Override
        public void dibujar(Graphics2D g2) {
            g2.setColor(color);
            g2.fillRoundRect(x, y, ancho, alto, 12, 12);
            g2.setColor(new Color(30, 41, 59));
            g2.fillRoundRect(x + 6, y + 42, ancho - 12, 18, 6, 6);
            g2.setColor(Color.YELLOW);
            g2.fillRect(x + 6, y + alto - 5, 10, 4);
            g2.fillRect(x + ancho - 16, y + alto - 5, 10, 4);
        }
    }

    // --- Moneda / Bonus ---
    private static class MonedaBonus extends EntidadJuego {
        public MonedaBonus(int x, int y, int velocidad) {
            super(x, y, 26, 26, velocidad);
        }

        @Override
        public void actualizar() {
            y += velocidad;
        }

        @Override
        public void dibujar(Graphics2D g2) {
            g2.setColor(new Color(250, 204, 21));
            g2.fillOval(x, y, ancho, alto);
            g2.setColor(new Color(161, 98, 7));
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(x, y, ancho, alto);
            g2.drawString("$", x + 9, y + 18);
        }
    }

    // --- Panel Principal del Juego ---
    private static class PanelCarrera extends JPanel implements ActionListener, KeyListener {
        private static final int ANCHO = 500;
        private static final int ALTO = 650;
        private static final int[] CARRILES = {115, 190, 265, 340};

        private final Timer timer;
        private final Random random = new Random();
        private final Image fondoImagen;

        private AutoJugador jugador;
        private final List<AutoRival> rivales = new ArrayList<>();
        private final List<MonedaBonus> monedas = new ArrayList<>();

        private int puntaje = 0;
        private int velocidadBase = 5;
        private int offsetLinea = 0;
        private int contadorSpawn = 0;
        private boolean turboActivo = false;
        private boolean gameOver = false;

        public PanelCarrera() {
            setPreferredSize(new Dimension(ANCHO, ALTO));
            setFocusable(true);
            addKeyListener(this);

            Image img = new ImageIcon("fondo_pista.jpg").getImage();
            this.fondoImagen = (img.getWidth(null) > 0) ? img : null;

            iniciarPartida();
            timer = new Timer(16, this); // ~60 FPS
            timer.start();
        }

        private void iniciarPartida() {
            jugador = new AutoJugador(ANCHO / 2 - 24, ALTO - 120, "auto_jugador.png");
            rivales.clear();
            monedas.clear();
            puntaje = 0;
            velocidadBase = 5;
            turboActivo = false;
            gameOver = false;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (gameOver) return;

            int velocidadActual = turboActivo ? velocidadBase + 4 : velocidadBase;
            offsetLinea = (offsetLinea + velocidadActual) % 40;

            jugador.actualizar();

            // Generar rivales y monedas periódicamente
            contadorSpawn++;
            if (contadorSpawn % 45 == 0) {
                int carril = CARRILES[random.nextInt(CARRILES.length)];
                Color[] colores = {
                        new Color(59, 130, 246),
                        new Color(16, 185, 129),
                        new Color(168, 85, 247),
                        new Color(249, 115, 22)
                };
                rivales.add(new AutoRival(carril, -90, velocidadActual, colores[random.nextInt(colores.length)]));

                if (random.nextBoolean()) {
                    int carrilMoneda = CARRILES[random.nextInt(CARRILES.length)] + 10;
                    if (carrilMoneda != carril + 10) {
                        monedas.add(new MonedaBonus(carrilMoneda, -40, velocidadActual));
                    }
                }
            }

            // Actualizar rivales y verificar choques
            Iterator<AutoRival> itRivales = rivales.iterator();
            while (itRivales.hasNext()) {
                AutoRival r = itRivales.next();
                r.velocidad = velocidadActual;
                r.actualizar();

                if (r.getHitbox().intersects(jugador.getHitbox())) {
                    gameOver = true;
                } else if (r.y > ALTO) {
                    itRivales.remove();
                    puntaje += 10;
                    if (puntaje % 100 == 0) velocidadBase++;
                }
            }

            // Actualizar monedas
            Iterator<MonedaBonus> itMonedas = monedas.iterator();
            while (itMonedas.hasNext()) {
                MonedaBonus m = itMonedas.next();
                m.velocidad = velocidadActual;
                m.actualizar();

                if (m.getHitbox().intersects(jugador.getHitbox())) {
                    puntaje += 25;
                    itMonedas.remove();
                } else if (m.y > ALTO) {
                    itMonedas.remove();
                }
            }

            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Fondo o carretera dibujada
            if (fondoImagen != null) {
                g2.drawImage(fondoImagen, 0, 0, ANCHO, ALTO, null);
            } else {
                // Césped lateral
                g2.setColor(new Color(22, 101, 52));
                g2.fillRect(0, 0, ANCHO, ALTO);

                // Asfalto
                g2.setColor(new Color(51, 65, 85));
                g2.fillRect(90, 0, 320, ALTO);

                // Bordes blanco/rojo
                g2.setColor(Color.WHITE);
                g2.fillRect(84, 0, 6, ALTO);
                g2.fillRect(410, 0, 6, ALTO);

                // Líneas de carriles animadas
                g2.setColor(new Color(226, 232, 240));
                for (int y = -40 + offsetLinea; y < ALTO; y += 40) {
                    g2.fillRect(170, y, 4, 22);
                    g2.fillRect(250, y, 4, 22);
                    g2.fillRect(330, y, 4, 22);
                }
            }

            // Dibujar entidades
            for (MonedaBonus m : monedas) m.dibujar(g2);
            for (AutoRival r : rivales) r.dibujar(g2);
            jugador.dibujar(g2);

            // HUD superior
            g2.setColor(new Color(15, 23, 42, 200));
            g2.fillRoundRect(12, 12, 210, 62, 10, 10);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2.drawString("Puntaje: " + puntaje, 24, 34);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.drawString("Velocidad: " + (turboActivo ? "TURBO!" : velocidadBase + "x"), 24, 56);

            // Pantalla de Game Over
            if (gameOver) {
                g2.setColor(new Color(0, 0, 0, 175));
                g2.fillRect(0, 0, ANCHO, ALTO);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 34));
                g2.drawString("¡CHOQUE!", ANCHO / 2 - 85, ALTO / 2 - 30);

                g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
                g2.drawString("Puntaje final: " + puntaje, ANCHO / 2 - 75, ALTO / 2 + 10);
                g2.drawString("Presiona R para reiniciar", ANCHO / 2 - 102, ALTO / 2 + 45);
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int code = e.getKeyCode();
            if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) jugador.movIzq = true;
            if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) jugador.movDer = true;
            if (code == KeyEvent.VK_SPACE) turboActivo = true;
            if (code == KeyEvent.VK_R && gameOver) iniciarPartida();
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int code = e.getKeyCode();
            if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) jugador.movIzq = false;
            if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) jugador.movDer = false;
            if (code == KeyEvent.VK_SPACE) turboActivo = false;
        }

        @Override
        public void keyTyped(KeyEvent e) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CarreraAutosJava().setVisible(true));
    }
}
