package com.example.user.library;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ProposalActivity extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.proposal_activity, container, false);
        ListView listView = rootView.findViewById(R.id.listView);
        final String[] proposalList = getResources().getStringArray(R.array.proposal_list_item);
        // используем адаптер данных
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, proposalList);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position,
                                    long id) {
                // Создадим новый фрагмент
                Fragment fragment = null;
                Class fragmentClass = null;
                // получим идентификатор выбранного пункта меню
                switch (position) {
                    case 0:
                        Log.d("onOptionsItemSelected", String.valueOf(id));
                        // Выполняем переход на ProposalActivity:
                        fragmentClass = CreateProposalActivity.class;
                        break;
                    case 1:
                        fragmentClass = ActiveProposalActivity.class;
                        break;
                }


                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("Error", e.getMessage());

                }

                // Вставляем фрагмент, заменяя текущий фрагмент
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
                // Выделяем выбранный пункт меню в шторке

                Log.d("!position", String.valueOf(position));
                Log.d("!id", String.valueOf(id));
            }
        });
        return rootView;
    }



}
