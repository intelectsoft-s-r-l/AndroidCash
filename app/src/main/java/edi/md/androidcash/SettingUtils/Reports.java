package edi.md.androidcash.SettingUtils;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReport;

import edi.md.androidcash.NetworkUtils.ApiUtils;
import edi.md.androidcash.NetworkUtils.FiscalServiceResult.PrintReportXResult;
import edi.md.androidcash.NetworkUtils.FiscalServiceResult.PrintReportZResult;
import edi.md.androidcash.NetworkUtils.FiscalServiceResult.XResponse;
import edi.md.androidcash.NetworkUtils.FiscalServiceResult.ZResponse;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.PrintXService;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.PrintZService;
import edi.md.androidcash.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static edi.md.androidcash.GlobalVariables.SharedPrefFiscalService;
import static edi.md.androidcash.GlobalVariables.SharedPrefSettings;

/**
 * Created by Igor on 28.10.2019
 */

public class Reports extends Fragment {
    Button X_report,Z_report;
    private ProgressDialog progress;
    TextView x_errore,z_errore;

    int fiscalManager = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootViewAdmin = inflater.inflate(R.layout.fragment_reports, container, false);

        X_report = rootViewAdmin.findViewById(R.id.btn_x_report_settings);
        Z_report = rootViewAdmin.findViewById(R.id.btn_z_report_settings);
        x_errore = rootViewAdmin.findViewById(R.id.txt_error_x_report_settings);
        z_errore = rootViewAdmin.findViewById(R.id.txt_error_z_report_settings);

        fiscalManager = getActivity().getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getInt("ModeFiscalWork",0);

        X_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fiscalManager == 1){
                    x_errore.setText("");
                    progress = new ProgressDialog(getContext());
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
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (reportNummber[0] != -1) {
                                        foundMessage.obtainMessage(111,reportNummber[0]).sendToTarget();
                                        DialogZXReportsSummary dialogSummary = new DialogZXReportsSummary(getActivity(), reportSummary);
                                        dialogSummary.show();
                                        DisplayMetrics metrics = new DisplayMetrics(); //get metrics of screen
                                        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
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
                if(fiscalManager == 2){
                    x_errore.setText("");
                    String ip = getActivity().getSharedPreferences(SharedPrefFiscalService, MODE_PRIVATE).getString("IpAdressFiscalService",null);
                    String port = getActivity().getSharedPreferences(SharedPrefFiscalService, MODE_PRIVATE).getString("PortFiscalService",null);
                    if(ip != null && port != null) {
                        String uri = ip + ":" + port;

                        PrintXService printXService = ApiUtils.printXService(uri);
                        Call<XResponse> responseCall = printXService.printXReport();

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
            }
        });

        Z_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fiscalManager == 1){
                    z_errore.setText("");
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
                                z_errore.setText(e.getMessage());
                            } finally {
                                progress.dismiss();
                                if (reportNumber[0] == -1){
                                    foundMessage.obtainMessage(202).sendToTarget();
                                }
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (reportNumber[0] != -1) {
                                        foundMessage.obtainMessage(222,reportNumber[0]).sendToTarget();
                                        DialogZXReportsSummary dialogSummary = new DialogZXReportsSummary(getActivity(), reportSummary);
                                        dialogSummary.show();
                                        DisplayMetrics metrics = new DisplayMetrics(); //get metrics of screen
                                        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
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
                if(fiscalManager == 2){
                    z_errore.setText("");
                    String ip = getActivity().getSharedPreferences(SharedPrefFiscalService, MODE_PRIVATE).getString("IpAdressFiscalService",null);
                    String port = getActivity().getSharedPreferences(SharedPrefFiscalService, MODE_PRIVATE).getString("PortFiscalService",null);
                    if(ip != null && port != null) {
                        String uri = ip + ":" + port;

                        PrintZService printZService = ApiUtils.printZService(uri);
                        Call<ZResponse> responseCall = printZService.printZReport();

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
            }
        });


        return rootViewAdmin;
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
