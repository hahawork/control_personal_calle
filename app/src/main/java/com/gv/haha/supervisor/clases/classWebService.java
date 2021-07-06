package com.gv.haha.supervisor.clases;

public class classWebService {
    private String Parametro;
    private String Valor;

    public classWebService(String parametro, String valor) {
        Parametro = parametro;
        Valor = valor;
    }

    public String getParametro() {
        return Parametro;
    }

    public String getValor() {
        return Valor;
    }
}
