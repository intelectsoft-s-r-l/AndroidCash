package edi.md.androidcash.SettingUtils.Preference;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import edi.md.androidcash.BaseApplication;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.ApiUtils;
import edi.md.androidcash.NetworkUtils.AssortmentServiceEntry;
import edi.md.androidcash.NetworkUtils.EposResult.AssortmentListService;
import edi.md.androidcash.NetworkUtils.EposResult.GetAssortmentListResult;
import edi.md.androidcash.NetworkUtils.EposResult.GetUsersListResult;
import edi.md.androidcash.NetworkUtils.EposResult.GetWorkplaceSettingsResult;
import edi.md.androidcash.NetworkUtils.EposResult.UserListServiceResult;
import edi.md.androidcash.NetworkUtils.EposResult.WorkPlaceSettings;
import edi.md.androidcash.NetworkUtils.FiscalDevice;
import edi.md.androidcash.NetworkUtils.PaymentType;
import edi.md.androidcash.NetworkUtils.Promotion;
import edi.md.androidcash.NetworkUtils.QuickGroup;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.CommandServices;
import edi.md.androidcash.NetworkUtils.User;
import edi.md.androidcash.R;
import edi.md.androidcash.RealmHelper.AssortmentRealm;
import edi.md.androidcash.RealmHelper.Barcodes;
import edi.md.androidcash.RealmHelper.QuickGroupRealm;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static edi.md.androidcash.BaseApplication.SharedPrefSettings;
import static edi.md.androidcash.BaseApplication.SharedPrefSyncSettings;
import static edi.md.androidcash.BaseApplication.SharedPrefWorkPlaceSettings;

public class SyncPageSet extends Fragment {

    private ProgressDialog pgH;
    private TextView mLastSync;
    private Switch startUpSyncSwitch,autoSyncSwitch;
    private Spinner timeAutoUpdateSpinner;
    ArrayAdapter<String> adapterUpdate;

    private long startDownloadAssortment,startDownloadUser,startDownloadWorkPlaceSet,endDownloadAssortment,endDownloadUser,endDownloadWorkPlaceSet,endInsertInDB;
    private int countAssortment = 0,countPayT = 0,countUsers = 0;
    private int MESSAGE_SUCCES = 0,MESSAGE_ERROR = 1,MESSAGE_FAILURE = 2;
    private static final String[] intervalUpdate = {"Nu este ales","30 min", "1 ora", "3 ora"};
    private int positionFromUpdate;
    private boolean updateAuto, updateToStart;
    String token, workplaceId;
    String uri;

    private Realm mRealm;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootViewAdmin = inflater.inflate(R.layout.fragment_sync_page, container, false);

        Button mSync = rootViewAdmin.findViewById(R.id.btn_synchonize);
        mLastSync = rootViewAdmin.findViewById(R.id.txt_last_synchronize);
        pgH = new ProgressDialog(getActivity());
        autoSyncSwitch = rootViewAdmin.findViewById(R.id.switch_enable_autosync);
        startUpSyncSwitch = rootViewAdmin.findViewById(R.id.switch_synchronization_startup);
        timeAutoUpdateSpinner = rootViewAdmin.findViewById(R.id.spinner);
        timeAutoUpdateSpinner.setEnabled(false);

        mRealm = Realm.getDefaultInstance();

        uri = getActivity().getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("URI",null);
        token = getActivity().getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("Token","null");

        mLastSync.setText("The latest synchronization was: " + getActivity().getSharedPreferences(SharedPrefSyncSettings, MODE_PRIVATE).getString("LastSync"," "));
        updateAuto = getActivity().getSharedPreferences(SharedPrefSyncSettings, MODE_PRIVATE).getBoolean("AutoSync",false);
        updateToStart =  getActivity().getSharedPreferences(SharedPrefSyncSettings, MODE_PRIVATE).getBoolean("SyncToStart",false);
        positionFromUpdate =  getActivity().getSharedPreferences(SharedPrefSyncSettings, MODE_PRIVATE).getInt("positionFromAutoUpdate",0);

        adapterUpdate = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, intervalUpdate);
        adapterUpdate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeAutoUpdateSpinner.setAdapter(adapterUpdate);

        timeAutoUpdateSpinner.setSelection(positionFromUpdate);

        if(updateToStart)
            startUpSyncSwitch.setChecked(true);
        if(updateAuto){
            timeAutoUpdateSpinner.setEnabled(true);
            autoSyncSwitch.setChecked(true);
        }

        mSync.setOnClickListener(v -> {
            mRealm.executeTransaction(realm -> realm.deleteAll());

            workplaceId = getActivity().getSharedPreferences(SharedPrefWorkPlaceSettings, MODE_PRIVATE).getString("WorkPlaceID", null);
            if(workplaceId != null) {
                new AssortmentTask().execute();
            }
        });

        startUpSyncSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
            if(checked)
                getActivity().getSharedPreferences(SharedPrefSyncSettings, MODE_PRIVATE).edit().putBoolean("SyncToStart",true).apply();
            else
                getActivity().getSharedPreferences(SharedPrefSyncSettings, MODE_PRIVATE).edit().putBoolean("SyncToStart",false).apply();
        });

