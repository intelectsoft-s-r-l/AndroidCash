package md.intelectsoft.salesepos;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import md.intelectsoft.salesepos.DatcesNewFile.PrinterManager;
import md.intelectsoft.salesepos.NetworkUtils.BrokerResultBody.Body.SendGetURI;
import md.intelectsoft.salesepos.NetworkUtils.BrokerResultBody.Body.SendRegisterApplication;
import md.intelectsoft.salesepos.NetworkUtils.BrokerResultBody.Results.AppDataRegisterApplication;
import md.intelectsoft.salesepos.NetworkUtils.BrokerResultBody.Results.RegisterApplication;
import md.intelectsoft.salesepos.NetworkUtils.EposResult.AuthentificateUserResult;
import md.intelectsoft.salesepos.NetworkUtils.EposResult.TokenReceivedFromAutenficateUser;
import md.intelectsoft.salesepos.NetworkUtils.RetrofitRemote.ApiUtils;
import md.intelectsoft.salesepos.NetworkUtils.RetrofitRemote.BrokerRetrofitClient;
import md.intelectsoft.salesepos.NetworkUtils.RetrofitRemote.BrokerServiceAPI;
import md.intelectsoft.salesepos.NetworkUtils.RetrofitRemote.CommandServices;
import md.intelectsoft.salesepos.NetworkUtils.User;
import md.intelectsoft.salesepos.Utils.BaseEnum;
import md.intelectsoft.salesepos.Utils.BrokerServiceEnum;
import md.intelectsoft.salesepos.Utils.LocaleHelper;
import md.intelectsoft.salesepos.Utils.Rfc2898DerivesBytes;
import md.intelectsoft.salesepos.Utils.UpdateHelper;
import md.intelectsoft.salesepos.Utils.UpdateInformation;
import md.intelectsoft.salesepos.connectors.AbstractConnector;
import md.intelectsoft.salesepos.connectors.UsbDeviceConnector;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static md.intelectsoft.salesepos.BaseApplication.SharedPrefSettings;
import static md.intelectsoft.salesepos.BaseApplication.SharedPrefWorkPlaceSettings;

public class StartedActivity extends AppCompatActivity implements UpdateHelper.OnUpdateCheckListener { //
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1, DATECS_USB_VID = 65520, FTDI_USB_VID = 1027;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private PendingIntent mPermissionIntent;
    private UsbManager mManager;

