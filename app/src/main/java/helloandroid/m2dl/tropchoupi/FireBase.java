package helloandroid.m2dl.tropchoupi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.UUID;

public class FireBase{

    MapsActivity mapsActivity;
    public FireBase(MapsActivity mapsActivity) {
        this.mapsActivity = mapsActivity;
    }

    // --------------------
    // REST REQUESTS
    // --------------------


        // 1 - Upload a picture in Firebase and send a message
        public void uploadPhoto(Bitmap bitmap,double latitude,double longitutde) {
            String uuid = UUID.randomUUID().toString(); // GENERATE UNIQUE STRING
            // A - UPLOAD TO GCS

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            StorageReference mImageRef = FirebaseStorage.getInstance().getReference("image")
                    .child(uuid+"#" + latitude + "#" + longitutde );
            mImageRef.putBytes(data).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                }
            });

        }

        public void getAllPhotos() {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference listRef = storage.getReference("image");

            listRef.listAll()
                    .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                        @Override
                        public void onSuccess(ListResult listResult) {
                            for (StorageReference prefix : listResult.getPrefixes()) {
                            }
                            for (StorageReference item : listResult.getItems()) {
                                final String nameItem = item.getName();
                                final long ONE_MEGABYTE = 1024 * 1024;
                                item.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        /**************************************/
                                        mapsActivity.runOnUiThread(new Runnable() {
                                            public void run() {
                                                mapsActivity.putOneMarker(recupCoord(nameItem),Bitmap.createScaledBitmap(bitmap,300,300,true));
                                                mapsActivity.onMapReady(mapsActivity.mGoogleMap);
                                            }
                                        });
                                        /**************************************/
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        System.out.println("Error download");

                                    }
                                });


                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Uh-oh, an error occurred!
                        }
                    });

        }

    public LatLng recupCoord(String str){
        String[] split = str.split("#");
        return new LatLng(Double.parseDouble(split[1]),Double.parseDouble(split[2]));
    }


}
