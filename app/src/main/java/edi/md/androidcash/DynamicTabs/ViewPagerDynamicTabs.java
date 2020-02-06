package edi.md.androidcash.DynamicTabs;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import edi.md.androidcash.RealmHelper.QuickGroupRealm;

/**
 * Created by Igor on 28.01.2020
 */

public class ViewPagerDynamicTabs extends FragmentStatePagerAdapter {
    private int noOfItems;
    private ArrayList<QuickGroupRealm>  groupID;

    public ViewPagerDynamicTabs(FragmentManager fm, int noOfItems , ArrayList<QuickGroupRealm>  id) {
        super(fm);
        this.noOfItems = noOfItems;
        this.groupID = id;
    }

    @Override
    public Fragment getItem(int position) {
        return DynamicFragment.newInstance(position, groupID);
    }

    @Override
    public int getCount() {
        return noOfItems;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return groupID.get(position).getGroupName();
    }
}
