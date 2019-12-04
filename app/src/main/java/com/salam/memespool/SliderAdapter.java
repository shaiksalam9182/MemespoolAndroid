package com.salam.memespool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

class SliderAdapter extends PagerAdapter {
    Context context;
    ArrayList<File> imagesList;
    LayoutInflater inflater;


    public SliderAdapter(Postupload postupload, ArrayList<File> imagesList) {
        this.context = postupload;
        this.imagesList = imagesList;
        inflater = LayoutInflater.from(postupload);
    }

    @Override
    public int getCount() {
        return imagesList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View v = inflater.inflate(R.layout.custom_image_view,container,false);
        ImageView img = (ImageView)v.findViewById(R.id.img);



        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });


        Glide.with(context).load(imagesList.get(position)).into(img);

        container.addView(v,0);


        return v;

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
