package com.example.entrevistasapp.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseUtil {
    private static FirebaseFirestore firestore;
    private static FirebaseStorage storage;

    public static FirebaseFirestore getFirestore() {
        if (firestore == null) firestore = FirebaseFirestore.getInstance();
        return firestore;
    }

    public static FirebaseStorage getStorage() {
        if (storage == null) storage = FirebaseStorage.getInstance();
        return storage;
    }

    public static StorageReference getStorageRef() {
        return getStorage().getReference();
    }
}
