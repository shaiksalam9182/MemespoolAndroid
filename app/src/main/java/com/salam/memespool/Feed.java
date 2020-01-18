package com.salam.memespool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Feed extends AppCompatActivity {


    FloatingActionButton btUpload;
    final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    File SelectedFile;
    RequestBody requestBody;
    MultipartBody.Builder mRequestBody;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        btUpload = (FloatingActionButton)findViewById(R.id.bt_upload);
        mRequestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        btUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissions()){

                    redirectToUploadPage();


                }else {
                    ActivityCompat.requestPermissions(Feed.this,PERMISSIONS,101);
                }
            }
        });
    }

    private void redirectToUploadPage() {
        startActivity(new Intent(Feed.this,Postupload.class));
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
                redirectToUploadPage();
            }else {
                Toast.makeText(Feed.this,getResources().getString(R.string.needPermissionsProfilePic),Toast.LENGTH_LONG).show();
            }
        }
    }

}
