package JuegoAutoProyecto;

import java.awt.Color; // Colores de pista, HUD y mensajes
import java.awt.Dimension; // Define el tamaño preferido del panel
import java.awt.Font; // Tipografías para textos y marcador
import java.awt.Graphics; // Clase base de dibujo en Swing
import java.awt.Graphics2D; // Versión mejorada de Graphics con antialiasing y transparencia
import java.awt.Image; // Imagen de fondo opcional
import java.awt.RenderingHints; // Suavizado de bordes al dibujar
import java.awt.event.ActionEvent; // Evento disparado por el Timer en cada tick
import java.awt.event.ActionListener; // Interfaz para escuchar los ticks del Timer
import java.awt.event.KeyEvent; // Evento de teclado (tecla presionada/soltada)
import java.awt.event.KeyListener; // Interfaz para capturar teclas
import java.util.ArrayList; // Lista dinámica para almacenar rivales y monedas
import java.util.Iterator; // Permite recorrer y eliminar elementos de una lista de forma segura
import java.util.List; // Interfaz List
import java.util.Random; // Generador de números aleatorios
import javax.swing.ImageIcon; // Carga imagen de fondo si existe
import javax.swing.JPanel; // Contenedor gráfico base en Swing
import javax.swing.Timer; // Temporizador que impulsa el bucle del juego

public class PanelCarrera extends JPanel implements ActionListener, KeyListener { // Panel que actúa como motor del juego y escucha eventos
    private static final int ANCHO = 500; // Ancho fijo de la pista
    private static final int ALTO = 650; // Alto fijo de la pista
    private static final int[] CARRILES = {115, 190, 265, 340}; // Coordenadas X de los 4 carriles

    private final Timer timer; // Temporizador del bucle principal
    private final Random random = new Random(); // Objeto Random para elegir carril, color o aparición de monedas
    private final Image fondoImagen; // Imagen de fondo personalizada (opcional)

    private AutoJugador jugador; // Instancia del auto del jugador
    private final List<AutoRival> rivales = new ArrayList<>(); // Lista de autos rivales activos
    private final List<MonedaBonus> monedas = new ArrayList<>(); // Lista de monedas activas

    private int puntaje; // Puntos acumulados por esquivar rivales o recoger monedas
    private int velocidadBase; // Velocidad base de la carrera
    private int offsetLinea; // Desplazamiento vertical de las líneas divisorias para dar sensación de movimiento
    private int contadorSpawn; // Contador de frames para generar nuevos objetos cada cierto tiempo
    private boolean turboActivo; // Indica si el jugador mantiene presionado ESPACIO
    private boolean gameOver; // Indica si el jugador chocó y terminó la partida

    public PanelCarrera() { // Constructor del panel
        setPreferredSize(new Dimension(ANCHO, ALTO)); // Fija el tamaño del panel a 500x650
        setFocusable(true); // Permite que el panel reciba el foco del teclado
        addKeyListener(this); // Registra este mismo panel como oyente de teclas

        Image img = new ImageIcon("fondo_pista.jpg").getImage(); // Intenta cargar 'fondo_pista.jpg'
        this.fondoImagen = (img.getWidth(null) > 0) ? img : null; // Usa la imagen solo si se cargó correctamente

        iniciarPartida(); // Inicializa variables, jugador y listas
        timer = new Timer(16, this); // Crea un Timer que llama a actionPerformed cada ~16 ms (~60 FPS)
        timer.start(); // Inicia el bucle del juego
    } // Cierra el constructor

    private void iniciarPartida() { // Reinicia o prepara todos los datos de una partida nueva
        jugador = new AutoJugador(ANCHO / 2 - 24, ALTO - 120, "auto_jugador.png"); // Ubica el auto abajo al centro
        rivales.clear(); // Limpia rivales de partidas anteriores
        monedas.clear(); // Limpia monedas de partidas anteriores
        puntaje = 0; // Reinicia puntaje a 0
        velocidadBase = 5; // Velocidad inicial
        offsetLinea = 0; // Reinicia desplazamiento de líneas
        contadorSpawn = 0; // Reinicia contador de aparición
        turboActivo = false; // Desactiva turbo al iniciar
        gameOver = false; // Marca que la partida está activa
    } // Cierra iniciarPartida

