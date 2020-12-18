package md.intelectsoft.salesepos.adapters;


import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import md.intelectsoft.salesepos.Fragments.FragmentAssortmentList;
import md.intelectsoft.salesepos.Fragments.FragmentBills;
import md.intelectsoft.salesepos.Fragments.FragmentQuickButtons;
import md.intelectsoft.salesepos.R;

public class TabQuickMenuAdapter extends FragmentStatePagerAdapter {
    Context context;
    public TabQuickMenuAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
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
                return context.getString(R.string.group_quick_buttons);
            case 1:
                return context.getString(R.string.group_assortment);
            case 2:
                return context.getString(R.string.group_bills);
            default:
                return null;
        }
    }
}
