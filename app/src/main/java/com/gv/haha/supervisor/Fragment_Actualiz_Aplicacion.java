package com.gv.haha.supervisor;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gv.haha.supervisor.clases.classCustomToast;
import com.gv.haha.supervisor.clases.classMetodosGenerales;
import com.gv.haha.supervisor.clases.interfVariablesGenerales;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import static com.gv.haha.supervisor.clases.interfVariablesGenerales.HOST_NAME;

public class Fragment_Actualiz_Aplicacion extends Fragment implements View.OnClickListener {

    SharedPreferences setting;
    Button btnActualizar;
    classMetodosGenerales cmg;

    public Fragment_Actualiz_Aplicacion() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_actualiz_aplicacion, container, false);
        setting = PreferenceManager.getDefaultSharedPreferences(getContext());
        cmg = new classMetodosGenerales(getContext());

        btnActualizar = (Button) view.findViewById(R.id.btnActualizarApp_A);
        btnActualizar.setOnClickListener(this);

        return view;
    }

    public static Fragment_Actualiz_Aplicacion newInstance(int Posic) {
        Fragment_Actualiz_Aplicacion myFragment = new Fragment_Actualiz_Aplicacion();
        Bundle args = new Bundle();
        args.putInt("Parametro", Posic);
        myFragment.setArguments(args);

        return myFragment;
    }

    @Override
    public void onClick(View v) {

        if (v == btnActualizar) {

            new DescargarArchivo().execute(HOST_NAME+"ad/appgps.apk");

        }
    }

    public void Instalar() {

        SharedPreferences.Editor editor = setting.edit();
        editor.putInt("appversion", 0);
        editor.apply();

        File app = new File(Environment.getExternalStorageDirectory() + "/grupovalor/grupovalor.apk");
        if (app.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(app), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            getActivity().finish();
        }
    }


    ProgressDialog pDialog;

    public class DescargarArchivo extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getContext());
            pDialog.setMessage("Descargando la nueva versión, por favor espere...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    new classCustomToast(getActivity()).Toast("Se descargará en segundo plano.", R.drawable.ic_info);
                }
            });
            pDialog.setMax(100);
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                if (cmg.TieneConexion()) {
                    URL url = new URL(f_url[0]);
                    URLConnection conection = url.openConnection();
                    conection.connect();
                    int lenghtOfFile = conection.getContentLength();
                    InputStream input = new BufferedInputStream(url.openStream(), 8192);

                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/grupovalor/");
                    if (!file.exists()) {
                        file.mkdirs();
                    }

                    OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/grupovalor/grupovalor.apk");
                    byte data[] = new byte[1024];
                    long total = 0;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        publishProgress("" + (int) ((total * 100) / lenghtOfFile));
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

            return "";
        }

        protected void onProgressUpdate(String... progress) {
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String file_url) {
            pDialog.dismiss();

            Instalar();

        }

    }
}
