package helloandroid.m2dl.tropchoupi;

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
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class Retouche extends AppCompatActivity implements SensorEventListener {
    ImageView retoucheImageView;
    ImageView useLightSensor;
    Bitmap bitmap;
    SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Sensor lightSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retouche);
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

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private void changeColorFilter(int color) {
        Bitmap bmpMonochrome = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmpMonochrome);
        ColorMatrix ma = new ColorMatrix();
        Paint paint = new Paint();
        ColorFilter filter = new LightingColorFilter(color, 0);
        paint.setColorFilter(filter);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        retoucheImageView.setImageBitmap(bmpMonochrome);
    }

    private void changeSaturation(float saturation) {
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

    public void reset(View view) {
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
                }else{
                    changeColorFilter(Color.MAGENTA);
                }
                break;
            case Sensor.TYPE_LIGHT:
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
