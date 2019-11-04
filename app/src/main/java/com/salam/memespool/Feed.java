package com.salam.memespool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;

public class Feed extends AppCompatActivity {


    FloatingActionButton btUpload;
    final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    File SelectedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        btUpload = (FloatingActionButton)findViewById(R.id.bt_upload);

        btUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissions()){
                    Intent selectImage = new Intent(Intent.ACTION_PICK);
                    selectImage.setType("image/*");
                    startActivityForResult(Intent.createChooser(selectImage,getResources().getString(R.string.selectImage)),100);
                }else {
                    ActivityCompat.requestPermissions(Feed.this,PERMISSIONS,101);
                }
            }
        });
    }


    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            for (String permission : PERMISSIONS){
                if (ActivityCompat.checkSelfPermission(Feed.this,permission)!= PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==101){
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Intent selectImage = new Intent(Intent.ACTION_PICK);
                selectImage.setType("image/*");
                startActivityForResult(Intent.createChooser(selectImage,getResources().getString(R.string.selectImage)),100);
            }else {
                Toast.makeText(Feed.this,getResources().getString(R.string.needPermissionsProfilePic),Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==100){
            if (resultCode==RESULT_OK){
                try{
                    Uri imagePath = data.getData();
                    Cursor cursor = null;
                    String[] proj = {MediaStore.Images.Media.DATA};
                    cursor = getContentResolver().query(imagePath,proj,null,null,null);
                    assert  cursor !=null;
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String realPath = cursor.getString(column_index);
                    SelectedFile = new File(realPath);

                }catch (NullPointerException e){
                    e.printStackTrace();
                }


            }
        }
    }
}
