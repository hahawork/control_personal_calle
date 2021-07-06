package com.gv.haha.supervisor.clases;

public class classPdv {
    private int idPdv;
    private String Nombre,coordenadas;


    public classPdv(int idPdv, String nombre, String coordenadas) {
        this.idPdv = idPdv;
        Nombre = nombre;
        this.coordenadas = coordenadas;
    }

    public int getIdPdv() {
        return idPdv;
    }

    public String getNombre() {
        return Nombre;
    }

    public String getCoordenadas() {
        return coordenadas;
    }
}
