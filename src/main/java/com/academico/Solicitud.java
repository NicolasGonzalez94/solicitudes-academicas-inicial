package com.academico;

import jakarta.persistence.*;

@Entity
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Estudiante estudiante;

    @ManyToOne
    private Responsable responsable; // null hasta que se asigna

    private String tipo;       // CERTIFICADO, EQUIVALENCIA, BAJA_MATERIA, OTRO
    private String prioridad;  // BAJA, MEDIA, ALTA
    private String descripcion;

    @Enumerated(EnumType.STRING)
    private Estado estado = Estado.PENDIENTE;

    public Solicitud() {}

    public Solicitud(Estudiante estudiante, String tipo, String prioridad, String descripcion) {
        this.estudiante = estudiante;
        this.tipo = tipo;
        this.prioridad = prioridad;
        this.descripcion = descripcion;
    }

    public Long getId() { return id; }
    public void setResponsable(Responsable responsable) { this.responsable = responsable; }
    public void setEstado(Estado estado) { this.estado = estado; }

    @Override
    public String toString() {
        String nombreResponsable;
        if (responsable == null) {
            nombreResponsable = "sin asignar";
        } else {
            nombreResponsable = responsable.getNombre();
        }
        return "#" + id + " | " + tipo + " | " + prioridad + " | " + estado
                + " | " + estudiante.getNombre()
                + " | Resp: " + nombreResponsable
                + " | " + descripcion;
    }
}
