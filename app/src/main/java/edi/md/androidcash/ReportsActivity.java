package edi.md.androidcash;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.datecs.fiscalprinter.SDK.model.DatecsFiscalDevice;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReport;

import edi.md.androidcash.NetworkUtils.FiscalServiceResult.PrintReportXResult;
import edi.md.androidcash.NetworkUtils.FiscalServiceResult.PrintReportZResult;
import edi.md.androidcash.NetworkUtils.FiscalServiceResult.XResponse;
import edi.md.androidcash.NetworkUtils.FiscalServiceResult.ZResponse;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.ApiUtils;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.CommandServices;
import edi.md.androidcash.SettingUtils.Reports;
import edi.md.androidcash.Utils.BaseEnum;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static edi.md.androidcash.BaseApplication.SharedPrefFiscalService;
import static edi.md.androidcash.BaseApplication.SharedPrefSettings;

public class ReportsActivity extends AppCompatActivity {
    private DrawerLayout drawer;
    private ConstraintLayout drawerConstraint;

    DatecsFiscalDevice myFiscalDevice;

    private ConstraintLayout csl_sales;
    private ConstraintLayout csl_shifts;
    private ConstraintLayout csl_tickets;
    private ConstraintLayout csl_reports;
    private ConstraintLayout csl_finReport;
    private ConstraintLayout csl_history;
    private ConstraintLayout csl_settings;

    Button X_report,Z_report;
    private ProgressDialog progress;
    TextView x_errore,z_errore;

