package JuegoAutoProyecto.pantallas;

import JuegoAutoProyecto.efectos.SistemaParticulas;
import JuegoAutoProyecto.efectos.TextoFlotante;
import JuegoAutoProyecto.entidades.AutoJugador;
import JuegoAutoProyecto.entidades.AutoRival;
import JuegoAutoProyecto.entidades.MonedaBonus;
import JuegoAutoProyecto.escenario.Pista;
import JuegoAutoProyecto.historial.HistorialPartidas;
import JuegoAutoProyecto.historial.Partida;
import JuegoAutoProyecto.util.Boton;
import JuegoAutoProyecto.util.Config;
import JuegoAutoProyecto.util.Estilo;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * La pantalla donde se juega la carrera. Es el "motor" del juego.
 *
 * <p>Funciona como una <b>máquina de estados</b>: según el {@link Estado} actual,
 * cada fotograma hace cosas distintas.</p>
 * <pre>
 *  CUENTA_REGRESIVA (3, 2, 1) → JUGANDO ⇄ PAUSA
 *                                  │ (choque)
 *                                  ▼
 *                               CHOQUE (explosión) → FIN (tarjeta de resultados)
 * </pre>
 * <p>Al chocar, la partida se guarda automáticamente en el {@link HistorialPartidas}.</p>
 */
public class PantallaCarrera extends Pantalla {

    /** Los momentos posibles de una partida. */
    private enum Estado { CUENTA_REGRESIVA, JUGANDO, PAUSA, CHOQUE, FIN }

    private static final int FRAMES_POR_NUMERO = 45;                 // Cada número de la cuenta dura 45 fotogramas
    private static final int DURACION_CUENTA = FRAMES_POR_NUMERO * 3;
    private static final int DURACION_CHOQUE = 75;                    // Cuánto dura la explosión antes de la tarjeta
    private static final int TARJETA_X = 50, TARJETA_Y = 108, TARJETA_W = 420, TARJETA_H = 474;

    // ------------------------------------------------------------------ Objetos del juego
    private final Pista pista = new Pista();
    private final SistemaParticulas particulas = new SistemaParticulas();
    private final List<AutoRival> rivales = new ArrayList<>();
    private final List<MonedaBonus> monedas = new ArrayList<>();
    private final List<TextoFlotante> textos = new ArrayList<>();
    private final Random azar = new Random();
    private AutoJugador jugador = new AutoJugador();

    // ------------------------------------------------------------------ Estado de la partida
    private Estado estado = Estado.CUENTA_REGRESIVA;
    private int framesEnEstado;            // Fotogramas desde que se entró al estado actual
    private String nombreJugador = "";
    private int puntaje;
    private int monedasRecogidas;
    private int rivalesEsquivados;
    private int nivel;
    private double velocidadBase;          // Velocidad según el nivel
    private double velocidadActual;        // Velocidad real (incluye el turbo, cambia suavemente)
    private double velocidadMaxima;
    private double energiaTurbo;           // 0 a 100
    private boolean teclaTurbo;            // ¿Está presionado ESPACIO?
    private boolean turboActivo;           // Turbo presionado Y con energía
    private double intensidadTurbo;        // 0 a 1, para animar las líneas de velocidad
    private int framesJugando;
    private long tiempoJugadoMs;           // Tiempo real jugado (sin contar pausas)
    private long ultimoInstante;
    private double distanciaDesdeUltimoTrafico;

    // ------------------------------------------------------------------ Animaciones
    private double sacudida;               // Intensidad de la sacudida de pantalla
    private double destello;               // Destello blanco del choque (1 = blanco total)
    private double pulsoPuntaje;           // Hace "latir" el puntaje cuando sube
    private int puntajeMostrado;           // En la tarjeta final el puntaje cuenta hacia arriba
    private boolean nuevoRecord;
    private double choqueX, choqueY;       // Dónde fue el choque (para la onda expansiva)

    private final Boton botonReintentar;
    private final Boton botonNuevo;
    private final Boton botonHistorial;

    public PantallaCarrera(VentanaJuego ventana) {
        super(ventana);
        int by = TARJETA_Y + 390;
        botonReintentar = new Boton("Reintentar", null, TARJETA_X + 20, by, 122, 46, Estilo.VERDE);
        botonNuevo = new Boton("Nuevo piloto", null, TARJETA_X + 149, by, 122, 46, Estilo.CIAN);
        botonHistorial = new Boton("Historial", null, TARJETA_X + 278, by, 122, 46, Estilo.MORADO);
    }

