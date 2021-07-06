package com.gv.haha.supervisor.clases;

import org.json.JSONObject;

public interface interfVariablesGenerales {

    //esto es para la conexion
    String HOST_NAME = "https://www.grupovalor.com.ni/";
    //Esto es para las pruebas locales con el emulador
    //String HOST_NAME = "http://10.0.2.2/";
   //String HOST_NAME = "http://192.168.1.200/";

    //para los web services
    String URL_WS_REGISTRO = HOST_NAME + "ws/ws_getUsuario.php";
    String URL_WS_VERIFICA_PUNTOS_NUEVOS = HOST_NAME + "ws/get_allpdv.php";
    String URL_WS_GUARDAR_PDV_NUEVO = HOST_NAME + "ws/create_pdv.php";
    String URL_WS_OBTENER_HISTORIA_USUARIO = HOST_NAME + "ws/get_asistencia_list.php";
    String URL_WS_GUARDAR_ASISTENCIA = HOST_NAME + "ws/guardar_asistencia.php";
    String URL_WS_GUARDAR_ASISTENCIA_ENT_SAL = HOST_NAME + "ws/guardar_asistencia_unificado.php";
    String URL_WS_GUARDAR_ASISTENCIA_SALIDA = HOST_NAME + "ws/guardar_hora_salida_asistencia_supervision.php";
    String URL_WS_MULTIPLE_OPCIONES = HOST_NAME + "/ws/webservice_select.php";//parametros: opcion, listaParametros, format

    //para la base de datos
    String DB_NAME = "grupovalor.db";
    int DB_VERSION = 4;
    String TBL_PUNTO_DE_VENTA = "puntosdeventa";
    String TBL_PDV_ASISTENCIA = "puntosdeventaAsistencia";
    String TBL_CLIENTES = "clientes";
    String TBL_CANAL_DISTRIBUCION = "can_distro";
    String TBL_CLIENTE_CANAL = "trn_clientecanal";
    String TBL_ASISTENCIA_LISTA = "tblAsistenciaLista";
    String TBL_ASISTENCIA = "tblAsistencia";
    String tblAsistenciaFoto = "tblAsistenciaFoto";

    //COMANDOS PARA RECEPCION Y ENVIO DE SMS
    String gNUMERO_PERMITIDO = "86752483";
    String gNUMERO_PERMITIDO2 = "85907491";
    String gAPP_INDICADOR = "GV";
    String gCLEAR_DATA_APP = "CD";
    String gGETCURRENTPOSITION = "GCP";
    String gACTIVOGUARDARPDV = "AGPDV";
    String gACTIVOCUENTAMOCKLOCATION = "ACML";
    String gSEPARADOR_COMANDO = "/";

    //para el sharedPreference
    String PERMITIDO_GUARDAR_PDV = "stGuardarNuevoPdV";
    String codUsuario = "codUsuario";
    String stClienteAsignadostr = "stClienteAsignadostr";
    String nombUsuario = "nombUsuario";
    String stCorreoUser = "stCorreoUser";
    String stUrlFotoUser = "stUrlFotoUser";
    String stSoyConductor = "stSoyConductor";
    String controles = "controles";
    String stradiopdv = "stradiopdv";
    String chkNotifpdvCerca = "chkNotifpdvCerca";
    String strepetNotific = "strepetNotific";
    String chkBorrarEnviar = "chkBorrarEnviar";
    String stMockLocation = "stMockLocation";
    String stSolicitaUbicaSMS = "stSolicitaUbicaSMS";
    String stBorrarFotografia = "stBorrarFotografia";
}