package edi.md.androidcash.SettingUtils.Equipment.FiscalMode;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import edi.md.androidcash.NetworkUtils.FiscalServiceResult.SimpleResult;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.ApiUtils;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.CommandServices;
import edi.md.androidcash.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static edi.md.androidcash.BaseApplication.SharedPrefFiscalService;

public class FiscalService extends Fragment {

    Button btn_resume, btn_check_connection;
    EditText et_ip, et_port;
    TextView txt_lastOnline;

    SimpleDateFormat sdfChisinau;
    TimeZone tzInChisinau;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootViewAdmin = inflater.inflate(R.layout.fragment_fiscal_service, container, false);

        btn_check_connection = rootViewAdmin.findViewById(R.id.btn_check_conection_fiscal_service);
        btn_resume = rootViewAdmin.findViewById(R.id.btn_resume);
        et_ip = rootViewAdmin.findViewById(R.id.et_ip_adress_conection_fiscal_service);
        et_port = rootViewAdmin.findViewById(R.id.et_port_conection_fiscal_service);
        txt_lastOnline = rootViewAdmin.findViewById(R.id.txt_last_conection_sync);


        sdfChisinau = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        tzInChisinau = TimeZone.getTimeZone("Europe/Chisinau");
        sdfChisinau.setTimeZone(tzInChisinau);

        et_ip.setText(getActivity().getSharedPreferences(SharedPrefFiscalService, MODE_PRIVATE).getString("IpAdressFiscalService",null));
        et_port.setText(getActivity().getSharedPreferences(SharedPrefFiscalService, MODE_PRIVATE).getString("PortFiscalService",null));
        txt_lastOnline.setText(getActivity().getSharedPreferences(SharedPrefFiscalService, MODE_PRIVATE).getString("LastConnect","The latest synchronization was: never"));

        btn_check_connection.setOnClickListener( view -> {
            String uri = et_ip.getText().toString() + ":" + et_port.getText().toString();
            getActivity().getSharedPreferences(SharedPrefFiscalService, MODE_PRIVATE)
                    .edit()
                    .putString("IpAdressFiscalService",et_ip.getText().toString())
                    .putString("PortFiscalService",et_port.getText().toString())
                    .apply();

            CommandServices commandServices = ApiUtils.commandFPService(uri);
            Call<SimpleResult> call = commandServices.getState();
            call.enqueue(new Callback<SimpleResult>() {
                @Override
                public void onResponse(Call<SimpleResult> call, Response<SimpleResult> response) {
                    SimpleResult result = response.body();
                    if(result != null){
                        int errorCode = result.getErrorCode();
                        String errorMsg = result.getErrorMessage();

                        if(errorCode == 0){
                            getActivity().getSharedPreferences(SharedPrefFiscalService, MODE_PRIVATE)
                                    .edit()
                                    .putString("LastConnect","The latest synchronization was: " + sdfChisinau.format(new Date()))
                                    .apply();
                            txt_lastOnline.setText("The latest synchronization was: " + sdfChisinau.format(new Date()));
                        }
                    }
                }

                @Override
                public void onFailure(Call<SimpleResult> call, Throwable t) {
                    String msg = t.getMessage();
                }
            });
        });

        return rootViewAdmin;
    }

}
