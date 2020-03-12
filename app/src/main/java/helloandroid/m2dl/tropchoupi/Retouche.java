package helloandroid.m2dl.tropchoupi;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.util.List;

public class Retouche extends MainActivity implements SensorEventListener {
    ImageView retoucheImageView;
    ImageView useLightSensor;
    Bitmap bitmap;
    SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Sensor lightSensor;
    private ImageView toDrag;
    private String TAG;
    private int GALLERY_REQUEST_CODE = 200;
    private ConstraintLayout constraintLayoutImageView;
    private Location lastKnownLocation;
    private FusedLocationProviderClient fusedLocationClient;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retouche);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            lastKnownLocation = location;
                        }
                    }
                });
        this.useLightSensor = findViewById(R.id.useLightSensor);
        this.retoucheImageView = findViewById(R.id.retoucheImageView);
        this.toDrag = findViewById(R.id.toDrag);
        this.constraintLayoutImageView = findViewById(R.id.constraintLayoutImageView);

        toDrag.setOnLongClickListener(new MyOnLongClickListener());


        toDrag.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                        v);
                v.startDrag(null, shadowBuilder, v, 0);
                v.setVisibility(View.INVISIBLE);

                return true;
            }
        });

        findViewById(R.id.retoucheLayout).setOnDragListener(new View.OnDragListener() {

            float startX = 0f;
            float startY = 0f;

            @Override
            public boolean onDrag(View v, DragEvent event) {
                int action = event.getAction();

                View view = (View) event.getLocalState();
                switch (action) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        startX = event.getX() - (view.getWidth() / 2f);
                        startY = event.getY() - (view.getHeight() / 2f);
                        Log.d(TAG, "onDrag: ACTION_DRAG_STARTED ");
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        Log.d(TAG, "onDrag: ACTION_DRAG_ENTERED ");
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        Log.d(TAG, "onDrag: ACTION_DRAG_EXITED ");
                        break;
                    case DragEvent.ACTION_DROP:
                        Log.d(TAG, "onDrag: ACTION_DRAG_DROP ");


                        float x = event.getX();
                        float y = event.getY();
                        Canvas canvas = new Canvas(bitmap);
                        Paint paint = new Paint();
                        paint.setARGB(255, 255, 0, 0);
                        canvas.drawPoint(x, y, paint);
                        view.setX(x);

//                        if ((y + view.getHeight()) > retoucheImageView.getHeight()) {
//                            view.setY(retoucheImageView.getHeight() - view.getHeight());
//                        } else {
                        view.setY(y);
                        //}
                        view.setVisibility(View.VISIBLE);
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
//                        if (!event.getResult()) {
//                            view.setX(startX);
//                            view.setY(startY);
//                            view.setVisibility(View.VISIBLE);
//                        }
                        Log.d(TAG, "onDrag: ACTION_DRAG_ENDED ");
                    default:
                        break;
                }
                return true;
            }
        });

        useLightSensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.unregisterListener(Retouche.this);
                sensorManager.registerListener(Retouche.this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        });

        findViewById(R.id.useAccelerometer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.unregisterListener(Retouche.this);
                sensorManager.registerListener(Retouche.this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        });

        getImageFromCameraCapure();
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);


    }

    private void upload() {
        FireBase fireBase = new FireBase(null);

        constraintLayoutImageView.setDrawingCacheEnabled(true);
        constraintLayoutImageView.buildDrawingCache();
        Bitmap bitmap = constraintLayoutImageView.getDrawingCache();

        fireBase.uploadPhoto(bitmap, lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);


        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                upload();
                Intent in1 = new Intent(Retouche.this, MapsActivity.class);
                startActivity(in1);
                return true;
            }
        });

        menu.getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                reset();
                return true;
            }
        });


        return true;
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private void changeColorFilter(int color) {
        sensorManager.unregisterListener(this);
        Bitmap bmpMonochrome = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmpMonochrome);
        ColorMatrix ma = new ColorMatrix();
        Paint paint = new Paint();
        ColorFilter filter = new LightingColorFilter(color, 0);
        paint.setColorFilter(filter);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        retoucheImageView.setImageBitmap(bmpMonochrome);
        sensorManager.registerListener(Retouche.this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void changeSaturation(float saturation) {
        sensorManager.unregisterListener(this);
        Bitmap bmpMonochrome = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmpMonochrome);
        ColorMatrix ma = new ColorMatrix();
        ma.setSaturation(saturation);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(ma));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        retoucheImageView.setImageBitmap(bmpMonochrome);
    }

    private void getImageFromCameraCapure() {
        String imageFile = getIntent().getStringExtra("image");
        File image = new File(imageFile);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
        ImageView imageView = findViewById(R.id.retoucheImageView);
        imageView.setImageBitmap(bitmap);
    }

    public void reset() {
        sensorManager.unregisterListener(Retouche.this);
        retoucheImageView.setImageBitmap(bitmap);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                System.out.println(z);
                if (z > 0 && z < 5) {
                    changeColorFilter(Color.RED);
                } else if (z >= 5 && z < 8) {
                    changeColorFilter(Color.BLUE);
                } else {
                    changeColorFilter(Color.MAGENTA);
                }

                break;
            case Sensor.TYPE_LIGHT:
                System.out.println(event.values[0]);
                changeSaturation(event.values[0]);
                break;
            default:
                break;
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void pickFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST_CODE);
    }

    public class MyOnLongClickListener implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View v) {
            ClipData.Item item = new ClipData.Item((CharSequence) v.getTag());
            String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};

            ClipData dragData = new ClipData(v.getTag().toString(), mimeTypes, item);
            View.DragShadowBuilder myShadow = new View.DragShadowBuilder(v);

            v.startDrag(dragData, myShadow, null, 0);
            return true;
        }
    }
}
