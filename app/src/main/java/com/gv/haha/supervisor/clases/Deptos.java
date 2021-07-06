package com.gv.haha.supervisor.clases;

import java.util.List;

public class Deptos {
    private int idDepto;
    private String Depto;
    private Boolean Selecc;

    public Deptos() {
    }

    public Deptos(int idDepto, String depto) {
        this.idDepto = idDepto;
        Depto = depto;
    }

    public Deptos(int idDepto, String depto, Boolean selecc) {
        this.idDepto = idDepto;
        Depto = depto;
        Selecc = selecc;
    }

    public int getIdDepto() {
        return idDepto;
    }

    public String getDepto() {
        return Depto;
    }

    public Boolean getSelecc() {
        return Selecc;
    }

    public void setIdDepto(int idDepto) {
        this.idDepto = idDepto;
    }

    public void setDepto(String depto) {
        Depto = depto;
    }

    public void setSelecc(Boolean selecc) {
        Selecc = selecc;
    }


    public Integer[] getArrayIdDepto(List<Deptos> deptos) {
        Integer[] arrDepto = new Integer[deptos.size()];
        for (int i = 0; i < deptos.size(); i++) {
            arrDepto[i] = deptos.get(i).getIdDepto();
        }
        return arrDepto;
    }

    public String[] getArrayDepto(List<Deptos> deptos) {
        String[] arrDepto = new String[deptos.size()];
        for (int i = 0; i < deptos.size(); i++) {
            arrDepto[i] = deptos.get(i).getDepto();
        }
        return arrDepto;
    }

    public boolean[] getArrayDeptoSelecc(List<Deptos> deptos) {
        boolean[] arrDepto = new boolean[deptos.size()];
        for (int i = 0; i < deptos.size(); i++) {
            arrDepto[i] = deptos.get(i).getSelecc();
        }
        return arrDepto;
    }
}
