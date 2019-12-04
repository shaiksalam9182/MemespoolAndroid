package com.salam.memespool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.lizhangqu.coreprogress.ProgressHelper;
import io.github.lizhangqu.coreprogress.ProgressUIListener;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Register extends AppCompatActivity {

    MaterialToolbar toolbar;
    TextInputEditText tiName,tiEmail,tiPassword,tiCpassword;
    Button btRegister;
    String name,email,password,cPassword,uploadedImage,receivedUserId;
    CircleImageView civProfileImage;
    final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    File SelectedFile;
    ProgressBar pbProfilePic,pbUpload;
    ActionCodeSettings actionCodeSettings;
    ProgressDialog pdLoading;
    TextView tvPercentGlobal;
    Dialog uploadDialogue;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        toolbar = (MaterialToolbar)findViewById(R.id.register_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle(getResources().getString(R.string.register));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        pdLoading = new ProgressDialog(Register.this);




        tiName = (TextInputEditText)findViewById(R.id.tiName);
        tiEmail = (TextInputEditText)findViewById(R.id.ti_email);
        tiPassword = (TextInputEditText)findViewById(R.id.ti_password);
        tiCpassword = (TextInputEditText)findViewById(R.id.ti_cpassword);
        civProfileImage = (CircleImageView)findViewById(R.id.civ_profile) ;
        pbProfilePic = (ProgressBar)findViewById(R.id.pb_profile_pic);


        civProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissions()){
                    Intent selectImage = new Intent(Intent.ACTION_PICK);
                    selectImage.setType("image/*");
                    startActivityForResult(Intent.createChooser(selectImage,getResources().getString(R.string.selectImage)),100);
                }else {
                    ActivityCompat.requestPermissions(Register.this,PERMISSIONS,101);
                }
            }
        });

        btRegister = (Button)findViewById(R.id.bt_register);

        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = tiName.getText().toString();
                email = tiEmail.getText().toString();
                password = tiPassword.getText().toString();
                cPassword = tiCpassword.getText().toString();

                if (name!=null && !name.equalsIgnoreCase("")){
                    if (email!=null && !email.equalsIgnoreCase("")){
                        if (password!=null && !password.equalsIgnoreCase("")){
                            if (cPassword!=null && !cPassword.equalsIgnoreCase("")){
                                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                                    if (password.equalsIgnoreCase(cPassword)){
                                        registerUser();
                                    }else {
                                        raiseDailog(getResources().getString(R.string.passwordsNotMatching));
                                    }
                                }else {
                                    raiseDailog(getResources().getString(R.string.invalidEmail));
                                }
                            }else {
                                raiseDailog(getResources().getString(R.string.confirmPassIsEmpty));
                            }
                        }else {
                            raiseDailog(getResources().getString(R.string.passwordIsEmpty));
                        }
                    }else {
                        raiseDailog(getResources().getString(R.string.emailIsEmpty));
                    }
                }else {
                    raiseDailog(getResources().getString(R.string.nameIsEmpty));
                }
            }
        });

    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            for (String permission : PERMISSIONS){
                if (ActivityCompat.checkSelfPermission(Register.this,permission)!= PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        return true;
    }

    private void registerUser() {
        new AsyncRegisterUser().execute();
    }


    public void raiseDailog(String message){
        AlertDialog.Builder dialog = new AlertDialog.Builder(Register.this);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
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
                    final MediaType MEDIATYPE = MediaType.parse("image/*");
                    MultipartBody multipartBody =
                            new MultipartBody.Builder().setType(MultipartBody.FORM)
                                    .addFormDataPart("uploads","profile.png",RequestBody.create(MEDIATYPE,SelectedFile)).build();

                    RequestBody req = ProgressHelper.withProgress(multipartBody, new ProgressUIListener() {

                        @Override
                        public void onUIProgressStart(long totalBytes) {
                            super.onUIProgressStart(totalBytes);
                            showDialog(Register.this,"Uploading...");
                        }

                        @Override
                        public void onUIProgressChanged(long numBytes, long totalBytes, final float percent, float speed) {
                            tvPercentGlobal.setText(String.valueOf((int) (percent*100)));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                pbUpload.setProgress((int) (percent*100),true);
                            }else {
                                pbUpload.setProgress((int) (percent*100));
                            }


                        }

                        @Override
                        public void onUIProgressFinish() {
                            super.onUIProgressFinish();
                            uploadDialogue.dismiss();
                            Log.e("uploadPercent", "finished");
                        }
                    });
                    new AsyncUploadImage().execute(req);
                }catch (NullPointerException e){
                    e.printStackTrace();
                }


            }
        }
    }


    public void showDialog(Activity activity, String msg) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_progress_bar);

        final ProgressBar text = (ProgressBar) dialog.findViewById(R.id.progress_horizontal);
        final TextView tvPercent = dialog.findViewById(R.id.value123);
        pbUpload = text;
        tvPercentGlobal = tvPercent;



        dialog.show();
        uploadDialogue = dialog;

        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
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
                Toast.makeText(Register.this,getResources().getString(R.string.needPermissionsProfilePic),Toast.LENGTH_LONG).show();
            }
        }
    }

    private class AsyncUploadImage extends AsyncTask<RequestBody,Void, JSONObject> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbProfilePic.setVisibility(View.VISIBLE);
        }


        @Override
        protected JSONObject doInBackground(RequestBody... voids) {
            Request request = new Request.Builder()
                    .url(Urls.upload)
                    .post(voids[0])
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
                if (jsonObject.optBoolean("success")){
                    uploadedImage = jsonObject.optJSONArray("image").optJSONObject(0).optString("filename");

                    Glide.with(Register.this).load(Urls.getImage+"/"+uploadedImage).addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            pbProfilePic.setVisibility(View.GONE);
                            Toast.makeText(Register.this,getResources().getString(R.string.unableToLoadProfilePic),Toast.LENGTH_LONG).show();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            pbProfilePic.setVisibility(View.GONE);
                            civProfileImage.setImageDrawable(resource);
                            return true;
                        }
                    }).into(civProfileImage);

                }else {
                    Toast.makeText(Register.this,jsonObject.optString("message"),Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(Register.this,getResources().getString(R.string.unbleToContactServer),Toast.LENGTH_LONG).show();
            }
        }
    }

    private class AsyncRegisterUser extends AsyncTask<Void,Void,JSONObject>{

        @Override
        protected JSONObject doInBackground(Void... voids) {
            JSONObject data = new JSONObject();
            try {
                data.put("name",name);
                data.put("email",email);
                data.put("password",password);
                data.put("profile_pic",uploadedImage);
                PostHelper postHelper = new PostHelper(Register.this);
                return postHelper.Post(Urls.register,data.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdLoading.setMessage("Loading...");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            if (jsonObject!=null){
                if (jsonObject.optBoolean("success")){
                    receivedUserId = jsonObject.optString("user_id");
                    registerWithFirebase();

                }else {
                    pdLoading.dismiss();
                    Toast.makeText(Register.this,jsonObject.optString("message"),Toast.LENGTH_LONG).show();
                }
            }else {
                pdLoading.dismiss();
                Toast.makeText(Register.this,getResources().getString(R.string.noResponse),Toast.LENGTH_LONG).show();
            }
        }
    }

    private void registerWithFirebase() {
        actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl(Urls.verifyEmail+receivedUserId)
                .setHandleCodeInApp(false)
                .setIOSBundleId("com.salam.memespool")
                .setAndroidPackageName("com.salam.memespool",false,null).build();


        final FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            sendVerificationEmail(auth);
                        }else {
                            pdLoading.dismiss();
                            Toast.makeText(Register.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void sendVerificationEmail(FirebaseAuth auth) {
        FirebaseUser user = auth.getCurrentUser();
        user.sendEmailVerification(actionCodeSettings).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    pdLoading.dismiss();
                    raiseDailogVerify(getResources().getString(R.string.verifyEmailMessage));
                }else {
                    pdLoading.dismiss();
                    Toast.makeText(Register.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void raiseDailogVerify(String string) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(Register.this);
        dialog.setTitle(getResources().getString(R.string.registeredSuccessful));
        dialog.setMessage(string);
        dialog.setCancelable(false);
        dialog.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Register.this,Login.class));
                finish();
            }
        });
        dialog.show();
    }
}
