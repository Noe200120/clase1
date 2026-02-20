
package com.example.entrevistasapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.entrevistasapp.R;
import com.example.entrevistasapp.adapters.EntrevistaAdapter;
import com.example.entrevistasapp.models.Entrevista;
import com.example.entrevistasapp.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    RecyclerView rv;
    EntrevistaAdapter adapter;
    List<Entrevista> lista = new ArrayList<>();
    FirebaseFirestore db = FirebaseUtil.getFirestore();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rv = findViewById(R.id.rvEntrevistas);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EntrevistaAdapter(this, lista);
        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddEditActivity.class));
        });

        cargarDatos();
    }

    private void cargarDatos() {
        db.collection("entrevistas")
                .orderBy("idOrden")
                .get()
                .addOnSuccessListener(this::onSuccess)
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void onSuccess(QuerySnapshot qs) {
        lista.clear();
        for (DocumentSnapshot d : qs.getDocuments()) {
            Entrevista e = d.toObject(Entrevista.class);
            if (e != null) {
                e.setId(d.getId());
                lista.add(e);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatos(); // recarga al volver
    }
}
