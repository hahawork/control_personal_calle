package com.gv.haha.supervisor.clases;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.gv.haha.supervisor.FotosPendientesEnvio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class TrabajarFotos {

    private Context mContext;
    Bitmap bmp;
    String FotoPath;

    public TrabajarFotos TFotos(Context context) {
        mContext = context;
        return this;
    }

    public TrabajarFotos RotarFoto(String photopath) {

        bmp = BitmapFactory.decodeFile(photopath);
        FotoPath = photopath;

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(new File(photopath));
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();

        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return this;
    }

    public boolean Mostrar(){

        return true;
    }

}
