package com.gv.haha.supervisor.clases;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;

import static com.gv.haha.supervisor.MainActivity.NotificadorTieneInternet;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.HOST_NAME;

public class receiverNetworkChange extends BroadcastReceiver {

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onReceive(final Context context, Intent intent) {
        try {
            if (isOnline(context)) {

                new AsyncTask<Void,Void,Boolean>(){
                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        try {
                            DefaultHttpClient httpClient = new DefaultHttpClient();
                            HttpGet httpGet = new HttpGet("https://www.grupovalor.com.ni/");
                            HttpResponse httpResponse = httpClient.execute(httpGet);
                            return httpResponse.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK;
                        } catch (UnknownHostException uhe) {
                            uhe.printStackTrace();
                        } catch (ClientProtocolException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return false;
                    }

                    @Override
                    protected void onPostExecute(Boolean aVoid) {
                        // your code here
                        super.onPostExecute(aVoid);

                        NotificadorTieneInternet(aVoid);


                        if (aVoid){

                            /*upload background upload service*/
                            Intent serviceIntent = new Intent(context, serviceEnviosPendientes.class);
                            context.startService(serviceIntent);

                            Log.w("Supervisor", "Online Connect Intenet ");

                        }

                    }
                };
            } else {

                NotificadorTieneInternet(false);

                Log.w("Supervisor", "Conectivity Failure !!! ");
            }
        } catch (NullPointerException e) {
            String s = e.getMessage();
            e.printStackTrace();
        }
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    private boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            //should check null because in airplane mode it will be null
            boolean conn = (netInfo != null && netInfo.isConnected());
           //boolean internet = isConnected();

            return (conn);

        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isConnected() throws InterruptedException, IOException {
        final String command = "ping -c 1 google.com";
        return Runtime.getRuntime().exec(command).waitFor() == 0;
    }
}
