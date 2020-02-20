package edi.md.androidcash.Fragments;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.BadgeDrawable;

import edi.md.androidcash.BaseApplication;
import edi.md.androidcash.MainActivity;
import edi.md.androidcash.R;
import edi.md.androidcash.RealmHelper.Bill;
import edi.md.androidcash.RealmHelper.Shift;
import edi.md.androidcash.adapters.CustomRCBillListRealmAdapter;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Igor on 11.02.2020
 */

public class FragmentBills extends Fragment {

    static Realm mRealm;
    static RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootViewAdmin = inflater.inflate(R.layout.fragment_bills, container, false);
        Log.d("Bills","onCreateView");

        mRealm = Realm.getDefaultInstance();
        recyclerView = rootViewAdmin.findViewById(R.id.rc_list_bill);
        RecyclerView.LayoutManager layout = new LinearLayoutManager(getActivity());
        layout.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(layout);


        showBillList();

        return rootViewAdmin;
    }

    public static void showBillList(){
        final Shift[] shift = {BaseApplication.getInstance().getShift()};
        final RealmResults<Bill>[] results = new RealmResults[]{null};
        String id = "";
        if(shift[0] != null){
           id = shift[0].getId();
        }
        String finalId = id;
        mRealm.executeTransaction(realm -> {
                results[0] = mRealm.where(Bill.class).equalTo("shiftId", finalId).and().equalTo("state", 0).findAll();
            });

        CustomRCBillListRealmAdapter adapterBillList = new CustomRCBillListRealmAdapter(results[0],true);

        recyclerView.setAdapter(adapterBillList);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("Bills","onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Bills","onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("Bills","onPause");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("Bills","onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Bills","onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("Bills","onDetach");
    }
}
