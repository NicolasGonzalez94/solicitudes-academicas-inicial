# Etapa 7 — Code review profesional

**Sistema:** Gestión de Solicitudes Académicas (Java, Hibernate 6, H2, Gradle)
**Commit revisado:** `746ef20` (HEAD de `main`; el código fuente es el mismo de `57478c2`, tag `etapa-refactoring`) — **Fecha:** 02/11/2026 — **Revisor:** Nicolas Gonzalez
**Método:** lectura del código, ejecución con `gradlew run` y revisión del repositorio.

## 1. Criterios de aceptación

1. Compila y corre con `gradlew run`.
2. Las operaciones válidas dan el mismo resultado que en `estado-inicial`.
3. Los errores no dejan transacciones abiertas ni recursos sin cerrar.
4. No hay consultas armadas por concatenación.
5. No quedan observaciones de severidad Alta abiertas.
6. El repo tiene README, tags requeridos y evidencias de pruebas.

## 2. Checklist aplicado

| Ítem | Resultado | Ref. |
|---|---|---|
| Nombres descriptivos y métodos cortos | Cumple | |
| Sin duplicación | Parcial | OBS-05 |
| Entidades encapsuladas | Cumple | |
| Sin tipos primitivos para dominios (enums) | Parcial | OBS-02 |
| Responsabilidades separadas por capas | No cumple | OBS-06 |
| Mensajes de error claros | Parcial | OBS-01, 03 |
| Rollback y cierre del `EntityManager` en todos los caminos | Cumple | |
| Recursos liberados al salir | Parcial | OBS-04 |
| Consultas parametrizadas | Cumple | |
| Mismo comportamiento externo que `estado-inicial` | Cumple | sección 4 |
| `.gitignore` e historial de commits claros | Cumple | |
| `README.md` | Pendiente (en curso) | OBS-09 |
| Tags de estado y etapa | Cumple | OBS-10 |
| Pruebas y evidencias | Parcial | OBS-11 a 15 |

## 3. Observaciones

| ID | Observación | Ubicación | Sev. | Acción correctiva | Estado |
|---|---|---|---|---|---|
| OBS-01 | Estado inválido muestra `No enum constant com.academico.Estado.2` | `Main.java` 103, 153 | Media | Capturar `valueOf` y mostrar los valores permitidos | Mejora pendiente (etapa 8) |
| OBS-02 | `tipo` y `prioridad` son `String` sin validar (se aceptó `1` y `3`) | `Solicitud.java` 18–19, `Main.java` 83–85 | Media | Enums `Tipo` y `Prioridad` o validación | Mejora pendiente (etapa 8) |
| OBS-03 | Menú no numérico no se captura y opción inexistente se ignora | `Main.java` 32, 35–41 | Media | `parseInt` dentro del `try` y `default` en el `switch` | Mejora pendiente (etapa 8) |
| OBS-04 | El proceso no termina al salir (servidor H2 sin detener) | `Main.java` 18 | Media | `server.stop()` tras `emf.close()` | Mejora pendiente (etapa 8) |
| OBS-05 | Secuencia `begin / find / null-check / commit` repetida | `Main.java` 87–94, 107–113, 128–135 | Baja | Método auxiliar | Mejora pendiente (etapa 8) |
| OBS-06 | `Main` mezcla UI, persistencia y validación; sin DIP | `Main.java` | Baja | Documentar como mejora pendiente | Mejora pendiente (etapa 8) |
| OBS-07 | `hbm2ddl=create` borra datos; SQL en consola; dialecto explícito | `persistence.xml` | Baja | Quitar dialecto, `show_sql=false` | Mejora pendiente (etapa 8) |
| OBS-08 | Tildes rotas en consola de Windows | `build.gradle` | Baja | `-Dfile.encoding=UTF-8` o `chcp 65001` en README | Mejora pendiente (etapa 8) |
| OBS-09 | Falta `README.md` | raíz | Alta | Crearlo | En curso (otro integrante) |
| OBS-10 | Faltaban los tags `estado-refactorizado`, `etapa-pruebas` y `etapa-docs` | repo | Alta | Crear los tags faltantes | Resuelto (tags creados por otro integrante) |
| OBS-11 | No había tests ni evidencias | `src/test`, `docs/` | Alta | Completar etapa 6 | Resuelto (parcial, ver OBS-12 a 15) |
| OBS-12 | Los tests no cubren los casos que distinguen antes/después: ID inexistente seguido de otra operación (transacción abierta en `estado-inicial`), filtro con comillas (inyección) y estado inválido a través de `Main`. CP-06 prueba `Enum.valueOf` desde el propio test | `SolicitudPersistenciaTest` (CP-06) | Media | Agregar casos para esos tres escenarios | Mejora pendiente (etapa 8) |
| OBS-13 | Salvo CP-07, los tests usan helpers y JPQL propios (por reflexión), no los métodos de `Main`: verifican el mapeo y las consultas, no la lógica de la aplicación | `SolicitudPersistenciaTest` | Media | Documentarlo como limitación o exponer métodos de `Main` probables | Mejora pendiente (etapa 8) |
| OBS-14 | CP-07 ejecuta `Main.main` con la base de archivo real (`./data/db`, `create`) y el puerto 8080: correr los tests borra los datos locales y falla si la app está abierta | `MainComportamientoTest` | Baja | Usar H2 en memoria y no levantar el servidor web en los tests | Mejora pendiente (etapa 8) |
| OBS-15 | Hay evidencia solo de la versión refactorizada y no hay un documento de casos esperados vs obtenidos, ni la misma ejecución sobre `estado-inicial` | `docs/` | Media | Correr los tests y el flujo en `estado-inicial` y documentar la comparación en `docs/pruebas.md` | Mejora pendiente (etapa 8) |

**Positivo:** consulta parametrizada, un `EntityManager` por operación con cierre garantizado, enum de estado, campos privados, commits claros y tests de no-regresión escritos para correr sobre ambas versiones (7 casos, CP-01 a CP-07) con H2 en memoria.

## 4. Verificación de comportamiento (02/11/2026)

| Prueba | Resultado |
|---|---|
| Registrar, cambiar estado, asignar, filtrar y reporte con datos válidos | OK |
| Estado inválido | Error técnico (OBS-01) |
| ID inexistente (99) y luego operación válida | `Error: No existe esa solicitud` y la siguiente funciona (en `estado-inicial` fallaba) |
| Salir (`0`) | El menú termina, el proceso sigue (OBS-04) |
| `gradlew test` (etapa 6) | `BUILD SUCCESSFUL`, 7 casos definidos (CP-01 a CP-07) |

## 5. Correcciones realizadas

No se modificó el código para evitar conflictos con las demás etapas. Las observaciones de severidad Media y Baja quedan registradas como mejoras pendientes (etapa 8).

## 6. Conclusión

**Aceptado con observaciones.** Se cumplen los criterios 1 a 5. El criterio 6 queda sujeto a que se incorpore el `README.md` (OBS-09); las limitaciones de las pruebas (OBS-12 a 15) se documentan como mejoras pendientes.
