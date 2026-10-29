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
    public String estado;     // PENDIENTE, EN_PROCESO, RESUELTA, RECHAZADA

    public Solicitud() {}
}
