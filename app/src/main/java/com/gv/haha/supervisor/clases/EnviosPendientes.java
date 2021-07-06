package com.gv.haha.supervisor.clases;

/**
 * Created by User on 09/02/2018.
 */

public class EnviosPendientes {

    private String IdAsistencia, codUsuario, Pdv, CostoTran, CostoTaxi, CostoAlim, CostoHosped, CostoVario, FechaEntrada, Comentario, estado, IdPdV, NombrePdV, FechaSalida, KmActual, IdEnviado, TIPO;

    public EnviosPendientes(String idAsistencia, String codUsuario, String pdv, String costoTran, String costoTaxi, String costoAlim, String costoHosped, String costoVario, String fechaEntrada, String comentario, String estado, String idPdV, String nombrePdV, String fechaSalida, String kmActual, String idEnviado, String tipo) {
        IdAsistencia = idAsistencia;
        this.codUsuario = codUsuario;
        Pdv = pdv;
        CostoTran = costoTran;
        CostoTaxi = costoTaxi;
        CostoAlim = costoAlim;
        CostoHosped = costoHosped;
        CostoVario = costoVario;
        FechaEntrada = fechaEntrada;
        Comentario = comentario;
        this.estado = estado;
        IdPdV = idPdV;
        NombrePdV = nombrePdV;
        FechaSalida = fechaSalida;
        KmActual = kmActual;
        IdEnviado = idEnviado;
        TIPO = tipo;
    }

    public String getIdAsistencia() {
        return IdAsistencia;
    }

    public String getCodUsuario() {
        return codUsuario;
    }

    public String getPdv() {
        return Pdv;
    }

    public String getCostoTran() {
        return CostoTran;
    }

    public String getCostoTaxi() {
        return CostoTaxi;
    }

    public String getCostoAlim() {
        return CostoAlim;
    }

    public String getCostoHosped() {
        return CostoHosped;
    }

    public String getCostoVario() {
        return CostoVario;
    }

    public String getFechaEntrada() {
        return FechaEntrada;
    }

    public String getComentario() {
        return Comentario;
    }

    public String getEstado() {
        return estado;
    }

    public String getIdPdV() {
        return IdPdV;
    }

    public String getNombrePdV() {
        return NombrePdV;
    }

    public String getFechaSalida() {
        return FechaSalida;
    }

    public String getKmActual() {
        return KmActual;
    }

    public String getIdEnviado() {
        return IdEnviado;
    }

    public String getTIPO() {
        return TIPO;
    }
}