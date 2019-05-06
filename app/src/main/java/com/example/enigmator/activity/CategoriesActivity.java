package com.example.enigmator.activity;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.view.MenuItem;

import com.example.enigmator.R;
import com.example.enigmator.fragment.CategoriesFragmentAdapter;
import com.example.enigmator.fragment.NonSwipeableViewPager;

public class CategoriesActivity extends AppCompatActivity {
    static final String INITIAL_FRAGMENT_KEY = "initial_fragment_key";

    private CategoriesFragmentAdapter fragmentAdapter;
    private NonSwipeableViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                return updateMainFragment(menuItem.getItemId());
            }
        });

        viewPager = findViewById(R.id.viewpager);
        fragmentAdapter = new CategoriesFragmentAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(fragmentAdapter);

        int initialFragment = getIntent().getIntExtra(INITIAL_FRAGMENT_KEY, R.id.navigation_enigma);
        navView.setSelectedItemId(initialFragment);
    }

    private boolean updateMainFragment(@IdRes int itemId) {
        switch (itemId) {
            case R.id.navigation_users:
                viewPager.setCurrentItem(0);
                return true;
            case R.id.navigation_enigma:
                viewPager.setCurrentItem(1);
                return true;
            case R.id.navigation_leaderboard:
                viewPager.setCurrentItem(2);
                return true;
            case R.id.navigation_forum:
                viewPager.setCurrentItem(3);
                return true;
        }
        return false;
    }
}
