package com.gv.haha.supervisor.clases;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gv.haha.supervisor.R;

public class classCustomToast {

    private Activity mActivity;

    public classCustomToast(Activity activity) {
        mActivity = activity;
    }

    // Custom Toast Method
    public void Show_ToastError(String mensaje) {

        // Layout Inflater for inflating custom view
        LayoutInflater inflater = mActivity.getLayoutInflater();

        // inflate the layout over view
        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) mActivity.findViewById(R.id.toast_root));

        // Get TextView id and set error
        TextView text = (TextView) layout.findViewById(R.id.toast_error);
        text.setText(mensaje);

        Toast toast = new Toast(mActivity);// Get Toast Context
        toast.setGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 0);// Set
        // Toast
        // gravity
        // and
        // Fill
        // Horizoontal

        toast.setDuration(Toast.LENGTH_SHORT);// Set Duration
        toast.setView(layout); // Set Custom View over toast

        toast.show();// Finally show toast
    }

    public void Toast(String Texto, int image) {
        LayoutInflater inflater = mActivity.getLayoutInflater();

        View layout = inflater.inflate(R.layout.custom_layout_toast, (ViewGroup) mActivity.findViewById(R.id.toast_layout_root)); //"inflamos" nuestro layout

        TextView text = (TextView) layout.findViewById(R.id.text_toast);
        ImageView imgToast = (ImageView) layout.findViewById(R.id.imgToast);
        text.setText(Texto); //texto a mostrar y asignado al textView de nuestro layout
        imgToast.setImageResource(image);
        Toast toast = new Toast(mActivity); //Instanciamos un objeto Toast
        toast.setGravity(Gravity.TOP, 10, 20); //lo situamos centrado arriba en la pantalla
        toast.setDuration(Toast.LENGTH_LONG); //duracion del toast
        toast.setView(layout); //asignamos nuestro layout personalizado al objeto Toast
        toast.show(); //mostramos el Toast en pantalla
    }

}