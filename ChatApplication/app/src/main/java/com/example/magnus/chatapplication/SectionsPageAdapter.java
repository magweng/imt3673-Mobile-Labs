package com.example.magnus.chatapplication;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * SectionsPageAdapter used to add fragments
 */

class SectionsPageAdapter extends FragmentPagerAdapter{

    // Keep track of fragments
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    /**
     * Add fragment
     * @param fragment fragment to add
     * @param title title of fragment
     */
    public void addFragment(Fragment fragment, String title){
        this.mFragmentList.add(fragment);
        this.mFragmentTitleList.add(title);
    }

    public SectionsPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }
}
