package md.intelectsoft.salesepos.Fragments;
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
import com.google.android.material.tabs.TabLayout;

import md.intelectsoft.salesepos.BaseApplication;
import md.intelectsoft.salesepos.MainActivity;
import md.intelectsoft.salesepos.R;
import md.intelectsoft.salesepos.RealmHelper.Bill;
import md.intelectsoft.salesepos.RealmHelper.Shift;
import md.intelectsoft.salesepos.adapters.BillListRealmRCAdapter;
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
        mRealm = Realm.getDefaultInstance();
        mRealm.executeTransaction(realm -> {
                results[0] = realm.where(Bill.class).equalTo("shiftId", finalId).and().equalTo("state", 0).findAll();
            });

        BillListRealmRCAdapter adapterBillList = new BillListRealmRCAdapter(results[0],true);
        recyclerView.setAdapter(adapterBillList);

        if(MainActivity.tabLayout.getTabAt(2) != null){
            TabLayout.Tab bills = MainActivity.tabLayout.getTabAt(2);
            if(bills != null){
                BadgeDrawable badge = bills.getOrCreateBadge();
                badge.setVisible(true);
                badge.setNumber(results[0].size());

            }
        }
    }
}
