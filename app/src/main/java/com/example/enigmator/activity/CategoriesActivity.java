package com.example.enigmator.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.design.widget.BottomNavigationView;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.enigmator.R;
import com.example.enigmator.controller.HttpManager;
import com.example.enigmator.entity.UserEnigmator;
import com.example.enigmator.fragment.CategoriesFragmentAdapter;
import com.example.enigmator.fragment.NonSwipeableViewPager;

public class CategoriesActivity extends HttpActivity {
    public static final String PREF_USER = "pref_user";

    private static final int REQUEST_SETTINGS = 41;

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

        navView.setSelectedItemId(R.id.navigation_enigma);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!TermsActivity.hasAgreed(this)) {
            startActivity(new Intent(this, TermsActivity.class));
        }

        if (!isInternetConnected()) {
            buildNoConnectionErrorDialog(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                }
            });
        }
    }

    private boolean updateMainFragment(@IdRes int itemId) {
        int index = -1;
        switch (itemId) {
            case R.id.navigation_users:
                index = 0;
                break;
            case R.id.navigation_enigma:
                index = 1;
                break;
            case R.id.navigation_leaderboard:
                index = 2;
                break;
            case R.id.navigation_forum:
                index = 3;
                break;
        }
        if (index < 0) {
            return false;
        } else {
            viewPager.setCurrentItem(index);
            setTitle(fragmentAdapter.getPageTitle(index));
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_profile:
                Intent intent = new Intent(this, UserActivity.class);
                intent.putExtra(UserActivity.USER_KEY, UserEnigmator.getCurrentUser(this));
                startActivity(intent);
                return true;
            case R.id.menu_friend_requests:
                Intent inviteIntent = new Intent(this, FriendInviteActivity.class);
                startActivity(inviteIntent);
                return true;
            case R.id.menu_refresh_enigmas :
                fragmentAdapter.notifyDataSetChanged();
                return true;
            case R.id.menu_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(settingsIntent, REQUEST_SETTINGS);
                return true;
            case R.id.menu_disconnect:
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.remove(HttpManager.PREF_USER_TOKEN);
                editor.remove(PREF_USER);
                editor.apply();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SETTINGS) {
            if (resultCode == RESULT_OK) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.remove(PREF_USER);
                editor.apply();
                finish();
            }
        }
        else super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
