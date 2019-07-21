package com.example.enigmator.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.enigmator.R;
import com.example.enigmator.controller.DownloadProfilePictureTask;
import com.example.enigmator.controller.HttpRequest;
import com.example.enigmator.entity.Enigma;
import com.example.enigmator.entity.Response;
import com.example.enigmator.entity.UserEnigmator;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.enigmator.controller.HttpRequest.GET;
import static com.example.enigmator.controller.HttpRequest.MEDIA_IMAGE;
import static com.example.enigmator.controller.HttpRequest.POST;

public class UserActivity extends HttpActivity {
    private static final String TAG = UserActivity.class.getName();

    private static final int REQUEST_CODE_CHOOSE_PICTURE = 55;

    public static final String USER_KEY = "user_key";

    private FloatingActionButton btnCompare, btnAddUser;
    private ProgressBar progressLoading, progressImage;
    private LinearLayout layoutCompare;
    private TextView textEmptyPicture;
    private ImageView imageUser;
    private ImageButton btnChangePicture;

    private UserEnigmator currentUser;

    private boolean isFriend, isComparing, isSelfProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Intent intent = getIntent();
        final UserEnigmator user = (UserEnigmator) intent.getSerializableExtra(USER_KEY);
        currentUser = UserEnigmator.getCurrentUser(this);
        assert currentUser != null;
        isSelfProfile = user.getId() == currentUser.getId();

        isComparing = false;

        String username = user.getUsername();
        setTitle(username);

        layoutCompare = findViewById(R.id.layout_compare);
        btnCompare = findViewById(R.id.btn_compare);
        btnAddUser = findViewById(R.id.btn_user_action);
        progressLoading = findViewById(R.id.progress_loading);

