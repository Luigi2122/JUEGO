package JuegoAutoProyecto;

import java.awt.BasicStroke; // Grosor del contorno de la moneda
import java.awt.Color; // Colores de la moneda
import java.awt.Graphics2D; // Contexto gráfico 2D

public class MonedaBonus extends EntidadJuego { // MonedaBonus también hereda de EntidadJuego
    public MonedaBonus(int x, int y, int velocidad) { // Constructor de la moneda
        super(x, y, 26, 26, velocidad); // Tamaño circular de 26x26 píxeles
    } // Cierra el constructor

    @Override // Sobrescribe actualizar
    public void actualizar() { // Mueve la moneda hacia abajo junto con la pista
        y += velocidad; // Avanza en el eje Y según la velocidad actual
    } // Cierra actualizar

    @Override // Sobrescribe dibujar
    public void dibujar(Graphics2D g2) { // Dibuja la moneda en pantalla
        g2.setColor(new Color(250, 204, 21)); // Color dorado
        g2.fillOval(x, y, ancho, alto); // Rellena el círculo de la moneda
        g2.setColor(new Color(161, 98, 7)); // Color marrón oscuro para el borde y símbolo
        g2.setStroke(new BasicStroke(2)); // Borde de 2 píxeles
        g2.drawOval(x, y, ancho, alto); // Dibuja el contorno circular
        g2.drawString("$", x + 9, y + 18); // Dibuja el signo '$' centrado en la moneda
    } // Cierra dibujar
} // Cierra MonedaBonus
