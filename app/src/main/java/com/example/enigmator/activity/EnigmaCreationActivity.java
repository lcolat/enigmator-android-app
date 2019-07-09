package com.example.enigmator.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.enigmator.R;
import com.example.enigmator.controller.HttpManager;
import com.example.enigmator.controller.HttpRequest;
import com.example.enigmator.entity.Enigma;
import com.example.enigmator.entity.Response;
import com.google.gson.Gson;

import static com.example.enigmator.controller.HttpRequest.POST;

public class EnigmaCreationActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 43;

    private static final String TAG = EnigmaCreationActivity.class.getName();

    private TextView textMedia;

    private HttpManager httpManager;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enigma_creation);

        httpManager = new HttpManager(this);
        gson = new Gson();

        textMedia = findViewById(R.id.text_media_chosen);
        ImageButton btnMedia = findViewById(R.id.btn_add_media);
        final EditText editName = findViewById(R.id.edit_name);
        final EditText editQuestion = findViewById(R.id.edit_question);
        final EditText editAnswer = findViewById(R.id.edit_answer);
        final EditText editScore = findViewById(R.id.edit_score);
        final Button btnSubmit = findViewById(R.id.btn_submit);

        btnMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mediaChooser = new Intent(Intent.ACTION_GET_CONTENT);
                mediaChooser.setType("video/*, image/*");
                startActivityForResult(mediaChooser, REQUEST_CODE);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String answer = editAnswer.getText().toString();
                String question = editQuestion.getText().toString();
                String name = editName.getText().toString();
                int score = Integer.parseInt(editScore.getText().toString());

                if (answer.isEmpty() || name.isEmpty() || score < Enigma.MIN_SCORE || score > Enigma.MAX_SCORE ) {
                    Toast.makeText(EnigmaCreationActivity.this, R.string.toast_invalid_form, Toast.LENGTH_SHORT).show();
                } else {
                    final Enigma enigma = new Enigma();
                    enigma.setAnswer(answer);
                    enigma.setScoreReward(score);
                    enigma.setName(name);

                    if (!question.isEmpty()) {
                        enigma.setQuestion(question);
                    } else {
                        //TODO: post media and show media
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(EnigmaCreationActivity.this);
                    builder.setTitle(R.string.enigma_creation);
                    builder.setMessage(R.string.enigma_dialog_message);
                    builder.setNegativeButton(android.R.string.cancel, null);
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            httpManager.addToQueue(POST, "/Enigmes/CreateEnigme", gson.toJson(enigma), new HttpRequest.HttpRequestListener() {
                                @Override
                                public void prepareRequest() {
                                    btnSubmit.setEnabled(false);
                                }

                                @Override
                                public void handleSuccess(Response response) {
                                    Toast.makeText(EnigmaCreationActivity.this, R.string.enigma_submitted, Toast.LENGTH_SHORT).show();
                                    finish();
                                }

                                @Override
                                public void handleError(Response error) {
                                    btnSubmit.setEnabled(true);
                                    Log.e(TAG, "/Enigmes/CreateEnigme : " + gson.toJson(enigma));
                                    Log.e(TAG, error.toString());
                                }
                            });
                        }
                    });
                    builder.create().show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // TODO: get chosen media

            }
        }
    }
}