    //NFC variables
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];

    //Reader ACR
    private Reader mReader;
    int fiscalMode;

    //layout with authentificate forms
    ConstraintLayout layoutRegister, layoutLogin, layoutPassword, layoutCard;

    //views register device form
    TextInputLayout layoutCode;
    TextInputEditText inputCode;
    MaterialButton btnRegister;
    ProgressBar RpgBar;

    //views login user form
    TextInputLayout inputLayoutLogin;
    TextInputEditText inputEditTextLogin;
    TextInputLayout inputLayoutPasswordLogin;
    TextInputEditText inputEditTextPasswordLogin;
    MaterialButton btnLogin;
    ProgressBar Lpgbar;

    //format date and time zone
    SimpleDateFormat simpleDateFormat;
    TimeZone timeZone;

    private ProgressDialog pgH;

    String tokenId;

    private SharedPreferences sharedPreferencesSettings;

    private Realm mRealm;
    private String androidID, deviceName, publicIp, privateIp, deviceSN, osVersion, deviceModel;

    BrokerServiceAPI brokerServiceAPI;
    Context context;

    @Override
    public void onUpdateCheckListener(UpdateInformation information) {
        boolean update = information.isUpdate();
        boolean updateTrial = information.isUpdateTrial();

        boolean autoUpDate = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getBoolean("autoUpdate",false);
        boolean autoUpDateTrial = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getBoolean("autoUpdateTrial",false);

        if(autoUpDateTrial){
            if(updateTrial && !information.getNewVersionTrial().equals(information.getCurrentVersion())){
                android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme)
                        .setTitle("New trial version " + information.getNewVersionTrial() + " available")
                        .setMessage("Please update to new trial version to continue use.Current version: " + information.getCurrentVersion())
                        .setPositiveButton("UPDATE",(dialogInterface, i) -> {
                            pgH.setMessage("download new trial version...");
                            pgH.setIndeterminate(true);
                            pgH.show();
                            downloadAndInstallTrialApk(information.getUrlTrial());
                        })
                        .setNegativeButton("No,thanks", (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                        })
                        .create();
                alertDialog.show();
            }
        }
        else{
            if(autoUpDate){
                if(update && !information.getNewVerion().equals(information.getCurrentVersion())){
                    android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme)
                            .setTitle("New version " + information.getNewVerion() + " available")
                            .setMessage("Please update to new version to continue use.Current version: " + information.getCurrentVersion())
                            .setPositiveButton("UPDATE",(dialogInterface, i) -> {
                                pgH.setMessage("download new version...");
                                pgH.setIndeterminate(true);
                                pgH.show();
                                downloadAndInstallApk(information.getUrl());
                            })
                            .setNegativeButton("No,thanks", (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            })
                            .create();
                    alertDialog.show();
                }
            }
        }
    }

    private void downloadAndInstallApk(String url){
        //get destination to update file and set Uri
        //TODO: First I wanted to store my update .apk file on internal storage for my app but apparently android does not allow you to open and install
        //aplication with existing package from there. So for me, alternative solution is Download directory in external storage. If there is better

        String destination = Environment.getExternalStorageDirectory()+ "/IntelectSoft";
        String fileName = "/cash.apk";
        destination += fileName;
        final Uri uri = Uri.parse("file://" + destination);

        //Delete update file if exists
        File file = new File(destination);
        if (file.exists())
            //file.delete() - test this, I think sometimes it doesnt work
            file.delete();

        //set download manager
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Download new final version...");
        request.setTitle("SalesEPOS update");

        //set destination
        request.setDestinationUri(uri);

        // get download service and enqueue file
        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = manager.enqueue(request);

        //set BroadcastReceiver to install app when .apk is downloaded
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                pgH.dismiss();
                File file = new File(Environment.getExternalStorageDirectory()+ "/IntelectSoft","/cash.apk"); // mention apk file path here

                Uri uri = FileProvider.getUriForFile(StartedActivity.this, "md.intelectsoft.salesepos.provider",file);
                if(file.exists()){
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setDataAndType(uri, "application/vnd.android.package-archive");
                    install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(install);
                }
                unregisterReceiver(this);
                finish();

            }
        };
        //register receiver for when .apk download is compete
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
    private void downloadAndInstallTrialApk(String url){
        //get destination to update file and set Uri
        //TODO: First I wanted to store my update .apk file on internal storage for my app but apparently android does not allow you to open and install
        //aplication with existing package from there. So for me, alternative solution is Download directory in external storage. If there is better

        String destination = Environment.getExternalStorageDirectory()+ "/IntelectSoft";
        String fileName = "/cash_trial.apk";
        destination += fileName;
        final Uri uri = Uri.parse("file://" + destination);

        //Delete update file if exists
        File file = new File(destination);
        if (file.exists())
            //file.delete() - test this, I think sometimes it doesnt work
            file.delete();

        //set download manager
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Download trial version...");
        request.setTitle("SalesEPOS update");

        //set destination
        request.setDestinationUri(uri);

        // get download service and enqueue file
        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = manager.enqueue(request);

        //set BroadcastReceiver to install app when .apk is downloaded
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                pgH.dismiss();
                File file = new File(Environment.getExternalStorageDirectory()+ "/IntelectSoft","/cash_trial.apk"); // mention apk file path here

                Uri uri = FileProvider.getUriForFile(StartedActivity.this, "md.intelectsoft.salesepos.provider",file);
                if(file.exists()){
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setDataAndType(uri, "application/vnd.android.package-archive");
                    install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(install);
                }
                unregisterReceiver(this);
                finish();

            }
        };
        //register receiver for when .apk download is compete
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private class OpenTask extends AsyncTask<UsbDevice, Void, Exception> {
        @Override
        protected Exception doInBackground(UsbDevice... params) {

            Exception result = null;

            try {
                mReader.open(params[0]);
            } catch (Exception e) {
                result = e;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Exception result) {
            if (result != null) {
                fail(result.toString());
            }
            else {
                fail("Reader: " + mReader.getReaderName());
            }
        }
    }
    private class CloseTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            mReader.close();
            return null;
        }

    }
    private class TransmitParams {
        public byte[] command;
        public int slotNum;
        public int controlCode;
    }
    private class TransmitProgress {
        public byte[] command;
        public int commandLength;
        public byte[] response;
        public int responseLength;
        public Exception e;
    }
    private class TransmitTask extends AsyncTask<TransmitParams, Void, TransmitProgress> {

        @Override
        protected TransmitProgress doInBackground(TransmitParams... params) {
            TransmitProgress progress = new TransmitProgress();
            int responseLength = 0;

            byte[] responses = new byte[300];

            try {
                responseLength = mReader.control(0,3500, params[0].command, params[0].command.length, responses, responses.length);

                progress.command = params[0].command;
                progress.commandLength = params[0].command.length;
                progress.response = responses;
                progress.responseLength = responseLength;
                progress.e = null;
            }
            catch (Exception e) {
                progress.command = null;
                progress.commandLength = 0;
                progress.response = null;
                progress.responseLength = 0;
                progress.e = e;
            }
            return progress;
        }

        @Override
        protected void onPostExecute(TransmitProgress transmitProgress) {
            if (transmitProgress.e == null) {
                StringBuilder sb = new StringBuilder();

                for (byte page :  transmitProgress.response) {
                    int b = page & 0xff;
                    if (b < 0x10)
                        sb.append("");
                    sb.append(b);
                }
                fail("sb " + sb.toString());

                String cardCode = getMD5HashCardCode(sb.toString());

//                if(frame_card.getVisibility() == View.VISIBLE){
//                    Realm realms = Realm.getDefaultInstance();
//                    realms.executeTransaction(realm -> {
//                        User userCard = realm.where(User.class).equalTo("cardBarcode",cardCode).findFirst();
//
//                        if(userCard != null){
//                            User authentificateUser = realm.copyFromRealm(userCard);
//                            Intent main = new Intent(StartedActivity.this,MainActivity.class);
//                            ((BaseApplication)getApplication()).setUser(authentificateUser);
//                            startActivity(main);
//                            finish();
//                        }
//                    });
//                }
            }
            else{
                fail(transmitProgress.e.getMessage());
            }
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if(fiscalMode == BaseEnum.FISCAL_DEVICE){
                        if (device.getManufacturerName().equals("Datecs")) {
                            BaseApplication.getInstance().setDeviceFiscal(device);
                            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                                AbstractConnector connector = new UsbDeviceConnector(StartedActivity.this, mManager, device);
                                HashMap<String, UsbDevice> deviceList = mManager.getDeviceList();
                                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                                while (deviceIterator.hasNext()) {
                                    UsbDevice devices = deviceIterator.next();
                                    if (devices.getManufacturerName().equals("ACS")) {
                                        if (!mManager.hasPermission(devices)) {
                                            mManager.requestPermission(devices, mPermissionIntent);
                                        }
                                    }
                                }
                                deviceConnect(connector);
                            }
                        }
                    }

                    if (device.getManufacturerName().equals("ACS")) {
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            new OpenTask().execute(device);

                            HashMap<String, UsbDevice> deviceList = mManager.getDeviceList();

                            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                            while (deviceIterator.hasNext()) {
                                UsbDevice devices = deviceIterator.next();

                                if ((devices.getVendorId() == DATECS_USB_VID) || (devices.getVendorId() == FTDI_USB_VID) && (devices.getManufacturerName().equals("Datecs"))) {
                                    if (!mManager.hasPermission(devices)) {
                                        mManager.requestPermission(devices, mPermissionIntent);
                                    }
                                }
                            }

                        }
                    }
                }

            }
            else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (device != null && device.equals(mReader.getDevice())) {
                        new CloseTask().execute();
                    }
                }
            }
            else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                for (UsbDevice device : mManager.getDeviceList().values()) {
                    if(fiscalMode == BaseEnum.FISCAL_DEVICE){
                        if (device.getManufacturerName().equals("Datecs")) {
                            if(mManager.hasPermission(device)){
                                AbstractConnector connector = new UsbDeviceConnector(StartedActivity.this, mManager, device);
                                HashMap<String, UsbDevice> deviceList = mManager.getDeviceList();
                                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                                while (deviceIterator.hasNext()) {
                                    UsbDevice devices = deviceIterator.next();
                                    if (devices.getManufacturerName().equals("ACS")) {
                                        if (!mManager.hasPermission(devices)) {
                                            mManager.requestPermission(devices, mPermissionIntent);
                                        }
                                    }
                                }
                                deviceConnect(connector);
                            }
                            else
                                mManager.requestPermission(device, mPermissionIntent);
                        }
                    }
                    if (device.getManufacturerName().equals("ACS")) {
                        // Request permission
                        mManager.requestPermission(device, mPermissionIntent);
                    }

                }

            }
        }
    };

    @SuppressLint("ObsoleteSdkInt")
    private void setAppLocale(String localeCode){
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR1){
            config.setLocale(new Locale(localeCode.toLowerCase()));
        } else {
            config.locale = new Locale(localeCode.toLowerCase());
        }
        resources.updateConfiguration(config, dm);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get USB manager
        mManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        // Register receiver for USB permission
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        fiscalMode = getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getInt("ModeFiscalWork", 0);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        registerReceiver(mReceiver, filter);
//-------------------------------------------------------------------------------


