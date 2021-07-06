package com.gv.haha.supervisor.clases;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;

import static com.gv.haha.supervisor.clases.interfVariablesGenerales.PERMITIDO_GUARDAR_PDV;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.gACTIVOCUENTAMOCKLOCATION;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.gACTIVOGUARDARPDV;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.gAPP_INDICADOR;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.gCLEAR_DATA_APP;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.gGETCURRENTPOSITION;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.gNUMERO_PERMITIDO;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.gNUMERO_PERMITIDO2;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.gSEPARADOR_COMANDO;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.stMockLocation;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.stSolicitaUbicaSMS;

public class SMSConfigReceiver extends BroadcastReceiver {

    SharedPreferences setting;

    @Override
    public void onReceive(Context context, Intent intent) {

        setting = PreferenceManager.getDefaultSharedPreferences(context);

        try {
            // Get Bundle object contained in the SMS intent passed in
            Bundle bundle = intent.getExtras();
            SmsMessage[] smsm = null;
            String sms_str = "";
            if (bundle != null) {
                // Get the SMS message
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    smsm = new SmsMessage[pdus.length];
                    String numero = "";
                    for (int i = 0; i < smsm.length; i++) {
                        smsm[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        numero = smsm[i].getOriginatingAddress();
                        //sms_str += "Sent From: " + smsm[i].getOriginatingAddress();
                        //sms_str += "\r\nMessage: ";
                        sms_str += smsm[i].getMessageBody().toString();
                        //sms_str += "\r\n";
                    }

                    Log.w("SMS", sms_str);


                    if (numero.contains(gNUMERO_PERMITIDO) || numero.contains(gNUMERO_PERMITIDO2)) {
                        String[] partesSMS = sms_str.split("/");

                        if (partesSMS.length > 1) {

                            Log.w("Partes del sms ", partesSMS.length + "");
/*
                    //Sent From: +50582442533 Message: App_gv
                    String[] parte1= partesSMS[0].split(":");
                    // [0]= sent form
                    // [1]= +505xxxxxxxx Message
                    // [2]= primera partwe del comando
*/
                            if (partesSMS[0].equalsIgnoreCase(gAPP_INDICADOR)) {
                                // Start Application's  MainActivty activity
                                Log.w("parte 1", partesSMS[0]);
                                Log.w("parte 2", partesSMS[1]);

                                try {
                                    String Comandos[] = partesSMS[1].split(gSEPARADOR_COMANDO);

                                    if (Comandos[0].equalsIgnoreCase(gCLEAR_DATA_APP)) {
                                        new classMetodosGenerales(context).clearData();
                                    }
                                    if (Comandos[0].equalsIgnoreCase(gGETCURRENTPOSITION)) {

                                        SharedPreferences.Editor editor = setting.edit();
                                        editor.putBoolean(stSolicitaUbicaSMS,true);
                                        editor.commit();

                                    }
                                    if (Comandos[0].equalsIgnoreCase(gACTIVOGUARDARPDV)) {

                                        SharedPreferences.Editor editor = setting.edit();
                                        editor.putBoolean(PERMITIDO_GUARDAR_PDV, true);
                                        //si viene la cantidad de pdv permitidos a guardar
                                        if (Comandos.length > 1)
                                            editor.putInt("stCantidadPdvPermitido", Integer.parseInt(Comandos[1]));

                                        editor.commit();
                                    }
                                    if (Comandos[0].equalsIgnoreCase(gACTIVOCUENTAMOCKLOCATION)) {
                                        SharedPreferences.Editor editor = setting.edit();
                                        editor.putInt(stMockLocation, 0);
                                        editor.commit();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                /*
                                Intent smsIntent = new Intent(context, MainActivity.class);
                                smsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                smsIntent.putExtra("sms_str", partesSMS[1]);
                                context.startActivity(smsIntent);*/

                                abortBroadcast();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
