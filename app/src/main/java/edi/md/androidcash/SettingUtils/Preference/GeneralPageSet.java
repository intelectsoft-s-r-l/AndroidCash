package edi.md.androidcash.SettingUtils.Preference;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.concurrent.TimeUnit;

import edi.md.androidcash.NetworkUtils.EposResult.GetWorkPlaceService;
import edi.md.androidcash.NetworkUtils.EposResult.GetWorkplacesResult;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.ServiceWorkplace;
import edi.md.androidcash.NetworkUtils.WorkplaceEntry;
import edi.md.androidcash.R;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;
import static edi.md.androidcash.GlobalVariables.SharedPrefWorkPlaceSettings;

public class GeneralPageSet extends Fragment {
    Spinner mSelectWorkPlace,mAuthMethod;
    TextView companyName, idnoCompany;
    int MESSAGE_SUCCES = 0,MESSAGE_ERROR = 1,MESSAGE_FAILURE = 2;
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

        companyName.setText(sPrefWorkPlace.getString("CompanyName",""));
        idnoCompany.setText(sPrefWorkPlace.getString("IDNO",""));

        adapterAuth = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, authStrings);
        adapterAuth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAuthMethod.setAdapter(adapterAuth);

        mAuthMethod.setSelection(pos);

        String uri = getActivity().getSharedPreferences("Settings",MODE_PRIVATE).getString("URI",null);

        getSyncWorkplace(uri);

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
    private void getSyncWorkplace(final String uri){
        final Thread getASL = new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(8, TimeUnit.MINUTES)
                        .readTimeout(6, TimeUnit.MINUTES)
                        .writeTimeout(8, TimeUnit.MINUTES)
                        .build();
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://"+ uri)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(okHttpClient)
                        .build();
                ServiceWorkplace workplace_API = retrofit.create(ServiceWorkplace.class);
                final Call<GetWorkPlaceService> workplace = workplace_API.getWorkplace("9cd98ecf-6ce5-4f6a-b828-8bf526a125a6");
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

                        }else{
                            mHandlerBills.obtainMessage(MESSAGE_ERROR,errorecode).sendToTarget();
                        }
                    }
                    @Override
                    public void onFailure(Call<GetWorkPlaceService> call, Throwable t) {
                        mHandlerBills.obtainMessage(MESSAGE_FAILURE,t.getMessage()).sendToTarget();
                    }
                });
            }
        });
        getASL.start();
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
        }
    };
}
