package JuegoAutoProyecto.util;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Carga las imágenes (fotos/sprites) y las fuentes de la carpeta {@code recursos/}.
 *
 * <p><b>¿Cómo encuentra los archivos?</b> Primero los busca dentro del
 * <i>classpath</i> (así funciona al ejecutar desde IntelliJ, porque IntelliJ copia
 * los archivos de {@code src/} a la carpeta de compilación). Si no los encuentra,
 * prueba con rutas normales de carpeta como respaldo.</p>
 *
 * <p>Cada archivo se carga <b>una sola vez</b> y se guarda en un mapa (caché),
 * así el juego no lee el disco en cada fotograma.</p>
 *
 * <p>Si un archivo no existe, el método devuelve {@code null} y el juego dibuja una
 * figura de respaldo en su lugar: el juego nunca se cae por una imagen faltante.</p>
 */
public final class Recursos {

    /** Carpeta de recursos dentro del classpath. */
    private static final String CARPETA = "/JuegoAutoProyecto/recursos/";

    /** Rutas de respaldo, relativas a la carpeta desde donde se ejecuta el programa. */
    private static final String[] CARPETAS_RESPALDO = {
            "src/JuegoAutoProyecto/recursos/",
            "Paso3/src/JuegoAutoProyecto/recursos/"
    };

    private static final Map<String, BufferedImage> IMAGENES = new HashMap<>();
    private static final Map<String, Font> FUENTES_BASE = new HashMap<>();
    private static final Map<String, Font> FUENTES_DERIVADAS = new HashMap<>();

    private Recursos() { }

    /**
     * Devuelve una imagen de la carpeta de recursos.
     *
     * @param nombreArchivo por ejemplo {@code "auto_jugador.png"}
     * @return la imagen, o {@code null} si el archivo no existe
     */
    public static BufferedImage imagen(String nombreArchivo) {
        if (IMAGENES.containsKey(nombreArchivo)) {          // ¿Ya la cargamos antes?
            return IMAGENES.get(nombreArchivo);              // Sí: la devolvemos de la caché
        }
        BufferedImage img = null;
        try (InputStream entrada = abrir(nombreArchivo)) {
            if (entrada != null) img = ImageIO.read(entrada);
        } catch (IOException e) {
            System.err.println("[Recursos] Error leyendo " + nombreArchivo + ": " + e.getMessage());
        }
        if (img == null) {
            System.err.println("[Recursos] No se encontró la imagen '" + nombreArchivo
                    + "'. Se dibujará una figura de respaldo.");
        }
        IMAGENES.put(nombreArchivo, img);                     // Guardamos aunque sea null para no reintentar
        return img;
    }

    /**
     * Devuelve una fuente (tipografía) de la carpeta de recursos con el tamaño pedido.
     * Si el archivo no existe, devuelve una fuente estándar de Java.
     *
     * @param nombreArchivo por ejemplo {@code "Anton-Regular.ttf"}
     * @param tamano        tamaño en puntos
     * @return la fuente lista para usar con {@code g2.setFont(...)}
     */
    public static Font fuente(String nombreArchivo, float tamano) {
        String clave = nombreArchivo + "@" + tamano;
        Font derivada = FUENTES_DERIVADAS.get(clave);
        if (derivada != null) return derivada;

        if (!FUENTES_BASE.containsKey(nombreArchivo)) {       // Primera vez que se pide este archivo
            Font base = null;
            try (InputStream entrada = abrir(nombreArchivo)) {
                if (entrada != null) base = Font.createFont(Font.TRUETYPE_FONT, entrada);
            } catch (Exception e) {
                System.err.println("[Recursos] No se pudo cargar la fuente " + nombreArchivo);
            }
            FUENTES_BASE.put(nombreArchivo, base);
        }
        Font base = FUENTES_BASE.get(nombreArchivo);
        derivada = (base != null)
                ? base.deriveFont(tamano)
                : new Font("SansSerif", Font.BOLD, Math.round(tamano)); // Respaldo
        FUENTES_DERIVADAS.put(clave, derivada);
        return derivada;
    }

    /** Abre un archivo de recursos buscando primero en el classpath y luego en carpetas. */
    private static InputStream abrir(String nombreArchivo) throws IOException {
        InputStream entrada = Recursos.class.getResourceAsStream(CARPETA + nombreArchivo);
        if (entrada != null) return entrada;
        for (String carpeta : CARPETAS_RESPALDO) {
            File archivo = new File(carpeta + nombreArchivo);
            if (archivo.isFile()) return new FileInputStream(archivo);
        }
        return null;
    }
}
