package edi.md.androidcash.SettingUtils.Preference;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Date;
import java.util.List;

import edi.md.androidcash.BaseApplication;
import edi.md.androidcash.MainActivity;
import edi.md.androidcash.NetworkUtils.EposResult.AuthentificateUserResult;
import edi.md.androidcash.NetworkUtils.EposResult.GetWorkPlaceService;
import edi.md.androidcash.NetworkUtils.EposResult.GetWorkplacesResult;
import edi.md.androidcash.NetworkUtils.EposResult.TokenReceivedFromAutenficateUser;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.ApiUtils;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.CommandServices;
import edi.md.androidcash.NetworkUtils.WorkplaceEntry;
import edi.md.androidcash.R;
import edi.md.androidcash.StartedActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static edi.md.androidcash.BaseApplication.SharedPrefSettings;
import static edi.md.androidcash.BaseApplication.SharedPrefWorkPlaceSettings;

public class GeneralPageSet extends Fragment {
    Spinner mSelectWorkPlace,mAuthMethod;
    TextView companyName, idnoCompany;
    int MESSAGE_SUCCES = 0,MESSAGE_ERROR = 1,MESSAGE_FAILURE = 2, MESSAGE_MethodNotAllowed = 3;
    String[] mWorkPlaceName,mWorkPlaceId;
    ArrayAdapter<String> adapterType,adapterAuth;

    private static final String[] authStrings = {"Login/Parola", "Parola", "Card"};

    SharedPreferences sPrefWorkPlace;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootViewAdmin = inflater.inflate(R.layout.fragment_general_page, container, false);

        mSelectWorkPlace = rootViewAdmin.findViewById(R.id.spinner_select_workplace);
        mAuthMethod = rootViewAdmin.findViewById(R.id.spinner_authentificate_method);
        companyName = rootViewAdmin.findViewById(R.id.txt_company_name);
        idnoCompany = rootViewAdmin.findViewById(R.id.txt_company_idno);

        sPrefWorkPlace = getActivity().getSharedPreferences(SharedPrefWorkPlaceSettings, MODE_PRIVATE);
        final SharedPreferences.Editor sPrefWorkPlace_Edit = sPrefWorkPlace.edit();
        int pos = sPrefWorkPlace.getInt("AuthPosition",0);

        companyName.setText(getActivity().getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getString("CompanyName",""));
        idnoCompany.setText(getActivity().getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getString("IDNO",""));

