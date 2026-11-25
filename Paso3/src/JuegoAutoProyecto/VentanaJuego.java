package JuegoAutoProyecto;

import javax.swing.JFrame; // Importa JFrame para crear una ventana de escritorio

public class VentanaJuego extends JFrame { // VentanaJuego hereda de JFrame
    public VentanaJuego() { // Constructor de la ventana
        setTitle("Programación 2 - Carrera de Autos"); // Título en la barra superior de la ventana
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Cierra el programa al pulsar la 'X' de la ventana
        setResizable(false); // Evita que el usuario cambie el tamaño y deforme la pista
        add(new PanelCarrera()); // Agrega el panel donde ocurre el juego dentro de la ventana
        pack(); // Ajusta el tamaño de la ventana al tamaño preferido de PanelCarrera
        setLocationRelativeTo(null); // Centra la ventana en el monitor
    } // Cierra el constructor
} // Cierra VentanaJuego
