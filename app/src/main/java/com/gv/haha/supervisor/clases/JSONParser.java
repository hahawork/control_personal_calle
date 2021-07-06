package com.gv.haha.supervisor.clases;
/*
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by anupamchugh on 29/08/16.
 *
public class JSONParser {

    static InputStream is = null;
    static JSONObject jObj = null;
    static JSONArray jArr = null;
    static String json = "";
    static String error = "";

    // constructor
    public JSONParser() {

    }

    // function get json from url
    // by making HTTP POST or GET mehtod
    public JSONObject makeHttpRequest(String url, String method,
                                      ArrayList params) {

        // Making HTTP request
        try {

            // check for request method
            if(method.equals("POST")){
                // request method is POST
                // defaultHttpClient
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new UrlEncodedFormEntity(params));
                try {
                    Log.e("API123", " " +convertStreamToString(httpPost.getEntity().getContent()));
                    Log.e("API123",httpPost.getURI().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                HttpResponse httpResponse = httpClient.execute(httpPost);
                Log.e("API123",""+httpResponse.getStatusLine().getStatusCode());
                error= String.valueOf(httpResponse.getStatusLine().getStatusCode());
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();

            }else if(method.equals("GET")){
                // request method is GET
                DefaultHttpClient httpClient = new DefaultHttpClient();
                String paramString = URLEncodedUtils.format(params, "utf-8");
                url += "?" + paramString;
                HttpGet httpGet = new HttpGet(url);

                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
            Log.d("API123",json);
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try to parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
            jObj.put("error_code",error);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;

    }

    private String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        is.close();
        return sb.toString();
    }
}
*/

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import static com.gv.haha.supervisor.MainActivity.NotificadorTieneInternet;

public class JSONParser {

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";

    static JSONArray jarr = null;

    // constructor
    public JSONParser() {

    }

    // function get json from url
    // by making HTTP POST or GET mehtod
    public JSONObject makeHttpRequest(String url, String method,
                                      List<NameValuePair> params) {

        // Making HTTP request
        try {

            // check for request method
            if (method == "POST") {
                // request method is POST
                // defaultHttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new UrlEncodedFormEntity(params));

                try {
                    HttpResponse httpResponse = httpClient.execute(httpPost); // envia

                    HttpEntity httpEntity = httpResponse.getEntity();       // recupera
                    is = httpEntity.getContent();

                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                }

            } else if (method == "GET") {
                // request method is GET
                try {
                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    String paramString = URLEncodedUtils.format(params, "utf-8");
                    url += "?" + paramString;

                    /*HttpGet httpGet = new HttpGet(url);

                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    HttpEntity httpEntity = httpResponse.getEntity();
                    is = httpEntity.getContent();*/

                    URL url1 = new URL(url);
                    HttpURLConnection urlConnection = (HttpsURLConnection) url1.openConnection();
                    urlConnection.setReadTimeout(60000);
                    urlConnection.setConnectTimeout(60000);
                    urlConnection.setRequestMethod(method);
                    urlConnection.setDoInput(true);

                    urlConnection.connect();

                    is = urlConnection.getInputStream();

                } catch (UnknownHostException uhe) {
                    uhe.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
            jarr = new JSONArray(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data [" + e.toString() + "] " + json);

            try {
                if (json.length() > 0)
                    jObj = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));
            } catch (Exception e0) {

                Log.e("JSON Parser0", "Error parsing data0 [" + e0.getMessage() + "] " + json);
                Log.e("JSON Parser0", "Error parsing data0 " + e0.toString());
                e0.printStackTrace();
                try {
                    if (json.length() > 0)
                        jObj = new JSONObject(json.substring(1));
                } catch (Exception e1) {

                    Log.e("JSON Parser1", "Error parsing data1 [" + e1.getMessage() + "] " + json);
                    Log.e("JSON Parser1", "Error parsing data1 " + e1.toString());
                    e1.printStackTrace();
                    try {
                        jObj = new JSONObject(json.substring(2));
                    } catch (Exception e2) {

                        Log.e("JSON Parser2", "Error parsing data2 [" + e2.getMessage() + "] " + json);
                        Log.e("JSON Parser2", "Error parsing data2 " + e2.toString());
                        e2.printStackTrace();
                        try {
                            jObj = new JSONObject(json.substring(3));
                        } catch (Exception e3) {

                            Log.e("JSON Parser3", "Error parsing data3 [" + e3.getMessage() + "] " + json);
                            Log.e("JSON Parser3", "Error parsing data3 " + e3.toString());
                            e3.printStackTrace();
                        }
                    }
                }
            }

        }

        // return JSON String
        return jObj;
        //       return new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));

    }
}