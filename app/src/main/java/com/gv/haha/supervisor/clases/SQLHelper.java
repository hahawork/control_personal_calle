package com.gv.haha.supervisor.clases;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.gv.haha.supervisor.clases.interfVariablesGenerales.DB_NAME;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.DB_VERSION;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.TBL_ASISTENCIA;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.TBL_ASISTENCIA_LISTA;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.TBL_CANAL_DISTRIBUCION;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.TBL_CLIENTES;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.TBL_CLIENTE_CANAL;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.TBL_PDV_ASISTENCIA;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.TBL_PUNTO_DE_VENTA;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.tblAsistenciaFoto;

/**
 * Created by User on 10/10/2016.
 */

public class SQLHelper extends SQLiteOpenHelper {

    SQLiteDatabase db;
    Context Cntx;
    String TBL_PDV = "CREATE TABLE IF NOT EXISTS " + TBL_PUNTO_DE_VENTA + " (IdPdV INTEGER PRIMARY KEY,Cliente text ,TipoCanal text ,Departamento text ,Ciudad text ," +
            " Cadena text ,NombrePdV text ,NombreReprPdV text ,TelefonoReprPdV text ,LocationGPS text ,UserSave text ," +
            "fechaSave text, enviado integer)";

    String TBL_PDV_ASIST = "CREATE TABLE IF NOT EXISTS " + TBL_PDV_ASISTENCIA + " (IdPdV INTEGER PRIMARY KEY, NombrePdV text, LocationGPS text, IdDepto text)";

    String TBL_CLIENT = "CREATE TABLE IF NOT EXISTS " + TBL_CLIENTES + " (IdCliente INTEGER PRIMARY KEY AUTOINCREMENT, NombreCliente TEXT)";

    String TBL_CAN_DIST = "CREATE TABLE IF NOT EXISTS " + TBL_CANAL_DISTRIBUCION + " (IdCanal INTEGER PRIMARY KEY AUTOINCREMENT, NombreCanal TEXT)";

    String TBL_TRN_CLI_CAN = "CREATE TABLE IF NOT EXISTS " + TBL_CLIENTE_CANAL + " (Id INTEGER PRIMARY KEY AUTOINCREMENT, IdCliente INTEGER, IdCanal INTEGER)";

    String TBL_ASIST_LISTA = "CREATE TABLE IF NOT EXISTS " + TBL_ASISTENCIA_LISTA + "(IdAsistencia PRIMARY KEY, Pdv TEXT, Fecha TIMESTAMP, Gastos DECIMAL(5,2), Observacion TEXT, FechaSalida text)";

    String TBL_USU_ASIST = "CREATE TABLE IF NOT EXISTS " + TBL_ASISTENCIA + " (IdAsistencia INTEGER PRIMARY KEY AUTOINCREMENT, codUsuario integer, Pdv integer, CostoTran decimal(5,2), CostoTaxi decimal(5,2), " +
            "CostoAlim decimal(5,2), CostoHosped decimal(5,2), CostoVario decimal(5,2), Fecha timestamp, Comentario text, KmActual text, estado integer, IdEnviado integer, FechaSalida timestamp, EstadoSalidaEnviado integer)";

    String TBL_USU_ASIST_FOTO = "CREATE TABLE IF NOT EXISTS " + tblAsistenciaFoto + " (Id INTEGER PRIMARY KEY AUTOINCREMENT, IdAsistencia INTEGER, puntoventa integer, FechaAsistenciaEntrada datetime, AsistenciaConInternet integer,  fotopath TEXT, Comentarios TEXT, estEnvio INTEGER)";


    public SQLHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.Cntx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(TBL_PDV);
        db.execSQL(TBL_CLIENT);
        db.execSQL(TBL_CAN_DIST);
        db.execSQL(TBL_TRN_CLI_CAN);
        db.execSQL(TBL_USU_ASIST);
        db.execSQL(TBL_USU_ASIST_FOTO);
        db.execSQL(TBL_PDV_ASIST);
        db.execSQL(TBL_ASIST_LISTA);

        db.execSQL("INSERT INTO " + TBL_CANAL_DISTRIBUCION + " (NombreCanal) VALUES('Auto Servicios');");
        db.execSQL("INSERT INTO " + TBL_CANAL_DISTRIBUCION + " (NombreCanal) VALUES('Farmacias');");
        db.execSQL("INSERT INTO " + TBL_CANAL_DISTRIBUCION + " (NombreCanal) VALUES('Independientes');");
        db.execSQL("INSERT INTO " + TBL_CANAL_DISTRIBUCION + " (NombreCanal) VALUES('Mayoristas');");
        db.execSQL("INSERT INTO " + TBL_CANAL_DISTRIBUCION + " (NombreCanal) VALUES('Mini Super');");
        db.execSQL("INSERT INTO " + TBL_CANAL_DISTRIBUCION + " (NombreCanal) VALUES('Pulperias');");

        this.db = db;
        ModificarDB();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(TBL_USU_ASIST);
        }
        if (oldVersion < 3) {
            db.execSQL(TBL_USU_ASIST_FOTO);
        }
        if (newVersion > oldVersion && newVersion == 4) {
            db.execSQL("ALTER TABLE tblAsistencia ADD COLUMN KmActual text DEFAULT '0'");
        }
    }

    private void ModificarDB() {
        //si no existe el campo
        if (!ExisteColumna(tblAsistenciaFoto, "Comentarios")) {
            //agregar el nuevo campo
            AgregarColumna(tblAsistenciaFoto, "Comentarios", "TEXT");
        }
    }

    /**
     * Verifica si ya existe un campo en la tabla en la base de datos
     *
     * @param Tabla   Nombre de la tabla en la que se va verificar
     * @param Columna Nombre de la columna a verificar
     * @return Verdadero si ya existe, Falso si no exciste el campo
     */
    public boolean ExisteColumna(String Tabla, String Columna) {

        boolean Existe = false;
        try {

            Cursor cVerificaColumna = db.rawQuery("select * from " + Tabla + " where 0", null);

            String[] ColumnasExis = cVerificaColumna.getColumnNames();
            if (ColumnasExis.length > 0) {
                for (int i = 0; i < ColumnasExis.length; i++) {
                    if (ColumnasExis[i].equalsIgnoreCase(Columna)) {
                        Existe = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return Existe;
    }

    /**
     * Agrega una columna a una tabla en la base de datos
     *
     * @param Tabla      Nombre de la tabla donde se va a agregsr el nuevo campo
     * @param newColumna El nommbre de la columna nueva
     * @param TipoDato   el tipo de dato del campo.
     */
    public void AgregarColumna(String Tabla, String newColumna, String TipoDato) {
        try {
            db.execSQL("ALTER TABLE " + Tabla + " ADD COLUMN " + newColumna + " " + TipoDato);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}