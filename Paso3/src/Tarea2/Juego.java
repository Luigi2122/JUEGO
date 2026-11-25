package Tarea2;



import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class Juego extends JPanel implements ActionListener, KeyListener {

    // Canasta
    int canastaX = 350;
    int canastaY = 490;
    int canastaAncho = 100;
    int canastaAlto = 50;

    // Marcador
    int puntos = 0;

    // Frutas
    ArrayList<Rectangle> frutas = new ArrayList<>();

    Random random = new Random();

    // Temporizador
    Timer timer = new Timer(20, this);

    // Movimiento
    boolean izquierda = false;
    boolean derecha = false;

    public Juego() {
        setBackground(new Color(135, 206, 235));

        addKeyListener(this);
        setFocusable(true);

        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Fondo
        g.setColor(new Color(135, 206, 235));
        g.fillRect(0, 0, 800, 600);

        // Suelo
        g.setColor(new Color(60, 180, 75));
        g.fillRect(0, 550, 800, 50);

        // Canasta
        g.setColor(new Color(139, 69, 19));
        g.fillRoundRect(canastaX, canastaY,
                canastaAncho, canastaAlto, 15, 15);

        g.setColor(Color.BLACK);
        g.drawString("CANASTA", canastaX + 20, canastaY + 30);

        // Frutas
        for (Rectangle fruta : frutas) {
            g.setColor(Color.RED);
            g.fillOval(fruta.x, fruta.y, fruta.width, fruta.height);
        }

        // Marcador
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Puntos: " + puntos, 650, 35);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // Movimiento de la canasta
        if (izquierda && canastaX > 0) {
            canastaX -= 7;
        }

        if (derecha && canastaX < 700) {
            canastaX += 7;
        }

        // Crear frutas aleatoriamente
        if (random.nextInt(30) == 0) {
            int x = random.nextInt(760);
            frutas.add(new Rectangle(x, 0, 30, 30));
        }

        // Mover frutas
        for (int i = frutas.size() - 1; i >= 0; i--) {

            Rectangle fruta = frutas.get(i);

            fruta.y += 5;

            // Detectar si la canasta atrapa la fruta
            Rectangle canasta = new Rectangle(
                    canastaX, canastaY,
                    canastaAncho, canastaAlto
            );

            if (canasta.intersects(fruta)) {
                puntos++;
                frutas.remove(i);
            }

            // Eliminar frutas que llegan al suelo
            else if (fruta.y > 550) {
                frutas.remove(i);
            }
        }

        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            izquierda = true;
        }

        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            derecha = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            izquierda = false;
        }

        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            derecha = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}

