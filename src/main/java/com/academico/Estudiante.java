package com.academico;

import jakarta.persistence.*;

@Entity
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String nombre;

    public Estudiante() {}

    public Estudiante(String n) {
        nombre = n;
    }
}
