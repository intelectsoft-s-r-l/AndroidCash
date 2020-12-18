package md.intelectsoft.salesepos.Fragments;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import md.intelectsoft.salesepos.MainActivity;
import md.intelectsoft.salesepos.R;
import md.intelectsoft.salesepos.RealmHelper.AssortmentRealm;
import md.intelectsoft.salesepos.adapters.AssortmentListGridAdapter;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Igor on 10.02.2020
 */

public class FragmentAssortmentList extends Fragment {

    static Realm mRealm;
    static GridView grid;
    ImageButton btnHome;
    static AssortmentListGridAdapter adapter;
//    String guidItem = "00000000-0000-0000-0000-000000000000";
    LinearLayout layout_buttons;
    static ViewGroup container;
    static Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup containerGroup, @Nullable Bundle savedInstanceState) {
        View rootViewAdmin = inflater.inflate(R.layout.fragment_assortment_list, containerGroup, false);
        container =  containerGroup;
        mRealm = Realm.getDefaultInstance();

        grid = rootViewAdmin.findViewById(R.id.gv_assortment_list);
        btnHome = rootViewAdmin.findViewById(R.id.img_btn_home_assortment);
        layout_buttons = rootViewAdmin.findViewById(R.id.ll_tree_way);

        context = getContext();

        homeAssortment();

        btnHome.setOnClickListener(v-> {
            ViewGroup parent  = (ViewGroup) v.getParent();
            int count = parent.getChildCount();

            for (int i=count-1; i>0; i--){
                parent.removeViewAt(i);
            }
            homeAssortment();
        });

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("ResourceAsColor")
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
                    AssortmentRealm assortmentRealm = adapter.getItem(i);
//                    guidItem = assortmentRealm.getId();

                    Button button = new Button(getActivity());
                    button.setText(assortmentRealm.getName());
                    button.setTag(assortmentRealm);
                    button.setOnClickListener(butons_);
                    button.setBackgroundColor(Color.rgb(0,138,124)); //color primary
                    button.setTextColor(Color.WHITE);
                    button.setPadding(5,0,5,0);

                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    lp.setMargins(5,-5,0,-5);
                    layout_buttons.addView(button, lp);

                    findAssortmentFromFolder(adapter.getItem(i).getId());
                }
            }
        });

        return rootViewAdmin;
    }
    View.OnClickListener butons_ = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AssortmentRealm assortmentEntry = (AssortmentRealm)view.getTag();
            ViewGroup parent  = (ViewGroup) view.getParent();
            int count = parent.getChildCount();

            for (int i = count-1; i > 0; i--){
                Button vi = (Button) parent.getChildAt(i);
                AssortmentRealm entry = (AssortmentRealm)vi.getTag();
                if(!entry.getId().equals(assortmentEntry.getId())){
                    parent.removeViewAt(i);
                }
                else if(entry.getId().equals(assortmentEntry.getId())){
                    break;
                }
            }
            findAssortmentFromFolder(assortmentEntry.getId());
        }
    };

    public static void searchAssortiment(String text){
        ArrayList<AssortmentRealm> listArray = new ArrayList<>();

        String filteringLower = "*" + text.toLowerCase() + "*";
        String filteringUpper = "*" + text.toUpperCase() + "*";

        mRealm.executeTransaction(realm -> {
            RealmResults<AssortmentRealm> result = realm.where(AssortmentRealm.class)
                    .like("name",filteringLower).or()
                    .like("marking",filteringLower).or()
                    .like("code",filteringLower).or()
                    .like("barcodes.bar",filteringLower).or()
                    .like("name",filteringUpper).or()
                    .like("code",filteringUpper).or()
                    .like("marking",filteringUpper).or()
                    .like("barcodes.bar",filteringUpper)
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
        int heightButton = (contHM - 74) / 4;

        sortAssortmentList(listArray);

        adapter = new AssortmentListGridAdapter(context,R.layout.item_grid_quick_buttons,listArray,heightButton);
        grid.setAdapter(adapter);
    }

    public static void homeAssortment (){

        ArrayList<AssortmentRealm> listArray = new ArrayList<>();

        mRealm.executeTransaction(realm -> {
            RealmResults<AssortmentRealm> result = realm.where(AssortmentRealm.class)
                    .equalTo("parentID","00000000-0000-0000-0000-000000000000")
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
        int heightButton = (contHM - 74) / 4;

        sortAssortmentList(listArray);

        adapter = new AssortmentListGridAdapter(context,R.layout.item_grid_quick_buttons,listArray,heightButton);
        grid.setAdapter(adapter);
    }

    private void findAssortmentFromFolder(String id){
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
        int heightButton = (contHM - 75) / 4;

        sortAssortmentList(listArray);

        adapter = new AssortmentListGridAdapter(context,R.layout.item_grid_quick_buttons,listArray,heightButton);
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
