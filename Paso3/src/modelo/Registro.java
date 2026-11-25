package modelo;

import java.util.ArrayList;
import java.util.List;

public class Registro {
    private ArrayList<Estudiante> estudiantes;

    public Registro() {
        estudiantes = new ArrayList<>();
    }
    public void agregar(Estudiante e) {
        estudiantes.add(e);
    }
    public List<Estudiante> getEstudiantes() {
        return estudiantes;
    }
    public Estudiante buscarPorCodigo(String codigo) {
        for (Estudiante e : estudiantes) {
            if (e.getCodigo().equals(codigo)) return e;
        }
        return null;                        // no está
    }
    public int cantidad() {
        return estudiantes.size();
    }
}
