package edi.md.androidcash.DynamicTabs;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

import edi.md.androidcash.RealmHelper.QuickGroupRealm;

/**
 * Created by Igor on 28.01.2020
 */

public class ViewPagerDynamicTabs extends FragmentPagerAdapter {
    private int noOfItems;
    private ArrayList<QuickGroupRealm>  groupID;
    FragmentManager fragmentManager;
    int i = 0;

    public ViewPagerDynamicTabs(FragmentManager fm, int noOfItems , ArrayList<QuickGroupRealm>  id) {
        super(fm,noOfItems);
        this.fragmentManager = fm;
        this.noOfItems = noOfItems;
        this.groupID = id;
    }

    @NonNull
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
