package modelo;
public class Aula {
    private int id;
    private String tipo; // salon, laboratorio, movil
    private boolean disponible = true;

    public Aula(int id, String tipo) {
        this.id = id;
        this.tipo = tipo;
    }

    public boolean isDisponible() { return disponible; }

    public void asignar() { this.disponible = false; }

    public String getTipo() { return tipo; }

    public int getId() { return id; }

    public void setId(int id) {
        this.id = id;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}
