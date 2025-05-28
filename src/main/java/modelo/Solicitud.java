package modelo;

import java.util.UUID;

public class Solicitud {

    private final String id;
    private final String programa;
    private final String facultad;
    private final int semestre;
    private final int salones;
    private final int laboratorios;

    public Solicitud(String programa, String facultad, int semestre, int salones, int laboratorios) {
        this.id = UUID.randomUUID().toString();
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
    public String getId() { return id; }
}
