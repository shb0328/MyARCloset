package org.ssuss.myarcloset;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CreateMetadata extends Activity {

    private RadioGroup radioGroup;
    private Button btn;
    private String classification;
    private String uid;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_create_metadata);

        uid = getIntent().getStringExtra("UID");
        /**
         * init firebase storage
         */
        // [START storage_field_initialization]
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Create a storage reference from our app
        storageRef = storage.getReference();
        /**
         * end
         */


        radioGroup = findViewById(R.id.topOrBottom);


        btn = findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int id = radioGroup.getCheckedRadioButtonId();
                classification = ((RadioButton)findViewById(id)).getText().toString();

                takePhoto();

            }
        });

    }

    private static final String TAG = "Log :: ";
    Uri photoURI;
    static final int REQUEST_TAKE_PHOTO = 1;
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

    private void uploadPhoto(String path,String userId) throws FileNotFoundException {
        UploadTask uploadTask;

        String storagePath = "/user/"+userId+"/images/";

        // [START upload_file]
        Uri file = Uri.fromFile(new File(path));
        StorageReference ref = storageRef.child(storagePath+file.getLastPathSegment());
        // Create file metadata including the content type
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("분류", classification)
                .build();
        uploadTask = ref.putFile(file,metadata);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d(TAG,"**unsuccessful uploads!");
                exception.getStackTrace();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });
        // [END upload_file]

        /**
         * if... you need a metadata~
         */
        // [START upload_with_metadata]
        // Create file metadata including the content type
//        StorageMetadata metadata = new StorageMetadata.Builder()
//                .setContentType("image/jpg")
//                .build();

        // Upload the file and metadata
//        uploadTask = storageRef.child("images/mountains.jpg").putFile(file, metadata);
        // [END upload_with_metadata]
        /**
         * Do this.
         */
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

/** This is THE TIMING at which the picture was taken!! **/
            //촬영한 사진 firebase storage에 저장
            try{
                uploadPhoto(currentPhotoPath, uid);
                Toast.makeText(CreateMetadata.this, "사진을 Firebase Storage 에 저장했습니다.", Toast.LENGTH_LONG).show();
                finish();
            }catch (FileNotFoundException fnfe){
                Log.d(TAG,"**FileNotFoundException**");
                fnfe.getStackTrace();
            }

            //촬영한 사진 imageView에 띄우기
//            ImageView imageView = (ImageView)findViewById(R.id.imageView);
//            imageView.setImageURI(photoURI);

            //TODO:촬영한 사진 갤러리에 저장
//            addImageToGallery();
/****/
        }

    }


    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()== MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

}
