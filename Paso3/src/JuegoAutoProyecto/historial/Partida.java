package JuegoAutoProyecto.historial;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Los datos de UNA partida terminada: quién jugó, cuántos puntos hizo, etc.
 *
 * <p>Todos sus atributos son {@code final}: una vez guardada, una partida no se puede
 * modificar (como un registro en un libro de actas). Esto es <b>encapsulamiento</b>:
 * los datos son privados y solo se leen con métodos "get".</p>
 */
public class Partida {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final int numero;              // 1 para la primera partida de la sesión, 2 para la segunda...
    private final String nombreJugador;
    private final int puntaje;
    private final int monedas;
    private final int rivalesEsquivados;
    private final int nivel;
    private final int velocidadMaximaKmh;
    private final long duracionSegundos;
    private final LocalDateTime fecha;

    public Partida(int numero, String nombreJugador, int puntaje, int monedas, int rivalesEsquivados,
                   int nivel, int velocidadMaximaKmh, long duracionSegundos, LocalDateTime fecha) {
        this.numero = numero;
        this.nombreJugador = nombreJugador;
        this.puntaje = puntaje;
        this.monedas = monedas;
        this.rivalesEsquivados = rivalesEsquivados;
        this.nivel = nivel;
        this.velocidadMaximaKmh = velocidadMaximaKmh;
        this.duracionSegundos = duracionSegundos;
        this.fecha = fecha;
    }

    public int getNumero() { return numero; }
    public String getNombreJugador() { return nombreJugador; }
    public int getPuntaje() { return puntaje; }
    public int getMonedas() { return monedas; }
    public int getRivalesEsquivados() { return rivalesEsquivados; }
    public int getNivel() { return nivel; }
    public int getVelocidadMaximaKmh() { return velocidadMaximaKmh; }
    public long getDuracionSegundos() { return duracionSegundos; }
    public LocalDateTime getFecha() { return fecha; }

    /** Duración en formato "mm:ss", por ejemplo "01:35". */
    public String getDuracionTexto() {
        return String.format("%02d:%02d", duracionSegundos / 60, duracionSegundos % 60);
    }

    /** Hora en que terminó la partida, por ejemplo "21:47:05". */
    public String getHoraTexto() {
        return fecha.format(FORMATO_HORA);
    }

    @Override
    public String toString() {
        return "#" + numero + " " + nombreJugador + " | " + puntaje + " pts | " + monedas + " monedas | "
                + rivalesEsquivados + " esquivados | nivel " + nivel + " | " + getDuracionTexto()
                + " | " + getHoraTexto();
    }
}
