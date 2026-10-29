# Etapa 3 — Deuda técnica y code smells

**Sistema:** Gestión de Solicitudes Académicas (Java, Hibernate 6, H2, Gradle)
**Versión analizada:** tag `estado-inicial` (líneas según ese tag)

El sistema funciona bien para el usuario (calidad externa), pero su estructura interna dificulta leerlo, probarlo y modificarlo (calidad interna). Se identificaron 8 code smells

| # | Smell | Evidencia | Ubicación | Impacto | Riesgo de mantenimiento | Prioridad |
|---|---|---|---|---|---|---|

| 1 | Long Method / God Class | `main()` de ~130 líneas con las 5 operaciones en un `if / else if` | `Main.java` 17–144 | No se puede entender ni probar una operación aislada | Alto: cada operación nueva obliga a editar `main()` | Alta |

| 2 | Código duplicado | `System.out.print(...)` + `in.nextLine().trim()` repetido en cada lectura; bloque `begin / find / null-check / commit` repetido | `Main.java` 48–55, 69–72, 91–94, 107–110 | Un cambio de criterio obliga a tocar muchos lugares | Medio: es fácil olvidar alguno | Media |

| 3 | Feature Envy | `l.get(i).id + " - " + l.get(i).nombre` y `formatear()` leen campos de otras clases | `Main.java` 46, 89, 159–168 | La presentación de las entidades vive fuera de ellas | Medio: `Main` queda acoplado a su estructura interna | Media |

| 4 | Data Class / campos públicos | `public Long id; public String estado;` sin getters ni comportamiento | `Solicitud.java` 10–21, `Estudiante.java` 10–11, `Responsable.java` 10–12 | Cualquier clase puede dejar un estado inválido sin control | Medio: no hay punto único de validación | Media |

| 5 | Primitive Obsession / literales mágicos | `estado` es `String` y `"PENDIENTE"`, `"EN_PROCESO"`, `"RESUELTA"`, `"RECHAZADA"` se escriben a mano | `Solicitud.java` 21, `Main.java` 60 y 73 | Un error de tipeo no se detecta al compilar | Alto: agregar un estado exige editar la cadena de `equals` | Alta |

| 6 | Long Parameter List | `crearSolicitud(estudiante, tipo, prioridad, descripcion, estado, responsable)`; en la llamada, 2 valores son constantes | `Main.java` 147–149 y 60 | Llamada difícil de leer; fácil intercambiar `String` consecutivos | Bajo: hay un único punto de uso | Baja |

| 7 | Nombres poco expresivos | `x`, `a`, `b`, `c`, `d`, `l`, `l1`, `l2`, `q`, `rs`, `t`, `r`, `n`; `a` es ID de estudiante en la rama 1 y estado en la 4 | `Main.java` 40, 49–55, 112–116, 124–139; `Estudiante.java` 15; `Responsable.java` 16 | Hay que leer todo el contexto para saber qué es cada variable | Medio: más probabilidad de errores al modificar | Media |

| 8 | Consulta JPQL por concatenación | `q + " and s.estado = '" + a + "'"` con texto del usuario | `Main.java` 112–116 | Entradas con comillas (`' OR '1'='1`) alteran o rompen la consulta (inyección) | Alto: debilidad de seguridad que se arrastra a cualquier filtro nuevo | Alta |

**Criterio de prioridad:** Alta si afecta seguridad o corrección, o bloquea otras mejoras; Media si encarece el mantenimiento diario; Baja si es una molestia local
**Orden de corrección sugerido:** 8 → 1 → 5 → 2 → 3 → 4 → 7 → 6

## Otras debilidades (para la etapa 4)

- `emf`, `em` e `in` son `public static` y compartidos; el `EntityManager` nunca se cierra (`Main.java` 13–15)
- Manejo de errores inconsistente: la rama 1 hace `rollback` (65) y las ramas 2 y 3 no (82–84, 102–104)
- El `catch` imprime solo `"Error"` (141–143)
- Condicionales anidados en la rama 2 (73–85)
