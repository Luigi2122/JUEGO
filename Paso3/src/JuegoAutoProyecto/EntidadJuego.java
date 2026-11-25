package JuegoAutoProyecto;

import java.awt.Graphics2D; // Permite dibujar formas y gráficos avanzados en 2D
import java.awt.Rectangle; // Representa un rectángulo usado para detectar colisiones

public abstract class EntidadJuego { // Clase abstracta: no se instancia sola, sirve de molde para otras clases
    protected int x, y; // Coordenadas superiores izquierdas del objeto en el panel
    protected int ancho, alto; // Tamaño del objeto en píxeles
    protected int velocidad; // Cuántos píxeles se mueve por actualización

    public EntidadJuego(int x, int y, int ancho, int alto, int velocidad) { // Constructor común a todas las entidades
        this.x = x; // Asigna la posición horizontal inicial
        this.y = y; // Asigna la posición vertical inicial
        this.ancho = ancho; // Asigna el ancho del objeto
        this.alto = alto; // Asigna el alto del objeto
        this.velocidad = velocidad; // Asigna la velocidad inicial
    } // Cierra el constructor

    public abstract void actualizar(); // Método abstracto: cada subclase define cómo se mueve o actualiza

    public abstract void dibujar(Graphics2D g2); // Método abstracto: cada subclase define cómo se dibuja

    public Rectangle getHitbox() { // Devuelve el área de colisión del objeto (ligeramente reducida para ser más justa)
        return new Rectangle(x + 4, y + 4, ancho - 8, alto - 8); // Crea y retorna el rectángulo de colisión
    } // Cierra getHitbox
} // Cierra EntidadJuego
