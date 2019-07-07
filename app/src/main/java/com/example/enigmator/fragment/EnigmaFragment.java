package com.example.enigmator.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.example.enigmator.controller.EnigmaRecyclerViewAdapter;
import com.example.enigmator.controller.HttpManager;
import com.example.enigmator.controller.HttpRequest;
import com.example.enigmator.entity.Enigma;
import com.example.enigmator.entity.Response;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class EnigmaFragment extends Fragment {
    private List<Enigma> enigmas;
    private EnigmaRecyclerViewAdapter adapter;
    private HttpManager httpManager;
    private OnListFragmentInteractionListener mListener;
    private Gson gson;

    Button btnEasy, btnHard, btnRandom;

    private TextView textEmpty;
    private ProgressBar progressBar;

    private enum SortType {
        EASIEST,
        HARDEST,
        RANDOM
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
                public void onListFragmentInteraction(View view, Enigma enigma) {
                    // TODO: startEnigma activity
                   /* Intent intent = new Intent(getContext(), EnigmaActivity.class);
                    intent.putExtra(EnigmaActivity.ENIGMA_ID_KEY, enigma.getId());
                    startActivity(intent);*/
                }
            };
        }
        httpManager = new HttpManager(getContext());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enigmas = new ArrayList<>();

        adapter = new EnigmaRecyclerViewAdapter(getContext(), enigmas, mListener);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enigma, container, false);

        progressBar = view.findViewById(R.id.progress_loading);
        textEmpty = view.findViewById(R.id.text_empty);
        RecyclerView recyclerView = view.findViewById(R.id.list_enigmas);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        btnEasy = view.findViewById(R.id.btn_sort_easy);
        btnHard = view.findViewById(R.id.btn_sort_hard);
        btnRandom = view.findViewById(R.id.btn_sort_random);
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

        if (enigmas.isEmpty()) {
            httpManager.addToQueue(HttpRequest.GET, "/Enigmes?filter[where][status]=false",
                    null, new HttpRequest.HttpRequestListener() {
                @Override
                public void prepareRequest() {
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void handleSuccess(Response response) {
                    progressBar.setVisibility(View.GONE);

                    if (response.getStatusCode() != 204) {
                        enigmas = Arrays.asList(gson.fromJson(response.getContent(), Enigma[].class));
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
                    Log.e(EnigmaFragment.class.getName(), "Enigmas error. " + error);
                }
            });
        }

        return view;
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

    private void sortEnigmas(SortType sortType) {
        switch (sortType) {
            case EASIEST:
                btnEasy.setTextColor(getResources().getColor(R.color.colorPrimary));
                btnHard.setTextColor(getResources().getColor(android.R.color.secondary_text_dark));
                btnRandom.setTextColor(getResources().getColor(android.R.color.secondary_text_dark));
                Collections.sort(enigmas, new Comparator<Enigma>() {
                    @Override
                    public int compare(Enigma o1, Enigma o2) {
                        return o1.getScoreReward() - o2.getScoreReward();
                    }
                });
                break;
            case HARDEST:
                btnHard.setTextColor(getResources().getColor(R.color.colorPrimary));
                btnEasy.setTextColor(getResources().getColor(android.R.color.secondary_text_dark));
                btnRandom.setTextColor(getResources().getColor(android.R.color.secondary_text_dark));
                Collections.sort(enigmas, new Comparator<Enigma>() {
                    @Override
                    public int compare(Enigma o1, Enigma o2) {
                        return o2.getScoreReward() - o1.getScoreReward();
                    }
                });
                break;
            case RANDOM:
                btnRandom.setTextColor(getResources().getColor(R.color.colorPrimary));
                btnEasy.setTextColor(getResources().getColor(android.R.color.secondary_text_dark));
                btnHard.setTextColor(getResources().getColor(android.R.color.secondary_text_dark));
                Collections.shuffle(enigmas);
                break;
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(View view, Enigma enigma);
    }

}
