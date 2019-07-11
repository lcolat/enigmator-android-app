package com.example.enigmator.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.enigmator.R;
import com.example.enigmator.activity.EnigmaActivity;
import com.example.enigmator.activity.EnigmaCreationActivity;
import com.example.enigmator.controller.EnigmaRecyclerViewAdapter;
import com.example.enigmator.controller.HttpManager;
import com.example.enigmator.controller.HttpRequest;
import com.example.enigmator.entity.Enigma;
import com.example.enigmator.entity.Response;
import com.example.enigmator.entity.UserEnigmator;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.example.enigmator.controller.HttpRequest.GET;

/**
 * A simple {@link Fragment} subclass.
 */
public class EnigmaFragment extends Fragment {
    private static final String TAG = EnigmaFragment.class.getName();

    private static final int REQUEST_CODE = 87;

    private List<Enigma> enigmas, waitingValidation;
    private EnigmaRecyclerViewAdapter adapter;
    private HttpManager httpManager;
    private OnListFragmentInteractionListener mListener;
    private Gson gson;

    private Button btnEasy, btnHard, btnRandom, btnValidate;

    private TextView textEmpty;
    private ProgressBar progressBar;

    private boolean isValidator;
    private int userId;
    private SortType lastSort;

    private enum SortType {
        EASIEST,
        HARDEST,
        RANDOM,
        VALIDATE
    }

    public EnigmaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        gson = new Gson();
        if (context instanceof UserFragment.OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            mListener = new EnigmaFragment.OnListFragmentInteractionListener() {
                @Override
                public void onListFragmentInteraction(Enigma enigma) {
                    Intent intent = new Intent(getContext(), EnigmaActivity.class);
                    intent.putExtra(EnigmaActivity.ENIGMA_KEY, gson.toJson(enigma));
                    startActivityForResult(intent, REQUEST_CODE);
                }
            };
        }
        httpManager = new HttpManager(getContext());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enigmas = new ArrayList<>();
        waitingValidation = new ArrayList<>();
        adapter = new EnigmaRecyclerViewAdapter(getContext(), enigmas, mListener);

