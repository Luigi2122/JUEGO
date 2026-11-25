package JuegoAutoProyecto;

import java.awt.BasicStroke; // Permite definir el grosor del borde al dibujar
import java.awt.Color; // Representa colores RGB
import java.awt.Graphics2D; // Contexto gráfico 2D
import java.awt.Image; // Representa una imagen cargada desde archivo
import javax.swing.ImageIcon; // Permite cargar imágenes desde ruta o recurso

public class AutoJugador extends EntidadJuego { // AutoJugador hereda atributos y métodos de EntidadJuego
    private final Image sprite; // Imagen opcional del auto del jugador
    private boolean movIzq; // Indica si la tecla izquierda/A está presionada
    private boolean movDer; // Indica si la tecla derecha/D está presionada

    public AutoJugador(int x, int y, String rutaImagen) { // Constructor del auto del jugador
        super(x, y, 48, 84, 7); // Llama al constructor padre: ancho 48, alto 84, velocidad 7
        Image img = new ImageIcon(rutaImagen).getImage(); // Intenta cargar la imagen desde rutaImagen
        this.sprite = (img.getWidth(null) > 0) ? img : null; // Guarda la imagen solo si existe y tiene ancho válido
    } // Cierra el constructor

    public void setMovIzq(boolean movIzq) { // Setter para activar/desactivar movimiento a la izquierda
        this.movIzq = movIzq; // Actualiza el estado del booleano
    } // Cierra setMovIzq

    public void setMovDer(boolean movDer) { // Setter para activar/desactivar movimiento a la derecha
        this.movDer = movDer; // Actualiza el estado del booleano
    } // Cierra setMovDer

    @Override // Indica que sobrescribe el método abstracto de EntidadJuego
    public void actualizar() { // Actualiza la posición horizontal del auto
        if (movIzq) x -= velocidad; // Mueve hacia la izquierda restando en X
        if (movDer) x += velocidad; // Mueve hacia la derecha sumando en X
        x = Math.max(95, Math.min(405 - ancho, x)); // Limita a X para que el auto no salga de la carretera
    } // Cierra actualizar

    @Override // Sobrescribe el método dibujar de EntidadJuego
    public void dibujar(Graphics2D g2) { // Dibuja el auto en pantalla
        if (sprite != null) { // Si se cargó una imagen válida...
            g2.drawImage(sprite, x, y, ancho, alto, null); // ...dibuja la imagen escalada al tamaño del auto
        } else { // Si no hay imagen, dibuja un auto con formas geométricas
            g2.setColor(new Color(220, 38, 38)); // Color rojo para la carrocería
            g2.fillRoundRect(x, y, ancho, alto, 14, 14); // Dibuja el chasis redondeado
            g2.setColor(new Color(186, 230, 253)); // Color celeste claro para el parabrisas
            g2.fillRoundRect(x + 7, y + 18, ancho - 14, 20, 6, 6); // Dibuja el parabrisas
            g2.setColor(Color.WHITE); // Color blanco para la franja central
            g2.fillRect(x + ancho / 2 - 3, y + 2, 6, alto - 4); // Dibuja la franja deportiva
            g2.setColor(Color.BLACK); // Color negro para las ruedas
            g2.fillRect(x - 4, y + 12, 4, 18); // Rueda delantera izquierda
            g2.fillRect(x + ancho, y + 12, 4, 18); // Rueda delantera derecha
            g2.fillRect(x - 4, y + alto - 30, 4, 18); // Rueda trasera izquierda
            g2.fillRect(x + ancho, y + alto - 30, 4, 18); // Rueda trasera derecha
            g2.setStroke(new BasicStroke(1)); // Restaura el grosor estándar de línea
        } // Cierra el else
    } // Cierra dibujar
} // Cierra AutoJugador
