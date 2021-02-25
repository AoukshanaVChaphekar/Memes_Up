package com.example.memesup;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private GestureDetectorCompat gestureDetector;
    private ImageView likeMeme;
    private ImageView memeImageView;
    private ProgressBar progressBar;
    private Animation animation;

    private String currentImageUrl=null;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window window=this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getResources().getColor(R.color.notificationBarColor));

        Toast.makeText(this,"Swipe left for more",Toast.LENGTH_LONG).show();

        animation= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.shrink_grow);

        gestureDetector=new GestureDetectorCompat(this, new DairyGestureListener());

        memeImageView=(ImageView)findViewById(R.id.memeImageView);
        progressBar=(ProgressBar)findViewById(R.id.progressBar);
        likeMeme=(ImageView)findViewById(R.id.likeMeme);


        loadMeme();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(gestureDetector.onTouchEvent(event))
        {
            return true;
        }
        else
            return super.onTouchEvent(event);
    }


    public class DairyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            likeMeme.startAnimation(animation);
            likeMeme.setAlpha(1f);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    likeMeme.setAlpha(0f);
                   // likeMeme.animate().scaleX(0f).scaleY(0f).setDuration(1500);
                  }
            }, 500);
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onFling(MotionEvent downEvent, MotionEvent moveEvent, float velocityX, float velocityY) {

                int Swipe_Threshold=100;
                int Swipe_Velocity_Threshold=100;

                float diffx = moveEvent.getX() - downEvent.getX();
                float diffy = moveEvent.getY() - downEvent.getY();

                if(Math.abs(diffx)>Math.abs(diffy))
                {
                    //left or right swipe
                    if(Math.abs(diffx)>Swipe_Threshold && Math.abs(velocityX)>Swipe_Velocity_Threshold)
                    {
                        if(diffx<0)
                        {
                            //right swipe
                            loadMeme();
                        }
                        else
                        {
                            //left swipe
                        }
                        return true;
                    }
                }
         /*    else
               {
                    bottom ir top swipe
               }
         */
            return super.onFling(downEvent, moveEvent, velocityX, velocityY);

        }
    }

    private void loadMeme()
    {
        progressBar.setVisibility(View.VISIBLE);
        final String[] url = {"https://meme-api.herokuapp.com/gimme"};

// Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url[0],null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {


                        try {
                            currentImageUrl = response.getString("url");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Glide.with(getApplicationContext()).load(currentImageUrl).listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                progressBar.setVisibility(View.INVISIBLE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                               progressBar.setVisibility(View.INVISIBLE);
                                return false;
                            }
                        }).into(memeImageView);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    public void shareMeme(View view)
    {
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,"Hey! Checkout this cool meme I got from reddit "+currentImageUrl);
        Intent chooser=Intent.createChooser(intent,"Share this meme using..");
        startActivity(chooser);
    }
    public void nextMeme(View view)
    {
        loadMeme();
    }
}