        adapterAuth = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, authStrings);
        adapterAuth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAuthMethod.setAdapter(adapterAuth);

        mAuthMethod.setSelection(pos);

        String uri = getActivity().getSharedPreferences("Settings",MODE_PRIVATE).getString("URI",null);

        String token = getActivity().getSharedPreferences("Settings",MODE_PRIVATE).getString("Token",null);
        long validToken = getActivity().getSharedPreferences("Settings",MODE_PRIVATE).getLong("TokenValidTo",0);
        long currentTime = new Date().getTime();

        if(currentTime < validToken)
            getSyncWorkplace(uri,token);
        else{
            String login = BaseApplication.getInstance().getUser().getUserName();
            String pass = BaseApplication.getInstance().getUserPasswordsNotHashed();
            setmAuthMethod ( login, pass);

        }

        mSelectWorkPlace.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sPrefWorkPlace_Edit.putString("WorkPlaceID",mWorkPlaceId[position]);
                sPrefWorkPlace_Edit.putString("WorkPlaceName",mWorkPlaceName[position]);
                sPrefWorkPlace_Edit.putInt("Position",position);
                sPrefWorkPlace_Edit.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mAuthMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sPrefWorkPlace_Edit.putString("AuthMethod",authStrings[position]);
                sPrefWorkPlace_Edit.putInt("AuthPosition",position);
                sPrefWorkPlace_Edit.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return rootViewAdmin;
    }
    private void getSyncWorkplace(final String uri, String token){
        CommandServices commandServices = ApiUtils.commandEposService(uri);
        final Call<GetWorkPlaceService> workplace = commandServices.getWorkplace(token);
        workplace.enqueue(new Callback<GetWorkPlaceService>() {
            @Override
            public void onResponse(Call<GetWorkPlaceService> call, Response<GetWorkPlaceService> response) {

                GetWorkPlaceService workPlaceService = response.body();
                GetWorkplacesResult result = workPlaceService != null ? workPlaceService.getGetWorkplacesResult() : null;

                int errorecode = 101;
                if (result != null) {
                    errorecode = result.getErrorCode();
                }

                if(errorecode == 0){
                    List<WorkplaceEntry> workplaceEntryList= result.getWorkplaces();
                    mWorkPlaceName = new String[workplaceEntryList.size()];
                    mWorkPlaceId = new String[workplaceEntryList.size()];

                    for (int i = 0; i < workplaceEntryList.size(); i++) {
                        WorkplaceEntry workplaceEntry = workplaceEntryList.get(i);
                        mWorkPlaceName[i] = workplaceEntry.getName();
                        mWorkPlaceId[i] = workplaceEntry.getID();
                    }

                    mHandlerBills.obtainMessage(MESSAGE_SUCCES).sendToTarget();

                }
                else if(errorecode == 405){
                    // не прав на просмотр рабочих мест
                    mHandlerBills.obtainMessage(MESSAGE_MethodNotAllowed).sendToTarget();
                }
                else if(errorecode == 401){
                    //необходимо обновить токен
                    String login = BaseApplication.getInstance().getUser().getUserName();
                    String pass = BaseApplication.getInstance().getUserPasswordsNotHashed();

                    setmAuthMethod(login,pass);
                }
                else{
                    mHandlerBills.obtainMessage(MESSAGE_ERROR,errorecode).sendToTarget();
                }
            }
            @Override
            public void onFailure(Call<GetWorkPlaceService> call, Throwable t) {
                mHandlerBills.obtainMessage(MESSAGE_FAILURE,t.getMessage()).sendToTarget();
            }
        });
    }

    private final Handler mHandlerBills = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_SUCCES) {
                adapterType = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mWorkPlaceName);
                adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSelectWorkPlace.setAdapter(adapterType);
                for(int i=0; i <mWorkPlaceName.length;i++){
                    if(mWorkPlaceName[i].equals(sPrefWorkPlace.getString("WorkPlaceName","null")) && !sPrefWorkPlace.getString("WorkPlaceName","null").equals("null")){
                        mSelectWorkPlace.setSelection(i);
                    }
                }
            }
            else if(msg.what == MESSAGE_ERROR) {
                AlertDialog.Builder failureAsl = new AlertDialog.Builder(getActivity());
                failureAsl.setCancelable(false);
                failureAsl.setTitle("Atentie!");
                failureAsl.setMessage("Eroare " + msg.obj.toString());
                failureAsl.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                failureAsl.show();
            }
            else if( msg.what == MESSAGE_FAILURE ){
                AlertDialog.Builder failureAsl = new AlertDialog.Builder(getActivity());
                failureAsl.setCancelable(false);
                failureAsl.setTitle("Atentie eroare!");
                failureAsl.setMessage("Eroare la descarcarea asortimentului.Mesajul erorii: "+ msg.obj.toString()+"\n" +"Doriti sa incercati din nou?");
                failureAsl.setPositiveButton("Da", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                failureAsl.setNegativeButton("Nu", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                failureAsl.show();
            }
            else if(msg.what == MESSAGE_MethodNotAllowed){
                dialogNewLoginsUser();
            }
        }
    };

    private void dialogNewLoginsUser(){
        LayoutInflater inflater1 = this.getLayoutInflater();
        final View dialogView = inflater1.inflate(R.layout.dialog_relogin_user, null);

        final android.app.AlertDialog exitApp = new android.app.AlertDialog.Builder(getActivity(),R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
        exitApp.setCancelable(false);
        exitApp.setView(dialogView);

        Button btn_ok = dialogView.findViewById(R.id.btn_relogin_select_workplace);
        EditText et_login = dialogView.findViewById(R.id.et_login_relogin_user);
        EditText et_pass = dialogView.findViewById(R.id.et_password_relogin_user);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setmAuthMethod(et_login.getText().toString(),et_pass.getText().toString());
                exitApp.dismiss();
            }
        });

        exitApp.show();
    }

    private void setmAuthMethod (String login , String pass){
        String uri = getActivity().getSharedPreferences("Settings",MODE_PRIVATE).getString("URI",null);
        String install_id = getActivity().getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("InstallationID",null);

        CommandServices commandServices = ApiUtils.commandEposService(uri);

        Call<AuthentificateUserResult> call = commandServices.autentificateUser(install_id,login,pass);

        call.enqueue(new Callback<AuthentificateUserResult>() {
            @Override
            public void onResponse(Call<AuthentificateUserResult> call, Response<AuthentificateUserResult> response) {
                AuthentificateUserResult authentificateUserResult = response.body();
                if(authentificateUserResult != null){
                    TokenReceivedFromAutenficateUser token = authentificateUserResult.getAuthentificateUserResult();
                    if(token.getErrorCode() == 0){
                        getActivity().getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).edit().putString("Token",token.getToken()).apply();
                        String date = token.getTokenValidTo();
                        date = date.replace("/Date(","");
                        date = date.replace("+0200)/","");
                        long dateLong = Long.parseLong(date);
                        getActivity().getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).edit().putLong("TokenValidTo",dateLong).apply();

                        getSyncWorkplace(uri,token.getToken());

                    }
                    else{
                        AlertDialog.Builder dialog_user = new AlertDialog.Builder(getActivity());
                        dialog_user.setTitle("Atentie!");
                        dialog_user.setMessage("Eroare!Codul: " + token.getErrorCode());
                        dialog_user.setPositiveButton("Ok", (dialog, which) -> {
                            dialog.dismiss();
                        });
                        dialog_user.setNeutralButton("Oricum intra",(dialog,which) -> {

                        });
                        dialog_user.show();
                    }
                }
                else{
                    AlertDialog.Builder dialog_user = new AlertDialog.Builder(getActivity());
                    dialog_user.setTitle("Atentie!");
                    dialog_user.setMessage("Nu este raspuns de la serviciu!");
                    dialog_user.setPositiveButton("Ok", (dialog, which) -> {
                        dialog.dismiss();
                    });
                    dialog_user.setNeutralButton("Oricum intra",(dialog,which) -> {

                    });
                    dialog_user.show();
                }
            }

            @Override
            public void onFailure(Call<AuthentificateUserResult> call, Throwable t) {
                String err = t.getMessage();
            }
        });
    }
}
