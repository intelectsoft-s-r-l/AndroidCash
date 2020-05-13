package edi.md.androidcash;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import edi.md.androidcash.DatcesNewFile.PrinterManager;
import edi.md.androidcash.NetworkUtils.BrokerResultBody.Body.BodyRegisterApp;
import edi.md.androidcash.NetworkUtils.BrokerResultBody.GetURIResult;
import edi.md.androidcash.NetworkUtils.BrokerResultBody.RegisterApplicationResult;
import edi.md.androidcash.NetworkUtils.EposResult.AuthentificateUserResult;
import edi.md.androidcash.NetworkUtils.EposResult.TokenReceivedFromAutenficateUser;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.ApiUtils;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.CommandServices;
import edi.md.androidcash.NetworkUtils.User;
import edi.md.androidcash.Utils.BaseEnum;
import edi.md.androidcash.Utils.Rfc2898DerivesBytes;
import edi.md.androidcash.Utils.UpdateHelper;
import edi.md.androidcash.connectors.AbstractConnector;
import edi.md.androidcash.connectors.UsbDeviceConnector;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static edi.md.androidcash.BaseApplication.SharedPrefSettings;
import static edi.md.androidcash.BaseApplication.SharedPrefWorkPlaceSettings;

public class StartedActivity extends AppCompatActivity implements UpdateHelper.OnUpdateCheckListener{ //
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
    EditText RetIDNOCompany,RetEmail, RetPassword;
    MaterialButton btnRegister;
    ProgressBar RpgBar;

    //views login user form
    EditText LetUserName, LetPassword;
    MaterialButton btnLogin;
    TextView LtvOthersAuthMethods, LtvForgotPass;
    ProgressBar Lpgbar;

    //format date and time zone
    SimpleDateFormat simpleDateFormat;
    TimeZone timeZone;
//
    @Override
    public void onUpdateCheckListener(String uri) {
        Log.d("TAG", "onUpdateCheckListener." + uri);
        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme)
                .setTitle("New version available")
                .setMessage("Please update to new version to continue use")
                .setPositiveButton("UPDATE",(dialogInterface, i) -> {
                    Toast.makeText(this, "" + uri, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No,thanks", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                })
                .create();
        alertDialog.show();
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
        setContentView(R.layout.activity_start);
        //init constraint layouts with authentificate form
        layoutRegister = findViewById(R.id.csl_register_form);
        layoutLogin = findViewById(R.id.csl_login_form);

        //init UI view register form
        RetIDNOCompany = findViewById(R.id.et_idno_company_register);
        RetEmail = findViewById(R.id.et_email_user_register);
        RetPassword = findViewById(R.id.et_password_register_user);
        RpgBar = findViewById(R.id.progressBar_register);
        btnRegister = findViewById(R.id.btn_register_device);

        //init UI view login form
        LetUserName = findViewById(R.id.et_login_user_form);
        LetPassword = findViewById(R.id.et_password_login_user);
        LtvForgotPass = findViewById(R.id.tv_forgot_pass_login_form);
        LtvOthersAuthMethods = findViewById(R.id.txt_other_auth_methods);
        Lpgbar = findViewById(R.id.progressBar_login_form);
        btnLogin = findViewById(R.id.btn_login_user_form);

        //--------------------------------------- check update app version ------------------
        UpdateHelper.with(this).onUpdateCheck(this).check();

        //check if it device support NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null)
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();

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
//
//                new TransmitTask().execute(params);
            }
        });

        //set format date and time zone in Chisinau
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        timeZone = TimeZone.getTimeZone("Europe/Chisinau");
        simpleDateFormat.setTimeZone(timeZone);

        //get instalation id from broker
        String brokerInstallationID = getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getString("InstallationID", null);

        //check instalation id , if it's null set register form visibility and others "gone"
        if (brokerInstallationID == null) {
            layoutRegister.setVisibility(View.VISIBLE);
            layoutLogin.setVisibility(View.GONE);
        } else {
            layoutRegister.setVisibility(View.GONE);
            layoutLogin.setVisibility(View.VISIBLE);
            //if instalation id is not null receive URI from breoker server
            doGetURIFromBrokerServer(brokerInstallationID, false);

            //set identification forms, depending on the settings for entering the program, default 0 - login and password

            int posAuth = getSharedPreferences(SharedPrefWorkPlaceSettings, MODE_PRIVATE).getInt("AuthPosition", 0);

            switch (posAuth) {
                case 0:{
                    layoutLogin.setVisibility(View.VISIBLE);
                    layoutRegister.setVisibility(View.GONE);
                }
                    break;
            }
        }

