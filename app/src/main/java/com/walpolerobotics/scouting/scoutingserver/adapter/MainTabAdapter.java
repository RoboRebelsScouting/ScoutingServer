package com.walpolerobotics.scouting.scoutingserver.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;

import com.walpolerobotics.scouting.scoutingserver.fragment.DevicesFragment;
import com.walpolerobotics.scouting.scoutingserver.fragment.FilesFragment;

public class MainTabAdapter extends FragmentPagerAdapter {

    private static final int PAGE_COUNT = 2;

    private SparseArray<Fragment> mFragments = new SparseArray<>();

    public MainTabAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (mFragments.get(position) == null) {
            switch (position) {
                case 0:
                    mFragments.put(position, new DevicesFragment());
                    break;
                case 1:
                    mFragments.put(position, new FilesFragment());
                    break;
            }
        }

        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case DevicesFragment.POSITION:
                return DevicesFragment.FRAGMENT_TITLE;
            case FilesFragment.POSITION:
                return FilesFragment.FRAGMENT_TITLE;
            default:
                return "";
        }
    }
}
