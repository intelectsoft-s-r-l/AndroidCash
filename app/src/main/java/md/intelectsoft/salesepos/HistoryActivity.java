package md.intelectsoft.salesepos;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import md.intelectsoft.salesepos.RealmHelper.History;
import md.intelectsoft.salesepos.Utils.BaseEnum;
import md.intelectsoft.salesepos.adapters.HistoryRealmRCAdapter;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class HistoryActivity extends AppCompatActivity {
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
    TextView tvUserNameNav;
    TextView tvUserEmailNav;

    HistoryRealmRCAdapter historyRealmAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout_history);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_history);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout_history);
        drawerConstraint = findViewById(R.id.nav_view_menu_history);
        recyclerView = findViewById(R.id.rc_list_history);
        totalEntriesLog = findViewById(R.id.tv_total_log_entries);

        csl_sales = findViewById(R.id.csl_sales);
        csl_shifts = findViewById(R.id.csl_shift);
        csl_reports = findViewById(R.id.csl_reports);
        csl_finReport = findViewById(R.id.csl_fin_reports);
        csl_history = findViewById(R.id.csl_history);
        csl_settings = findViewById(R.id.csl_setting_nav);
        tvUserNameNav = findViewById(R.id.tv_user_name_nav);
        tvUserEmailNav = findViewById(R.id.tv_email_auth_user);

        mRealm = Realm.getDefaultInstance();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        csl_sales.setOnClickListener(view -> {
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
            startActivityForResult(new Intent(this, FinancialRepActivity.class), BaseEnum.Activity_FinRep);
            finish();
        });
        csl_history.setOnClickListener(view -> {
            drawer.closeDrawer(GravityCompat.START);
        });
        csl_settings.setOnClickListener(v ->{
            startActivityForResult(new Intent(this, SettingsActivity.class),BaseEnum.Activity_Settings);
            finish();
        });

        RealmResults<History> result = mRealm.where(History.class).sort("date",Sort.DESCENDING).findAll();

        if(!result.isEmpty()){
            historyRealmAdapter = new HistoryRealmRCAdapter(result,true);
            recyclerView.setAdapter(historyRealmAdapter);
            totalEntriesLog.setText(String.valueOf(result.size()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        tvUserNameNav.setText(BaseApplication.getInstance().getUser().getFirstName() + " " +  BaseApplication.getInstance().getUser().getLastName());
        tvUserEmailNav.setText(BaseApplication.getInstance().getUser().getEmail());
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_history, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_delete: {
                new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                        .setTitle("Attention!")
                        .setMessage("Are you sure you want to clear the action history?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            mRealm.delete(History.class);
                        })
                        .setNegativeButton("No",(dialogInterface, i) -> {
                            dialogInterface.dismiss();
                        })
                        .show();
            }break;
            case R.id.action_share: {
                //TODO set action share
            }
        }
        return super.onOptionsItemSelected(item);
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
}
