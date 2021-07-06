package com.gv.haha.supervisor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.gv.haha.supervisor.clases.SQLHelper;
import com.gv.haha.supervisor.clases.TrabajarFotos;
import com.gv.haha.supervisor.clases.classCustomToast;
import com.gv.haha.supervisor.clases.classMetodosGenerales;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.gv.haha.supervisor.clases.interfVariablesGenerales.HOST_NAME;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.TBL_ASISTENCIA;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.TBL_PDV_ASISTENCIA;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.stBorrarFotografia;
import static com.gv.haha.supervisor.clases.interfVariablesGenerales.tblAsistenciaFoto;

public class FotosPendientesEnvio extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FotoAdapter adapter;
    SQLiteDatabase db;
    private List<Foto> fotoList;
    SharedPreferences setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fotos_pendientes_envio);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initCollapsingToolbar();

        db = new SQLHelper(this).getWritableDatabase();
        setting = PreferenceManager.getDefaultSharedPreferences(this);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        fotoList = new ArrayList<>();
        adapter = new FotoAdapter(this, fotoList);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        prepareFotos();

        try {
            Glide.with(this).load(R.drawable.bg_fotos_pendientes).into((ImageView) findViewById(R.id.backdrop));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializing collapsing toolbar
     * Will show and hide the toolbar title on scroll
     */
    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(getString(R.string.app_name));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    /**
     * Adding few albums for testing
     */
    private void prepareFotos() {

        try {

            Cursor cursor = db.rawQuery("select * from " + tblAsistenciaFoto + " where estEnvio = 0", null);
            //si existe el registro
            //Id INTEGER PRIMARY KEY AUTOINCREMENT, IdAsistencia INTEGER, puntoventa integer, FechaAsistenciaEntrada datetime, AsistenciaConInternet integer,  fotopath TEXT, Comentarios TEXT, estEnvio
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

                String Id = cursor.getString(cursor.getColumnIndex("Id"));
                String IdAsistencia = cursor.getString(cursor.getColumnIndex("IdAsistencia"));
                String puntoventa = cursor.getString(cursor.getColumnIndex("puntoventa"));
                String NombPdV = getNombById(TBL_PDV_ASISTENCIA, "NombrePdV", "IdPdV", puntoventa);
                String FechaAsistenciaEntrada = cursor.getString(cursor.getColumnIndex("FechaAsistenciaEntrada"));
                String AsistenciaConInternet = cursor.getString(cursor.getColumnIndex("AsistenciaConInternet"));
                String fotopath = cursor.getString(cursor.getColumnIndex("fotopath"));
                int idOnline = Integer.parseInt(getNombById(TBL_ASISTENCIA,"IdEnviado","IdAsistencia",IdAsistencia));
                String Comentarios = "";//cursor.getString(cursor.getColumnIndex("Comentarios"));

                if (new File(fotopath).exists()) {

                    Foto foto = new Foto(Id, IdAsistencia, puntoventa, NombPdV, FechaAsistenciaEntrada, AsistenciaConInternet, fotopath, Comentarios, idOnline);
                    fotoList.add(foto);

                } else {
                    db.delete(tblAsistenciaFoto, "Id = " + Id, null);
                }
            }

            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private String getNombById(String Tabla, String CampoObtener, String CampoFiltro, String Valor) {
        try {

            Cursor cursor = db.rawQuery(String.format("SELECT %s FROM %s WHERE %s = '%s'", CampoObtener, Tabla, CampoFiltro, Valor), null);
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(CampoObtener));
            } else {
                return "N/D";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "error" + e.getMessage();
        }
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    private void DialogFotos(final Foto foto, final int posicion) {

        try {
            final Dialog dialog = new Dialog(this, R.style.FullScreenDialogStyle);
            dialog.setContentView(R.layout.custom_dialog_foto_visor);

            Toolbar toolbar = (Toolbar) dialog.findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toolbar.setTitle("Opciones...");//new File(fotopath).getName());
            toolbar.setNavigationIcon(R.drawable.ic_dialog_close_light);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });


            // loading album cover using Glide library
            final ImageView ivfoto = dialog.findViewById(R.id.iv_FotoVisor_dg);
            Glide.with(this).load(foto.getFotopath()).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(ivfoto);

            final TextInputEditText etComentario = (TextInputEditText) dialog.findViewById(R.id.etComentarios_visorFoto);

            //para el boton borrar
            ((Button) dialog.findViewById(R.id.btnDelete_visorFoto)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(FotosPendientesEnvio.this)
                            .setTitle("Borrar foto.")
                            .setMessage("¿Seguro que deseas borrar la fotografia?")
                            .setPositiveButton("Borrar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface alert, int which) {
                                    if (new File(foto.getFotopath()).delete()) {
                                        dialog.dismiss();
                                        fotoList.remove(posicion);
                                        adapter.notifyDataSetChanged();
                                        new classCustomToast(FotosPendientesEnvio.this).Toast("Borrado con exito.", R.drawable.ic_success);
                                    } else {
                                        new classCustomToast(FotosPendientesEnvio.this).Show_ToastError("Ha fallado el intento.");
                                    }
                                }
                            })
                            .setNegativeButton("No borrar", null)
                            .show();
                }
            });

            //para el boton rotar
            ((Button) dialog.findViewById(R.id.btnRotate_visorFoto)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ivfoto.setImageBitmap(null);
                    if (new TrabajarFotos().TFotos(FotosPendientesEnvio.this).RotarFoto(foto.getFotopath()).Mostrar()) {
                        Glide.with(FotosPendientesEnvio.this).load(foto.getFotopath()).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(ivfoto);
                    }
                }
            });

            ((Button) dialog.findViewById(R.id.btnShare_visorFoto)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String coment = TextUtils.isEmpty(etComentario.getText()) ? "" : etComentario.getText().toString();

                    new classMetodosGenerales(FotosPendientesEnvio.this)
                            .CompartirFoto(foto.getFotopath(), foto.getNombrePdv(), coment);
                }
            });

            ((Button) dialog.findViewById(R.id.btnSend_visorFoto)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // si la asistencia ya fue enviada al servidor
                    if (foto.getIdEnviado() > 0) {
                        foto.setComentarios(etComentario.getText().toString());
                        EnviarImagen(foto, dialog, posicion);
                    } else {
                        new AlertDialog.Builder(FotosPendientesEnvio.this)
                                .setIcon(R.drawable.ic_error)
                                .setTitle("No se puede enviar la foto.")
                                .setTitle("La asistencia en " + foto.getNombrePdv() +" aun esta pendiente de envio.")
                                .setPositiveButton("Env. Pendientes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startActivity(new Intent(FotosPendientesEnvio.this, AsistenciasPendientesEnvio.class));
                                        FotosPendientesEnvio.this.finish();
                                    }
                                })
                                .setNegativeButton("Entendido", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface alert, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    }
                }
            });

            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void EnviarImagen(final Foto foto, final Dialog dialog, final int posicion) {
        try {

            String URL_SUBIRPICTURE = HOST_NAME + "ws/imageUpload.php";

            if (foto.getFotopath().length() > 0) {

                String uploadId = UUID.randomUUID().toString();
                new MultipartUploadRequest(this, uploadId, URL_SUBIRPICTURE)
                        .addFileToUpload(foto.getFotopath(), "uploaded_file")
                        .addParameter("idUsuAsist", foto.getIdEnviado()+"")
                        .addParameter("Comentario", foto.getComentarios())
                        //aqui otros parametros
                        .setMaxRetries(2)
                        .setDelegate(new UploadStatusDelegate() {
                            @Override
                            public void onProgress(UploadInfo uploadInfo) {
                                ((ProgressBar) dialog.findViewById(R.id.progressBar)).setProgress(uploadInfo.getProgressPercent());

                            }

                            @Override
                            public void onError(UploadInfo uploadInfo, Exception e) {
                            }

                            @Override
                            public void onCompleted(UploadInfo uploadInfo, ServerResponse serverResponse) {
                                //ELiminar imagen
                                File eliminar = new File(foto.getFotopath());
                                if (eliminar.exists() && setting.getBoolean(stBorrarFotografia, false)) {
                                    if (eliminar.delete()) {

                                    } else {
                                    }
                                }
                                Toast.makeText(FotosPendientesEnvio.this, "Imagen subida exitosamente.", Toast.LENGTH_SHORT).show();

                                try {
                                    db.delete(tblAsistenciaFoto, "Id = " + foto.getId() + " AND IdAsistencia = " + foto.getIdAsistencia(), null);
                                    dialog.dismiss();
                                    fotoList.remove(posicion);
                                    adapter.notifyDataSetChanged();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onCancelled(UploadInfo uploadInfo) {
                            }
                        })

                        //esto muestra una notificacion en la barra con wel progreso de subida
                        .setNotificationConfig(new UploadNotificationConfig()
                                .setTitle("Subiendo fotografía")
                                .setAutoClearOnSuccess(true)
                                .setIcon(R.mipmap.ic_launcher_round)

                        )
                        .startUpload();

            }


        } catch (Exception exc) {
            System.out.println(exc.getMessage() + " " + exc.getLocalizedMessage());
        }
    }

    public class Foto {


        private String Id, IdAsistencia, puntoventa, NombrePdv, FechaAsistenciaEntrada, AsistenciaConInternet, fotopath, Comentarios;
        private int IdEnviado;

        public Foto() {
        }

        public Foto(String id, String idAsistencia, String puntoventa, String nombrePdv, String fechaAsistenciaEntrada, String asistenciaConInternet, String fotopath, String comentarios, int idEnviado) {
            this.Id = id;
            this.IdAsistencia = idAsistencia;
            this.puntoventa = puntoventa;
            this.NombrePdv = nombrePdv;
            this.FechaAsistenciaEntrada = fechaAsistenciaEntrada;
            this.AsistenciaConInternet = asistenciaConInternet;
            this.fotopath = fotopath;
            this.Comentarios = comentarios;
            this.IdEnviado = idEnviado;
        }

        public String getId() {
            return Id;
        }

        public String getIdAsistencia() {
            return IdAsistencia;
        }

        public String getPuntoventa() {
            return puntoventa;
        }

        public String getNombrePdv() {
            return NombrePdv;
        }

        public String getFechaAsistenciaEntrada() {
            return FechaAsistenciaEntrada;
        }

        public String getAsistenciaConInternet() {
            return AsistenciaConInternet;
        }

        public String getFotopath() {
            return fotopath;
        }

        public String getComentarios() {
            return Comentarios;
        }

        public int getIdEnviado() {
            return IdEnviado;
        }

        public void setComentarios(String comentarios) {
            Comentarios = comentarios;
        }

        public void setIdEnviado(int idEnviado) {
            IdEnviado = idEnviado;
        }
    }

    public class FotoAdapter extends RecyclerView.Adapter<FotoAdapter.MyViewHolder> {

        private Context mContext;
        private List<Foto> fotoList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView title, count;
            public ImageView thumbnail, overflow;

            public MyViewHolder(View view) {
                super(view);
                title = (TextView) view.findViewById(R.id.title);
                count = (TextView) view.findViewById(R.id.count);
                thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
                overflow = (ImageView) view.findViewById(R.id.overflow);
            }
        }


        public FotoAdapter(Context mContext, List<Foto> fotoList) {
            this.mContext = mContext;
            this.fotoList = fotoList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fotos_card, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            final Foto foto = fotoList.get(position);
            holder.title.setText(foto.getNombrePdv());
            holder.count.setText(foto.getFechaAsistenciaEntrada());

            // loading album cover using Glide library
            Glide.with(mContext).load(foto.getFotopath()).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(holder.thumbnail);

            holder.overflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopupMenu(holder.overflow);
                }
            });

            holder.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    DialogFotos(foto, position);
                }
            });
        }

        /**
         * Showing popup menu when tapping on 3 dots
         */
        private void showPopupMenu(View view) {
            // inflate menu
            PopupMenu popup = new PopupMenu(mContext, view);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_fotos, popup.getMenu());
            popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
            popup.show();
        }

        /**
         * Click listener for popup menu items
         */
        class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

            public MyMenuItemClickListener() {
            }

            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_share:
                        Toast.makeText(mContext, "Compartir foto", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.action_delete:
                        Toast.makeText(mContext, "Borrar foto", Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                }
                return false;
            }
        }

        @Override
        public int getItemCount() {
            return fotoList.size();
        }
    }
}
