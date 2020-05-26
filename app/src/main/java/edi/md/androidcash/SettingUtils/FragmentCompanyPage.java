package edi.md.androidcash.SettingUtils;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import edi.md.androidcash.BaseApplication;
import edi.md.androidcash.NetworkUtils.EposResult.AuthentificateUserResult;
import edi.md.androidcash.NetworkUtils.EposResult.GetWorkPlaceService;
import edi.md.androidcash.NetworkUtils.EposResult.GetWorkplacesResult;
import edi.md.androidcash.NetworkUtils.EposResult.TokenReceivedFromAutenficateUser;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.ApiUtils;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.CommandServices;
import edi.md.androidcash.NetworkUtils.WorkplaceEntry;
import edi.md.androidcash.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static edi.md.androidcash.BaseApplication.SharedPrefSettings;
import static edi.md.androidcash.BaseApplication.SharedPrefWorkPlaceSettings;

public class FragmentCompanyPage extends Fragment {
    private TextView mSelectWorkPlace;
    private List<WorkplaceEntry> workplaceEntryList = new ArrayList<>();

    private SharedPreferences sPrefWorkPlace, sPrefSettings;

    private ConstraintLayout csl_workplace;
    private ProgressDialog pgH;
    private LayoutInflater inflater2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootViewAdmin = inflater.inflate(R.layout.fragment_company_page, container, false);
        inflater2 = inflater;

        mSelectWorkPlace = rootViewAdmin.findViewById(R.id.txt_company_workplace);
        csl_workplace = rootViewAdmin.findViewById(R.id.csl_company_workplace);
        TextView companyName = rootViewAdmin.findViewById(R.id.txt_company_name);
        TextView idnoCompany = rootViewAdmin.findViewById(R.id.txt_company_idno);
        pgH = new ProgressDialog(getContext(),R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme);

        sPrefWorkPlace = getActivity().getSharedPreferences(SharedPrefWorkPlaceSettings, MODE_PRIVATE);
        sPrefSettings = getActivity().getSharedPreferences(SharedPrefSettings, MODE_PRIVATE);

        companyName.setText(sPrefSettings.getString("CompanyName",""));
        idnoCompany.setText(sPrefSettings.getString("IDNO",""));
        mSelectWorkPlace.setText(sPrefWorkPlace.getString("WorkPlaceName",""));


