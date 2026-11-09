package mx.uaemex.planificador;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Catálogo de materias, seriaciones y grupos. Lee tres CSV de src/main/resources/datos/:
 *   materias.csv     clave,nombre,semestre,etiqueta
 *   seriaciones.csv  clave,prerrequisito
 *   grupos.csv       clave,gpo,profesor,dia,ini,fin,aula   (una fila por sesión semanal)
 * Para cambiar la oferta basta con reemplazar esos archivos; no hay que tocar código.
 * Si hay una clave que no existe o una hora mal escrita, falla al arrancar indicando archivo y línea.
 */
public final class Catalogo {
    private static final Map<String, Materia> MATERIAS = new LinkedHashMap<>();
    private static final Map<String, List<Grupo>> GRUPOS = new HashMap<>();

    static {
        try {
            Map<String, List<String>> prerreq = new HashMap<>();
            for (String[] f : leer("seriaciones.csv", 2)) prerreq.computeIfAbsent(f[0], k -> new ArrayList<>()).add(f[1]);

            for (String[] f : leer("materias.csv", 4)) {
                if (MATERIAS.containsKey(f[0])) throw new IllegalStateException("materias.csv: clave repetida " + f[0]);
                MATERIAS.put(f[0], new Materia(f[0], f[1], Integer.parseInt(f[2]), f[3],
                        List.copyOf(prerreq.getOrDefault(f[0], List.of()))));
            }
            for (var e : prerreq.entrySet()) {
                if (!MATERIAS.containsKey(e.getKey())) throw new IllegalStateException("seriaciones.csv: clave desconocida " + e.getKey());
                for (String p : e.getValue())
                    if (!MATERIAS.containsKey(p)) throw new IllegalStateException("seriaciones.csv: prerrequisito desconocido " + p);
            }

            Map<String, List<Sesion>> ses = new LinkedHashMap<>();   // "clave|gpo" -> sesiones
            Map<String, String[]> cab = new HashMap<>();             // "clave|gpo" -> {clave, gpo, profesor}
            for (String[] f : leer("grupos.csv", 7)) {
                if (!MATERIAS.containsKey(f[0])) throw new IllegalStateException("grupos.csv: clave desconocida " + f[0]);
                String k = f[0] + "|" + f[1];
                cab.putIfAbsent(k, new String[]{f[0], f[1], f[2]});
                ses.computeIfAbsent(k, x -> new ArrayList<>()).add(new Sesion(Integer.parseInt(f[3]), minutos(f[4]), minutos(f[5])));
            }
            for (var e : ses.entrySet()) {
                String[] c = cab.get(e.getKey());
                GRUPOS.computeIfAbsent(c[0], k -> new ArrayList<>())
                      .add(new Grupo(MATERIAS.get(c[0]), c[1], c[2], List.copyOf(e.getValue())));
            }
        } catch (IOException | RuntimeException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }
    private Catalogo() {}

    private static int minutos(String hhmm) {
        String[] p = hhmm.trim().split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }

    /** Lee un CSV (UTF-8) del classpath; omite vacías, comentarios (#) y el encabezado. */
    private static List<String[]> leer(String archivo, int columnas) throws IOException {
        List<String[]> filas = new ArrayList<>();
        try (InputStream in = Catalogo.class.getResourceAsStream("/datos/" + archivo)) {
            if (in == null) throw new FileNotFoundException("No se encontró /datos/" + archivo + " en el classpath");
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String linea; int n = 0; boolean encabezado = true;
            while ((linea = br.readLine()) != null) {
                n++;
                if (n == 1 && linea.startsWith("\uFEFF")) linea = linea.substring(1);
                if (linea.isBlank() || linea.startsWith("#")) continue;
                if (encabezado) { encabezado = false; continue; }
                String[] f = linea.split(",", -1);
                if (f.length != columnas)
                    throw new IllegalStateException(archivo + " línea " + n + ": se esperaban " + columnas + " columnas y hay " + f.length);
                for (int i = 0; i < f.length; i++) f[i] = f[i].trim();
                filas.add(f);
            }
        }
        return filas;
    }

    public static Materia materia(String clave) { return MATERIAS.get(clave); }
    public static List<Grupo> grupos(String clave) { return GRUPOS.getOrDefault(clave, List.of()); }

    /** Materias que pueden marcarse como aprobadas al reinscribirse a "sem". */
    public static List<Materia> anteriores(int sem) {
        return MATERIAS.values().stream().filter(x -> x.getSemestre() < sem).toList();
    }
    /** Materias que se muestran como cursables: semestre actual y siguiente, no aprobadas. */
    public static List<Materia> cursables(int sem, Set<String> aprobadas) {
        return MATERIAS.values().stream()
            .filter(x -> (x.getSemestre() == sem || x.getSemestre() == sem + 1) && !aprobadas.contains(x.getClave()))
            .toList();
    }
}
