package JuegoAutoProyecto.historial;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Guarda todas las partidas jugadas <b>mientras el juego está abierto</b>.
 *
 * <p>Las partidas se guardan en un {@link ArrayList} en memoria. Al cerrar la
 * ventana, el historial se borra (así lo pide el enunciado: "guardar el historial
 * mientras el juego esté corriendo"). Además, cada partida se imprime en la consola
 * para poder revisarla desde IntelliJ.</p>
 */
public class HistorialPartidas {

    private final List<Partida> partidas = new ArrayList<>();

    /**
     * Crea y guarda una partida nueva con los datos de un juego que acaba de terminar.
     *
     * @return la partida guardada
     */
    public Partida registrar(String nombre, int puntaje, int monedas, int esquivados, int nivel,
                             int velocidadMaximaKmh, long duracionSegundos) {
        Partida partida = new Partida(partidas.size() + 1, nombre, puntaje, monedas, esquivados,
                nivel, velocidadMaximaKmh, duracionSegundos, LocalDateTime.now());
        partidas.add(partida);
        System.out.println("[Historial] Partida guardada -> " + partida);
        return partida;
    }

    /** @return las partidas en el orden en que se jugaron (no se puede modificar) */
    public List<Partida> getPartidas() {
        return Collections.unmodifiableList(partidas);
    }

    /** @return las partidas de la más reciente a la más antigua */
    public List<Partida> getRecientes() {
        List<Partida> copia = new ArrayList<>(partidas);
        Collections.reverse(copia);
        return copia;
    }

    /** @return las partidas ordenadas de mayor a menor puntaje (ranking) */
    public List<Partida> getPorPuntaje() {
        List<Partida> copia = new ArrayList<>(partidas);
        copia.sort(Comparator.comparingInt(Partida::getPuntaje).reversed()
                .thenComparingInt(Partida::getNumero));
        return copia;
    }

    /** @return la partida con mayor puntaje, o {@code null} si todavía no se jugó ninguna */
    public Partida getMejor() {
        Partida mejor = null;
        for (Partida p : partidas) {
            if (mejor == null || p.getPuntaje() > mejor.getPuntaje()) mejor = p;
        }
        return mejor;
    }

    /**
     * Indica si un puntaje superaría el récord actual.
     * Hay que llamarlo <i>antes</i> de registrar la partida.
     */
    public boolean esNuevoRecord(int puntaje) {
        Partida mejor = getMejor();
        return puntaje > 0 && (mejor == null || puntaje > mejor.getPuntaje());
    }

    /** @return cantidad de partidas jugadas */
    public int cantidad() {
        return partidas.size();
    }

    /** @return suma de todas las monedas recogidas en la sesión */
    public int totalMonedas() {
        int total = 0;
        for (Partida p : partidas) total += p.getMonedas();
        return total;
    }

    /** @return {@code true} si todavía no hay partidas */
    public boolean estaVacio() {
        return partidas.isEmpty();
    }
}
