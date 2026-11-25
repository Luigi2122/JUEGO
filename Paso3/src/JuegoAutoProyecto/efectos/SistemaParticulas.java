package JuegoAutoProyecto.efectos;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Administra todas las partículas del juego y ofrece "recetas" listas para usar:
 * humo del escape, llamas del turbo, chispas de las monedas y la explosión del choque.
 */
public class SistemaParticulas {

    /** Límite de partículas vivas para que el juego no se ponga lento. */
    private static final int MAXIMO = 700;

    private final List<Particula> particulas = new ArrayList<>();
    private final Random azar = new Random();

    /** Humo gris que sale del escape y se queda atrás. */
    public void humo(double x, double y) {
        agregar(new Particula(x + rango(3), y, rango(0.4), 1.5 + azar.nextDouble(),
                5 + azar.nextDouble() * 3, 0.35, 0.97, 0, 32 + azar.nextInt(14),
                new Color(203, 213, 225, 110), new Color(100, 116, 139, 0)));
    }

    /** Llamas anaranjadas del turbo. */
    public void llama(double x, double y) {
        agregar(new Particula(x + rango(4), y, rango(0.6), 3 + azar.nextDouble() * 2,
                9 + azar.nextDouble() * 4, -0.45, 0.95, 0, 14 + azar.nextInt(8),
                new Color(255, 237, 160, 235), new Color(239, 68, 68, 0)));
    }

    /** Chispas que salen disparadas en todas direcciones (por ejemplo al recoger una moneda). */
    public void chispas(double x, double y, Color color, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            double angulo = azar.nextDouble() * Math.PI * 2;
            double rapidez = 1.5 + azar.nextDouble() * 4.5;
            agregar(new Particula(x, y, Math.cos(angulo) * rapidez, Math.sin(angulo) * rapidez,
                    3 + azar.nextDouble() * 4, -0.08, 0.92, 0.05, 25 + azar.nextInt(20),
                    color, new Color(255, 255, 255, 0)));
        }
    }

    /** Gran explosión de fuego y humo para cuando el jugador choca. */
    public void explosion(double x, double y) {
        for (int i = 0; i < 70; i++) {          // Bola de fuego
            double angulo = azar.nextDouble() * Math.PI * 2;
            double rapidez = 1 + azar.nextDouble() * 6;
            agregar(new Particula(x, y, Math.cos(angulo) * rapidez, Math.sin(angulo) * rapidez,
                    10 + azar.nextDouble() * 14, -0.25, 0.93, 0, 30 + azar.nextInt(25),
                    new Color(255, 230, 120, 255), new Color(220, 38, 38, 0)));
        }
        for (int i = 0; i < 45; i++) {          // Humo oscuro que queda después
            double angulo = azar.nextDouble() * Math.PI * 2;
            double rapidez = 0.5 + azar.nextDouble() * 2.5;
            agregar(new Particula(x + rango(10), y + rango(10), Math.cos(angulo) * rapidez,
                    Math.sin(angulo) * rapidez - 0.6, 14 + azar.nextDouble() * 12, 0.45, 0.96, -0.01,
                    70 + azar.nextInt(50), new Color(71, 85, 105, 150), new Color(15, 23, 42, 0)));
        }
        chispas(x, y, new Color(253, 224, 71, 255), 40);   // Chispas brillantes
    }

    /** Mueve todas las partículas y borra las que ya murieron. */
    public void actualizar() {
        Iterator<Particula> it = particulas.iterator();
        while (it.hasNext()) {
            Particula p = it.next();
            p.actualizar();
            if (p.estaMuerta()) it.remove();
        }
    }

    /** Dibuja todas las partículas vivas. */
    public void dibujar(Graphics2D g2) {
        for (Particula p : particulas) p.dibujar(g2);
    }

    /** Borra todas las partículas (al empezar una partida nueva). */
    public void limpiar() {
        particulas.clear();
    }

    private void agregar(Particula p) {
        if (particulas.size() < MAXIMO) particulas.add(p);
    }

    /** Número al azar entre -max y +max. */
    private double rango(double max) {
        return (azar.nextDouble() * 2 - 1) * max;
    }
}
