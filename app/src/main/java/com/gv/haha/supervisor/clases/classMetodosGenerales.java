package com.gv.haha.supervisor.clases;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gv.haha.supervisor.MensajeViewer;
import com.gv.haha.supervisor.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import static com.gv.haha.supervisor.clases.interfVariablesGenerales.HOST_NAME;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.PERMITIDO_GUARDAR_PDV;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.TBL_ASISTENCIA;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.TBL_CLIENTES;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.TBL_PDV_ASISTENCIA;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.URL_WS_GUARDAR_ASISTENCIA_ENT_SAL;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.URL_WS_MULTIPLE_OPCIONES;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.URL_WS_VERIFICA_PUNTOS_NUEVOS;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.codUsuario;

public class classMetodosGenerales {


    JSONParser jsonParser = new JSONParser();
    SharedPreferences setting;
    private Context mContext;
    private ProgressDialog pDialog;


    public classMetodosGenerales(Context context) {

        this.mContext = context;
        setting = PreferenceManager.getDefaultSharedPreferences(mContext);
    }


    /**
     * Función que elimina acentos y caracteres especiales de
     * una cadena de texto.
     *
     * @param input
     * @return cadena de texto limpia de acentos y caracteres especiales.
     */
    public String remover_acentos(String input) {
        // Cadena de caracteres original a sustituir.
        String original = "áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜñÑçÇ";
        // Cadena de caracteres ASCII que reemplazarán los originales.
        String ascii = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUnNcC";
        String output = input;
        for (int i = 0; i < original.length(); i++) {
            // Reemplazamos los caracteres especiales.
            output = output.replace(original.charAt(i), ascii.charAt(i));
        }//for i
        return output;
    }

    public float getVersionApp(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            //int versionCode = android.support.design.BuildConfig.VERSION_CODE;
            // String versionName = android.support.design.BuildConfig.VERSION_NAME;
            return Float.parseFloat(version);
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean TieneConexion() {
        boolean bConectado = false;
        try {
            ConnectivityManager connec = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] redes = connec.getAllNetworkInfo();
            for (int i = 0; i < 2; i++) {
                if (redes[i].getState() == NetworkInfo.State.CONNECTED && redes[i].isConnected() && redes[i].isAvailable()) {
                    bConectado = true;
                    //bConectado = isInternetAvailable();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bConectado;
    }

    public boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        RunnableFuture<Boolean> futureRun = new FutureTask<Boolean>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if ((networkInfo.isAvailable()) && (networkInfo.isConnected())) {
                    try {
                        HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                        urlc.setRequestProperty("User-Agent", "Test");
                        urlc.setRequestProperty("Connection", "close");
                        urlc.setConnectTimeout(1500);
                        urlc.connect();
                        return (urlc.getResponseCode() == 200);
                    } catch (IOException e) {
                        Log.e("internet", "Error checking internet connection", e);
                    }
                } else {
                    Log.d("internet", "No network available!");
                }
                return false;
            }
        });

        new Thread(futureRun).start();


        try {
            return futureRun.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void CompartirTexto(String shareBody) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Coordenadas.");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        mContext.startActivity(Intent.createChooser(sharingIntent, "Enviar a travez de..."));
    }

