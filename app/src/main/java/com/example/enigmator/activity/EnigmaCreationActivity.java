package com.example.enigmator.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.enigmator.R;
import com.example.enigmator.controller.HttpRequest;
import com.example.enigmator.entity.Enigma;
import com.example.enigmator.entity.Response;

import static com.example.enigmator.controller.HttpRequest.POST;

public class EnigmaCreationActivity extends HttpActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enigma_creation);

        final TextView textMedia = findViewById(R.id.text_media_chosen);
        ImageButton btnMedia = findViewById(R.id.btn_add_media);
        final EditText editQuestion = findViewById(R.id.edit_question);
        final EditText editAnswer = findViewById(R.id.edit_answer);
        final Button btnSubmit = findViewById(R.id.btn_submit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String answer = editAnswer.getText().toString();
                String question = editQuestion.getText().toString();

                if (answer.isEmpty()) {
                    Toast.makeText(EnigmaCreationActivity.this, R.string.toast_answer_empty, Toast.LENGTH_SHORT).show();
                } else if (question.isEmpty() && textMedia.getVisibility() == View.GONE) {
                    Toast.makeText(EnigmaCreationActivity.this, R.string.toast_no_question, Toast.LENGTH_SHORT).show();
                } else {
                    final Enigma enigma = new Enigma();
                    enigma.setAnswer(answer);
                    if (!question.isEmpty()) {
                        enigma.setQuestion(question);
                    } else {
                        //TODO: post media
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(EnigmaCreationActivity.this);
                    builder.setNegativeButton(android.R.string.cancel, null);
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            httpManager.addToQueue(POST, "/Enigmes", gson.toJson(enigma), new HttpRequest.HttpRequestListener() {
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
                                }
                            });
                        }
                    });
                }
            }
        });
    }
}
