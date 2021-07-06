package com.gv.haha.supervisor;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gv.haha.supervisor.clases.AsyncTaskComplete;
import com.gv.haha.supervisor.clases.SendSMS;
import com.gv.haha.supervisor.clases.classCustomToast;
import com.gv.haha.supervisor.clases.classDetectMockLocation;
import com.gv.haha.supervisor.clases.classMetodosGenerales;
import com.gv.haha.supervisor.clases.classWebService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.gv.haha.supervisor.clases.interfVariablesGenerales.URL_WS_MULTIPLE_OPCIONES;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.codUsuario;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.gNUMERO_PERMITIDO;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.stMockLocation;

public class MockLocationMessage extends AppCompatActivity implements AsyncTaskComplete {

    classDetectMockLocation DML;
    SharedPreferences setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock_location_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DML = new classDetectMockLocation(this);
        setting = PreferenceManager.getDefaultSharedPreferences(this);

        TextView tvMensaj = (TextView) findViewById(R.id.tvMockLocatioDetect_MLM);
        String mensaje = "Se ha detectado que estas usando una aplicación para simular tu ubicación (" +
                TextUtils.join(",", DML.getListOfFakeLocationApps()) +
                "), estás consciente que este tipo de fraude tiene graves consecuencias.\n\n" +
                "1 - Se ha bloqueado el usuario, para recuperar debes comunicarte con soporte.\n\n" +
                "2 - Se ha generado un reporte que será enviado al supervisor correspondiente.\n\n" +
                "3 - Por su bien le recomendamos no volver a intentarlo.\n\n\n" +
                "Valoremos el trabajo! 👌";
        tvMensaj.setText(mensaje);

        ImageView advertencia = (ImageView) findViewById(R.id.ivMockLocatioDetect_MLM);
        advertencia.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(MockLocationMessage.this, "Eres libre!", Toast.LENGTH_LONG).show();
                SharedPreferences.Editor editor = setting.edit();
                editor.putInt(stMockLocation, 0);
                editor.commit();
                return true;
            }
        });

        int Bloqueado = setting.getInt(stMockLocation, 0);
        //si aun no esta bloqueado
        if (Bloqueado == 0) {
            SharedPreferences.Editor editor = setting.edit();
            editor.putInt(stMockLocation, 1);
            editor.commit();

            String sms = "Hola, soy " + setting.getString(getResources().getString(R.string.nombUsuario), "") +
                    " con Id " + setting.getInt(getResources().getString(R.string.codUsuario), 0) +
                    ", He usado una app para simular mi ubicación, por lo cual se me ha bloqueado mi usuario.";
            new SendSMS(this, gNUMERO_PERMITIDO, sms);
        }

        List<classWebService> params = new ArrayList<>();
        params.add(new classWebService("opcion", "ActualizaUsuarioUbicacionSimulada"));
        params.add(new classWebService("listaParametros", Bloqueado + "¬" + setting.getInt(codUsuario, 0)));
        params.add(new classWebService("format", "json"));

        new classMetodosGenerales(this).webServicePost(URL_WS_MULTIPLE_OPCIONES,
                params, option_enviar_ubic_simul);

        //writeToFile(mensaje);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_historial, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_actualizar:
                List<classWebService> params = new ArrayList<>();
                params.add(new classWebService("opcion", "ObtenerUsuarioUbicacionSimulada"));
                params.add(new classWebService("listaParametros", "" + setting.getInt(codUsuario, 0)));
                params.add(new classWebService("format", "json"));

                new classMetodosGenerales(this).webServicePost(URL_WS_MULTIPLE_OPCIONES,
                        params, option_obtener_ubic_simul);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    private void writeToFile(String data) {
        // write on SD card file data in the text box
        try {
            String Fecha = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/grupovalor/supervisor/";
            //si no existe la carpeta, se crea.
            if (!new File(path).exists()) {
                new File(path).mkdirs();
            }

            File myFile = new File(path + "MockLocation_" + Fecha + ".txt");
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter =
                    new OutputStreamWriter(fOut);
            myOutWriter.append(data);
            myOutWriter.close();
            fOut.close();
            Toast.makeText(getBaseContext(),
                    "Done writing SD 'mysdfile.txt'",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void ReadFile() {
        try {
            File myFile = new File("/sdcard/mysdfile.txt");
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(
                    new InputStreamReader(fIn));
            String aDataRow = "";
            String aBuffer = "";
            while ((aDataRow = myReader.readLine()) != null) {
                aBuffer += aDataRow + "\n";
            }
            ((TextView) findViewById(R.id.tvMockLocatioDetect_MLM)).setText(aBuffer);
            myReader.close();
            Toast.makeText(getBaseContext(),
                    "Done reading SD 'mysdfile.txt'",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAsyncTaskComplete(JSONObject result, int option) {
        //esto es lo que devuelve el servidor
        if (result != null) {

            if (option == option_obtener_ubic_simul) {
                try {
                    int success = result.getInt("success");
                    JSONArray jsonArray = result.getJSONArray("Datos");
                    if (jsonArray.length() > 0) {
                        JSONObject c = jsonArray.getJSONObject(0);
                        int UsoUbicacionSimulada = c.getInt("UsoUbicacionSimulada");
                        if (UsoUbicacionSimulada == 0) {
                            SharedPreferences.Editor editor = setting.edit();
                            editor.putInt(stMockLocation, UsoUbicacionSimulada);
                            editor.commit();
                            startActivity(new Intent(this, MainActivity.class));
                            this.finish();
                        }else {
                            new classCustomToast(this).Show_ToastError("Aun sigues bloqueado.");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } else {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_error)
                    .setTitle("Error al obtener datos.")
                    .setMessage("No se ha podido sincronizar, puede que no tengas internet.")
                    .show();
        }
    }
}
