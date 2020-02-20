package edi.md.androidcash.Fragments;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import edi.md.androidcash.DynamicTabs.ViewPagerDynamicTabs;
import edi.md.androidcash.MainActivity;
import edi.md.androidcash.R;
import edi.md.androidcash.RealmHelper.AssortmentRealm;
import edi.md.androidcash.RealmHelper.QuickGroupRealm;
import edi.md.androidcash.adapters.GridAssortmentListAdapter;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Igor on 10.02.2020
 */

public class FragmentAssortmentList extends Fragment {

    Realm mRealm;
    GridView grid;
    ImageButton btnHome;
    GridAssortmentListAdapter adapter;
    String guidItem = "00000000-0000-0000-0000-000000000000";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootViewAdmin = inflater.inflate(R.layout.fragment_assortment_list, container, false);

        mRealm = Realm.getDefaultInstance();

        grid = rootViewAdmin.findViewById(R.id.gv_assortment_list);
        btnHome = rootViewAdmin.findViewById(R.id.img_btn_home_assortment);

        homeAssortment(container);

        btnHome.setOnClickListener(v-> homeAssortment(container));

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapter.getItem(i) != null && !adapter.getItem(i).isFolder()){
                    AssortmentRealm assortmentRealm = adapter.getItem(i);
                    if(MainActivity.addItemsToOpenedBill(assortmentRealm,1,"quickgroup",true)){
                        Toast toast = Toast.makeText(getActivity(), "Added: " + assortmentRealm.getName() , Toast.LENGTH_SHORT);
//                        toast.setGravity(Gravity.TOP, 0, 45);
//                        toast.show();
                    }
                }
                else if(adapter.getItem(i) != null && adapter.getItem(i).isFolder()){
                    findAssortmentFromFolder(container,adapter.getItem(i).getId());
                }
            }
        });

        return rootViewAdmin;
    }

    private void homeAssortment (@Nullable ViewGroup container){
        guidItem = "00000000-0000-0000-0000-000000000000";

        ArrayList<AssortmentRealm> listArray = new ArrayList<>();

        mRealm.executeTransaction(realm -> {
            RealmResults<AssortmentRealm> result = realm.where(AssortmentRealm.class)
                    .equalTo("parentID",guidItem)
                    .and()
                    .sort("isFolder")
                    .findAll();

            if(!result.isEmpty()) {
                listArray.addAll(result);
            }
        });

        if(listArray.size() % 4 != 0.0){
            do{
                AssortmentRealm test = null;
                listArray.add(test);
            }
            while ((listArray.size() + 1) % 4 == 0);
        }

        if(listArray.size() < 16){
            while ((listArray.size() < 16)){
                AssortmentRealm test = null;
                listArray.add(test);
            }
        }
        int contHM = container.getMeasuredHeight();
        int heightButton = (contHM - 69) / 4;

        sortAssortmentList(listArray);

        adapter = new GridAssortmentListAdapter(getActivity(),R.layout.item_grid_quick_buttons,listArray,heightButton);
        grid.setAdapter(adapter);
    }

    private void findAssortmentFromFolder(@Nullable ViewGroup container,String id){
        ArrayList<AssortmentRealm> listArray = new ArrayList<>();

        mRealm.executeTransaction(realm -> {
            RealmResults<AssortmentRealm> result = realm.where(AssortmentRealm.class)
                    .equalTo("parentID",id)
                    .and()
                    .sort("isFolder")
                    .findAll();

            if(!result.isEmpty()) {
                listArray.addAll(result);
            }
        });

        if(listArray.size() % 5 != 0.0){
            do{
                AssortmentRealm test = null;
                listArray.add(test);
            }
            while ((listArray.size() + 1) % 5 == 0);
        }

        if(listArray.size() < 15){
            while ((listArray.size() < 15)){
                AssortmentRealm test = null;
                listArray.add(test);
            }
        }
        int contHM = container.getMeasuredHeight();
        int heightButton = (contHM - 20) / 3;

        sortAssortmentList(listArray);

        adapter = new GridAssortmentListAdapter(getActivity(),R.layout.item_grid_quick_buttons,listArray,heightButton);
        grid.setAdapter(adapter);
    }

    private static void sortAssortmentList(ArrayList<AssortmentRealm> listArray) {
        Collections.sort(listArray, new Comparator<AssortmentRealm>() {
            public int compare(AssortmentRealm o1, AssortmentRealm o2) {

                String xy1 = " ";
                String xy2 = " ";

                if(o1 != null)
                    xy1 = String.valueOf(o1.isFolder());
                if(o2 != null)
                    xy2 = String.valueOf(o2.isFolder());

                int sComp = xy2.compareTo(xy1);

                if (sComp != 0) {
                    return sComp;
                }
                else {
                    String x1 = " ";
                    String x2 = " ";
                    if (o1 != null) {
                        x1 = o1.getName();
                    }
                    if (o2 != null) {
                        x2 = o2.getName();
                    }

                    return x1.compareTo (x2);
                }
            }});
    }
}
