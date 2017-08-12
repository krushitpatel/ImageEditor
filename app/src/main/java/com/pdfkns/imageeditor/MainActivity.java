package com.pdfkns.imageeditor;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    Button btn;
    ImageView imageViewFrame,imageViewDisplay,imageViewHide;
    private SharedPreferences permissionStatus;
    private boolean sentToSettings = false;
    private static final int EXTERNAL_STORAGE_PERMISSION_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;

    TouchImageView img;
    Bitmap bitmap1,bitmap2,bmOverlay,bmCombine,bitmapFinal,bitmapBorder;
    RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        btn = (Button)findViewById(R.id.btnSelectImage);
        img = (TouchImageView)findViewById(R.id.imageview);
        imageViewFrame = (ImageView)findViewById(R.id.imageviewFrame);
        imageViewDisplay = (ImageView)findViewById(R.id.imageviewDisplay);
        imageViewHide = (ImageView)findViewById(R.id.imageviewHide);
        relativeLayout = (RelativeLayout)findViewById(R.id.combineFrame);


        bitmap1= BitmapFactory.decodeResource(getResources(),
                R.drawable.line);
        imageViewFrame.setImageBitmap(bitmap1);
        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);


        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) +ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Need Storage Permission");
                builder.setMessage("This app needs storage permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
                        //         ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.MANAGE_DOCUMENTS}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, false) && (permissionStatus.getBoolean(Manifest.permission.READ_EXTERNAL_STORAGE, false))) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Need Storage Permission");
                builder.setMessage("This app needs storage permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(getBaseContext(), "Go to Permissions to Grant Storage", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
                //   ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.MANAGE_DOCUMENTS}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);

            }

            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, true);
            editor.putBoolean(Manifest.permission.READ_EXTERNAL_STORAGE,true);
            // editor.putBoolean(Manifest.permission.MANAGE_DOCUMENTS,true);

            editor.commit();


        }else {
            proceedAfterPermission();
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_upload = new Intent(Intent.ACTION_PICK);
                intent_upload.setType("image/*");
                startActivityForResult(intent_upload, 1);
            }
        });
    }

    private void proceedAfterPermission() {
        //We've got the permission, now we can proceed further
        // Toast.makeText(getBaseContext(), "We got the Storage Permission", Toast.LENGTH_LONG).show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == EXTERNAL_STORAGE_PERMISSION_CONSTANT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //The External Storage Write Permission is granted to you... Continue your left job...
                proceedAfterPermission();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)&&ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    //Show Information about why you need the permission
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Need Storage Permission");
                    builder.setMessage("This app needs storage permission");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();


                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);


                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } else {
                    Toast.makeText(getBaseContext(),"Unable to get Permission",Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {

            if (resultCode == RESULT_OK) {
                try {
                    Uri uri = data.getData();

                    try {

                        bitmap2 = null;
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG,50,bytearrayoutputstream);

                        byte[] bytearray = bytearrayoutputstream.toByteArray();

                        bitmap2 = BitmapFactory.decodeByteArray(bytearray,0,bytearray.length);
                        img.setImageBitmap(bitmap2);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (OutOfMemoryError e) {
                } catch (Exception e) {

                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.save:

                img.destroyDrawingCache();
                imageViewFrame.destroyDrawingCache();

                img.buildDrawingCache();
                Bitmap bitmap1 = img.getDrawingCache();

                imageViewFrame.buildDrawingCache();
                imageViewFrame.hasOverlappingRendering();
                Bitmap bitmap2 = imageViewFrame.getDrawingCache();


                bmOverlay = overlay(bitmap1,bitmap2);

                imageViewHide.setVisibility(View.VISIBLE);
                relativeLayout.buildDrawingCache();
                bmCombine = relativeLayout.getDrawingCache();
                imageViewHide.setVisibility(View.INVISIBLE);
                new BackgroundTask().execute();



                default:
                    return super.onOptionsItemSelected(item);
        }

    }

    public Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {

        bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, 0, 0, null);
        return bmOverlay;
    }
    private Bitmap findDifference(Bitmap firstImage, Bitmap secondImage) {
        Bitmap bmp = secondImage.copy(secondImage.getConfig(), true);

        if (firstImage.getWidth() != secondImage.getWidth()
                || firstImage.getHeight() != secondImage.getHeight()) {
            return null;
        }

        for (int i = 0; i < firstImage.getWidth(); i++) {
            for (int j = 0; j < firstImage.getHeight(); j++) {

                int firstImagePixel = firstImage.getPixel(i,j);
                if (firstImagePixel == Color.TRANSPARENT){
                    bmp.setPixel(i, j, Color.TRANSPARENT);
                }
            }
        }


        return bmp;
    }


    private class BackgroundTask extends AsyncTask<String, String, String> {
        private ProgressDialog dialog;
        private volatile boolean running = true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Processing Image, please wait.");
            dialog.setCancelable(false);
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                   cancel(true);

                }
            });

            dialog.show();
        }


        @Override
        protected String doInBackground(String... args) {

            while ( running) {
                try {
                    //Thread.sleep(5000);
                    if (isCancelled()) {
                        return null;
                    }

                    bitmapFinal = findDifference(bmCombine, bmOverlay);
                   // bitmapBorder = addWhiteBorder(bitmapFinal,3);
                    break;
                    //findDifference();
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            running  = false;
            dialog.dismiss();
        }

        @Override
        protected void onPostExecute(String result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (bitmapFinal != null){
                imageViewDisplay.setImageBitmap(bitmapFinal);
            }
            else {
                Toast.makeText(getApplicationContext(),"No Image Return ",Toast.LENGTH_SHORT).show();
            }

        }


    }
//    private Bitmap addWhiteBorder(Bitmap bmp, int borderSize) {
//        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
//        Canvas canvas = new Canvas(bmpWithBorder);
//        canvas.drawColor(Color.BLACK);
//        canvas.drawBitmap(bmp, borderSize, borderSize, null);
//        return bmpWithBorder;
//    }



}
