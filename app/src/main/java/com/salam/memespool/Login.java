package com.salam.memespool;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Login extends AppCompatActivity {

    EditText tiEmail,tiPassword;
    Button btSubmit,btRegister;
    String email,password;
    SharedPreferences sd;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Color.parseColor("#21d6d3"));
        }

        tiEmail = (EditText)findViewById(R.id.ti_email);
        tiPassword = (EditText)findViewById(R.id.ti_password);

        btSubmit  = (Button)findViewById(R.id.bt_submit);
        btRegister = (Button)findViewById(R.id.bt_register);

        sd = getSharedPreferences("memespool", Context.MODE_PRIVATE);
        editor = sd.edit();

        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = tiEmail.getText().toString();
                password = tiPassword.getText().toString();

                if (!email.equalsIgnoreCase("")){
                    if (!password.equalsIgnoreCase("")){
                        new AsyncAuthenticateUser().execute();
                    }else {
                        raiseDialog(getResources().getString(R.string.passwordIsEmpty));
                    }
                }else {
                    raiseDialog(getResources().getString(R.string.emailIsEmpty));
                }
            }
        });

        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this,Register.class));
            }
        });

    }

    private void raiseDialog(String string) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(Login.this,R.style.AlertDialogTheme);
        dialog.setMessage(string);
        dialog.setCancelable(false);
        dialog.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private class AsyncAuthenticateUser extends AsyncTask<Void,Void, JSONObject> {

        ProgressDialog pdLoading = new ProgressDialog(Login.this,R.style.AlertDialogTheme);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdLoading.setMessage(getResources().getString(R.string.authenticating));
            pdLoading.setCancelable(false);
            pdLoading.show();
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {


            PostHelper postHelper = new PostHelper(Login.this);
            JSONObject data = new JSONObject();
            try {
                data.put("email",email);
                data.put("password",password);
                return postHelper.Post(Urls.login,data.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            pdLoading.dismiss();
            if (jsonObject!=null){
                Log.e("loginRes",jsonObject.toString());
                if (jsonObject.optBoolean("success")){
                        storeUserData(jsonObject);
                        startActivity(new Intent(Login.this,Feed.class));
                        finish();
                }else {
                    Toast.makeText(Login.this,jsonObject.optString("message"),Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(Login.this,getResources().getString(R.string.noResponse),Toast.LENGTH_LONG).show();
            }
        }
    }

    private void storeUserData(JSONObject jsonObject) {
        editor.putString("userData",jsonObject.optString("data"));
        editor.apply();
    }
}
