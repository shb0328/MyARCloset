package org.ssuss.myarcloset;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity //implements ActivityCompat.OnRequestPermissionsResultCallback
{

    static final int REQUEST_TAKE_PHOTO = 1;
    private static final int PERMISSIONS_REQUEST_CODE = 328;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.CAMERA, // 카메라
            Manifest.permission.WRITE_EXTERNAL_STORAGE};  // 외부 저장소

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /**permission check**/
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);

        /**DENIED**/
        if(cameraPermission == PackageManager.PERMISSION_DENIED
            && writeExternalStoragePermission == PackageManager.PERMISSION_DENIED){

            //Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                Snackbar.make(getWindow().getDecorView().getRootView(),
                        "이 앱을 실행하려면 카메라와 외부 저장소 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("확인", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        REQUIRED_PERMISSIONS,
                                        PERMISSIONS_REQUEST_CODE);
                            }
                        }).show();

            } else { // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                                                    REQUIRED_PERMISSIONS,
                                                    PERMISSIONS_REQUEST_CODE);
            }

        }
        /**GRANTED**/

            //take a picture
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int isOK = takePhoto();
                    if (isOK != FAIL) {
                        addImageToGallery();
                    } else {
                        Toast.makeText(getApplicationContext(), "사진촬영에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            });


    }

    //requestPermissions callback method
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!

                } else {

                    // permission denied, boo!
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                            || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        Snackbar.make(getWindow().getDecorView().getRootView(),
                                "앱이 카메라와 외부저장소에 접근이 거부되었습니다. 설정에서 앱 권한을 허용해주세요. ",
                                Snackbar.LENGTH_INDEFINITE)
                                .setAction("확인", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        finish();
                                    }
                                }).show();
                    }

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    Uri photoURI;
    static final int SUCCESS = 1;
    static final int FAIL = -1;
    private int takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            }catch(IOException ie){
                ie.getStackTrace();
            }
            if(photoFile != null) {
                photoURI = FileProvider.getUriForFile(
                        this,
                        "org.ssuss.myarcloset",
                        photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, REQUEST_TAKE_PHOTO);
                return SUCCESS;
            }else {
                return FAIL;
            }
        }else{
            return FAIL;
        }
    }

    String currentPhotoPath;
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "MyARCloset_"+timeStamp;
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDirectory
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void addImageToGallery() {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri uri = Uri.fromFile(f);
        intent.setData(uri);
        this.sendBroadcast(intent);

    }

}