    /**
     * Deja todo listo para una partida nueva (puntaje en 0, sin rivales, etc.).
     *
     * @param nombre nombre del jugador que va a correr
     */
    public void prepararPartida(String nombre) {
        nombreJugador = nombre;
        jugador = new AutoJugador();
        rivales.clear();
        monedas.clear();
        textos.clear();
        particulas.limpiar();
        puntaje = monedasRecogidas = rivalesEsquivados = 0;
        nivel = 1;
        velocidadBase = Config.VELOCIDAD_INICIAL;
        velocidadActual = 0;
        velocidadMaxima = 0;
        energiaTurbo = 100;
        teclaTurbo = turboActivo = false;
        intensidadTurbo = 0;
        framesJugando = 0;
        tiempoJugadoMs = 0;
        distanciaDesdeUltimoTrafico = 0;
        sacudida = destello = pulsoPuntaje = 0;
        puntajeMostrado = 0;
        nuevoRecord = false;
        cambiarEstado(Estado.CUENTA_REGRESIVA);
    }

    private void cambiarEstado(Estado nuevo) {
        estado = nuevo;
        framesEnEstado = 0;
        ultimoInstante = System.currentTimeMillis();
    }

    @Override
    protected void alOcultar() {
        // Soltamos todas las teclas por si el jugador las tenía presionadas al cambiar de pantalla
        jugador.setMoverIzquierda(false);
        jugador.setMoverDerecha(false);
        teclaTurbo = false;
    }

    // =================================================================== Lógica (cada fotograma)

    @Override
    protected void actualizar() {
        framesEnEstado++;
        switch (estado) {
            case CUENTA_REGRESIVA:
                pista.avanzar(2);                         // La pista rueda despacio durante la cuenta
                if (framesEnEstado % 3 == 0) particulas.humo(jugador.getEscapeX(), jugador.getEscapeY());
                if (framesEnEstado >= DURACION_CUENTA) cambiarEstado(Estado.JUGANDO);
                break;
            case JUGANDO:
                actualizarJuego();
                break;
            case PAUSA:
                return;                                  // En pausa no se mueve nada
            case CHOQUE:
                velocidadActual *= 0.93;                 // El mundo frena de a poco
                pista.avanzar(velocidadActual);
                jugador.actualizarChoque();
                for (AutoRival r : rivales) r.actualizar(velocidadActual);
                if (framesEnEstado >= DURACION_CHOQUE) cambiarEstado(Estado.FIN);
                break;
            case FIN:
                velocidadActual *= 0.93;
                pista.avanzar(velocidadActual);
                // El puntaje de la tarjeta "cuenta" rápido hasta el valor real
                if (puntajeMostrado < puntaje) {
                    puntajeMostrado = Math.min(puntaje, puntajeMostrado + Math.max(1, (puntaje - puntajeMostrado) / 10));
                }
                actualizarBotones(botonReintentar, botonNuevo, botonHistorial);
                break;
            default:
                break;
        }

        particulas.actualizar();
        Iterator<TextoFlotante> it = textos.iterator();
        while (it.hasNext()) {
            TextoFlotante t = it.next();
            t.actualizar();
            if (t.estaMuerto()) it.remove();
        }
        sacudida *= 0.9;
        destello *= 0.88;
        pulsoPuntaje *= 0.88;
    }

