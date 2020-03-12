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
import android.graphics.PorterDuff;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;

public class Retouche extends AppCompatActivity implements SensorEventListener {
    ImageView retoucheImageView;
    ImageView useLightSensor;
    Bitmap bitmap;
    SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Sensor lightSensor;
    private Location lastKnownLocation;
    Canvas canvas;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retouche);


        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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


        canvas = new Canvas(bitmap);
    }

    private void upload() {
        FireBase fireBase = new FireBase(null);

        ConstraintLayout view = (ConstraintLayout) findViewById(R.id.retoucheImageConstraintLayout);
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();

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
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
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


}