        btnRegister.setOnClickListener(v ->{
            RpgBar.setVisibility(View.VISIBLE);
            String idno = RetIDNOCompany.getText().toString();
            String email = RetEmail.getText().toString();
            String pass = RetPassword.getText().toString();

            //get android unique id for send to broker server
            String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

            //data send to register app in broker server
            BodyRegisterApp registerApp = new BodyRegisterApp();
            registerApp.setDeviceId(androidID);
            registerApp.setPlatform(2); // 2 - android
            registerApp.setProductType(1); // 1 - casa Market
            registerApp.setEmail(email);
            registerApp.setIdno(idno);
            registerApp.setPassword(pass);

            //save register data on local device
            getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).edit()
                    .putString("DeviceId",androidID)
                    .putString("Email",email)
                    .putString("IDNO",idno)
                    .putString("Password",pass)
                    .apply();

            //register app on broker
            doRegisterAppToBrokerServer(registerApp);
        });

        btnLogin.setOnClickListener(v1 ->{
            Lpgbar.setVisibility(View.VISIBLE);

            String userName = LetUserName.getText().toString();
            String userPass = LetPassword.getText().toString();

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
                String token = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("Token",null);
                long tokenValidDate = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getLong("TokenValidTo",0);
                Date dateToken = new Date(tokenValidDate);
                Date currDate = new Date();

                if(token == null && tokenValidDate == 0){
                    authenticateUser(userName,userPass,true);
                }
                else if(currDate.after(dateToken))
                    authenticateUser(userName,userPass,true);
                else {
                    Intent main = new Intent(StartedActivity.this,MainActivity.class);
                    startActivity(main);
                    finish();
                }
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

    private void authenticateUser(String userName, String passUser,boolean initialStart){
        //user not found in local data bases then we connect to accounting system for receive token and verify user
        String uri = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("URI",null);
        String install_id = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("InstallationID",null);

        CommandServices commandServices = ApiUtils.commandEposService(uri);
        Call<AuthentificateUserResult> call = commandServices.autentificateUser(install_id,userName,passUser);
        call.enqueue(new Callback<AuthentificateUserResult>() {
            @Override
            public void onResponse(Call<AuthentificateUserResult> call, Response<AuthentificateUserResult> response) {
                AuthentificateUserResult authentificateUserResult = response.body();
                if(authentificateUserResult != null){

                    //get information for token
                    TokenReceivedFromAutenficateUser token = authentificateUserResult.getAuthentificateUserResult();
                    if(token.getErrorCode() == 0){
                        //save token in shared preferense
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
                            Intent main = new Intent(StartedActivity.this,MainActivity.class);
                            User user = new User();
                            user.setUserName(userName);
                            user.setPassword(getMD5HashCardCode(passUser));
                            BaseApplication.getInstance().setUserPasswordsNotHashed(passUser);
                            BaseApplication.getInstance().setUser(user);
                            startActivity(main);
                            finish();
                        }
                    }
                    else{
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

    private void doRegisterAppToBrokerServer(BodyRegisterApp bodyRegisterApp){
        CommandServices commandServices = ApiUtils.commandBrokerService();
        Call<RegisterApplicationResult> call = commandServices.registerApplicationCall(bodyRegisterApp);
        call.enqueue(new Callback<RegisterApplicationResult>() {
            @Override
            public void onResponse(Call<RegisterApplicationResult> call, Response<RegisterApplicationResult> response) {
                RegisterApplicationResult result = response.body();

                if (result == null){
                    Toast.makeText(StartedActivity.this, "Response from broker server is null!", Toast.LENGTH_SHORT).show();
                }
                else{
                    int errorCode = result.getErrorCode();
                    String instalation_id = result.getInstalationId();

                    if(errorCode == 0 && instalation_id != null){
                        //if app registered succesful , save instalation id and comapany name
                        getSharedPreferences(SharedPrefSettings,MODE_PRIVATE)
                                .edit()
                                .putString("InstallationID",instalation_id)
                                .putString("CompanyName",result.getName())
                                .apply();
                        //after register app ,get URI for accounting system on broker server
                        doGetURIFromBrokerServer(instalation_id,true);
                    }
                    else {
                        Toast.makeText(StartedActivity.this, result.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RegisterApplicationResult> call, Throwable t) {
                RpgBar.setVisibility(View.INVISIBLE);
                Toast.makeText(StartedActivity.this, "Connect to broker error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void doGetURIFromBrokerServer(String instal_id, final boolean registerApp){
        CommandServices commandServices = ApiUtils.commandBrokerService();
        Call<GetURIResult> call = commandServices.getURICall(instal_id);
        call.enqueue(new Callback<GetURIResult>() {
            @Override
            public void onResponse(Call<GetURIResult> call, Response<GetURIResult> response) {
                GetURIResult uriResult = response.body();
                if(uriResult != null){
                    int erroreCode = uriResult.getErrorCode();
                    String uri = uriResult.getUri();
                    String dateValid = uriResult.getInstalationidvalidto();
                    String dateServer = uriResult.getDateNow();
                    //save time when registered app and received instalation id
                    long currentDate = new Date().getTime();

                    if(erroreCode == 0 && uri != null){
                        //get time valid instalation id
                        dateValid = dateValid.replace("/Date(","");
                        dateValid = dateValid.replace("+0200)/","");
                        dateValid = dateValid.replace("+0300)/","");
                        long validDate = Long.parseLong(dateValid);

                        //get time from broker server
                        dateServer = dateServer.replace("/Date(","");
                        dateServer = dateServer.replace("+0200)/","");
                        dateServer = dateServer.replace("+0300)/","");
                        long serverDate = Long.parseLong(dateServer);

                        //save the received data
                        getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).edit()
                                .putString("URI",uri)
                                .putLong("DateValid",validDate)
                                .putLong("DateGetURI",currentDate)
                                .putLong("ServerDate",serverDate)
                                .apply();
                    }
                    else {
                        Toast.makeText(StartedActivity.this, uriResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(StartedActivity.this, "Response from broker server is null!", Toast.LENGTH_SHORT).show();
                }
                processAfterGetURI(registerApp);
            }

            @Override
            public void onFailure(Call<GetURIResult> call, Throwable t) {
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
                    fail("finish");
                    ((BaseApplication)getApplication()).setMyFiscalDevice(PrinterManager.instance.getFiscalDevice());
                    String sTitle = getTitle() + "  " + PrinterManager.instance.getModelVendorName() + ":" + PrinterManager.getsConnectorType();
                    fail(sTitle);
                }
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
//        unregisterReceiver(mUsbDeviceDetachedReceiver);
//        unregisterReceiver(mUsbDeviceAttachedReceiver);
    }
    @Override
    public void onPause(){
        super.onPause();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
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
