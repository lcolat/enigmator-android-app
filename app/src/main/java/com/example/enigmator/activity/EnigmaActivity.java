package com.example.enigmator.activity;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.enigmator.R;
import com.example.enigmator.controller.DownloadFileFromURLTask;
import com.example.enigmator.controller.HttpManager;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.enigmator.controller.HttpRequest.GET;


public class EnigmaActivity extends AppCompatActivity {
    private static final String TAG = EnigmaActivity.class.getName();

    public static final String ENIGMA_ID_KEY = "enigma_id_key";
    public static final String ENIGMA_KEY = "enigma_key";
    public static final String VALIDATION_STATUS_KEY = "validation_status_key";

    private static final String PAST_ANSWERS_BASE_PREF = "past_answers_";

    private ProgressBar progressLoading;
    private TextView textQuestion;
    private ImageButton btnSendAnswer;

    private SharedPreferences prefs;
    private HttpRequestGenerator httpRequestGenerator;
    private Gson gson;
    private HttpManager httpManager;

    private List<String> pastAnswers;
    private ArrayAdapter adapter;
    private Enigma enigma;
    private String mediaDlFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enigma);

        textQuestion = findViewById(R.id.text_enigma_question);
        btnSendAnswer = findViewById(R.id.btn_send_response);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        progressLoading = findViewById(R.id.progress_loading);
        httpRequestGenerator = new HttpRequestGenerator(this);
        httpManager = new HttpManager(this);
        gson = new Gson();

        Intent intent = getIntent();
        String enigmaJson = intent.getStringExtra(ENIGMA_KEY);
        if (enigmaJson != null) {
            enigma = gson.fromJson(enigmaJson, Enigma.class);
            setupScreen();
        } else {
            final int id = intent.getIntExtra(ENIGMA_ID_KEY, -1);
            if (id < 0) {
                finish();
            } else {
                httpManager.addToQueue(GET, "/Enigmes/" + id, null, new HttpRequestListener() {
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
                        Log.e(TAG, "/Enigmes/" + id);
                        Log.e(TAG, error.toString());
                        finish();
                    }
                });
            }
        }
    }

    private void setupScreen() {
        setTitle(enigma.getName());      
        
        textQuestion.setText(enigma.getQuestion());
      
        // Validator setup
        boolean isValidator = UserEnigmator.getCurrentUser(this).isValidator();
        if (!enigma.isStatus() && isValidator) {
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
                    intent.putExtra(ENIGMA_ID_KEY, enigma.getId());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            });

            btnValidate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: change method
                    //httpManager.addToQueue(HttpRequest.PUT,
                      //      "/Enigmes/" + enigma.getId() + "/ValidateEnigme", null,null);

                    intent.putExtra(VALIDATION_STATUS_KEY, true);
                    intent.putExtra(ENIGMA_ID_KEY, enigma.getId());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            });
        }
        else {
            RelativeLayout layoutAnswer = findViewById(R.id.layout_answer_enigma);
            layoutAnswer.setVisibility(View.VISIBLE);

            // List past answers
            Set<String> pastSavedAnswers = prefs.getStringSet(PAST_ANSWERS_BASE_PREF + enigma.getId(), new HashSet<String>());
            if (pastSavedAnswers == null) pastAnswers = new ArrayList<>();
            else pastAnswers = new ArrayList<>(pastSavedAnswers);

            ListView listAnswers = findViewById(R.id.list_past_answers);
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, pastAnswers);
            listAnswers.setAdapter(adapter);


            httpRequestGenerator.requestMediaOfEnigma(enigma.getId(),
                    new HttpRequestListener() {
                @Override
                public void prepareRequest() {
                    progressLoading.setVisibility(View.VISIBLE);
                }

                @Override
                public void handleSuccess(Response response) {
                    btnSendAnswer.setOnClickListener(sendAnswerEnigma(btnSendAnswer));

                    if (response.getStatusCode() != HttpURLConnection.HTTP_NO_CONTENT &&
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
                    Log.e(TAG, "GetMediaOfEnigma. " + error);
                    finish();
                }
            });
        }
    }

    private void downloadMedia(final String enigmaType, String fileNameMedia) {
        final Context context = this;
      
        new DownloadFileFromURLTask(new HttpRequestListener() {
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
                        break;
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
                Log.e(TAG, " Error when download Media > " + error);
            }
        } ).execute(new StoreMedia(fileNameMedia, this, enigmaType));
    }

    private View.OnClickListener sendAnswerEnigma(final ImageButton button) {
        final EditText zoneText =  findViewById(R.id.edit_chat);
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String answer = zoneText.getText().toString();

                httpRequestGenerator.answerEnigma(enigma.getId(),
                    answer,
                    new HttpRequestListener() {
                        @Override
                        public void prepareRequest() {
                            button.setEnabled(false);
                            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                            View view = getCurrentFocus();
                            if (view == null) {
                                view = new View(EnigmaActivity.this);
                            }
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }

                        @Override
                        public void handleSuccess(Response response) {
                            if (response.getContent().contains("mauvaise")) {
                                zoneText.startAnimation(shakeError(zoneText));
                                pastAnswers.add(answer);
                                adapter.notifyDataSetChanged();
                            } else {
                                zoneText.setEnabled(false);
                                zoneText.setTextColor(Color.GREEN);
                                zoneText.setFocusable(false);
                                prefs.edit().remove(PAST_ANSWERS_BASE_PREF + enigma.getId()).apply();
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
        setResult(Activity.RESULT_CANCELED);

        if (enigma != null) {
            prefs.edit()
                    .putStringSet(PAST_ANSWERS_BASE_PREF + enigma.getId(), new HashSet<>(pastAnswers))
                    .apply();
        }

        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        if (findViewById(R.id.videoView).getVisibility() == View.VISIBLE) {
            Log.d(TAG, "Player released");
            PlayerView videoView = findViewById(R.id.videoView);
            videoView.getPlayer().stop();
            videoView.getPlayer().release();
        }

        if (mediaDlFileName != null) {
            String status = new File(mediaDlFileName).delete() ? "Successfully" : "Unsuccessfully";
            Log.d(TAG, "Media Downloaded has been deleted > " + status);
        }

        super.onDestroy();
    }
}
