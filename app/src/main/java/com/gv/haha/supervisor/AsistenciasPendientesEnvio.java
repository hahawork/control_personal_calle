package com.gv.haha.supervisor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.gv.haha.supervisor.clases.EnviosPendientes;
import com.gv.haha.supervisor.clases.JSONParser;
import com.gv.haha.supervisor.clases.SQLHelper;
import com.gv.haha.supervisor.clases.classCustomToast;
import com.gv.haha.supervisor.clases.classMetodosGenerales;
import com.gv.haha.supervisor.clases.interfVariablesGenerales;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.gv.haha.supervisor.clases.interfVariablesGenerales.TBL_ASISTENCIA;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.URL_WS_GUARDAR_ASISTENCIA_ENT_SAL;

public class AsistenciasPendientesEnvio extends AppCompatActivity {

    ListView lvAsisPend;
    SQLiteDatabase db;
    ProgressDialog pDialog;

    int IdEnviado;
    boolean HaySalidaMarcada = false;

    SharedPreferences setting;
    List<EnviosPendientes> arrEnviosPendientes = new ArrayList<>();

    // para el adaptador dsel listview
    List<HashMap<String, String>> aList = new ArrayList<HashMap<String, String>>();
    String[] keys = new String[]{"pdv", "fecha", "comentario", "Estado"};
    int[] controles = new int[]{R.id.tvNombrePDV_CD, R.id.tvCiudad_CD, R.id.tvContacto_CD};