        csl_workplace.setOnClickListener(view -> {
            pgH.setMessage("loading workplace...");
            pgH.setIndeterminate(true);
            pgH.show();

            String uri = sPrefSettings.getString("URI",null);
            String token = sPrefSettings.getString("Token",null);
            getSyncWorkplace(uri,token);
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
                    if(result.getWorkplaces() != null){
                        workplaceEntryList = result.getWorkplaces();

                        ListAdapter adapterDialog = new ArrayAdapter<WorkplaceEntry>(getContext(), R.layout.item_workplace_main_dialog, workplaceEntryList) {

                            ViewHolder holder;

                            class ViewHolder {
                                TextView title;
                            }

                            public View getView(int position, View convertView, ViewGroup parent) {
                                if (convertView == null) {
                                    convertView = inflater2.inflate(R.layout.item_workplace_main_dialog, null);

                                    holder = new ViewHolder();
                                    holder.title = (TextView) convertView.findViewById(R.id.textView122);
                                    convertView.setTag(holder);
                                } else {
                                    // view already defined, retrieve view holder
                                    holder = (ViewHolder) convertView.getTag();
                                }
                                holder.title.setText(workplaceEntryList.get(position).getName());

                                return convertView;
                            }
                        };

                        String[] itemsWo = new String[result.getWorkplaces().size()];
                        String[] itemsWoID = new String[result.getWorkplaces().size()];
                        int i = 0;
                        for (WorkplaceEntry wo :result.getWorkplaces()) {
                            itemsWo[i] = wo.getName();
                            itemsWoID[i] = wo.getID();
                            i++;
                        }
                        pgH.dismiss();

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Select workplace item");
//                        builder.setAdapter(adapterDialog, (dialog, position) -> {
//                            sPrefWorkPlace.edit().putString("WorkPlaceID", workplaceEntryList.get(position).getID()).apply();
//                            sPrefWorkPlace.edit().putString("WorkPlaceName", workplaceEntryList.get(position).getName()).apply();
//                            mSelectWorkPlace.setText(workplaceEntryList.get(position).getName());
//                            dialog.dismiss();
//                        });
                        builder.setSingleChoiceItems(itemsWo, -1, (dialogInterface, position) -> {
                            sPrefWorkPlace.edit().putString("WorkPlaceID", itemsWoID[position]).apply();
                            sPrefWorkPlace.edit().putString("WorkPlaceName", itemsWo[position]).apply();
                            mSelectWorkPlace.setText(itemsWo[position]);
                            dialogInterface.dismiss();
                        });

                        builder.setNegativeButton("Cancel",(dialogInterface, posi) -> {
                           dialogInterface.dismiss();
                        });

                        AlertDialog alert = builder.create();
                        alert.setCanceledOnTouchOutside(false);
                        alert.show();

                    }
                }
                else if(errorecode == 405){
                    pgH.dismiss();
                    // не прав на просмотр рабочих мест
                    new MaterialAlertDialogBuilder(getContext(), R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                            .setTitle("Attention!")
                            .setMessage("No rights to view workplace! You want to enter other login?")
                            .setCancelable(false)
                            .setPositiveButton("YES", (dialogInterface, i) -> {

                                View dialogView = inflater2.inflate(R.layout.dialog_login_user, null);

                                AlertDialog reLogin = new AlertDialog.Builder(getContext(),R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
                                reLogin.setCancelable(false);
                                reLogin.setView(dialogView);

                                EditText LetUserName = dialogView.findViewById(R.id.et_login_user_form);
                                EditText LetPassword = dialogView.findViewById(R.id.et_password_login_user);
                                MaterialButton btnLogin = dialogView.findViewById(R.id.btn_login_user_form);

                                btnLogin.setOnClickListener(view -> {
                                    pgH.setMessage("loading...");
                                    pgH.setIndeterminate(true);
                                    pgH.show();

                                    authUserToServer(LetUserName.getText().toString(),LetPassword.getText().toString());
                                });
                                reLogin.show();

                                dialogInterface.dismiss();
                            })
                            .setNegativeButton("NO",(dialogInterface, i) -> {

                            })
                            .show();
                }
                else if(errorecode == 401){
                    //необходимо обновить токен
                    String login = BaseApplication.getInstance().getUser().getUserName();
                    String pass = BaseApplication.getInstance().getUserPasswordsNotHashed();

                    authUserToServer(login,pass);
                }
            }
            @Override
            public void onFailure(Call<GetWorkPlaceService> call, Throwable t) {

            }
        });
    }

    private void authUserToServer(String login , String pass){
        String uri = sPrefSettings.getString("URI",null);
        String install_id = sPrefSettings.getString("InstallationID",null);

        CommandServices commandServices = ApiUtils.commandEposService(uri);

        Call<AuthentificateUserResult> call = commandServices.autentificateUser(install_id,login,pass);

        call.enqueue(new Callback<AuthentificateUserResult>() {
            @Override
            public void onResponse(Call<AuthentificateUserResult> call, Response<AuthentificateUserResult> response) {
                AuthentificateUserResult authentificateUserResult = response.body();
                if(authentificateUserResult != null){
                    TokenReceivedFromAutenficateUser token = authentificateUserResult.getAuthentificateUserResult();
                    if(token.getErrorCode() == 0){
                        sPrefSettings.edit().putString("Token",token.getToken()).apply();
                        String date = token.getTokenValidTo();
                        date = date.replace("/Date(","");
                        date = date.replace("+0200)/","");
                        long dateLong = Long.parseLong(date);
                        sPrefSettings.edit().putLong("TokenValidTo",dateLong).apply();

                        getSyncWorkplace(uri,token.getToken());
                    }
                    else{
                        AlertDialog.Builder dialog_user = new AlertDialog.Builder(getContext());
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
                    AlertDialog.Builder dialog_user = new AlertDialog.Builder(getContext());
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
