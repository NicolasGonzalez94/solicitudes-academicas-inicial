package com.academico;

import jakarta.persistence.*;

import java.sql.SQLException;
import java.util.List;
import org.h2.tools.Server;

import java.util.Scanner;

public class Main {

    public static EntityManagerFactory emf = Persistence.createEntityManagerFactory("solicitudesPU");
    public static Scanner in = new Scanner(System.in);

    public static void main(String[] args) throws SQLException, InterruptedException {

        Server server = Server.createWebServer("-web", "-webPort", "8080").start();
        System.out.println("H2 Console: " + server.getURL());

        // datos de prueba
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(new Estudiante("Ana Pérez"));
            em.persist(new Estudiante("Juan Gómez"));
            em.persist(new Responsable("Laura Díaz", "Alumnado"));
            em.persist(new Responsable("Carlos Ruiz", "Secretaría"));
            em.getTransaction().commit();
        } finally {
            cerrar(em);
        }

        int opcion;
        do {
            System.out.println("\n--- SOLICITUDES ACADÉMICAS ---");
            System.out.println("1. Registrar solicitud");
            System.out.println("2. Cambiar estado");
            System.out.println("3. Asignar responsable");
            System.out.println("4. Buscar / filtrar solicitudes");
            System.out.println("5. Reporte");
            System.out.println("0. Salir");
            System.out.print("Opcion: ");
            opcion = Integer.parseInt(in.nextLine().trim());

            try {
                switch (opcion) {
                    case 1 -> registrarSolicitud();
                    case 2 -> cambiarEstado();
                    case 3 -> asignarResponsable();
                    case 4 -> filtrar();
                    case 5 -> reporte();
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        } while (opcion != 0);

        emf.close();
    }

    // Cierra el EntityManager y deshace la transacción si quedó abierta (también ante excepciones)
    static void cerrar(EntityManager em) {
        if (em.getTransaction().isActive()) em.getTransaction().rollback();
        em.close();
    }

    static void registrarSolicitud() {
        EntityManager em = emf.createEntityManager();
        try {
            List<Estudiante> estudiantes = em.createQuery("from Estudiante", Estudiante.class).getResultList();
            for (Estudiante e : estudiantes) {
                System.out.println(e.id + " - " + e.nombre);
            }
            System.out.print("ID estudiante: ");
            Long idEstudiante = Long.parseLong(in.nextLine().trim());
            System.out.print("Tipo (CERTIFICADO/EQUIVALENCIA/BAJA_MATERIA/OTRO): ");
            String tipo = in.nextLine().trim().toUpperCase();
            System.out.print("Prioridad (BAJA/MEDIA/ALTA): ");
            String prioridad = in.nextLine().trim().toUpperCase();
            System.out.print("Descripción: ");
            String descripcion = in.nextLine().trim();

            em.getTransaction().begin();
            Estudiante estudiante = em.find(Estudiante.class, idEstudiante);
            if (estudiante == null) {
                throw new IllegalArgumentException("No existe ese estudiante");
            }
            Solicitud solicitud = crearSolicitud(estudiante, tipo, prioridad, descripcion, "PENDIENTE", null);
            em.persist(solicitud);
            em.getTransaction().commit();
            System.out.println("Solicitud registrada: " + formatear(solicitud));
        } finally {
            cerrar(em);
        }
    }

    static void cambiarEstado() {
        System.out.print("ID solicitud: ");
        Long idSolicitud = Long.parseLong(in.nextLine().trim());
        System.out.print("Nuevo estado (PENDIENTE/EN_PROCESO/RESUELTA/RECHAZADA): ");
        String nuevoEstado = in.nextLine().trim().toUpperCase();
        boolean estadoValido = nuevoEstado.equals("PENDIENTE") || nuevoEstado.equals("EN_PROCESO")
                || nuevoEstado.equals("RESUELTA") || nuevoEstado.equals("RECHAZADA");
        if (!estadoValido) {
            throw new IllegalArgumentException("Estado inválido: " + nuevoEstado);
        }

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Solicitud solicitud = em.find(Solicitud.class, idSolicitud);
            if (solicitud == null) {
                throw new IllegalArgumentException("No existe esa solicitud");
            }
            solicitud.estado = nuevoEstado;
            em.getTransaction().commit();
            System.out.println("Estado actualizado.");
        } finally {
            cerrar(em);
        }
    }

