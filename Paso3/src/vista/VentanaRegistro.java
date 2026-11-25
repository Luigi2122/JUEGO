package vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.*;
import modelo.Estudiante;
import modelo.Registro;

public class VentanaRegistro extends JFrame {
    private Registro registro;              // llega desde el Main
    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JTextArea area;
    private JTextField txtBuscar;

    public VentanaRegistro(Registro registro) {
        this.registro = registro;
        setTitle("Registro de estudiantes");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        txtCodigo = new JTextField(10);
        txtNombre = new JTextField(16);
        JButton btnAgregar = new JButton("Agregar");
        form.add(new JLabel("Código:")); form.add(txtCodigo);
        form.add(new JLabel("Nombre:")); form.add(txtNombre);
        form.add(new JLabel(""));        form.add(btnAgregar);

        area = new JTextArea(8, 30);
        area.setEditable(false);

        JPanel barra = new JPanel(new FlowLayout());
        txtBuscar = new JTextField(10);
        JButton btnBuscar = new JButton("Buscar");
        barra.add(new JLabel("Buscar por código:"));
        barra.add(txtBuscar);
        barra.add(btnBuscar);

        add(form, BorderLayout.NORTH);
        add(new JScrollPane(area), BorderLayout.CENTER);
        add(barra, BorderLayout.SOUTH);

        btnAgregar.addActionListener(e -> agregar());
        btnBuscar.addActionListener(e -> buscar());
        mostrarLista();
        pack();
        setLocationRelativeTo(null);
    }

    private void agregar() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        if (codigo.isBlank() || nombre.isBlank()) {
            JOptionPane.showMessageDialog(this, "Completá código y nombre.");
            return;
        }
        registro.agregar(new Estudiante(codigo, nombre));
        txtCodigo.setText("");
        txtNombre.setText("");
        mostrarLista();
    }

    private void buscar() {
        String codigo = txtBuscar.getText().trim();
        Estudiante est = registro.buscarPorCodigo(codigo);
        if (est == null) {
            JOptionPane.showMessageDialog(this,
                    "No hay estudiante con el código " + codigo + ".");
        } else {
            JOptionPane.showMessageDialog(this, "Encontrado: " + est);
        }
    }

    private void mostrarLista() {
        String texto = "Inscritos: " + registro.cantidad() + "\n";
        for (Estudiante e : registro.getEstudiantes()) {
            texto += e + "\n";
        }
        area.setText(texto);
    }
}
