package JuegoAutoProyecto;

import java.awt.Color; // Colores para diferenciar rivales
import java.awt.Graphics2D; // Contexto gráfico 2D

public class AutoRival extends EntidadJuego { // AutoRival hereda de EntidadJuego
    private final Color color; // Color individual de cada auto rival

    public AutoRival(int x, int y, int velocidad, Color color) { // Constructor del rival
        super(x, y, 46, 80, velocidad); // Ancho 46, alto 80 y velocidad recibida
        this.color = color; // Guarda el color del auto rival
    } // Cierra el constructor

    @Override // Sobrescribe actualizar
    public void actualizar() { // Hace que el rival avance hacia abajo en la pantalla
        y += velocidad; // Incrementa la coordenada Y (en Swing, Y crece hacia abajo)
    } // Cierra actualizar

    @Override // Sobrescribe dibujar
    public void dibujar(Graphics2D g2) { // Dibuja el auto rival
        g2.setColor(color); // Aplica el color asignado al rival
        g2.fillRoundRect(x, y, ancho, alto, 12, 12); // Dibuja el cuerpo del auto rival
        g2.setColor(new Color(30, 41, 59)); // Color oscuro para el cristal trasero/cabina
        g2.fillRoundRect(x + 6, y + 42, ancho - 12, 18, 6, 6); // Dibuja la ventana
        g2.setColor(Color.YELLOW); // Color amarillo para los faros/luces
        g2.fillRect(x + 6, y + alto - 5, 10, 4); // Luz izquierda
        g2.fillRect(x + ancho - 16, y + alto - 5, 10, 4); // Luz derecha
    } // Cierra dibujar
} // Cierra AutoRival
