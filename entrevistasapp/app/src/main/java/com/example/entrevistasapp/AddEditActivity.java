package com.example.entrevistasapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.example.entrevistasapp.R;
import com.example.entrevistasapp.models.Entrevista;
import com.example.entrevistasapp.utils.FirebaseUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddEditActivity extends AppCompatActivity {

    private static final int REQ_PICK_IMAGE = 1001;

    private EditText etIdOrden, etDescripcion, etPeriodista;
    private ImageView imgPreview;
    private Button btnPickImage, btnRecord, btnSave;
    private TextView tvAudioStatus;

    private Uri imageUri = null;
    private String audioPath = null;
    private MediaRecorder recorder;
    private boolean isRecording = false;
    private String docIdEditing = null;

    private FirebaseFirestore db = FirebaseUtil.getFirestore();
    private StorageReference storageRef = FirebaseUtil.getStorageRef();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        etIdOrden = findViewById(R.id.etIdOrden);
        etDescripcion = findViewById(R.id.etDescripcion);
        etPeriodista = findViewById(R.id.etPeriodista);
        imgPreview = findViewById(R.id.imgPreview);
        btnPickImage = findViewById(R.id.btnPickImage);
        btnRecord = findViewById(R.id.btnRecordAudio);
        btnSave = findViewById(R.id.btnSave);
        tvAudioStatus = findViewById(R.id.tvAudioStatus);

        // Si vienen datos para editar
        if (getIntent().hasExtra("id")) {
            docIdEditing = getIntent().getStringExtra("id");
            cargarParaEditar(docIdEditing);
        }

        btnPickImage.setOnClickListener(v -> pedirPermisosGaleria());

        btnRecord.setOnClickListener(v -> {
            if (!isRecording) startRecording();
            else stopRecording();
        });

        btnSave.setOnClickListener(v -> guardarEntrevista());
    }

    private void cargarParaEditar(String id) {
        db.collection("entrevistas").document(id).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Entrevista e = doc.toObject(Entrevista.class);
                        if (e != null) {
                            etIdOrden.setText(String.valueOf(e.getIdOrden()));
                            etDescripcion.setText(e.getDescripcion());
                            etPeriodista.setText(e.getPeriodista());

                            if (e.getImageUrl() != null) {
                                Glide.with(this).load(e.getImageUrl()).into(imgPreview);
                            }
                            if (e.getAudioUrl() != null) {
                                tvAudioStatus.setText("Audio cargado (puedes regrabar)");
                                audioPath = e.getAudioUrl(); // Se guarda temporalmente
                            }
                        }
                    }
                });
    }

    // -------------------- PERMISOS --------------------------

    private void pedirPermisosGaleria() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.RECORD_AUDIO
                    },
                    2001
            );
        } else {
            requestPermissions(
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO
                    },
                    2001
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 2001) {
            pickImage();
        }
    }

    // ---------------------- IMAGEN -----------------------------

    private void pickImage() {
        Intent i = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQ_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_PICK_IMAGE &&
                resultCode == Activity.RESULT_OK &&
                data != null) {

            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(imgPreview);
        }
    }

    // ---------------------- AUDIO -------------------------------

    private void startRecording() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Debe permitir grabar audio", Toast.LENGTH_SHORT).show();
            return;
        }

        audioPath = getExternalCacheDir().getAbsolutePath() + "/" +
                UUID.randomUUID().toString() + ".3gp";

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        // Necesario para Android 11+
        recorder.setAudioEncodingBitRate(128000);
        recorder.setAudioSamplingRate(44100);

        recorder.setOutputFile(audioPath);

        try {
            recorder.prepare();
            recorder.start();
            isRecording = true;
            btnRecord.setText("Detener Grabación");
            tvAudioStatus.setText("Grabando...");
        } catch (Exception e) {
            Toast.makeText(this, "Error al grabar: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void stopRecording() {
        try {
            recorder.stop();
        } catch (Exception ignored) {}

        recorder.release();
        recorder = null;

        isRecording = false;
        btnRecord.setText("Regrabar Audio");
        tvAudioStatus.setText("Audio grabado");
    }

    // ---------------------- GUARDAR ------------------------------

    private void guardarEntrevista() {
        String sIdOrden = etIdOrden.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String periodista = etPeriodista.getText().toString().trim();

        if (sIdOrden.isEmpty()) {
            etIdOrden.setError("Requerido");
            return;
        }

        int idOrden = Integer.parseInt(sIdOrden);

        // SUBIR IMAGEN Y/O AUDIO SEGÚN CORRESPONDA
        if (imageUri != null) {
            subirImagenYAudioLuegoGuardar(imageUri, audioPath, idOrden, descripcion, periodista);

        } else if (audioPath != null && audioPath.startsWith("http")) {
            guardarDocumento(null, audioPath, idOrden, descripcion, periodista);

        } else if (audioPath != null) {
            subirAudioYGuardar(null, audioPath, idOrden, descripcion, periodista);

        } else {
            guardarDocumento(null, null, idOrden, descripcion, periodista);
        }
    }

    private void subirImagenYAudioLuegoGuardar(Uri imageUri,
                                               String audioLocalPath,
                                               int idOrden,
                                               String descripcion,
                                               String periodista) {

        String imageName = "images/" + UUID.randomUUID().toString();
        StorageReference imgRef = storageRef.child(imageName);

        imgRef.putFile(imageUri).addOnSuccessListener(task ->
                imgRef.getDownloadUrl().addOnSuccessListener(uri -> {

                    String imgUrl = uri.toString();

                    if (audioLocalPath != null && !audioLocalPath.startsWith("http")) {
                        subirAudioYGuardar(imgUrl, audioLocalPath, idOrden, descripcion, periodista);
                    } else {
                        guardarDocumento(imgUrl, audioLocalPath, idOrden, descripcion, periodista);
                    }
                })
        ).addOnFailureListener(e ->
                Toast.makeText(this, "Error subir imagen: " + e.getMessage(), Toast.LENGTH_LONG).show()
        );
    }

    private void subirAudioYGuardar(String imageUrl,
                                    String audioLocalPath,
                                    int idOrden,
                                    String descripcion,
                                    String periodista) {

        if (audioLocalPath == null) {
            guardarDocumento(imageUrl, null, idOrden, descripcion, periodista);
            return;
        }

        if (audioLocalPath.startsWith("http")) {
            guardarDocumento(imageUrl, audioLocalPath, idOrden, descripcion, periodista);
            return;
        }

        Uri file = Uri.fromFile(new File(audioLocalPath));
        String audioName = "audios/" + UUID.randomUUID().toString() + ".3gp";
        StorageReference audioRef = storageRef.child(audioName);

        audioRef.putFile(file).addOnSuccessListener(task ->
                audioRef.getDownloadUrl().addOnSuccessListener(uri -> {

                    String audioUrl = uri.toString();
                    guardarDocumento(imageUrl, audioUrl, idOrden, descripcion, periodista);
                })
        ).addOnFailureListener(e ->
                Toast.makeText(this, "Error subir audio: " + e.getMessage(), Toast.LENGTH_LONG).show()
        );
    }

    private void guardarDocumento(String imageUrl,
                                  String audioUrl,
                                  int idOrden,
                                  String descripcion,
                                  String periodista) {

        Map<String, Object> map = new HashMap<>();
        map.put("idOrden", idOrden);
        map.put("descripcion", descripcion);
        map.put("periodista", periodista);
        map.put("fecha", Timestamp.now());
        map.put("imageUrl", imageUrl);
        map.put("audioUrl", audioUrl);

        if (docIdEditing != null) {
            db.collection("entrevistas").document(docIdEditing)
                    .set(map)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Entrevista actualizada", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        } else {
            db.collection("entrevistas")
                    .add(map)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(this, "Entrevista guardada", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        }
    }
}
