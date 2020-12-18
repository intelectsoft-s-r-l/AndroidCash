package md.intelectsoft.salesepos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import md.intelectsoft.salesepos.RealmHelper.History;
import md.intelectsoft.salesepos.RealmHelper.Shift;
import md.intelectsoft.salesepos.Utils.BaseEnum;
import md.intelectsoft.salesepos.adapters.ListShiftsRealmRCAdapter;
import md.intelectsoft.salesepos.adapters.TabShiftInfoTicketsAdapter;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static md.intelectsoft.salesepos.BaseApplication.SharedPrefSettings;
import static md.intelectsoft.salesepos.BaseApplication.SharedPrefWorkPlaceSettings;

public class ShiftsActivity extends AppCompatActivity {
    private DrawerLayout drawer;
    private RecyclerView recyclerView;
    private ConstraintLayout drawerConstraint;
    private ConstraintLayout csl_sales;
    private ConstraintLayout csl_shifts;
    private ConstraintLayout csl_reports;
    private ConstraintLayout csl_finReport;
    private ConstraintLayout csl_history;
    private ConstraintLayout csl_settings;

    private static ViewPager viewPager;
    private static TabLayout tabLayout;
    static TabShiftInfoTicketsAdapter shiftInfoTicketsAdapter;

    private Realm mRealm;

    ListShiftsRealmRCAdapter shiftsRealmAdapter;

    static TextView btnActionShift;

    SimpleDateFormat simpleDateFormatMD;
    TimeZone timeZoneMD;

    static Shift lastShift;
    TextView tvUserNameNav;
    TextView tvUserEmailNav;
    static TextView textInfo,textInfo2;

    static FragmentManager fManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout_shifts);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_shifts);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout_shifts);
        drawerConstraint = findViewById(R.id.nav_view_menu_shifts);
        recyclerView = findViewById(R.id.rc_list_shifts);
//        totalEntriesLog = findViewById(R.id.tv_total_shifts_entries);

        csl_sales = findViewById(R.id.csl_sales);
        csl_shifts = findViewById(R.id.csl_shift);
        csl_reports = findViewById(R.id.csl_reports);
        csl_finReport = findViewById(R.id.csl_fin_reports);
        csl_history = findViewById(R.id.csl_history);
        csl_settings = findViewById(R.id.csl_setting_nav);
        viewPager = findViewById(R.id.shift_container);
        tabLayout = findViewById(R.id.tab_information_tickets_shift);
        btnActionShift = findViewById(R.id.tv_open_shift);
        tvUserNameNav = findViewById(R.id.tv_user_name_nav);
        tvUserEmailNav = findViewById(R.id.tv_email_auth_user);
        textInfo = findViewById(R.id.textView58);
        textInfo2 = findViewById(R.id.textView31);

        mRealm = Realm.getDefaultInstance();

        simpleDateFormatMD = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        timeZoneMD = TimeZone.getTimeZone("Europe/Chisinau");
        simpleDateFormatMD.setTimeZone(timeZoneMD);

        fManager = getSupportFragmentManager();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        csl_sales.setOnClickListener(view -> {
            finish();
        });
        csl_shifts.setOnClickListener(view -> {
            drawer.closeDrawer(GravityCompat.START);
        });
        csl_reports.setOnClickListener(view -> {
            startActivityForResult(new Intent(this, ReportsActivity.class), BaseEnum.Activity_Reports);
            finish();
        });
        csl_finReport.setOnClickListener(view -> {
            startActivityForResult(new Intent(this, FinancialRepActivity.class), BaseEnum.Activity_FinRep);
            finish();
        });
        csl_history.setOnClickListener(view -> {
            startActivityForResult(new Intent(this, HistoryActivity.class),BaseEnum.Activity_History);
            finish();
        });
        csl_settings.setOnClickListener(v ->{
            startActivityForResult(new Intent(this, SettingsActivity.class),BaseEnum.Activity_Settings);
            finish();
        });

        showShiftList();

        btnActionShift.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                    .setTitle(getResources().getString(R.string.message_attention))
                    .setMessage(getResources().getString(R.string.message_open_shift))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.btn_yes), (dialogInterface, i) -> {
                        long opened_new_shift = new Date().getTime();
                        long setShiftDuring= getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getLong("ShiftDuringSettings",14400000);
                        long need_close = opened_new_shift + setShiftDuring;

                        Shift shiftEntry = new Shift();
                        shiftEntry.setName("SHF " + simpleDateFormatMD.format(opened_new_shift));
                        shiftEntry.setWorkPlaceId(getSharedPreferences(SharedPrefWorkPlaceSettings, MODE_PRIVATE).getString("WorkPlaceID", "null"));
                        shiftEntry.setWorkPlaceName(getSharedPreferences(SharedPrefWorkPlaceSettings, MODE_PRIVATE).getString("WorkPlaceName", "null"));
                        shiftEntry.setAuthor(BaseApplication.getInstance().getUserId());
                        shiftEntry.setAuthorName(BaseApplication.getInstance().getUser().getFullName());
                        shiftEntry.setStartDate(new Date().getTime());
                        shiftEntry.setClosed(false);
                        shiftEntry.setNeedClose(need_close);
                        shiftEntry.setId(UUID.randomUUID().toString());

                        lastShift = shiftEntry;

                        if(lastShift.isClosed()) {
                            setViewPagerVisibility(false);
                        }
                        else{
                            setViewPagerVisibility(true);
                        }

                        mRealm.executeTransaction(realm -> realm.insert(shiftEntry));

                        History history = new History();
                        history.setDate(new Date().getTime());
                        history.setMsg("Shift: " + shiftEntry.getName());
                        history.setType(BaseEnum.History_OpenShift);
                        mRealm.executeTransaction(realm -> realm.insert(history));

                        BaseApplication.getInstance().setShift(shiftEntry);
                        setViewPagerVisibility(true);
                    })
                    .setNegativeButton(getResources().getString(R.string.btn_no),((dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    }))

                    .show();
        });
    }

    private void showShiftList(){
        RealmResults<Shift> result = mRealm.where(Shift.class).equalTo("workPlaceId",getSharedPreferences(SharedPrefWorkPlaceSettings,MODE_PRIVATE).getString("WorkPlaceID","")).sort("startDate", Sort.DESCENDING).findAll();
        shiftsRealmAdapter = new ListShiftsRealmRCAdapter(result,true);
        recyclerView.setAdapter(shiftsRealmAdapter);

        if(!result.isEmpty()){
            recyclerView.setAdapter(shiftsRealmAdapter);

            lastShift = result.first();
            if(lastShift != null){
                if(lastShift.isClosed()) {
                    setViewPagerVisibility(false);
                }
                else{
                    setViewPagerVisibility(true);
                }
            }
            else
                setViewPagerVisibility(false);

        }
    }

    public static void setViewPagerVisibility(boolean visible){
        if(visible){
            btnActionShift.setVisibility(View.GONE);
            textInfo.setVisibility(View.GONE);
            textInfo2.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.VISIBLE);
            shiftInfoTicketsAdapter = new TabShiftInfoTicketsAdapter(fManager,lastShift);
            viewPager.setAdapter(shiftInfoTicketsAdapter);
            tabLayout.setupWithViewPager(viewPager);
        }
        else{
            viewPager.setVisibility(View.GONE);
            tabLayout.setVisibility(View.GONE);

            btnActionShift.setVisibility(View.VISIBLE);
            textInfo.setVisibility(View.VISIBLE);
            textInfo2.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
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
}
