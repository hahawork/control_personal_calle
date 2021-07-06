package com.gv.haha.supervisor;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gv.haha.supervisor.clases.classCustomToast;
import com.gv.haha.supervisor.clases.classMetodosGenerales;
import com.gv.haha.supervisor.clases.classWebService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.GET_ACCOUNTS;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.URL_WS_REGISTRO;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.codUsuario;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.nombUsuario;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.stClienteAsignadostr;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.stCorreoUser;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.stUrlFotoUser;

public class Registro extends AppCompatActivity implements View.OnClickListener {

    private static EditText emailid, telefono;
    private static Button loginButton;
    private static LinearLayout loginLayout;
    private static Animation shakeAnimation;
    classMetodosGenerales MG;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        iniciarToolBar();

        MG = new classMetodosGenerales(this);
        emailid = (EditText) findViewById(R.id.login_emailid);
        telefono = (EditText) findViewById(R.id.login_password);
        loginButton = (Button) findViewById(R.id.loginBtn);
        loginLayout = (LinearLayout) findViewById(R.id.login_layout);

        // Load ShakeAnimation
        shakeAnimation = AnimationUtils.loadAnimation(this,
                R.anim.shake);

        EnableRuntimePermission();
        emailid.setText(getUserEmail());

        setListeners();

        telefono.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //si hay texto en el campo celular
                if (count>0){
                    emailid.setText("");
                }
                //si no hay texto vuelve a obtener el correo del celular
                else{
                    getUserEmail();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        emailid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count>0){
                    telefono.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void iniciarToolBar() {
        try {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Set Listeners
    private void setListeners() {
        loginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginBtn:
                checkValidation();
                break;
        }

    }

    // Check Validation before login
    private void checkValidation() {

        boolean isOK = true;
        String mensaje = "";

        // Get email id and password
        String getEmailId = emailid.getText().toString();
        String getTelefono = telefono.getText().toString();

        //si el campo de correo o el campo del telefono esrtan vacios
        if (TextUtils.isEmpty(emailid.getText()) && TextUtils.isEmpty(telefono.getText())) {

            isOK = false;
            mensaje = "Favor ingrese sus datos.";
        }

        // si el campo de correo no esta vacio y
        // verifica si el correo no es  valido
        boolean formato_correo = android.util.Patterns.EMAIL_ADDRESS.matcher(getEmailId).matches();
        if (!TextUtils.isEmpty(emailid.getText()) && !formato_correo) {
            isOK = false;
            mensaje = "El formato de correo no es aceptado.";
        }

        if (isOK) {

            //parametros para enviar al web service
            ArrayList<classWebService> parametros = new ArrayList<>();
            parametros.add(new classWebService("EmailUsuario", getEmailId));
            parametros.add(new classWebService("TelefUsuario", getTelefono));

            //se manda a ejecutar el web service
            // el resultado esta en el metodo onTaskRegistroComplete
            webServicePost(URL_WS_REGISTRO, parametros);


        } else {

            loginLayout.startAnimation(shakeAnimation);
            new classCustomToast(this).Show_ToastError(mensaje);
        }

    }

    public void webServicePost(String URL_WS, final List<classWebService> params) {

        //parametros para enviar al web service

        try {
            StringRequest jsonRequest = new StringRequest(Request.Method.POST, URL_WS,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            JSONObject result = null;
                            try {
                                result = new JSONObject(response);
                                SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(Registro.this);
                                SharedPreferences.Editor editor = setting.edit();

                                if (result == null) {

                                    new AlertDialog.Builder(Registro.this).setTitle("Error al Obtener.").setMessage("Sin repuesta del servidor, o revisa la conexión de datos.").setIcon(R.drawable.error).show();

                                } else {
                                    int success = 0;
                                    try {
                                        success = result.getInt("success");

                                        if (success == 1) {

                                            editor.putInt(codUsuario, result.getInt("MaxIdUsuario"));
                                            editor.putString(stClienteAsignadostr, result.getString("IdCliente"));
                                            editor.putString(nombUsuario, result.getString("NombreUsuario"));
                                            editor.putString(stCorreoUser, result.getString("EmailUsuario"));
                                            editor.putString(stUrlFotoUser, result.getString("FotoUsuario"));
                                            editor.commit();


                                            new classCustomToast(Registro.this).Toast(result.getString("message"), R.drawable.ic_success);

                                            startActivity(new Intent(Registro.this, MainActivity.class));

                                        } else {

                                            new AlertDialog.Builder(Registro.this).setTitle("Error al Recuperar.").setMessage(result.getString("message")).setIcon(R.drawable.error).show();

                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    new classCustomToast(Registro.this).Show_ToastError("Error en la consulta al ws: " + error.getMessage());
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
            Volley.newRequestQueue(this).add(jsonRequest);
            Volley.newRequestQueue(this).addRequestFinishedListener(new RequestQueue.RequestFinishedListener<String>() {
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


    public void EnableRuntimePermission() {

        ActivityCompat.requestPermissions(this, new String[]
                {
                        GET_ACCOUNTS/*,
                        READ_CONTACTS,
                        READ_PHONE_STATE,
                        READ_EXTERNAL_STORAGE,
                        WRITE_EXTERNAL_STORAGE*/
                }, 1);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    boolean GetAccountPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (GetAccountPermission) {
                        emailid.setText(getUserEmail());
                    }
                }
                break;
        }
    }

    public String getUserEmail() {

        if (ActivityCompat.checkSelfPermission(this, GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, GET_ACCOUNTS)) {

            } else {

                ActivityCompat.requestPermissions(this, new String[]{GET_ACCOUNTS}, 1);

            }

        } else {
            AccountManager manager = AccountManager.get(this);
            Account[] accounts = manager.getAccountsByType("com.google");
            List<String> possibleEmails = new LinkedList<String>();

            for (Account account : accounts) {
                // TODO: Check possibleEmail against an email regex or treat
                // account.name as an email address only for certain account.type values.
                possibleEmails.add(account.name);
            }

            if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null) {
                String email = possibleEmails.get(0);
                String[] parts = email.split("@");

                if (parts.length > 1)
                    return email;
                else
                    return "";
            }
        }
        return "";

    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }
}
