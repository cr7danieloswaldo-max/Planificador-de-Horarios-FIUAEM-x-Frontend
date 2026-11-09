package mx.uaemex.planificador;

import java.util.List;

public class Grupo {
    private final Materia materia;
    private final String numero, profesor; // el grupo puede ser "CO01", "G7", "12"...
    private final List<Sesion> sesiones;

    public Grupo(Materia materia, String numero, String profesor, List<Sesion> sesiones) {
        this.materia = materia; this.numero = numero; this.profesor = profesor; this.sesiones = sesiones;
    }
    public Materia getMateria() { return materia; }
    public String getNumero() { return numero; }
    public String getProfesor() { return profesor; }
    public List<Sesion> getSesiones() { return sesiones; }

    public boolean usaDia(int d) { return sesiones.stream().anyMatch(s -> s.getDia() == d); }

    public boolean choca(Grupo o) {
        for (Sesion a : sesiones) for (Sesion b : o.sesiones) if (a.choca(b)) return true;
        return false;
    }
}
