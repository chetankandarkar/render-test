package com.example.neoncache.service;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class FirebaseImageLogService {

    private final DatabaseReference imageRef;

    public FirebaseImageLogService(FirebaseDatabase firebaseDatabase) {
        this.imageRef = firebaseDatabase.getReference("imageUploads");
    }

    public String saveImageDetails(Map<String, Object> details)
            throws Exception {

        DatabaseReference newRecord = imageRef.push();

        // Wait until Firebase confirms the write.
        newRecord.setValueAsync(details)
                .get(15, TimeUnit.SECONDS);

        return newRecord.getKey();
    }
}
