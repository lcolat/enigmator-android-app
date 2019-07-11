package com.example.enigmator.fragment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.enigmator.R;

public class CategoriesFragmentAdapter extends FragmentPagerAdapter {
    private final Context context;

    public CategoriesFragmentAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public Fragment getItem(int position) {
        System.out.println("position: "+ position);
        switch (position) {
            case 0:
                return new UserFragment();
            case 1:
                return new EnigmaFragment();
            case 2:
                return new LeaderboardFragment();
            case 3:
                return new ForumFragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.users);
            case 1:
                return context.getString(R.string.enigma);
            case 2:
                return context.getString(R.string.leaderboard);
            case 3:
                return context.getString(R.string.forum);
            default:
                return super.getPageTitle(position);
        }
    }
}
