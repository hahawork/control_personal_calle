package com.gv.haha.supervisor.clases;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gv.haha.supervisor.R;

import net.gotev.uploadservice.Placeholders;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import static com.gv.haha.supervisor.clases.interfVariablesGenerales.HOST_NAME;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.nombUsuario;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.stCorreoUser;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.stUrlFotoUser;


public class InfoUsuario {

    private static SharedPreferences setting;

    private static Context mcontext;

    public InfoUsuario(Context mcontext) {
        this.mcontext = mcontext;
        setting = PreferenceManager.getDefaultSharedPreferences(mcontext);

    }

    public InfoUsuario SetNombre(TextView tvNombre) {
        tvNombre.setText(setting.getString(nombUsuario, ""));
        return this;
    }

    public InfoUsuario SetCorreo(TextView tvCorreo) {
        tvCorreo.setText(setting.getString(stCorreoUser, "Correo no disponible"));
        return this;
    }

    public InfoUsuario SetFotoPerfil(ImageView ivFoto) {

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/grupovalor/Perfil_usuario.jpg");
        if (file.exists()) {

            Glide.with(mcontext).load(file).into(ivFoto);

        } else {

            String urlfoto = setting.getString(stUrlFotoUser, "");
            if (urlfoto.length() > 0) {
                DescargaFotoPerfil(HOST_NAME + urlfoto, ivFoto);
            }
        }

        return this;
    }

    @SuppressLint("StaticFieldLeak")
    private void DescargaFotoPerfil(final String URL, ImageView ivFoto) {

        try {
            if (new classMetodosGenerales(mcontext).TieneConexion()) {

                Glide.with(mcontext).load(URL).into(ivFoto);

                new AsyncTask<String, Void, Void>() {

                    @Override
                    protected Void doInBackground(String... strings) {
                        int count;
                        try {

                            URL url = new URL(URL);
                            URLConnection conection = url.openConnection();
                            conection.connect();
                            int lenghtOfFile = conection.getContentLength();

                            InputStream input = new BufferedInputStream(url.openStream(), 8192);

                            String ruta = Environment.getExternalStorageDirectory().getAbsolutePath() + "/grupovalor/";
                            File file = new File(ruta);
                            if (!file.exists()) {
                                file.mkdirs();
                            }

                            OutputStream output = new FileOutputStream(ruta + "Perfil_usuario.jpg");
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

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.execute();

            } else {

            }
        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }
    }
}
