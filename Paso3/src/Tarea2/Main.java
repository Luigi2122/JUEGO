package Tarea2;


import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame ventana = new JFrame("Atrapa Frutas");

        Juego juego = new Juego();

        ventana.add(juego);
        ventana.setSize(800, 600);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setLocationRelativeTo(null);
        ventana.setResizable(false);
        ventana.setVisible(true);

        juego.requestFocusInWindow();
    }
}

