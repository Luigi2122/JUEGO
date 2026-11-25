package Practica1;

import javax.swing.*;
import java.awt.*;

public class VentanaPedidos extends JFrame {
    private JTextField txtCliente, txtCantidad;
    private JComboBox<String> cboMesa, cboPlato;
    private JRadioButton rbAqui, rbLlevar;
    private JCheckBox chkBebida, chkPostre;
    private JTextArea txtDetalle;
    private JLabel lblTotal;
    private JButton btnAgregar, btnLimpiar, btnConfirmar;

    public  VentanaPedidos(){
        setTitle("Pedidos - Dona Rosa");
        setSize(820, 510);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ---- Encabezado (Norte) ----
        JPanel pnlTitulo = new JPanel(new GridLayout(2, 1));
        pnlTitulo.setBackground(new Color(15, 35, 60));
        JLabel lblTitulo = new JLabel(" Cafeteria Dona Rosa");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        JLabel lblSub = new JLabel("  Registro de pedidos");
        lblSub.setForeground(Color.WHITE);
        pnlTitulo.add(lblTitulo);
        pnlTitulo.add(lblSub);
        add(pnlTitulo, BorderLayout.NORTH);

        // ---- Centro: formulario + detalle ----
        JPanel pnlCentro = new JPanel(new GridLayout(1, 2, 10, 0));

        // Formulario (izquierda)
        JPanel pnlDatos = new JPanel(new GridLayout(6, 2, 5, 10));
        pnlDatos.setBorder(BorderFactory.createTitledBorder("Datos del pedido"));

        txtCliente = new JTextField();
        cboMesa = new JComboBox<>(new String[]{"Mesa 1", "Mesa 2", "Mesa 3"});
        cboPlato = new JComboBox<>(new String[]{"Majadito", "Silpancho", "Sopa de maní"});
        txtCantidad = new JTextField();

        rbAqui = new JRadioButton("Comer aquí", true);
        rbLlevar = new JRadioButton("Para llevar");
        ButtonGroup grupoTipo = new ButtonGroup();
        grupoTipo.add(rbAqui);
        grupoTipo.add(rbLlevar);
        JPanel pnlTipo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlTipo.add(rbAqui);
        pnlTipo.add(rbLlevar);

        chkBebida = new JCheckBox("Bebida");
        chkPostre = new JCheckBox("Postre");
        JPanel pnlExtras = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlExtras.add(chkBebida);
        pnlExtras.add(chkPostre);

        pnlDatos.add(new JLabel("Cliente:"));   pnlDatos.add(txtCliente);
        pnlDatos.add(new JLabel("Mesa:"));      pnlDatos.add(cboMesa);
        pnlDatos.add(new JLabel("Plato:"));     pnlDatos.add(cboPlato);
        pnlDatos.add(new JLabel("Cantidad:"));  pnlDatos.add(txtCantidad);
        pnlDatos.add(new JLabel("Tipo:"));      pnlDatos.add(pnlTipo);
        pnlDatos.add(new JLabel("Extras:"));    pnlDatos.add(pnlExtras);

        // Detalle (derecha)
        txtDetalle = new JTextArea();
        txtDetalle.setEditable(false);
        JScrollPane scroll = new JScrollPane(txtDetalle);
        scroll.setBorder(BorderFactory.createTitledBorder("Detalle del pedido"));

        pnlCentro.add(pnlDatos);
        pnlCentro.add(scroll);
        add(pnlCentro, BorderLayout.CENTER);

        // ---- Sur: total + botones ----
        JPanel pnlSur = new JPanel(new BorderLayout());
        lblTotal = new JLabel("  Total: Bs 0.00");
        lblTotal.setFont(new Font("SansSerif", Font.PLAIN, 16));

        JPanel pnlBotones = new JPanel(new FlowLayout());
        btnAgregar = new JButton("Agregar");
        btnLimpiar = new JButton("Limpiar");
        btnConfirmar = new JButton("Confirmar");
        pnlBotones.add(btnAgregar);
        pnlBotones.add(btnLimpiar);
        pnlBotones.add(btnConfirmar);

        pnlSur.add(lblTotal, BorderLayout.WEST);
        pnlSur.add(pnlBotones, BorderLayout.EAST);
        add(pnlSur, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        new VentanaPedidos().setVisible(true);
    }
}


