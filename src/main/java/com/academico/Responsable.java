package com.academico;

import jakarta.persistence.*;

@Entity
public class Responsable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String area;

    public Responsable() {}

    public Responsable(String nombre, String area) {
        this.nombre = nombre;
        this.area = area;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }

    @Override
    public String toString() { return id + " - " + nombre + " (" + area + ")"; }
}
