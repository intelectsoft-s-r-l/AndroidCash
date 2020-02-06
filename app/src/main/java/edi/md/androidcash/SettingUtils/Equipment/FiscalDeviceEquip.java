package edi.md.androidcash.SettingUtils.Equipment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import edi.md.androidcash.R;
import edi.md.androidcash.SettingUtils.Equipment.FiscalMode.FiscalDevice;
import edi.md.androidcash.SettingUtils.Equipment.FiscalMode.FiscalService;

import static android.content.Context.MODE_PRIVATE;
import static edi.md.androidcash.BaseApplication.SharedPrefSettings;

public class FiscalDeviceEquip extends Fragment {

    Spinner selectionMode;
    ArrayAdapter<String> adapter_spiner_userSettings;
    FragmentManager fgManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootViewAdmin = inflater.inflate(R.layout.fragment_equipment_fiscaldevice, container, false);

        selectionMode = rootViewAdmin.findViewById(R.id.spinner_mode_select_fiscal);

        fgManager = getActivity().getSupportFragmentManager();

        String[] settings_userSpinner = {"NoNe","Aparat fiscal", "Serviciu fiscal"};
        adapter_spiner_userSettings = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, settings_userSpinner);
        adapter_spiner_userSettings.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectionMode.setAdapter(adapter_spiner_userSettings);
        int position = getActivity().getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getInt("ModeFiscalWork",0);
        selectionMode.setSelection(position);

        selectionMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if(position == 0){
                    getActivity().getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).edit().putInt("ModeFiscalWork",position).apply();
                    fgManager.getFragments().clear();
                }
                else if(position == 1){
                    getActivity().getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).edit().putInt("ModeFiscalWork",position).apply();
                    FiscalDevice fiscalDevice = new FiscalDevice();
                    fgManager.beginTransaction().replace(R.id.container_fiscal_detail_mode,fiscalDevice).commit();
                }
                else if(position == 2){
                    getActivity().getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).edit().putInt("ModeFiscalWork",position).apply();
                    FiscalService service = new FiscalService();
                    fgManager.beginTransaction().replace(R.id.container_fiscal_detail_mode,service).commit();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return rootViewAdmin;
    }
}
