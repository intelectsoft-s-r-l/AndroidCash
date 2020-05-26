package edi.md.androidcash;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import edi.md.androidcash.RealmHelper.Bill;
import edi.md.androidcash.RealmHelper.History;
import edi.md.androidcash.RealmHelper.Shift;
import edi.md.androidcash.Utils.BaseEnum;
import edi.md.androidcash.adapters.ListShiftsRealmRCAdapter;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static edi.md.androidcash.BaseApplication.SharedPrefSettings;
import static edi.md.androidcash.BaseApplication.SharedPrefWorkPlaceSettings;
import static edi.md.androidcash.MainActivity.datecsFiscalDevice;
import static edi.md.androidcash.MainActivity.printZReport;

public class ShiftsActivity extends AppCompatActivity {
    private DrawerLayout drawer;
    private ConstraintLayout drawerConstraint;
    private RecyclerView recyclerView;
    private TextView totalEntriesLog;

    private ConstraintLayout csl_sales;
    private ConstraintLayout csl_shifts;
    private ConstraintLayout csl_tickets;
    private ConstraintLayout csl_reports;
    private ConstraintLayout csl_finReport;
    private ConstraintLayout csl_history;
    private ConstraintLayout csl_settings;

    private Realm mRealm;

    ListShiftsRealmRCAdapter shiftsRealmAdapter;

    TextView openedShift,openedShiftBy , countBillShift, closedShiftBy,closedShift , shiftName;
    MaterialButton btnActionShift;

    SimpleDateFormat simpleDateFormatMD;
    TimeZone timeZoneMD;

