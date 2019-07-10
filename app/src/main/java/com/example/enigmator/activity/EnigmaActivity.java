package com.example.enigmator.activity;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.enigmator.R;
import com.example.enigmator.controller.DownloadFileFromURL;
import com.example.enigmator.controller.HttpManager;
import com.example.enigmator.controller.HttpRequest;
import com.example.enigmator.controller.HttpRequest.HttpRequestListener;
import com.example.enigmator.entity.Enigma;
import com.example.enigmator.entity.Response;
import com.example.enigmator.entity.StoreMedia;
import com.example.enigmator.entity.UserEnigmator;
import com.example.enigmator.utils.HttpRequestGenerator;
import com.example.enigmator.utils.MediaPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.io.File;
import java.net.HttpURLConnection;


public class EnigmaActivity extends AppCompatActivity {
    private static final String TAG = TopicActivity.class.getName();

    public static final String ENIGMA_ID_KEY = "enigma_id_key";
    public static final String ENIGMA_KEY = "enigma_key";
    public static final String VALIDATION_STATUS_KEY = "validation_status_key";

    private ProgressBar progressLoading;
    private HttpManager httpManager;
    HttpRequestGenerator httpRequestGenerator;
    private Gson gson;

    private Enigma enigma;
    private String mediaDlFileName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enigma);

        httpManager = new HttpManager(this);
        gson = new Gson();
        progressLoading = findViewById(R.id.progress_loading);
        httpRequestGenerator = new HttpRequestGenerator(this);

        Intent intent = getIntent();
        String enigmaJson = intent.getStringExtra(ENIGMA_KEY);
        if (enigmaJson != null) {
            enigma = gson.fromJson(enigmaJson, Enigma.class);
            setupScreen();
        } else {
            int id = intent.getIntExtra(ENIGMA_ID_KEY, -1);
            if (id < 0) {
                finish();
            } else {
                httpManager.addToQueue(HttpRequest.GET, "/Enigmes/" + id, null, new HttpRequestListener() {
                    @Override
                    public void prepareRequest() {
                        progressLoading.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void handleSuccess(Response response) {
                        progressLoading.setVisibility(View.GONE);

                        enigma = gson.fromJson(response.getContent(), Enigma.class);
                        setupScreen();
                    }

                    @Override
                    public void handleError(Response error) {
                        progressLoading.setVisibility(View.GONE);
                        finish();
                    }
                });
            }
        }
    }

    private void setupScreen() {
        setTitle(enigma.getName());
        ((TextView)findViewById(R.id.enigma_question)).setText(enigma.getQuestion());

        // Validator setup
        boolean isValidator = UserEnigmator.getCurrentUser(this).isValidator();
        if (true/*!enigma.isStatus() && isValidator*/) {
            LinearLayout layoutButtons = findViewById(R.id.layout_validator_buttons);
            layoutButtons.setVisibility(View.VISIBLE);

            Button btnReject = findViewById(R.id.btn_reject);
            Button btnValidate = findViewById(R.id.btn_validate);

            final Intent intent = new Intent();
            intent.putExtra(ENIGMA_ID_KEY, enigma.getId());

            btnReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: POST Rejection


                    intent.putExtra(VALIDATION_STATUS_KEY, false);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            });

            btnValidate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    httpManager.addToQueue(HttpRequest.PATCH,
                            "/Enigmes/" + enigma.getId() + "/ValidateEnigme", null,null);

                    intent.putExtra(VALIDATION_STATUS_KEY, true);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            });
        }
        else {
            httpRequestGenerator.requestMediaOfEnigma(enigma.getId(),
                    new HttpRequestListener() {
                @Override
                public void prepareRequest() {
                    progressLoading.setVisibility(View.VISIBLE);
                }

                @Override
                public void handleSuccess(Response response) {
                    findViewById(R.id.layout_validator_buttons).setVisibility(View.VISIBLE);

                    ImageButton button = findViewById(R.id.btn_send_response);
                    button.setOnClickListener(sendAnswerEnigma(button));

                    if(response.getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT &&
                            gson.fromJson(response.getContent(), JsonArray.class).size() != 0) {

                        String enigmaType = gson.fromJson(response.getContent(), JsonArray.class).
                                get(0).getAsJsonObject().get("type").getAsString();

                        downloadMedia(enigmaType,
                                gson.fromJson(response.getContent(), JsonArray.class).get(0)
                                        .getAsJsonObject().get("filename").getAsString());
                    }

                    progressLoading.setVisibility(View.GONE);
                }

                @Override
                public void handleError(Response error) {
                    progressLoading.setVisibility(View.GONE);
                    Log.e(TAG, "GetMediaOfEnigma" + error.toString());
                    finish();
                }
            });
        }
    }

    private void downloadMedia(final String enigmaType, String fileNameMedia) {

        final Context context = this;
        new DownloadFileFromURL( new HttpRequestListener() {

            @Override
            public void prepareRequest() { }

            @Override
            public void handleSuccess(Response response) {
                mediaDlFileName = response.getContent();
                if(mediaDlFileName == null) {
                    handleError(response);
                    return;
                }

                switch (enigmaType.toLowerCase()) {
                    case "image":
                        Bitmap bitmap = BitmapFactory.decodeFile(response.getContent());
                        ImageView imageView = findViewById(R.id.image_view);
                        imageView.setImageBitmap(bitmap);
                        imageView.setVisibility(View.VISIBLE);
                        break;

                    case "audio":
                    case "video":
                        PlayerView videoView = findViewById(R.id.videoView);
                        MediaPlayer mediaPlayer = new MediaPlayer();
                        videoView.setPlayer(mediaPlayer.getPlayerImpl(context));
                        mediaPlayer.play(response.getContent());
                        videoView.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void handleError(Response error) {
                Log.e(TAG, " Error when download Media > " +
                        error.getStatusCode());
            }
        } ).execute(new StoreMedia(fileNameMedia, this, enigmaType));
    }

    private View.OnClickListener sendAnswerEnigma(final ImageButton button) {
        final EditText zoneText =  findViewById(R.id.edit_chat);
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                httpRequestGenerator.answerEnigma(enigma.getId(),
                    zoneText.getText().toString(),
                    new HttpRequestListener() {

                        @Override
                        public void prepareRequest() {
                            button.setEnabled(false);
                            hideSoftKeyboard();
                        }

                        @Override
                        public void handleSuccess(Response response) {
                            Log.i(TAG, "responce=" + response.getContent());
                            if(response.getContent().contains("mauvaise réponse !")) {
                                Log.d(TAG, "BAD Answer");
                                zoneText.startAnimation(shakeError(zoneText));
                            } else {
                                zoneText.setTextColor(Color.GREEN);
                                zoneText.setFocusable(false);
                            }
                            button.setEnabled(true);
                        }

                        @Override
                        public void handleError(Response error) {
                            button.setEnabled(true);
                        }

                        private TranslateAnimation shakeError(EditText zoneText) {
                            TranslateAnimation shake = new TranslateAnimation(0,
                                    0,
                                    0,
                                    10);
                            shake.setDuration(1000);
                            shake.setInterpolator(new CycleInterpolator(5));

                            ValueAnimator valueAnimator = ObjectAnimator.ofInt(zoneText,
                                    "textColor",
                                    Color.BLACK,
                                    Color.RED
                            );
                            valueAnimator.setEvaluator(new ArgbEvaluator());
                            valueAnimator.setDuration(1000);
                            valueAnimator.setRepeatCount(1);
                            valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
                            valueAnimator.start();
                            return shake;
                        }
                    });
            }
        };
    }


    @Override
    public void onBackPressed() {
        exitActivity();
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }


    void exitActivity() {
        if(findViewById(R.id.videoView).getVisibility() == View.VISIBLE) {
            Log.d(TAG, "Player released");
            PlayerView videoView = findViewById(R.id.videoView);
            videoView.getPlayer().stop();
            videoView.getPlayer().release();
        }

        if(mediaDlFileName != null) {
            String status = new File(mediaDlFileName).delete() ? "Successfully" : "Unsuccessfully";
            Log.i(TAG, "Media Downloaded has been deleted > " + status);
        }
    }

    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) this.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                this.getCurrentFocus().getWindowToken(), 0);
    }
}
