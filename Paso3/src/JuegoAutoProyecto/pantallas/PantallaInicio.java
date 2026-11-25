package JuegoAutoProyecto.pantallas;

import JuegoAutoProyecto.entidades.AutoRival;
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
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Pantalla de bienvenida: pide el <b>nombre del jugador</b> antes de empezar.
 *
 * <p>De fondo se ve la pista en movimiento con autos pasando. El campo de texto está
 * dibujado a mano: guardamos lo que escribe el usuario en un {@link StringBuilder}
 * y dibujamos un cursor que parpadea. Si se intenta empezar sin nombre, la tarjeta
 * "tiembla" y aparece un mensaje en rojo.</p>
 */
public class PantallaInicio extends Pantalla {

    // Posición de la tarjeta central y del campo de texto
    private static final int TARJETA_X = 50, TARJETA_Y = 258, TARJETA_W = 420, TARJETA_H = 236;
    private static final int CAMPO_X = 80, CAMPO_Y = 306, CAMPO_W = 360, CAMPO_H = 56;

    private final Pista pista = new Pista();
    private final List<AutoRival> autosFondo = new ArrayList<>();
    private final Random azar = new Random();
    private final StringBuilder nombre = new StringBuilder();
    private final Boton botonJugar;
    private final Boton botonHistorial;

    private int temblor;              // > 0 mientras la tarjeta tiembla por un error
    private String mensajeError = null;

    public PantallaInicio(VentanaJuego ventana) {
        super(ventana);
        botonJugar = new Boton("¡A CORRER!", "ENTER", 130, 404, 260, 58, Estilo.ROSA);
        botonHistorial = new Boton("Ver historial", null, 170, 552, 180, 42, Estilo.MORADO);
    }

    @Override
    protected void alMostrar() {
        nombre.setLength(0);
        nombre.append(ventana.getUltimoJugador());   // Recuerda el último nombre usado
        mensajeError = null;
        temblor = 0;
    }

    // =================================================================== Lógica

    @Override
    protected void actualizar() {
        pista.avanzar(4.5);

        // Autos de fondo: aparecen de vez en cuando en un carril al azar
        if (ticks % 70 == 0) {
            autosFondo.add(new AutoRival(azar.nextInt(Config.NUM_CARRILES), -100, azar));
        }
        Iterator<AutoRival> it = autosFondo.iterator();
        while (it.hasNext()) {
            AutoRival r = it.next();
            r.actualizar(4.5);
            if (r.salioDePantalla()) it.remove();
        }

        if (temblor > 0) temblor--;
        actualizarBotones(botonJugar, botonHistorial);
    }

    /** Intenta empezar la carrera; si no hay nombre, muestra el error. */
    private void intentarEmpezar() {
        String limpio = nombre.toString().trim();
        if (limpio.isEmpty()) {
            mensajeError = "Escribe tu nombre para empezar";
            temblor = 30;
            return;
        }
        ventana.iniciarCarrera(limpio);
    }

    @Override
    protected void caracterEscrito(char c) {
        if (c == '\b') {                                            // Borrar
            if (nombre.length() > 0) nombre.deleteCharAt(nombre.length() - 1);
            return;
        }
        boolean permitido = Character.isLetterOrDigit(c) || c == ' ' || c == '-' || c == '_' || c == '.';
        if (!permitido || nombre.length() >= Config.MAX_LETRAS_NOMBRE) return;
        if (c == ' ' && (nombre.length() == 0 || nombre.charAt(nombre.length() - 1) == ' ')) return;
        nombre.append(c);
        mensajeError = null;                                        // Al escribir, se borra el error
    }

