package com.sze.findmeamechanic.managers;

import android.graphics.Bitmap;
import android.net.Uri;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class ImageUploadManager {
    private String imageId;
    private String pathToImage;

    public ImageUploadManager() { }

    public void profilePictureUploader(Bitmap bitmap, final FirestoreManager firestoreManager, final UploadUrlCallback callback) {
        byte[] data = getImageToBytes(bitmap);
        imageId = UUID.randomUUID().toString();

        firestoreManager.getUserPictureRef(imageId).putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                firestoreManager.getUserPictureRef(imageId).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        pathToImage = task.getResult().toString();
                        callback.onUploadSuccessCallback(pathToImage);

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onUploadFailedCallback();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progressPercent = (100.00 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                callback.onUploadProgressListener(progressPercent);
            }
        });
    }

    public void jobPictureUploader(Bitmap bitmap, final FirestoreManager firestoreManager, final UploadUrlCallback callback) {
        byte[] data = getImageToBytes(bitmap);
        imageId = UUID.randomUUID().toString();

        firestoreManager.getJobPictureRef(imageId).putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                firestoreManager.getJobPictureRef(imageId).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        pathToImage = task.getResult().toString();
                        callback.onUploadSuccessCallback(pathToImage);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onUploadFailedCallback();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progressPercent = (100.00 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                callback.onUploadProgressListener(progressPercent);
            }
        });
    }

    private byte[] getImageToBytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        return baos.toByteArray();
    }

    public interface UploadUrlCallback {
        void onUploadSuccessCallback(String imageUrl);
        void onUploadFailedCallback();
        void onUploadProgressListener(double progress);
    }

}