    /** Un fotograma de juego normal: mover todo, generar tráfico y revisar choques. */
    private void actualizarJuego() {
        framesJugando++;
        long ahora = System.currentTimeMillis();
        tiempoJugadoMs += Math.min(100, ahora - ultimoInstante);
        ultimoInstante = ahora;

        // --- Turbo: se gasta mientras se usa y se recarga solo cuando no
        turboActivo = teclaTurbo && energiaTurbo > 0;
        if (turboActivo) energiaTurbo = Math.max(0, energiaTurbo - 0.7);
        else energiaTurbo = Math.min(100, energiaTurbo + 0.12);
        intensidadTurbo += ((turboActivo ? 1 : 0) - intensidadTurbo) * 0.1;

        // --- Velocidad: se acerca suavemente a la velocidad deseada
        double deseada = velocidadBase + (turboActivo ? Config.EXTRA_TURBO : 0);
        velocidadActual += (deseada - velocidadActual) * 0.06;
        velocidadMaxima = Math.max(velocidadMaxima, velocidadActual);

        pista.avanzar(velocidadActual);
        jugador.setTurbo(turboActivo);
        jugador.actualizar(velocidadActual);

        // --- Humo y llamas del escape
        if (framesJugando % 2 == 0) particulas.humo(jugador.getEscapeX(), jugador.getEscapeY());
        if (turboActivo) {
            particulas.llama(jugador.getEscapeX() - 9, jugador.getEscapeY());
            particulas.llama(jugador.getEscapeX() + 9, jugador.getEscapeY());
        }

        // --- Tráfico: cada cierta distancia recorrida aparece un rival (y a veces una moneda)
        distanciaDesdeUltimoTrafico += velocidadActual;
        double separacion = Math.max(150, 260 - nivel * 12);   // A más nivel, más tráfico
        if (distanciaDesdeUltimoTrafico >= separacion) {
            generarTrafico();
            distanciaDesdeUltimoTrafico = 0;
        }

        // --- Rivales: moverlos, revisar choque y sumar puntos al esquivarlos
        Iterator<AutoRival> itRivales = rivales.iterator();
        while (itRivales.hasNext()) {
            AutoRival r = itRivales.next();
            r.actualizar(velocidadActual);
            if (r.colisionaCon(jugador)) {
                chocar(r);
                return;
            }
            if (!r.isEsquivado() && r.getY() > jugador.getY() + jugador.getAlto()) {
                r.marcarEsquivado();
                rivalesEsquivados++;
                sumarPuntos(Config.PUNTOS_POR_RIVAL);
                textos.add(new TextoFlotante("+" + Config.PUNTOS_POR_RIVAL, r.getCentroX(),
                        jugador.getY() + jugador.getAlto() - 4, new Color(226, 232, 240), 20, 40));
            }
            if (r.salioDePantalla()) itRivales.remove();
        }

        // --- Monedas: moverlas y revisar si el jugador las recoge
        Iterator<MonedaBonus> itMonedas = monedas.iterator();
        while (itMonedas.hasNext()) {
            MonedaBonus m = itMonedas.next();
            m.actualizar(velocidadActual);
            if (m.colisionaCon(jugador)) {
                monedasRecogidas++;
                energiaTurbo = Math.min(100, energiaTurbo + 20);   // Las monedas recargan el turbo
                sumarPuntos(Config.PUNTOS_POR_MONEDA);
                particulas.chispas(m.getCentroX(), m.getCentroY(), new Color(253, 224, 71, 255), 22);
                textos.add(new TextoFlotante("+" + Config.PUNTOS_POR_MONEDA, m.getCentroX(), m.getY(),
                        Estilo.AMARILLO, 28, 50));
                itMonedas.remove();
            } else if (m.salioDePantalla()) {
                itMonedas.remove();
            }
        }
    }

    /**
     * Crea un rival en un carril libre y, a veces, una moneda en otro carril.
     * Nunca bloquea todos los carriles: siempre queda al menos un camino libre.
     */
    private void generarTrafico() {
        boolean[] ocupado = new boolean[Config.NUM_CARRILES];
        int rivalesArriba = 0;
        for (AutoRival r : rivales) {
            if (r.getY() < 140) ocupado[r.getCarril()] = true;   // Carril con un auto recién aparecido
            if (r.getY() < 230) rivalesArriba++;
        }
        if (rivalesArriba >= Config.NUM_CARRILES - 1) return;    // Dejar siempre un hueco

        List<Integer> libres = new ArrayList<>();
        for (int c = 0; c < Config.NUM_CARRILES; c++) if (!ocupado[c]) libres.add(c);
        if (libres.isEmpty()) return;

        int carril = libres.remove(azar.nextInt(libres.size()));
        rivales.add(new AutoRival(carril, -AutoRival.ALTO - 10, azar));

        // En niveles altos, a veces aparecen dos autos a la vez
        boolean doble = nivel >= 3 && azar.nextInt(100) < Math.min(35, nivel * 5);
        if (doble && libres.size() >= 2 && rivalesArriba + 2 < Config.NUM_CARRILES) {
            int segundo = libres.remove(azar.nextInt(libres.size()));
            rivales.add(new AutoRival(segundo, -AutoRival.ALTO - 10, azar));
        }

        // 45 % de probabilidad de que aparezca una moneda en otro carril libre
        if (!libres.isEmpty() && azar.nextInt(100) < 45) {
            int carrilMoneda = libres.get(azar.nextInt(libres.size()));
            monedas.add(new MonedaBonus(carrilMoneda, -60));
        }
    }

