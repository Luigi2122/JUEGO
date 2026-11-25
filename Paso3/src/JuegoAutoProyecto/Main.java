package JuegoAutoProyecto;

import javax.swing.SwingUtilities; // Importa la utilidad para manejar la interfaz gráfica de forma segura

public class Main { // Declara la clase principal del programa
    public static void main(String[] args) { // Punto de entrada donde empieza a ejecutarse Java
        SwingUtilities.invokeLater(() -> { // Ejecuta la creación de la ventana en el hilo de eventos de Swing (EDT)
            VentanaJuego ventana = new VentanaJuego(); // Crea la ventana principal del juego
            ventana.setVisible(true); // Hace visible la ventana en la pantalla
        }); // Cierra la expresión lambda
    } // Cierra el método main
} // Cierra la clase Main