    Context mContext;
    classMetodosGenerales VG;
    Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asistencias_pendientes_envio);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mActivity = this;
        mContext = this;
        VG = new classMetodosGenerales(mContext);

        setting = PreferenceManager.getDefaultSharedPreferences(this);
        db = new SQLHelper(this).getWritableDatabase();

        try {

            String consulta = "SELECT * FROM " + TBL_ASISTENCIA + " INNER JOIN puntosdeventaAsistencia ON IdPdV = Pdv WHERE ESTADO = '0' or EstadoSalidaEnviado = '0'";
            Cursor cAsistenc = db.rawQuery(consulta, null);


            if (cAsistenc.moveToFirst()) {
                do {

                    String TipoEnvio = DeterminaTipoEnvio(
                            cAsistenc.getInt(cAsistenc.getColumnIndex("estado")),
                            cAsistenc.getString(cAsistenc.getColumnIndex("FechaSalida"))
                    );

                    if (TipoEnvio.length() > 0) { // si se cumple al menos una de las condiciones para envio
                        arrEnviosPendientes.add(new EnviosPendientes(
                                cAsistenc.getString(cAsistenc.getColumnIndex("IdAsistencia")),
                                "" + setting.getInt("codUsuario", 0),
                                cAsistenc.getString(cAsistenc.getColumnIndex("Pdv")),
                                cAsistenc.getString(cAsistenc.getColumnIndex("CostoTran")),
                                cAsistenc.getString(cAsistenc.getColumnIndex("CostoTaxi")),
                                cAsistenc.getString(cAsistenc.getColumnIndex("CostoAlim")),
                                cAsistenc.getString(cAsistenc.getColumnIndex("CostoHosped")),
                                cAsistenc.getString(cAsistenc.getColumnIndex("CostoVario")),
                                cAsistenc.getString(cAsistenc.getColumnIndex("Fecha")),
                                cAsistenc.getString(cAsistenc.getColumnIndex("Comentario")),
                                cAsistenc.getString(cAsistenc.getColumnIndex("estado")),
                                cAsistenc.getString(cAsistenc.getColumnIndex("IdPdV")),
                                cAsistenc.getString(cAsistenc.getColumnIndex("NombrePdV")),
                                cAsistenc.getString(cAsistenc.getColumnIndex("FechaSalida")),
                                cAsistenc.getString(cAsistenc.getColumnIndex("KmActual")),
                                cAsistenc.getString(cAsistenc.getColumnIndex("IdEnviado")),
                                cAsistenc.getString(cAsistenc.getColumnIndex("EstadoSalidaEnviado"))));

                        HashMap<String, String> hm = new HashMap<String, String>();
                        hm.put(keys[0], cAsistenc.getString(cAsistenc.getColumnIndex("NombrePdV")) + " .:. " + TipoEnvio);
                        hm.put(keys[1], "IdEnvio: "+cAsistenc.getString(cAsistenc.getColumnIndex("IdEnviado"))+"\nEnt: " + cAsistenc.getString(cAsistenc.getColumnIndex("Fecha")) + "\nSal: " + cAsistenc.getString(cAsistenc.getColumnIndex("FechaSalida")));
                        hm.put(keys[2], cAsistenc.getString(cAsistenc.getColumnIndex("Comentario")));
                        aList.add(hm);
                    }

                } while (cAsistenc.moveToNext());
            } else {

            }
        } catch (Exception e) {
            new AlertDialog.Builder(AsistenciasPendientesEnvio.this).setTitle("error!.").setMessage(e.getMessage()).show();
            e.printStackTrace();
        }

        lvAsisPend = (ListView) findViewById(R.id.lvAsistenciaP_APE);
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, aList, R.layout.list_pdv, keys, controles);
        lvAsisPend.setAdapter(simpleAdapter);

        lvAsisPend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                try {
                    IdEnviado = Integer.parseInt(arrEnviosPendientes.get(position).getIdAsistencia());

                    String fs = arrEnviosPendientes.get(position).getFechaSalida() == null ? "" : arrEnviosPendientes.get(position).getFechaSalida();
                    if (fs.length() > 0) {
                        HaySalidaMarcada = true;
                    }

                    if (VG.TieneConexion()) {

                        String idenv = arrEnviosPendientes.get(position).getIdEnviado() == null ? "0" : arrEnviosPendientes.get(position).getIdEnviado();
                        //if (idenv.equals("0")) {

                            EnviosPendientes enviosPendientes = new EnviosPendientes(
                                    arrEnviosPendientes.get(position).getIdAsistencia(),
                                    "" + setting.getInt("codUsuario", 0),
                                    arrEnviosPendientes.get(position).getPdv(),
                                    arrEnviosPendientes.get(position).getCostoTran(),
                                    arrEnviosPendientes.get(position).getCostoTaxi(),
                                    arrEnviosPendientes.get(position).getCostoAlim(),
                                    arrEnviosPendientes.get(position).getCostoHosped(),
                                    arrEnviosPendientes.get(position).getCostoVario(),
                                    arrEnviosPendientes.get(position).getFechaEntrada(),
                                    arrEnviosPendientes.get(position).getComentario(),
                                    arrEnviosPendientes.get(position).getEstado(),
                                    arrEnviosPendientes.get(position).getIdPdV(),
                                    arrEnviosPendientes.get(position).getNombrePdV(),
                                    arrEnviosPendientes.get(position).getFechaSalida(),
                                    arrEnviosPendientes.get(position).getKmActual(),
                                    arrEnviosPendientes.get(position).getIdEnviado(),
                                    //Tipo = -1 sino hay salida, 0 si hay salida pero no se ha enviado
                                    arrEnviosPendientes.get(position).getTIPO());


                            new classMetodosGenerales(AsistenciasPendientesEnvio.this).crearNuevaAsistencia(enviosPendientes);
                       // }

                    } else {
                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(AsistenciasPendientesEnvio.this);
                        alertDialog.setTitle("Error al enviar asistencia.");
                        alertDialog.setMessage("No tienes conexion a internet");
                        alertDialog.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Metodo utilizado para mostarle al usuariio si el envio pendiente es solo entrada, entrada y salida, o solo salida
     *
     * @param EstEnviadoEnt para ver si la entrada ya fue enviada al servidor
     * @param FechaSalida   para ver si la fecha de salida ya esta guardada
     * @return el tipo de envio
     */
    public String DeterminaTipoEnvio(int EstEnviadoEnt, String FechaSalida) {

        if (EstEnviadoEnt == 0 && FechaSalida.length() == 0) {

            return "Solo entrada";

        } else if (EstEnviadoEnt == 1 && FechaSalida.length() > 0) {

            return "Solo salida";

        } else if (EstEnviadoEnt == 0 && FechaSalida.length() > 0) {

            return "Entrada y Salida";

        } else if (EstEnviadoEnt == 1 && FechaSalida.length() == 0) {
            return "";
        }

        return "";
    }
}
