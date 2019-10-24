package com.salam.memespool;

import android.annotation.SuppressLint;
import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetHelper {
    Context mContext;
    OkHttpClient client = new OkHttpClient();

    public GetHelper(Context context) {
        this.mContext = context;
    }

    @SuppressLint("NewApi")
    public JSONObject run(String url) throws IOException, JSONException {

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return new JSONObject(response.body().string());
        }

    }


}
