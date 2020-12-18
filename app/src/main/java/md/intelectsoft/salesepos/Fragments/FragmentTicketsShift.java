package md.intelectsoft.salesepos.Fragments;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import md.intelectsoft.salesepos.R;
import md.intelectsoft.salesepos.RealmHelper.Bill;
import md.intelectsoft.salesepos.RealmHelper.Shift;
import md.intelectsoft.salesepos.adapters.ListTicketsRealmRCAdapter;
import io.realm.RealmResults;

/**
 * Created by Igor on 26.05.2020
 */

public class FragmentTicketsShift extends Fragment {
    static RealmResults<Bill> billRealmResults;
    static Shift shift;

    static RecyclerView rcTicketsList;
    static ListTicketsRealmRCAdapter adapter;
    static Context context;


    public FragmentTicketsShift(Shift shift) {
        this.shift = shift;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootViewAdmin = inflater.inflate(R.layout.fragment_shift_tickets, container, false);
        rcTicketsList = rootViewAdmin.findViewById(R.id.ll_list_of_tickets);
        context = getContext();

        updateInfoDisplay();
        return rootViewAdmin;
    }

    public static void setListBills(RealmResults<Bill> listBills){
        billRealmResults = listBills;
        updateInfoDisplay();
    }

    private static void updateInfoDisplay(){
        adapter = new ListTicketsRealmRCAdapter(context,billRealmResults,shift.getName(), shift.getStartDate(),true);
        if(rcTicketsList != null)
            rcTicketsList.setAdapter(adapter);
    }
}
