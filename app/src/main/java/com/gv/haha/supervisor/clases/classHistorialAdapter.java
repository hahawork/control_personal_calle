package com.gv.haha.supervisor.clases;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.plus.model.people.Person;
import com.gv.haha.supervisor.R;

import java.util.ArrayList;
import java.util.List;

public class classHistorialAdapter extends RecyclerView.Adapter<classHistorialAdapter.MyViewHolder> {
    private List<Historia> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        CardView cv;
        TextView tvNombrePdv;
        TextView tvHoras;
        TextView tvComentario;

        MyViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            tvNombrePdv = (TextView) itemView.findViewById(R.id.tvNombrePDV_H);
            tvHoras = (TextView) itemView.findViewById(R.id.tvHoras_H);
            tvComentario = (TextView) itemView.findViewById(R.id.tvComentarios_H);
        }

    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public classHistorialAdapter(List<Historia> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_historial, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.tvNombrePdv.setText(mDataset.get(position).PdV);
        holder.tvHoras.setText(mDataset.get(position).Horas);
        holder.tvComentario.setText(mDataset.get(position).Coment);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    public static class Historia {
        String  PdV;
        String Horas;
        String  Coment;

        public Historia(String pdv, String horas, String coment) {
            this.PdV = pdv;
            this.Horas = horas;
            this.Coment = coment;
        }
    }
}