package com.gv.haha.supervisor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.gv.haha.supervisor.clases.AsyncTaskComplete;
import com.gv.haha.supervisor.clases.Deptos;
import com.gv.haha.supervisor.clases.EnviosPendientes;
import com.gv.haha.supervisor.clases.InfoUsuario;
import com.gv.haha.supervisor.clases.SQLHelper;
import com.gv.haha.supervisor.clases.classBottomNavigationBehavior;
import com.gv.haha.supervisor.clases.classCustomToast;
import com.gv.haha.supervisor.clases.classDetectMockLocation;
import com.gv.haha.supervisor.clases.classMetodosGenerales;
import com.gv.haha.supervisor.clases.classPdv;
import com.gv.haha.supervisor.clases.classWebService;
import com.gv.haha.supervisor.clases.receiverNetworkChange;
import com.rom4ek.arcnavigationview.ArcNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.gv.haha.supervisor.clases.interfVariablesGenerales.PERMITIDO_GUARDAR_PDV;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.TBL_ASISTENCIA;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.TBL_CLIENTES;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.TBL_PDV_ASISTENCIA;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.URL_WS_MULTIPLE_OPCIONES;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.codUsuario;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.nombUsuario;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.stMockLocation;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.stradiopdv;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, android.location.LocationListener, NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback, AsyncTaskComplete, BottomNavigationView.OnNavigationItemSelectedListener {

    final int REQUEST_CODE_CAMERA_PICTURE = 1,
            REQUEST_CODE_ASK_PERMISSIONS_GPS = 3,
            REQUEST_CODE_ASK_PERMISSIONS_READ_EXTERNAL_STORAGE = 6;
    Boolean SOLICITA_UBICACION_SMS = false;
    Toolbar toolbar, mToolbarBottom;
    GoogleMap googleMap;
    SupportMapFragment mapFragment = null;
    static Uri ImagenTomadaCamara = null;
    static int CadaVezQueAbreLaApp = 0;
    private ProgressDialog pDialog;
    TextView tvPosicion, tvObtenUbic, tvNombre, tvCorreo;
    ImageView ivFoto;
    ArrayList<classPdv> arrPdv = new ArrayList<>();
    Context mContext;
    Activity mActivity;
    EditText etFechEntrada, etCostoTransp, etCostoTranspTaxi, etCostoAlim, etCostoHosped, etCostoVario, etKmActual, etComentario;
    Button btnMarcarEntrada, btnMarcarSalida, ibPhoto, btnObten_ubic;
    Spinner spnpdvent;
    boolean isGPS = false, isEventoEspecial = false, sesionActiva;
    static int gcodUsuario = 0, IdAsistenciaMarcada, gcodPdV = 0;
    float ZoomActual = 17f;
    double LatActual = 0f, LongActual = 0f;
    Float intDistPdVMasCercvano = 1000f;
    SharedPreferences setting;
    private static final String TAG = MainActivity.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocationMap;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    PendingResult<LocationSettingsResult> result;
    LocationManager locationManager;
    SQLHelper sqlHelper;
    SQLiteDatabase db;
    int SEGUNDOS_BUSCANDO_COORDENADAS = 0;


    private BroadcastReceiver mNetworkReceiver;

    static TextView tv_check_connection;

    classMetodosGenerales CMG;
    private Handler customHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iniciarToolBar();

        mContext = this;
        mActivity = this;
        setting = PreferenceManager.getDefaultSharedPreferences(this);
        gcodUsuario = setting.getInt("codUsuario", 0);

        verificarRegistro();

    }

    /**
     * Metodo para iniciar el toolbar
     */
    private void iniciarToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ArcNavigationView navigationView1=(ArcNavigationView)findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);
        ivFoto = (ImageView) headerView.findViewById(R.id.iv_foto_usuario_MA);
        tvNombre = (TextView) headerView.findViewById(R.id.tv_nombre_usuario_MA);
        tvCorreo = (TextView) headerView.findViewById(R.id.tv_correo_usuario_MA);
    }

    /**
     * verifica si existe un usuario habilitado
     */
    private void verificarRegistro() {
        try {

            CMG = new classMetodosGenerales(this);
            CMG.EnableRuntimePermission();

            //getExtras();

            //si el usuario esta vacio o el  codigo de usuario
            if (setting.getString(getResources().getString(R.string.nombUsuario), "").equals("") ||
                    setting.getInt(getResources().getString(R.string.codUsuario), 0) == 0) {

                startActivity(new Intent(this, Registro.class));

                // else { Si la cuenta esta bloqueada por usar simulador de ubicacion gps
            } else if (setting.getInt(getResources().getString(R.string.stMockLocation), 0) == 1) {

                startActivity(new Intent(this, MockLocationMessage.class));
                this.finish();

            } else {

                getExtras();

                iniciarComponentes();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Metodo para iniciar variables y elementos de la interfaz
     */
    private void iniciarComponentes() {
        try {

            sqlHelper = new SQLHelper(this);
            db = sqlHelper.getWritableDatabase();

            etFechEntrada = (EditText) findViewById(R.id.etFechHoraEnt_MA);
            etCostoTransp = (EditText) findViewById(R.id.etGastosMovim_MA);
            etCostoTranspTaxi = (EditText) findViewById(R.id.etGastosMovTaxi_MA);
            etCostoAlim = (EditText) findViewById(R.id.etGastosAlim_MA);
            etCostoHosped = (EditText) findViewById(R.id.etGastosHosped_MA);
            etCostoVario = (EditText) findViewById(R.id.etGastosVario_MA);
            etKmActual = (EditText) findViewById(R.id.etKmActual_MA);
            etComentario = (EditText) findViewById(R.id.etComentario_MA);
            spnpdvent = (Spinner) findViewById(R.id.spn_pdvent_Ma);
            tvPosicion = (TextView) findViewById(R.id.tvPosicion_Ma);
            btnMarcarEntrada = (Button) findViewById(R.id.btnMarcarEntrada_MA);
            btnMarcarSalida = (Button) findViewById(R.id.btnMarcarSalida_MA);
            ibPhoto = (Button) findViewById(R.id.ib_photo_MA);
            btnObten_ubic = (Button) findViewById(R.id.btngetMyLocatio_MA);

            gcodUsuario = setting.getInt(codUsuario, 0);
            //esto es para mostrar el saludo cada vez que abre la app
            if (CadaVezQueAbreLaApp == 0) {
                new classCustomToast(this).Toast("Hola, " + setting.getString(nombUsuario, ""), R.drawable.ic_info);
            }
            new InfoUsuario(this).SetNombre(tvNombre).SetCorreo(tvCorreo).SetFotoPerfil(ivFoto);

            BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
            navigation.setOnNavigationItemSelectedListener(this);

            // attaching bottom sheet behaviour - hide / show on scroll
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) navigation.getLayoutParams();
            layoutParams.setBehavior(new classBottomNavigationBehavior());


            // paRA EL mensajito de activar el gps desde la panalla principal
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(AppIndex.API)
                    .addApi(LocationServices.API)
                    .build();


            //verifica los servicios de google play
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
            //inicializa el mapa
            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            tv_check_connection = (TextView) findViewById(R.id.tv_check_connection);


            new Handler().postDelayed(new Runnable() {
                public void run() {
                    mNetworkReceiver = new receiverNetworkChange();
                    registerNetworkBroadcastForNougat();
                }
            }, 5000);

            if (CMG.TieneConexion()) {

                if (CadaVezQueAbreLaApp == 0) {
                    //ServicioEnviosPendientesAutomatico();
                    //EnviarUbicacionActualServidor();
                    //*******************************************************************************
                    // verifica si existe una version mas actualizada  de la aplicaciion
                    CMG.VerificarNuevaVersion();
                    CMG.VerificarNuevoMensaje(gcodUsuario);
                    CMG.VerificarSolicitudDeNumero();
                    //si esta false el guardar nuevo pdv
                    if (setting.getBoolean(PERMITIDO_GUARDAR_PDV, false) == false) {
                        CMG.VerificaPermisoGuardarPdvNevo("VerificarPermisoGuardarNuevoPdV");

                        CMG.UpdateVerificarPermisoGuardarNuevoPdV();
                    }

                    VerificarNuevosPuntosguardados(0);
                    VerificarClientes();
                    verificaUsoUbicacionSimulada();
                    CadaVezQueAbreLaApp = 1;
                }
            }

            MuestraOcultaControlesSegunconfiguracionUsuario();
            VerificaAsistenciaActiva();

            //llamada al metodo que carga los eventos
            EventosControles();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //region Actualizador de fecha y hora
    private Runnable updateTimerThread = new Runnable() {
        public void run() {

            try {
                etFechEntrada.setText(new SimpleDateFormat("dd MMM yyyy hh:mm:ss a").format(new Date()));

                ((TextView) findViewById(R.id.tvObteniendoCoordenada_MA)).setText(String.format("Obteniendo Coordenadas... (%s seg.)", SEGUNDOS_BUSCANDO_COORDENADAS));

                SEGUNDOS_BUSCANDO_COORDENADAS++;

                if (SEGUNDOS_BUSCANDO_COORDENADAS % 10 == 0) {

                    //si han pasado mas de 30 seg. verifica de nuevo si el gps esta activado
                    // si no esta lo manda a activar de la otra manera.
                    //showSettingsAlert(2);
                    if (!isGPS) {
                        showSettingsAlert(1);
                    }
                    GPSFisico();

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            customHandler.postDelayed(this, 1000);
        }
    };
    //endregion

    private void EventosControles() {

        // paRA EL mensajito de activar el gps desde la panalla principal
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(AppIndex.API)
                .addApi(LocationServices.API)
                .build();


        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }


        ((Button) findViewById(R.id.btngetMyLocatio_MA)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getmyPosition();
            }
        });

        ibPhoto.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {

               /* if (ContextCompat.checkSelfPermission(mContext, CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    if (shouldShowRequestPermissionRationale(CAMERA)) {

                        showMessageOKCancelar("Por favor permitir el uso de la cámara para tomar fotografias.", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestPermissions(new String[]{CAMERA}, REQUEST_CODE_CAMERA_PICTURE);
                            }
                        });

                        return;
                    }

                }*/
                if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(mActivity, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                } else {

                    startActivity(new Intent(mContext, Camara.class));

                    /*final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "grupovalor/imgtmp" + File.separator);
                    if (!root.exists())
                        root.mkdirs();

                    final String fname = setting.getString("strPhotoName", "") + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".jpg";

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    ImagenTomadaCamara = Uri.fromFile(new File(root.getPath() + File.separator + fname));

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, ImagenTomadaCamara);
                    startActivityForResult(intent, REQUEST_CODE_CAMERA_PICTURE);*/
                }
            }
        });

        spnpdvent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gcodPdV = arrPdv.get(position).getIdPdv();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnMarcarEntrada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sesionActiva) {
                    MarcarEntrada();
                }
            }
        });
        btnMarcarSalida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sesionActiva) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Asistencia Activa.");
                    builder.setMessage("¿Qué desea hacer?");
                    builder.setCancelable(false);
                    builder.setNegativeButton("No Salir!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.setPositiveButton("Marcar Salida", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MarcarSalida();
                        }
                    });
                    builder.create();
                    builder.show();
                }
            }
        });

        spnpdvent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                isEventoEspecial = arrPdv.get(i).getIdPdv() == 29 ? true : false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        etComentario.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    MarcarEntrada();
                }
                return false;
            }
        });


        tvPosicion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGPS) {
                    StringBuilder compartir = new StringBuilder("Hola, soy: ");
                    compartir.append(setting.getString(getResources().getString(R.string.nombUsuario), ""));
                    compartir.append(" id: " + gcodUsuario);
                    compartir.append(" Coordenadas: " + tvPosicion.getText());
                    CMG.CompartirTexto(compartir.toString());
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    this.finishAffinity();
                } else {
                    this.finish();
                }
                System.exit(0);
                android.os.Process.killProcess(android.os.Process.myPid());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.ib_photo_pendentes) {

            startActivity(new Intent(mContext, FotosPendientesEnvio.class));

        } else if (id == R.id.action_descarg_pdv) {

            VerificarNuevosPuntosguardados(1);

        } else if (id == R.id.action_enviarpendient) {

            startActivity(new Intent(mContext, AsistenciasPendientesEnvio.class));

        } else if (id == R.id.nav_Home) {

            //startActivity(new Intent(mContext, Camara.class));
            /*if (gcodUsuario == 6) {
                startActivity(new Intent(this, Home.class));
            }*/

        } else if (id == R.id.nav_PdV) {

            if (setting.getBoolean("stGuardarNuevoPdV", false)) {
                startActivity(new Intent(this, PuntosDeVenta.class));
                overridePendingTransition(R.anim.zoomout, R.anim.rotar_fondo_antihorario);

            } else {

                new AlertDialog.Builder(mContext)
                        .setTitle("No esta permitido")
                        .setMessage("Desea solicitar via sms el permiso para guardar un nuevo punto de venta?")
                        .setPositiveButton("Enviar SMS", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + "86752483"));
                                intent.putExtra("sms_body", "Hola, soy " + setting.getString("nombUsuario", "") + " y solicito permiso para guardar PdV nuevo.");
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create()
                        .show();
            }

        } else if (id == R.id.nav_Configuracion) {

            startActivity(new Intent(this, Configuracion.class));
            /* overridePendingTransition(R.anim.rotar_fondo_antihorario, R.anim.rotar_fondo_horario);
             */
        } else if (id == R.id.nav_update) {

            startActivity(new Intent(this, Actualizacion.class));

        } else if (id == R.id.nav_Historial) {

            startActivity(new Intent(this, Historial.class));

        } else if (id == R.id.nav_ayuda) {

            startActivity(new Intent(this, Ayuda.class));

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //region Metodos para el gps
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
            if (mGoogleApiClient != null) {
                if (mGoogleApiClient.isConnected()) {
                    startLocationUpdates();
                } else {
                    try {
                        mGoogleApiClient.connect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            startLocationUpdates();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            stopLocationUpdates();
            if (mGoogleApiClient != null) {
                if (mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.disconnect();
                }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterNetworkChanges();
    }

    /**
     * Method to display the location on UI
     */
    public Location displayLocation() {

        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                    if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_CODE_ASK_PERMISSIONS_GPS);

                    }
                    /*else if (hasWriteContactsPermission == PackageManager.PERMISSION_GRANTED) {

                        displayLocation();

                    }*/
                }
            }

            if (mLastLocationMap == null)
                mLastLocationMap = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);


            if (mLastLocationMap != null) {
                tvPosicion.setText(mLastLocationMap.getLatitude() + "," + mLastLocationMap.getLongitude() +
                        /*"\n" + DateFormat.getDateTimeInstance().format(new Date(mLastLocationMap.getTime())) +*/
                        "\nRadio: " + setting.getString(stradiopdv, "100") + " mts.");
                isGPS = true;
                //startLocationUpdates();

                LatActual = mLastLocationMap.getLatitude();
                LongActual = mLastLocationMap.getLongitude();

                if (!sesionActiva) {
                    findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                }

                return mLastLocationMap;
            } else {

                showSettingsAlert(1);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mLastLocationMap;
    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     */
    @SuppressLint("RestrictedApi")
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(1);
    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {

        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {

            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {

                // new classCustomToast.Show_Toast (mActivity, "Dispositivo no soporta play services.", R.drawable.ic_error);
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

            if (mGoogleApiClient.isConnected())
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } catch (Exception e) {

            //para mostar la alerta de que necesita encender el gps
            showSettingsAlert(1);
            //VG.Toast(mContext, "Error relacionado con los servicios de google services.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        try {
            if (mGoogleApiClient != null) {
                if (mGoogleApiClient.isConnected()) {
                    LocationServices.FusedLocationApi.removeLocationUpdates(
                            mGoogleApiClient, this);
                }
            }

            locationManager.removeUpdates(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        //Log.i(TAG, "Conexión Fallida: ConnectionResult.getErrorCode() = "+ result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        // Once connected with google api, get the location
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        try {
            mLastLocationMap = location;
            // Displaying the new location on UI
            if (location.getAccuracy() < 500) {
                //si se esta usando una app de ubicacion simulada
                if (new classDetectMockLocation(this).isMockLocationOn(location)) {

                    startActivity(new Intent(this, MockLocationMessage.class));
                    this.finish();

                } else {

                    // si la latitud o longitud nueva es diferente de la anterior en las variables
                    if (LatActual != location.getLatitude() || LongActual != location.getLongitude()) {
                        //Actualiza la informacion y valores de la variables
                        displayLocation();
                        //recrea el mapa para posicionarlo en las coordenadas actuals
                        createMapView();
                    }
                }
            }

            startLocationUpdates();

        } catch (Exception e) {
            startLocationUpdates();
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public void GPSFisico() {

        locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

        // getting GPS status
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            // no network provider is enabled
        } else {

            // First get location from Network Provider
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, this);
                Log.d("Network", "Network");
                if (locationManager != null) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(mContext, "No tienes permiso de usar GPS ", Toast.LENGTH_LONG).show();

                    }
                    mLastLocationMap = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (mLastLocationMap != null) {
                        displayLocation();
                    } else {
                        Log.w("GPS Tracker ", "posicion: location es nula");
                    }
                }
            }
            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled) {
                if (mLastLocationMap == null) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
                    Log.d("GPS Enabled", "GPS Enabled");
                    if (locationManager != null) {
                        mLastLocationMap = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (mLastLocationMap != null) {
                            displayLocation();
                        } else
                            Log.w("GPS Tracker ", "posicion: locatiion nula");
                    }
                }
            }
        }
    }

    /**
     * funcion para mostrar alerta si el GPS no esta activado y enviarlo a la configuracion.
     */
    public void showSettingsAlert(int modoActivarGps) {

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        boolean isGPSProvider = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetowrkProvider = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSProvider || !isNetowrkProvider) {

            if (modoActivarGps == 1) {
                mLocationRequest = LocationRequest.create();
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mLocationRequest.setInterval(100); /// tenia 30 * 1000 como parametro
                mLocationRequest.setFastestInterval(10);   // tenia 5 * 1000 como parametro
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
                builder.setAlwaysShow(true);
                result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
                result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(LocationSettingsResult result) {
                        final Status status = result.getStatus();
                        switch (status.getStatusCode()) {
                            case LocationSettingsStatusCodes.SUCCESS:
                                break;
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    status.startResolutionForResult(MainActivity.this, 7);
                                } catch (IntentSender.SendIntentException e) {

                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                break;
                        }
                    }
                });
            }

            if (modoActivarGps == 2) {
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
        }

    }

    //endregion
    private void createMapView() {

        try {

            if (mapFragment == null) {
                //googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
                mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));

                if (mapFragment != null)
                    mapFragment.getMapAsync(this);
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
            }
            LatLng mi_posic = null;
            try {
                if (mLastLocationMap != null)
                    mi_posic = new LatLng(mLastLocationMap.getLatitude(), mLastLocationMap.getLongitude());

            } catch (Exception e) {
                e.printStackTrace();
            }
            Float bearing = null;
            if (googleMap != null && googleMap.getMyLocation() != null) {
                bearing = googleMap.getMyLocation().getBearing();
            }
            if (bearing != null && mi_posic != null) {
                CameraPosition currentPlace = new CameraPosition.Builder()
                        .target(mi_posic)
                        .bearing(bearing).tilt(45.5f).zoom(ZoomActual).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
            }
            //googleMap.animateCamera(CameraUpdateFactory.zoomTo(16), null);

           /* circle = googleMap.addCircle(new CircleOptions()
                    .center(mi_posic)
                    .radius(Integer.parseInt(setting.getString("stradiopdv", "100")))
                    .strokeWidth(1)
                    .strokeColor(Color.BLUE));*/

            if (SOLICITA_UBICACION_SMS) {
                StringBuilder mensaje = new StringBuilder();
                mensaje
                        .append("hola, ")
                        .append("soy " + setting.getString("nombUsuario", ""))
                        .append(" mi ubicacion actual es: ")
                        .append(mLastLocationMap != null ? (mLastLocationMap.getLatitude() + "," + mLastLocationMap.getLongitude()) : "N/D");
                //new SendSMS(mContext, "86752483", mensaje.toString());
                SOLICITA_UBICACION_SMS = false;
            } else {
                //Toast.makeText(MainActivity.this, "Estoy en enviar sms", Toast.LENGTH_LONG).show();
            }

            googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    if (cameraPosition.zoom != ZoomActual) {
                        ZoomActual = (int) cameraPosition.zoom;
                    }
                }
            });
            googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    mLastLocationMap = googleMap.getMyLocation();
                    displayLocation();
                }
            });
            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    //DialogoMostrarMapa();
                }
            });
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
        }

    }

    private void getExtras() {

        // Esto viene de SMSBroadcastReceiver
        Intent sms_intent = getIntent();
        Bundle b = sms_intent.getExtras();
        if (b != null) {
            try {
                // Display SMS in the TextView
                if (b.getString("sms_str") != null) {
                    String Comandos[] = b.getString("sms_str").split("/");
                    if (Comandos[0].equalsIgnoreCase("CLEARALLDATA")) {
                        CMG.clearData();
                    }
                    if (Comandos[0].equalsIgnoreCase("GETCURRENTPOSITION")) {
                        SOLICITA_UBICACION_SMS = true;
                    }
                    if (Comandos[0].equalsIgnoreCase("ACTIVOGUARDARPDV")) {
                        SharedPreferences.Editor editor = setting.edit();
                        editor.putBoolean("stGuardarNuevoPdV", true);
                        //si viene la cantidad de pdv permitidos a guardar
                        if (Comandos.length > 1)
                            editor.putInt("stCantidadPdvPermitido", Integer.parseInt(Comandos[1]));

                        editor.commit();
                    }
                    if (Comandos[0].equalsIgnoreCase("ACTIVOCUENTAMOCKLOCATION")) {
                        SharedPreferences.Editor editor = setting.edit();
                        editor.putInt(getResources().getString(R.string.stMockLocation), 0);
                        editor.commit();
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void VerificaAsistenciaActiva() {

        int IdAsist = Integer.parseInt(setting.getString("strIdAsistencia", "0"));
        if (IdAsist == 0) {

            ControlesSegunAsist(false);

        } else { // si existe una entrada actva

            ControlesSegunAsist(true);

            IdAsistenciaMarcada = IdAsist;
            etCostoTransp.setText(setting.getString("strCosto", "0.0"));
            etCostoTranspTaxi.setText(setting.getString("strCostoTaxi", "0.0"));
            etCostoAlim.setText(setting.getString("strCostoAlim", "0.0"));
            etCostoHosped.setText(setting.getString("strCostoHosped", "0.0"));
            etCostoVario.setText(setting.getString("strCostoVario", "0.0"));
            etKmActual.setText(setting.getString("strKmActual", "0"));
            etComentario.setText(setting.getString("strComentario", ""));
            etFechEntrada.setText(setting.getString("strFechaEntr", "¿?"));

            arrPdv.add(new classPdv(Integer.parseInt(setting.getString("strIdPdVAsist", "0")), setting.getString("strNombPdV", "Punto Especial"), ""));
            spnpdvent.setAdapter(new Custom_Spn_Lista(arrPdv));

            customHandler.removeCallbacks(updateTimerThread);

            ((TextView) findViewById(R.id.tvObteniendoCoordenada_MA)).setText("Asistencia Activa. Marque salida.");
        }
    }

    private void getmyPosition() {
        try {
            if (isGPS) {

                arrPdv.clear();

                Cursor cPdv = db.rawQuery("SELECT * FROM " + TBL_PDV_ASISTENCIA, null);

                if (cPdv.getCount() > 0) {

                   /* for (cPdv.moveToFirst(); !cPdv.isAfterLast(); cPdv.moveToNext()) {
                        result += cursor.getString(iRow) + ": " + cursor.getString(iName) + " - " + cursor.getDouble(iLat) + " latitude " + cursor.getDouble(iLon) + " longitude\n";
                    }*/


                    if (cPdv.moveToFirst()) {
                        do {
                            if (PdVCercano(cPdv.getString(cPdv.getColumnIndex("LocationGPS")))) {

                                arrPdv.add(new classPdv(
                                        Integer.parseInt(cPdv.getString(cPdv.getColumnIndex("IdPdV"))),
                                        cPdv.getString(cPdv.getColumnIndex("NombrePdV")),
                                        cPdv.getString(cPdv.getColumnIndex("LocationGPS"))));
                            }
                        } while (cPdv.moveToNext());


                        if (arrPdv.isEmpty()) {
                            PdVNoEncontrado();
                        } else {
                            isEventoEspecial = false;
                            //adicionalmente se agrtega a la lista el punto especial
                            arrPdv.add(new classPdv(29, "Punto Esecial", ""));
                            spnpdvent.setAdapter(new Custom_Spn_Lista(arrPdv));
                        }
                    }
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("No puntos de venta")
                            .setMessage("Aun no has descargado los puntos de venta, debes actualizar al menos una vez.")
                            .setCancelable(true)
                            .setPositiveButton("descargar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (CMG.TieneConexion()) {
                                        VerificarNuevosPuntosguardados(1);
                                    }
                                }
                            })
                            .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
                //**********************************************************************
            } else {
                new classCustomToast(this).Toast("Aún no se ha obtenido su ubicación", R.drawable.ic_error);
            }
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    /**
     * Método creado para calcular la distancia entre mi dispositivo y los puntos de venta
     * obteniendo los puntos que tengan menor distancia
     *
     * @param LatLong Cadena con la latitud y longitud del dispositivo
     * @return True: si la distancia es menor que la seleccionada en configuracion de la app; False: si la
     * distancia es mayor que la seleccion de la configuracion dentro de la aplicacion
     */
    private boolean PdVCercano(String LatLong) {
        try {
            if (mLastLocationMap != null) {

                String[] LatLng = LatLong.split(",");
                if (LatLng.length == 2) {
                    Location destino = new Location("dummyprovider");
                    destino.setLatitude(Float.valueOf(LatLng[0]));
                    destino.setLongitude(Float.valueOf(LatLng[1]));

                    Float cerca = mLastLocationMap.distanceTo(destino);

                    if (cerca < intDistPdVMasCercvano) {
                        intDistPdVMasCercvano = cerca;
                    }

                    //Log.i("pdvcercano", "distancia: " + cerca + " metros. mas cercano a : " + intDistPdVMasCercvano);

                    if (cerca <= Integer.parseInt(setting.getString("stradiopdv", "100"))) {
                        return true;

                    } else {
                        return false;
                    }
                } else {
                    return false;
                }

            } else {
                //Log.i("pdvcercano", "No se ha podido determinar la ubicación actual.");
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Muestra el mensaje de dialgo e punto de vent no encontrado. con las opciones para ese momento.
     */
    private void PdVNoEncontrado() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("No se ha encontrado un punto de venta cercano");
        builder.setCancelable(true);
        builder.setItems(new String[]{"Cancelar", "Actividad especial (no es punto de venta)"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:

                        break;
                    case 1:
                        isEventoEspecial = true;
                        arrPdv.clear();
                        arrPdv.add(new classPdv(29, "Punto Especial", ""));
                        spnpdvent.setAdapter(new Custom_Spn_Lista(arrPdv));
                        break;
                }
            }
        });
        builder.create();
        builder.show();
    }

    public class Custom_Spn_Lista extends ArrayAdapter<classPdv> {

        private final List<classPdv> Pdv;

        public Custom_Spn_Lista(List<classPdv> pdv) {
            super(mContext, R.layout.list_pdv_find, pdv);
            this.Pdv = pdv;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = mActivity.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.list_pdv_find, null, true);

            try {
                TextView txtpdv = (TextView) rowView.findViewById(R.id.tv_pdv_spnfind);
                txtpdv.setText(Pdv.get(position).getNombre());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return rowView;
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void ControlesSegunAsist(boolean activa) {
        try {

            if (activa) {
                //cardview entrada
                btnObten_ubic.setEnabled(false);
                etCostoTransp.setEnabled(false);
                etCostoTranspTaxi.setEnabled(false);
                etCostoAlim.setEnabled(false);
                etCostoHosped.setEnabled(false);
                etCostoVario.setEnabled(false);
                etKmActual.setEnabled(false);
                etComentario.setEnabled(false);
                spnpdvent.setEnabled(false);
                btnMarcarEntrada.setEnabled(false);
                btnMarcarSalida.setEnabled(true);
                //cardview operaciones
                ibPhoto.setEnabled(true);
                sesionActiva = true;

            } else {
                //cardview entrada
                btnObten_ubic.setEnabled(true);
                etCostoTransp.setEnabled(true);
                etCostoTranspTaxi.setEnabled(true);
                etCostoAlim.setEnabled(true);
                etCostoHosped.setEnabled(true);
                etCostoVario.setEnabled(true);
                etKmActual.setEnabled(true);
                etComentario.setEnabled(true);
                spnpdvent.setEnabled(true);
                btnMarcarEntrada.setEnabled(true);
                btnMarcarSalida.setEnabled(false);
                //cardview operaciones
                ibPhoto.setEnabled(false);
                customHandler.postDelayed(updateTimerThread, 1000);


                arrPdv.clear();
                arrPdv.add(new classPdv(0, "Obtenga Punto de Venta  ->", ""));
                spnpdvent.setAdapter(new Custom_Spn_Lista(arrPdv));

                sesionActiva = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Este metodo es para guardar una asistencia a un pdv, si tiene internet lo envia, de lo contrario lo
     * guarda localmente
     */
    public void MarcarEntrada() {
        try {

            etCostoTransp.setError(null);
            etComentario.setError(null);

            if (isGPS) {        // si ya se obtuvieron las coordenadas

                int IdPdV = 0;

                String Fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                float CostoAlim = TextUtils.isEmpty(etCostoAlim.getText()) ? 0.00f : Float.parseFloat(etCostoAlim.getText().toString());
                float CostoHosped = TextUtils.isEmpty(etCostoHosped.getText()) ? 0.00f : Float.parseFloat(etCostoHosped.getText().toString());
                float CostoVario = TextUtils.isEmpty(etCostoVario.getText()) ? 0.00f : Float.parseFloat(etCostoVario.getText().toString());
                float CostoTaxi = TextUtils.isEmpty(etCostoTranspTaxi.getText()) ? 0.00f : Float.parseFloat(etCostoTranspTaxi.getText().toString());
                float CostoTran = TextUtils.isEmpty(etCostoTransp.getText()) ? 0.00f : Float.parseFloat(etCostoTransp.getText().toString());
                String KmActual = TextUtils.isEmpty(etKmActual.getText()) ? "0" : etKmActual.getText().toString();
                String Comentario = !isEventoEspecial ? etComentario.getText().toString() : etComentario.getText().toString() + " ~ " + tvPosicion.getText().toString();

                IdPdV = arrPdv.get(spnpdvent.getSelectedItemPosition()).getIdPdv() == 29 ? 29 : arrPdv.get(spnpdvent.getSelectedItemPosition()).getIdPdv();      //29 es el registro con el punto de venta especial en la base de datos

                if (IdPdV > 0) {    // SI YA SELECCIONO UN PUNTO DE VENTA

                    if (!TextUtils.isEmpty(etComentario.getText())) {   // SI HA INGRESADO UN COMENTARIO SOBRE EL REGISTRO

                        // se guardara local siempre para siempre tener registros aun que  falle el internet
                        try {

                            // se preparan los valores a insertar en la bd
                            ContentValues values = new ContentValues();
                            values.put("IdAsistencia", (byte[]) null);
                            values.put("codUsuario", gcodUsuario);
                            values.put("Pdv", IdPdV);
                            values.put("CostoTran", CostoTran);
                            values.put("CostoTaxi", CostoTaxi);
                            values.put("CostoAlim", CostoAlim);
                            values.put("CostoHosped", CostoHosped);
                            values.put("CostoVario", CostoVario);
                            values.put("Fecha", Fecha);
                            values.put("Comentario", Comentario);
                            values.put("KmActual", KmActual);
                            values.put("estado", 0);
                            values.put("IdEnviado", 0);
                            values.put("FechaSalida", "");
                            values.put("EstadoSalidaEnviado", -1);

                            // se inserta en el bd
                            int IdInsert = (int) db.insert("tblAsistencia", null, values);

                            // si la  insercion fue exitosa
                            if (IdInsert > 0) {

                                IdAsistenciaMarcada = IdInsert;

                                SharedPreferences.Editor editor = setting.edit();
                                editor.putString("strIdAsistencia", "" + IdAsistenciaMarcada);
                                editor.putString("strPhotoName", "img_" + gcodUsuario + "_" + gcodPdV + "_" + IdAsistenciaMarcada + "_");
                                editor.putString("strIdPdVAsist", String.valueOf(IdPdV));
                                editor.putString("strNombPdV", isEventoEspecial ? "Punto Especial" : arrPdv.get(spnpdvent.getSelectedItemPosition()).getNombre());
                                editor.putString("strFechaEntr", etFechEntrada.getText().toString());
                                editor.putString("strCosto", etCostoTransp.getText().toString());
                                editor.putString("strCostoTaxi", etCostoTranspTaxi.getText().toString());
                                editor.putString("strCostoAlim", etCostoAlim.getText().toString());
                                editor.putString("strCostoHosped", etCostoHosped.getText().toString());
                                editor.putString("strCostoVario", etCostoVario.getText().toString());
                                editor.putString("strKmActual", etKmActual.getText().toString());
                                editor.putString("strComentario", etComentario.getText().toString());
                                editor.putString("strFechaEntrada", Fecha);
                                editor.commit();

                                new classCustomToast(mActivity).Toast("Se ha marcado la entrada, se procede a enviar al servidor.", R.drawable.ic_success);

                                if (CMG.TieneConexion()) {  // SI TIENE CONEXION A INTERNET

                                    String consulta = "SELECT * FROM " + TBL_ASISTENCIA + " AS A INNER JOIN " + TBL_PDV_ASISTENCIA + " AS B ON IdPdV = Pdv WHERE IdAsistencia = '" + IdAsistenciaMarcada + "'";
                                    Cursor cAsistenc = db.rawQuery(consulta, null);

                                    if (cAsistenc.moveToFirst()) {

                                        //parametros para enviar al web service
                                       /* ArrayList<classWebService> parametros = new ArrayList<>();
                                        parametros.add(new classWebService("idUsuario", String.valueOf(gcodUsuario)));
                                        parametros.add(new classWebService("IdPdV", cAsistenc.getString(cAsistenc.getColumnIndex("Pdv"))));
                                        parametros.add(new classWebService("CantGastosMovim", cAsistenc.getString(cAsistenc.getColumnIndex("CostoTran"))));
                                        parametros.add(new classWebService("CantGastosMovimTaxi", cAsistenc.getString(cAsistenc.getColumnIndex("CostoTaxi"))));
                                        parametros.add(new classWebService("CantGastosAlim", cAsistenc.getString(cAsistenc.getColumnIndex("CostoAlim"))));
                                        parametros.add(new classWebService("CantGastosHosped", cAsistenc.getString(cAsistenc.getColumnIndex("CostoHosped"))));
                                        parametros.add(new classWebService("CantGastosVario", cAsistenc.getString(cAsistenc.getColumnIndex("CostoVario"))));
                                        parametros.add(new classWebService("FechaRegistro", cAsistenc.getString(cAsistenc.getColumnIndex("Fecha"))));
                                        parametros.add(new classWebService("Observacion", cAsistenc.getString(cAsistenc.getColumnIndex("Comentario"))));
                                        parametros.add(new classWebService("KmActual", cAsistenc.getString(cAsistenc.getColumnIndex("KmActual"))));
                                        parametros.add(new classWebService("FechaSalida", cAsistenc.getString(cAsistenc.getColumnIndex("FechaSalida"))));
                                        parametros.add(new classWebService("IdAsistenciaOnLine", cAsistenc.getString(cAsistenc.getColumnIndex("IdEnviado"))));
                                        parametros.add(new classWebService("SOLO_ENT", "SOLO_ENT"));// indica que la asistencia es solo con hora de entradas y no con salida*/

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

                                        CMG.crearNuevaAsistencia(enviosPendientes);

                                    }
                                } else {

                                    new AlertDialog.Builder(mContext)
                                            .setTitle("Marcar Entrada")
                                            .setMessage("Has marcado tu asistencia con éxito localmente.\nVerifique estado en envios pendientes.")
                                            .setIcon(R.drawable.ic_success)
                                            .setCancelable(true)
                                            .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            })
                                            .show();
                                }

                                ControlesSegunAsist(true);

                                customHandler.removeCallbacks(updateTimerThread);

                            } else { // si no se inserto con exito
                                new classCustomToast(this).Toast("Error al guardar ", R.drawable.ic_error);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {        // SI NO INGRESO UN COMENTARIO
                        etComentario.setError("Ingrese un comentario.");
                    }
                } else { // SI NO HA SELECCIONADO UN PDV

                    new classCustomToast(mActivity).Toast("Obtenga el punto de venta actual Pin de color amarillo.", R.drawable.ic_error);
                }

            } else {        // SI NO SE HAN OBTENIDO LAS COORDENADAS
                new classCustomToast(mActivity).Toast("Aun no se ha obtenido su ubicación", R.drawable.ic_error);
            }

        } catch (Exception e) {
            new classCustomToast(mActivity).Toast("Ha ocurrido un error " + e.getMessage(), R.drawable.ic_error);
            e.printStackTrace();
        }
    }

    /**
     * ESTE METODO ES PARA LIMPIAR LOS CAMPOS Y LOS SETTTING a la espera de una nueva marcada en un pdv
     */
    private void MarcarSalida() {

        try {

            ContentValues values = new ContentValues();
            values.put("FechaSalida", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            values.put("EstadoSalidaEnviado", 0);
            int success = db.update(TBL_ASISTENCIA, values, "IdAsistencia = '" + IdAsistenciaMarcada + "'", null);

            int temporal = 10000;
            if (success > 0 || IdAsistenciaMarcada > temporal) { // numero filas afectadas

                SharedPreferences.Editor editor = setting.edit();
                editor.putString("strIdAsistencia", "0");
                editor.putString("strPhotoName", "img_");
                editor.putString("strIdPdVAsist", "");
                editor.putString("strFechaEntr", "");
                editor.putString("strCosto", "");
                editor.putString("strCostoTaxi", "");
                editor.putString("strCostoAlim", "");
                editor.putString("strCostoHosped", "");
                editor.putString("strCostoVario", "");
                editor.putString("strKmActual", "");
                editor.putString("strComentario", "");

                etCostoTransp.setText("");
                etCostoTranspTaxi.setText("");
                etCostoAlim.setText("");
                etCostoHosped.setText("");
                etCostoVario.setText("");
                etKmActual.setText("");
                etComentario.setText("");
                editor.commit();

                if (CMG.TieneConexion()) {  // SI TIENE CONEXION A INTERNET

                    String consulta = "SELECT * FROM tblAsistencia INNER JOIN puntosdeventaAsistencia ON IdPdV = Pdv WHERE IdAsistencia = '" + IdAsistenciaMarcada + "'";
                    Cursor cAsistenc = db.rawQuery(consulta, null);

                    if (cAsistenc.moveToFirst()) {

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

                        CMG.crearNuevaAsistencia(enviosPendientes);
                    }
                } else {
                    ControlesSegunAsist(false);
                    recreate();
                }

            }

        } catch (Exception e) {
            new AlertDialog.Builder(mContext).setTitle("Error!").setMessage(e.getMessage() + " Linea:" + e.getLocalizedMessage()).setIcon(R.drawable.ic_error).show();
            e.printStackTrace();
        }
    }

    /**
     * Clase generada para guardar en la bese de datos online la asistencia de la supervisora en un punto
     * de venta
     */
    private void crearNuevaAsistencia(String URL_WS, final List<classWebService> params) {
        try {

            final ProgressDialog pDial = new ProgressDialog(mContext);
            pDial.setTitle("Enviando los datos. Espere...");
            pDial.setIndeterminate(true);
            pDial.setCancelable(true);
            pDial.setCanceledOnTouchOutside(false);
            pDial.show();

            StringRequest jsonRequest = new StringRequest(Request.Method.POST, URL_WS,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            JSONObject file_url = null;
                            try {
                                file_url = new JSONObject(response);
                                if (file_url == null) {
                                    new classCustomToast(mActivity).Toast("sin repuesta del servidor o no tienes acceso a internet", R.drawable.ic_error);
                                    new AlertDialog.Builder(mContext).setTitle("Éxito marcar Entrada").setMessage("Has marcado tu asistencia con éxito localmente.\nSe intentará enviar al servidor.\nVerifique estado en envios pendientes.").setIcon(R.drawable.ic_success).setCancelable(true).setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }).show();

                                } else {
                                    int success = file_url.getInt("successInsert");
                                    if (success == 1) {

                                        SharedPreferences.Editor editor = setting.edit();

                                        int successMaxID = file_url.getInt("successMaxID");
                                        int IdAsistencia = file_url.getInt("IdAsistencia");

                                        if (successMaxID == 1) {

                                            ContentValues values = new ContentValues();
                                            values.put("estado", 1);
                                            values.put("IdEnviado", IdAsistencia);
                                            values.put("EstadoSalidaEnviado", params.get(12).getValor().equalsIgnoreCase("SOLO_ENT") ? 0 : 1); // si es solo entrada pone estado salida cero de lo contrario 1

                                            int successUpdate = db.update("tblAsistencia", values, "IdAsistencia = '" + IdAsistenciaMarcada + "'", null);
                                            if (successUpdate > 0) {
                                                new classCustomToast(mActivity).Toast("Se ha enviado al servidor correctamente.", R.drawable.ic_success);
                                            }

                                        }

                                    } else {
                                        new classCustomToast(mActivity).Toast("Error en el servidor " + file_url.getString("messageInsert"), R.drawable.ic_success);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } finally {
                                if (pDial.isShowing()) {
                                    pDial.dismiss();
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    new classCustomToast((Activity) mContext).Show_ToastError("Error en la consulta al ws: " + error.getMessage());
                    if (pDial.isShowing()) {
                        pDial.dismiss();
                    }

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
                    if (pDial != null && pDial.isShowing())
                        pDial.dismiss();

                    recreate();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    //fin crear nueva asistencia


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_Refrescar:
                recreate();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    private void VerificarNuevosPuntosguardados(int TIPO_DESCARGA) {
        try {

            if (TIPO_DESCARGA == 1) { // el usuario dio click al boton descargar puntos de venta
                new AlertDialog.Builder(mContext)
                        .setTitle("Descargar Puntos de venta")
                        .setMessage("Seleccione el modo de descarga.")
                        .setPositiveButton("Todos", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                CMG.VerificarNuevosPuntosguardados(
                                        //si no hay ningun registro, muestra el alert de descarga de pdv
                                        true, // true indica que va a mostrar alerta de que se estan descargando los puntos de venta.
                                        0,// 0 indica que va a descargar todos los puntos de venta
                                        "");//vacio indica todos los departamentos
                            }
                        })
                       /* .setNegativeButton("Departamentos", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                final List<Deptos> Deptos = new ArrayList<>();
                                Deptos.add(new Deptos(1, "Boaco", false));
                                Deptos.add(new Deptos(2, "Carazo", false));
                                Deptos.add(new Deptos(3, "Chinandega", false));
                                Deptos.add(new Deptos(4, "Chontales", false));
                                Deptos.add(new Deptos(5, "Esteli", false));
                                Deptos.add(new Deptos(6, "Granada", false));
                                Deptos.add(new Deptos(7, "Jinotega", false));
                                Deptos.add(new Deptos(8, "Leon", false));
                                Deptos.add(new Deptos(9, "Madriz", false));
                                Deptos.add(new Deptos(10, "Managua", false));
                                Deptos.add(new Deptos(11, "Masaya", false));
                                Deptos.add(new Deptos(12, "Matagalpa", false));
                                Deptos.add(new Deptos(13, "Nueva Segovia", false));
                                Deptos.add(new Deptos(14, "RAAN", false));
                                Deptos.add(new Deptos(15, "RAAS", false));
                                Deptos.add(new Deptos(16, "Rio San Juan", false));
                                Deptos.add(new Deptos(17, "Rivas", false));

                                // Build an AlertDialog
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                                // Set multiple choice items for alert dialog

                                builder.setMultiChoiceItems(new Deptos().getArrayDepto(Deptos), new Deptos().getArrayDeptoSelecc(Deptos), new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                                        // Update the current focused item's checked status
                                        Deptos.get(which).setSelecc(isChecked);

                                        // Get the current focused item
                                        String currentItem = Deptos.get(which).getIdDepto() + " - " + Deptos.get(which).getDepto() + " seleccionado:";

                                        // Notify the current action
                                        Toast.makeText(getApplicationContext(),
                                                currentItem + " " + Deptos.get(which).getSelecc(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                                // Specify the dialog is not cancelable
                                builder.setCancelable(false);

                                // Set a title for alert dialog
                                builder.setTitle("Departamento que visito");

                                // Set the positive/yes button click listener
                                builder.setPositiveButton("Descargar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Do something when click positive button
                                        String strDeptosSelecc = "";
                                        for (int i = 0; i < Deptos.size(); i++) {
                                            boolean checked = Deptos.get(i).getSelecc();
                                            if (checked) {
                                                strDeptosSelecc += Deptos.get(i).getIdDepto();
                                                if (i != Deptos.size() - 1) {
                                                    strDeptosSelecc = strDeptosSelecc + ", ";
                                                }
                                            }
                                        }

                                        // si selecciono al menos un departamento
                                        if (strDeptosSelecc.length() > 0) {

                                            CMG.VerificarNuevosPuntosguardados(
                                                    //si no hay ningun registro, muestra el alert de descarga de pdv
                                                    true, // true indica que va a mostrar alerta de que se estan descargando los puntos de venta.
                                                    0,// 0 indica que va a descargar todos los puntos de venta
                                                    strDeptosSelecc);//vacio indica todos los departamentos, si no descargara segun deptos selecionado
                                        }
                                        //si no selecciono ningun depto
                                        else {
                                            Toast.makeText(MainActivity.this, "Por favor seleccionar al menos un departamento.", Toast.LENGTH_LONG).show();
                                        }

                                    }
                                });

                                // Set the neutral/cancel button click listener
                                builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Do something when click the neutral button
                                    }
                                });

                                AlertDialog dialog = builder.create();
                                // Display the alert dialog on interface
                                dialog.show();
                            }
                        })*/
                        .show();

            } else if (TIPO_DESCARGA == 0)

            {// se esta verificando en segundo plano automaticamente si hay nuevos puntos de venta.

                Cursor cMaxidGuardado = db.rawQuery("SELECT MAX(idpdv) AS idpdv FROM " + TBL_PDV_ASISTENCIA, null);
                if (cMaxidGuardado.getCount() > 0) {
                    cMaxidGuardado.moveToFirst();
                    int idmaxim = cMaxidGuardado.getInt(0);

                    CMG.VerificarNuevosPuntosguardados(
                            //si no hay ningun registro, muestra el alert de descarga de pdv
                            idmaxim == 0 ? true : false, // true indica que va a mostrar alerta de que se estan descargando los puntos de venta.
                            idmaxim,// 0 indica que va a descargar todos los puntos de venta
                            "");//vacio indica todos los departamentos
                }

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void VerificarClientes() {
        try {
            Cursor cmaxCli = db.rawQuery("select max(IdCliente) as IdCliente from " + TBL_CLIENTES, null);

            if (cmaxCli.moveToFirst()) {

                String idmax = cmaxCli.getString(0);

                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("opcion", "ObtenerClientes");
                parameters.put("listaParametros", idmax == null ? "0" : idmax);
                parameters.put("format", "json");

                CMG.webServicesObtenerClientes(parameters, db);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void verificaUsoUbicacionSimulada(){
        if (CMG.TieneConexion()) {

            List<classWebService> params = new ArrayList<>();
            params.add(new classWebService("opcion", "ObtenerUsuarioUbicacionSimulada"));
            params.add(new classWebService("listaParametros", "" + setting.getInt(codUsuario, 0)));
            params.add(new classWebService("format", "json"));

            new classMetodosGenerales(this).webServicePost(URL_WS_MULTIPLE_OPCIONES,
                    params, option_obtener_ubic_simul);
        }

    }

    private void MuestraOcultaControlesSegunconfiguracionUsuario() {

        try {
            Set<String> Controles = setting.getStringSet("controles", null);

            if (Controles != null) {

                String[] selected = Controles.toArray(new String[]{});
                boolean alim = false, hosped = false;

                for (int i = 0; i < selected.length; i++) {

                    if (selected[i].equalsIgnoreCase("Transporte")) {
                        etCostoTransp.setVisibility(View.VISIBLE);
                        etCostoTranspTaxi.setVisibility(View.VISIBLE);
                        ((LinearLayout) findViewById(R.id.llTransporte_MA)).setVisibility(View.GONE);
                    }
                    if (selected[i].equalsIgnoreCase("Alimento")) {
                        alim = true;
                        etCostoAlim.setVisibility(View.GONE);
                    }
                    if (selected[i].equalsIgnoreCase("Hospedaje")) {
                        hosped = true;
                        etCostoHosped.setVisibility(View.GONE);
                    }
                    if (selected[i].equalsIgnoreCase("Otros")) {
                        etCostoVario.setVisibility(View.GONE);
                    }
                }

                if (alim && hosped) {
                    ((LinearLayout) findViewById(R.id.llAlimentHosped_MA)).setVisibility(View.GONE);
                }

                etKmActual.setVisibility(setting.getBoolean(getResources().getString(R.string.stSoyConductor), false) ? View.VISIBLE : View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {

        switch (permsRequestCode) {

            case REQUEST_CODE_CAMERA_PICTURE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(mContext, Camara.class));
                }

                break;

        }
    }

    private void showMessageOKCancelar(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancelar", null)
                .create()
                .show();
    }

    @SuppressLint("ResourceAsColor")
    public static void NotificadorTieneInternet(boolean value) {

        try {

            if (value) {
                tv_check_connection.setVisibility(View.VISIBLE);
                tv_check_connection.setText("Con conexión a internet");
                tv_check_connection.setBackgroundColor(R.color.colorPrimaryDark);
                tv_check_connection.setTextColor(Color.WHITE);

           /* Handler handler = new Handler();
            Runnable delayrunnable = new Runnable() {
                @Override
                public void run() {
                    tv_check_connection.setVisibility(View.GONE);
                }
            };
            handler.postDelayed(delayrunnable, 500);*/

                tv_check_connection.setVisibility(View.GONE);

            } else {
                tv_check_connection.setVisibility(View.VISIBLE);
                tv_check_connection.setText("No tienes internet");
                tv_check_connection.setBackgroundColor(Color.RED);
                tv_check_connection.setTextColor(Color.WHITE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    protected void unregisterNetworkChanges() {
        try {

            unregisterReceiver(mNetworkReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAsyncTaskComplete(JSONObject result, int option) {

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
                            startActivity(new Intent(this,MockLocationMessage.class));
                            finish();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            switch (option) {
                case option_descPdv:

                    break;
            }

        } else {

        }
    }
}