//        autoSyncSwitch.setOnCheckedChangeListener(((compoundButton, checked) -> {
//            if(checked){
//                getActivity().getSharedPreferences(SharedPrefSyncSettings, MODE_PRIVATE).edit().putBoolean("AutoSync",true).apply();
//                timeAutoUpdateSpinner.setEnabled(true);
//            }
//            else{
//                getActivity().getSharedPreferences(SharedPrefSyncSettings, MODE_PRIVATE).edit().putBoolean("AutoSync",false).apply();
//                timeAutoUpdateSpinner.setEnabled(false);
//            }
//        }));
//
//        timeAutoUpdateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                getActivity().getSharedPreferences(SharedPrefSyncSettings, MODE_PRIVATE).edit().putInt("positionFromAutoUpdate",i).apply();
//                switch (i){
//                    case 0:{
//                        getActivity().getSharedPreferences(SharedPrefSyncSettings, MODE_PRIVATE).edit().putInt("intervalForAutoUpdate",0).apply();
//                    }break;
//                    case 1:{
//                        getActivity().getSharedPreferences(SharedPrefSyncSettings, MODE_PRIVATE).edit().putInt("intervalForAutoUpdate",60000).apply();
//                        BaseApplication.getInstance().autoUpdateAssortment();
//                    }break;
//                    case 2:{
//                        getActivity().getSharedPreferences(SharedPrefSyncSettings, MODE_PRIVATE).edit().putInt("intervalForAutoUpdate",180000).apply();
//                        BaseApplication.getInstance().autoUpdateAssortment();
//                    }break;
//                    case 3:{
//                        getActivity().getSharedPreferences(SharedPrefSyncSettings, MODE_PRIVATE).edit().putInt("intervalForAutoUpdate",240000).apply();
//                        BaseApplication.getInstance().autoUpdateAssortment();
//                    }break;
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });



        return rootViewAdmin;
    }

    private void readAssortment (final Call<AssortmentListService> assortiment){
                assortiment.enqueue(new Callback<AssortmentListService>() {
                    @Override
                    public void onResponse(Call<AssortmentListService> call, Response<AssortmentListService> response) {
                        endDownloadAssortment = new Date().getTime();
                        AssortmentListService assortiment_body = response.body();
                        GetAssortmentListResult result = assortiment_body != null ? assortiment_body.getGetAssortmentListResult() : null;

                        int errorecode = 101;
                        if (result != null) {
                            errorecode = result.getErrorCode();
                        }
                        if(errorecode == 0){
                            List<AssortmentServiceEntry> assortmentListData = result.getAssortments();

                            countAssortment = assortmentListData.size();

                            mRealm.executeTransaction(realm -> {

                               for(AssortmentServiceEntry assortmentServiceEntry: assortmentListData){
                                   AssortmentRealm ass = new AssortmentRealm();

                                   RealmList<Barcodes> listBarcode = new RealmList<>();
                                   RealmList<Promotion> listPromotion = new RealmList<>();

                                   if(assortmentServiceEntry.getBarcodes() != null){
                                       for(String barcodes : assortmentServiceEntry.getBarcodes()){
                                           Barcodes barcodes1 = new Barcodes();
                                           barcodes1.setBar(barcodes);
                                           listBarcode.add(barcodes1);
                                       }
                                   }
                                   if(assortmentServiceEntry.getPromotions()!= null){
                                       listPromotion.addAll(assortmentServiceEntry.getPromotions());
                                   }
                                   ass.setId(assortmentServiceEntry.getID());
                                   ass.setName(assortmentServiceEntry.getName());
                                   ass.setBarcodes(listBarcode);
                                   ass.setFolder(assortmentServiceEntry.getIsFolder());
                                   ass.setPromotions(listPromotion);
                                   ass.setAllowDiscounts(assortmentServiceEntry.getAllowDiscounts());
                                   ass.setAllowNonInteger(assortmentServiceEntry.getAllowNonInteger());
                                   ass.setCode(assortmentServiceEntry.getCode());
                                   ass.setEnableSaleTimeRange(assortmentServiceEntry.getEnableSaleTimeRange());
                                   ass.setMarking(assortmentServiceEntry.getMarking());
                                   ass.setParentID(assortmentServiceEntry.getParentID());
                                   ass.setPrice(assortmentServiceEntry.getPrice());
                                   ass.setPriceLineId(assortmentServiceEntry.getPriceLineId());
                                   ass.setShortName(assortmentServiceEntry.getShortName());
                                   ass.setVat(assortmentServiceEntry.getVAT());
                                   ass.setUnit(assortmentServiceEntry.getUnit());
                                   ass.setQuickButtonNumber(assortmentServiceEntry.getQuickButtonNumber());
                                   ass.setQuickGroupName(assortmentServiceEntry.getQuickGroupName());
                                   ass.setStockBalance(assortmentServiceEntry.getStockBalance());
                                   ass.setStockBalanceDate(assortmentServiceEntry.getStockBalanceDate());
                                   ass.setSaleStartTime(replaceDate(assortmentServiceEntry.getSaleStartTime()));
                                   ass.setSaleEndTime(replaceDate(assortmentServiceEntry.getSaleEndTime()));
                                   ass.setPriceLineStartDate(replaceDate(assortmentServiceEntry.getPriceLineStartDate()));
                                   ass.setPriceLineEndDate(replaceDate(assortmentServiceEntry.getPriceLineEndDate()));

                                   realm.insert(ass);
                               }

                               if(result.getQuickGroups() != null){
                                   for(QuickGroup quickGroup : result.getQuickGroups()){
                                       QuickGroupRealm quickGroupRealm = new QuickGroupRealm();

                                       String nameGroup = quickGroup.getName();
                                       RealmList<String> assortment = new RealmList<>();
                                       assortment.addAll(quickGroup.getAssortmentID());

                                       quickGroupRealm.setGroupName(nameGroup);
                                       quickGroupRealm.setAssortmentId(assortment);

                                       realm.insert(quickGroupRealm);
                                   }
                               }

                            });

                            endInsertInDB = new Date().getTime();
                            startDownloadUser = new Date().getTime();
                            new UserTask().execute();

                        }else{
                            mHandlerBills.obtainMessage(MESSAGE_ERROR,errorecode + "Assortment download").sendToTarget();
                        }
                    }
                    @Override
                    public void onFailure(Call<AssortmentListService> call, Throwable t) {
                        mHandlerBills.obtainMessage(MESSAGE_FAILURE,t.getMessage()).sendToTarget();
                    }
                });
    }

    private void readWorkPlaceSettings(Call<WorkPlaceSettings> workPlaceSettingsCall){
        workPlaceSettingsCall.enqueue(new Callback<WorkPlaceSettings>() {
                    @Override
                    public void onResponse(Call<WorkPlaceSettings> call, Response<WorkPlaceSettings> response) {
                        WorkPlaceSettings workPlaceSettings = response.body();

                        GetWorkplaceSettingsResult result = workPlaceSettings != null ? workPlaceSettings.getGetWorkplaceSettingsResult() : null;

                        int errorecode = 101;
                        if (result != null) {
                            errorecode = result.getErrorCode();
                        }
                        if(errorecode == 0){

                            if(result.getPaymentTypes() != null){
                                List<PaymentType> paymentTypes = result.getPaymentTypes();
                                countPayT = paymentTypes.size();

                                for(PaymentType paymentType : paymentTypes){
                                    mRealm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            realm.insert(paymentType);
                                        }
                                    });

                                }
                            }
                            if( result.getFiscalDevice() != null){
                                FiscalDevice fiscalDevice = result.getFiscalDevice();
                                mRealm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        realm.insert(fiscalDevice);
                                    }
                                });
                            }

                            endDownloadWorkPlaceSet = new Date().getTime();
                            mHandlerBills.obtainMessage(MESSAGE_SUCCES).sendToTarget();

                        }else{
                            mHandlerBills.obtainMessage(MESSAGE_ERROR,errorecode).sendToTarget();
                        }
                    }
                    @Override
                    public void onFailure(Call<WorkPlaceSettings> call, Throwable t) {
                        mHandlerBills.obtainMessage(MESSAGE_FAILURE,t.getMessage()).sendToTarget();
                    }
                });

    }

    private void readUsers(Call<UserListServiceResult> userListServiceResultCall){
        userListServiceResultCall.enqueue(new Callback<UserListServiceResult>() {
            @Override
            public void onResponse(Call<UserListServiceResult> call, Response<UserListServiceResult> response) {
                UserListServiceResult userListServiceResult = response.body();
                GetUsersListResult result = userListServiceResult != null ? userListServiceResult.getGetUsersListResult() : null;

                int errorecode = 101;
                if (result != null) {
                    errorecode = result.getErrorCode();
                }
                if(errorecode == 0){
                    List<User> users = result.getUsers();

                    countUsers = users.size();
                    mRealm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            for(User user : users){
                                realm.insert(user);
                            }
                        }
                    });
                    endDownloadUser = new Date().getTime();
                    startDownloadWorkPlaceSet = new Date().getTime();
                    new WorkPlaceTask().execute();

                }else{
                    mHandlerBills.obtainMessage(MESSAGE_ERROR,errorecode).sendToTarget();
                }
            }
            @Override
            public void onFailure(Call<UserListServiceResult> call, Throwable t) {
                mHandlerBills.obtainMessage(MESSAGE_FAILURE,t.getMessage()).sendToTarget();
            }
        });

    }

    private final Handler mHandlerBills = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            pgH.dismiss();
            if (msg.what == MESSAGE_SUCCES) {
                mRealm.close();

                SimpleDateFormat sdfChisinau = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                TimeZone tzInChisinau = TimeZone.getTimeZone("Europe/Chisinau");
                sdfChisinau.setTimeZone(tzInChisinau);
                Date finalSync = new Date();
                mLastSync.setText("The latest synchronization was: " + sdfChisinau.format(finalSync));

                SharedPreferences sPref_SYNC = getContext().getSharedPreferences("SyncSetting", MODE_PRIVATE);
                SharedPreferences.Editor sPref =  sPref_SYNC.edit();
                sPref.putString("LastSync",sdfChisinau.format(finalSync));
                sPref.apply();

                AlertDialog.Builder failureAsl = new AlertDialog.Builder(getActivity());
                failureAsl.setTitle("Atentie!");
                failureAsl.setMessage(countAssortment + " pozitii de asortiment descarcate si inscrise\n" + "Inceput descrc: " + sdfChisinau.format(startDownloadAssortment) + "\n"
                        + "Sfirsit descarc: " + sdfChisinau.format(endDownloadAssortment) + "\n" + "Inscrise baza: " + sdfChisinau.format(endInsertInDB)
                        + "Setarile la " + countPayT + " casa descarcate si inscrise\n" + "Inceput:" + sdfChisinau.format(startDownloadWorkPlaceSet) + "\n" + "Sfirsit: " + sdfChisinau.format(endDownloadWorkPlaceSet) + "\n" +
                        countUsers + "utilizatori descarcati si inscrisi\n " + "Inceput:" + sdfChisinau.format(startDownloadUser) + "\n" + "Sfirsit: " + sdfChisinau.format(endDownloadUser) + "\n");
                failureAsl.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mRealm.beginTransaction();
                        RealmResults<AssortmentRealm> books = mRealm.where(AssortmentRealm.class).equalTo("isFolder",true).findAll();
                        if(!books.isEmpty()) {
                            for(int si = books.size() - 1; si >= 0; si--) {
                                String codes = books.get(si).getCode();
                                String name = books.get(si).getName();
                            }
                        }
                        mRealm.commitTransaction();
                    }
                });
                failureAsl.show();
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

    class AssortmentTask extends AsyncTask<Void,Void,Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pgH.setMessage("Incarcare assortiment...");
            pgH.setIndeterminate(true);
            pgH.show();
        }

        @Override
        protected Void doInBackground(Void... dates) {
            String uri = getActivity().getSharedPreferences("Settings",MODE_PRIVATE).getString("URI",null);
            CommandServices commandServices = ApiUtils.commandEposService(uri);

            final Call<AssortmentListService> assortiment = commandServices.getAssortiment(token, workplaceId);
            startDownloadAssortment = new Date().getTime();
            readAssortment(assortiment);
            return null;
        }
    }
    class UserTask extends AsyncTask<Void,Void,Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pgH.dismiss();
            pgH.setMessage("Incarcare utilizatori...");
            pgH.setIndeterminate(true);
            pgH.show();
        }

        @Override
        protected Void doInBackground(Void... dates) {
            String uri = getActivity().getSharedPreferences("Settings",MODE_PRIVATE).getString("URI",null);
            CommandServices commandServices = ApiUtils.commandEposService(uri);

            final Call<UserListServiceResult> userListServiceResultCall = commandServices.getUsers(token, workplaceId);
            readUsers(userListServiceResultCall);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


        }
    }
    class WorkPlaceTask extends AsyncTask<Void,Void,Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pgH.dismiss();
            pgH.setMessage("Incarcare setarilor...");
            pgH.setIndeterminate(true);
            pgH.show();
        }

        @Override
        protected Void doInBackground(Void... dates) {
            String uri = getActivity().getSharedPreferences("Settings",MODE_PRIVATE).getString("URI",null);
            CommandServices commandServices = ApiUtils.commandEposService(uri);

            final Call<WorkPlaceSettings> workPlaceSettingsCall = commandServices.getWorkplaceSettings(token, workplaceId);
            readWorkPlaceSettings(workPlaceSettingsCall);
            return null;
        }

    }

    private long replaceDate(String date){
        if(date !=null ){
            date = date.replace("/Date(","");
            date = date.replace("+0200)/","");
            date = date.replace("+0300)/","");
            return Long.parseLong(date);
        }
        else
            return 0;

    }
}