    /** Suma puntos y revisa si se subió de nivel. */
    private void sumarPuntos(int cantidad) {
        int nivelAnterior = puntaje / Config.PUNTOS_POR_NIVEL;
        puntaje += cantidad;
        pulsoPuntaje = 1;
        if (puntaje / Config.PUNTOS_POR_NIVEL > nivelAnterior) subirNivel();
    }

    private void subirNivel() {
        nivel++;
        velocidadBase = Math.min(Config.VELOCIDAD_MAXIMA, velocidadBase + Config.AUMENTO_POR_NIVEL);
        textos.add(new TextoFlotante("¡NIVEL " + nivel + "!", Config.ANCHO / 2.0, Config.ALTO / 2.0 - 70,
                Estilo.CIAN, 50, 80));
        particulas.chispas(jugador.getCentroX(), jugador.getCentroY(), new Color(125, 211, 252, 255), 30);
    }

    /** El jugador chocó: explosión, sacudida y guardar la partida en el historial. */
    private void chocar(AutoRival rival) {
        double px = (jugador.getCentroX() + rival.getCentroX()) / 2;
        double py = (jugador.getCentroY() + rival.getCentroY()) / 2;
        particulas.explosion(px, py);
        choqueX = px;
        choqueY = py;
        sacudida = 18;
        destello = 1;
        jugador.chocar();
        turboActivo = false;
        guardarPartida();
        cambiarEstado(Estado.CHOQUE);
    }

    /** Registra la partida en el historial compartido. */
    private void guardarPartida() {
        HistorialPartidas historial = ventana.getHistorial();
        nuevoRecord = historial.esNuevoRecord(puntaje);   // Se pregunta ANTES de registrar
        int kmh = (int) Math.round(velocidadMaxima * Config.FACTOR_KMH);
        historial.registrar(nombreJugador, puntaje, monedasRecogidas, rivalesEsquivados, nivel, kmh,
                tiempoJugadoMs / 1000);
    }

    // =================================================================== Teclado y mouse

    @Override
    protected void teclaPresionada(KeyEvent e) {
        int tecla = e.getKeyCode();
        if (tecla == KeyEvent.VK_LEFT || tecla == KeyEvent.VK_A) jugador.setMoverIzquierda(true);
        if (tecla == KeyEvent.VK_RIGHT || tecla == KeyEvent.VK_D) jugador.setMoverDerecha(true);
        if (tecla == KeyEvent.VK_SPACE) teclaTurbo = true;

        switch (estado) {
            case JUGANDO:
                if (tecla == KeyEvent.VK_P || tecla == KeyEvent.VK_ESCAPE) cambiarEstado(Estado.PAUSA);
                break;
            case PAUSA:
                if (tecla == KeyEvent.VK_P) cambiarEstado(Estado.JUGANDO);
                if (tecla == KeyEvent.VK_ESCAPE) ventana.irAInicio();   // Abandona sin guardar
                break;
            case FIN:
                if (tecla == KeyEvent.VK_R || tecla == KeyEvent.VK_ENTER) prepararPartida(nombreJugador);
                if (tecla == KeyEvent.VK_N) ventana.irAInicio();
                if (tecla == KeyEvent.VK_H) ventana.irAHistorial();
                break;
            default:
                break;
        }
    }

    @Override
    protected void teclaSoltada(KeyEvent e) {
        int tecla = e.getKeyCode();
        if (tecla == KeyEvent.VK_LEFT || tecla == KeyEvent.VK_A) jugador.setMoverIzquierda(false);
        if (tecla == KeyEvent.VK_RIGHT || tecla == KeyEvent.VK_D) jugador.setMoverDerecha(false);
        if (tecla == KeyEvent.VK_SPACE) teclaTurbo = false;
    }

    @Override
    protected void click(Point p) {
        if (estado != Estado.FIN || framesEnEstado < 28) return;   // Solo cuando la tarjeta ya llegó
        if (botonReintentar.contiene(p)) prepararPartida(nombreJugador);
        else if (botonNuevo.contiene(p)) ventana.irAInicio();
        else if (botonHistorial.contiene(p)) ventana.irAHistorial();
    }

    // =================================================================== Dibujo

