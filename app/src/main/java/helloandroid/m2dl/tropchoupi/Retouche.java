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
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class Retouche extends AppCompatActivity {
    ImageView retoucheImageView;
    ImageView monochromeButton;
    Bitmap bitmap;
    SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retouche);
        this.monochromeButton = findViewById(R.id.monochromeButton);
        this.retoucheImageView = findViewById(R.id.retoucheImageView);
        this.seekBar = findViewById(R.id.saturation);

        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                changeSaturation(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        monochromeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSaturation(0);
            }
        });

        findViewById(R.id.change_to_blue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeColorFilter(Color.BLUE);
            }
        });

        findViewById(R.id.change_to_red).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeColorFilter(Color.RED);
            }
        });

        findViewById(R.id.upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });
        getImageFromCameraCapure();

    }

    private void changeColorFilter(int color){
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
        fireBase.uploadPhoto(bitmap);
    }


    public void reset(View view) {
        retoucheImageView.setImageBitmap(bitmap);
        this.seekBar.setProgress(1);
    }


}
