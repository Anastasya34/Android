package com.example.user.library;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AdminMyProposals_Main extends Fragment {
    public static int admin_id = -1;
    View rootView;
    AdminMyProposals_PagaerAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("GetArguments", String.valueOf(getArguments()));
        if (getArguments() != null) {
            admin_id = getArguments().getInt(Constants.ADMIN_ID);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tab_admin_proposals, container, false);
        // Получаем ViewPager и устанавливаем в него адаптер
        ViewPager viewPager = rootView.findViewById(R.id.viewpager);
        adapter = new AdminMyProposals_PagaerAdapter(getFragmentManager(), rootView.getContext());
        setupViewPager(viewPager);
        // Передаём ViewPager в TabLayout
        TabLayout tabLayout = rootView.findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(1);
        Log.d(this.getTag(), "onCreateView");
        return rootView;
    }


    private void setupViewPager(ViewPager viewPager) {

        Bundle args = new Bundle();
        args.putInt(Constants.ADMIN_ID, admin_id);
        adapter.addFragment(new AdminMyProposals_NewProposalsFragment(), args);
        //adapter.addFragment(new AdminMyProposals_ApprovedProposalsFragment(), args);
        adapter.addFragment(new AdminMyProposals_ReturnProposalsFragment(), args);
        args.putString("admORuserId", "fk_admin");
        adapter.addFragment(new AdminMyProposals_UsersBooks(), args);
        viewPager.setAdapter(adapter);
    }
}
