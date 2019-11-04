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
                    Intent selectImage = new Intent();
                    selectImage.setAction(Intent.ACTION_PICK);
                    selectImage.setType("image/*");
                    selectImage.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
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
                Intent selectImage = new Intent();
                selectImage.setType("image/*");
                selectImage.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
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

                    if (data!=null && data.getClipData().getItemCount()<15){
                        for (int i=0;i<data.getClipData().getItemCount();i++){
                            Uri imagePath = data.getClipData().getItemAt(i).getUri();
                            getimagefilepaxth(imagePath);
                        }
                        new AsyncSendImages().execute();
                    }else {
                        Toast.makeText(Feed.this,"Returned Null",Toast.LENGTH_LONG).show();
                    }



                }catch (NullPointerException e){
                    e.printStackTrace();
                }


            }
        }
    }

    private void getimagefilepaxth(Uri imagePath) {

        Cursor cursor = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        cursor = getContentResolver().query(imagePath,proj,null,null,null);
        assert  cursor !=null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String realPath = cursor.getString(column_index);
        File file = new File(realPath);
        Log.e("fikles",file.toString()) ;
        final MediaType MEDIATYPE = MediaType.parse("image/*");
        requestBody = RequestBody.create(MEDIATYPE,file);
        mRequestBody.addFormDataPart("uploads","imageName.png",requestBody);

    }

    private class AsyncSendImages extends AsyncTask<Void,Void, JSONObject> {

        ProgressDialog pdLoading = new ProgressDialog(Feed.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdLoading.setMessage("uploading....");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {

            RequestBody req = mRequestBody.build();

            Request request = new Request.Builder()
                    .url(Urls.upload)
                    .post(req)
                    .build();
            OkHttpClient okHttpClient = new OkHttpClient();
            try {
                Response response = okHttpClient.newCall(request).execute();
                return new JSONObject(response.body().string());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            pdLoading.dismiss();
            if (jsonObject!=null){
                Log.e("recData",jsonObject.toString());
            }else {
                Toast.makeText(Feed.this,"somethingworkd",Toast.LENGTH_LONG).show();
            }
        }
    }
}