        UserEnigmator user = UserEnigmator.getCurrentUser(getContext());
        assert user != null;
        isValidator = user.isValidator();
        userId = user.getId();
        lastSort = SortType.EASIEST;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enigma, container, false);

        progressBar = view.findViewById(R.id.progress_loading);
        textEmpty = view.findViewById(R.id.text_empty);
        final RecyclerView recyclerView = view.findViewById(R.id.list_enigmas);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        btnEasy = view.findViewById(R.id.btn_sort_easy);
        btnHard = view.findViewById(R.id.btn_sort_hard);
        btnRandom = view.findViewById(R.id.btn_sort_random);
        btnValidate = view.findViewById(R.id.btn_validate_enigma);
        btnEasy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortEnigmas(SortType.EASIEST);
            }
        });
        btnHard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortEnigmas(SortType.HARDEST);
            }
        });
        btnRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortEnigmas(SortType.RANDOM);
            }
        });

        // Restore select button color
        switch (lastSort) {
            case EASIEST:
                btnEasy.setTextColor(getResources().getColor(R.color.colorPrimary));
                break;
            case HARDEST:
                btnHard.setTextColor(getResources().getColor(R.color.colorPrimary));
                break;
            case RANDOM:
                btnRandom.setTextColor(getResources().getColor(R.color.colorPrimary));
                break;
            case VALIDATE:
                btnValidate.setTextColor(getResources().getColor(R.color.colorPrimary));
                break;
        }

        FloatingActionButton btnAdd = view.findViewById(R.id.btn_add_enigma);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            btnAdd.setSupportBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        }
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), EnigmaCreationActivity.class);
                startActivity(intent);
            }
        });

        if (enigmas.isEmpty()) {
            httpManager.addToQueue(GET, "/UserEnigmators/" + userId + "/GetEnigmeNotDone",
                    null, new HttpRequest.HttpRequestListener() {
                @Override
                public void prepareRequest() {
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void handleSuccess(Response response) {
                    progressBar.setVisibility(View.GONE);

                    if (response.getStatusCode() != 204) {
                        enigmas = new ArrayList<>(Arrays.asList(gson.fromJson(response.getContent(), Enigma[].class)));
                        adapter.setValues(enigmas);
                        textEmpty.setVisibility(View.GONE);
                    } else {
                        textEmpty.setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                    sortEnigmas(SortType.EASIEST);
                }

                @Override
                public void handleError(Response error) {
                    progressBar.setVisibility(View.GONE);
                    textEmpty.setVisibility(View.VISIBLE);
                    Log.e(TAG, "/UserEnigmators/" + userId + "/GetEnigmeNotDone");
                    Log.e(TAG, error.toString());
                }
            });
        }

        if (isValidator) {
            btnValidate.setVisibility(View.VISIBLE);
            btnValidate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetButtonsColor();
                    textEmpty.setVisibility(waitingValidation.isEmpty() ? View.VISIBLE : View.GONE);
                    btnValidate.setTextColor(getResources().getColor(R.color.colorPrimary));

                    adapter.setValues(waitingValidation);
                    adapter.notifyDataSetChanged();
                    lastSort = SortType.VALIDATE;
                }
            });

            httpManager.addToQueue(GET, "/Enigmes?filter[where][status]=false", null, new HttpRequest.HttpRequestListener() {
                @Override
                public void prepareRequest() {
                    recyclerView.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void handleSuccess(Response response) {
                    recyclerView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);

                    if (response.getStatusCode() != 204) {
                        waitingValidation = new ArrayList<>(Arrays.asList(gson.fromJson(response.getContent(), Enigma[].class)));
                    }
                }

                @Override
                public void handleError(Response error) {
                    recyclerView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Enigmes?filter[where][status]=false");
                    Log.e(TAG, error.toString());
                }
            });
        }

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                int enigmaId = data.getIntExtra(EnigmaActivity.ENIGMA_ID_KEY, -1);
                boolean isEnigmaValidated = data.getBooleanExtra(EnigmaActivity.VALIDATION_STATUS_KEY, false);

                for (Enigma enigma : waitingValidation) {
                    if (enigma.getId() == enigmaId) {
                        waitingValidation.remove(enigma);
                        if (isEnigmaValidated) enigmas.add(enigma);
                        break;
                    }
                }

                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        httpManager.cancel(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void resetButtonsColor() {
        btnEasy.setTextColor(getResources().getColor(android.R.color.secondary_text_dark));
        btnHard.setTextColor(getResources().getColor(android.R.color.secondary_text_dark));
        btnRandom.setTextColor(getResources().getColor(android.R.color.secondary_text_dark));
        btnValidate.setTextColor(getResources().getColor(android.R.color.secondary_text_dark));
    }

    private void sortEnigmas(SortType sortType) {
        resetButtonsColor();
        textEmpty.setVisibility(enigmas.isEmpty() ? View.VISIBLE : View.GONE);

        switch (sortType) {
            case EASIEST:
                btnEasy.setTextColor(getResources().getColor(R.color.colorPrimary));
                Collections.sort(enigmas, new Comparator<Enigma>() {
                    @Override
                    public int compare(Enigma o1, Enigma o2) {
                        return o1.getScoreReward() - o2.getScoreReward();
                    }
                });
                break;
            case HARDEST:
                btnHard.setTextColor(getResources().getColor(R.color.colorPrimary));
                Collections.sort(enigmas, new Comparator<Enigma>() {
                    @Override
                    public int compare(Enigma o1, Enigma o2) {
                        return o2.getScoreReward() - o1.getScoreReward();
                    }
                });
                break;
            case RANDOM:
                btnRandom.setTextColor(getResources().getColor(R.color.colorPrimary));
                Collections.shuffle(enigmas);
                break;
        }
        lastSort = sortType;
        adapter.setValues(enigmas);
        adapter.notifyDataSetChanged();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Enigma enigma);
    }
}
