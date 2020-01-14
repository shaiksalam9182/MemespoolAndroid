package com.salam.memespool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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

    int deviceWidth;
    int imageWidth,imageHeight;
    ArrayList<File> imagesList;
    ViewPager vpImages;
    RecyclerView rvImages;
    RecyclerView.SmoothScroller smoothScroller;
    LinearLayoutManager layoutManager;
    LinearLayout llPrevious,llCurrent;
    int clickedPostion;
    ImagesAdapter imagesAdapter;
    TextView tvPost;
    ProgressDialog pdLoading;
    int totalCount,currentItem,statusItem;
    JSONArray uploadedURLS;
    String userData;
    JSONObject userDataObject;
    SharedPreferences sd;
    SharedPreferences.Editor editor;

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
        pdLoading = new ProgressDialog(Postupload.this);

        sd = getSharedPreferences("memespool",Context.MODE_PRIVATE);
        editor = sd.edit();

        userData = sd.getString("userData","");
        try {
            userDataObject = new JSONObject(userData);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        vpImages = (ViewPager)findViewById(R.id.vp_images);
        rvImages = (RecyclerView)findViewById(R.id.rv_images);
        tvPost = (TextView)findViewById(R.id.tv_post);

        tvPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                totalCount = imagesList.size();
                currentItem = 0;
                statusItem =1;
                uploadedURLS = new JSONArray();
                new AsyncSendImage().execute(imagesList.get(0));


            }
        });

        vpImages.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.e("position",position+"");
                layoutManager.scrollToPositionWithOffset(position,15);
                try {
                    llPrevious.setBackgroundColor(Color.parseColor("#000000"));
                    rvImages.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.ll_img).setBackgroundColor(Color.parseColor("#000000"));
                    RecyclerView.ViewHolder holder = (RecyclerView.ViewHolder) rvImages.findViewHolderForAdapterPosition(position);
                    holder.itemView.findViewById(R.id.ll_img).setBackgroundColor(Color.parseColor("#ffffff"));

                    llPrevious = (LinearLayout) rvImages.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.ll_img);
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        smoothScroller = new LinearSmoothScroller(this){
            @Override
            protected int getHorizontalSnapPreference() {
                return super.getHorizontalSnapPreference();
            }
        };


        deviceWidth = getResources().getDisplayMetrics().widthPixels;
        imageWidth = deviceWidth/2;
        imageHeight = (int) (imageWidth/1.87);
        mRequestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        imagesList = new ArrayList<File>();


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
                        vpImages.setAdapter(new SliderAdapter(Postupload.this,imagesList));
                        layoutManager = new LinearLayoutManager(Postupload.this,LinearLayoutManager.HORIZONTAL,false);
                        rvImages.setLayoutManager(layoutManager);
                        imagesAdapter = new ImagesAdapter(Postupload.this,imagesList);
                        rvImages.setAdapter(imagesAdapter);
                    }else {
                        Toast.makeText(Postupload.this,"Returned Null",Toast.LENGTH_LONG).show();
                    }



                }catch (NullPointerException e){
                    e.printStackTrace();
                }


            }else {
                finish();
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
        imagesList.add(file);

        final MediaType MEDIATYPE = MediaType.parse("image/*");


    }


    private class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.MyViewHolder> {

        Context mContext;
        ArrayList<File> imagesList;

        public ImagesAdapter(Postupload postupload, ArrayList<File> imagesList) {
            this.imagesList = imagesList;
            mContext = postupload;

        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.custom_horizontal_image_view, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
            Glide.with(mContext).load(imagesList.get(position)).into(holder.ivImage);

            holder.ivImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vpImages.setCurrentItem(position,true);
                }
            });
        }

        @Override
        public int getItemCount() {
            return imagesList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView ivImage;
            LinearLayout llImg;
            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                ivImage = (ImageView)itemView.findViewById(R.id.iv_hz_img);
                llImg = (LinearLayout)itemView.findViewById(R.id.ll_img);

            }
        }
    }

    private class AsyncSendImage extends AsyncTask<File,Void,JSONObject>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdLoading.setMessage("Uploading "+ statusItem+"/"+totalCount );
            pdLoading.setCancelable(false);
            pdLoading.show();
        }

        @Override
        protected JSONObject doInBackground(File... files) {
            final MediaType MEDIATYPE = MediaType.parse("image/*");
            RequestBody req = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("image","profile.png",RequestBody.create(MEDIATYPE,files[0])).build();

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
            if (jsonObject!=null){
                uploadedURLS.put(jsonObject.optString("image_url"));
            }
           if (currentItem<totalCount-1){
               currentItem++;
               statusItem = currentItem+1;

               new AsyncSendImage().execute(imagesList.get(currentItem));
           }else {
               pdLoading.dismiss();
               sendPost();
           }
        }
    }

    private void sendPost() {
        new AsyncSendPost().execute();
    }

    class AsyncSendPost extends AsyncTask<Void,Void,JSONObject>{

        ProgressDialog pdLoading = new ProgressDialog(Postupload.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdLoading.setMessage("Publishing your post..");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            JSONObject data = new JSONObject();
            try {
                data.put("user_id",userDataObject.optString("user_id"));
                data.put("token",userDataObject.optString("token"));
                data.put("images",uploadedURLS);
                data.put("description","Testing....");
                PostHelper postHelper = new PostHelper(Postupload.this);
                return postHelper.Post(Urls.sendPost,data.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject object) {
            super.onPostExecute(object);
            pdLoading.dismiss();
            if (object!=null){
                if (object.optBoolean("success")){
                    Toast.makeText(Postupload.this,object.optString("message"),Toast.LENGTH_LONG).show();
                    finish();
                }else {
                    Toast.makeText(Postupload.this,object.optString("message"),Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(Postupload.this,"something went wrong",Toast.LENGTH_LONG).show();
            }
        }
    }
}
