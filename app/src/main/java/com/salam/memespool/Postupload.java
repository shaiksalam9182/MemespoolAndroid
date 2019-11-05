package com.salam.memespool;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;

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

public class Postupload extends AppCompatActivity {

    RequestBody requestBody;
    MultipartBody.Builder mRequestBody;
    MaterialToolbar postToolbar;
    Button btUpload;
    GridLayout gridLayout;
    int deviceWidth;
    int imageWidth,imageHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postupload);

        postToolbar = (MaterialToolbar)findViewById(R.id.post_toolbar);
        setSupportActionBar(postToolbar);
        postToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp));
        postToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        postToolbar.setTitle("Post Memes");
        btUpload = (Button)findViewById(R.id.bt_upload);
        gridLayout = (GridLayout)findViewById(R.id.grid_layout);
        gridLayout.setColumnCount(2);
        gridLayout.setRowCount(8);
        deviceWidth = getResources().getDisplayMetrics().widthPixels;
        imageWidth = deviceWidth/2;
        imageHeight = (int) (imageWidth/1.87);
        mRequestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);



        btUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncSendImages().execute();
            }
        });



        Intent selectImage = new Intent();
        selectImage.setAction(Intent.ACTION_PICK);
        selectImage.setType("image/*");
        selectImage.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        startActivityForResult(Intent.createChooser(selectImage,getResources().getString(R.string.selectImage)),100);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==100){
            if (resultCode==RESULT_OK){
                try{

                    if (data!=null && data.getClipData().getItemCount()<=15){
                        for (int i=0;i<data.getClipData().getItemCount();i++){
                            Uri imagePath = data.getClipData().getItemAt(i).getUri();
                            getimagefilepaxth(imagePath);
                        }

                    }else {
                        Toast.makeText(Postupload.this,"Returned Null",Toast.LENGTH_LONG).show();
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
        ImageView oImageView = new ImageView(this);
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        oImageView.setImageBitmap(bitmap);

        oImageView.setLayoutParams(new ViewGroup.LayoutParams(imageWidth,imageHeight));
        gridLayout.addView(oImageView);
        final MediaType MEDIATYPE = MediaType.parse("image/*");
        requestBody = RequestBody.create(MEDIATYPE,file);
        mRequestBody.addFormDataPart("uploads","imageName.png",requestBody);

    }

    private class AsyncSendImages extends AsyncTask<Void,Void, JSONObject> {

        ProgressDialog pdLoading = new ProgressDialog(Postupload.this);

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
                Toast.makeText(Postupload.this,"somethingworkd",Toast.LENGTH_LONG).show();
            }
        }
    }
}