    public void CompartirFoto(String pathFoto, String pdv, String Comentarios) {
        // Now send it out to share
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("image/*");
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + pathFoto));
        share.putExtra(android.content.Intent.EXTRA_SUBJECT, "Foto de " + pdv);
        share.putExtra(android.content.Intent.EXTRA_TEXT, Comentarios);
        try {
            mContext.startActivity(Intent.createChooser(share, "compartir foto de " + pdv));
        } catch (Exception e) {

        }
    }


    public void EnableRuntimePermission() {
        ActivityCompat.requestPermissions(((Activity) mContext), new String[]
                {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.GET_ACCOUNTS,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 111);
    }

    public void clearData() {
        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                ((ActivityManager) ((Activity) mContext).getSystemService(Context.ACTIVITY_SERVICE)).clearApplicationUserData();
                ((Activity) mContext).recreate();
            } else {
                //"No soporta clear data memory";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void VerificarNuevosPuntosguardados(final boolean mostrarinfo, final int idPdVActual, final String indicDeptos) {

        class VerificarNuevosPuntosguardados extends AsyncTask<String, String, JSONObject> {


            SQLiteDatabase db = new SQLHelper(mContext).getWritableDatabase();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                if (mostrarinfo) {
                    pDialog = new ProgressDialog(mContext);
                    pDialog.setTitle("Descargando lista de puntos de venta. Espere...");
                    pDialog.setIndeterminate(false);
                    pDialog.setCancelable(true);
                    pDialog.setCanceledOnTouchOutside(false);
                    pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            VerificarNuevosPuntosguardados.this.cancel(true);
                            new AlertDialog.Builder(mContext).setMessage("Se ha cancelado la petición.").setIcon(R.drawable.ic_info).show();
                        }
                    });
                    pDialog.show();
                }
            }

            @Override
            protected JSONObject doInBackground(String... strings) {

                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("idPdVActual", String.valueOf(idPdVActual)));
                params.add(new BasicNameValuePair("indicDeptos", String.valueOf(indicDeptos)));

                // getting JSON Object
                // Note that create product url accepts POST method
                JSONObject json = jsonParser.makeHttpRequest(URL_WS_VERIFICA_PUNTOS_NUEVOS, "GET", params);
                if (json != null) {
                    try {
                        int success = json.getInt("success");

                        if (success == 1) {     //Descargado correctamente

                            if (idPdVActual == 0) {
                                //limpia la tabla de la base de datos
                                db.delete(TBL_PDV_ASISTENCIA, null, null);
                            }

                            JSONArray PdV = json.getJSONArray("pdv");

                            for (int i = 0; i < PdV.length(); i++) {        // PARA CADA PUNTO DE VENTA REGISTRADO

                                JSONObject c = PdV.getJSONObject(i);

                                if (mostrarinfo && pDialog.isShowing()) {
                                    publishProgress("" + i, "" + PdV.length(), c.getString("NombrePdV"));
                                }
                                ContentValues values = new ContentValues();
                                values.put("IdPdV", c.getString("IdPdV"));
                                values.put("NombrePdV", c.getString("NombrePdV"));
                                values.put("LocationGPS", c.getString("LocationGPS"));
                                values.put("IdDepto", c.getString("Departamento"));
                                db.insert(TBL_PDV_ASISTENCIA, null, values); // se inserta en la base de datos local

                                Log.w("id " + c.getString("IdPdV"), "Nombre: " + c.getString("NombrePdV"));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                return json;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                super.onProgressUpdate(values);
                int setMax = Integer.parseInt(values[1]);
                int setpregress = Integer.parseInt(values[0]);
                pDialog.setMax(setMax);
                pDialog.setProgress(setpregress);
                pDialog.setMessage(values[0] + " de " + values[1] + " \n " + values[2]);
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                super.onPostExecute(jsonObject);
                try {

                    if (jsonObject == null) {
                        if (mostrarinfo && pDialog.isShowing()) {
                            new AlertDialog.Builder(mContext).setTitle("Error al Obtener.").setMessage("No se ha podido actualizar clientes nuevos, pueda que no tengas internet.").setIcon(R.drawable.ic_error).show();
                            pDialog.dismiss();
                        }
                    } else {
                        int success = jsonObject.getInt("success");
                        String message = jsonObject.getString("message");
                        if (success == 1) {

                            if (mostrarinfo && pDialog.isShowing()) {
                                pDialog.dismiss();
                                new classCustomToast(((Activity) mContext)).Toast(message, R.drawable.ic_success);
                                ((Activity) mContext).recreate();
                            }
                        }
                    }

                } catch (JSONException e) {
                    if (mostrarinfo && pDialog.isShowing())
                        pDialog.dismiss();

                    e.printStackTrace();
                }
            }
        }

        new VerificarNuevosPuntosguardados().execute();
    }

    /**
     * este metodo busca en el servidor online si existe un mensaje para el usuario
     * con la fecha actual
     */
    public void VerificarNuevoMensaje(final int idUsuario) {

        class Mensaje extends AsyncTask<String, JSONObject, JSONObject> {

            JSONParser jParser = new JSONParser();
            JSONObject jsonObject = null;

            @Override
            protected JSONObject doInBackground(String... args) {

                try {
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("idUsuario", String.valueOf(idUsuario)));
                    params.add(new BasicNameValuePair("Fecha", new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
                    params.add(new BasicNameValuePair("EstadoVisto", "0"));
                    return jParser.makeHttpRequest(HOST_NAME + "ws/mensajeusuario/getmensajeUsuario.php", "GET", params);

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final JSONObject jsonObject) {
                super.onPostExecute(jsonObject);
                try {

                    if (jsonObject == null) {
                    } else {
                        int success = jsonObject.getInt("success");
                        if (success == 1) {

                            new AlertDialog.Builder(mContext).setTitle("Mensaje").setCancelable(true).setMessage("Hay un nuevo mensaje para ti.")
                                    .setPositiveButton("Ver Ahora", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            try {
                                                mContext.startActivity(new Intent(mContext, MensajeViewer.class));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).setNegativeButton("No por ahora", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }

        new Mensaje().execute();
    }

    public void VerificarSolicitudDeNumero() {

        class VerificarSolicitudDeNumero extends AsyncTask<String, String, JSONObject> {

            JSONParser jParser = new JSONParser();
            JSONObject jsonObject = null;

            @Override
            protected JSONObject doInBackground(String... strings) {

                try {
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("listaParametros", strings[0]));
                    params.add(new BasicNameValuePair("opcion", "VerificaSolicitarNumeroCelular"));
                    params.add(new BasicNameValuePair("format", "json"));
                    jsonObject = jParser.makeHttpRequest(HOST_NAME + "ws/webservice_select.php", "POST", params);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }

                return jsonObject;
            }

            @Override
            protected void onPostExecute(JSONObject s) {
                super.onPostExecute(s);

                try {
                    if (s == null) {

                    } else {

                        int success = s.getInt("success");
                        if (success == 1) {
                            try {

                                JSONArray datos = s.getJSONArray("Datos");
                                JSONObject jsonObject = datos.getJSONObject(0);
                                SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(mContext);

                                String mensaje = "Hola, soy " + setting.getString("nombUsuario", "") + ", este es mi numero de celular";

                                //new SendSMS(mContext, jsonObject.getString("EnviarSMSANumero"), mensaje);

                                new ActualizarPedidoDeNumero().execute("" + setting.getInt("codUsuario", 0));


                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        new VerificarSolicitudDeNumero().execute("" + setting.getInt("codUsuario", 0));

    }

    /**
     * Metodo para conectarswe al servidor online y buscar si existe una version mas reciente de la
     * actual instalada.
     */
    public void VerificarNuevaVersion() {

        class Version extends AsyncTask<String, JSONObject, JSONObject> {

            JSONParser jParser = new JSONParser();

            @Override
            protected JSONObject doInBackground(String... args) {
                try {
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    String version = args[0];
                    params.add(new BasicNameValuePair("version", version));
                    params.add(new BasicNameValuePair("idapp", "1"));
                    JSONObject jsonObject = jParser.makeHttpRequest(HOST_NAME + "ws/get_new_version.php", "GET", params);

                    try {

                        if (jsonObject == null) {

                        } else {
                            int success = jsonObject.getInt("success");
                            if (success == 1) {
                                try {
                                    final String rutaapk = Environment.getExternalStorageDirectory().getAbsolutePath() + "/grupovalor/grupovalor" + jsonObject.getString("Version") + ".apk";
                                    File apk = new File(rutaapk);
                                    if (apk.exists()) {

                                        return jsonObject;

                                    } else {
                                        int count;
                                        try {
                                            if (TieneConexion()) {
                                                URL url = new URL(jsonObject.getString("URLDescargaApp"));
                                                URLConnection conection = url.openConnection();
                                                conection.connect();
                                                int lenghtOfFile = conection.getContentLength();

                                                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                                                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/grupovalor/");
                                                if (!file.exists()) {
                                                    file.mkdirs();
                                                }

                                                OutputStream output = new FileOutputStream(rutaapk);
                                                byte data[] = new byte[1024];
                                                long total = 0;
                                                while ((count = input.read(data)) != -1) {
                                                    total += count;
                                                    Log.w("publishProgress", "" + (int) ((total * 100) / lenghtOfFile));
                                                    output.write(data, 0, count);
                                                }
                                                output.flush();
                                                output.close();
                                                input.close();
                                            } else {

                                            }
                                        } catch (Exception e) {
                                            Log.e("Error: ", e.getMessage());
                                        }

                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    return jsonObject;

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final JSONObject jsonObject) {
                super.onPostExecute(jsonObject);
                try {

                    if (jsonObject == null) {
                    } else {
                        int success = jsonObject.getInt("success");
                        if (success == 1) {
                            String message = jsonObject.getString("MensajeActualizacion");
                            final String rutaapk = Environment.getExternalStorageDirectory().getAbsolutePath() + "/grupovalor/grupovalor" + jsonObject.getString("Version") + ".apk";
                            File apk = new File(rutaapk);
                            if (apk.exists()) {

                                new AlertDialog.Builder(mContext)
                                        .setTitle(message)
                                        .setMessage(String.format(" La versión %s ha sido descargada y esta lista para instalarse\nVersión actual: %s", jsonObject.get("Version"), getVersionApp(mContext)))
                                        .setPositiveButton("INSTALAR AHORA", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Instalar(rutaapk);
                                            }
                                        })
                                        .setNegativeButton("No por ahora", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                try {
                                                    if (jsonObject.getInt("isNecesary") == 1) {
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                                            ((Activity) mContext).finishAffinity();
                                                        } else {
                                                            ((Activity) mContext).finish();
                                                        }
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        })
                                        .show();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }

        new Version().execute(getVersionApp(mContext) + "");
    }

    public void Instalar(String rutaapk) {

        File app = new File(rutaapk);
        if (app.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(app), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);

            ((Activity) mContext).finish();
        }
    }

    public void UpdateVerificarPermisoGuardarNuevoPdV() {

        class UpdateVerificarPermisoGuardarNuevoPdV extends AsyncTask<String, Void, Void> {

            JSONParser jParser = new JSONParser();

            @Override
            protected Void doInBackground(String... strings) {
                try {
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("listaParametros", "" + setting.getInt("codUsuario", 0)));
                    params.add(new BasicNameValuePair("opcion", "UpdateVerificarPermisoGuardarNuevoPdV"));
                    params.add(new BasicNameValuePair("format", "json"));
                    jParser.makeHttpRequest(HOST_NAME + "ws/webservice_select.php", "POST", params);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                return null;
            }
        }

        new UpdateVerificarPermisoGuardarNuevoPdV().execute();
    }

    /**
     * este metodo verifica si el usuaeio esta habilitado para guardar nuevos pdv
     *
     * @param MetodoEfectuar esto es si va a consultar si tiene permiso : VerificarPermisoGuardarNuevoPdV
     */
    public void VerificaPermisoGuardarPdvNevo(String MetodoEfectuar) {

        try {
            SimpleMultiPartRequest smr = new SimpleMultiPartRequest(Request.Method.POST, URL_WS_MULTIPLE_OPCIONES,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("Response", response);
                            try {
                                JSONObject jObj = new JSONObject(response);
                                int status = jObj.getInt("success");
                                String message = jObj.getString("message");

                                if (status == 1) {
                                    JSONArray ja = jObj.getJSONArray("Datos");
                                    JSONObject jo = ja.getJSONObject(0);
                                    SharedPreferences.Editor editor = setting.edit();
                                    boolean permitido = jo.getInt("Permitir") == 1 ? true : false;//si se obtiene un 1 guarda true delo contrario guardas false
                                    editor.putBoolean(PERMITIDO_GUARDAR_PDV, permitido);
                                    editor.putInt("stCantidadPdvPermitido", permitido ? jo.getInt("CantidadPdvPermitido") : 0);//si esta permitido guarda la cantidad, si no guarda 0
                                    editor.commit();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });
            smr.addStringParam("listaParametros", "" + setting.getInt(codUsuario, 0));
            smr.addStringParam("opcion", MetodoEfectuar);
            smr.addStringParam("format", "json");
            classMyApplication.getInstance().addToRequestQueue(smr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * este metodo es para obtener los datos del desde el web service
     *
     * @param params estos son loss parametros para enviar al web service
     */
    public void webServicePost(String URL_WS, final List<classWebService> params, final int option) {

        // para usar ste metodo el activity debe implementar AsyncTaskComplete

        final AsyncTaskComplete mCallBack = (AsyncTaskComplete) mContext;
        //parametros para enviar al web service

        try {
            StringRequest jsonRequest = new StringRequest(Request.Method.POST, URL_WS,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            JSONObject jObj = null;
                            try {
                                jObj = new JSONObject(response);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            mCallBack.onAsyncTaskComplete(jObj, option);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    new classCustomToast((Activity) mContext).Show_ToastError("Error en la consulta al ws: " + error.getMessage());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> parameters = new HashMap<String, String>();
                    for (int i = 0; i < params.size(); i++)
                        parameters.put(params.get(i).getParametro(), remover_acentos(params.get(i).getValor()));

                    return parameters;
                }
            };
            Volley.newRequestQueue(mContext).add(jsonRequest);
            Volley.newRequestQueue(mContext).addRequestFinishedListener(new RequestQueue.RequestFinishedListener<String>() {
                @Override
                public void onRequestFinished(Request<String> request) {
                    //if (progressDialog !=  null && progressDialog.isShowing())
                    //progressDialog.dismiss();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Dialogo() {


    }

    /**
     * * este metodo es para enviar los datos al web service
     *
     * @param params estos son loss parametros para enviar al web service
     */
    public void webServicesObtenerClientes(final Map<String, String> params, final SQLiteDatabase db) {


        try {
            StringRequest jsonRequest = new StringRequest(Request.Method.POST, URL_WS_MULTIPLE_OPCIONES,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(response);
                                int success = jsonObject.getInt("success");
                                String message = jsonObject.getString("message");
                                if (success == 1) {

                                    JSONArray Datos = jsonObject.getJSONArray("Datos");

                                    for (int i = 0; i < Datos.length(); i++) {        // PARA CADA REGISTRO
                                        JSONObject c = Datos.getJSONObject(i);

                                        ContentValues values = new ContentValues();
                                        values.put("IdCliente", c.getString("IdCliente"));
                                        values.put("NombreCliente", c.getString("RazonSocial"));
                                        db.insert(TBL_CLIENTES, null, values); // se inserta en la base de datos local
                                    }

                                } else {
                                    //Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //Toast.makeText(mContext, "Error al ws: webServicesObtenerClientes " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters = params;
                    return parameters;
                }
            };
            Volley.newRequestQueue(mContext).add(jsonRequest);
            Volley.newRequestQueue(mContext).addRequestFinishedListener(new RequestQueue.RequestFinishedListener<String>() {
                @Override
                public void onRequestFinished(Request<String> request) {
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Clase generada para guardar en la bese de datos online la asistencia de la supervisora en un punto
     * de venta
     */
    public void crearNuevaAsistencia(final EnviosPendientes params) {
        try {

            final SQLiteDatabase db = new SQLHelper(mContext).getWritableDatabase();

            StringRequest jsonRequest = new StringRequest(Request.Method.POST, URL_WS_GUARDAR_ASISTENCIA_ENT_SAL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            JSONObject file_url = null;
                            try {
                                file_url = new JSONObject(response);
                                if (file_url == null) {

                                } else {
                                    int success = file_url.getInt("successInsert");
                                    String mensaje = file_url.getString("messageInsert");
                                    if (success == 1) {

                                        SharedPreferences.Editor editor = setting.edit();

                                        int successMaxID = file_url.getInt("successMaxID");
                                        int IdAsistencia = file_url.getInt("IdAsistencia");

                                        if (successMaxID == 1) {

                                            ContentValues values = new ContentValues();
                                            values.put("estado", 1);
                                            values.put("IdEnviado", IdAsistencia);
                                            values.put("EstadoSalidaEnviado", params.getTIPO().equals("0") ? "1" : "-1"); // si es solo entrada pone estado salida cero de lo contrario 1

                                            int successUpdate = db.update(TBL_ASISTENCIA, values, "IdAsistencia = '" + params.getIdAsistencia() + "'", null);
                                            if (successUpdate > 0) {
                                                try {
                                                    new classCustomToast((Activity) mContext).Toast(mensaje, R.drawable.ic_info);
                                                    ((Activity) mContext).recreate();

                                                } catch (Exception e) {
                                                }
                                            }

                                        }

                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    String a = error.getMessage();
                    new classCustomToast((Activity) mContext).Toast("Ha ocurrido un error  en el servidor, se enviara mas tarde.", R.drawable.ic_error);
                    ((Activity) mContext).recreate();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put("idAsistencia", params.getIdEnviado());
                    parameters.put("idUsuario", params.getCodUsuario());
                    parameters.put("IdPdV", params.getIdPdV());
                    parameters.put("CantGastosMovim", params.getCostoTran());
                    parameters.put("CantGastosMovimTaxi", params.getCostoTaxi());
                    parameters.put("CantGastosAlim", params.getCostoAlim());
                    parameters.put("CantGastosHosped", params.getCostoHosped());
                    parameters.put("CantGastosVario", params.getCostoVario());
                    parameters.put("FechaRegistro", params.getFechaEntrada());
                    parameters.put("Observacion", remover_acentos(params.getComentario()));
                    parameters.put("KmActual", params.getKmActual());
                    parameters.put("FechaSalida", params.getFechaSalida());
                    return parameters;
                }
            };
            Volley.newRequestQueue(mContext).add(jsonRequest);
            Volley.newRequestQueue(mContext).addRequestFinishedListener(new RequestQueue.RequestFinishedListener<String>() {
                @Override
                public void onRequestFinished(Request<String> request) {
                    String a = request.toString();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Clase generada para guardar en la bese de datos online la asistencia de la supervisora en un punto
     * de venta
     */
    private void ActualizaFechaSalidaAsistencia(String URL_WS, final List<classWebService> params, final int IdAsistenciaMarcada) {
        try {

            final SQLiteDatabase db = new SQLHelper(mContext).getWritableDatabase();

            StringRequest jsonRequest = new StringRequest(Request.Method.POST, URL_WS,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            JSONObject file_url = null;
                            try {
                                file_url = new JSONObject(response);

                                if (file_url == null) {
                                } else {
                                    int success = file_url.getInt("successInsert");
                                    if (success == 1) {

                                        ContentValues values = new ContentValues();
                                        values.put("EstadoSalidaEnviado", 1);
                                        int successUpdate = db.update("tblAsistencia", values, "IdAsistencia = '" + IdAsistenciaMarcada + "'", null);
                                        if (successUpdate > 0) {
                                        }

                                    }
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> parameters = new HashMap<String, String>();
                    for (int i = 0; i < params.size(); i++)
                        parameters.put(params.get(i).getParametro(), params.get(i).getValor());

                    return parameters;
                }
            };
            Volley.newRequestQueue(mContext).add(jsonRequest);
            Volley.newRequestQueue(mContext).addRequestFinishedListener(new RequestQueue.RequestFinishedListener<String>() {
                @Override
                public void onRequestFinished(Request<String> request) {
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //fin crear nueva asistencia

    /**
     * una veas comprobado que se solicita el numero de celular, se debe camvbiar el estado en
     * la base de daros en la nube
     */
    class ActualizarPedidoDeNumero extends AsyncTask<String, Void, Void> {

        JSONParser jParser = new JSONParser();

        @Override
        protected Void doInBackground(String... strings) {
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("listaParametros", strings[0]));
                params.add(new BasicNameValuePair("opcion", "ActualizaSolicitarNumeroCelular"));
                params.add(new BasicNameValuePair("format", "json"));
                jParser.makeHttpRequest(HOST_NAME + "ws/webservice_select.php", "POST", params);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return null;
        }
    }

}
