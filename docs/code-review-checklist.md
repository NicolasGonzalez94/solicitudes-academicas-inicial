# Etapa 7 — Code review profesional

**Sistema:** Gestión de Solicitudes Académicas (Java, Hibernate 6, H2, Gradle)
**Commit revisado:** `57478c2` (tag `etapa-refactoring`, HEAD de `main`) — **Fecha:** 02/11/2026 — **Revisor:** Nicolas Gonzalez
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
| `README.md` | No cumple | OBS-09 |
| Tags de estado y etapa | Parcial | OBS-10 |
| Pruebas y evidencias | No cumple | OBS-11 |

## 3. Observaciones

| ID | Observación | Ubicación | Sev. | Acción correctiva | Estado |
|---|---|---|---|---|---|
| OBS-01 | Estado inválido muestra `No enum constant com.academico.Estado.2` | `Main.java` 103, 153 | Media | Capturar `valueOf` y mostrar los valores permitidos | Pendiente |
| OBS-02 | `tipo` y `prioridad` son `String` sin validar (se aceptó `1` y `3`) | `Solicitud.java` 18–19, `Main.java` 83–85 | Media | Enums `Tipo` y `Prioridad` o validación | Pendiente |
| OBS-03 | Menú no numérico no se captura y opción inexistente se ignora | `Main.java` 32, 35–41 | Media | `parseInt` dentro del `try` y `default` en el `switch` | Pendiente |
| OBS-04 | El proceso no termina al salir (servidor H2 sin detener) | `Main.java` 18 | Media | `server.stop()` tras `emf.close()` | Pendiente |
| OBS-05 | Secuencia `begin / find / null-check / commit` repetida | `Main.java` 87–94, 107–113, 128–135 | Baja | Método auxiliar | Pendiente |
| OBS-06 | `Main` mezcla UI, persistencia y validación; sin DIP | `Main.java` | Baja | Documentar como mejora pendiente | Pendiente |
| OBS-07 | `hbm2ddl=create` borra datos; SQL en consola; dialecto explícito | `persistence.xml` | Baja | Quitar dialecto, `show_sql=false` | Pendiente |
| OBS-08 | Tildes rotas en consola de Windows | `build.gradle` | Baja | `-Dfile.encoding=UTF-8` o `chcp 65001` en README | Pendiente |
| OBS-09 | Falta `README.md` | raíz | Alta | Crearlo | Pendiente |
| OBS-10 | Existen `estado-inicial`, `etapa-diagnostico` y `etapa-refactoring`; faltan `estado-refactorizado`, `etapa-pruebas` y `etapa-docs` | repo | Alta | Crear los tags faltantes | Pendiente |
| OBS-11 | No hay tests ni evidencias antes/después | `src/test`, `docs/` | Alta | Completar etapa 6 | Pendiente |

**Positivo:** consulta parametrizada, un `EntityManager` por operación con cierre garantizado, enum de estado, campos privados y commits claros.

## 4. Verificación de comportamiento (02/11/2026)

| Prueba | Resultado |
|---|---|
| Registrar, cambiar estado, asignar, filtrar y reporte con datos válidos | OK |
| Estado inválido | Error técnico (OBS-01) |
| ID inexistente (99) y luego operación válida | `Error: No existe esa solicitud` y la siguiente funciona (en `estado-inicial` fallaba) |
| Salir (`0`) | El menú termina, el proceso sigue (OBS-04) |

## 5. Correcciones realizadas

Ninguna por ahora, para no generar conflictos en `Main.java`. Se registrará acá el commit de cada corrección.

## 6. Conclusión

**Aceptación condicionada:** se cumplen los criterios 1 a 4; el 5 y el 6 dependen de OBS-09, 10 y 11.
