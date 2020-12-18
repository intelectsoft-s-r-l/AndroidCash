package md.intelectsoft.salesepos.Fragments;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReport;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import md.intelectsoft.salesepos.BaseApplication;
import md.intelectsoft.salesepos.NetworkUtils.FiscalServiceResult.SimpleResult;
import md.intelectsoft.salesepos.NetworkUtils.RetrofitRemote.ApiUtils;
import md.intelectsoft.salesepos.NetworkUtils.RetrofitRemote.CommandServices;
import md.intelectsoft.salesepos.R;
import md.intelectsoft.salesepos.RealmHelper.Bill;
import md.intelectsoft.salesepos.RealmHelper.BillPaymentType;
import md.intelectsoft.salesepos.RealmHelper.History;
import md.intelectsoft.salesepos.RealmHelper.Shift;
import md.intelectsoft.salesepos.ShiftsActivity;
import md.intelectsoft.salesepos.Utils.BaseEnum;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static md.intelectsoft.salesepos.BaseApplication.SharedPrefSettings;
import static md.intelectsoft.salesepos.MainActivity.datecsFiscalDevice;

/**
 * Created by Igor on 26.05.2020
 */

public class FragmentInformationShift extends Fragment {
    private static Shift shift;
    private static Button closeShift;
    private static TextView nameShift, openShift, openedShiftBy, closeDateShift, closedShiftBy, countBillToShift, tvCashSales, tvCardSales, tvOtherSales,
            tvIncomeSales, tvCashToEndShift, tvCashToStartShift, tvCashInToShift,tvCashOutToShift;

    static SimpleDateFormat simpleDateFormatMD;
    TimeZone timeZoneMD;

    static Realm mRealm;

    Context context;
    Activity activity;
    private ProgressDialog progressDialogPrintReport;
    LayoutInflater inflater;


