package com.gv.haha.supervisor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.gv.haha.supervisor.clases.AsyncTaskComplete;
import com.gv.haha.supervisor.clases.SQLHelper;
import com.gv.haha.supervisor.clases.classCustomToast;
import com.gv.haha.supervisor.clases.classMetodosGenerales;
import com.gv.haha.supervisor.clases.classWebService;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.gv.haha.supervisor.clases.interfVariablesGenerales.TBL_CANAL_DISTRIBUCION;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.TBL_CLIENTES;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.URL_WS_GUARDAR_PDV_NUEVO;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.stClienteAsignadostr;

public class PuntosDeVenta extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, AsyncTaskComplete {

    //region Variables publicas de la clase
    //private static String url_create_product = "http://10.0.2.2/ad/create_pdv.php";  //localhost
    Spinner spnEmpresas, spnDeptos, spnTipoCanal;
    EditText etCiudad, etNombCanal, etNombPdV, etNombRepres, etTelefono;
    FloatingActionButton fab;
    Button btnGPS;
    boolean IsGPS = false;
    String Coordenadas, UserSave;

    SQLHelper sqlHelper;
    SQLiteDatabase db;
    String Fecha;
    SharedPreferences setting;
    classMetodosGenerales MG;


    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 10;

    private Location mLastLocation;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 1000; // 1 sec
    private static int FATEST_INTERVAL = 500; // 0.5 sec
    private static int DISPLACEMENT = 1; // 1 meters