    @Override // Se ejecuta automáticamente en cada tick del Timer
    public void actionPerformed(ActionEvent e) { // Bucle lógico del juego
        if (gameOver) return; // Si ya chocó, no actualiza movimiento ni colisiones

        int velocidadActual = turboActivo ? velocidadBase + 4 : velocidadBase; // Suma +4 de velocidad si hay turbo
        offsetLinea = (offsetLinea + velocidadActual) % 40; // Cicla el offset cada 40 px para animar las líneas

        jugador.actualizar(); // Actualiza posición del auto del jugador

        contadorSpawn++; // Incrementa el contador de frames
        if (contadorSpawn % 45 == 0) { // Cada 45 ticks (~0.7s) genera un nuevo rival
            int carril = CARRILES[random.nextInt(CARRILES.length)]; // Escoge un carril al azar
            Color[] colores = { // Arreglo de colores posibles para los rivales
                    new Color(59, 130, 246), // Azul
                    new Color(16, 185, 129), // Verde esmeralda
                    new Color(168, 85, 247), // Morado
                    new Color(249, 115, 22)  // Naranja
            }; // Cierra arreglo de colores
            Color colorElegido = colores[random.nextInt(colores.length)]; // Elige un color aleatorio
            rivales.add(new AutoRival(carril, -90, velocidadActual, colorElegido)); // Crea rival arriba de la pantalla

            if (random.nextBoolean()) { // 50% de probabilidad de generar una moneda
                int carrilMoneda = CARRILES[random.nextInt(CARRILES.length)] + 10; // Centra la moneda en un carril
                if (carrilMoneda != carril + 10) { // Evita ponerla exactamente encima del rival recién creado
                    monedas.add(new MonedaBonus(carrilMoneda, -40, velocidadActual)); // Agrega la moneda arriba
                } // Cierra if
            } // Cierra if
        } // Cierra if de spawn

        Iterator<AutoRival> itRivales = rivales.iterator(); // Obtiene iterador para recorrer rivales
        while (itRivales.hasNext()) { // Mientras haya más rivales en la lista
            AutoRival r = itRivales.next(); // Obtiene el siguiente rival
            r.velocidad = velocidadActual; // Sincroniza su velocidad con la velocidad actual del juego
            r.actualizar(); // Mueve el rival hacia abajo

            if (r.getHitbox().intersects(jugador.getHitbox())) { // Comprueba si el hitbox del rival choca con el jugador
                gameOver = true; // Activa el estado de Game Over
            } else if (r.y > ALTO) { // Si el rival salió por la parte inferior sin chocar
                itRivales.remove(); // Elimina el rival de la lista para liberar memoria
                puntaje += 10; // Suma 10 puntos por esquivarlo
                if (puntaje % 100 == 0) velocidadBase++; // Cada 100 puntos aumenta la dificultad/velocidad
            } // Cierra if/else
        } // Cierra while de rivales

        Iterator<MonedaBonus> itMonedas = monedas.iterator(); // Obtiene iterador para recorrer monedas
        while (itMonedas.hasNext()) { // Mientras haya monedas
            MonedaBonus m = itMonedas.next(); // Obtiene la moneda actual
            m.velocidad = velocidadActual; // Sincroniza velocidad
            m.actualizar(); // Mueve la moneda hacia abajo

            if (m.getHitbox().intersects(jugador.getHitbox())) { // Si el jugador toca la moneda
                puntaje += 25; // Suma 25 puntos extra
                itMonedas.remove(); // Elimina la moneda recogida
            } else if (m.y > ALTO) { // Si la moneda salió de la pantalla
                itMonedas.remove(); // La elimina de la lista
            } // Cierra if/else
        } // Cierra while de monedas

        repaint(); // Solicita a Swing que vuelva a ejecutar paintComponent para redibujar el frame
    } // Cierra actionPerformed

