package com.gv.haha.supervisor;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.gv.haha.supervisor.clases.AsyncTaskComplete;
import com.gv.haha.supervisor.clases.SQLHelper;
import com.gv.haha.supervisor.clases.classBottomNavigationBehavior;
import com.gv.haha.supervisor.clases.classCustomToast;
import com.gv.haha.supervisor.clases.classHistorialAdapter;
import com.gv.haha.supervisor.clases.classMetodosGenerales;
import com.gv.haha.supervisor.clases.classWebService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.gv.haha.supervisor.clases.interfVariablesGenerales.TBL_ASISTENCIA;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.TBL_ASISTENCIA_LISTA;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.TBL_PDV_ASISTENCIA;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.URL_WS_OBTENER_HISTORIA_USUARIO;

public class Historial extends AppCompatActivity implements AsyncTaskComplete, BottomNavigationView.OnNavigationItemSelectedListener {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    List<classHistorialAdapter.Historia> arrHistoria = new ArrayList<>();

    static TextView tvViaticos;
    static SharedPreferences setting;
    static SQLiteDatabase db;
    static Context mContext;
    static Activity mActivity;
    static classMetodosGenerales cmg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        initToolBar();

        initControles();

        initializeData();


    }




    private void initToolBar() {


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //esto es para el booton bar
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);
        // attaching bottom sheet behaviour - hide / show on scroll
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) navigation.getLayoutParams();
        layoutParams.setBehavior(new classBottomNavigationBehavior());


    }

    private void initControles() {

        mContext = this;
        mActivity = this;
        setting = PreferenceManager.getDefaultSharedPreferences(mContext);
        cmg = new classMetodosGenerales(this);

        tvViaticos = (TextView) findViewById(R.id.tvTotalViaticos_H);

        db = new SQLHelper(this).getWritableDatabase();


        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    private void initializeData() {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Cursor cAsistenc = db.rawQuery("SELECT * FROM " + TBL_ASISTENCIA + " AS A INNER JOIN " + TBL_PDV_ASISTENCIA + " AS B ON IdPdV = Pdv WHERE ESTADO = '0'", null);
            if (cAsistenc.moveToFirst()) {
                do {
                    arrHistoria.add(new classHistorialAdapter.Historia(
                            "Id: " + cAsistenc.getString(cAsistenc.getColumnIndex("IdAsistencia")) + " .:. " + cAsistenc.getString(cAsistenc.getColumnIndex("NombrePdV")),
                            new SimpleDateFormat("EEEE dd MMMM hh:mm a").format(sdf.parse(cAsistenc.getString(cAsistenc.getColumnIndex("Fecha")))),
                            String.format("*Envio pendiente*")
                    ));
                } while (cAsistenc.moveToNext());
            }


            Float Viaticos = 0.0f;
            Cursor cAsist = db.rawQuery("Select * from " + TBL_ASISTENCIA_LISTA, null);

            if (cAsist.moveToFirst()) {
                do {

                    try {
                        Date fechaEntrada = sdf.parse(cAsist.getString(2));
                        String FS = cAsist.getString(cAsist.getColumnIndex("FechaSalida"));
                        Date fechaSalida;
                        //se busca una fecha que inicie con 2 por que estamos arriba del año 2000 (2000 inicia con 2)
                        fechaSalida = FS.substring(0, 1).equals("2") ? sdf.parse(FS) : null;

                        String fech = "Ent: " + new SimpleDateFormat("EEEE dd MMMM hh:mm a").format(fechaEntrada) + "\nSal: " + (fechaSalida != null ? new SimpleDateFormat("EEEE dd MMMM hh:mm a").format(fechaSalida) : "N/D");

                        Viaticos = Viaticos + cAsist.getFloat(3);
                        String gastos = "C$" + cAsist.getString(3) + "\n" + cAsist.getString(4);

                        arrHistoria.add(new classHistorialAdapter.Historia(
                                "Id: " + cAsist.getString(0) + " .:. " + cAsist.getString(1),
                                fech,
                                gastos
                        ));

                    } catch (ParseException pe) {
                        Log.w("Fecha", cAsist.getString(2));
                    }

                } while (cAsist.moveToNext());
            } else {

                arrHistoria.add(new classHistorialAdapter.Historia(
                        "Id: 0 .:. Aun no tienes registros",
                        new SimpleDateFormat("dd-MMM HH:mm").format(new Date()),
                        "C$0.00"
                ));

                descargarInformacion();
            }

            // specify an adapter (see also next example)
            mAdapter = new classHistorialAdapter(arrHistoria);
            mRecyclerView.setAdapter(mAdapter);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_actualizar:

                descargarInformacion();

                return true;
        }

        return false;
    }

    private void descargarInformacion() {

        //parametros para enviar al web service
        ArrayList<classWebService> parametros = new ArrayList<>();
        parametros.add(new classWebService("usuario", String.valueOf(setting.getInt("codUsuario", 0))));

        //se manda a ejecutar el web service
        // el resultado esta en el metodo onTaskRegistroComplete
        new classMetodosGenerales(this).webServicePost(URL_WS_OBTENER_HISTORIA_USUARIO, parametros, option_historial);

    }

    @Override
    public void onAsyncTaskComplete(JSONObject result, int option) {

        if (result == null) {

            new classCustomToast(this).Show_ToastError("sin repuesta del servidor, o verifique su conexión a internet.");

        } else {

            try {

                int success = result.getInt("success");

                if (success == 1) {     //Insertado correctamente

                    //limpia la tabla
                    db.delete(TBL_ASISTENCIA_LISTA,null,null);

                    JSONArray Asistencia = result.getJSONArray("usuario");
                    for (int i = 0; i < Asistencia.length(); i++) {        // PARA CADA PUNTO DE VENTA REGISTRADO

                        JSONObject c = Asistencia.getJSONObject(i);

                        try {
                            ContentValues values = new ContentValues();
                            values.put("IdAsistencia", c.getString("idAsistencia"));
                            values.put("Pdv", c.getString("NombrePdV"));
                            values.put("Fecha", c.getString("FechaRegistro"));
                            values.put("Gastos", c.getString("CantGastosMovim"));
                            values.put("Observacion", c.getString("Observacion"));
                            values.put("FechaSalida", c.getString("FechaSalida"));

                            db.insert(TBL_ASISTENCIA_LISTA, null, values);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    new classCustomToast(this).Toast("guardado", R.drawable.ic_success);
                    recreate();
                    new classCustomToast(this).Toast(result.getString("SumaViaticos"), R.drawable.ic_success);

                } else {
                    new classCustomToast(this).Show_ToastError(result.getString("message"));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
