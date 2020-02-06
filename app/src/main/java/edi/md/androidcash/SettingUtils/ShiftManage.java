package edi.md.androidcash.SettingUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.datecs.fiscalprinter.SDK.model.DatecsFiscalDevice;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReceipt;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import edi.md.androidcash.BaseApplication;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.ApiUtils;
import edi.md.androidcash.NetworkUtils.FiscalServiceResult.PrintReportZResult;
import edi.md.androidcash.NetworkUtils.FiscalServiceResult.ZResponse;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.CommandServices;
import edi.md.androidcash.NetworkUtils.User;
import edi.md.androidcash.R;
import edi.md.androidcash.RealmHelper.Bill;
import edi.md.androidcash.RealmHelper.Shift;
import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static edi.md.androidcash.BaseApplication.SharedPrefFiscalService;
import static edi.md.androidcash.BaseApplication.SharedPrefSettings;
import static edi.md.androidcash.BaseApplication.SharedPrefWorkPlaceSettings;

/**
 * Created by Igor on 28.10.2019
 */

public class ShiftManage extends Fragment {
    ConstraintLayout open_shift,close_shift,cash_in,cash_out,cash_incasatia;
    TextView tv_opened_shift,tv_closed_shift,tv_schedule_close_shift;
    private EditText edSetCioSum;

    SimpleDateFormat sdfChisinauSchedule;
    SimpleDateFormat sdfChisinau;
    TimeZone tzInChisinau;

    //Declare timer
    CountDownTimer cTimer = null;

    private ProgressDialog progress;

    DatecsFiscalDevice myFiscalDevice;

    private Realm mRealm;
    Shift shiftEntry;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootViewAdmin = inflater.inflate(R.layout.fragment_shift_manage, container, false);

        open_shift = rootViewAdmin.findViewById(R.id.cl_opening_shift);
        close_shift = rootViewAdmin.findViewById(R.id.view_group_close_shift);
        cash_in = rootViewAdmin.findViewById(R.id.cl_cash_in);
        cash_out = rootViewAdmin.findViewById(R.id.cl_cash_out);
        cash_incasatia = rootViewAdmin.findViewById(R.id.cl_incasatia);
        tv_opened_shift = rootViewAdmin.findViewById(R.id.txt_date_opening_shift);
        tv_closed_shift = rootViewAdmin.findViewById(R.id.txt_date_close_shift);
        tv_schedule_close_shift = rootViewAdmin.findViewById(R.id.txt_schedule_close_shift);

        sdfChisinau = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        sdfChisinauSchedule = new SimpleDateFormat("HH:mm:ss");
        tzInChisinau = TimeZone.getTimeZone("Europe/Chisinau");
        sdfChisinau.setTimeZone(tzInChisinau);

        User user = ((BaseApplication)getActivity().getApplication()).getUser();
        myFiscalDevice = ((BaseApplication)getActivity().getApplication()).getMyFiscalDevice();

        SharedPreferences sPrefWorkPlace = getActivity().getSharedPreferences(SharedPrefWorkPlaceSettings, MODE_PRIVATE);
        final String workplaceId = sPrefWorkPlace.getString("WorkPlaceID", "null");

        mRealm = Realm.getDefaultInstance();

        Shift result = mRealm.where(Shift.class).equalTo("closed",false).findFirst();

        if(result != null){
            shiftEntry = mRealm.copyFromRealm(result);

            long opened = shiftEntry.getStartDate();

            if(opened == 0){
                tv_opened_shift.setText("Открыта: " );
            }
            else{
                tv_opened_shift.setText("Открыта: " + sdfChisinau.format(opened));
                tv_closed_shift.setText("Закрыть в: " + sdfChisinau.format(shiftEntry.getNeedClose()));
                if(new Date().getTime() < shiftEntry.getNeedClose()){
                    startTimer(shiftEntry.getNeedClose() - new Date().getTime());
                }
                else{
                    tv_schedule_close_shift.setText("До закрытия осталось: 00:00:00");
                }
            }
        }

