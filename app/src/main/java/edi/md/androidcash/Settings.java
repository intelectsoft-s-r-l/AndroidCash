package edi.md.androidcash;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import java.util.Objects;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import edi.md.androidcash.SettingUtils.Equipment.EquipmentPageAdapter;
import edi.md.androidcash.SettingUtils.Reports;
import edi.md.androidcash.SettingUtils.Returns;
import edi.md.androidcash.SettingUtils.Preference.SettingPageAdapter;
import edi.md.androidcash.SettingUtils.ShiftManage;
import edi.md.androidcash.Utils.Rfc2898DerivesBytes;

public class Settings extends AppCompatActivity {
    TextView btn_settings,btn_reports,btn_shifts,tv_header_text,btn_returns, btn_equipments;
    ProgressDialog pgH;

    FragmentManager fgManager;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home : {
                finish();
            }break;
        }
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        pgH=new ProgressDialog(this);
        btn_settings = findViewById(R.id.btn_settings_in_settings);
        btn_reports = findViewById(R.id.btn_reports_settings);
        btn_shifts = findViewById(R.id.btn_shift_manage_settings);
        btn_returns = findViewById(R.id.btn_returns_settings);
        btn_equipments = findViewById(R.id.btn_equipment_settings);
        tv_header_text = findViewById(R.id.txt_header_settings);


        fgManager = getSupportFragmentManager();
        SettingPageAdapter settingPages = new SettingPageAdapter();
        fgManager.beginTransaction().replace(R.id.container_setting,settingPages).commit();

        tv_header_text.setText("Настройки");

        btn_settings.setOnClickListener(v -> {
            SettingPageAdapter settingPage = new SettingPageAdapter();
            fgManager.beginTransaction().replace(R.id.container_setting,settingPage).commit();
            tv_header_text.setText("Настройки");
        });

        btn_reports.setOnClickListener(v -> {
            Reports reports = new Reports();
            fgManager.beginTransaction().replace(R.id.container_setting,reports).commit();
            tv_header_text.setText("Reports");
        });

        btn_shifts.setOnClickListener(v -> {
            ShiftManage shiftManage = new ShiftManage();
            fgManager.beginTransaction().replace(R.id.container_setting,shiftManage).commit();
            tv_header_text.setText("Shift manage");
        });

        btn_returns.setOnClickListener(v -> {
            Returns returns = new Returns();
            fgManager.beginTransaction().replace(R.id.container_setting,returns).commit();
            tv_header_text.setText("Returns");
        });

        btn_equipments.setOnClickListener(v ->{
            EquipmentPageAdapter equipmentPageAdapter = new EquipmentPageAdapter();
            fgManager.beginTransaction().replace(R.id.container_setting,equipmentPageAdapter).commit();
            tv_header_text.setText("Equipments");
        });




//        Get_ASL.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {

//        });
//        btn_test.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//              String return_pass  = GetSHA1HashUserPassword("This is the code for UserPass","Admin202827");
//              String newPas = return_pass.replace("\n","");
//              int inter = 0;
//              btn_test.setText(newPas);
//
//            }
//        });
//
//        btn_stg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mViewPager.setAdapter(mSectionsPagerAdapter);
//            }
//        });

    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
    public static String GetSHA1Hash(String keyHint,String message) {
        byte[] hintBytes = ("This is strong key").getBytes();
        String form = "";
        try {

            Rfc2898DerivesBytes test = new Rfc2898DerivesBytes(keyHint,hintBytes,1000);
            byte[] secretKey = test.GetBytes(18);

            SecretKeySpec signingKey = new SecretKeySpec(secretKey, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] bytes = mac.doFinal(message.getBytes());
            form = Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return form;
    }
}
