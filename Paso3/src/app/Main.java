package app;

import javax.swing.SwingUtilities;
import modelo.Estudiante;
import modelo.Registro;
import vista.VentanaRegistro;

public class Main {
    public static void main(String[] args) {
        Registro r = new Registro();
        r.agregar(new Estudiante("250101", "Ana Rojas"));
        r.agregar(new Estudiante("250102", "Bruno Paz"));

        SwingUtilities.invokeLater(() -> new VentanaRegistro(r).setVisible(true));
    }
}