        progressImage = findViewById(R.id.progress_image);
        imageUser = findViewById(R.id.image_profile);
        textEmptyPicture = findViewById(R.id.text_empty);
        btnChangePicture = findViewById(R.id.btn_change_picture);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            btnAddUser.setSupportBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
            btnCompare.setSupportBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        }

        TextView textScore = findViewById(R.id.text_user_score);
        textScore.setText(getString(R.string.text_score, user.getScore()));

        final TextView textEasy = findViewById(R.id.text_easy_solved);
        final TextView textMedium = findViewById(R.id.text_medium_solved);
        final TextView textHard = findViewById(R.id.text_hard_solved);
        final TextView textExtreme = findViewById(R.id.text_extreme_solved);
        final TextView textTotal = findViewById(R.id.text_total_solved);

        httpManager.addToQueue(GET, "/UserEnigmators/" + user.getId() + "/GetEnigmeDone", null, new HttpRequest.HttpRequestListener() {
            @Override
            public void prepareRequest() {
                progressLoading.setVisibility(View.VISIBLE);
            }

            @Override
            public void handleSuccess(Response response) {
                progressLoading.setVisibility(View.GONE);

                List<Enigma> userSolved = new ArrayList<>();
                if (response.getStatusCode() != 204) {
                    userSolved = new ArrayList<>(Arrays.asList(gson.fromJson(response.getContent(), Enigma[].class)));
                }

                setTextViews(UserActivity.this, userSolved, textEasy, textMedium, textHard, textExtreme, textTotal);
            }

            @Override
            public void handleError(Response error) {
                progressLoading.setVisibility(View.GONE);
                Log.e(TAG, "/UserEnigmators/" + user.getId() + "/GetEnigmeDone");
                Log.e(TAG, error.toString());
            }
        });

        if (user.getProfilePicture() == null) {
            textEmptyPicture.setVisibility(View.VISIBLE);
            if (isSelfProfile) btnChangePicture.setVisibility(View.VISIBLE);
        } else {
            downloadMedia(user.getProfilePicture());
        }

        if (isSelfProfile) {
            btnCompare.hide();
        } else {
            TextView textSelfScore = findViewById(R.id.text_self_user_score);
            textSelfScore.setText(getString(R.string.text_score, currentUser.getScore()));

            final TextView textSelfEasy = findViewById(R.id.text_self_easy_solved);
            final TextView textSelfMedium = findViewById(R.id.text_self_medium_solved);
            final TextView textSelfHard = findViewById(R.id.text_self_hard_solved);
            final TextView textSelfExtreme = findViewById(R.id.text_self_extreme_solved);
            final TextView textSelfTotal = findViewById(R.id.text_self_total_solved);

            httpManager.addToQueue(GET, "/UserEnigmators/" + currentUser.getId() + "/GetEnigmeDone", null, new HttpRequest.HttpRequestListener() {
                @Override
                public void prepareRequest() {
                    btnCompare.setEnabled(false);
                }

                @Override
                public void handleSuccess(Response response) {
                    btnCompare.setEnabled(true);

                    List<Enigma> selfSolved = new ArrayList<>();
                    if (response.getStatusCode() != 204) {
                        selfSolved = new ArrayList<>(Arrays.asList(gson.fromJson(response.getContent(), Enigma[].class)));
                    }

                    setTextViews(UserActivity.this, selfSolved, textSelfEasy, textSelfMedium, textSelfHard, textSelfExtreme, textSelfTotal);
                }

                @Override
                public void handleError(Response error) {
                    Log.e(TAG, "/UserEnigmators/" + currentUser.getId() + "/GetEnigmeDone");
                    Log.e(TAG, error.toString());
                }
            });

            httpManager.addToQueue(GET, "/UserEnigmators/" + user.getId() + "/isFriend",
                    null, new HttpRequest.HttpRequestListener() {
                        @Override
                        public void prepareRequest() {
                            btnAddUser.setEnabled(false);
                        }

                        @Override
                        public void handleSuccess(Response response) {
                            isFriend = gson.fromJson(response.getContent(), JsonObject.class)
                                    .get("isFriend").getAsBoolean();

                            if (!isFriend) {
                                btnAddUser.show();
                                btnAddUser.setEnabled(true);
                            }
                        }

                        @Override
                        public void handleError(Response error) {
                            Log.e(TAG, "/UserEnigmators/" + user.getId() + "/isFriend");
                            Log.e(TAG, error.toString());
                        }
                    });

            btnCompare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isComparing = !isComparing;
                    layoutCompare.setVisibility(isComparing ? View.VISIBLE : View.GONE);
                    btnCompare.setSupportBackgroundTintList(ColorStateList.valueOf(getResources()
                            .getColor(isComparing ? R.color.colorPrimaryDark : R.color.colorPrimary)));
                }
            });

            btnAddUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    httpManager.addToQueue(HttpRequest.POST, "/UserEnigmators/" + user.getId()
                            + "/AddAFriend", null, new HttpRequest.HttpRequestListener() {
                        @Override
                        public void prepareRequest() {
                            btnAddUser.setEnabled(false);
                        }

                        @Override
                        public void handleSuccess(Response response) {
                            btnAddUser.hide();
                            Toast.makeText(UserActivity.this, R.string.invite_sent, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void handleError(Response error) {
                            btnAddUser.setEnabled(true);
                            Log.e(TAG, "/UserEnigmators/" + user.getId() + "/AddAFriend");

                            if (error.getStatusCode() == 422) {
                                Log.d(TAG, "Invite already sent");
                                btnAddUser.hide();
                                Toast.makeText(UserActivity.this, R.string.invite_sent, Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e(TAG, error.toString());
                            }
                        }
                    });
                }
            });
        }

        btnChangePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mediaChooser = new Intent(Intent.ACTION_GET_CONTENT);
                mediaChooser.setType("image/*");
                startActivityForResult(mediaChooser, REQUEST_CODE_CHOOSE_PICTURE);
            }
        });
    }

    private static void setTextViews(Context context, List<Enigma> enigmas, TextView easy, TextView medium, TextView hard, TextView extreme, TextView total) {
        int easyCount = 0, mediumCount = 0, hardCount = 0, extremeCount = 0;
        for (Enigma e : enigmas) {
            int score = e.getScoreReward();
            if (score < Enigma.MEDIUM_THRESHOLD) easyCount++;
            else if (score < Enigma.HARD_THRESHOLD) mediumCount++;
            else if (score < Enigma.EXTREME_THRESHOLD) hardCount++;
            else extremeCount++;
        }

        easy.setText(context.getString(R.string.easy_solved, easyCount));
        medium.setText(context.getString(R.string.medium_solved, mediumCount));
        hard.setText(context.getString(R.string.hard_solved, hardCount));
        extreme.setText(context.getString(R.string.extreme_solved, extremeCount));
        total.setText(context.getString(R.string.total_solved, enigmas.size()));
    }

    private void downloadMedia(String filename) {
        new DownloadProfilePictureTask(this, new HttpRequest.HttpRequestListener() {
            @Override
            public void prepareRequest() {
                progressImage.setVisibility(View.VISIBLE);
            }

            @Override
            public void handleSuccess(Response response) {
                progressImage.setVisibility(View.GONE);
                String mediaDlFileName = response.getContent();
                if (mediaDlFileName == null) {
                    handleError(response);
                    return;
                }

                Bitmap bitmap = BitmapFactory.decodeFile(response.getContent());
                imageUser.setImageBitmap(bitmap);
                if (isSelfProfile) btnChangePicture.setVisibility(View.VISIBLE);
            }

            @Override
            public void handleError(Response error) {
                progressImage.setVisibility(View.GONE);
                if (isSelfProfile) btnChangePicture.setVisibility(View.VISIBLE);
                textEmptyPicture.setVisibility(View.VISIBLE);
                Log.e(TAG, " Error when download Media > " + error);
            }
        }).execute(filename);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_CHOOSE_PICTURE) {
            if (resultCode == RESULT_OK) {
                assert data != null;
                Uri uri = data.getData();

                assert uri != null;
                final String filename = EnigmaCreationActivity.getFileName(this, uri);

                final String path_temp = getCacheDir() + "/" + filename;
                File file = new File(path_temp);

                FileOutputStream fos = null;
                InputStream is = null;
                try {
                    byte[] buffer = new byte[1024];
                    fos = new FileOutputStream(file);
                    is = getContentResolver().openInputStream(uri);
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
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error on closing stream", e);
                    }
                }

                httpManager.addToQueue(POST, "/UserEnigmators/AddProfilePic",
                        path_temp, MEDIA_IMAGE, new HttpRequest.HttpRequestListener() {
                            @Override
                            public void prepareRequest() {
                                progressImage.setVisibility(View.VISIBLE);
                                btnChangePicture.setEnabled(false);
                            }

                            @Override
                            public void handleSuccess(Response response) {
                                Toast.makeText(UserActivity.this, R.string.profile_picture_changed, Toast.LENGTH_SHORT).show();
                                btnChangePicture.setEnabled(true);
                                imageUser.setImageBitmap(BitmapFactory.decodeFile(path_temp));
                                progressImage.setVisibility(View.GONE);
                                currentUser.setProfilePicture(filename);

                                UserEnigmator.saveCurrentUser(UserActivity.this, gson.toJson(currentUser));
                            }

                            @Override
                            public void handleError(Response error) {
                                progressImage.setVisibility(View.GONE);
                                Log.e(TAG, "UserEnigmators/AddProfilePic");
                                Log.e(TAG, error.toString());
                                btnChangePicture.setEnabled(true);
                            }
                        });
            }
        }
    }
}