    public FragmentInformationShift(Shift shift) {
        FragmentInformationShift.shift = shift;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootViewAdmin = inflater.inflate(R.layout.fragment_shift_information, container, false);

        closeShift = rootViewAdmin.findViewById(R.id.btn_closed_current_shift);
        nameShift = rootViewAdmin.findViewById(R.id.txt_no_shift);
        openedShiftBy = rootViewAdmin.findViewById(R.id.text_shift_opened_by);
        openShift = rootViewAdmin.findViewById(R.id.text_opened_shift);
        closeDateShift = rootViewAdmin.findViewById(R.id.text_closed_shift_date);
        closedShiftBy = rootViewAdmin.findViewById(R.id.text_closed_by_shift);
        countBillToShift = rootViewAdmin.findViewById(R.id.tv_count_bill_for_shift);
        tvCashSales = rootViewAdmin.findViewById(R.id.tv_cash_bill_for_shift);
        tvCardSales = rootViewAdmin.findViewById(R.id.tv_card_bill_for_shift);
        tvOtherSales = rootViewAdmin.findViewById(R.id.tv_other_bill_for_shift);
        tvIncomeSales = rootViewAdmin.findViewById(R.id.tv_virucika_for_shift);
        tvCashToEndShift = rootViewAdmin.findViewById(R.id.tv_cash_for_shift_end);
        tvCashToStartShift = rootViewAdmin.findViewById(R.id.tv_cash_for_shift_start);
        tvCashInToShift = rootViewAdmin.findViewById(R.id.tv_cash_in_for_shift);
        tvCashOutToShift = rootViewAdmin.findViewById(R.id.tv_out_cash_for_shift);

        simpleDateFormatMD = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        timeZoneMD = TimeZone.getTimeZone("Europe/Chisinau");
        simpleDateFormatMD.setTimeZone(timeZoneMD);

        mRealm = Realm.getDefaultInstance();
        context = getContext();
        activity = getActivity();
        this.inflater = inflater;

        closeShift.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(getContext(), R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                    .setTitle(getResources().getString(R.string.message_attention))
                    .setMessage(getResources().getString(R.string.message_close_shift))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.btn_yes), (dialogInterface, i) -> {
                        closeShift();
                    })
                    .setNegativeButton(getResources().getString(R.string.btn_no),((dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    }))
                    .show();

        });

        updateDisplayInfo();

        return rootViewAdmin;
    }

    public static void updateShift(Shift shiftEntry){
         shift = shiftEntry;

         if(closeShift != null)
            updateDisplayInfo();
    }

    private static void updateDisplayInfo(){
        if(shift.isClosed()) {
            closeShift.setVisibility(View.GONE);
            closedShiftBy.setText(shift.getClosedByName());
            closeDateShift.setText(simpleDateFormatMD.format(shift.getEndDate()));
        }
        else {
            closeShift.setVisibility(View.VISIBLE);
            closedShiftBy.setText("-");
            closeDateShift.setText("-");
        }

        tvCashOutToShift.setText("MDL " + String.valueOf(shift.getCashOut()));
        tvCashInToShift.setText("MDL " + String.valueOf(shift.getCashIn()));



        RealmResults<Bill> billRealmResults = mRealm.where(Bill.class).equalTo("shiftId",shift.getId()).findAll();

        FragmentTicketsShift.setListBills(billRealmResults);

        countBillToShift.setText(String.valueOf(billRealmResults.size()));

        RealmQuery<BillPaymentType> query = mRealm.where(BillPaymentType.class);
            // ids is a list of the category ids
        if (billRealmResults.size() > 0) {
            query = query.equalTo("billID", billRealmResults.get(0).getId());
            for (int i = 1; i < billRealmResults.size(); i++) {
                query = query.or().equalTo("billID", billRealmResults.get(i).getId());
            }
        }

        double sumOfCash = 0;
        double sumOfCard = 0;
        double sumOfOther = 0;
        if (billRealmResults.size() > 0) {
            RealmResults<BillPaymentType> result = query.findAll();
            if(!result.isEmpty()){
                for(BillPaymentType pay : result){
                    if(pay.getPaymentCode() == 0)
                        sumOfCash += pay.getSum();
                    else if (pay.getPaymentCode() == 2)
                        sumOfCard += pay.getSum();
                    else
                        sumOfOther += pay.getSum();
                }
            }
        }


        tvCashSales.setText("MDL " + String.valueOf(sumOfCash));
        tvCardSales.setText("MDL " + String.valueOf(sumOfCard));
        tvOtherSales.setText("MDL " + String.valueOf(sumOfOther));
        tvIncomeSales.setText("MDL " + String.valueOf(sumOfCash + sumOfCard + sumOfOther));

        tvCashToEndShift.setText("MDL " + String.valueOf(sumOfCash + shift.getCashIn() - shift.getCashOut()));
//        tvCashToStartShift.setText("MDL " + String.valueOf(shift.getCashIn()));

        nameShift.setText(shift.getName());
        openShift.setText(simpleDateFormatMD.format(shift.getStartDate()));
        openedShiftBy.setText(shift.getAuthorName());
        countBillToShift.setText(String.valueOf(shift.getBillCounter()));
    }


    private void closeShift(){
        RealmResults<Bill> billEntryResult = mRealm.where(Bill.class)
                .equalTo("shiftId",shift.getId())
                .and()
                .equalTo("state",0)
                .findAll();
        if(!billEntryResult.isEmpty()){
            new MaterialAlertDialogBuilder(getContext(), R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                    .setTitle(getResources().getString(R.string.message_attention))
                    .setMessage(getString(R.string.message_cannot_close_shift_bill_active)  + billEntryResult.size() + getString(R.string.message_open_bills))
                    .setCancelable(false)
                    .setPositiveButton("OKAY", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    })
                    .show();
        }
        else{
            long close = new Date().getTime();

            mRealm.executeTransaction(realm -> {
                Shift shift1 = realm.where(Shift.class).equalTo("id",shift.getId()).findFirst();
                if(shift1 != null){
                    shift1.setClosedBy(BaseApplication.getInstance().getUserId());
                    shift1.setEndDate(close);
                    shift1.setClosed(true);
                    shift1.setClosedByName(BaseApplication.getInstance().getUser().getFullName());
                    shift1.setSended(false);

                    ShiftsActivity.setViewPagerVisibility(false);
                }
            });

            BaseApplication.getInstance().setShift(null);

            History history = new History();
            history.setDate(new Date().getTime());
            history.setMsg("Shift: " + shift.getName());
            history.setType(BaseEnum.History_ClosedShift);
            mRealm.executeTransaction(realm -> realm.insert(history));

            int workFisc = getContext().getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getInt("ModeFiscalWork",BaseEnum.FISCAL_SERVICE);

            if(workFisc == BaseEnum.FISCAL_DEVICE) {
                if (datecsFiscalDevice != null && datecsFiscalDevice.isConnectedDeviceV2())
                    printZReport();
            }
            if(workFisc == BaseEnum.FISCAL_SERVICE){
                String uri = getContext().getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getString("FiscalServiceAddress","0.0.0.0:1111");
                printZReportFiscalService(uri);
            }

        }
    }
    private void printZReport(){
        progressDialogPrintReport = new ProgressDialog(context,R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog);
        progressDialogPrintReport.setTitle(context.getString(R.string.message_z_report_starting));
        progressDialogPrintReport.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialogPrintReport.setCancelable(false);
        progressDialogPrintReport.show();
        final int[] reportNumber = {0};
        final cmdReport.ReportSummary reportSummary = new cmdReport.ReportSummary();

        new Thread(new Runnable() {
            @Override
            public void run() {
                // do the thing that takes a long time
                try {
                    cmdReport cmd = new cmdReport();
                    reportNumber[0] = cmd.PrintZreport(reportSummary);

                    History history = new History();
                    history.setDate(new Date().getTime());
                    history.setMsg(context.getString(R.string.message_z_report) + reportNumber[0]);
                    history.setType(BaseEnum.History_Printed_Z);
                    mRealm.executeTransaction(realm -> realm.insert(history));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    progressDialogPrintReport.dismiss();
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(context, "Z report print!", Toast.LENGTH_SHORT).show();
//                        View dialogView = inflater.inflate(R.layout.dialog_x_z_total,null);
//
//                        final AlertDialog dialog_summary = new AlertDialog.Builder(activity,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
//                        dialog_summary.setCancelable(false);
//                        dialog_summary.setView(dialogView);
//                        dialog_summary.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//
//                        TextView tvTaxA = dialogView.findViewById(R.id.tvTaxA);
//                        TextView tvTaxB = dialogView.findViewById(R.id.tvTaxB);
//                        TextView tvTaxC = dialogView.findViewById(R.id.tvTaxC);
//                        TextView tvTaxD = dialogView.findViewById(R.id.tvTaxD);
//                        TextView tvTaxE = dialogView.findViewById(R.id.tvTaxE);
//                        TextView tvTaxF = dialogView.findViewById(R.id.tvTaxF);
//                        TextView tvTaxG = dialogView.findViewById(R.id.tvTaxG);
//                        TextView tvTaxH = dialogView.findViewById(R.id.tvTaxH);
//                        Button btnOk = dialogView.findViewById(R.id.btn_total_reports);
//
//                        btnOk.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                dialog_summary.dismiss();
//                            }
//                        });
//
//                        tvTaxA.setText(String.valueOf(reportSummary.totalA));
//                        tvTaxB.setText(String.valueOf(reportSummary.totalB));
//                        tvTaxC.setText(String.valueOf(reportSummary.totalC));
//                        tvTaxD.setText(String.valueOf(reportSummary.totalD));
//                        tvTaxE.setText(String.valueOf(reportSummary.totalE));
//                        tvTaxF.setText(String.valueOf(reportSummary.totalF));
//                        tvTaxG.setText(String.valueOf(reportSummary.totalG));
//                        tvTaxH.setText(String.valueOf(reportSummary.totalH));
//
//                        dialog_summary.show();
//
//                        DisplayMetrics metrics = new DisplayMetrics(); //get metrics of screen
//                        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
//                        int width = (int) (metrics.widthPixels * 0.4); //set width to 50% of display
//                        int height = (int) (metrics.heightPixels * 0.9); //set height to 90% of display
//                        dialog_summary.getWindow().setLayout(width, height); //set layout
                        progressDialogPrintReport.dismiss();
                    }
                });
            }
        }).start();
    }
    private void printZReportFiscalService(String uri) {
        progressDialogPrintReport = new ProgressDialog(activity,R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog);
        progressDialogPrintReport.setTitle(context.getString(R.string.message_z_report_working));
        progressDialogPrintReport.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialogPrintReport.setCancelable(false);
        progressDialogPrintReport.show();

        CommandServices commandServices = ApiUtils.commandFPService(uri);
        Call<SimpleResult> responseCall = commandServices.printZReport(BaseEnum.FiscalPrint_Master);

        responseCall.enqueue(new Callback<SimpleResult>() {
            @Override
            public void onResponse(Call<SimpleResult> call, Response<SimpleResult> response) {
                SimpleResult zResponse = response.body();
                if (zResponse != null) {
                    int errorCode = zResponse.getErrorCode();
                    if (errorCode == 0) {
                        progressDialogPrintReport.dismiss();
                        History history = new History();
                        history.setDate(new Date().getTime());
                        history.setMsg(context.getString(R.string.message_report_z_printed_task_id) + zResponse.getTaskId());
                        history.setType(BaseEnum.History_Printed_Z);
                        mRealm.executeTransaction(realm -> realm.insert(history));
                    }

                }
            }

            @Override
            public void onFailure(Call<SimpleResult> call, Throwable t) {
                progressDialogPrintReport.dismiss();
                Toast.makeText(activity, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
