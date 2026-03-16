package com.wardrobe.digital_wardrobe;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;
import com.wardrobe.digital_wardrobe.ClothingItem;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class FirebaseService {

    public String uploadAndGetUrl(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID().toString() + ".jpg";
        Bucket bucket = StorageClient.getInstance().bucket();

        // Upload the file
        Blob blob = bucket.create(fileName, file.getBytes(), file.getContentType());

        // Generate a public-facing URL valid for a long time (e.g., 1 year)
        // This allows your designers to see the images in <img> tags
        return blob.signUrl(365, TimeUnit.DAYS).toString();
    }

    public void saveItem(ClothingItem item) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        db.collection("clothes").document().set(item);
    }

    public List<ClothingItem> getItems(String userId, String category) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference clothes = db.collection("clothes");

        // Query: Filter by User ID (Isolation)
        Query query = clothes.whereEqualTo("userId", userId);

        // Query: Filter by Category if not "All"
        if (category != null && !category.equals("All")) {
            query = query.whereEqualTo("category", category);
        }

        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        return querySnapshot.get().getDocuments().stream()
                .map(doc -> {
                    ClothingItem item = doc.toObject(ClothingItem.class);
                    item.setId(doc.getId()); // <--- CRITICAL: Manually set the ID
                    return item;
                })
                .collect(Collectors.toList());
    }
    public void deleteItem(String id, String imageUrl) throws Exception {
        Firestore db = FirestoreClient.getFirestore();

        // 1. Delete the record from Firestore
        db.collection("clothes").document(id).delete().get();

        // 2. Delete the actual image from Storage
        if (imageUrl != null && !imageUrl.isEmpty() && imageUrl.contains("firebasestorage.googleapis.com")) {
            try {
                // Extract filename from the URL (everything between /o/ and ?)
                String fileName = imageUrl.substring(imageUrl.lastIndexOf("/o/") + 3, imageUrl.indexOf("?"));
                // Firebase encodes slashes as %2F, we need to decode it back
                fileName = java.net.URLDecoder.decode(fileName, "UTF-8");

                Bucket bucket = StorageClient.getInstance().bucket();
                boolean deleted = bucket.get(fileName).delete();

                if (deleted) {
                    System.out.println("File deleted from storage: " + fileName);
                }
            } catch (Exception e) {
                System.err.println("Could not delete file from storage: " + e.getMessage());
                // We don't throw the error because the DB record is already gone
            }
        }
    }
}