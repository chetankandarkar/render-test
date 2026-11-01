package com.example.neoncache.controller;



import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.neoncache.service.FirebaseImageLogService;
import com.example.neoncache.service.ImageService;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;
    private final FirebaseImageLogService firebaseService;

    public ImageController(
            ImageService imageService,
            FirebaseImageLogService firebaseService) {

        this.imageService = imageService;
        this.firebaseService = firebaseService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file) {

        try {

            // 1. Upload image to Cloudinary
            Map result = imageService.uploadImage(file);

            // 2. Capture Cloudinary response received time
            Instant responseReceivedTime = Instant.now();

            // 3. Prepare Firebase record
            Map<String, Object> firebaseData = new HashMap<>();

            firebaseData.put("originalFileName", file.getOriginalFilename());
            firebaseData.put("contentType", file.getContentType());
            firebaseData.put("imageUrl", result.get("secure_url"));
            firebaseData.put("publicId", result.get("public_id"));
            firebaseData.put("format", result.get("format"));
            firebaseData.put("size", result.get("bytes"));

            firebaseData.put(
                    "cloudinaryResponseReceivedAt",
                    responseReceivedTime.toString()
            );

            // 4. Save details in Firebase and wait for confirmation
            String firebaseId =
                    firebaseService.saveImageDetails(firebaseData);

            // 5. Return response only after Firebase save succeeds
            Map<String, Object> response = new HashMap<>();

            response.put("success", true);
            response.put("message", "Image uploaded successfully");
            response.put("firebaseId", firebaseId);
            response.put("imageUrl", result.get("secure_url"));
            response.put("publicId", result.get("public_id"));
            response.put("format", result.get("format"));
            response.put("size", result.get("bytes"));
            response.put(
                    "responseReceivedAt",
                    responseReceivedTime.toString()
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", e.getMessage()
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "success", false,
                            "message", "Image upload or Firebase save failed"
                    )
            );
        }
    }
}