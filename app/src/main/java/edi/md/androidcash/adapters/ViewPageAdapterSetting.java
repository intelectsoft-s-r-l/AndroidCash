package edi.md.androidcash.adapters;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import edi.md.androidcash.SettingUtils.Preference.GeneralPageSet;
import edi.md.androidcash.SettingUtils.Preference.SyncPageSet;

public class ViewPageAdapterSetting extends FragmentStatePagerAdapter {
    public ViewPageAdapterSetting(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position== 0){
            GeneralPageSet generalPage= new GeneralPageSet();
            return generalPage;
        }
        else{
            SyncPageSet syncPage= new SyncPageSet();
            return syncPage;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