//        int langInt = getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getInt("Language",2);

        String lang = LocaleHelper.getLanguage(this);

        setAppLocale(lang);

        setContentView(R.layout.activity_start);
        //init constraint layouts with authentificate form
        layoutRegister = findViewById(R.id.csl_register_form);
        layoutLogin = findViewById(R.id.csl_login_form);

        //init UI view register form
        layoutCode = findViewById(R.id.layoutCode);
        inputCode = findViewById(R.id.inputCode);
        RpgBar = findViewById(R.id.progressBar_register);
        btnRegister = findViewById(R.id.btn_register_device);

        //init UI view login form
        inputLayoutLogin = findViewById(R.id.layoutLogin);
        inputLayoutPasswordLogin = findViewById(R.id.layoutPasswordLogin);
        inputEditTextLogin = findViewById(R.id.inputLogin);
        inputEditTextPasswordLogin = findViewById(R.id.inputPasswordLogin);
        Lpgbar = findViewById(R.id.progressBar_login_form);
        btnLogin = findViewById(R.id.btn_login_user_form);

        //--------------------------------------- check update app version ------------------
        boolean autoUpDate = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getBoolean("autoUpdate",false);
        boolean autoUpDateTrial = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getBoolean("autoUpdateTrial",false);
        if(autoUpDateTrial || autoUpDate)
            UpdateHelper.with(this).onUpdateCheck(this).check();

        context = this;
        pgH = new ProgressDialog(this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme);
        sharedPreferencesSettings = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE);
        mRealm = Realm.getDefaultInstance();

        //check if it device support NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null)
            Toast.makeText(this, R.string.message_device_not_support_NFC, Toast.LENGTH_LONG).show();

        //ask necessary permisions
        AskForPermissions();

        // Initialize reader ACR (ACS 122 U)
        mReader = new Reader(mManager);
        mReader.setOnStateChangeListener((slotNum, prevState, currState) -> {
            if (prevState < Reader.CARD_UNKNOWN || prevState > Reader.CARD_SPECIFIC) {
                prevState = Reader.CARD_UNKNOWN;
            }
            if (currState < Reader.CARD_UNKNOWN || currState > Reader.CARD_SPECIFIC) {
                currState = Reader.CARD_UNKNOWN;
            }
            if (currState == Reader.CARD_PRESENT) {

                byte[] sendBuffer = new byte[]{ (byte)0xFF, (byte)0xCA, (byte)0x00, (byte)0x00, (byte)0x00};
                byte[] receiveBuffer = new byte[16];

                try {
                    int byteCount = mReader.control(0, Reader.IOCTL_CCID_ESCAPE, sendBuffer, sendBuffer.length, receiveBuffer, receiveBuffer.length);

                    StringBuilder sb = new StringBuilder();

                    for (byte page : receiveBuffer) {
                        int b = page & 0xff;
                        if (b < 0x10)
                            sb.append("");
                        sb.append(b);
                    }
                    fail("String builder " + sb.toString());

                    //int MIFARE_CLASSIC_UID_LENGTH = 4;
                    StringBuffer uid = new StringBuffer();
                    for (int i = 0; i < (byteCount - 2); i++) {

                        uid.append(String.format("%02X", receiveBuffer[i]));
                        if (i < byteCount - 3) {
                            uid.append(":");
                        }

                    }
                    // TODO plugin should just return the UID as byte[]
                    fail("uid " + uid.toString());
                } catch (ReaderException e) {
                    e.printStackTrace();
                }



//                byte[] comman = { (byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
//
//                TransmitParams params = new TransmitParams();
//                params.slotNum = 0;
//                params.controlCode = 3500;
//                params.command = comman;
//                new TransmitTask().execute(params);
            }
        });

        //set format date and time zone in Chisinau
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        timeZone = TimeZone.getTimeZone("Europe/Chisinau");
        simpleDateFormat.setTimeZone(timeZone);

        //get android unique id for send to broker server
        androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String deviceId = new UUID(androidID.hashCode(),androidID.hashCode()).toString();
        deviceModel = Build.MODEL;
        deviceName = Build.DEVICE;
        osVersion = Build.VERSION.RELEASE;
        publicIp = getPublicIPAddress(this);
        privateIp = getIPAddress(true);

        sharedPreferencesSettings.edit().putString("DeviceID",deviceId).apply();
        sharedPreferencesSettings.edit().putString("AndroidDeviceID",androidID).apply();

        brokerServiceAPI = BrokerRetrofitClient.getApiBrokerService();

        //get instalation id from broker
        String licenseID = sharedPreferencesSettings.getString("LicenseID", null);
        boolean firstStart = false;

        //check instalation id , if it's null set register form visibility and others "gone"
        if (licenseID == null) {
            layoutRegister.setVisibility(View.VISIBLE);
            layoutLogin.setVisibility(View.GONE);
            firstStart = true;
        } else {
            layoutRegister.setVisibility(View.GONE);
            layoutLogin.setVisibility(View.VISIBLE);

            String code = sharedPreferencesSettings.getString("LicenseActivationCode","");
            //if instalation id is not null receive URI from breoker server
            doGetURIFromBrokerServer(licenseID, code,false);

            //check installation id if valid from broker service
            checkApplicationToUse();
        }
        boolean finalFirstStart = firstStart;

        btnRegister.setOnClickListener(v ->{
            String codeLicense = inputCode.getText().toString();

            if(codeLicense.equals("")){
                if(codeLicense.equals(""))
                    layoutCode.setError("Input field!");
            }
            else{
                RpgBar.setVisibility(View.VISIBLE);
            }

            //data send to register app in broker server
            SendRegisterApplication registerApplication = new SendRegisterApplication();

            String ids = new UUID(androidID.hashCode(),androidID.hashCode()).toString();
            registerApplication.setDeviceID(ids);
            registerApplication.setDeviceModel(deviceModel);
            registerApplication.setDeviceName(deviceName);
            registerApplication.setSerialNumber(deviceSN);
            registerApplication.setPrivateIP(privateIp);
            registerApplication.setPublicIP(publicIp);
            registerApplication.setOSType(BrokerServiceEnum.Android);
            registerApplication.setApplicationVersion(getAppVersion(this));
            registerApplication.setProductType(BrokerServiceEnum.Retail);
            registerApplication.setOSVersion(osVersion);
            registerApplication.setLicenseActivationCode(codeLicense);

           //save register data on local device
            sharedPreferencesSettings.edit()
                    .putString("LicenseActivationCode",codeLicense)
                    .apply();

            //register app on broker
            doRegisterAppToBrokerServer(registerApplication, codeLicense);
        });
        btnLogin.setOnClickListener(v1 ->{
            String userName = inputEditTextLogin.getText().toString();
            String userPass = inputEditTextPasswordLogin.getText().toString();

            if(userName.equals("") && userPass.equals("")){
                inputLayoutLogin.setError("Input field!");
                inputLayoutPasswordLogin.setError("Input field!");
            }
            else{
                if(userName.equals("") || userPass.equals("")){
                    if(userName.equals(""))
                        inputLayoutLogin.setError("Input field!");
                    if(userPass.equals(""))
                        inputLayoutPasswordLogin.setError("Input field!");
                }
                else{
                    if(getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getBoolean("UserAuthentificate",false)){
                        Lpgbar.setVisibility(View.VISIBLE);
                        //save user password only app is runing
                        BaseApplication.getInstance().setUserPasswordsNotHashed(userPass);

                        //hash SHA1 password
                        String passGenerate = GetSHA1HashUserPassword("This is the code for UserPass",userPass).replace("\n","");

                        //search in local data base user with such data
                        Realm realm = Realm.getDefaultInstance();
                        User result = realm.where(User.class)
                                .equalTo("userName",userName)
                                .and()
                                .equalTo("password",passGenerate)
                                .findFirst();
                        if(result != null) {
                            //such user found,save this user while app is running
                            BaseApplication.getInstance().setUser(realm.copyFromRealm(result));

                            String token = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("Token",null);
                            long tokenValidDate = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getLong("TokenValidTo",0);
                            Date dateToken = new Date(tokenValidDate);
                            Date currDate = new Date();

                            if(token == null && tokenValidDate == 0){
                                authenticateUser(userName,userPass,false);
                            }
                            if(currDate.after(dateToken))
                                authenticateUser(userName,userPass,false);

                            Intent main = new Intent(StartedActivity.this,MainActivity.class);
                            startActivity(main);
                            finish();
                        }
                        else{
                            if(finalFirstStart){
                                authenticateUser(userName,userPass,true);
                            }
                            else{
                                Lpgbar.setVisibility(View.INVISIBLE);
                                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    else{
                        Lpgbar.setVisibility(View.VISIBLE);
                        authenticateUser(userName,userPass,true);
                    }
                }
            }







            if(userName.equals("") || userPass.equals("")){
                Toast.makeText(this, "Enter login or password", Toast.LENGTH_SHORT).show();
            }
            else{

                if(getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getBoolean("UserAuthentificate",false)){
                    Lpgbar.setVisibility(View.VISIBLE);
                    //save user password only app is runing
                    BaseApplication.getInstance().setUserPasswordsNotHashed(userPass);

                    //hash SHA1 password
                    String passGenerate = GetSHA1HashUserPassword("This is the code for UserPass",userPass).replace("\n","");

                    //search in local data base user with such data
                    Realm realm = Realm.getDefaultInstance();
                    User result = realm.where(User.class)
                            .equalTo("userName",userName)
                            .and()
                            .equalTo("password",passGenerate)
                            .findFirst();
                    if(result != null) {
                        //such user found,save this user while app is running
                        BaseApplication.getInstance().setUser(realm.copyFromRealm(result));

                        String token = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("Token",null);
                        long tokenValidDate = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getLong("TokenValidTo",0);
                        Date dateToken = new Date(tokenValidDate);
                        Date currDate = new Date();

                        if(token == null && tokenValidDate == 0){
                            authenticateUser(userName,userPass,false);
                        }
                        if(currDate.after(dateToken))
                            authenticateUser(userName,userPass,false);

                        Intent main = new Intent(StartedActivity.this,MainActivity.class);
                        startActivity(main);
                        finish();
                    }
                    else{
                        if(finalFirstStart){
                            authenticateUser(userName,userPass,true);
                        }
                        else{
                            Lpgbar.setVisibility(View.INVISIBLE);
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else{
                    Lpgbar.setVisibility(View.VISIBLE);
                    authenticateUser(userName,userPass,true);
                }

            }


        });

        inputCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.equals(""))
                    layoutCode.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputEditTextLogin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.equals(""))
                    inputLayoutLogin.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputEditTextPasswordLogin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.equals(""))
                    inputLayoutPasswordLogin.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });




        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //read NFC tegs and card block
        readFromIntent(getIntent());

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };

        //Device load
        initUSBDevice();
    }

    private void checkApplicationToUse() {
        boolean restriction = false;

        // get all date for check it
        long dateValidInstallationID = sharedPreferencesSettings.getLong("DateInstallationIDValid",0);
        long dateReceiveURI = sharedPreferencesSettings.getLong("DateReceiveURI",0);
        long oneDay = 86400000;
        long dateLimitCanUseApp = dateReceiveURI + (oneDay * 60);
        long brokerServerDate = sharedPreferencesSettings.getLong("BrokerServerDate",0);
        long currentDate = new Date().getTime();

        //check if user can use application
        restriction = currentDate < dateValidInstallationID && currentDate < dateLimitCanUseApp && currentDate > brokerServerDate;

        //check if user can use application
        if (restriction){
            //TODO add restriction
        }
    }

    private String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }

    private String getPublicIPAddress(Context context) {
        //final NetworkInfo info = NetworkUtils.getNetworkInfo(context);

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo info = cm.getActiveNetworkInfo();

        RunnableFuture<String> futureRun = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                if ((info != null && info.isAvailable()) && (info.isConnected())) {
                    StringBuilder response = new StringBuilder();

                    try {
                        HttpURLConnection urlConnection = (HttpURLConnection) (
                                new URL("http://checkip.amazonaws.com/").openConnection());
                        urlConnection.setRequestProperty("User-Agent", "Android-device");
                        //urlConnection.setRequestProperty("Connection", "close");
                        urlConnection.setReadTimeout(1000);
                        urlConnection.setConnectTimeout(1000);
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setRequestProperty("Content-type", "application/json");
                        urlConnection.connect();

                        int responseCode = urlConnection.getResponseCode();

                        if (responseCode == HttpURLConnection.HTTP_OK) {

                            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }

                        }
                        urlConnection.disconnect();
                        return response.toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //Log.w(TAG, "No network available INTERNET OFF!");
                    return null;
                }
                return null;
            }
        });

        new Thread(futureRun).start();

        try {
            return futureRun.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getAppVersion(Context context){
        String result = "";

        try{
            result = context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName;
            result = result.replaceAll("[a-zA-Z] |-","");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void authenticateUser(String userName, String passUser,boolean initialStart){
        //user not found in local data bases then we connect to accounting system for receive token and verify user
        String uri = sharedPreferencesSettings.getString("URI","0.0.0.0:1111");
        String licenseID = sharedPreferencesSettings.getString("LicenseID", null);

        CommandServices commandServices = ApiUtils.commandEposService(uri);
        Call<AuthentificateUserResult> call = commandServices.autentificateUser(licenseID ,userName,passUser);

        call.enqueue(new Callback<AuthentificateUserResult>() {
            @Override
            public void onResponse(Call<AuthentificateUserResult> call, Response<AuthentificateUserResult> response) {
                AuthentificateUserResult authentificateUserResult = response.body();
                if(authentificateUserResult != null){

                    //get information for token
                    TokenReceivedFromAutenficateUser token = authentificateUserResult.getAuthentificateUserResult();
                    if(token.getErrorCode() == 0){
                        //save token in shared preference
                        String date = token.getTokenValidTo();
                        date = date.replace("/Date(","");
                        date = date.replace("+0200)/","");
                        date = date.replace("+0300)/","");
                        long dateLong = Long.parseLong(date);

                        getSharedPreferences(SharedPrefSettings,MODE_PRIVATE)
                                .edit()
                                .putString("Token",token.getToken())
                                .putLong("TokenValidTo",dateLong)
                                .apply();
                        if(initialStart){
                            Intent main = new Intent(StartedActivity.this, MainActivity.class);
                            User user = new User();
                            user.setUserName(userName);
                            user.setPassword(GetSHA1HashUserPassword("This is the code for UserPass",passUser).replace("\n",""));
                            BaseApplication.getInstance().setUserPasswordsNotHashed(passUser);
                            BaseApplication.getInstance().setUser(user);

                            startActivity(main);
                            finish();
                        }
                    }
                    else{
                        Lpgbar.setVisibility(View.INVISIBLE);
                        Toast.makeText(StartedActivity.this, "Error to authenticate user to server! "+ token.getErrorCode(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AuthentificateUserResult> call, Throwable t) {
                Lpgbar.setVisibility(View.INVISIBLE);
                Toast.makeText(StartedActivity.this, "Error to authenticate user to server! "+ t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void doRegisterAppToBrokerServer(SendRegisterApplication bodyRegisterApp, String activationCode){
        Call<RegisterApplication> call = brokerServiceAPI.registerApplicationCall(bodyRegisterApp);
        call.enqueue(new Callback<RegisterApplication>() {
            @Override
            public void onResponse(Call<RegisterApplication> call, Response<RegisterApplication> response) {
                RegisterApplication result = response.body();

                if (result == null){
                    pgH.dismiss();
                    Toast.makeText(context, "Response from broker server is null!", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(result.getErrorCode() == 0) {
                        AppDataRegisterApplication appDataRegisterApplication = result.getAppData();
                        //if app registered successful , save installation id and company name
                        sharedPreferencesSettings.edit()
                                .putString("LicenseID",appDataRegisterApplication.getLicenseID())
                                .putString("LicenseCode",appDataRegisterApplication.getLicenseCode())
                                .putString("CompanyName",appDataRegisterApplication.getCompany())
                                .putString("CompanyIDNO",appDataRegisterApplication.getIDNO())
                                .apply();

                        if(appDataRegisterApplication.getURI() != null && !appDataRegisterApplication.getURI().equals("")){
                            long nowDate = new Date().getTime();
                            String serverStringDate = appDataRegisterApplication.getServerDateTime();
                            serverStringDate = serverStringDate.replace("/Date(","");
                            serverStringDate = serverStringDate.replace("+0200)/","");
                            serverStringDate = serverStringDate.replace("+0300)/","");

                            long serverDate = Long.parseLong(serverStringDate);

                            sharedPreferencesSettings.edit()
                                    .putString("URI", appDataRegisterApplication.getURI())
                                    .putLong("DateReceiveURI", nowDate)
                                    .putLong("ServerDate", serverDate)
                                    .apply();

                            //after register app ,get URI for accounting system on broker server
                            RpgBar.setVisibility(View.INVISIBLE);

                            layoutLogin.setVisibility(View.VISIBLE);
                            layoutRegister.setVisibility(View.GONE);
                        }
                        else{
                            RpgBar.setVisibility(View.INVISIBLE);
                            doGetURIFromBrokerServer(appDataRegisterApplication.getLicenseID(), activationCode, true);
                        }

                    }
                    else {
                        RpgBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(context, result.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RegisterApplication> call, Throwable t) {
                RpgBar.setVisibility(View.INVISIBLE);
                Toast.makeText(context, "Connect to broker error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void doGetURIFromBrokerServer(String licenseID,String activationCode, final boolean registerApp){
        String workPlaceName = getSharedPreferences(SharedPrefWorkPlaceSettings,MODE_PRIVATE).getString("WorkPlaceName","");
        //data send to register app in broker server
        SendGetURI registerApplication = new SendGetURI();

        String ids = new UUID(androidID.hashCode(),androidID.hashCode()).toString();
        registerApplication.setDeviceID(ids);
        registerApplication.setDeviceModel(deviceModel);
        registerApplication.setDeviceName(deviceName);
        registerApplication.setSerialNumber(deviceSN);
        registerApplication.setPrivateIP(privateIp);
        registerApplication.setPublicIP(publicIp);
        registerApplication.setLicenseID(licenseID);
        registerApplication.setOSType(BrokerServiceEnum.Android);
        registerApplication.setApplicationVersion(getAppVersion(this));
        registerApplication.setProductType(BrokerServiceEnum.Retail);
        registerApplication.setOSVersion(osVersion);
        registerApplication.setWorkPlace(workPlaceName);


        Call<RegisterApplication> getURICall = brokerServiceAPI.getURICall(registerApplication);

        if (registerApp) {
            pgH.setMessage("Obtain Uri");
            pgH.setCancelable(false);
            pgH.setIndeterminate(true);
            pgH.setButton(-1, "cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getURICall.cancel();
                    if (getURICall.isCanceled())
                        dialog.dismiss();
                }
            });
            pgH.show();
        }

        getURICall.enqueue(new Callback<RegisterApplication>() {
            @Override
            public void onResponse(Call<RegisterApplication> call, Response<RegisterApplication> response) {
                RegisterApplication result = response.body();
                if (result == null){
                    pgH.dismiss();
                    Toast.makeText(context, "Response from broker server is null!", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(result.getErrorCode() == 0) {
                        AppDataRegisterApplication appDataRegisterApplication = result.getAppData();
                        //if app registered successful , save installation id and company name
                        sharedPreferencesSettings.edit()
                                .putString("LicenseID",appDataRegisterApplication.getLicenseID())
                                .putString("LicenseCode",appDataRegisterApplication.getLicenseCode())
                                .putString("CompanyName",appDataRegisterApplication.getCompany())
                                .putString("CompanyIDNO",appDataRegisterApplication.getIDNO())
                                .apply();

                        if(appDataRegisterApplication.getURI() != null && !appDataRegisterApplication.getURI().equals("")) {
                            long nowDate = new Date().getTime();

                            sharedPreferencesSettings.edit()
                                    .putString("URI", appDataRegisterApplication.getURI())
                                    .putLong("DateReceiveURI", nowDate)
                                    .apply();

                            if(registerApp){
                                layoutLogin.setVisibility(View.VISIBLE);
                                layoutRegister.setVisibility(View.GONE);
                            }
                        }
                    } else
                        Toast.makeText(context, result.getErrorMessage(), Toast.LENGTH_SHORT).show();

                    pgH.dismiss();
                }
            }

            @Override
            public void onFailure(Call<RegisterApplication> call, Throwable t) {
                Toast.makeText(StartedActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                processAfterGetURI(registerApp);
            }
        });
    }
    private void processAfterGetURI (boolean registeredApp){
        RpgBar.setVisibility(View.INVISIBLE);

        //берем данные о URI и ID инсталяций
        long dateValid_URI = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getLong("DateValid",0);
        long dateGetURI = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getLong("DateGetURI",0);
        long oneDay = 86400000;
        long dateLimit = dateGetURI + (oneDay * 60);
        long serverDate = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getLong("ServerDate",0);
        //сохраняем время устройство в независимости какой ответ получили при запросе URI
        long currentDate = new Date().getTime();

        //сравниваем не поменяли ли время устройства или не истек ли время действительности ID
        if(currentDate < dateValid_URI && currentDate < dateLimit && currentDate > serverDate){
            //если все в порядке то не ставим никакие ограничения
            getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).edit().putBoolean("Restriction",false).apply();
        }
        else{
            //если прошло слишком много времени с момента получений последнего запроса URI  или меняли время на устройство то ставим ограничения
            getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).edit().putBoolean("Restriction",true).apply();
        }

        //если проходим регистрацию то после открываем главное окно
        //если это не регистрация то ничего не делаем, дальше идет проверка по логину/ пароля/ карточки
        if (registeredApp){
            layoutLogin.setVisibility(View.VISIBLE);
            layoutRegister.setVisibility(View.GONE);
        }
    }

    private void logBuffer(byte[] buffer, int bufferLength) {

        String bufferString = "";
        StringBuilder sb = new StringBuilder();
        for (byte page : buffer) {
            int b = page & 0xff;
            if (b < 0x10)
                sb.append("");
            sb.append(b);
        }


        fail(sb.toString());

        for (int i = 0; i < bufferLength; i++) {

            String hexChar = Integer.toHexString(buffer[i] & 0xFF);
            if (hexChar.length() == 1) {
                hexChar = "0" + hexChar;
            }

            if (i % 16 == 0) {

                if (bufferString != "") {

                    fail(bufferString);
                    bufferString = "";
                }
            }

            bufferString += hexChar.toUpperCase() + " ";
        }

        if (bufferString != "") {
            AlertDialog.Builder bytes = new AlertDialog.Builder(StartedActivity.this);
            bytes.setTitle("Log buffer");
            bytes.setMessage("SB: " + sb.toString() + "\nBuferAray: " + bufferString + "\nreversedHex " + toReversedHex(buffer) + "\ntoHexStrng: " + toHexString(buffer));
            bytes.show();
        }
    }

    public static long replaceDate(String date){
        if(date !=null ){
            date = date.replace("/Date(","");
            date = date.replace("+0200)/","");
            date = date.replace("+0300)/","");
            return Long.parseLong(date);
        }
        else
            return 0;

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        readFromIntent(intent);
    }
    private void readFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            if (tagFromIntent!=null) {
                MifareUltralight mUltra = MifareUltralight.get(tagFromIntent);
                if (mUltra != null) {
                    try {
                        mUltra.connect();
                        StringBuilder sb = new StringBuilder();
                        byte[] pages = mUltra.readPages(0);
                        for (byte page : pages) {
                            int b = page & 0xff;
                            if (b < 0x10)
                                sb.append("");
                            sb.append(b);
                        }
                        String cardCode = getMD5HashCardCode(sb.toString());

                        Realm realms = Realm.getDefaultInstance();
                        realms.executeTransaction(realm -> {
                            User userCard = realm.where(User.class).equalTo("cardBarcode",cardCode).findFirst();

                            if(userCard != null){
                                User authentificateUser = realm.copyFromRealm(userCard);
                                Intent main = new Intent(StartedActivity.this,MainActivity.class);
                                ((BaseApplication)getApplication()).setUser(authentificateUser);
                                startActivity(main);
                                finish();
                            }
                            else{
                                Toast.makeText(this, "Card code not found!", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            mUltra.close();
                            Log.d("NFC", "MifareUltralight disconected");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                boolean auth = false;
                MifareClassic mfc = MifareClassic.get(tagFromIntent);
                if (mfc!=null) {
                    try {
                        String metaInfo = "";
                        //Enable I/O operations to the tag from this TagTechnology object.
                        mfc.connect();
                        StringBuilder sb = new StringBuilder();
//
                        //Authenticate a sector with key A.
                        auth = mfc.authenticateSectorWithKeyA(0,
                                MifareClassic.KEY_DEFAULT);

                        if (auth) {

                            byte[] data = mfc.readBlock(0);

                            for (byte page : data) {
                                int b = page & 0xff;
                                if (b < 0x10)
                                    sb.append("");
                                sb.append(b);
                            }
                        }
                        else {
                            metaInfo += "Sector " + 0 + ": Verified failure\n";
                            Log.d("Error NFC", metaInfo);
                        }

                        String cardCode = getMD5HashCardCode(sb.toString());

                        Realm realms = Realm.getDefaultInstance();
                        realms.executeTransaction(realm -> {
                            User userCard = realm.where(User.class).equalTo("cardBarcode",cardCode).findFirst();

                            if(userCard != null){
                                User authentificateUser = realm.copyFromRealm(userCard);
                                Intent main = new Intent(StartedActivity.this,MainActivity.class);
                                ((BaseApplication)getApplication()).setUser(authentificateUser);
                                startActivity(main);
                                finish();
                            }
                            else{
                                Toast.makeText(this, "Card code not found!", Toast.LENGTH_SHORT).show();
                            }
                        });

                        byte[] id = tagFromIntent.getId();
                        Log.d("NFC", "MifareClassic Reverse " + toReversedHex(id));
                        Log.d("NFC", "MifareClassic Reverse hex " + toHexString(mfc.readBlock(0)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            mfc.close();
                            Log.d("NFC", "MifareClassic disconected");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }else {
            Log.e("Error NFC", "Unknown intent " + intent);
        }
    }

    private void AskForPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        int readpermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writepermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int READ_PHONEpermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (writepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (readpermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (READ_PHONEpermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
        }
    }

    private void initUSBDevice() {
        if (mManager != null) {
            HashMap<String, UsbDevice> deviceList = mManager.getDeviceList();

            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            while (deviceIterator.hasNext()) {
                UsbDevice device = deviceIterator.next();

                if ((device.getVendorId() == DATECS_USB_VID) || (device.getVendorId() == FTDI_USB_VID && device.getManufacturerName().equals("Datecs")) ) {
                    if(mManager.hasPermission(device)){
                        AbstractConnector connector = new UsbDeviceConnector(this, mManager, device);
                        deviceConnect(connector);
                    }
                    else{
                        mManager.requestPermission(device, mPermissionIntent);
                    }
                }
                else if(device.getManufacturerName().equals("ACS")) {
                    if(mManager.hasPermission(device)){
                        new OpenTask().execute(device);
                    }
                    else{
                        mManager.requestPermission(device, mPermissionIntent);
                    }
                }

            }
        }
    }

    private void deviceConnect(final AbstractConnector item) {

        item.getConnectorType();
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    try {
                        item.connect();
                    } catch (Exception e) {
                        fail("Connection error: " + e.getMessage());
                        return;
                    }

                    try {
                        PrinterManager.instance.init(item);
                    } catch (Exception e) {
                        try {
                            item.close();

                        } catch (IOException e1) {
                            fail("Error e1: " + e1.getMessage() + " close");

                            e1.printStackTrace();
                        }
                        fail("Error: " + e.getMessage());
                        return;
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((BaseApplication)getApplication()).setMyFiscalDevice(PrinterManager.instance.getFiscalDevice());

                        }
                    });
                } finally {
                    BaseApplication.getInstance().setMyFiscalDevice(PrinterManager.instance.getFiscalDevice());
//
                }
//                String sTitle = getTitle() + "  " + PrinterManager.instance.getModelVendorName() + ":" + PrinterManager.getsConnectorType();
//                    if(!StartedActivity.this.isDestroyed())
//                        fail(sTitle);
            }
        });
        thread.start();
    }

    @Override
    protected void onDestroy() {

        // Close reader
        mReader.close();

        // Unregister receiver
        unregisterReceiver(mReceiver);
        super.onDestroy();

        Lpgbar.setVisibility(View.INVISIBLE);

    }
    @Override
    public void onPause(){
        super.onPause();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }

        boolean workPlaceChanged = getSharedPreferences(SharedPrefWorkPlaceSettings,MODE_PRIVATE).getBoolean("WorkPlaceChanged",false);
        if(!workPlaceChanged)
            sharedPreferencesSettings.edit().putBoolean("startApp",true).apply();
        else{
            sharedPreferencesSettings.edit().putBoolean("startApp",false).apply();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        hideSystemUI();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
        }
    }

    private String toReversedHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; ++i) {
            if (i > 0) {
                sb.append("-");
            }
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
        }
        return sb.toString().toUpperCase();
    }

    private String toHexString(byte[] buffer) {

        String bufferString = "";

        for (int i = 0; i < buffer.length; i++) {

            String hexChar = Integer.toHexString(buffer[i] & 0xFF);
            if (hexChar.length() == 1) {
                hexChar = "0" + hexChar;
            }

            bufferString += hexChar.toUpperCase() + " ";
        }

        return bufferString;
    }

    private byte[] toByteArray(String hexString) {

        int hexStringLength = hexString.length();
        byte[] byteArray = null;
        int count = 0;
        char c;
        int i;

        // Count number of hex characters
        for (i = 0; i < hexStringLength; i++) {

            c = hexString.charAt(i);
            if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a'
                    && c <= 'f') {
                count++;
            }
        }

        byteArray = new byte[(count + 1) / 2];
        boolean first = true;
        int len = 0;
        int value;
        for (i = 0; i < hexStringLength; i++) {

            c = hexString.charAt(i);
            if (c >= '0' && c <= '9') {
                value = c - '0';
            } else if (c >= 'A' && c <= 'F') {
                value = c - 'A' + 10;
            } else if (c >= 'a' && c <= 'f') {
                value = c - 'a' + 10;
            } else {
                value = -1;
            }

            if (value >= 0) {

                if (first) {

                    byteArray[len] = (byte) (value << 4);

                } else {

                    byteArray[len] |= value;
                    len++;
                }

                first = !first;
            }
        }

        return byteArray;
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

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            hideSystemUI();
            if (v instanceof EditText) {
                v.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }

        return super.dispatchTouchEvent(event);
    }

    public static String GetSHA1HashUserPassword(String keyHint, String message) {
        byte[] hintBytes = ("This is strong key").getBytes();
        String form = "";
        try {

            Rfc2898DerivesBytes test = new Rfc2898DerivesBytes(keyHint,hintBytes,1000);
            byte[] secretKey = test.GetBytes(18);

            SecretKeySpec signingKey = new SecretKeySpec(secretKey, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] bytes = mac.doFinal(message.getBytes());
            form = Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return form;
    }
    public static String getMD5HashCardCode(String message) {
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        m.reset();
        m.update(message.getBytes());
        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1,digest);
        String hashtext = bigInt.toString(16);
// Now we need to zero pad it if you actually want the full 32 chars.
        while(hashtext.length() < 32 ){
            hashtext = "0"+hashtext;
        }
        return hashtext;
    }

    private void fail(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