    @Override
    protected void dibujar(Graphics2D g2) {
        AffineTransform base = g2.getTransform();
        if (sacudida > 0.5) {                                       // Sacudida de cámara al chocar
            g2.translate((azar.nextDouble() * 2 - 1) * sacudida, (azar.nextDouble() * 2 - 1) * sacudida);
        }

        pista.dibujar(g2);
        dibujarLineasVelocidad(g2);
        for (MonedaBonus m : monedas) m.dibujar(g2);
        for (AutoRival r : rivales) r.dibujar(g2);
        boolean explosionEncima = estado == Estado.CHOQUE || estado == Estado.FIN;
        if (!explosionEncima) particulas.dibujar(g2);   // El humo va detrás del auto...
        jugador.dibujar(g2);
        if (explosionEncima) {                          // ...pero la explosión va encima
            dibujarOndaExpansiva(g2);
            particulas.dibujar(g2);
        }
        for (TextoFlotante t : textos) t.dibujar(g2);
        pista.dibujarVineta(g2);

        g2.setTransform(base);                          // El HUD no tiembla
        dibujarHUD(g2);

        if (destello > 0.02) {
            g2.setColor(new Color(255, 255, 255, (int) (220 * destello)));
            g2.fillRect(0, 0, Config.ANCHO, Config.ALTO);
        }

        switch (estado) {
            case CUENTA_REGRESIVA: dibujarCuentaRegresiva(g2); break;
            case JUGANDO: if (framesJugando < 40) dibujarYa(g2); break;
            case PAUSA: dibujarPausa(g2); break;
            case FIN: dibujarTarjetaFinal(g2); break;
            default: break;
        }
    }

    /** Anillo de luz que se expande desde el punto del choque, con un resplandor de fuego. */
    private void dibujarOndaExpansiva(Graphics2D g2) {
        if (estado != Estado.CHOQUE || framesEnEstado > 32) return;
        double t = framesEnEstado / 32.0;                 // 0 al chocar, 1 al terminar la onda
        double radio = 20 + 150 * Estilo.easeOutCubic(t);
        float radioFuego = (float) (40 + 70 * t);
        g2.setPaint(new RadialGradientPaint(new Point2D.Double(choqueX, choqueY), radioFuego,
                new float[]{0f, 0.5f, 1f}, new Color[]{
                        new Color(255, 245, 200, (int) (220 * (1 - t))),
                        new Color(251, 146, 60, (int) (140 * (1 - t))),
                        new Color(239, 68, 68, 0)}));
        g2.fill(new Ellipse2D.Double(choqueX - radioFuego, choqueY - radioFuego, radioFuego * 2, radioFuego * 2));
        g2.setStroke(new BasicStroke((float) (12 * (1 - t) + 1)));
        g2.setColor(new Color(255, 237, 213, (int) (200 * (1 - t))));
        g2.draw(new Ellipse2D.Double(choqueX - radio, choqueY - radio, radio * 2, radio * 2));
    }

