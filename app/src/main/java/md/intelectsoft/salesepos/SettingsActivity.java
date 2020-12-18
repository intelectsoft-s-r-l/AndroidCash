package md.intelectsoft.salesepos;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import md.intelectsoft.salesepos.SettingUtils.FragmentCompanyPage;
import md.intelectsoft.salesepos.SettingUtils.FragmentFiscalPage;
import md.intelectsoft.salesepos.SettingUtils.FragmentGeneralPage;
import md.intelectsoft.salesepos.SettingUtils.FragmentSyncPage;
import md.intelectsoft.salesepos.SettingUtils.FragmentUpdatePage;
import md.intelectsoft.salesepos.Utils.BaseEnum;
import md.intelectsoft.salesepos.Utils.Rfc2898DerivesBytes;

public class SettingsActivity extends AppCompatActivity {
    TextView startMsg;
    ProgressDialog pgH;

    FragmentManager fgManager;
    FrameLayout frmContainer;

    private static DrawerLayout drawer;
    private static ConstraintLayout drawerConstraint;

    private ConstraintLayout csl_company, csl_sync, csl_fiscal, csl_sales,csl_shifts,csl_reports,csl_finReport,csl_general,csl_history,csl_settings, csl_update;

    private boolean firstItemSelected = false;
    TextView tvUserNameNav;
    TextView tvUserEmailNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.drawer_layout_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);

        pgH = new ProgressDialog(this);
        drawer = findViewById(R.id.drawer_layout_settings);
        drawerConstraint = findViewById(R.id.nav_view_menu_settings);
        csl_company = findViewById(R.id.csl_company);
        csl_sync = findViewById(R.id.csl_sync);
        csl_fiscal = findViewById(R.id.csl_fiscal_service);
        csl_update = findViewById(R.id.csl_update);
        csl_general = findViewById(R.id.csl_general);

        csl_sales = findViewById(R.id.csl_sales);
        csl_shifts = findViewById(R.id.csl_shift);
        csl_reports = findViewById(R.id.csl_reports);
        csl_finReport = findViewById(R.id.csl_fin_reports);
        csl_history = findViewById(R.id.csl_history);
        csl_settings = findViewById(R.id.csl_setting_nav);

        tvUserNameNav = findViewById(R.id.tv_user_name_nav);
        startMsg = findViewById(R.id.txt_start_settings_msg);
        frmContainer = findViewById(R.id.container_setting);
        tvUserEmailNav = findViewById(R.id.tv_email_auth_user);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        fgManager = getSupportFragmentManager();

        csl_company.setOnClickListener(view -> {
            FragmentCompanyPage companyPage = new FragmentCompanyPage();
            replaceFragment(companyPage);
        });
        csl_sync.setOnClickListener(view -> {
            FragmentSyncPage fragmentSyncPage = new FragmentSyncPage();
            replaceFragment(fragmentSyncPage);
        });
        csl_fiscal.setOnClickListener(view -> {
            FragmentFiscalPage fiscalPage = new FragmentFiscalPage();
            replaceFragment(fiscalPage);
        });
        csl_update.setOnClickListener(view -> {
            FragmentUpdatePage updatePage = new FragmentUpdatePage();
            replaceFragment(updatePage);
        });
        csl_general.setOnClickListener(view -> {
            FragmentGeneralPage generalPage = new FragmentGeneralPage();
            replaceFragment(generalPage);
        });


        csl_sales.setOnClickListener(v ->{
            finish();
        });
        csl_shifts.setOnClickListener(view -> {
            startActivityForResult(new Intent(this, ShiftsActivity.class), BaseEnum.Activity_Shifts);
            finish();
        });
        csl_reports.setOnClickListener(view -> {
            startActivityForResult(new Intent(this, ReportsActivity.class), BaseEnum.Activity_Reports);
            finish();
        });
        csl_finReport.setOnClickListener(view -> {
            startActivityForResult(new Intent(this, FinancialRepActivity.class),BaseEnum.Activity_FinRep);
            finish();
        });
        csl_history.setOnClickListener(view -> {
            startActivityForResult(new Intent(this, HistoryActivity.class),BaseEnum.Activity_History);
            finish();
        });
        csl_settings.setOnClickListener(v ->{
            drawer.closeDrawer(GravityCompat.START);
        });
    }

    private void replaceFragment(Fragment fragment){
        if(startMsg.getVisibility() == View.GONE){
            fgManager.beginTransaction().replace(R.id.container_setting,fragment).commit();
        }
        else{
            frmContainer.setVisibility(View.VISIBLE);
            startMsg.setVisibility(View.GONE);
            fgManager.beginTransaction().replace(R.id.container_setting,fragment).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        tvUserNameNav.setText(BaseApplication.getInstance().getUser().getFirstName() + " " +  BaseApplication.getInstance().getUser().getLastName());
        tvUserEmailNav.setText(BaseApplication.getInstance().getUser().getEmail());
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
