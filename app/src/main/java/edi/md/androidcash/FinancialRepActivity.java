package edi.md.androidcash;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.datecs.fiscalprinter.SDK.model.DatecsFiscalDevice;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReceipt;
import com.google.android.material.button.MaterialButton;

import edi.md.androidcash.RealmHelper.Shift;
import edi.md.androidcash.Utils.BaseEnum;
import io.realm.Realm;

import static edi.md.androidcash.BaseApplication.SharedPrefSettings;

public class FinancialRepActivity extends AppCompatActivity {
    private DrawerLayout drawer;
    private ConstraintLayout drawerConstraint;

    ConstraintLayout cash_in,cash_out,cash_collection;

    DatecsFiscalDevice myFiscalDevice;

    private ConstraintLayout csl_sales;
    private ConstraintLayout csl_shifts;
    private ConstraintLayout csl_tickets;
    private ConstraintLayout csl_reports;
    private ConstraintLayout csl_finReport;
    private ConstraintLayout csl_history;
    private ConstraintLayout csl_settings;
    TextView tvUserNameNav;
    TextView tvUserEmailNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout_finrep);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_finrep);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout_finrep);
        drawerConstraint = findViewById(R.id.nav_view_menu_finrep);

        cash_in = findViewById(R.id.cl_cash_in);
        cash_out = findViewById(R.id.cl_cash_out);
        cash_collection = findViewById(R.id.cl_incasatia);

        csl_sales = findViewById(R.id.csl_sales);
        csl_shifts = findViewById(R.id.csl_shift);
        csl_reports = findViewById(R.id.csl_reports);
        csl_finReport = findViewById(R.id.csl_fin_reports);
        csl_history = findViewById(R.id.csl_history);
        csl_settings = findViewById(R.id.csl_setting_nav);
        tvUserNameNav = findViewById(R.id.tv_user_name_nav);
        tvUserEmailNav = findViewById(R.id.tv_email_auth_user);

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
            drawer.closeDrawer(GravityCompat.START);
        });
        csl_history.setOnClickListener(view -> {
            startActivityForResult(new Intent(this, HistoryActivity.class), BaseEnum.Activity_History);
            finish();
        });
        csl_settings.setOnClickListener(v ->{
            startActivityForResult(new Intent(this, SettingsActivity.class),BaseEnum.Activity_Settings);
            finish();
        });


        cash_in.setOnClickListener(v -> {
            Shift shiftEntry = BaseApplication.getInstance().getShift();
            myFiscalDevice = BaseApplication.getInstance().getMyFiscalDevice();
            if(shiftEntry != null ){
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_cash_in, null);

                final AlertDialog addPosition = new AlertDialog.Builder(FinancialRepActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
                addPosition.setCancelable(false);
                addPosition.setView(dialogView);
                addPosition.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

                final TextView amount = dialogView.findViewById(R.id.et_input_data);
                ImageButton btn_Cancel = dialogView.findViewById(R.id.btn_cancel_cashin);
                MaterialButton btn_ok = dialogView.findViewById(R.id.btn_ok_cashin);
                MaterialButton btn_clear = dialogView.findViewById(R.id.btn_cashin_clear);
                MaterialButton btn_point = dialogView.findViewById(R.id.btn_add_point);

                MaterialButton number_1 = dialogView.findViewById(R.id.btn_cashin_1);
                MaterialButton number_2 = dialogView.findViewById(R.id.btn_cashin_2);
                MaterialButton number_3 = dialogView.findViewById(R.id.btn_cashin_3);
                MaterialButton number_4 = dialogView.findViewById(R.id.btn_cashin_4);
                MaterialButton number_5 = dialogView.findViewById(R.id.btn_cashin_5);
                MaterialButton number_6 = dialogView.findViewById(R.id.btn_cashin_6);
                MaterialButton number_7 = dialogView.findViewById(R.id.btn_cashin_7);
                MaterialButton number_8 = dialogView.findViewById(R.id.btn_cashin_8);
                MaterialButton number_9 = dialogView.findViewById(R.id.btn_cashin_9);
                MaterialButton number_0 = dialogView.findViewById(R.id.btn_cashin_0);

                number_1.setOnClickListener(v113 -> amount.append("1"));
                number_2.setOnClickListener(v112 -> amount.append("2"));
                number_3.setOnClickListener(v111 -> amount.append("3"));
                number_4.setOnClickListener(v110 -> amount.append("4"));
                number_5.setOnClickListener(v19 -> amount.append("5"));
                number_6.setOnClickListener(v18 -> amount.append("6"));
                number_7.setOnClickListener(v17 -> amount.append("7"));
                number_8.setOnClickListener(v16 -> amount.append("8"));
                number_9.setOnClickListener(v15 -> amount.append("9"));
                number_0.setOnClickListener(v13 -> amount.append("0"));

                btn_point.setOnClickListener(v12 -> {
                    String test = amount.getText().toString();
                    boolean contains = false;
                    for (int i = 0; i < test.length(); i++) {
                        String chars = String.valueOf(test.charAt(i));
                        if (chars.equals(".")) {
                            contains = true;
                        }
                    }
                    if (!contains) {
                        amount.append(".");
                    }
                });

                btn_clear.setOnClickListener(v1 -> amount.setText(""));
                btn_ok.setOnClickListener(v14 -> {

                    addPosition.dismiss();
                    int fiscalManager = getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getInt("ModeFiscalWork", BaseEnum.FISCAL_SERVICE);

                    if(fiscalManager == BaseEnum.FISCAL_DEVICE){
                        if(myFiscalDevice != null && myFiscalDevice.isConnectedDeviceV2()){
                            try {
                                Double newAmount = Double.valueOf(amount.getText().toString());
                                cash_IN_OUT(String.format("%.2f", newAmount),newAmount);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else
                            postMessage("Фискальный апарат не доступен!");
                    }
                    else if(fiscalManager == BaseEnum.FISCAL_SERVICE){
                        //TODO service create method

                    }


                });

                btn_Cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addPosition.dismiss();
                    }
                });

                addPosition.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                addPosition.show();

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int displayWidth = displayMetrics.widthPixels;
//        int displayHeight = displayMetrics.heightPixels;
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(addPosition.getWindow().getAttributes());
                int dialogWindowWidth = (int) (displayWidth * 0.4f);
//        int dialogWindowHeight = (int) (displayHeight * 0.5f);
                layoutParams.width = dialogWindowWidth;
                layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                addPosition.getWindow().setAttributes(layoutParams);

            }
            else
                postMessage("Смена закрыта или не действительна!");
        });
        cash_out.setOnClickListener(v -> {
            Shift shiftEntry = BaseApplication.getInstance().getShift();
            myFiscalDevice = BaseApplication.getInstance().getMyFiscalDevice();
            if(shiftEntry != null){
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_cash_out, null);

                final AlertDialog addPosition = new AlertDialog.Builder(FinancialRepActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
                addPosition.setCancelable(false);
                addPosition.setView(dialogView);
                addPosition.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

                final TextView amount = dialogView.findViewById(R.id.et_input_data);
                ImageButton btn_Cancel = dialogView.findViewById(R.id.btn_cancel_cashout);
                MaterialButton btn_ok = dialogView.findViewById(R.id.btn_ok_cashout);
                MaterialButton btn_clear = dialogView.findViewById(R.id.btn_cashout_clear);
                MaterialButton btn_point = dialogView.findViewById(R.id.btn_add_point);

                MaterialButton number_1 = dialogView.findViewById(R.id.btn_cashout_1);
                MaterialButton number_2 = dialogView.findViewById(R.id.btn_cashout_2);
                MaterialButton number_3 = dialogView.findViewById(R.id.btn_cashout_3);
                MaterialButton number_4 = dialogView.findViewById(R.id.btn_cashout_4);
                MaterialButton number_5 = dialogView.findViewById(R.id.btn_cashout_5);
                MaterialButton number_6 = dialogView.findViewById(R.id.btn_cashout_6);
                MaterialButton number_7 = dialogView.findViewById(R.id.btn_cashout_7);
                MaterialButton number_8 = dialogView.findViewById(R.id.btn_cashout_8);
                MaterialButton number_9 = dialogView.findViewById(R.id.btn_cashout_9);
                MaterialButton number_0 = dialogView.findViewById(R.id.btn_cashout_0);

                number_1.setOnClickListener(v114 -> amount.append("1"));
                number_2.setOnClickListener(v115 -> amount.append("2"));
                number_3.setOnClickListener(v116 -> amount.append("3"));
                number_4.setOnClickListener(v117 -> amount.append("4"));
                number_5.setOnClickListener(v118 -> amount.append("5"));
                number_6.setOnClickListener(v119 -> amount.append("6"));
                number_7.setOnClickListener(v120 -> amount.append("7"));
                number_8.setOnClickListener(v121 -> amount.append("8"));
                number_9.setOnClickListener(v122 -> amount.append("9"));
                number_0.setOnClickListener(v123 -> amount.append("0"));
                btn_point.setOnClickListener(v124 -> {
                    String test = amount.getText().toString();
                    boolean contains = false;
                    for (int i = 0; i < test.length(); i++) {
                        String chars = String.valueOf(test.charAt(i));
                        if (chars.equals(".")) {
                            contains = true;
                        }
                    }
                    if (!contains) {
                        amount.append(".");
                    }
                });

                btn_clear.setOnClickListener(v125 -> amount.setText(""));
                btn_ok.setOnClickListener(v126 -> {
                    addPosition.dismiss();
                    if(myFiscalDevice != null && myFiscalDevice.isConnectedDeviceV2()){
                        try {
                            Double newAmount = -1 * Double.valueOf(amount.getText().toString());
                            if(cash_IN_OUT(String.format("%.2f", newAmount),newAmount)){
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else
                        postMessage("Фискальный апарат не доступен!");


                });

                btn_Cancel.setOnClickListener(v127 -> addPosition.dismiss());
                addPosition.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                addPosition.show();

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int displayWidth = displayMetrics.widthPixels;
//        int displayHeight = displayMetrics.heightPixels;
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(addPosition.getWindow().getAttributes());
                int dialogWindowWidth = (int) (displayWidth * 0.4f);
//        int dialogWindowHeight = (int) (displayHeight * 0.5f);
                layoutParams.width = dialogWindowWidth;
                layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                addPosition.getWindow().setAttributes(layoutParams);
            }
            else
                postMessage("Смена закрыта или не действительна!");
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        tvUserNameNav.setText(BaseApplication.getInstance().getUser().getFirstName() + " " +  BaseApplication.getInstance().getUser().getLastName());
        tvUserEmailNav.setText(BaseApplication.getInstance().getUser().getEmail());
    }
    private void postMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
    //Cash in / cash out
    private boolean cash_IN_OUT(String valueOfCurrency,double value) {
        try {
            // cashInSafe Holds result of operation:
            //0-cashSum
            //1-cashIn
            //2-cashOut
            Double[] cashInSafe = new Double[3];

            valueOfCurrency = valueOfCurrency.replace(",",".");
            cashInSafe = new cmdReceipt().cashInCashOut(Double.valueOf(valueOfCurrency), false);

//            AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
//            builder1.setMessage("Cash in safe:" + cashInSafe[0] + "\n\r" +
//                    " Sum of Cash IN:" + cashInSafe[1] + "\n\r" +
//                    " Sum of Cash OUT:" + cashInSafe[2]);
//            builder1.setCancelable(false);
//            builder1.setPositiveButton(R.string.okButtonText, new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int id) {
//                    dialog.cancel();
//                }
//            });
//            AlertDialog alert11 = builder1.create();
//            alert11.show();

            if (cashInSafe[0] != null){
                Realm.getDefaultInstance().executeTransaction(realm -> {
                    Shift shift = realm.where(Shift.class).equalTo("id",BaseApplication.getInstance().getShift().getId()).findFirst();
                    if(shift != null){
                        if(value > 0){
                            shift.setCashIn(shift.getCashIn() + value);
                            BaseApplication.getInstance().getShift().setCashIn(shift.getCashIn());

                        }
                        else {
                            double absValue = Math.abs(value);
                            shift.setCashOut(shift.getCashOut() + absValue);
                            BaseApplication.getInstance().getShift().setCashOut(shift.getCashOut());
                        }
                    }
                });

                return true;
            }
            else
                return false;
        } catch (Exception e) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(FinancialRepActivity.this);
            builder1.setMessage(e.getMessage());
            builder1.setCancelable(false);
            builder1.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            AlertDialog alert11 = builder1.create();
            alert11.show();

//            e.printStackTrace();
            return false;
        }

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
