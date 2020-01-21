package edi.md.androidcash.adapters;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import edi.md.androidcash.SettingUtils.Equipment.FiscalDeviceEquip;
import edi.md.androidcash.SettingUtils.Equipment.Scales;

public class ViewPageAdapterEquipment extends FragmentStatePagerAdapter {
    public ViewPageAdapterEquipment(FragmentManager fm) {
        super(fm);
    }
    @Override
    public Fragment getItem(int position) {
        if (position == 0){
            FiscalDeviceEquip fiscalDevice= new FiscalDeviceEquip();
            return fiscalDevice;
        }
        else{
            Scales scales = new Scales();
            return scales;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
