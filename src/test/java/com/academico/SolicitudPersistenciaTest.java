package com.academico;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Pruebas de no-regresión sobre la persistencia.
 * Está escrita para compilar y correr SIN CAMBIOS tanto en la versión inicial
 * (campos públicos, estado como String) como en la refactorizada (campos privados, enum Estado).
 * Por eso no usa constructores, getters, setters ni el enum Estado de forma directa:
 * accede a los campos por reflexión y compara estados mediante un parámetro JPQL.
 */
@DisplayName("No-regresión: lógica de solicitudes (persistencia)")
class SolicitudPersistenciaTest {

    private EntityManagerFactory emf;
    private Long idEstudiante;
    private Long idResponsable;

    @BeforeEach
    void setUp() {
        // H2 en memoria, aislada en cada test, sin tocar la base real
        emf = Persistence.createEntityManagerFactory("solicitudesPU", Map.of(
                "jakarta.persistence.jdbc.url", "jdbc:h2:mem:test" + System.nanoTime() + ";DB_CLOSE_DELAY=-1",
                "hibernate.hbm2ddl.auto", "create-drop",
                "hibernate.show_sql", "false"));

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Estudiante estudiante = new Estudiante("Ana Pérez");
            Responsable responsable = new Responsable("Laura Díaz", "Alumnado");
            em.persist(estudiante);
            em.persist(responsable);
            em.getTransaction().commit();
            idEstudiante = idDe(estudiante);
            idResponsable = idDe(responsable);
        } finally {
            em.close();
        }
    }

    @AfterEach
    void tearDown() {
        emf.close();
    }

    // ---------------------------------------------------------------------
    // Helpers compatibles con las dos versiones
    // ---------------------------------------------------------------------

    private Long idDe(Object entidad) {
        return (Long) emf.getPersistenceUnitUtil().getIdentifier(entidad);
    }

    /** Asigna un campo por reflexión (sirve aunque sea público o privado). */
    private static void setCampo(Object objeto, String nombre, Object valor) throws Exception {
        Field campo = objeto.getClass().getDeclaredField(nombre);
        campo.setAccessible(true);
        campo.set(objeto, valor);
    }

    private static boolean existeEstadoEnum() {
        try {
            Class.forName("com.academico.Estado");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Devuelve el valor de estado que corresponde a cada versión:
     * el enum Estado en la refactorizada, el String en la inicial.
     * Con un nombre inválido, en la refactorizada lanza IllegalArgumentException.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object estado(String nombre) throws Exception {
        if (!existeEstadoEnum()) {
            return nombre;
        }
        return Enum.valueOf((Class) Class.forName("com.academico.Estado"), nombre);
    }

    private Long crearSolicitud(String tipo, String prioridad) throws Exception {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Estudiante estudiante = em.find(Estudiante.class, idEstudiante);

            Solicitud solicitud = new Solicitud();
            setCampo(solicitud, "estudiante", estudiante);
            setCampo(solicitud, "tipo", tipo);
            setCampo(solicitud, "prioridad", prioridad);
            setCampo(solicitud, "descripcion", "Descripción de prueba");
            // En la versión inicial el estado es String y arranca en null (lo completaba Main con "PENDIENTE").
            // En la refactorizada ya arranca en PENDIENTE por defecto, y ahí no se toca para probarlo de verdad.
            if (Solicitud.class.getDeclaredField("estado").getType() == String.class) {
                setCampo(solicitud, "estado", "PENDIENTE");
            }

            em.persist(solicitud);
            em.getTransaction().commit();
            return idDe(solicitud);
        } finally {
            em.close();
        }
    }

    private void cambiarEstado(Long idSolicitud, String nuevoEstado) throws Exception {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            setCampo(em.find(Solicitud.class, idSolicitud), "estado", estado(nuevoEstado));
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    private long contar(String jpql) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(jpql, Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }

    private long contarPorEstado(String nombreEstado) throws Exception {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("select count(s) from Solicitud s where s.estado = :estado", Long.class)
                    .setParameter("estado", estado(nombreEstado))
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    // ---------------------------------------------------------------------
    // Casos de prueba
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("CP-01: Registrar solicitud válida queda en estado PENDIENTE")
    void registrarSolicitud() throws Exception {
        crearSolicitud("CERTIFICADO", "ALTA");

        assertEquals(1, contar("select count(s) from Solicitud s"));
        assertEquals(1, contarPorEstado("PENDIENTE"));
    }

    @Test
    @DisplayName("CP-02: Cambiar estado de una solicitud")
    void cambiarEstado() throws Exception {
        Long id = crearSolicitud("CERTIFICADO", "ALTA");

        cambiarEstado(id, "EN_PROCESO");

        assertEquals(1, contarPorEstado("EN_PROCESO"));
        assertEquals(0, contarPorEstado("PENDIENTE"));
    }

    @Test
    @DisplayName("CP-03: Asignar responsable a una solicitud")
    void asignarResponsable() throws Exception {
        Long id = crearSolicitud("EQUIVALENCIA", "MEDIA");
        assertEquals(1, contar("select count(s) from Solicitud s where s.responsable is null"));

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Solicitud solicitud = em.find(Solicitud.class, id);
            setCampo(solicitud, "responsable", em.find(Responsable.class, idResponsable));
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        assertEquals(0, contar("select count(s) from Solicitud s where s.responsable is null"));
    }

    @Test
    @DisplayName("CP-04: Filtrar por estado y tipo devuelve solo las coincidentes")
    void filtrarPorEstadoYTipo() throws Exception {
        crearSolicitud("CERTIFICADO", "ALTA");
        crearSolicitud("EQUIVALENCIA", "BAJA");
        Long idTercera = crearSolicitud("CERTIFICADO", "MEDIA");

        cambiarEstado(idTercera, "RESUELTA");

        EntityManager em = emf.createEntityManager();
        try {
            List<Solicitud> resultado = em.createQuery(
                            "from Solicitud s where 1=1 and s.estado = :estado and s.tipo = :tipo", Solicitud.class)
                    .setParameter("estado", estado("RESUELTA"))
                    .setParameter("tipo", "CERTIFICADO")
                    .getResultList();

            assertEquals(1, resultado.size());
        } finally {
            em.close();
        }
    }

    @Test
    @DisplayName("CP-05: El reporte agrupa correctamente por estado")
    void reportePorEstado() throws Exception {
        crearSolicitud("CERTIFICADO", "ALTA");
        crearSolicitud("OTRO", "BAJA");
        Long id = crearSolicitud("OTRO", "BAJA");

        cambiarEstado(id, "RECHAZADA");

        EntityManager em = emf.createEntityManager();
        try {
            Map<Object, Long> porEstado = new HashMap<>();
            for (Object[] fila : em.createQuery(
                    "select s.estado, count(s) from Solicitud s group by s.estado", Object[].class).getResultList()) {
                porEstado.put(fila[0], (Long) fila[1]);
            }

            assertEquals(2L, porEstado.get(estado("PENDIENTE")));
            assertEquals(1L, porEstado.get(estado("RECHAZADA")));
        } finally {
            em.close();
        }
    }

    @Test
    @DisplayName("CP-06: Un estado inexistente es rechazado con IllegalArgumentException")
    void estadoInvalido() {
        // Solo aplica a la versión refactorizada: en la inicial no existe el enum Estado
        // (esa validación vivía en Main y queda cubierta por MainComportamientoTest).
        assumeTrue(existeEstadoEnum(), "La versión inicial no tiene enum Estado: se omite");

        assertThrows(IllegalArgumentException.class, () -> estado("INEXISTENTE"));
    }
}
