package com.gv.haha.supervisor.clases;

import android.view.ContextMenu;
import android.view.View;

import org.json.JSONObject;

public interface AsyncTaskComplete{

    int option_registro = 1;
    int option_descPdv = 2;
    int option_historial = 3;
    int option_guardar_pdv = 4;
    int option_enviar_ubic_simul = 5;
    int option_obtener_ubic_simul = 6;

    // Define data you like to return from AysncTask
    public void onAsyncTaskComplete(JSONObject result, int option);


}
