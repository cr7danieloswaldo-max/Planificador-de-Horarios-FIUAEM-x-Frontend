package mx.uaemex.planificador;

import java.util.*;

public final class Motor {
    private static final int MAX_OPCIONES = 4;
    private static final int MAX_NODOS = 2_000_000; // tope de seguridad para la búsqueda
    private static final int CORTE_TURNO = 14 * 60; // inicio < 14:00 = matutino
    private Motor() {}

    public static class Resultado {
        private final List<List<Grupo>> opciones;
        private final String motivo;
        Resultado(List<List<Grupo>> o, String m) { opciones = o; motivo = m; }
        public List<List<Grupo>> getOpciones() { return opciones; }
        public String getMotivo() { return motivo; }
        public boolean isVacio() { return opciones.isEmpty(); }
    }

    private static boolean permitido(Grupo g, Estado e) {
        if (e.isViernesLibre() && g.usaDia(4)) return false;
        if ("mat".equals(e.getTurno()) && g.getSesiones().stream().anyMatch(s -> s.getInicio() >= CORTE_TURNO)) return false;
        if ("ves".equals(e.getTurno()) && g.getSesiones().stream().anyMatch(s -> s.getInicio() < CORTE_TURNO)) return false;
        return true;
    }

    public static Resultado generar(Estado e) {
        List<String> orden = new ArrayList<>(e.getSeleccionadas()); // claves, en el orden elegido
        Map<String, List<Grupo>> cand = new LinkedHashMap<>();
        for (String c : orden) {
            List<Grupo> l = Catalogo.grupos(c).stream().filter(g -> permitido(g, e)).toList();
            if (l.isEmpty()) return new Resultado(List.of(), "«" + nombre(c) + "» no tiene grupos con tus preferencias de horario.");
            cand.put(c, l);
        }
        // Se busca primero por las materias con menos grupos (poda más rápido)
        List<String> mats = new ArrayList<>(orden);
        mats.sort(Comparator.comparingInt(c -> cand.get(c).size()));
        List<List<Grupo>> out = new ArrayList<>();
        int[] nodos = {0};
        buscar(0, mats, cand, new ArrayList<>(), out, nodos);
        for (List<Grupo> o : out) o.sort(Comparator.comparingInt(g -> orden.indexOf(g.getMateria().getClave())));
        if (!out.isEmpty()) return new Resultado(out, null);
        if (nodos[0] >= MAX_NODOS)
            return new Resultado(List.of(), "La combinación es demasiado grande para revisarla completa. Prueba con menos materias o con otro turno.");

        String turno = "mat".equals(e.getTurno()) ? " del turno matutino" : "ves".equals(e.getTurno()) ? " del turno vespertino" : "";
        for (int i = 0; i < orden.size(); i++)
            for (int j = i + 1; j < orden.size(); j++) {
                boolean posible = false;
                for (Grupo a : cand.get(orden.get(i))) for (Grupo b : cand.get(orden.get(j))) if (!a.choca(b)) posible = true;
                if (!posible) return new Resultado(List.of(), nombre(orden.get(i)) + " y " + nombre(orden.get(j))
                    + " coinciden en todos los grupos" + turno + ". El sistema no fuerza combinaciones con cruces.");
            }
        return new Resultado(List.of(), "Las materias elegidas no pueden combinarse sin cruces.");
    }

    private static String nombre(String clave) { return Catalogo.materia(clave).getNombre(); }

    private static void buscar(int i, List<String> mats, Map<String, List<Grupo>> cand, List<Grupo> cur,
                               List<List<Grupo>> out, int[] nodos) {
        if (out.size() >= MAX_OPCIONES || nodos[0] >= MAX_NODOS) return;
        if (i == mats.size()) { out.add(new ArrayList<>(cur)); return; }
        for (Grupo g : cand.get(mats.get(i))) {
            nodos[0]++;
            if (cur.stream().noneMatch(c -> c.choca(g))) {
                cur.add(g); buscar(i + 1, mats, cand, cur, out, nodos); cur.remove(cur.size() - 1);
            }
        }
    }
}
