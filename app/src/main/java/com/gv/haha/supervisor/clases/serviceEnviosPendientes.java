package com.gv.haha.supervisor.clases;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gv.haha.supervisor.AsistenciasPendientesEnvio;
import com.gv.haha.supervisor.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.gv.haha.supervisor.clases.interfVariablesGenerales.TBL_ASISTENCIA;

public class serviceEnviosPendientes extends Service {

    SQLiteDatabase db;
    SharedPreferences setting;
    int IdEnviado;
    boolean HaySalidaMarcada = false;
    classMetodosGenerales cmg;
    String TAG = "serviceEnviosPendientes";

    public serviceEnviosPendientes() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @SuppressWarnings("static-access")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            setting = PreferenceManager.getDefaultSharedPreferences(this);

            if (isOnline()) {
                Log.d(TAG, "Hay internet, se procede a enviar");
                ObtenerEnviarVisitasPendienesdeEnvios();
            } else {
                Log.d(TAG, "No hay internet, detener el servicio.");
                stopSelf();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return START_NOT_STICKY;
    }

    private void ObtenerEnviarVisitasPendienesdeEnvios() {
        try {

            cmg = new classMetodosGenerales(this);

            db = new SQLHelper(this).getWritableDatabase();

            Cursor cAsistenc = db.rawQuery("SELECT * FROM " + TBL_ASISTENCIA + " INNER JOIN puntosdeventaAsistencia ON IdPdV = Pdv WHERE ESTADO = '0' or EstadoSalidaEnviado = '0'", null);

            if (cAsistenc.getCount() > 0) {

                ////para mostrar notificacion de que se enviara automaticamente
                pushNotificacion(cAsistenc.getCount(), "REPORTE DE ASISTENCIA", 1000);

                for (cAsistenc.moveToFirst(); !cAsistenc.isAfterLast(); cAsistenc.moveToNext()) {


                    EnviosPendientes enviosPendientes = new EnviosPendientes(
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
                            //Tipo = -1 sino hay salida, 0 si hay salida pero no se ha enviado
                            cAsistenc.getString(cAsistenc.getColumnIndex("EstadoSalidaEnviado")));

                    try {
                        cmg.crearNuevaAsistencia(enviosPendientes);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                //cuando sale del ciclo de envios pendientes se detiene el servicio
                stopSelf();

            } else {
                Log.d(TAG, "no hay envios pendientes, detener el servicio");
                //si no hay pendientes sd detiene el servicio
                stopSelf();
            }
        } catch (Exception e) {
            //si ocurre un error.
            stopSelf();
            e.printStackTrace();
        }
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void pushNotificacion(int cantidad, String tipoenvio, int idnotificacion) {

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = getBigTextStyle(new Notification.Builder(this), cantidad, tipoenvio);
        notificationManager.notify(idnotificacion, notification);

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private Notification getBigTextStyle(Notification.Builder builder, int cantidad, String tipoenvio) {

        Intent intent = new Intent(this, AsistenciasPendientesEnvio.class)
                .putExtra("NotificationMessage", 12).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, 15, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Sonido por defecto de notificaciones, podemos usar otro
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String setContentTitle = "Se detectó conexión a Internet",
                setContentText = "y tenias " + cantidad + " " + tipoenvio + " pendientes de envio. Se enviará automaticamente, no requiere ninguna acción por parte del usuario.";
        builder
                .setContentTitle(setContentTitle)
                .setContentText(setContentText)
                .setContentInfo("Supervisión")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(defaultSound)
                .setLights(Color.BLUE, 1, 0)
                .setAutoCancel(true);
        // .addAction(android.R.drawable.ic_menu_view, "Abrir App.", pIntent);

        return new Notification.BigTextStyle(builder)
                .setBigContentTitle(setContentTitle)
                .bigText(setContentText)
                .setSummaryText("MARKETING ONE")
                .build();
    }
}