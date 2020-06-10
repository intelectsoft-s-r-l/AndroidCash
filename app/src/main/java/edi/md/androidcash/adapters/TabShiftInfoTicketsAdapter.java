package edi.md.androidcash.adapters;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import edi.md.androidcash.Fragments.FragmentInformationShift;
import edi.md.androidcash.Fragments.FragmentTicketsShift;
import edi.md.androidcash.RealmHelper.Shift;

/**
 * Created by Igor on 26.05.2020
 */

public class TabShiftInfoTicketsAdapter extends FragmentStatePagerAdapter {
    Shift shift;

    public TabShiftInfoTicketsAdapter(@NonNull FragmentManager fm , Shift shift) {
        super(fm);
        this.shift = shift;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position== 0){
            FragmentInformationShift informationShift = new FragmentInformationShift(shift);
            return informationShift;
        }
        else{
            FragmentTicketsShift ticketsShift = new FragmentTicketsShift(shift);
            return ticketsShift;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        switch (position) {
            case 0:
                return "Information";
            case 1:
                return "Tickets";

            default:
                return null;
        }
    }
}
