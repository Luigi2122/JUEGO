package Tarea.Modelo;

public class Maquina {
    private String codigo;
    private String nombre;
    private String estado;

    public Maquina(String codigo, String nombre, String estado){
        this.codigo = codigo;
        this.nombre = nombre;
        this.estado = estado;
    }
    public String getCodigo(){
        return codigo;
    }
    public String getNombre(){
     return nombre;
    }
    public String getEstado(){
        return estado;
    }
    @Override
    public String toString(){
        return codigo + " - " + nombre + " - " + " [" + estado + "]";
    }
}
