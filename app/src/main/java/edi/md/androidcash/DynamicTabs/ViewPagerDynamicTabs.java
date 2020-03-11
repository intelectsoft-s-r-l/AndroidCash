package edi.md.androidcash.DynamicTabs;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

import edi.md.androidcash.Fragments.FragmentAssortmentList;
import edi.md.androidcash.RealmHelper.QuickGroupRealm;

/**
 * Created by Igor on 28.01.2020
 */

public class ViewPagerDynamicTabs extends FragmentStatePagerAdapter {
    private int noOfItems;
    private ArrayList<QuickGroupRealm>  groupID;
    FragmentManager fragmentManager;
    ArrayList<Fragment> oPooledFragments = new ArrayList<>();

    public ViewPagerDynamicTabs(FragmentManager fm, int noOfItems , ArrayList<QuickGroupRealm>  id) {
        super(fm,noOfItems);
        this.fragmentManager = fm;
        this.noOfItems = noOfItems;
        this.groupID = id;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fr = DynamicFragment.newInstance(position, groupID);
        oPooledFragments.add(fr);
        return fr;
    }

    @Override
    public int getCount() {
        return noOfItems;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return groupID.get(position).getGroupName();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        Fragment oFragment=(Fragment)object;
        oPooledFragments = new ArrayList<>(fragmentManager.getFragments());
        if(oPooledFragments.contains(oFragment))
            return POSITION_NONE;
        else
            return POSITION_UNCHANGED;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


}
