package com.academico;

import jakarta.persistence.*;

@Entity
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    public Estudiante estudiante;

    @ManyToOne
    public Responsable responsable; // null hasta que se asigna

    public String tipo;       // CERTIFICADO, EQUIVALENCIA, BAJA_MATERIA, OTRO
    public String prioridad;  // BAJA, MEDIA, ALTA
    public String descripcion;
    @Enumerated(EnumType.STRING)
    public Estado estado = Estado.PENDIENTE;

    public Solicitud() {}

    public Solicitud(Estudiante estudiante, String tipo, String prioridad, String descripcion) {
        this.estudiante = estudiante;
        this.tipo = tipo;
        this.prioridad = prioridad;
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        String nombreResponsable;
        if (responsable == null) {
            nombreResponsable = "sin asignar";
        } else {
            nombreResponsable = responsable.nombre;
        }
        return "#" + id + " | " + tipo + " | " + prioridad + " | " + estado
                + " | " + estudiante.nombre
                + " | Resp: " + nombreResponsable
                + " | " + descripcion;
    }
}
