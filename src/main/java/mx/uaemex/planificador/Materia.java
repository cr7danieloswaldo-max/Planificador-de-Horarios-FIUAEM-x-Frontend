package mx.uaemex.planificador;

import java.util.List;

/** Unidad de aprendizaje. La identidad es la CLAVE (única en materias.csv), no el nombre. */
public class Materia {
    private final String clave, nombre, etiqueta;
    private final int semestre;
    private final List<String> seriacion; // claves de los prerrequisitos

    public Materia(String clave, String nombre, int semestre, String etiqueta, List<String> seriacion) {
        this.clave = clave; this.nombre = nombre; this.semestre = semestre;
        this.etiqueta = etiqueta == null || etiqueta.isBlank() ? null : etiqueta;
        this.seriacion = seriacion;
    }
    public String getClave() { return clave; }
    public String getNombre() { return nombre; }
    public int getSemestre() { return semestre; }
    public List<String> getSeriacion() { return seriacion; }
    /** Texto corto para el bloque del calendario (si no hay, el nombre completo). */
    public String getEtiqueta() { return etiqueta != null ? etiqueta : nombre; }
}