    int fiscalManager = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout_reports);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_reports);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout_reports);
        drawerConstraint = findViewById(R.id.nav_view_menu_reports);

        csl_sales = findViewById(R.id.csl_sales);
        csl_finReport = findViewById(R.id.csl_fin_reports);
        csl_history = findViewById(R.id.csl_history);
        csl_settings = findViewById(R.id.csl_setting_nav);

        X_report = findViewById(R.id.btn_x_report_settings);
        Z_report = findViewById(R.id.btn_z_report_settings);
        x_errore = findViewById(R.id.txt_error_x_report_settings);
        z_errore = findViewById(R.id.txt_error_z_report_settings);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        fiscalManager = getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getInt("ModeFiscalWork", BaseEnum.NONE_SELECTED_FISCAL_MODE);

        csl_sales.setOnClickListener(view -> {
            finish();
        });
        csl_history.setOnClickListener(view -> {
            startActivityForResult(new Intent(this, HistoryActivity.class), BaseEnum.Activity_History);
            finish();
        });
        csl_finReport.setOnClickListener(view -> {
            startActivityForResult(new Intent(this, FinancialRepActivity.class), BaseEnum.Activity_FinRep);
            finish();
        });

        X_report.setOnClickListener(v -> {
            if(fiscalManager == BaseEnum.FISCAL_DEVICE){
                x_errore.setText("");
                progress = new ProgressDialog(ReportsActivity.this);
                progress.setCancelable(true);
                progress.setTitle("X report is starting !!!");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.setCancelable(false);
                progress.show();

                final int[] reportNummber = new int[1];
                final cmdReport.ReportSummary reportSummary = new cmdReport.ReportSummary();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // do the thing that takes a long time
                        try {

                            cmdReport cmd = new cmdReport();
                            reportNummber[0] = cmd.PrintXreport(reportSummary);
                        } catch (Exception e) {
                            e.printStackTrace();
                            x_errore.setText(e.getMessage());
                        } finally {
                            progress.dismiss();
                            if (reportNummber[0] == -1){
                                foundMessage.obtainMessage(101).sendToTarget();
                            }
                        }
                        ReportsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (reportNummber[0] != -1) {
                                    foundMessage.obtainMessage(111,reportNummber[0]).sendToTarget();
                                    DialogZXReportsSummary dialogSummary = new DialogZXReportsSummary(ReportsActivity.this, reportSummary);
                                    dialogSummary.show();
                                    DisplayMetrics metrics = new DisplayMetrics(); //get metrics of screen
                                    getWindowManager().getDefaultDisplay().getMetrics(metrics);
                                    int width = (int) (metrics.widthPixels * 0.5); //set width to 50% of display
                                    int height = (int) (metrics.heightPixels * 0.9); //set height to 90% of display
                                    dialogSummary.getWindow().setLayout(width, height); //set layout
                                    progress.dismiss();
                                }
                            }
                        });
                    }
                }).start();
            }
            if(fiscalManager == BaseEnum.FISCAL_SERVICE){
                x_errore.setText("");
                String ip = getSharedPreferences(SharedPrefFiscalService, MODE_PRIVATE).getString("IpAdressFiscalService",null);
                String port = getSharedPreferences(SharedPrefFiscalService, MODE_PRIVATE).getString("PortFiscalService",null);
                if(ip != null && port != null) {
                    String uri = ip + ":" + port;

                    CommandServices commandServices = ApiUtils.commandFPService(uri);
                    Call<XResponse> responseCall = commandServices.printXReport();

                    responseCall.enqueue(new Callback<XResponse>() {
                        @Override
                        public void onResponse(Call<XResponse> call, Response<XResponse> response) {
                            XResponse xResponse = response.body();
                            if(xResponse != null){
                                PrintReportXResult reportXResult = xResponse.getPrintReportXResult();
                                int errorCode = reportXResult.getErrorCode();
                                if(errorCode == 0)
                                    x_errore.setText("Raportul X a fost imprimat!");
                            }
                        }

                        @Override
                        public void onFailure(Call<XResponse> call, Throwable t) {
                            x_errore.setText(t.getMessage());
                        }
                    });
                }
            }
        });
        Z_report.setOnClickListener(v -> {
            if(fiscalManager == BaseEnum.FISCAL_DEVICE){
                z_errore.setText("");
                progress = new ProgressDialog(ReportsActivity.this);
                progress.setCancelable(true);
                progress.setTitle("Z report is working !!!");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.setCancelable(false);
                progress.show();
                final int[] reportNumber = {0};
                final cmdReport.ReportSummary reportSummary = new cmdReport.ReportSummary();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // do the thing that takes a long time
                        try {
                            cmdReport cmd = new cmdReport();
                            reportNumber[0] = cmd.PrintZreport(reportSummary);
                        } catch (Exception e) {
                            e.printStackTrace();
                            z_errore.setText(e.getMessage());
                        } finally {
                            progress.dismiss();
                            if (reportNumber[0] == -1){
                                foundMessage.obtainMessage(202).sendToTarget();
                            }
                        }
                        ReportsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (reportNumber[0] != -1) {
                                    foundMessage.obtainMessage(222,reportNumber[0]).sendToTarget();
                                    DialogZXReportsSummary dialogSummary = new DialogZXReportsSummary(ReportsActivity.this, reportSummary);
                                    dialogSummary.show();
                                    DisplayMetrics metrics = new DisplayMetrics(); //get metrics of screen
                                    getWindowManager().getDefaultDisplay().getMetrics(metrics);
                                    int width = (int) (metrics.widthPixels * 0.5); //set width to 50% of display
                                    int height = (int) (metrics.heightPixels * 0.9); //set height to 90% of display
                                    dialogSummary.getWindow().setLayout(width, height); //set layout
                                    progress.dismiss();
                                }
                            }
                        });

                    }
                }).start();
            }
            if(fiscalManager == BaseEnum.FISCAL_SERVICE){
                z_errore.setText("");
                String ip = getSharedPreferences(SharedPrefFiscalService, MODE_PRIVATE).getString("IpAdressFiscalService",null);
                String port = getSharedPreferences(SharedPrefFiscalService, MODE_PRIVATE).getString("PortFiscalService",null);
                if(ip != null && port != null) {
                    String uri = ip + ":" + port;

                    CommandServices commandServices = ApiUtils.commandFPService(uri);
                    Call<ZResponse> responseCall = commandServices.printZReport();

                    responseCall.enqueue(new Callback<ZResponse>() {
                        @Override
                        public void onResponse(Call<ZResponse> call, Response<ZResponse> response) {
                            ZResponse zResponse = response.body();
                            if(zResponse != null){
                                PrintReportZResult reportZResult = zResponse.getPrintReportZResult();
                                int errorCode = reportZResult.getErrorCode();
                                if(errorCode == 0)
                                    z_errore.setText("Raportul Z a fost imprimat!");
                            }
                        }

                        @Override
                        public void onFailure(Call<ZResponse> call, Throwable t) {
                            z_errore.setText(t.getMessage());
                        }
                    });
                }
            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
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

    private final Handler foundMessage = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 101){
                x_errore.setText("Nu este raspuns de la aparat!");
            }
            else if (msg.what == 202){
                z_errore.setText("Nu este raspuns de la aparat!");
            }
            else if (msg.what == 222){
                z_errore.setText("Raportul Z nr: " + msg.obj.toString());
            }
            else if (msg.what == 111){
                x_errore.setText("Raportul X din Z cu nr:" + msg.obj.toString());
            }
        }
    };

    //DIALOG  ZXReportsSummary
    public class DialogZXReportsSummary extends Dialog implements View.OnClickListener {
        private final cmdReport.ReportSummary summary;

        private DialogZXReportsSummary(Activity a, cmdReport.ReportSummary summary) {
            super(a);
            this.summary = summary;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_xz_summary);

            TextView tvTaxA = findViewById(R.id.tvTaxA);
            TextView tvTaxB = findViewById(R.id.tvTaxB);
            TextView tvTaxC = findViewById(R.id.tvTaxC);
            TextView tvTaxD = findViewById(R.id.tvTaxD);
            TextView tvTaxE = findViewById(R.id.tvTaxE);
            TextView tvTaxF = findViewById(R.id.tvTaxF);
            TextView tvTaxG = findViewById(R.id.tvTaxG);
            TextView tvTaxH = findViewById(R.id.tvTaxH);
            Button btnOk = findViewById(R.id.btn_dialogOk);
            btnOk.setOnClickListener(this);

            tvTaxA.setText(String.valueOf(summary.totalA));
            tvTaxB.setText(String.valueOf(summary.totalB));
            tvTaxC.setText(String.valueOf(summary.totalC));
            tvTaxD.setText(String.valueOf(summary.totalD));
            tvTaxE.setText(String.valueOf(summary.totalE));
            tvTaxF.setText(String.valueOf(summary.totalF));
            tvTaxG.setText(String.valueOf(summary.totalG));
            tvTaxH.setText(String.valueOf(summary.totalH));

        }

        //DIALOG ON CLICK OK
        @Override
        public void onClick(View v) {
            dismiss();

        }
    }
}
