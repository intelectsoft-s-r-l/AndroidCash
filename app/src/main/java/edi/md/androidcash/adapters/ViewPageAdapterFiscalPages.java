package edi.md.androidcash.adapters;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import edi.md.androidcash.SettingUtils.Equipment.FiscalMode.FiscalDevice;
import edi.md.androidcash.SettingUtils.Equipment.FiscalMode.FiscalService;

public class ViewPageAdapterFiscalPages extends FragmentStatePagerAdapter {
    public ViewPageAdapterFiscalPages(FragmentManager fm) {
        super(fm);
    }
    @Override
    public Fragment getItem(int position) {
        if (position == 0){
            FiscalDevice fiscalDevice= new FiscalDevice();
            return fiscalDevice;
        }
        else{
             FiscalService scales = new FiscalService();
            return scales;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
