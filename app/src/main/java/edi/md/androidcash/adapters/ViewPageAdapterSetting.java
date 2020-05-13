package edi.md.androidcash.adapters;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import edi.md.androidcash.SettingUtils.FragmentSyncPage;

public class ViewPageAdapterSetting extends FragmentStatePagerAdapter {
    public ViewPageAdapterSetting(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        FragmentSyncPage syncPage= new FragmentSyncPage();
        return syncPage;
    }

    @Override
    public int getCount() {
        return 1;
    }
}
