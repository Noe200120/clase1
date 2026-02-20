package com.example.entrevistasapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.entrevistasapp.models.Entrevista;
import com.example.entrevistasapp.ViewActivity;
import com.example.entrevistasapp.R;

import java.util.List;

public class EntrevistaAdapter extends RecyclerView.Adapter<EntrevistaAdapter.ViewHolder> {

    private List<Entrevista> entrevistas;
    private Context ctx;

    public EntrevistaAdapter(Context ctx, List<Entrevista> entrevistas) {
        this.ctx = ctx;
        this.entrevistas = entrevistas;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entrevista, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Entrevista e = entrevistas.get(position);
        holder.tvTitulo.setText("Orden: " + e.getIdOrden());
        holder.tvPeriodista.setText(e.getPeriodista() == null ? "" : e.getPeriodista());
        Glide.with(ctx)
                .load(e.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_report_image)
                .into(holder.imgThumb);

        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(ctx, ViewActivity.class);
            i.putExtra("id", e.getId());
            ctx.startActivity(i);
        });

        holder.btnEdit.setOnClickListener(v -> {
            Intent i = new Intent(ctx, com.example.entrevistasapp.AddEditActivity.class);
            i.putExtra("id", e.getId());
            ctx.startActivity(i);
        });
    }

    @Override
    public int getItemCount() { return entrevistas.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvTitulo, tvPeriodista;
        ImageButton btnEdit;

        public ViewHolder(View item) {
            super(item);
            imgThumb = item.findViewById(R.id.imgThumb);
            tvTitulo = item.findViewById(R.id.tvTitulo);
            tvPeriodista = item.findViewById(R.id.tvPeriodista);
            btnEdit = item.findViewById(R.id.btnEdit);
        }
    }
}
