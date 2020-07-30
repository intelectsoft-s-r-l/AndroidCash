package edi.md.androidcash.SettingUtils;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

import edi.md.androidcash.R;

import static edi.md.androidcash.BaseApplication.SharedPrefSettings;

/**
 * Created by Igor on 26.06.2020
 */

public class FragmentGeneralPage extends Fragment {
    ConstraintLayout csl_language, shiftSettings;
    ImageView img_lang;
    private Locale myLocale;

    TextView txtStateSettings;

    SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootViewAdmin = inflater.inflate(R.layout.fragment_general_page_version0, container, false);

        csl_language = rootViewAdmin.findViewById(R.id.csl_language_change);
        shiftSettings = rootViewAdmin.findViewById(R.id.csl_shift_time);
        txtStateSettings = rootViewAdmin.findViewById(R.id.txt_time_shift_settings);
        img_lang = rootViewAdmin.findViewById(R.id.image_language_selected);
        sharedPreferences = getContext().getSharedPreferences(SharedPrefSettings, Context.MODE_PRIVATE);

        String[] languageList = {"English","Русский","Română"};
        String[] shiftSettingsList = {"5 minuts","10 minuts","15 minuts", "Disable"};

        int langInt = sharedPreferences.getInt("Language",2);
        long setShift = sharedPreferences.getLong("ShiftNotificationSettings",0);

        if (setShift == 300000) {
            txtStateSettings.setText(shiftSettingsList[0]);
        } else if (setShift == 600000) {
            txtStateSettings.setText(shiftSettingsList[1]);
        } else if (setShift == 900000) {
            txtStateSettings.setText(shiftSettingsList[2]);
        } else if (setShift == 0) {
            txtStateSettings.setText(shiftSettingsList[3]);
        }


        switch(langInt){
            case 0:{
                img_lang.setImageResource(R.drawable.us);
                changeLang("en");
            }break;
            case 1:{
                img_lang.setImageResource(R.drawable.russia);
                changeLang("ru");
            }break;
            case 2:{
                img_lang.setImageResource(R.drawable.romania);
                changeLang("ro");
            }break;
        }

        csl_language.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(getContext(), R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                    .setTitle(R.string.message_attention)
                    .setItems(languageList, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(getContext(), "Selected item " + i + ".Language selected: " + languageList[i], Toast.LENGTH_SHORT).show();

                            sharedPreferences.edit().putInt("Language",i).apply();

                            switch (i){
                                case 0:{
                                    img_lang.setImageResource(R.drawable.us);
                                    changeLang("en");
                                    dialogInterface.dismiss();
                                }break;
                                case 1:{
                                    img_lang.setImageResource(R.drawable.russia);
                                    changeLang("ru");
                                    dialogInterface.dismiss();
                                }break;
                                case 2:{
                                    img_lang.setImageResource(R.drawable.romania);
                                    changeLang("ro");
                                    dialogInterface.dismiss();
                                }break;
                            }
                        }
                    })
                    .setCancelable(false)
                    .setPositiveButton(R.string.btn_cancel, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        shiftSettings.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(getContext(), R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                    .setTitle(R.string.message_attention)
                    .setItems(shiftSettingsList, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(getContext(), "Selected item " + i + ".Settings selected: " + shiftSettingsList[i], Toast.LENGTH_SHORT).show();

                            switch (i){
                                case 0:{
                                    txtStateSettings.setText(shiftSettingsList[0]);
                                    sharedPreferences.edit().putLong("ShiftNotificationSettings",300000).apply();
                                    dialogInterface.dismiss();
                                }break;
                                case 1:{
                                    txtStateSettings.setText(shiftSettingsList[1]);
                                    sharedPreferences.edit().putLong("ShiftNotificationSettings",600000).apply();
                                    dialogInterface.dismiss();
                                }break;
                                case 2:{
                                    txtStateSettings.setText(shiftSettingsList[2]);
                                    sharedPreferences.edit().putLong("ShiftNotificationSettings",900000).apply();
                                    dialogInterface.dismiss();
                                }break;
                                case 3:{
                                    txtStateSettings.setText(shiftSettingsList[3]);
                                    sharedPreferences.edit().putLong("ShiftNotificationSettings",0).apply();
                                    dialogInterface.dismiss();
                                }break;
                            }
                        }
                    })
                    .setCancelable(false)
                    .setPositiveButton(R.string.btn_cancel, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        return rootViewAdmin;
    }

    public void changeLang(String lang) {
        if (lang.equalsIgnoreCase(""))
            return;
        myLocale = new Locale(lang);
        Locale.setDefault(myLocale);
        Configuration config = new Configuration();
        config.locale = myLocale;
        getContext().getResources().updateConfiguration(config, getContext().getResources().getDisplayMetrics());

    }
}