        open_shift.setOnClickListener(v -> {
            if(shiftEntry == null){
                LayoutInflater inflater1 = getActivity().getLayoutInflater();
                final View dialogView = inflater1.inflate(R.layout.dialog_open_shift, null);

                final AlertDialog exitApp = new AlertDialog.Builder(getContext(),R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
                exitApp.setCancelable(false);
                exitApp.setView(dialogView);

                Button btn_Cancel = dialogView.findViewById(R.id.btn_no_open_shift);
                Button btn_ok = dialogView.findViewById(R.id.btn_yes_open_shift);

                btn_Cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        exitApp.dismiss();
                    }
                });

                btn_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                        TimeZone tzIn = TimeZone.getTimeZone("Europe/Chisinau");
                        sdf.setTimeZone(tzIn);

                        long opened_new_shift = new Date().getTime();
                        long need_close = opened_new_shift +  28800000;

                        shiftEntry = new Shift();
                        shiftEntry.setName("SHF " + sdf.format(opened_new_shift));
                        shiftEntry.setWorkPlaceId(workplaceId);
                        shiftEntry.setAuthor(user.getId());
                        shiftEntry.setStartDate(new Date().getTime());
                        shiftEntry.setClosed(false);
                        shiftEntry.setNeedClose(need_close);
                        shiftEntry.setId(UUID.randomUUID().toString());

                        mRealm.beginTransaction();
                        mRealm.insert(shiftEntry);
                        mRealm.commitTransaction();

                        tv_closed_shift.setText("Закрыть в: " + sdfChisinau.format(need_close));
                        tv_opened_shift.setText("Открыта: " + sdfChisinau.format(opened_new_shift));
                        startTimer(need_close - new Date().getTime());
                        exitApp.dismiss();
                    }
                });
                exitApp.show();
            }
            else{
                postMessage("Смена уже открыта!");
            }


        });
        close_shift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shiftEntry != null){
                    RealmResults<Bill> billEntryResult = mRealm.where(Bill.class)
                            .equalTo("shiftId",shiftEntry.getId())
                            .and()
                            .equalTo("state",0)
                            .findAll();
                    if(!billEntryResult.isEmpty()){
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        final View dialogView = inflater.inflate(R.layout.dialog_exist_opened_bills, null);

                        final AlertDialog exitApp = new AlertDialog.Builder(getContext(),R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
                        exitApp.setCancelable(false);
                        exitApp.setView(dialogView);

                        Button btn_ok = dialogView.findViewById(R.id.btn_understand);
                        TextView text_msg = dialogView.findViewById(R.id.text_message);

                        text_msg.setText("Нельзя закрыть смену пока есть открытые счета!\nУ вас осталось " + billEntryResult.size() + " открытых счетов.");

                        btn_ok.setOnClickListener(v128 -> exitApp.dismiss());

                        exitApp.show();
                    }
                    else{
                        long open = shiftEntry.getStartDate();
                        if(open != 0){
                            LayoutInflater inflater = getActivity().getLayoutInflater();
                            final View dialogView = inflater.inflate(R.layout.dialog_close_shift, null);

                            final AlertDialog exitApp = new AlertDialog.Builder(getContext(),R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
                            exitApp.setCancelable(false);
                            exitApp.setView(dialogView);

                            Button btn_Cancel = dialogView.findViewById(R.id.btn_no_close_shift);
                            Button btn_ok = dialogView.findViewById(R.id.btn_yes_close_shift);

                            btn_Cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    exitApp.dismiss();
                                }
                            });

                            btn_ok.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    long close = new Date().getTime();
                                    shiftEntry.setClosedBy(user.getId());
                                    shiftEntry.setEndDate(close);
                                    shiftEntry.setClosed(true);

                                    mRealm.executeTransaction(realm -> {
                                        RealmResults<Shift> shift = realm.where(Shift.class).equalTo("id", shiftEntry.getId()).findAll();
                                        shift.setString("closedBy", user.getId());
                                        shift.setLong("endDate", close);
                                        shift.setBoolean("closed", true);
                                        shift.setBoolean("isSended",false);
                                    });

                                    tv_closed_shift.setText("Закрыта: " + sdfChisinau.format(close));
                                    tv_opened_shift.setText("Открыта: ");
                                    tv_schedule_close_shift.setText("До закрытия осталось: ");
                                    cancelTimer();
                                    shiftEntry = null;
                                    exitApp.dismiss();

                                    int workFisc = getActivity().getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getInt("ModeFiscalWork",0);

                                    if(workFisc == 1) {
                                        if (myFiscalDevice != null && myFiscalDevice.isConnectedDeviceV2())
                                            printZReport();
                                    }
                                    if(workFisc == 2)
                                        printZReportFiscalService();
                                }
                            });
                            exitApp.show();
                        }

                    }
                }
                else{
                    postMessage("Сперва откройте смену!");
                }
            }
        });
        cash_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shiftEntry != null ){
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.dialog_cash_in, null);

                    final AlertDialog addPosition = new AlertDialog.Builder(getActivity(),R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
                    addPosition.setCancelable(false);
                    addPosition.setView(dialogView);
                    addPosition.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

                    final TextView amount = dialogView.findViewById(R.id.et_input_data);
                    Button btn_Cancel = dialogView.findViewById(R.id.btn_cancel_cashin);
                    Button btn_ok = dialogView.findViewById(R.id.btn_ok_cashin);
                    Button btn_clear = dialogView.findViewById(R.id.btn_cashin_clear);
                    Button btn_point = dialogView.findViewById(R.id.btn_add_point);

                    Button number_1 = dialogView.findViewById(R.id.btn_cashin_1);
                    Button number_2 = dialogView.findViewById(R.id.btn_cashin_2);
                    Button number_3 = dialogView.findViewById(R.id.btn_cashin_3);
                    Button number_4 = dialogView.findViewById(R.id.btn_cashin_4);
                    Button number_5 = dialogView.findViewById(R.id.btn_cashin_5);
                    Button number_6 = dialogView.findViewById(R.id.btn_cashin_6);
                    Button number_7 = dialogView.findViewById(R.id.btn_cashin_7);
                    Button number_8 = dialogView.findViewById(R.id.btn_cashin_8);
                    Button number_9 = dialogView.findViewById(R.id.btn_cashin_9);
                    Button number_0 = dialogView.findViewById(R.id.btn_cashin_0);

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
                        if(myFiscalDevice != null && myFiscalDevice.isConnectedDeviceV2()){
                            try {
                                Double newAmount = Double.valueOf(amount.getText().toString());
                                cash_IN_OUT(String.format("%.2f", newAmount));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else
                            postMessage("Фискальный апарат не доступен!");

                    });

                    btn_Cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            addPosition.dismiss();
                        }
                    });
                    addPosition.show();
                }
                else
                    postMessage("Смена закрыта или не действительна!");


            }
        });
        cash_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shiftEntry != null){
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.dialog_cash_out, null);

                    final AlertDialog addPosition = new AlertDialog.Builder(getActivity(),R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
                    addPosition.setCancelable(false);
                    addPosition.setView(dialogView);
                    addPosition.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

                    final TextView amount = dialogView.findViewById(R.id.et_input_data);
                    Button btn_Cancel = dialogView.findViewById(R.id.btn_cancel_cashout);
                    Button btn_ok = dialogView.findViewById(R.id.btn_ok_cashout);
                    Button btn_clear = dialogView.findViewById(R.id.btn_cashout_clear);
                    Button btn_point = dialogView.findViewById(R.id.btn_add_point);

                    Button number_1 = dialogView.findViewById(R.id.btn_cashout_1);
                    Button number_2 = dialogView.findViewById(R.id.btn_cashout_2);
                    Button number_3 = dialogView.findViewById(R.id.btn_cashout_3);
                    Button number_4 = dialogView.findViewById(R.id.btn_cashout_4);
                    Button number_5 = dialogView.findViewById(R.id.btn_cashout_5);
                    Button number_6 = dialogView.findViewById(R.id.btn_cashout_6);
                    Button number_7 = dialogView.findViewById(R.id.btn_cashout_7);
                    Button number_8 = dialogView.findViewById(R.id.btn_cashout_8);
                    Button number_9 = dialogView.findViewById(R.id.btn_cashout_9);
                    Button number_0 = dialogView.findViewById(R.id.btn_cashout_0);

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
                                if(cash_IN_OUT(String.format("%.2f", newAmount))){

//                                        mRealm.executeTransaction(realm -> {
//                                             realm.insert();
//                                         });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else
                            postMessage("Фискальный апарат не доступен!");


                });

                    btn_Cancel.setOnClickListener(v127 -> addPosition.dismiss());
                    addPosition.show();
                }
                else
                    postMessage("Смена закрыта или не действительна!");
            }
        });

        return rootViewAdmin;
    }

    private void printZReportFiscalService(){
        progress = new ProgressDialog(getContext());
        progress.setCancelable(true);
        progress.setTitle("Z report is working !!!");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setCancelable(false);
        progress.show();

        String ip = getActivity().getSharedPreferences(SharedPrefFiscalService, MODE_PRIVATE).getString("IpAdressFiscalService",null);
        String port = getActivity().getSharedPreferences(SharedPrefFiscalService, MODE_PRIVATE).getString("PortFiscalService",null);
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
                        if(errorCode == 0){
                            progress.dismiss();
                        }

                    }
                }

                @Override
                public void onFailure(Call<ZResponse> call, Throwable t) {
                    progress.dismiss();
                    postMessage(t.getMessage());
                }
            });
        }
    }

    private void postMessage(String message){
        Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();
    }

    //start timer function
    void startTimer(long time) {
        cTimer = new CountDownTimer(time, 1000) {
            public void onTick(long millisUntilFinished) {
                long second = (millisUntilFinished / 1000) % 60;
                long minute = (millisUntilFinished / (1000 * 60)) % 60;
                long hour = (millisUntilFinished / (1000 * 60 * 60)) % 24;

                String time = String.format("%02d:%02d:%02d", hour, minute, second);
                tv_schedule_close_shift.setText("До закрытия осталось: " + time);
            }
            public void onFinish() {
            }
        };
        cTimer.start();
    }
    //cancel timer
    void cancelTimer() {
        if(cTimer!=null)
            cTimer.cancel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelTimer();
        if(mRealm != null){
            mRealm.close();
        }
    }

    private void printZReport(){
        progress = new ProgressDialog(getContext());
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
                } finally {
                    progress.dismiss();
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogZXReportsSummary dialogSummary = new DialogZXReportsSummary(getActivity(), reportSummary);
                        dialogSummary.show();
                        DisplayMetrics metrics = new DisplayMetrics(); //get metrics of screen
                        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
                        int width = (int) (metrics.widthPixels * 0.5); //set width to 50% of display
                        int height = (int) (metrics.heightPixels * 0.9); //set height to 90% of display
                        dialogSummary.getWindow().setLayout(width, height); //set layout
                        progress.dismiss();

                    }
                });
            }
        }).start();
    }

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

    //Cash in / cash out
    private boolean cash_IN_OUT(String valueOfCurrency) {
        try {
            // cashInSafe Holds result of operation:
            //0-cashSum
            //1-cashIn
            //2-cashOut
            Double[] cashInSafe = new Double[3];

            valueOfCurrency=valueOfCurrency.replace(",",".");
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

            if (cashInSafe[0] != null)
                return true;
            else
                return false;
        } catch (Exception e) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
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
}