package md.intelectsoft.salesepos.SettingUtils;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.datecs.fiscalprinter.SDK.FiscalException;
import com.datecs.fiscalprinter.SDK.model.DatecsFiscalDevice;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.List;

import md.intelectsoft.salesepos.BaseApplication;
import md.intelectsoft.salesepos.DatcesNewFile.PrinterManager;
import md.intelectsoft.salesepos.MainActivity;
import md.intelectsoft.salesepos.NetworkUtils.AssortmentServiceEntry;
import md.intelectsoft.salesepos.NetworkUtils.EposResult.AssortmentListService;
import md.intelectsoft.salesepos.NetworkUtils.EposResult.GetAssortmentListResult;
import md.intelectsoft.salesepos.NetworkUtils.EposResult.GetUsersListResult;
import md.intelectsoft.salesepos.NetworkUtils.EposResult.GetWorkplaceSettingsResult;
import md.intelectsoft.salesepos.NetworkUtils.EposResult.UserListServiceResult;
import md.intelectsoft.salesepos.NetworkUtils.EposResult.WorkPlaceSettings;
import md.intelectsoft.salesepos.NetworkUtils.FiscalDevice;
import md.intelectsoft.salesepos.NetworkUtils.FiscalServiceResult.SimpleResult;
import md.intelectsoft.salesepos.NetworkUtils.PaymentType;
import md.intelectsoft.salesepos.NetworkUtils.Promotion;
import md.intelectsoft.salesepos.NetworkUtils.QuickGroup;
import md.intelectsoft.salesepos.NetworkUtils.RetrofitRemote.ApiUtils;
import md.intelectsoft.salesepos.NetworkUtils.RetrofitRemote.CommandServices;
import md.intelectsoft.salesepos.NetworkUtils.User;
import md.intelectsoft.salesepos.R;
import md.intelectsoft.salesepos.RealmHelper.AssortmentRealm;
import md.intelectsoft.salesepos.RealmHelper.Barcodes;
import md.intelectsoft.salesepos.RealmHelper.QuickGroupRealm;
import md.intelectsoft.salesepos.Utils.BaseEnum;
import md.intelectsoft.salesepos.connectors.AbstractConnector;
import md.intelectsoft.salesepos.connectors.UsbDeviceConnector;
import io.realm.Realm;
import io.realm.RealmList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static md.intelectsoft.salesepos.BaseApplication.SharedPrefSettings;
import static md.intelectsoft.salesepos.BaseApplication.SharedPrefSyncSettings;
import static md.intelectsoft.salesepos.BaseApplication.SharedPrefWorkPlaceSettings;

public class FragmentFiscalPage extends Fragment {

    private RadioButton fiscalServiceButton,fiscalDeviceButton;

    private int fiscalWorkMode;
    TextView fiscalServiceState, fiscalDeviceState;

    private ConstraintLayout csl_fiscServiceSettings, csl_fiscDeviceSettings;
    private SharedPreferences sharedPrefSettings;
    private ProgressDialog pgH;

    ImageButton btnFiscalServiceSettings;

    DatecsFiscalDevice myFiscalDevice;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootViewAdmin = inflater.inflate(R.layout.fragment_fiscal_page, container, false);

        pgH = new ProgressDialog(getContext(),R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme);
        btnFiscalServiceSettings = rootViewAdmin.findViewById(R.id.img_btn_settings_fiscal_service);
        csl_fiscServiceSettings = rootViewAdmin.findViewById(R.id.csl_fiscal_service_settings);
        csl_fiscDeviceSettings = rootViewAdmin.findViewById(R.id.csl_fiscal_device_settings);
        fiscalServiceButton = rootViewAdmin.findViewById(R.id.radio_btn_fiscal_service);
        fiscalDeviceButton = rootViewAdmin.findViewById(R.id.radio_btn_fiscal_device);
        fiscalServiceState = rootViewAdmin.findViewById(R.id.txt_fiscal_service_state);
        fiscalDeviceState = rootViewAdmin.findViewById(R.id.txt_fiscal_device_settings);

        sharedPrefSettings = getActivity().getSharedPreferences(SharedPrefSettings,MODE_PRIVATE);
        fiscalWorkMode = sharedPrefSettings.getInt("ModeFiscalWork", BaseEnum.FISCAL_SERVICE);

        if(fiscalWorkMode == BaseEnum.FISCAL_DEVICE) {
            fiscalDeviceButton.setChecked(true);
            fiscalServiceButton.setChecked(false);
            btnFiscalServiceSettings.setEnabled(false);

            csl_fiscServiceSettings.setEnabled(false);
            csl_fiscDeviceSettings.setEnabled(true);
            csl_fiscServiceSettings.setBackgroundColor(Color.parseColor("#2CACABAB"));
        }
        else if(fiscalWorkMode == BaseEnum.FISCAL_SERVICE){
            connectToFiscalService();

            fiscalServiceButton.setChecked(true);
            fiscalDeviceButton.setChecked(false);
            btnFiscalServiceSettings.setEnabled(true);

            csl_fiscDeviceSettings.setEnabled(false);
            csl_fiscServiceSettings.setEnabled(true);
            csl_fiscDeviceSettings.setBackgroundColor(Color.parseColor("#2CACABAB"));
        }

