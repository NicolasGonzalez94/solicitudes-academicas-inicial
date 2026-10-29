package com.academico;

import jakarta.persistence.*;

@Entity
public class Responsable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String nombre;
    public String area;

    public Responsable() {}

    public Responsable(String n, String a) {
        nombre = n;
        area = a;
    }

    @Override
    public String toString() {
        return id + " - " + nombre + " (" + area + ")";
    }
}
