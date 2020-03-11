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
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retouche);

        this.useLightSensor = findViewById(R.id.useLightSensor);
        this.retoucheImageView = findViewById(R.id.retoucheImageView);
        this.toDrag = findViewById(R.id.toDrag);


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

        retoucheImageView.setOnDragListener(new View.OnDragListener() {

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


                        float x = event.getX() - (view.getWidth() / 2f);
                        float y = event.getY() - (view.getHeight() / 2f);

                        view.setX(x);

                        if ((y + view.getHeight()) > retoucheImageView.getHeight()) {
                            view.setY(retoucheImageView.getHeight() - view.getHeight());
                        } else {
                            view.setY(y);
                        }
                        view.setVisibility(View.VISIBLE);
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        if (!event.getResult()) {
                            view.setX(startX);
                            view.setY(startY);
                            view.setVisibility(View.VISIBLE);
                        }
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
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);


        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                upload();
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

    public void upload(){
        FireBase fireBase = new FireBase();
        Bitmap bitmap = ((BitmapDrawable)retoucheImageView.getDrawable()).getBitmap();
        fireBase.uploadPhoto(bitmap, 70 , 70);
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

    public class MyOnLongClickListener implements View.OnLongClickListener{

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
