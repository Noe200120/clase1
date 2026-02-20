package com.example.entrevistasapp;


import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.entrevistasapp.R;
import com.example.entrevistasapp.models.Entrevista;
import com.example.entrevistasapp.utils.FirebaseUtil;

public class ViewActivity extends AppCompatActivity {

    ImageView imgDetail;
    TextView tvDescripcion, tvPeriodista, tvFecha;
    Button btnPlay, btnDelete;
    String docId;
    Entrevista entrevista;
    MediaPlayer mp;
    FirebaseFirestore db = FirebaseUtil.getFirestore();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        imgDetail = findViewById(R.id.imgDetail);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvPeriodista = findViewById(R.id.tvPeriodista);
        tvFecha = findViewById(R.id.tvFecha);
        btnPlay = findViewById(R.id.btnPlay);
        btnDelete = findViewById(R.id.btnDelete);

        docId = getIntent().getStringExtra("id");
        cargarDetalle();

        btnPlay.setOnClickListener(v -> {
            if (entrevista != null && entrevista.getAudioUrl() != null) {
                reproducir(entrevista.getAudioUrl());
            } else {
                Toast.makeText(this, "No hay audio", Toast.LENGTH_SHORT).show();
            }
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirmar")
                    .setMessage("¿Eliminar entrevista?")
                    .setPositiveButton("Sí", (dialog, which) -> eliminar())
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void cargarDetalle() {
        db.collection("entrevistas").document(docId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                entrevista = doc.toObject(Entrevista.class);
                if (entrevista != null) {
                    Glide.with(this).load(entrevista.getImageUrl()).into(imgDetail);
                    tvDescripcion.setText(entrevista.getDescripcion());
                    tvPeriodista.setText(entrevista.getPeriodista());
                    tvFecha.setText(entrevista.getFecha() != null ? entrevista.getFecha().toDate().toString() : "");
                }
            }
        });
    }

    private void reproducir(String url) {
        if (mp != null && mp.isPlaying()) {
            mp.stop();
            mp.release();
            mp = null;
            btnPlay.setText("Reproducir Audio");
            return;
        }
        mp = new MediaPlayer();
        try {
            mp.setDataSource(url);
            mp.prepareAsync();
            btnPlay.setText("Cargando...");
            mp.setOnPreparedListener(mediaPlayer -> {
                mediaPlayer.start();
                btnPlay.setText("Detener");
            });
            mp.setOnCompletionListener(mediaPlayer -> {
                btnPlay.setText("Reproducir Audio");
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reproducir audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void eliminar() {
        db.collection("entrevistas").document(docId).delete().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> Toast.makeText(this, "Error eliminar: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mp != null) {
            if (mp.isPlaying()) mp.stop();
            mp.release();
            mp = null;
        }
    }
}
