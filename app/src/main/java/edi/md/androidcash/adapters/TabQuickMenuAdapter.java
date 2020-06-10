package edi.md.androidcash.adapters;


import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import edi.md.androidcash.Fragments.FragmentAssortmentList;
import edi.md.androidcash.Fragments.FragmentBills;
import edi.md.androidcash.Fragments.FragmentQuickButtons;

public class TabQuickMenuAdapter extends FragmentStatePagerAdapter {

    public TabQuickMenuAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position== 0){
            FragmentQuickButtons generalPage = new FragmentQuickButtons();
            return generalPage;
        }
        else if (position == 1){
            FragmentAssortmentList assortmentList = new FragmentAssortmentList();
            return assortmentList;
        }
        else{
            FragmentBills assortmentList = new FragmentBills();
            return assortmentList;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        switch (position) {
            case 0:
                return "Quick Buttons";
            case 1:
                return "Assortment";
            case 2:
                return "Bills";
            default:
                return null;
        }
    }
}
