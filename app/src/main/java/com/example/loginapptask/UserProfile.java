package com.example.loginapptask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.loginapptask.databinding.ActivityMainBinding;
import com.example.loginapptask.databinding.ActivityUserProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Calendar;
import java.util.UUID;

public class UserProfile extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Database").child("user");
    private ActivityUserProfileBinding binding;

    public static final int PICK_IMG_REQUEST = 6541;
    private Uri mImageUri;

    private StorageReference mstorageReference;
    private DatabaseReference mdatabaseReference;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_profile);

        mstorageReference = FirebaseStorage.getInstance().getReference("uploads");
        mdatabaseReference = FirebaseDatabase.getInstance().getReference("uploads");


        binding.btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!TextUtils.isEmpty(binding.editProfileFullName.getText().toString())) {

                    UserHelperClass userHelperClass = new UserHelperClass(binding.editProfileFullName.getText().toString(),
                            binding.profileEmail.getText().toString(),
                            binding.profilePhone.getText().toString(),
                            binding.profilePassword.getText().toString());
                    reference.push().setValue(userHelperClass);
                    Toast.makeText(UserProfile.this, "Database Created", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(UserProfile.this, "please enter your name", Toast.LENGTH_SHORT).show();
                }
            }
        });


        binding.btnChoseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        binding.btnUpdatePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                uploadFile();
            }
        });

    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {

        if (mImageUri != null){

            StorageReference fileReference = mstorageReference.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));

            fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(UserProfile.this, "upload successful", Toast.LENGTH_SHORT).show();
                            String uploadKey = mdatabaseReference.push().getKey();
                            mdatabaseReference.child(uploadKey).setValue("upload");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(UserProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            
                        }
                    });


        } else {
            Toast.makeText(this, "no file selcted", Toast.LENGTH_SHORT).show();
        }
    }

    private void openFileChooser() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMG_REQUEST);

    }


    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (reqCode == PICK_IMG_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();

            Picasso.with(this).load(mImageUri).into(binding.profileImage);
        }
    }

}