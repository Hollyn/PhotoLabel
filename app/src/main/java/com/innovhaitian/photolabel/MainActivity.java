package com.innovhaitian.photolabel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.support.v7.widget.ShareActionProvider;
import android.widget.Toast;
import com.soundcloud.android.crop.Crop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    ImageView imageUser;
    ImageButton tvice1, tvice2, tvice3, tvice4, djakout1, djakout2, djakout3, djakout4;
    Uri destination;
    public static final String IMAGE_TYPE = "image/*";
    Toolbar toolbar;
    ShareActionProvider mShareActionProvider;
    Bitmap bitmap = null;
    Bitmap bitmap1, myBitmap;
    RelativeLayout rl;
    RelativeLayout.LayoutParams params;
    int width = 1;
    boolean visited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.appBar);

        setSupportActionBar(toolbar);
        rl = (RelativeLayout)findViewById(R.id.rl);

        imageUser = (ImageView) findViewById(R.id.imageView);
        tvice1 = (ImageButton) findViewById(R.id.tvice1);
        tvice2 = (ImageButton) findViewById(R.id.tvice2);
        tvice3 = (ImageButton) findViewById(R.id.tvice3);
        tvice4 = (ImageButton) findViewById(R.id.tvice4);
        djakout1 = (ImageButton) findViewById(R.id.djakout1);
        djakout2 = (ImageButton) findViewById(R.id.djakout2);
        djakout3 = (ImageButton) findViewById(R.id.djakout3);
        djakout4 = (ImageButton) findViewById(R.id.djakout4);

        toImplementOnClick();
        createScareBoxImage();
    }

    public static Bitmap combineImages(Bitmap c, Bitmap s, int width, int height)
    {
        Bitmap cs;
        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(cs);
        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s, 0, 0f, null);
        return cs;
    }

    public void toImplementOnClick(){
        imageUser.setOnClickListener(this);
        tvice1.setOnClickListener(this);
        tvice2.setOnClickListener(this);
        tvice3.setOnClickListener(this);
        tvice4.setOnClickListener(this);
        djakout1.setOnClickListener(this);
        djakout2.setOnClickListener(this);
        djakout3.setOnClickListener(this);
        djakout4.setOnClickListener(this);
    }

    public void createScareBoxImage() {
        rl = (RelativeLayout) findViewById(R.id.rl);
        rl.post(new Runnable() {
            @Override
            public void run() {
                width = rl.getWidth();
                params = new RelativeLayout.LayoutParams(width, width);
                params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                imageUser.setLayoutParams(params);
            }

        });
    }

    ////////////////////////// TRAITEMENT /////////////////////////////
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Crop.REQUEST_PICK) {
                beginCrop(data.getData());
            } else if (requestCode == Crop.REQUEST_CROP) {
                try {
                    handleCrop(resultCode, data);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(MainActivity.class.getSimpleName(), "Failed to load image", e);
                }
            }
        }else {
            // report failure
            Toast.makeText(getApplicationContext(), R.string.msg_failed_to_get_intent_data, Toast.LENGTH_LONG).show();
            Log.d(MainActivity.class.getSimpleName(), "Failed to get intent data, result code is " + resultCode);
        }
    }


    private void beginCrop(Uri source) {
        destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) throws IOException {
        if (resultCode == RESULT_OK) {
            bitmap = getResizedBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), Crop.getOutput(result)), width, width);
            bitmap1 = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.tvicebottom), width, width);
            //myBitmap = combineImages(bitmap, bitmap1, width, width);
            imageUser.setImageBitmap(combineImages(bitmap, bitmap1, width, width));
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //////////////////////////////// FIN TRAITEMENT /////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem shareItem = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        share();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.menu_item_download){
            saveCanvas();
            return true;
        }

        if (id == R.id.menu_item_share){
            share();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void share(){

        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DirName";
        File photoFile = new File(path+"/image.png");
        if (!photoFile.exists()) {
            FileOutputStream ostream;
            try {
                photoFile.createNewFile();
                ostream = new FileOutputStream(photoFile);
                myBitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
                ostream.flush();
                ostream.close();
                Toast.makeText(getApplicationContext(), "image save in " + path, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e+"", Toast.LENGTH_LONG).show();
            }
        }

        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(photoFile));
        Toast.makeText(this, photoFile + "", Toast.LENGTH_LONG).show();
        startActivity(Intent.createChooser(shareIntent, "Share image using"));
        mShareActionProvider.setShareIntent(shareIntent);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imageView:
                Intent intent = new Intent();
                intent.setType(IMAGE_TYPE);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        getString(R.string.select_picture)), Crop.REQUEST_PICK);
                break;
            case R.id.tvice1:
                myBitmap = combineImages(bitmap, getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.tvicebottom), width, width), width, width);
                imageUser.setImageBitmap(myBitmap);
                break;
            case R.id.tvice2:
                myBitmap = combineImages(bitmap, getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.tvicesubtitlecenter), width, width), width, width);
                imageUser.setImageBitmap(myBitmap);
                break;
            case R.id.tvice3:
                myBitmap = combineImages(bitmap, getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.tviceright), width, width), width, width);
                imageUser.setImageBitmap(myBitmap);
                break;
            case R.id.tvice4:
                myBitmap = combineImages(bitmap, getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.tvice_all), width, width), width, width);
                imageUser.setImageBitmap(myBitmap);
                break;
            case R.id.djakout1:
                myBitmap = combineImages(bitmap, getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.djakout_number_one_all), width, width), width, width);
                imageUser.setImageBitmap(myBitmap);
                break;
            case R.id.djakout2:
                myBitmap = combineImages(bitmap, getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.djakout_number_one_bottom), width, width), width, width);
                imageUser.setImageBitmap(myBitmap);
                break;
            case R.id.djakout3:
                myBitmap = combineImages(bitmap, getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.djakout_number_one_center), width, width), width, width);
                imageUser.setImageBitmap(myBitmap);
                break;
            case R.id.djakout4:
                myBitmap = combineImages(bitmap, getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.djakout_number_one_right), width, width), width, width);
                imageUser.setImageBitmap(myBitmap);
                break;
        }
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    ////////////////// SAVE THE CANVAS ///////////////////////////////////////

    public void saveCanvas(){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DirName";
        File direct = new File(path);
        if (!direct.exists()) {
            File wallpaperDirectory = new File(path + "/image.png");
            wallpaperDirectory.mkdirs();
        }

        File file = new File(path + "/image.png");
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream ostream;
        try {
            file.createNewFile();
            ostream = new FileOutputStream(file);
            myBitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
            ostream.flush();
            ostream.close();
            Toast.makeText(getApplicationContext(), "image save in " + path, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e+"", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
     /*   if (!visited) {
            startActivity(new Intent(MainActivity.this, ChoosePicture.class));
            finish();
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause();
    }

    private void pause() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        resume();
    }

    private void resume() {
    }
}