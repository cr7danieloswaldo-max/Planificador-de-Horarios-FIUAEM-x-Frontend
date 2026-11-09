# Planificador de Horarios — Prototipo 1 (Catálogo desde CSV)

Proyecto desarrollado por el equipo **Informaniacos** para la Ingeniería en Computación (UAEMéx).
Importar en el IDE como proyecto Maven (File > Open Project).

## Arquitectura y Características del Prototipo

* **Carga de Datos:** El archivo `Catalogo.java` lee la información dinámicamente desde la ruta `src/main/resources/datos/`:
  * `materias.csv` (clave, nombre, semestre, etiqueta corta)
  * `seriaciones.csv` (clave, prerrequisito)
  * `grupos.csv` (una fila por sesión semanal: clave, gpo, profesor, día 0-5, ini, fin, aula)
* **Gestión de Horarios:** La clase `Grupo` contiene una lista de `Sesion` (día, inicio, fin en minutos). Esto admite horarios distintos por día y duraciones reales (1.5 h, 2.5 h, 3 h). Se complementa con las clases `Sesion.java` y `Bloque.java`.
* **Identificación:** Todas las materias se procesan e identifican internamente por su **clave** única. (Nota: Cálculo II usa `LINC04` como clave de prueba).
* **Motor de Generación:** El `Motor` busca primero por las materias con menos grupos disponibles y cuenta con un tope de seguridad de nodos para optimizar el rendimiento.
* **Interfaz (JSP):** 
  * `avance.jsp` / `prefs.jsp`: Los formularios de selección envían las claves al servidor.
  * `resultados.jsp` / `PlanificadorServlet`: El calendario renderiza cada bloque por minuto y ajusta el rango de horas automáticamente según los grupos generados.

## Fuentes de Datos

* **Plantillas:** ICO 2026A (Excel), Propedéuticas 2025B (PDF) e Inglés 2026B (PDF, solo grupos ICO).
* **Validación:** El archivo `datos/_advertencias.txt` lista los grupos excluidos por horarios dudosos en las plantillas originales. 
* *Nota:* Si una clave o una hora está mal escrita en los CSV, la aplicación fallará al arrancar indicando el archivo y la línea exacta del error.