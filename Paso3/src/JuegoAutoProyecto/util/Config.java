package JuegoAutoProyecto.util;

/**
 * Constantes de configuración del juego.
 *
 * <p>Tener todos los "números mágicos" en un solo lugar hace que el juego sea fácil
 * de ajustar: si quieres una ventana más grande, más carriles o que las monedas
 * valgan más, solo cambias el valor aquí.</p>
 */
public final class Config {

    private Config() { } // Clase de utilidades: no se crean objetos de ella

    // ---------------------------------------------------------------- Ventana
    /** Texto que aparece en la barra de título de la ventana. */
    public static final String TITULO = "Carrera de Autos - Programación 2";
    /** Ancho del área de juego en píxeles. */
    public static final int ANCHO = 520;
    /** Alto del área de juego en píxeles. */
    public static final int ALTO = 680;
    /** Milisegundos entre cada fotograma (16 ms ≈ 60 fotogramas por segundo). */
    public static final int MS_POR_FRAME = 16;

    // ---------------------------------------------------------------- Pista
    /** Coordenada X donde empieza el asfalto (a la izquierda hay pasto). */
    public static final int PISTA_X = 110;
    /** Ancho total del asfalto. */
    public static final int PISTA_ANCHO = 300;
    /** Cantidad de carriles de la pista. */
    public static final int NUM_CARRILES = 4;
    /** Ancho de cada carril (300 / 4 = 75 px). */
    public static final int CARRIL_ANCHO = PISTA_ANCHO / NUM_CARRILES;

    // ---------------------------------------------------------------- Reglas
    /** Puntos que se ganan al esquivar (pasar) un auto rival. */
    public static final int PUNTOS_POR_RIVAL = 10;
    /** Puntos que se ganan al recoger una moneda. */
    public static final int PUNTOS_POR_MONEDA = 25;
    /** Cada cuántos puntos se sube de nivel (y aumenta la velocidad). */
    public static final int PUNTOS_POR_NIVEL = 100;
    /** Velocidad de la pista al empezar una partida (píxeles por fotograma). */
    public static final double VELOCIDAD_INICIAL = 6.0;
    /** Cuánto aumenta la velocidad en cada nivel. */
    public static final double AUMENTO_POR_NIVEL = 0.7;
    /** Velocidad máxima que puede alcanzar la pista sin turbo. */
    public static final double VELOCIDAD_MAXIMA = 16.0;
    /** Velocidad extra que da el turbo. */
    public static final double EXTRA_TURBO = 5.0;
    /** Factor para mostrar la velocidad en km/h (solo es visual). */
    public static final double FACTOR_KMH = 18.0;
    /** Largo máximo del nombre del jugador. */
    public static final int MAX_LETRAS_NOMBRE = 14;

    /**
     * Devuelve la coordenada X del centro de un carril.
     *
     * @param carril número de carril, de 0 (izquierda) a {@link #NUM_CARRILES} - 1 (derecha)
     * @return X del centro del carril
     */
    public static int centroCarril(int carril) {
        return PISTA_X + CARRIL_ANCHO * carril + CARRIL_ANCHO / 2;
    }
}
