package com.example.enigmator.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
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
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.example.enigmator.controller.HttpRequest.POST;

public class EnigmaCreationActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_CHOOSE_MEDIA = 43;

    private static final String TAG = EnigmaCreationActivity.class.getName();

    private TextView textMedia;

    private HttpManager httpManager;
    private Gson gson;
    private String mediaType;
    private String chosenMedia;

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
                mediaChooser.setType("image/*, video/*");
                startActivityForResult(mediaChooser, REQUEST_CODE_CHOOSE_MEDIA);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String answer = editAnswer.getText().toString();
                final String question = editQuestion.getText().toString();
                String name = editName.getText().toString();
                String scoreEdit = editScore.getText().toString();

                int score = -1;
                if (scoreEdit.matches("\\d{1,3}")) {
                    score = Integer.parseInt(scoreEdit);
                }

                if (answer.isEmpty() || name.isEmpty() || question.isEmpty() || score < Enigma.MIN_SCORE || score > Enigma.MAX_SCORE ) {
                    Toast.makeText(EnigmaCreationActivity.this, R.string.toast_invalid_form, Toast.LENGTH_SHORT).show();
                } else {
                    final Enigma enigma = new Enigma();
                    enigma.setAnswer(answer);
                    enigma.setScoreReward(score);
                    enigma.setName(name);
                    enigma.setQuestion(question);

                    httpManager.addToQueue(POST, "/Enigmes/CreateEnigme", gson.toJson(enigma), new HttpRequest.HttpRequestListener() {
                        @Override
                        public void prepareRequest() {
                            btnSubmit.setEnabled(false);
                        }

                        @Override
                        public void handleSuccess(Response response) {
                            if (chosenMedia == null) {
                                Toast.makeText(EnigmaCreationActivity.this, R.string.enigma_submitted, Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                final int enigmaId = gson.fromJson(response.getContent(), JsonObject.class)
                                        .get("id").getAsInt();

                                httpManager.addToQueue(POST, "/Enigmes/" + enigmaId + "/AddMediaToEnigme",
                                        chosenMedia, mediaType, new HttpRequest.HttpRequestListener() {
                                    @Override
                                    public void prepareRequest() {

                                    }

                                    @Override
                                    public void handleSuccess(Response response) {
                                        Toast.makeText(EnigmaCreationActivity.this, R.string.enigma_submitted, Toast.LENGTH_SHORT).show();
                                        setResult(RESULT_OK);
                                        finish();
                                    }

                                    @Override
                                    public void handleError(Response error) {
                                        Log.e(TAG, "/Enigmes/" + enigmaId + "/AddMediaToEnigme");
                                        Log.e(TAG, error.toString());
                                    }
                                });
                            }
                        }

                        @Override
                        public void handleError(Response error) {
                            btnSubmit.setEnabled(true);
                            Log.e(TAG, "/Enigmes/CreateEnigme : " + gson.toJson(enigma));
                            Log.e(TAG, error.toString());
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CHOOSE_MEDIA) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();

                assert uri != null;
                String filename = getFileName(this, uri);

                String extension = filename.substring(filename.indexOf(".") + 1);
                String path_temp = getCacheDir() + filename;
                File file = new File(path_temp);

                FileOutputStream fos = null;
                try {
                    byte[] buffer = new byte[1024];
                    fos = new FileOutputStream(file);
                    InputStream is = getContentResolver().openInputStream(uri);
                    assert is != null;
                    int len = is.read(buffer);
                    while (len != -1) {
                        fos.write(buffer, 0, len);
                        len = is.read(buffer);
                    }
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "Error while opening file", e);
                } catch (IOException e) {
                    Log.e(TAG, "Error while opening file", e);
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            Log.e(TAG, "Error while closing output stream", e);
                        }
                    }
                }

                if ("jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension)
                        || "png".equalsIgnoreCase(extension)) {
                    mediaType = HttpRequest.MEDIA_IMAGE;
                } else {
                    mediaType = HttpRequest.MEDIA_VIDEO;
                }
                chosenMedia = path_temp;
                textMedia.setText(filename);
                textMedia.setVisibility(View.VISIBLE);
            }
        }
    }

    static String getFileName(Context context, Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            assert result != null;
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}
