package com.boilerplate.music.musicrecognition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.acrcloud.rec.sdk.ACRCloudClient;
import com.acrcloud.rec.sdk.ACRCloudConfig;
import com.acrcloud.rec.sdk.IACRCloudListener;
import com.airbnb.lottie.LottieAnimationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class Recognize extends AppCompatActivity implements IACRCloudListener {

    private ACRCloudClient mClient;
    private ACRCloudConfig mConfig;

    private TextView mVolume;
    private TextView mResult;

    private boolean mProcessing = false;
    private boolean initState = false;

    private String path = "";

    private long startTime = 0;
    private long stopTime = 0;

    private boolean response = false;

    private Button startBtn;

    private ImageView closeButton;

    private LottieAnimationView animationView;

    FrameLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize);

        Util.requestPermission(this, android.Manifest.permission.RECORD_AUDIO);
        Util.requestPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        path = Environment.getExternalStorageDirectory().toString()
                + "/acrcloud/model";

        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }

        rootLayout = findViewById(R.id.root_layout);

        if (savedInstanceState == null) {
            rootLayout.setVisibility(View.INVISIBLE);

            ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        circularRevealActivity();
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            rootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                });
            }
        }

        mVolume = findViewById(R.id.volume);
        mResult = findViewById(R.id.result);

        startBtn = findViewById(R.id.start);
        startBtn.setText(getResources().getString(R.string.start));

        closeButton = findViewById(R.id.close_activity);

        animationView = findViewById(R.id.lottieAnimationView);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                animationView.setVisibility(View.VISIBLE);
                /*startBtn.setVisibility(View.INVISIBLE);*/

                startBtn.setVisibility(View.INVISIBLE);
                startBtn.startAnimation(fadeAnimation(false));

                /*cancelBtn.setVisibility(View.VISIBLE);*/

                response = false;

                start();
            }
        });

        /*cancelBtn.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        startBtn.setVisibility(View.VISIBLE);
                        cancelBtn.setVisibility(View.INVISIBLE);

                        cancel();
                    }
                });*/


        this.mConfig = new ACRCloudConfig();
        this.mConfig.acrcloudListener = this;

        // If you implement IACRCloudResultWithAudioListener and override "onResult(ACRCloudResult result)", you can get the Audio data.
        //this.mConfig.acrcloudResultWithAudioListener = this;

        this.mConfig.context = this;
        this.mConfig.host = "identify-ap-southeast-1.acrcloud.com";
        this.mConfig.dbPath = path; // offline db path, you can change it with other path which this app can access.
        this.mConfig.accessKey = "be517564c7047126c2ced56f17118fee";
        this.mConfig.accessSecret = "gi38FCOSTRiBtsb1cpTRW0Z7WfWkxW8KiYxd7jvL";
        this.mConfig.protocol = ACRCloudConfig.ACRCloudNetworkProtocol.PROTOCOL_HTTP; // PROTOCOL_HTTPS
        this.mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_REMOTE;
        //this.mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_LOCAL;
        //this.mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_BOTH;

        this.mClient = new ACRCloudClient();
        // If reqMode is REC_MODE_LOCAL or REC_MODE_BOTH,
        // the function initWithConfig is used to load offline db, and it may cost long time.
        this.initState = this.mClient.initWithConfig(this.mConfig);
        if (this.initState) {
            this.mClient.startPreRecord(3000); //start prerecord, you can call "this.mClient.stopPreRecord()" to stop prerecord.
        }

        start();

        if(!response)
        {

            startBtn.setVisibility(View.INVISIBLE);


//            cancelBtn.setVisibility(View.INVISIBLE);

        }
        else if(response) {

            startBtn.setVisibility(View.VISIBLE);
//            cancelBtn.setVisibility(View.VISIBLE);

        }

    }

    public void start() {

        animationView.playAnimation();

        if (!this.initState) {
            Toast.makeText(this, "init error", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mProcessing) {
            mProcessing = true;
            mVolume.setText("");
            mResult.setText("Listening...");
            if (this.mClient == null || !this.mClient.startRecognize()) {
                mProcessing = false;
                mResult.setText("start error!");
            }
            startTime = System.currentTimeMillis();
        }
    }

    protected void cancel() {
        if (mProcessing && this.mClient != null) {
            mProcessing = false;
            this.mClient.cancel();
            mResult.setText("No Results Found");
        }
    }

    private Animation fadeAnimation(boolean fadeIn) {

        Animation animation = null;
        if (fadeIn)
            animation = new AlphaAnimation(0f, 1.0f);
        else
            animation = new AlphaAnimation(1.0f, 0f);
        animation.setDuration(500);
        animation.setFillEnabled(true);
        animation.setFillAfter(true);
        return animation;

    }

    private void circularRevealActivity() {

        int cx = rootLayout.getWidth() / 2;
        int cy = rootLayout.getHeight() / 2;

        float finalRadius = Math.max(rootLayout.getWidth(), rootLayout.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, cx, cy, 0, finalRadius);
        circularReveal.setDuration(1000);

        // make the view visible and start the animation
        rootLayout.setVisibility(View.VISIBLE);
        circularReveal.start();
    }

        // Old api
    @Override
    public void onResult(String result) {
        if (this.mClient != null) {
            this.mClient.cancel();
            mProcessing = false;
        }

        String tres = "\n";

        try {
            JSONObject j = new JSONObject(result);
            JSONObject j1 = j.getJSONObject("status");
            int j2 = j1.getInt("code");
            if(j2 == 0){
                JSONObject metadata = j.getJSONObject("metadata");
                //
                if (metadata.has("humming")) {
                    JSONArray hummings = metadata.getJSONArray("humming");
                    for(int i=0; i<hummings.length(); i++) {
                        JSONObject tt = (JSONObject) hummings.get(i);
                        String title = tt.getString("title");
                        JSONArray artistt = tt.getJSONArray("artists");
                        JSONObject art = (JSONObject) artistt.get(0);
                        String artist = art.getString("name");
                        tres = tres + (i+1) + ".  " + title + "\n";
                    }
                }
                if (metadata.has("music")) {
                    JSONArray musics = metadata.getJSONArray("music");
                    for(int i=0; i<1; i++) {
                        JSONObject tt = (JSONObject) musics.get(i);
                        String title = tt.getString("title");
                        JSONArray artistt = tt.getJSONArray("artists");
                        JSONObject art = (JSONObject) artistt.get(0);
                        String artist = art.getString("name");
                        tres = /*tres + (i+1) +*/ " Title: " + title + "  Artist: " + artist + "\n";
                    }
                }
                if (metadata.has("streams")) {
                    JSONArray musics = metadata.getJSONArray("streams");
                    for(int i=0; i<musics.length(); i++) {
                        JSONObject tt = (JSONObject) musics.get(i);
                        String title = tt.getString("title");
                        String channelId = tt.getString("channel_id");
                        tres = tres + (i+1) + ".  Title: " + title + "    Channel Id: " + channelId + "\n";
                    }
                }
                if (metadata.has("custom_files")) {
                    JSONArray musics = metadata.getJSONArray("custom_files");
                    for(int i=0; i<musics.length(); i++) {
                        JSONObject tt = (JSONObject) musics.get(i);
                        String title = tt.getString("title");
                        tres = tres + (i+1) + ".  Title: " + title + "\n";
                    }
                }
                /*tres = tres + "\n\n" + result;*/
            }else if(j2 == 1001){
                tres = "No Result Found";
            }
        } catch (JSONException e) {
            tres = result;
            e.printStackTrace();
        }

        mResult.setText(tres);

        response = true;

        startBtn.setVisibility(View.VISIBLE);
        startBtn.startAnimation(fadeAnimation(true));
//        cancelBtn.setVisibility(View.INVISIBLE);
        animationView.setProgress(0f);
        animationView.pauseAnimation();
        animationView.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onVolumeChanged(double volume) {
        long time = (System.currentTimeMillis() - startTime) / 1000;
        mVolume.setText(getResources().getString(R.string.volume) + volume + "\n Time：" + time + " s");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("MainActivity", "release");
        if (this.mClient != null) {
            this.mClient.release();
            this.initState = false;
            this.mClient = null;
        }
    }


}
