# Planificador de Horarios — versión de PRUEBA (catálogo desde CSV)

Importar como proyecto Maven aparte (File > Open Project). No modifica el proyecto original.

## Qué cambió respecto a la versión anterior
- `Catalogo.java` ya no tiene datos escritos en código: lee `src/main/resources/datos/`
  - `materias.csv` (clave, nombre, semestre, etiqueta corta)
  - `seriaciones.csv` (clave, prerrequisito)
  - `grupos.csv` (una fila por sesión semanal: clave, gpo, profesor, día 0-5, ini, fin, aula)
- Las materias se identifican por **clave** (antes por nombre). Cálculo II usa `LINC04` solo como clave de PRUEBA.
- `Grupo` ahora tiene una lista de `Sesion` (día, inicio, fin en minutos): admite horarios distintos por día y
  duraciones reales (1.5 h, 2.5 h, 3 h...). Se agregan `Sesion.java` y `Bloque.java`.
- `Motor`: busca primero por las materias con menos grupos y tiene un tope de seguridad de nodos.
- `resultados.jsp`/`PlanificadorServlet`: el calendario coloca cada bloque por minuto y ajusta el rango de horas.
- `avance.jsp`/`prefs.jsp`: los formularios envían claves; se quitó un `</label>` sobrante.
- Se eliminó el código comentado de «materia central».

## Datos
Fuentes: ICO 2026A (Excel), Propedéuticas 2025B (PDF) e Inglés 2026B (PDF, solo grupos ICO).
`datos/_advertencias.txt` lista los grupos excluidos por horarios dudosos en las plantillas.
Si una clave o una hora está mal escrita, la aplicación falla al arrancar indicando archivo y línea.
