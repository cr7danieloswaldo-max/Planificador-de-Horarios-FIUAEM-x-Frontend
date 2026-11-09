package mx.uaemex.planificador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

/** Controlador único: GET muestra pasos, POST procesa formularios. */
@WebServlet("/planificador")
public class PlanificadorServlet extends HttpServlet {
    private static final String[] VISTAS = {"inicio", "avance", "prefs", "resultados"};
    private static final int ALTO_FILA = 32; // px por hora en el calendario (debe coincidir con .cal td en estilos.css)

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Estado e = estado(req);
        String op = req.getParameter("opcion");
        if (op != null) e.setOpcion(entero(op, 0));
        mostrar(req, resp, e, entero(req.getParameter("paso"), 0));
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        Estado e = estado(req);
        String accion = Optional.ofNullable(req.getParameter("accion")).orElse("");
        switch (accion) {
            case "semestre" -> { guardarAvance(req, e); mostrar(req, resp, e, 1); }
            case "avance"   -> { guardarAvance(req, e); mostrar(req, resp, e, 2); }
            case "generar"  -> {
                guardarPreferencias(req, e);
                if (e.getSeleccionadas().size() < 2) {
                    req.setAttribute("error", "Selecciona al menos 2 materias para generar horarios.");
                    mostrar(req, resp, e, 2);
                } else {
                    e.setResultado(Motor.generar(e)); e.setOpcion(0);
                    mostrar(req, resp, e, 3);
                }
            }
            default -> mostrar(req, resp, e, 0);
        }
    }

    // Los formularios envían CLAVES de materia (no nombres).
    private void guardarAvance(HttpServletRequest req, Estado e) {
        int sem = entero(req.getParameter("semestre"), e.getSemestre());
        if (sem != e.getSemestre()) e.getSeleccionadas().clear();
        e.setSemestre(sem);
        Set<String> apr = new HashSet<>();
        for (String c : valores(req, "apr")) {
            Materia m = Catalogo.materia(c);
            if (m != null && m.getSemestre() < sem) apr.add(c);
        }
        e.setAprobadas(apr);
        e.getSeleccionadas().removeIf(c -> !e.faltantes(Catalogo.materia(c)).isEmpty());
    }

    private void guardarPreferencias(HttpServletRequest req, Estado e) {
        Set<String> sel = new LinkedHashSet<>();
        for (String c : valores(req, "sel")) {
            Materia m = Catalogo.materia(c);
            if (m != null && e.faltantes(m).isEmpty()) sel.add(c); // respeta seriación
        }
        e.setSeleccionadas(sel);
        String t = req.getParameter("turno");
        e.setTurno("ves".equals(t) || "sin".equals(t) ? t : "mat");
        e.setViernesLibre(req.getParameter("viernes") != null);
    }

    private void mostrar(HttpServletRequest req, HttpServletResponse resp, Estado e, int paso) throws ServletException, IOException {
        if (paso < 0 || paso > 3) paso = 0;
        if (paso == 3 && e.getResultado() == null) paso = 2;
        req.setAttribute("paso", paso);
        req.setAttribute("estado", e);
        switch (paso) {
            case 1 -> req.setAttribute("anteriores", Catalogo.anteriores(e.getSemestre()));
            case 2 -> req.setAttribute("lista", Catalogo.cursables(e.getSemestre(), e.getAprobadas()));
            case 3 -> prepararResultados(req, e);
            default -> { }
        }
        req.getRequestDispatcher("/WEB-INF/vistas/" + VISTAS[paso] + ".jsp").forward(req, resp);
    }

    private void prepararResultados(HttpServletRequest req, Estado e) {
        Motor.Resultado r = e.getResultado();
        req.setAttribute("resultado", r);
        if (r.isVacio()) return;
        int op = Math.max(0, Math.min(e.getOpcion(), r.getOpciones().size() - 1));
        e.setOpcion(op);

        Map<String, List<Bloque>> celdas = new HashMap<>();   // "dia-hora" -> bloques que inician en esa hora
        Map<String, Integer> colores = new HashMap<>();        // clave de materia -> índice de color
        int horaIni = 7, horaFin = 17;                         // rango mínimo visible (como en el boceto)
        for (Grupo g : r.getOpciones().get(op)) {
            colores.computeIfAbsent(g.getMateria().getClave(), k -> colores.size());
            for (Sesion s : g.getSesiones()) {
                int hora = s.getInicio() / 60;
                int top = 1 + (s.getInicio() % 60) * ALTO_FILA / 60;
                int alto = (s.getFin() - s.getInicio()) * ALTO_FILA / 60 - 2;
                celdas.computeIfAbsent(s.getDia() + "-" + hora, k -> new ArrayList<>()).add(new Bloque(g, top, alto));
                horaIni = Math.min(horaIni, hora);
                horaFin = Math.max(horaFin, (s.getFin() - 1) / 60); // última fila que ocupa el bloque
            }
        }
        req.setAttribute("celdas", celdas);
        req.setAttribute("colores", colores);
        req.setAttribute("horaIni", horaIni);
        req.setAttribute("horaFin", horaFin);
    }

    private static Estado estado(HttpServletRequest req) {
        HttpSession s = req.getSession();
        Estado e = (Estado) s.getAttribute("estado");
        if (e == null) { e = new Estado(); s.setAttribute("estado", e); }
        return e;
    }
    private static int entero(String s, int def) { try { return Integer.parseInt(s); } catch (Exception ex) { return def; } }
    private static String[] valores(HttpServletRequest req, String n) {
        String[] v = req.getParameterValues(n); return v == null ? new String[0] : v;
    }
}