    boolean shiftIsClosed = true;
    Shift lastShift;
    TextView tvUserNameNav;
    TextView tvUserEmailNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout_shifts);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_shifts);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout_shifts);
        drawerConstraint = findViewById(R.id.nav_view_menu_shifts);
        recyclerView = findViewById(R.id.rc_list_shifts);
        totalEntriesLog = findViewById(R.id.tv_total_shifts_entries);

        csl_sales = findViewById(R.id.csl_sales);
        csl_shifts = findViewById(R.id.csl_shift);
        csl_reports = findViewById(R.id.csl_reports);
        csl_finReport = findViewById(R.id.csl_fin_reports);
        csl_history = findViewById(R.id.csl_history);
        csl_settings = findViewById(R.id.csl_setting_nav);

        openedShift = findViewById(R.id.tv_opened_shift);
        openedShiftBy = findViewById(R.id.tv_current_shift_opened_by);
        countBillShift = findViewById(R.id.tv_current_shift_bills_counter);
        closedShiftBy = findViewById(R.id.tv_current_shift_closed_by);
        closedShift = findViewById(R.id.tv_closed_shift_entry);
        shiftName = findViewById(R.id.tv_current_shift_name);
        btnActionShift = findViewById(R.id.btn_open_closed_current_shift);
        tvUserNameNav = findViewById(R.id.tv_user_name_nav);
        tvUserEmailNav = findViewById(R.id.tv_email_auth_user);

        mRealm = Realm.getDefaultInstance();

        simpleDateFormatMD = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        timeZoneMD = TimeZone.getTimeZone("Europe/Chisinau");
        simpleDateFormatMD.setTimeZone(timeZoneMD);

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
            if(shiftIsClosed){
                new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                        .setTitle("Attention!")
                        .setMessage("Do you want open shift?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            long opened_new_shift = new Date().getTime();
                            long need_close = opened_new_shift + 28800000;

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
                            showLastShiftInfo(lastShift);

                            mRealm.executeTransaction(realm -> realm.insert(shiftEntry));

                            History history = new History();
                            history.setDate(new Date().getTime());
                            history.setMsg("Shift: " + shiftEntry.getName());
                            history.setType(BaseEnum.History_OpenShift);
                            mRealm.executeTransaction(realm -> realm.insert(history));


                            BaseApplication.getInstance().setShift(shiftEntry);
                            shiftIsClosed = false;
                            btnActionShift.setText("Close this shift");
                        })
                        .setNegativeButton("No",((dialogInterface, i) -> {
                            dialogInterface.dismiss();
                        }))

                        .show();
            }
            else{
                new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                        .setTitle("Attention!")
                        .setMessage("Do you want close this shift?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            closeShift();
                        })
                        .setNegativeButton("No",((dialogInterface, i) -> {
                            dialogInterface.dismiss();
                        }))
                        .show();
            }
        });
    }

    private void showLastShiftInfo(Shift shift){
        openedShift.setText(simpleDateFormatMD.format(shift.getStartDate()));
        openedShiftBy.setText(lastShift.getAuthorName());
        if(lastShift.getEndDate() == 0){
            closedShift.setText("");
        }
        else{
            closedShift.setText(simpleDateFormatMD.format(shift.getEndDate()));
        }
        shiftName.setText(shift.getName());
        closedShiftBy.setText(shift.getClosedByName());
        countBillShift.setText(String.valueOf(shift.getBillCounter()));
        if(shift.isClosed()) {
            shiftIsClosed = true;
            btnActionShift.setText("Open new shift");
        }
        else{
            shiftIsClosed = false;
            btnActionShift.setText("Closed this shift");
        }
    }

    private void showShiftList(){
        RealmResults<Shift> result = mRealm.where(Shift.class).sort("startDate", Sort.DESCENDING).findAll();
        shiftsRealmAdapter = new ListShiftsRealmRCAdapter(result,true);
        recyclerView.setAdapter(shiftsRealmAdapter);

        if(!result.isEmpty()){

            recyclerView.setAdapter(shiftsRealmAdapter);
            totalEntriesLog.setText(String.valueOf(result.size()));

            lastShift = result.first();
            showLastShiftInfo(lastShift);
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

    private void closeShift(){
        RealmResults<Bill> billEntryResult = mRealm.where(Bill.class)
                .equalTo("shiftId",lastShift.getId())
                .and()
                .equalTo("state",0)
                .findAll();
        if(!billEntryResult.isEmpty()){
            new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                    .setTitle("Attention!")
                    .setMessage("You cannot close a shift while there are open bills!\nYou have left " + billEntryResult.size() + " open bills.")
                    .setCancelable(false)
                    .setPositiveButton("OKAY", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    })
                    .show();
        }
        else{
            long close = new Date().getTime();

            mRealm.executeTransaction(realm -> {
                Shift shift = realm.where(Shift.class).equalTo("id",lastShift.getId()).findFirst();
                if(shift != null){
                    shift.setClosedBy(BaseApplication.getInstance().getUserId());
                    shift.setEndDate(close);
                    shift.setClosed(true);
                    shift.setClosedByName(BaseApplication.getInstance().getUser().getFullName());
                    shift.setSended(false);
                    showLastShiftInfo(shift);
                }
            });

            BaseApplication.getInstance().setShift(null);

            History history = new History();
            history.setDate(new Date().getTime());
            history.setMsg("Shift: " + lastShift.getName());
            history.setType(BaseEnum.History_ClosedShift);
            mRealm.executeTransaction(realm -> realm.insert(history));

            int workFisc = getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getInt("ModeFiscalWork",BaseEnum.FISCAL_SERVICE);

            if(workFisc == BaseEnum.FISCAL_DEVICE) {
                if (datecsFiscalDevice != null && datecsFiscalDevice.isConnectedDeviceV2())
                    printZReport();
            }
            if(workFisc == BaseEnum.FISCAL_SERVICE){
                String uri = getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getString("FiscalServiceAddress","0.0.0.0:1111");
                MainActivity.printZReportFiscalService(uri);
            }
            shiftIsClosed = true;
            btnActionShift.setText("Open new shift");
        }
    }
}
