package helloandroid.m2dl.tropchoupi;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Message;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class FireBase {


    public void putPhoto(Bitmap bitmap){
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("Hello, World!");
    }
            // --------------------
            // REST REQUESTS
            // --------------------


        // 1 - Upload a picture in Firebase and send a message
        public void uploadPhoto(Bitmap bitmap) {
            String uuid = UUID.randomUUID().toString(); // GENERATE UNIQUE STRING
            // A - UPLOAD TO GCS

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            StorageReference mImageRef = FirebaseStorage.getInstance().getReference(uuid);
            mImageRef.putBytes(data).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                }
            });

        }



}
