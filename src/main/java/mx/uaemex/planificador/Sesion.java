package mx.uaemex.planificador;

/** Una sesión semanal de un grupo. Horas en minutos desde las 00:00 (07:30 = 450). */
public class Sesion {
    private final int dia, inicio, fin; // dia: 0 = Lun ... 5 = Sáb

    public Sesion(int dia, int inicio, int fin) { this.dia = dia; this.inicio = inicio; this.fin = fin; }
    public int getDia() { return dia; }
    public int getInicio() { return inicio; }
    public int getFin() { return fin; }

    public boolean choca(Sesion o) { return dia == o.dia && inicio < o.fin && o.inicio < fin; }
}
