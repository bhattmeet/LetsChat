package com.example.letschat.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.example.letschat.R;
import com.squareup.picasso.Picasso;

public class ImageViewActivity extends AppCompatActivity {

    private ImageView imgFull;
    private String imgUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        imgFull = findViewById(R.id.imgFull);
        imgUrl = getIntent().getStringExtra("url");

        Picasso.get().load(imgUrl).into(imgFull);
    }
}