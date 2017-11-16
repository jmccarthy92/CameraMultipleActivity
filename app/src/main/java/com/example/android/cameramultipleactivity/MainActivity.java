package com.example.android.cameramultipleactivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;


import java.io.File;
import java.io.FileNotFoundException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import jp.wasabeef.blurry.Blurry;

import static android.os.Environment.getExternalStoragePublicDirectory;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class MainActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE = 100;
//    public static final int MEDIA_TYPE_IMAGE = 1;
    private Button takePhoto;
    private Button filterPhotoButton;
    private Button blurPhotoButton;
    private Button galleryButton;
    private Button shareButton;
    private File photoFile;
    private String mCurrentPhotoPath;
//    private Camera mCamera = null;
//    private CameraView mCameraView = null;
    private ImageView mImageView;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.image_view);
        takePhoto = (Button) findViewById(R.id.button);
        filterPhotoButton = (Button) findViewById(R.id.button2);
        blurPhotoButton = (Button) findViewById(R.id.button3);
        shareButton = (Button) findViewById(R.id.button4);
        galleryButton = (Button) findViewById(R.id.button5);
        shareButton.setOnClickListener(sharePhoto);
        galleryButton.setOnClickListener(openGallery);
        filterPhotoButton.setOnClickListener(filterClick);
        blurPhotoButton.setOnClickListener(blurClick);
        takePhoto.setOnClickListener(takePhotoClick);
        FrameLayout fl = (FrameLayout) findViewById(R.id.camera_view);
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.relative_lay);

        if(getResources().getConfiguration().orientation == 2){
            ViewGroup.LayoutParams frameParams = (ViewGroup.LayoutParams) fl.getLayoutParams();
            Display display = getWindowManager().getDefaultDisplay();
            int screen_height = display.getHeight();
            screen_height = (int) (0.40 * screen_height);
            frameParams.height = screen_height;
            fl.setLayoutParams(frameParams);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //modifyLayout(newConfig);
        FrameLayout fl = (FrameLayout) findViewById(R.id.camera_view);
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.relative_lay);
        //RelativeLayout.LayoutParams relParams = (RelativeLayout.LayoutParams) rl.getLayoutParams();
        ImageView iv = (ImageView) findViewById(R.id.image_view);
        //LinearLayout.LayoutParams imageParams = (LinearLayout.LayoutParams) iv.getLayoutParams();
        ViewGroup.LayoutParams frameParams = (ViewGroup.LayoutParams) fl.getLayoutParams();
        Button button = (Button) findViewById(R.id.button);
        Button button4 = (Button) findViewById(R.id.button4);
        Button button5 = (Button) findViewById(R.id.button5);
        RelativeLayout.LayoutParams params1
                = (RelativeLayout.LayoutParams) button.getLayoutParams();
        RelativeLayout.LayoutParams params4
                = (RelativeLayout.LayoutParams) button4.getLayoutParams();
        RelativeLayout.LayoutParams params5
                = (RelativeLayout.LayoutParams) button5.getLayoutParams();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d("HELLO", "hello");
            Display display = getWindowManager().getDefaultDisplay();
            int screen_height = display.getHeight();
            screen_height = (int) (0.40 * screen_height);
            frameParams.height = screen_height;
            fl.setLayoutParams(frameParams);
            params4.addRule(RelativeLayout.LEFT_OF, R.id.button3);
            params5.addRule(RelativeLayout.RIGHT_OF, R.id.button3);
            button.setLayoutParams(params1);
            button4.setLayoutParams(params4);
            button5.setLayoutParams(params5);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Display display = getWindowManager().getDefaultDisplay();
            int screen_height = display.getHeight();
            screen_height = (int) (0.50 * screen_height);
            frameParams.height = screen_height;
            fl.setLayoutParams(frameParams);

        }

    }


    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    private View.OnClickListener sharePhoto = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            if(mCurrentPhotoPath != null) {
               // File f=new File("full image path");
                //Uri uri = Uri.parse("file://"+mCurrentPhotoPath);
                Uri uri = FileProvider.getUriForFile(MainActivity.this, "com.example.android.fileprovider", photoFile);
                Intent share = new Intent(Intent.ACTION_SEND);
                share.putExtra(Intent.EXTRA_STREAM, uri);
                share.setType("image/*");
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivity(Intent.createChooser(share, "Share image File"));

            }
        }
    };


    private View.OnClickListener openGallery = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
//            Intent intent = new Intent (Intent.ACTION_VIEW, Uri.parse(
//                    "content://media/internal/images/media"));
//            startActivity(intent);
            openImageChooser();
        }
    };

    private View.OnClickListener takePhotoClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dispatchTakePictureIntent();
        }
    };

    private View.OnClickListener blurClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            blurPicture();
        }
    };

    private View.OnClickListener filterClick = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            Random r = new Random();
            int lowerBound = 0;
            int upperBound = 2;
            int [] randomColors = {0xffff0000, 0xff00ff00, 0xff0000ff };
            int result = r.nextInt(upperBound - lowerBound + 1) + lowerBound;
            mImageView.getDrawable().setColorFilter(randomColors[result] , PorterDuff.Mode.MULTIPLY);
        }
    };

    private void blurPicture(){
        Blurry.with(MainActivity.this)
                .radius(25)
                .sampling(1)
                .color(Color.argb(66, 255, 255, 0))
                .async()
                .capture(mImageView)
                .into(mImageView);
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if( takePictureIntent.resolveActivity(getPackageManager()) != null){
            photoFile = null;
            try{
                photoFile = createImageFile();
            } catch (IOException e){
                Log.d("FILE", "Error Occured making file : " + e.getMessage());
            }
            if( photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                galleryAddPic();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_"+timeStamp+"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if( requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
            File file = new File(mCurrentPhotoPath);
            //   mImageView.setImageBitmap(imageBitmap);
            mImageView.setImageURI(Uri.fromFile(file));
        }
    }



}