        fiscalServiceButton.setOnClickListener(view -> {
            sharedPrefSettings.edit().putInt("ModeFiscalWork",BaseEnum.FISCAL_SERVICE).apply();
            csl_fiscDeviceSettings.setEnabled(false);
            csl_fiscServiceSettings.setEnabled(true);
            btnFiscalServiceSettings.setEnabled(true);

            csl_fiscDeviceSettings.setBackgroundColor(Color.parseColor("#2CACABAB"));
            csl_fiscServiceSettings.setBackgroundColor(Color.TRANSPARENT);
        });

        fiscalDeviceButton.setOnClickListener(view -> {
            sharedPrefSettings.edit().putInt("ModeFiscalWork",BaseEnum.FISCAL_DEVICE).apply();
            csl_fiscServiceSettings.setEnabled(false);
            csl_fiscDeviceSettings.setEnabled(true);
            btnFiscalServiceSettings.setEnabled(false);

            csl_fiscDeviceSettings.setBackgroundColor(Color.TRANSPARENT);
            csl_fiscServiceSettings.setBackgroundColor(Color.parseColor("#2CACABAB"));
        });

        csl_fiscDeviceSettings.setOnClickListener(view -> {

            myFiscalDevice = BaseApplication.getInstance().getMyFiscalDevice();

            if(myFiscalDevice != null && myFiscalDevice.isConnectedDeviceV2()){
                fiscalDeviceState.setText(PrinterManager.instance.getModelVendorName() + " connected.");
            }
            else{
                fiscalDeviceState.setText("Device not connected.");
            }

        });
        csl_fiscServiceSettings.setOnClickListener(view -> {
            connectToFiscalService();
        });

        btnFiscalServiceSettings.setOnClickListener(view -> {
            String uri = sharedPrefSettings.getString("FiscalServiceAddress","0.0.0.0:1111");
            int index = uri.indexOf(":");
            String aipi = uri.substring(0,index);
            String ports = uri.substring(index + 1,uri.length());

            View dialogView = inflater.inflate(R.layout.dialog_fiscal_service_settings, null);

            AlertDialog addClient = new AlertDialog.Builder(getActivity(),R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
            addClient.setView(dialogView);

            MaterialButton btnSave = dialogView.findViewById(R.id.btn_save_settings_fiscal_service);
            MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel_fiscal_service_settings);

            EditText etIp = dialogView.findViewById(R.id.ip_fiscal_service_settings);
            EditText etPort = dialogView.findViewById(R.id.port_fiscal_service_settings);

            if(!aipi.equals("0.0.0.0")){
                etIp.setText(aipi);
                etPort.setText(ports);
            }

            btnCancel.setOnClickListener(view1 -> {
                addClient.dismiss();
            });
            btnSave.setOnClickListener(view1 -> {
                String ip = etIp.getText().toString();
                String port = etPort.getText().toString();

                sharedPrefSettings.edit().putString("FiscalServiceAddress", ip + ":" + port).apply();

                addClient.dismiss();
            });

            addClient.show();
            addClient.getWindow().setLayout(420, LinearLayout.LayoutParams.WRAP_CONTENT);
            addClient.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            addClient.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        });



        return rootViewAdmin;
    }

    private void connectToFiscalService(){
        String uriService = sharedPrefSettings.getString("FiscalServiceAddress","0.0.0.0:1111");

        fiscalServiceState.setTextColor(Color.rgb(145,145,145));
        fiscalServiceState.setText("Connecting to http://" + uriService + "/fpservice" );

        CommandServices commandServices = ApiUtils.commandFPService(uriService);
        Call<SimpleResult> call = commandServices.getState();

        call.enqueue(new Callback<SimpleResult>() {
            @Override
            public void onResponse(Call<SimpleResult> call, Response<SimpleResult> response) {
                SimpleResult result = response.body();

                if(result != null && result.getErrorCode() == 0){
                    fiscalServiceState.setText("Connected to http://" + uriService + "/fpservice" );
                    fiscalServiceState.setTextColor(Color.rgb(48, 128, 20));
                }
                else{
                    fiscalServiceState.setText("Not connected to http://" + uriService + "/fpservice , Error: " + result.getErrorCode() + ". Tap to test connection");
                    fiscalServiceState.setTextColor(Color.rgb(219,45,45));
                }

            }

            @Override
            public void onFailure(Call<SimpleResult> call, Throwable t) {
                fiscalServiceState.setText("Not connected to http://" + uriService + "/fpservice , Error:" + t.getMessage() + ". Tap to test connection");
                fiscalServiceState.setTextColor(Color.rgb(219,45,45));
            }
        });
    }
}
