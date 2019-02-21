package com.kumar.ak.arpit.mydecks;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.net.URL;

public class ImageDialog extends AppCompatActivity {

    private ImageView mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_dialog);

        mDialog = (ImageView)findViewById(R.id.card);
        mDialog.setClickable(true);

        QueryUtils utils = new QueryUtils();

        String imageUrl = getIntent().getStringExtra("imageUrl");
        URL url = utils.createUrl(imageUrl);
        GlideApp.with(ImageDialog.this).load(url).into(mDialog);


        //finish the activity (dismiss the image dialog) if the user clicks
        //anywhere on the image
        mDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
