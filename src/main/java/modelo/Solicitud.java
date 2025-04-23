package modelo;

/**
 * Clase de datos que representa una solicitud de aulas.
 */
public class Solicitud {
    private String programa;
    private String facultad;
    private int semestre;
    private int salones;
    private int laboratorios;

    public Solicitud(String programa, String facultad, int semestre, int salones, int laboratorios) {
        this.programa = programa;
        this.facultad = facultad;
        this.semestre = semestre;
        this.salones = salones;
        this.laboratorios = laboratorios;
    }

    public String getPrograma() { return programa; }
    public String getFacultad() { return facultad; }
    public int getSemestre() { return semestre; }
    public int getSalones() { return salones; }
    public int getLaboratorios() { return laboratorios; }
}