    //endregion


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puntos_venta);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //region Instancias de clses y controles.

        setting = PreferenceManager.getDefaultSharedPreferences(this);
        db = new SQLHelper(this).getWritableDatabase();
        MG = new classMetodosGenerales(this);

        etCiudad = (EditText) findViewById(R.id.etCiudad_S);
        etNombCanal = (EditText) findViewById(R.id.etNombCanal_S);
        etNombPdV = (EditText) findViewById(R.id.etNombPdV_S);
        etNombRepres = (EditText) findViewById(R.id.etNombReprese_S);
        etTelefono = (EditText) findViewById(R.id.etTelefonoRepPdV_S);
        btnGPS = (Button) findViewById(R.id.btnGPS_s);

        Fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        UserSave = "" + setting.getInt("codUsuario", 0);

        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();

            createLocationRequest();
        }


        //endregion

        //region agregar lista con imagenes
        spnTipoCanal = (Spinner) findViewById(R.id.spnTipocanal);
        spnDeptos = (Spinner) findViewById(R.id.spnDeptos);
        spnEmpresas = (Spinner) findViewById(R.id.spnEmpresas_S);
        /*spnEmpresas.setAdapter(new Custom_Lista_Empresas(this, arrEmpresasNomb, arrEmpresasLogo));*/

        //endregion

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GuardarRegistro();
            }
        });

        btnGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayLocation();
                startLocationUpdates();
                // btnGPS.setText(ObtenerGPS());
            }
        });


        String[] list_depto = new String[]{"Boaco", "Carazo", "Chinandega", "Chontales", "Esteli", "Granada", "Jinotega", "Leon", "Madriz", "Managua", "Masaya", "Matagalpa", "Nueva Segovia", "RAAN", "RAAS", "Rio San Juan", "Rivas"};
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list_depto);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnDeptos.setAdapter(dataAdapter);

        //Obtiene la lista de clientes desde la base de datos
        Cursor cClientes = db.rawQuery("SELECT * FROM " + TBL_CLIENTES, null);
        List<String> arrClientes = new ArrayList<>();
        final List<Integer> arrClientesId = new ArrayList<>();
        if (cClientes.moveToFirst()) {
            do {
                arrClientes.add(cClientes.getString(cClientes.getColumnIndex("NombreCliente")));
                arrClientesId.add(cClientes.getInt(cClientes.getColumnIndex("IdCliente")));
            } while (cClientes.moveToNext());

            ArrayAdapter<String> daclientes = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, arrClientes);
            daclientes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnEmpresas.setAdapter(daclientes);
        }



        try {
            Cursor cCanal = db.rawQuery("SELECT * FROM " + TBL_CANAL_DISTRIBUCION, null);

            ArrayList <String> arrCanales=new ArrayList<>();

            if (cCanal.moveToFirst()) {
                do {
                    arrCanales.add(cCanal.getString(cCanal.getColumnIndex("NombreCanal")));

                } while (cCanal.moveToNext());

                ArrayAdapter<String> datipocanal = new ArrayAdapter<String>(PuntosDeVenta.this,
                        android.R.layout.simple_spinner_item, arrCanales);
                datipocanal.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spnTipoCanal.setAdapter(datipocanal);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int clienteasignado = Integer.parseInt(setting.getString(stClienteAsignadostr, "0"));
        if (clienteasignado == 0) {
            spnEmpresas.setEnabled(true);
        } else {
            spnEmpresas.setEnabled(false);
        }
        spnEmpresas.setSelection(clienteasignado - 1);
    }


    //region metodos del gps
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();

        try {
            // Resuming the periodic location updates
            if (mGoogleApiClient.isConnected()) {
                startLocationUpdates();
                displayLocation();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            stopLocationUpdates();
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    /**
     * Method to display the location on UI
     */
    public Location displayLocation() {

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return mLastLocation;
            }
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLastLocation != null) {
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();

                btnGPS.setText(latitude + ", " + longitude);
                Coordenadas = latitude + "," + longitude;
                IsGPS = true;

                return mLastLocation;
            } else {

                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    showSettingsAlert();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mLastLocation;
    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();

            mGoogleApiClient.connect();
        } else {
            mGoogleApiClient.connect();
        }

    }

    /**
     * Creating location request object
     */
    @SuppressLint("RestrictedApi")
    protected void createLocationRequest() {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                // new VariablesGlobales().Toast(this, "Dispositivo no soporta.", R.drawable.ic_error);
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Starting the location updates
     */
    protected void startLocationUpdates() {

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } catch (Exception e) {
            // new VariablesGlobales().Toast(this, "Error relacionado con los servicios de google services.", R.drawable.ic_error);
            e.printStackTrace();
        }
    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Conexión Fallida: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        // Once connected with google api, get the location
        displayLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

        // Displaying the new location on UI
        displayLocation();
    }

    //endregion

    private void GuardarRegistro() {

        etCiudad.setError(null);
        etNombCanal.setError(null);
        etNombPdV.setError(null);
        etNombRepres.setError(null);
        etTelefono.setError(null);

        View focus = null;
        boolean ok = true;

        if (TextUtils.isEmpty(etCiudad.getText())) {
            etCiudad.setError("Por favor llene con la ciudad actual");
            focus = etCiudad;
            ok = false;
        }
        if (TextUtils.isEmpty(etNombCanal.getText())) {
            etNombCanal.setError("Por favor llene con un canal valido");
            focus = etNombCanal;
            ok = false;
        }
        if (TextUtils.isEmpty(etNombPdV.getText())) {
            etNombPdV.setError("Por favor llene con el nombre del punto de venta.");
            focus = etNombPdV;
            ok = false;
        }
        if (TextUtils.isEmpty(etNombRepres.getText())) {
            etNombRepres.setError("Por favor llene con el nombre del administrador del local.");
            focus = etNombRepres;
            ok = false;
        }
        if (TextUtils.isEmpty(etTelefono.getText())) {
            etTelefono.setError("Por favor llene con ceros(0) si no lo tiene.");
            focus = etTelefono;
            ok = false;
        }

        if (!IsGPS) {
            ok = false;
            new classCustomToast(this).Show_ToastError("Debe Obtener las coordenadas");
            focus = btnGPS;
            checkPlayServices();
        }

        if (ok) {

            String Cliente = spnEmpresas.getSelectedItem().toString();
            String TipoCanal = spnTipoCanal.getSelectedItem().toString();
            String Departamento = (spnDeptos.getSelectedItemPosition() + 1) + "";
            String Ciudad = etCiudad.getText().toString();
            String NombreCanal = etNombCanal.getText().toString();
            String NombrePdV = etNombPdV.getText().toString();
            String NombreReprPdV = etNombRepres.getText().toString();
            String TelefonoReprPdV = etTelefono.getText().toString();
            String LocationGPS = Coordenadas;
            String UserSave = "" + setting.getInt("codUsuario", 0);

            if (new classMetodosGenerales(this).TieneConexion()) {

                new classCustomToast(this).Toast("Enviando los datos, espere....", R.drawable.ic_info);

                //se manda a ejecutar el web service
                //parametros para enviar al web service
                ArrayList<classWebService> parametros = new ArrayList<>();
                parametros.add(new classWebService("Cliente", Cliente));
                parametros.add(new classWebService("TipoCanal", TipoCanal));
                parametros.add(new classWebService("Departamento", Departamento));
                parametros.add(new classWebService("Ciudad", Ciudad));
                parametros.add(new classWebService("NombreCanal", NombreCanal));
                parametros.add(new classWebService("NombrePdV", NombrePdV));
                parametros.add(new classWebService("NombreReprPdV", NombreReprPdV));
                parametros.add(new classWebService("TelefonoReprPdV", TelefonoReprPdV));
                parametros.add(new classWebService("LocationGPS", LocationGPS));
                parametros.add(new classWebService("UserSave", UserSave));
                parametros.add(new classWebService("fechaSave", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                // el resultado esta en el metodo onTaskRegistroComplete
                MG.webServicePost(URL_WS_GUARDAR_PDV_NUEVO, parametros, AsyncTaskComplete.option_guardar_pdv);
            } else {
                new classCustomToast(this).Show_ToastError("No tienes conexión a internet");
            }
            stopLocationUpdates();
        }

    }

    private void LimpiarControles() {

        etCiudad.setText("");
        etNombCanal.setText("");
        etNombPdV.setText("");
        etNombRepres.setText("");
        etTelefono.setText("");
        btnGPS.setText("Obtener Coordenadas");
        IsGPS = false;

    }

    /*
     *funcion para mostrar alerta si el GPS no esta activado y enviarlo a la
     * configuracion
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("El GPS no está activado.");
        alertDialog.setMessage("¿Deseas Activar el GPS de tu dispositivo?");
        alertDialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onAsyncTaskComplete(JSONObject result, int option) {
        if (option == option_guardar_pdv) {
            //si no hay respuesta del servidor
            if (result == null ) {
                new classCustomToast(this).Show_ToastError("Error al guardar, favor verifica tu conexion a internet");
            } else {
                try {
                    int success = result.getInt("success");
                    String message = result.getString("message");
                    if (success == 1) {
                        LimpiarControles();
                        new classCustomToast(this).Toast(message, R.drawable.ic_success);
                    } else {
                        new classCustomToast(this).Show_ToastError(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }

    }

}