    @Override // Dibuja todo el contenido visual del panel
    protected void paintComponent(Graphics g) { // Método estándar de dibujado en JPanel
        super.paintComponent(g); // Limpia el panel antes de dibujar el nuevo frame
        Graphics2D g2 = (Graphics2D) g; // Convierte Graphics a Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Suaviza bordes

        if (fondoImagen != null) { // Si el usuario colocó 'fondo_pista.jpg'
            g2.drawImage(fondoImagen, 0, 0, ANCHO, ALTO, null); // Dibuja la imagen como fondo
        } else { // Si no hay imagen, dibuja la carretera programáticamente
            g2.setColor(new Color(22, 101, 52)); // Verde para los bordes laterales
            g2.fillRect(0, 0, ANCHO, ALTO); // Dibuja el fondo lateral

            g2.setColor(new Color(51, 65, 85)); // Gris oscuro para el asfalto
            g2.fillRect(90, 0, 320, ALTO); // Dibuja la calzada

            g2.setColor(Color.WHITE); // Blanco para las líneas de borde
            g2.fillRect(84, 0, 6, ALTO); // Borde izquierdo
            g2.fillRect(410, 0, 6, ALTO); // Borde derecho

            g2.setColor(new Color(226, 232, 240)); // Gris claro para líneas divisorias de carril
            for (int y = -40 + offsetLinea; y < ALTO; y += 40) { // Repite líneas cada 40 px desplazadas por offsetLinea
                g2.fillRect(170, y, 4, 22); // Línea entre carril 1 y 2
                g2.fillRect(250, y, 4, 22); // Línea entre carril 2 y 3
                g2.fillRect(330, y, 4, 22); // Línea entre carril 3 y 4
            } // Cierra for
        } // Cierra else

        for (MonedaBonus m : monedas) m.dibujar(g2); // Dibuja todas las monedas (polimorfismo)
        for (AutoRival r : rivales) r.dibujar(g2); // Dibuja todos los rivales (polimorfismo)
        jugador.dibujar(g2); // Dibuja el auto del jugador encima de la pista

        g2.setColor(new Color(15, 23, 42, 200)); // Fondo semitransparente oscuro para el marcador (HUD)
        g2.fillRoundRect(12, 12, 210, 62, 10, 10); // Dibuja la caja del marcador
        g2.setColor(Color.WHITE); // Texto blanco
        g2.setFont(new Font("SansSerif", Font.BOLD, 15)); // Fuente en negrita
        g2.drawString("Puntaje: " + puntaje, 24, 34); // Muestra el puntaje actual
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12)); // Fuente normal
        g2.drawString("Velocidad: " + (turboActivo ? "TURBO!" : velocidadBase + "x"), 24, 56); // Muestra velocidad o turbo

        if (gameOver) { // Si el jugador perdió, dibuja la pantalla final superpuesta
            g2.setColor(new Color(0, 0, 0, 175)); // Capa oscura semitransparente
            g2.fillRect(0, 0, ANCHO, ALTO); // Cubre toda la pantalla

            g2.setColor(Color.WHITE); // Texto blanco
            g2.setFont(new Font("SansSerif", Font.BOLD, 34)); // Título grande
            g2.drawString("¡CHOQUE!", ANCHO / 2 - 85, ALTO / 2 - 30); // Mensaje de choque

            g2.setFont(new Font("SansSerif", Font.PLAIN, 18)); // Subtítulo
            g2.drawString("Puntaje final: " + puntaje, ANCHO / 2 - 75, ALTO / 2 + 10); // Muestra puntaje final
            g2.drawString("Presiona R para reiniciar", ANCHO / 2 - 102, ALTO / 2 + 45); // Instrucción para reiniciar
        } // Cierra if gameOver
    } // Cierra paintComponent

    @Override // Se ejecuta cuando el usuario presiona una tecla
    public void keyPressed(KeyEvent e) { // Maneja teclas presionadas
        int code = e.getKeyCode(); // Obtiene el código numérico de la tecla
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) jugador.setMovIzq(true); // Activa movimiento izq
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) jugador.setMovDer(true); // Activa movimiento der
        if (code == KeyEvent.VK_SPACE) turboActivo = true; // Activa turbo mientras se mantenga presionado
        if (code == KeyEvent.VK_R && gameOver) iniciarPartida(); // Reinicia si está en Game Over y pulsa R
    } // Cierra keyPressed

    @Override // Se ejecuta cuando el usuario suelta una tecla
    public void keyReleased(KeyEvent e) { // Maneja teclas soltadas
        int code = e.getKeyCode(); // Obtiene el código de la tecla soltada
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) jugador.setMovIzq(false); // Detiene movimiento izq
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) jugador.setMovDer(false); // Detiene movimiento der
        if (code == KeyEvent.VK_SPACE) turboActivo = false; // Apaga el turbo al soltar ESPACIO
    } // Cierra keyReleased

    @Override // Método obligatorio de KeyListener (no se usa en este juego)
    public void keyTyped(KeyEvent e) {} // Vacío porque usamos keyPressed y keyReleased
} // Cierra PanelCarrera
