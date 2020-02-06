package edi.md.androidcash.DynamicTabs;

import android.os.Bundle;
import android.view.Gravity;
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
import edi.md.androidcash.adapters.GridQuickButtonAssortmentAdapter;
import io.realm.Realm;

/**
 * Created by Igor on 30.12.2019
 */

public class DynamicFragment extends Fragment {
    View view;

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_GROUP_ID = "section_number";
    private int sectionNumber;
    private ArrayList<String> groupId;

    List<AssortmentRealm> list;

    private GridQuickButtonAssortmentAdapter assortmentAdapter;

    private Realm mRealm;

    public DynamicFragment() { }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupId = getArguments() != null ? getArguments().getStringArrayList(ARG_GROUP_ID) : null;
        mRealm = Realm.getDefaultInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

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

        if(list.size() % 3 != 0.0){
            do{
                AssortmentRealm test = null;
                list.add(test);
            }
            while ((list.size() + 1) % 3 == 0);
        }

        if(list.size() < 12){
            while ((list.size() < 12)){
                AssortmentRealm test = null;
//                test.setName("null");
                list.add(test);
            }
        }
        int contHM = container.getMeasuredHeight();
        int heightButton = (contHM - 20) / 4;
        assortmentAdapter = new GridQuickButtonAssortmentAdapter(getContext(),R.layout.item_grid_quick_buttons,list,heightButton);

        gridView.setAdapter(assortmentAdapter);

        gridView.setOnItemClickListener((adapterView, view, i, l) -> {
            if(assortmentAdapter.getItem(i) != null){
                AssortmentRealm assortmentRealm = assortmentAdapter.getItem(i);
                MainActivity.addAssortmentToBill(assortmentRealm,1,"quickgroup",true);
                Toast toast = Toast.makeText(getActivity(), assortmentRealm.getName() , Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, 45);
                toast.show();
            }

        });

        return view;
    }

    public static DynamicFragment newInstance(int sectionNumber , ArrayList<QuickGroupRealm>  groupId) {
        QuickGroupRealm group = groupId.get(sectionNumber);

        ArrayList<String> ids = new ArrayList<>(group.getAssortmentId());

        DynamicFragment fragment = new DynamicFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_GROUP_ID, ids);
        fragment.setArguments(args);

        return fragment;
    }
}