    @Override
    protected void teclaPresionada(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) intentarEmpezar();
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) nombre.setLength(0);
    }

    @Override
    protected void click(Point p) {
        if (botonJugar.contiene(p)) intentarEmpezar();
        if (botonHistorial.contiene(p)) ventana.irAHistorial();
    }

    // =================================================================== Dibujo

    @Override
    protected void dibujar(Graphics2D g2) {
        // Fondo: pista + autos + capa oscura para que se lea bien el texto
        pista.dibujar(g2);
        for (AutoRival r : autosFondo) r.dibujar(g2);
        pista.dibujarVineta(g2);
        g2.setPaint(new LinearGradientPaint(0, 0, 0, Config.ALTO, new float[]{0f, 0.45f, 1f},
                new Color[]{new Color(9, 12, 28, 175), new Color(9, 12, 28, 110), new Color(9, 12, 28, 205)}));
        g2.fillRect(0, 0, Config.ANCHO, Config.ALTO);

        dibujarTitulo(g2);
        dibujarTarjeta(g2);
        dibujarRecord(g2);
        botonHistorial.dibujar(g2);
        dibujarControles(g2);
    }

    /** Título con brillo que "cae" al aparecer y luego flota suavemente. */
    private void dibujarTitulo(Graphics2D g2) {
        double entrada = Estilo.easeOutBack(ticks / 40.0);           // 0 -> 1 en los primeros 40 fotogramas
        double flotar = Math.sin(ticks * 0.04) * 4;
        double y = -120 + (140 + 120) * entrada + flotar;

        g2.setFont(Estilo.titulo(92));
        Estilo.textoBrillante(g2, "CARRERA", Config.ANCHO / 2.0, y, Estilo.AMARILLO, Estilo.NARANJA, 14);
        g2.setFont(Estilo.titulo(44));
        Estilo.textoBrillante(g2, "D E   A U T O S", Config.ANCHO / 2.0, y + 54, Color.WHITE, Estilo.ROSA, 8);
        g2.setFont(Estilo.normal(13));
        Composite original = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) Math.min(1, ticks / 50.0)));
        g2.setColor(Estilo.TEXTO_SUAVE);
        Estilo.textoCentrado(g2, "Programación 2  ·  Proyecto Juego de Autos", Config.ANCHO / 2.0, y + 86);
        g2.setComposite(original);
    }

    /** Tarjeta con el campo para escribir el nombre y el botón de empezar. */
    private void dibujarTarjeta(Graphics2D g2) {
        double entrada = Estilo.easeOutCubic((ticks - 10) / 30.0);   // Aparece un poquito después del título
        double temblorX = temblor > 0 ? Math.sin(temblor * 1.3) * temblor * 0.5 : 0;
        AffineTransform original = g2.getTransform();
        Composite compOriginal = g2.getComposite();
        g2.translate(temblorX, (1 - entrada) * 60);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) Math.max(0, entrada)));

        Estilo.panelVidrio(g2, TARJETA_X, TARJETA_Y, TARJETA_W, TARJETA_H, 28);

        g2.setFont(Estilo.negrita(12));
        g2.setColor(Estilo.CIAN);
        Estilo.textoCentrado(g2, "N O M B R E   D E L   P I L O T O", Config.ANCHO / 2.0, TARJETA_Y + 34);

        // Campo de texto
        RoundRectangle2D campo = new RoundRectangle2D.Double(CAMPO_X, CAMPO_Y, CAMPO_W, CAMPO_H, 18, 18);
        g2.setColor(new Color(2, 6, 23, 200));
        g2.fill(campo);
        double pulso = 0.5 + 0.5 * Math.sin(ticks * 0.08);
        Color borde = mensajeError != null ? Estilo.ROJO : Estilo.mezclar(Estilo.CIAN, Color.WHITE, pulso * 0.3);
        for (int i = 3; i >= 1; i--) {                                  // Resplandor del borde
            g2.setColor(Estilo.conAlfa(borde, 25 / i));
            g2.setStroke(new BasicStroke(i * 3f));
            g2.draw(campo);
        }
        g2.setColor(borde);
        g2.setStroke(new BasicStroke(2f));
        g2.draw(campo);

        g2.setFont(Estilo.negrita(22));
        double textoY = CAMPO_Y + CAMPO_H / 2.0 + 8;
        if (nombre.length() == 0) {
            g2.setFont(Estilo.normal(18));
            g2.setColor(Estilo.TEXTO_APAGADO);
            Estilo.textoCentrado(g2, "Escribe tu nombre...", Config.ANCHO / 2.0, textoY - 1);
            g2.setFont(Estilo.negrita(22));
        } else {
            g2.setColor(Color.WHITE);
            Estilo.textoCentrado(g2, nombre.toString(), Config.ANCHO / 2.0, textoY);
        }
        // Cursor que parpadea (visible la mitad del tiempo)
        if ((ticks / 30) % 2 == 0) {
            double cursorX = Config.ANCHO / 2.0 + Estilo.anchoTexto(g2, nombre.toString()) / 2.0 + 3;
            if (nombre.length() == 0) cursorX = Config.ANCHO / 2.0 - 92;
            g2.setColor(Estilo.CIAN);
            g2.fillRect((int) cursorX, CAMPO_Y + 15, 3, CAMPO_H - 30);
        }

        // Mensaje de ayuda o de error
        g2.setFont(Estilo.normal(12));
        if (mensajeError != null) {
            g2.setColor(new Color(252, 165, 165));
            Estilo.textoCentrado(g2, mensajeError, Config.ANCHO / 2.0, CAMPO_Y + CAMPO_H + 24);
        } else {
            g2.setColor(Estilo.TEXTO_APAGADO);
            Estilo.textoCentrado(g2, "Máximo " + Config.MAX_LETRAS_NOMBRE + " letras · presiona ENTER para empezar",
                    Config.ANCHO / 2.0, CAMPO_Y + CAMPO_H + 24);
        }

        botonJugar.dibujar(g2);
        g2.setComposite(compOriginal);
        g2.setTransform(original);
    }

    /** Muestra el récord actual de la sesión. */
    private void dibujarRecord(Graphics2D g2) {
        HistorialPartidas historial = ventana.getHistorial();
        Partida mejor = historial.getMejor();
        String texto = mejor == null
                ? "Aún no hay récord. ¡Sé el primero!"
                : "Récord: " + mejor.getPuntaje() + " pts  ·  " + mejor.getNombreJugador()
                  + "   (" + historial.cantidad() + (historial.cantidad() == 1 ? " partida)" : " partidas)");
        g2.setFont(Estilo.negrita(13));
        int w = Estilo.anchoTexto(g2, texto) + 34;
        double x = (Config.ANCHO - w) / 2.0;
        Estilo.trofeo(g2, x, 513, 20);
        Estilo.textoConSombra(g2, texto, x + 30, 528, Estilo.AMARILLO);
    }

    /** Fila de teclas con las instrucciones de juego. */
    private void dibujarControles(Graphics2D g2) {
        double y = 616;
        g2.setPaint(new GradientPaint(0, (float) y - 12, new Color(255, 255, 255, 0),
                Config.ANCHO / 2f, (float) y - 12, new Color(255, 255, 255, 50), true));
        g2.fillRect(40, (int) y - 14, Config.ANCHO - 80, 1);

        // Medimos el ancho total para centrar la fila
        String[][] grupos = {{"←", "→"}, {"ESPACIO"}, {"P"}};
        String[] etiquetas = {"mover", "turbo", "pausa"};
        double x = 62;
        for (int i = 0; i < grupos.length; i++) {
            for (String t : grupos[i]) {
                x += Estilo.tecla(g2, t, x, y) + 4;
            }
            g2.setFont(Estilo.normal(12));
            g2.setColor(Estilo.TEXTO_SUAVE);
            g2.drawString(etiquetas[i], (float) x + 4, (float) y + 17);
            x += Estilo.anchoTexto(g2, etiquetas[i]) + 30;
        }
        g2.setFont(Estilo.normal(11));
        g2.setColor(Estilo.TEXTO_APAGADO);
        Estilo.textoCentrado(g2, "Esquiva los autos (+" + Config.PUNTOS_POR_RIVAL + ")  ·  recoge monedas (+"
                + Config.PUNTOS_POR_MONEDA + ")  ·  cada " + Config.PUNTOS_POR_NIVEL + " pts sube la velocidad",
                Config.ANCHO / 2.0, 664);
    }
}
