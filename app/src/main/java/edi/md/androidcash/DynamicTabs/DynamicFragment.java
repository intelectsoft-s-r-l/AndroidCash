package edi.md.androidcash.DynamicTabs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import edi.md.androidcash.MainActivity;
import edi.md.androidcash.R;
import edi.md.androidcash.RealmHelper.AssortmentRealm;
import edi.md.androidcash.RealmHelper.QuickGroupRealm;
import edi.md.androidcash.adapters.QuickButtonGridAdapter;
import io.realm.Realm;

/**
 * Created by Igor on 30.12.2019
 */

public class DynamicFragment extends Fragment {
    View view;
    private static final String ARG_GROUP_ID = "section_number";
    private int sectionNumber;
    private String groupName;
    private ArrayList<String> groupId;

    List<AssortmentRealm> list;

    private QuickButtonGridAdapter assortmentAdapter;

    private Realm mRealm;

    public static int height = 0;

    public DynamicFragment(int section_number, String name) {
        sectionNumber = section_number;
        groupName = name;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupId = getArguments() != null ? getArguments().getStringArrayList(ARG_GROUP_ID) : null;
        mRealm = Realm.getDefaultInstance();

        Log.d("QuickButtons","DynamicFragment onCreate " + sectionNumber + " " + groupName);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if(view == null){
            Log.d("QuickButtons","DynamicFragment onCreateView " + sectionNumber + " " + groupName);

            view = inflater.inflate(R.layout.fragment_dynamic_tab_content, container, false);
            GridView gridView = (GridView) view.findViewById(R.id.gv);

            list = new ArrayList<>();
            for(String id: groupId){
                mRealm.executeTransaction(realm ->{
                    AssortmentRealm assortmentRealm = realm.where(AssortmentRealm.class).equalTo("id",id).findFirst();
                    if(assortmentRealm != null){
                        AssortmentRealm realmAss = realm.copyFromRealm(assortmentRealm);
                        list.add(realmAss);
                    }
                });
            }

            if(list.size() % 4 != 0.0){
                do{
                    AssortmentRealm test = null;
                    list.add(test);
                }
                while ((list.size() + 1) % 4 == 0);
            }

            if(list.size() < 16){
                while ((list.size() < 16)){
                    AssortmentRealm test = null;
                    list.add(test);
                }
            }
            int contHM = container.getMeasuredHeight();
            int heightButton = (contHM - 8) / 4;
            height = heightButton;
            assortmentAdapter = new QuickButtonGridAdapter(getContext(),R.layout.item_grid_quick_buttons,list,heightButton);

            gridView.setAdapter(assortmentAdapter);

            gridView.setOnItemClickListener((adapterView, view, i, l) -> {
                if(assortmentAdapter.getItem(i) != null){
                    AssortmentRealm assortmentRealm = assortmentAdapter.getItem(i);
                    if(MainActivity.addItemsToOpenedBill(assortmentRealm,1,"quickgroup",true)){
                        Toast toast = Toast.makeText(getActivity(), "Added: " + assortmentRealm.getName() , Toast.LENGTH_SHORT);
//                        toast.setGravity(Gravity.TOP, 0, 45);
//                        toast.show();
                    }
                }

            });
        }
        return view;
    }

    public static DynamicFragment newInstance(int sectionNumber , ArrayList<QuickGroupRealm>  groupId) {
        QuickGroupRealm group = groupId.get(sectionNumber);

        ArrayList<String> ids = new ArrayList<>(group.getAssortmentId());

        DynamicFragment fragment = new DynamicFragment(sectionNumber , group.getGroupName());
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_GROUP_ID, ids);
        fragment.setArguments(args);

        return fragment;
    }
}
