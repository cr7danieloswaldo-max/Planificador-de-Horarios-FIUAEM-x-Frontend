package mx.uaemex.planificador;

import java.io.Serializable;
import java.util.*;

/** Estado del asistente; vive en la sesión HTTP (sin login ni número de cuenta). Todo se guarda por CLAVE. */
public class Estado implements Serializable {
    private int semestre = 2;
    private Set<String> aprobadas = new HashSet<>();
    private Set<String> seleccionadas = new LinkedHashSet<>();
    private String turno = "mat"; // mat | ves | sin
    private boolean viernesLibre;
    private transient Motor.Resultado resultado;
    private int opcion;

    public Estado() {
        for (Materia m : Catalogo.anteriores(semestre)) aprobadas.add(m.getClave()); // ejemplo: todo el 1.er semestre aprobado
    }

    public int getSemestre() { return semestre; }
    public void setSemestre(int s) { this.semestre = s; }
    public Set<String> getAprobadas() { return aprobadas; }
    public void setAprobadas(Set<String> a) { this.aprobadas = a; }
    public Set<String> getSeleccionadas() { return seleccionadas; }
    public void setSeleccionadas(Set<String> s) { this.seleccionadas = s; }
    public String getTurno() { return turno; }
    public void setTurno(String t) { this.turno = t; }
    public boolean isViernesLibre() { return viernesLibre; }
    public void setViernesLibre(boolean v) { this.viernesLibre = v; }
    public Motor.Resultado getResultado() { return resultado; }
    public void setResultado(Motor.Resultado r) { this.resultado = r; }
    public int getOpcion() { return opcion; }
    public void setOpcion(int o) { this.opcion = o; }

    /** NOMBRES de los prerrequisitos que aún no se cumplen (invocable desde EL: ${estado.faltantes(m)}). */
    public List<String> faltantes(Materia m) {
        return m.getSeriacion().stream().filter(c -> !aprobadas.contains(c))
                .map(c -> Catalogo.materia(c).getNombre()).toList();
    }
}
