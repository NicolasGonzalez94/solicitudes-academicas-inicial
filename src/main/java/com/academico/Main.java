package com.academico;

import jakarta.persistence.*;

import java.sql.SQLException;
import java.util.List;
import org.h2.tools.Server;

import java.util.Scanner;

public class Main {

    public static EntityManagerFactory emf = Persistence.createEntityManagerFactory("solicitudesPU");
    public static EntityManager em = emf.createEntityManager();
    public static Scanner in = new Scanner(System.in);

    public static void main(String[] args) throws SQLException, InterruptedException {

        Server server = Server.createWebServer("-web", "-webPort", "8080").start();
        System.out.println("H2 Console: " + server.getURL());

        // datos de prueba
        em.getTransaction().begin();
        em.persist(new Estudiante("Ana Pérez"));
        em.persist(new Estudiante("Juan Gómez"));
        em.persist(new Responsable("Laura Díaz", "Alumnado"));
        em.persist(new Responsable("Carlos Ruiz", "Secretaría"));
        em.getTransaction().commit();

        int x;
        do {
            System.out.println("\n--- SOLICITUDES ACADÉMICAS ---");
            System.out.println("1. Registrar solicitud");
            System.out.println("2. Cambiar estado");
            System.out.println("3. Asignar responsable");
            System.out.println("4. Buscar / filtrar solicitudes");
            System.out.println("5. Reporte");
            System.out.println("0. Salir");
            System.out.print("Opcion: ");
            x = Integer.parseInt(in.nextLine().trim());

            try {
                if (x == 1) {
                    List<Estudiante> l = em.createQuery("from Estudiante", Estudiante.class).getResultList();
                    for (int i = 0; i < l.size(); i++) {
                        System.out.println(l.get(i).id + " - " + l.get(i).nombre);
                    }
                    System.out.print("ID estudiante: ");
                    Long a = Long.parseLong(in.nextLine().trim());
                    System.out.print("Tipo (CERTIFICADO/EQUIVALENCIA/BAJA_MATERIA/OTRO): ");
                    String b = in.nextLine().trim().toUpperCase();
                    System.out.print("Prioridad (BAJA/MEDIA/ALTA): ");
                    String c = in.nextLine().trim().toUpperCase();
                    System.out.print("Descripción: ");
                    String d = in.nextLine().trim();

                    em.getTransaction().begin();
                    Estudiante est = em.find(Estudiante.class, a);
                    if (est != null) {
                        Solicitud s = crearSolicitud(est, b, c, d, "PENDIENTE", null);
                        em.persist(s);
                        em.getTransaction().commit();
                        System.out.println("Solicitud registrada: " + formatear(s));
                    } else {
                        em.getTransaction().rollback();
                        throw new RuntimeException("Error");
                    }
                } else if (x == 2) {
                    System.out.print("ID solicitud: ");
                    Long a = Long.parseLong(in.nextLine().trim());
                    System.out.print("Nuevo estado (PENDIENTE/EN_PROCESO/RESUELTA/RECHAZADA): ");
                    String b = in.nextLine().trim().toUpperCase();
                    if (b.equals("PENDIENTE") || b.equals("EN_PROCESO") || b.equals("RESUELTA") || b.equals("RECHAZADA")) {
                        em.getTransaction().begin();
                        Solicitud s = em.find(Solicitud.class, a);
                        if (s != null) {
                            s.estado = b;
                            em.getTransaction().commit();
                            System.out.println("Estado actualizado.");
                        } else {
                            throw new RuntimeException("Error");
                        }
                    } else {
                        throw new RuntimeException("Error");
                    }
                } else if (x == 3) {
                    List<Responsable> rs = em.createQuery("from Responsable", Responsable.class).getResultList();
                    for (Responsable t : rs) {
                        System.out.println(t.id + " - " + t.nombre + " (" + t.area + ")");
                    }
                    System.out.print("ID solicitud: ");
                    Long a = Long.parseLong(in.nextLine().trim());
                    System.out.print("ID responsable: ");
                    Long b = Long.parseLong(in.nextLine().trim());

                    em.getTransaction().begin();
                    Solicitud s = em.find(Solicitud.class, a);
                    Responsable r = em.find(Responsable.class, b);
                    if (s != null && r != null) {
                        s.responsable = r;
                        em.getTransaction().commit();
                        System.out.println("Responsable asignado.");
                    } else {
                        throw new RuntimeException("Error");
                    }
                } else if (x == 4) {
                    System.out.print("Estado (Enter = todos): ");
                    String a = in.nextLine().trim().toUpperCase();
                    System.out.print("Tipo (Enter = todos): ");
                    String b = in.nextLine().trim().toUpperCase();

                    String q = "from Solicitud s where 1=1";
                    if (!a.isEmpty()) q = q + " and s.estado = '" + a + "'";
                    if (!b.isEmpty()) q = q + " and s.tipo = '" + b + "'";

                    List<Solicitud> l = em.createQuery(q, Solicitud.class).getResultList();
                    if (l.size() == 0) System.out.println("Sin resultados.");
                    for (int i = 0; i < l.size(); i++) {
                        System.out.println(formatear(l.get(i)));
                    }
                } else if (x == 5) {
                    System.out.println("--- REPORTE ---");
                    System.out.println("Total: " + em.createQuery("select count(s) from Solicitud s", Long.class).getSingleResult());
                    System.out.println("Sin responsable: " + em.createQuery(
                            "select count(s) from Solicitud s where s.responsable is null", Long.class).getSingleResult());

                    System.out.println("Por estado:");
                    List<Object[]> l1 = em.createQuery(
                            "select s.estado, count(s) from Solicitud s group by s.estado", Object[].class).getResultList();
                    for (int i = 0; i < l1.size(); i++) {
                        System.out.println("  " + l1.get(i)[0] + ": " + l1.get(i)[1]);
                    }

                    System.out.println("Por tipo:");
                    List<Object[]> l2 = em.createQuery(
                            "select s.tipo, count(s) from Solicitud s group by s.tipo", Object[].class).getResultList();
                    for (int i = 0; i < l2.size(); i++) {
                        System.out.println("  " + l2.get(i)[0] + ": " + l2.get(i)[1]);
                    }
                }
            } catch (Exception ex) {
                System.out.println("Error");
            }
        } while (x != 0);
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
