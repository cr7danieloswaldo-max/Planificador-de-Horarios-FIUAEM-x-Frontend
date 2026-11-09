package mx.uaemex.planificador;

/** Bloque ya posicionado en la celda de su hora de inicio (lo usa resultados.jsp). */
public class Bloque {
    private final Grupo grupo;
    private final int top, alto; // píxeles dentro de la celda

    public Bloque(Grupo grupo, int top, int alto) { this.grupo = grupo; this.top = top; this.alto = alto; }
    public Grupo getGrupo() { return grupo; }
    public int getTop() { return top; }
    public int getAlto() { return alto; }
}
