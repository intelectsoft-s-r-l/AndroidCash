package md.intelectsoft.salesepos.SettingUtils;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import md.intelectsoft.salesepos.MainActivity;
import md.intelectsoft.salesepos.R;
import md.intelectsoft.salesepos.SplashActivity;
import md.intelectsoft.salesepos.StartedActivity;
import md.intelectsoft.salesepos.Utils.LocaleHelper;

import static md.intelectsoft.salesepos.BaseApplication.SharedPrefSettings;

/**
 * Created by Igor on 26.06.2020
 */

public class FragmentGeneralPage extends Fragment {
    ConstraintLayout csl_language, shiftSettings, shiftSettingDuring;
    ImageView img_lang;
    private Locale myLocale;

    TextView txtStateSettings,textDuringShift;

    SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootViewAdmin = inflater.inflate(R.layout.fragment_general_page_version0, container, false);

        csl_language = rootViewAdmin.findViewById(R.id.csl_language_change);
        shiftSettings = rootViewAdmin.findViewById(R.id.csl_shift_time);
        txtStateSettings = rootViewAdmin.findViewById(R.id.txt_time_shift_settings);
        img_lang = rootViewAdmin.findViewById(R.id.image_language_selected);
        shiftSettingDuring = rootViewAdmin.findViewById(R.id.csl_shift_time_end);
        textDuringShift = rootViewAdmin.findViewById(R.id.txt_time_end_shift_settings);

        sharedPreferences = getContext().getSharedPreferences(SharedPrefSettings, Context.MODE_PRIVATE);

        String[] languageList = {"English","Русский","Română"};
        String[] shiftSettingsList = {"5 minuts","10 minuts","15 minuts", "Disable"};
        String[] shiftSettingsHourList = {"4 ore","8 ore","16 ore", "24 ore", "Test(5 minute)"};

        long setShift = sharedPreferences.getLong("ShiftNotificationSettings",0);
        long setShiftDuring= sharedPreferences.getLong("ShiftDuringSettings",14400000);

        if (setShift == 300000)
            txtStateSettings.setText(shiftSettingsList[0]);
        else if (setShift == 600000)
            txtStateSettings.setText(shiftSettingsList[1]);
        else if (setShift == 900000)
            txtStateSettings.setText(shiftSettingsList[2]);
        else if (setShift == 0)
            txtStateSettings.setText(shiftSettingsList[3]);

        if(setShiftDuring == 14400000)
            textDuringShift.setText(shiftSettingsHourList[0]);
        else if (setShiftDuring == 28800000)
            textDuringShift.setText(shiftSettingsHourList[1]);
        else if (setShiftDuring == 57600000)
            textDuringShift.setText(shiftSettingsHourList[2]);
        else if (setShiftDuring == 86400000)
            textDuringShift.setText(shiftSettingsHourList[3]);
        else if (setShiftDuring == 300000)
            textDuringShift.setText(shiftSettingsHourList[4]);

        String lang = LocaleHelper.getLanguage(getContext());

        if(lang.equals("ru"))
            img_lang.setImageResource(R.drawable.russia);
        else if(lang.equals("ro"))
            img_lang.setImageResource(R.drawable.romania);
        else if(lang.equals("en"))
            img_lang.setImageResource(R.drawable.us);


        csl_language.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(getContext(), R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                    .setTitle(R.string.message_attention)
                    .setItems(languageList, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i){
                                case 0:{
                                    img_lang.setImageResource(R.drawable.us);
                                    dialogInterface.dismiss();
                                    LocaleHelper.setLocale(getContext(),"en");
                                }break;
                                case 1:{
                                    img_lang.setImageResource(R.drawable.russia);
//                                    changeLang("ru", dialogInterface);
                                    dialogInterface.dismiss();
                                    LocaleHelper.setLocale(getContext(),"ru");
                                }break;
                                case 2:{
                                    img_lang.setImageResource(R.drawable.romania);

                                    dialogInterface.dismiss();
                                    LocaleHelper.setLocale(getContext(),"ro");

                                }break;
                            }

                            Activity activity = MainActivity.getActivity();
                            Intent start = new Intent(getContext(), SplashActivity.class);
                            activity.finish();
                            activity.startActivity(start);

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
        shiftSettingDuring.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(getContext(), R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                    .setTitle(R.string.message_attention)
                    .setItems(shiftSettingsHourList, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i){
                                case 0:{
                                    textDuringShift.setText(shiftSettingsHourList[0]);
                                    sharedPreferences.edit().putLong("ShiftDuringSettings",14400000).apply();
                                    dialogInterface.dismiss();
                                }break;
                                case 1:{
                                    textDuringShift.setText(shiftSettingsHourList[1]);
                                    sharedPreferences.edit().putLong("ShiftDuringSettings",28800000).apply();
                                    dialogInterface.dismiss();
                                }break;
                                case 2:{
                                    textDuringShift.setText(shiftSettingsHourList[2]);
                                    sharedPreferences.edit().putLong("ShiftDuringSettings",57600000).apply();
                                    dialogInterface.dismiss();
                                }break;
                                case 3:{
                                    textDuringShift.setText(shiftSettingsHourList[3]);
                                    sharedPreferences.edit().putLong("ShiftDuringSettings",86400000).apply();
                                    dialogInterface.dismiss();
                                }break;
                                case 4:{
                                    textDuringShift.setText(shiftSettingsHourList[4]);
                                    sharedPreferences.edit().putLong("ShiftDuringSettings",300000).apply();
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

    public void changeLang(String lang, DialogInterface dialogInterface) {
        if (lang.equalsIgnoreCase(""))
            return;
        myLocale = new Locale(lang);
        Locale.setDefault(myLocale);
        Configuration config = new Configuration();
        config.locale = myLocale;
        getContext().getResources().updateConfiguration(config, getContext().getResources().getDisplayMetrics());
        dialogInterface.dismiss();
    }
}
