package com.example.EnglishApp.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.EnglishApp.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2};
    private final Context mContext;

    private  String lessonLevel="";
    private  int lessonNumber=0;

    public SectionsPagerAdapter(Context context, FragmentManager fm, String lessonLevel, int lessonNumber) {
        super(fm);
        this.lessonLevel=lessonLevel;
        this.lessonNumber=lessonNumber;
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        Fragment fragment=null;
        switch (position){
            case 0:
                Tab1Rule tab1Rule = Tab1Rule.newInstance(lessonLevel,lessonNumber);
                return  tab1Rule;
            case 1:
                Tab2Test tab2Test = Tab2Test.newInstance(lessonLevel,lessonNumber);

                return  tab2Test;
                //TODO dodelai okno 2
        }

        return fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }

    public String getLessonLevel() {
        return lessonLevel;
    }

    public void setLessonLevel(String lessonLevel) {
        this.lessonLevel = lessonLevel;
    }

    public int getLessonNumber() {
        return lessonNumber;
    }

    public void setLessonNumber(int lessonNumber) {
        this.lessonNumber = lessonNumber;
    }


}