    /** Líneas blancas que pasan rápido cuando se usa el turbo. */
    private void dibujarLineasVelocidad(Graphics2D g2) {
        if (intensidadTurbo < 0.05) return;
        double d = pista.getDesplazamiento();
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 16; i++) {
            double lx = Config.PISTA_X + 12 + (i * 97) % (Config.PISTA_ANCHO - 24);
            double largo = 60 + (i * 37) % 70;
            double ly = ((i * 131 + d * 1.6) % (Config.ALTO + 200)) - 200;
            g2.setColor(new Color(255, 255, 255, (int) (70 * intensidadTurbo)));
            g2.draw(new Line2D.Double(lx, ly, lx, ly + largo));
        }
    }

    /** Marcador: barra superior, energía del turbo (izquierda) y velocímetro (derecha). */
    private void dibujarHUD(Graphics2D g2) {
        int ancho = Config.ANCHO, alto = Config.ALTO;

        // ---------- Barra superior
        Estilo.panelVidrio(g2, 10, 10, ancho - 20, 60, 22);
        g2.setFont(Estilo.negrita(10));
        g2.setColor(Estilo.TEXTO_APAGADO);
        g2.drawString("PILOTO", 28, 33);
        g2.setFont(Estilo.negrita(16));
        Estilo.textoConSombra(g2, Estilo.recortar(g2, nombreJugador, 140), 28, 56, Color.WHITE);

        g2.setFont(Estilo.negrita(10));
        g2.setColor(Estilo.TEXTO_APAGADO);
        Estilo.textoCentrado(g2, "PUNTAJE", ancho / 2.0, 30);
        AffineTransform t = g2.getTransform();
        double escala = 1 + 0.22 * pulsoPuntaje;
        g2.translate(ancho / 2.0, 61);
        g2.scale(escala, escala);
        g2.setFont(Estilo.titulo(30));
        Estilo.textoCentradoConSombra(g2, String.valueOf(puntaje), 0, 0,
                Estilo.mezclar(Estilo.AMARILLO, Color.WHITE, pulsoPuntaje * 0.6));
        g2.setTransform(t);

        Estilo.iconoMoneda(g2, ancho - 122, 21, 18);
        g2.setFont(Estilo.negrita(16));
        Estilo.textoConSombra(g2, "x " + monedasRecogidas, ancho - 98, 37, Color.WHITE);
        g2.setFont(Estilo.negrita(11));
        g2.setColor(Estilo.CIAN);
        String textoNivel = "NIVEL " + nivel;
        g2.drawString(textoNivel, ancho - 28 - Estilo.anchoTexto(g2, textoNivel), 58);

        // ---------- Energía del turbo (sobre el pasto izquierdo)
        int px = 16, py = alto - 204;
        Estilo.panelVidrio(g2, px, py, 80, 188, 20);
        g2.setFont(Estilo.negrita(11));
        g2.setColor(turboActivo ? Estilo.NARANJA : Estilo.TEXTO_SUAVE);
        Estilo.textoCentrado(g2, "TURBO", px + 40, py + 22);
        int barraX = px + 27, barraY = py + 32, barraW = 26, barraH = 112;
        g2.setColor(new Color(2, 6, 23, 200));
        g2.fill(new RoundRectangle2D.Double(barraX, barraY, barraW, barraH, 12, 12));
        int lleno = (int) (barraH * energiaTurbo / 100.0);
        if (lleno > 0) {
            g2.setPaint(new GradientPaint(0, barraY + barraH, Estilo.CIAN, 0, barraY, Estilo.MORADO));
            g2.fill(new RoundRectangle2D.Double(barraX, barraY + barraH - lleno, barraW, lleno, 12, 12));
            if (turboActivo) {                                   // Brillo parpadeante mientras se usa
                g2.setColor(new Color(255, 255, 255, 60 + (int) (50 * Math.sin(ticks * 0.6))));
                g2.fill(new RoundRectangle2D.Double(barraX, barraY + barraH - lleno, barraW, lleno, 12, 12));
            }
        }
        g2.setColor(new Color(255, 255, 255, 40));
        for (int i = 1; i < 4; i++) g2.fillRect(barraX + 4, barraY + barraH * i / 4, barraW - 8, 1);
        g2.setColor(new Color(255, 255, 255, 70));
        g2.draw(new RoundRectangle2D.Double(barraX, barraY, barraW, barraH, 12, 12));
        g2.setFont(Estilo.negrita(13));
        g2.setColor(Color.WHITE);
        Estilo.textoCentrado(g2, (int) energiaTurbo + "%", px + 40, py + 164);
        g2.setFont(Estilo.negrita(9));
        g2.setColor(Estilo.TEXTO_APAGADO);
        Estilo.textoCentrado(g2, "ESPACIO", px + 40, py + 178);

        // ---------- Velocímetro (sobre el pasto derecho)
        dibujarVelocimetro(g2, ancho - 60, alto - 84);
    }

    /** Velocímetro circular con aguja. */
    private void dibujarVelocimetro(Graphics2D g2, double cx, double cy) {
        double radio = 40;
        Estilo.panelVidrio(g2, cx - 50, cy - 50, 100, 100, 100);
        double kmh = velocidadActual * Config.FACTOR_KMH;
        double fraccion = Math.min(1, kmh / 380.0);

        // Arco de fondo (270 grados, abierto abajo)
        g2.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(255, 255, 255, 35));
        g2.draw(new Arc2D.Double(cx - radio, cy - radio, radio * 2, radio * 2, 225, -270, Arc2D.OPEN));
        // Arco de color según la velocidad: verde → amarillo → rojo
        Color color = fraccion < 0.5
                ? Estilo.mezclar(Estilo.VERDE, Estilo.AMARILLO, fraccion * 2)
                : Estilo.mezclar(Estilo.AMARILLO, Estilo.ROJO, (fraccion - 0.5) * 2);
        g2.setColor(color);
        g2.draw(new Arc2D.Double(cx - radio, cy - radio, radio * 2, radio * 2, 225, -270 * fraccion, Arc2D.OPEN));

        // Aguja
        double angulo = Math.toRadians(225 - 270 * fraccion);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(248, 113, 113));
        g2.draw(new Line2D.Double(cx, cy, cx + Math.cos(angulo) * (radio - 10), cy - Math.sin(angulo) * (radio - 10)));
        g2.setColor(Color.WHITE);
        g2.fillOval((int) cx - 4, (int) cy - 4, 8, 8);

        g2.setFont(Estilo.titulo(20));
        Estilo.textoCentradoConSombra(g2, String.valueOf((int) kmh), cx, cy + 30, Color.WHITE);
        g2.setFont(Estilo.negrita(8));
        g2.setColor(Estilo.TEXTO_APAGADO);
        Estilo.textoCentrado(g2, "KM/H", cx + 1, cy + 41);
    }

    /** Números gigantes 3, 2, 1 que aparecen con un "zoom". */
    private void dibujarCuentaRegresiva(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRect(0, 0, Config.ANCHO, Config.ALTO);

        g2.setFont(Estilo.negrita(18));
        Estilo.textoCentradoConSombra(g2, "¡Prepárate, " + nombreJugador + "!", Config.ANCHO / 2.0,
                Config.ALTO / 2.0 - 120, Color.WHITE);

        int indice = Math.min(2, framesEnEstado / FRAMES_POR_NUMERO);
        double t = (framesEnEstado % FRAMES_POR_NUMERO) / (double) FRAMES_POR_NUMERO;
        String[] numeros = {"3", "2", "1"};
        Color[] colores = {Estilo.ROJO, Estilo.NARANJA, Estilo.AMARILLO};
        dibujarTextoGigante(g2, numeros[indice], colores[indice],
                1.8 - 0.8 * Estilo.easeOutCubic(Math.min(1, t * 2.5)),
                t < 0.75 ? 1f : (float) ((1 - t) / 0.25));
    }

    /** "¡YA!" en verde que crece y se desvanece al empezar. */
    private void dibujarYa(Graphics2D g2) {
        double t = framesJugando / 40.0;
        dibujarTextoGigante(g2, "¡YA!", Estilo.VERDE, 1 + 0.5 * t, (float) (1 - t));
    }

    private void dibujarTextoGigante(Graphics2D g2, String texto, Color color, double escala, float alfa) {
        Composite original = g2.getComposite();
        AffineTransform t = g2.getTransform();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, alfa))));
        g2.translate(Config.ANCHO / 2.0, Config.ALTO / 2.0 + 30);
        g2.scale(escala, escala);
        g2.setFont(Estilo.titulo(140));
        Estilo.textoBrillante(g2, texto, 0, 0, color, color, 18);
        g2.setTransform(t);
        g2.setComposite(original);
    }

    private void dibujarPausa(Graphics2D g2) {
        g2.setColor(new Color(2, 6, 23, 170));
        g2.fillRect(0, 0, Config.ANCHO, Config.ALTO);
        g2.setFont(Estilo.titulo(84));
        Estilo.textoBrillante(g2, "PAUSA", Config.ANCHO / 2.0, Config.ALTO / 2.0, Color.WHITE, Estilo.CIAN, 12);
        g2.setFont(Estilo.normal(15));
        g2.setColor(Estilo.TEXTO_SUAVE);
        Estilo.textoCentrado(g2, "P  para continuar   ·   ESC  para salir al menú", Config.ANCHO / 2.0,
                Config.ALTO / 2.0 + 44);
    }

    /** Tarjeta de resultados que sube "rebotando" después del choque. */
    private void dibujarTarjetaFinal(Graphics2D g2) {
        double progreso = framesEnEstado / 28.0;
        g2.setColor(new Color(2, 6, 23, (int) (150 * Math.min(1, progreso))));
        g2.fillRect(0, 0, Config.ANCHO, Config.ALTO);

        AffineTransform original = g2.getTransform();
        g2.translate(0, (1 - Estilo.easeOutBack(progreso)) * 520);   // Sube desde abajo con rebote

        int x = TARJETA_X, y = TARJETA_Y, w = TARJETA_W;
        double cx = x + w / 2.0;
        Estilo.panelVidrio(g2, x, y, w, TARJETA_H, 30);

        g2.setFont(Estilo.titulo(54));
        Estilo.textoBrillante(g2, "¡CHOQUE!", cx, y + 66, Color.WHITE, Estilo.ROJO, 12);
        g2.setFont(Estilo.normal(15));
        g2.setColor(Estilo.TEXTO_SUAVE);
        Estilo.textoCentrado(g2, "Piloto: " + nombreJugador, cx, y + 94);

        g2.setFont(Estilo.negrita(11));
        g2.setColor(Estilo.TEXTO_APAGADO);
        Estilo.textoCentrado(g2, "P U N T A J E   F I N A L", cx, y + 126);
        g2.setFont(Estilo.titulo(76));
        Estilo.textoBrillante(g2, String.valueOf(puntajeMostrado), cx, y + 202, Estilo.AMARILLO, Estilo.NARANJA, 10);

        // Récord
        Partida mejor = ventana.getHistorial().getMejor();
        if (nuevoRecord) {
            double pulso = 1 + 0.06 * Math.sin(ticks * 0.15);
            AffineTransform antes = g2.getTransform();
            g2.translate(cx, y + 234);
            g2.scale(pulso, pulso);
            RoundRectangle2D insignia = new RoundRectangle2D.Double(-100, -15, 200, 30, 30, 30);
            g2.setPaint(new GradientPaint(-100, 0, Estilo.AMARILLO, 100, 0, Estilo.NARANJA));
            g2.fill(insignia);
            Estilo.trofeo(g2, -88, -10, 20);
            g2.setFont(Estilo.negrita(14));
            g2.setColor(new Color(66, 32, 6));
            Estilo.textoCentrado(g2, "¡NUEVO RÉCORD!", 12, 6);
            g2.setTransform(antes);
        } else if (mejor != null) {
            g2.setFont(Estilo.negrita(13));
            String texto = "Récord: " + mejor.getPuntaje() + " pts  ·  " + mejor.getNombreJugador();
            double tx = cx - (Estilo.anchoTexto(g2, texto) + 26) / 2.0;
            Estilo.trofeo(g2, tx, y + 225, 18);
            Estilo.textoConSombra(g2, texto, tx + 26, y + 239, Estilo.AMARILLO);
        }

        // Estadísticas en 4 cajitas
        Partida ultima = ventana.getHistorial().cantidad() > 0
                ? ventana.getHistorial().getPartidas().get(ventana.getHistorial().cantidad() - 1) : null;
        String[] etiquetas = {"MONEDAS", "ESQUIVADOS", "TIEMPO", "KM/H MÁX"};
        String[] valores = {
                String.valueOf(monedasRecogidas),
                String.valueOf(rivalesEsquivados),
                ultima != null ? ultima.getDuracionTexto() : "00:00",
                (ultima != null ? ultima.getVelocidadMaximaKmh() : 0) + ""
        };
        Color[] colores = {Estilo.AMARILLO, Estilo.VERDE, Estilo.CIAN, Estilo.ROSA};
        for (int i = 0; i < 4; i++) {
            double bx = x + 20 + i * 97, by = y + 264;
            g2.setColor(new Color(2, 6, 23, 150));
            g2.fill(new RoundRectangle2D.Double(bx, by, 89, 74, 16, 16));
            g2.setColor(Estilo.conAlfa(colores[i], 160));
            g2.fill(new RoundRectangle2D.Double(bx + 24, by, 41, 3, 3, 3));
            g2.setFont(Estilo.negrita(9));
            g2.setColor(Estilo.TEXTO_APAGADO);
            Estilo.textoCentrado(g2, etiquetas[i], bx + 44.5, by + 24);
            g2.setFont(Estilo.titulo(26));
            Estilo.textoCentradoConSombra(g2, valores[i], bx + 44.5, by + 58, colores[i]);
        }
        g2.setFont(Estilo.normal(12));
        g2.setColor(Estilo.TEXTO_SUAVE);
        Estilo.textoCentrado(g2, "Llegaste al nivel " + nivel, cx, y + 366);

        botonReintentar.dibujar(g2);
        botonNuevo.dibujar(g2);
        botonHistorial.dibujar(g2);

        g2.setFont(Estilo.normal(11));
        g2.setColor(Estilo.TEXTO_APAGADO);
        Estilo.textoCentrado(g2, "R  reintentar   ·   N  nuevo piloto   ·   H  historial", cx, y + 458);
        g2.setTransform(original);
    }
}