    static void asignarResponsable() {
        EntityManager em = emf.createEntityManager();
        try {
            List<Responsable> responsables = em.createQuery("from Responsable", Responsable.class).getResultList();
            for (Responsable r : responsables) {
                System.out.println(r.id + " - " + r.nombre + " (" + r.area + ")");
            }
            System.out.print("ID solicitud: ");
            Long idSolicitud = Long.parseLong(in.nextLine().trim());
            System.out.print("ID responsable: ");
            Long idResponsable = Long.parseLong(in.nextLine().trim());

            em.getTransaction().begin();
            Solicitud solicitud = em.find(Solicitud.class, idSolicitud);
            Responsable responsable = em.find(Responsable.class, idResponsable);
            if (solicitud == null || responsable == null) {
                throw new IllegalArgumentException("Solicitud o responsable inexistente");
            }
            solicitud.responsable = responsable;
            em.getTransaction().commit();
            System.out.println("Responsable asignado.");
        } finally {
            cerrar(em);
        }
    }

    static void filtrar() {
        System.out.print("Estado (Enter = todos): ");
        String estado = in.nextLine().trim().toUpperCase();
        System.out.print("Tipo (Enter = todos): ");
        String tipo = in.nextLine().trim().toUpperCase();

        String jpql = "from Solicitud s where 1=1";
        if (!estado.isEmpty()) jpql = jpql + " and s.estado = '" + estado + "'";
        if (!tipo.isEmpty()) jpql = jpql + " and s.tipo = '" + tipo + "'";

        EntityManager em = emf.createEntityManager();
        try {
            List<Solicitud> solicitudes = em.createQuery(jpql, Solicitud.class).getResultList();
            if (solicitudes.isEmpty()) System.out.println("Sin resultados.");
            for (Solicitud solicitud : solicitudes) {
                System.out.println(formatear(solicitud));
            }
        } finally {
            cerrar(em);
        }
    }

    static void reporte() {
        EntityManager em = emf.createEntityManager();
        try {
            System.out.println("--- REPORTE ---");
            System.out.println("Total: " + em.createQuery("select count(s) from Solicitud s", Long.class).getSingleResult());
            System.out.println("Sin responsable: " + em.createQuery(
                    "select count(s) from Solicitud s where s.responsable is null", Long.class).getSingleResult());

            System.out.println("Por estado:");
            List<Object[]> porEstado = em.createQuery(
                    "select s.estado, count(s) from Solicitud s group by s.estado", Object[].class).getResultList();
            for (Object[] fila : porEstado) {
                System.out.println("  " + fila[0] + ": " + fila[1]);
            }

            System.out.println("Por tipo:");
            List<Object[]> porTipo = em.createQuery(
                    "select s.tipo, count(s) from Solicitud s group by s.tipo", Object[].class).getResultList();
            for (Object[] fila : porTipo) {
                System.out.println("  " + fila[0] + ": " + fila[1]);
            }
        } finally {
            cerrar(em);
        }
    }

    static Solicitud crearSolicitud(Estudiante estudiante, String tipo, String prioridad, String descripcion,
                                    String estado, Responsable responsable) {
        Solicitud s = new Solicitud();
        s.estudiante = estudiante;
        s.tipo = tipo;
        s.prioridad = prioridad;
        s.descripcion = descripcion;
        s.estado = estado;
        s.responsable = responsable;
        return s;
    }

    static String formatear(Solicitud s) {
        String r;
        if (s.responsable == null) {
            r = "sin asignar";
        } else {
            r = s.responsable.nombre;
        }
        return "#" + s.id + " | " + s.tipo + " | " + s.prioridad + " | " + s.estado
                + " | " + s.estudiante.nombre
                + " | Resp: " + r
                + " | " + s.descripcion;
    }
}
