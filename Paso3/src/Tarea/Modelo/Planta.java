package Tarea.Modelo;
import java.util.ArrayList;
import java.util.List;

public class Planta {
    private ArrayList<Maquina> maquinas;

    public Planta() {
        maquinas = new ArrayList<>();
    }
    public void agregar(Maquina m) {
        maquinas.add(m);
    }
    public List<Maquina> getMaquina() {
        return maquinas;
    }
    public Maquina buscarPorCodigo(String codigo) {
        for (Maquina m : maquinas) {
            if (m.getCodigo().equals(codigo)) return m;
        }
        return null;                        // no está
    }
    public int cantidad() {
        return maquinas.size();
    }
    public int contarOperativas(){
        int cont = 0;
        for(Maquina m : maquinas){
            if (m.getEstado().equals("operativa ")){
                cont++;
            }
        }
        return cont;
    }

}