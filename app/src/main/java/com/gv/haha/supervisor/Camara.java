package com.gv.haha.supervisor;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gv.haha.supervisor.clases.SQLHelper;
import com.gv.haha.supervisor.clases.classBottomNavigationBehavior;
import com.gv.haha.supervisor.clases.classCustomToast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.gv.haha.supervisor.MainActivity.gcodUsuario;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.TBL_ASISTENCIA;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.tblAsistenciaFoto;


public class Camara extends AppCompatActivity implements View.OnClickListener, BottomNavigationView.OnNavigationItemSelectedListener {


    private static final String LOG_TAG = "GVCamera";

    private int cameraId = 0;
    private static Camera camera = null;
    private static boolean CameraPermission = false;

    SurfaceView preview_surface;
    ImageView bitmap_view;
    LinearLayout llfotos;
    SharedPreferences setting;
    SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camara);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setting = PreferenceManager.getDefaultSharedPreferences(this);
        db = new SQLHelper(Camara.this).getWritableDatabase();

        //esto es para el booton bar
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);
        // attaching bottom sheet behaviour - hide / show on scroll
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) navigation.getLayoutParams();
        layoutParams.setBehavior(new classBottomNavigationBehavior());

        //Add MenuItem with icon to Menu
        navigation.getMenu().add(Menu.NONE, 1, Menu.NONE, "Cambiar").setIcon(R.drawable.bg_switch_camera);
        navigation.getMenu().add(Menu.NONE, 2, Menu.NONE, "Capturar").setIcon(R.drawable.ic_camera);
        navigation.getMenu().add(Menu.NONE, 3, Menu.NONE, "Enviar").setIcon(android.R.drawable.ic_menu_send);

        preview_surface = (SurfaceView) findViewById(R.id.preview_surface);
        //  bitmap_view = (ImageView) findViewById(R.id.bitmap_view);
        llfotos = (LinearLayout) findViewById(R.id.llFotos_C);

        askForPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE, 3);
        askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 2);

        ObtenerFotosTomadas();
    }

    private void ObtenerFotosTomadas() {
        try {

            String IdAsistencia = setting.getString("strIdAsistencia", "");

            Cursor cursor = db.rawQuery("select * from " + tblAsistenciaFoto + " where IdAsistencia = " + IdAsistencia + " and estEnvio = 0", null);
            //si existe el registro
            llfotos.removeAllViews();

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

                String path = cursor.getString(cursor.getColumnIndex("fotopath"));
                String Id = cursor.getString(cursor.getColumnIndex("Id"));

                if (new File(path).exists()) {

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 6;
                    Bitmap bmp = BitmapFactory.decodeFile(path, options);

                    AgregarFoto(bmp, path, Id, IdAsistencia);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            }
        } else {
            //Toast.makeText(this, "" + permission + " ya esta permtido.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startCamera(cameraId);
                } else {

                }
                return;
            }
            case 2: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {

                }
                return;
            }
        }
    }


    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (v.getTag() != null && v.getTag().getClass() == Camera.Size.class) {
            Camera.Size sz = (Camera.Size) v.getTag();
            Log.d(LOG_TAG, "setPictureSize " + sz.width + "x" + sz.height);
            setPictureSize(sz.width, sz.height);
            //((View) v.getParent()).setVisibility(View.INVISIBLE);
        }
    }

    public void switchCamera() {
        startCamera(1 - cameraId);
    }

    @Override
    public void onResume() {
        Log.d(LOG_TAG, "onResume");
        super.onResume();
        setSurface();
    }

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {

                //se obtienen los datos segun la asistencia marcada
                String IdAsistencia = setting.getString("strIdAsistencia", "");

                Cursor cursor = db.rawQuery("select * from " + TBL_ASISTENCIA + " where IdAsistencia = " + IdAsistencia, null);
                //si existe el registro
                if (cursor.moveToFirst()) {

                    String puntoventa = cursor.getString(cursor.getColumnIndex("Pdv")),
                            FechaAsistenciaEntrada = cursor.getString(cursor.getColumnIndex("Fecha"));
                    int AsistenciaConInternet = cursor.getInt(cursor.getColumnIndex("IdEnviado"));


                    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/grupovalor/supervisor/",
                            NombreFoto = "img_Usu" +
                                    gcodUsuario + "_Pdv" +
                                    puntoventa + "_" + (AsistenciaConInternet > 0 ? "Online" : "Local") +
                                    (AsistenciaConInternet > 0 ? AsistenciaConInternet : IdAsistencia) + "_" + // si el idenviado esmayor que cero, se nombra la foto con ese id
                                    new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".jpg";// la fecha mas la extension

                    //i no existe la carpeta, se crea.
                    if (!new File(path).exists()) {
                        new File(path).mkdirs();
                    }


                    FileOutputStream jpg = new FileOutputStream(path + NombreFoto);
                    jpg.write(data);
                    jpg.close();

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 6;
                    Bitmap bmp = BitmapFactory.decodeFile(path + NombreFoto, options);
                    //Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

                    Log.i(LOG_TAG, "bmp dimensions " + bmp.getWidth() + "x" + bmp.getHeight());

                /*ImageView bmpView = (ImageView) findViewById(R.id.bitmap_view);
                bmpView.setImageBitmap(bmp);
                bmpView.setVisibility(View.VISIBLE);*/

                    //**********************************************


                    ContentValues values = new ContentValues();
                    values.put("Id", (byte[]) null);
                    values.put("IdAsistencia", IdAsistencia);
                    values.put("puntoventa", puntoventa);
                    values.put("FechaAsistenciaEntrada", FechaAsistenciaEntrada);
                    values.put("AsistenciaConInternet", AsistenciaConInternet);
                    values.put("fotopath", path + NombreFoto);
                    values.put("estEnvio", 0);

                    Long id = db.insert(tblAsistenciaFoto, null, values);
                    if (id > 0) {

                        AgregarFoto(bmp, (path + NombreFoto), String.valueOf(id), IdAsistencia);
                    }
                }
                //**********************************************

                restartPreview();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    private void AgregarFoto(final Bitmap bmp, final String path, final String Id, final String IdAsistencia) {

        String[] nombrefoto = path.split("/");  //se dividela cadena en partes
        final String fotoname = nombrefoto[nombrefoto.length - 1]; //se agarra la oltima parte que es el nombre dela foto

        LinearLayout.LayoutParams params = new LinearLayout
                .LayoutParams(100, 100);
        ImageView item = new ImageView(Camara.this);
        item.setOnClickListener(Camara.this);
        item.setLayoutParams(params);
        item.isClickable();
        item.hasOnClickListeners();
        item.setImageBitmap(bmp);
        item.setScaleType(ImageView.ScaleType.FIT_CENTER);

        llfotos.addView(item,0);

        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //EnviarImagen(path, fotoname, Id, IdAsistencia);
            }
        });
    }

    private SurfaceHolder.Callback shCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i(LOG_TAG, "surfaceDestroyed callback");
            if (camera != null) {
                camera.stopPreview();
                camera.release();
            }
            camera = null;
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.i(LOG_TAG, "surfaceCreated callback");
            if (ContextCompat.checkSelfPermission(Camara.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                askForPermission(Manifest.permission.CAMERA, 1);
            else {
                startCamera(cameraId);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            Log.i(LOG_TAG, "surfaceChanged callback " + width + "x" + height);
            restartPreview();
        }
    };

    private void setSurface() {
        SurfaceView previewSurfaceView = (SurfaceView) findViewById(R.id.preview_surface);
        previewSurfaceView.getHolder().addCallback(shCallback);
    }

    protected void startCamera(final int id) {

        releaseCamera();
//		startPreview(id, openCamera(id));

        new AsyncTask<Integer, Void, Camera>() {

            @Override
            protected Camera doInBackground(Integer... ids) {
                return openCamera(ids[0]);
            }

            @Override
            protected void onPostExecute(Camera c) {
                startPreview(id, c);
            }

        }.execute(id);
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private void restartPreview() {
        if (camera == null) {
            return;
        }
        int degrees = 0;
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        Camera.CameraInfo ci = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, ci);
        if (ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            degrees += ci.orientation;
            degrees %= 360;
            degrees = 360 - degrees;
        } else {
            degrees = 360 - degrees;
            degrees += ci.orientation;
        }
        camera.setDisplayOrientation(degrees % 360);
        camera.startPreview();

        openChoosePictureResolution();
    }

    private void openChoosePictureResolution() {
        try {
            List<Camera.Size> supportedSizes;
            Camera.Parameters params = camera.getParameters();

            List<String> focusModes = params.getSupportedFocusModes();
            Log.d("focusModes=", focusModes.toString());
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                camera.setParameters(params);
            }
//		supportedSizes = params.getSupportedPreviewSizes();
//		for (Camera.Size sz : supportedSizes) {
//			Log.d(LOG_TAG, "supportedPreviewSizes " + sz.width + "x" + sz.height);
//		}
//		supportedSizes = params.getSupportedVideoSizes();
//		for (Camera.Size sz : supportedSizes) {
//			Log.d(LOG_TAG, "supportedVideoSizes " + sz.width + "x" + sz.height);
//		}

            supportedSizes = params.getSupportedPictureSizes();

            ArrayList<String> contacts = new ArrayList<>();

            for (Camera.Size sz : supportedSizes) {
                Log.d(LOG_TAG, "supportedPictureSizes " + sz.width + "x" + sz.height);
                TextView item = new TextView(this);
                item.setOnClickListener(this);
                item.setText("    " + sz.width + "x" + sz.height);
                contacts.add("    " + sz.width + "x" + sz.height);
                item.setTag(sz);
                //  lv.addView(item);
            }

        /*Spinner spinner = (Spinner) findViewById(R.id.spinner);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, contacts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);*/

            // lv.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Camera openCamera(int id) {

        Log.d(LOG_TAG, "opening camera " + id);
        Camera camera = null;
        try {
            camera = Camera.open(id);
            Log.d(LOG_TAG, "opened camera " + id);
        } catch (Exception e) {
            e.printStackTrace();
            camera.release();
            camera = null;
        }
        return camera;
    }

    private void setPictureSize(int width, int height) {
        Camera.Parameters params = camera.getParameters();
        params.setPictureSize(width, height);
        camera.setParameters(params);
    }

    private void startPreview(int id, Camera c) {
        if (c != null) {
            try {

                SurfaceView previewSurfaceView = (SurfaceView) findViewById(R.id.preview_surface);
                SurfaceHolder holder = previewSurfaceView.getHolder();
                c.setPreviewDisplay(holder);
                camera = c;
                cameraId = id;
                restartPreview();
            } catch (IOException e) {
                e.printStackTrace();
                c.release();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case 1:
                switchCamera();
                return true;

            case 2:
                try {
                    if (camera !=null) {
                        camera.takePicture(null, null, pictureCallback);
                    }else{
                        new classCustomToast(this).Show_ToastError("No se ha iniciado la cámara.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;

            case 3:

                startActivity(new Intent(this,FotosPendientesEnvio.class));

                return true;
        }

        return false;
    }
}
