package com.example.user.library;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdminMyProposals_PagaerAdapter extends FragmentPagerAdapter {
    private Context context;
    private final List<Fragment> mFragmentList = new ArrayList<>();

    public AdminMyProposals_PagaerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override public int getCount() {
        return Constants.PAGE_COUNT;
    }

    @Override public Fragment getItem(int position) {
        Fragment currFragment =  mFragmentList.get(position);
        return currFragment;
    }

    @Override public CharSequence getPageTitle(int position) {
        // генерируем заголовок в зависимости от позиции
        return Constants.tabTitles[position];
    }
    public void addFragment(Fragment fragment, Bundle args) {
        fragment.setArguments(args);
        mFragmentList.add(fragment);
    }
}
