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
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

import edi.md.androidcash.NetworkUtils.EposResult.AuthentificateUserResult;
import edi.md.androidcash.NetworkUtils.EposResult.TokenReceivedFromAutenficateUser;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.ApiUtils;
import edi.md.androidcash.NetworkUtils.BrokerResultBody.Body.BodyRegisterApp;
import edi.md.androidcash.NetworkUtils.BrokerResultBody.GetURIResult;
import edi.md.androidcash.NetworkUtils.BrokerResultBody.RegisterApplicationResult;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.CommandServices;
import edi.md.androidcash.NetworkUtils.User;
import edi.md.androidcash.Utils.Rfc2898DerivesBytes;
import edi.md.androidcash.connectors.AbstractConnector;
import edi.md.androidcash.connectors.UsbDeviceConnector;
import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static edi.md.androidcash.BaseApplication.SharedPrefSettings;

public class StartedActivity extends AppCompatActivity {

    TextView txt_other_methods;
    ImageButton btn_idno_auth, btn_pass_auth, btn_card_auth, btn_exit;
    Button btn_go;
    LinearLayout others, others_buttons, frame_start, frame_idno, frame_pass, frame_card, frame_text_card, frame_btn_login;
    EditText et_user_name, et_user_password, et_idno, et_user_idno, et_pass_idno, et_password;

    SimpleDateFormat sdfChisinau;
    TimeZone tzInChisinau;

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    public static final int DATECS_USB_VID = 65520;
    public static final int FTDI_USB_VID = 1027;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private PendingIntent mPermissionIntent;
    private UsbManager mManager;

    //NFC variables
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];

    //Reader ACR
    private Reader mReader;

    //connection broker
    BodyRegisterApp bodyRegisterApp;
    String brokerInstallationID = null;

    private void postToast(final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(StartedActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
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
                postToast(result.toString());
            }
            else {
                postToast("Reader: " + mReader.getReaderName());
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
                postToast("sb " + sb.toString());

                String cardCode = getMD5HashCardCode(sb.toString());

                if(frame_card.getVisibility() == View.VISIBLE){
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
                    });
                }
            }
            else{
                postToast(transmitProgress.e.getMessage());
            }
        }
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {

                synchronized (this) {

                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if(isFiscalPrinter){
                        if (device.getManufacturerName().equals("Datecs")) {
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
                    if(isFiscalPrinter){
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


    boolean isFiscalPrinter = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set full screen mode
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Get USB manager
        mManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        // Register receiver for USB permission
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        isFiscalPrinter = getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getInt("ModeFiscalWork", 0) == 1;

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        registerReceiver(mReceiver, filter);

        setContentView(R.layout.activity_started);

        txt_other_methods = findViewById(R.id.txt_other_methods);
        btn_idno_auth = findViewById(R.id.idno_buton_auth);
        btn_pass_auth = findViewById(R.id.pass_buton_auth);
        btn_card_auth = findViewById(R.id.card_buton_auth);
        others = findViewById(R.id.LL_other_methods);
        others_buttons = findViewById(R.id.LL_butons_methods);
        frame_start = findViewById(R.id.LL_frames);
        frame_idno = findViewById(R.id.LL_idnp);
        frame_card = findViewById(R.id.LL_card_auth);
        frame_pass = findViewById(R.id.LL_pass);
        frame_text_card = findViewById(R.id.LL_text_card_auth);
        btn_go = findViewById(R.id.btn_login);
        frame_btn_login = findViewById(R.id.LL_btn_login);
        btn_exit = findViewById(R.id.btn_exit_app);
        et_user_name = findViewById(R.id.et_user_name);
        et_user_password = findViewById(R.id.et_password_user);
        et_idno = findViewById(R.id.et_idno_login);
        et_user_idno = findViewById(R.id.et_user_idno);
        et_pass_idno = findViewById(R.id.et_password_idno);
        et_password = findViewById(R.id.et_passwords);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
        }

        AskForPermissions();

        // Initialize reader ACR
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
                    postToast("String builder " + sb.toString());

                    //int MIFARE_CLASSIC_UID_LENGTH = 4;
                    StringBuffer uid = new StringBuffer();
                    for (int i = 0; i < (byteCount - 2); i++) {

                        uid.append(String.format("%02X", receiveBuffer[i]));
                        if (i < byteCount - 3) {
                            uid.append(":");
                        }

                    }
                    // TODO plugin should just return the UID as byte[]
                    postToast("uid " + uid.toString());
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

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //формат даты для проверок времени
        sdfChisinau = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        tzInChisinau = TimeZone.getTimeZone("Europe/Chisinau");
        sdfChisinau.setTimeZone(tzInChisinau);

        //преверяем brokerInstallationID
        brokerInstallationID = getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getString("InstallationID", null);

        //в зависимости от ID  ставим форму регистраций или авторизаций (null - registerForm)
        if (brokerInstallationID == null) {
            registerForm();
        } else {
            //если ID не null то запрашиваем URI
            doGetURIFromBrokerServer(brokerInstallationID, false);

            //паралельно ставим формы аутентификаций в зависимости от настроек входа в программу, по умолчанию 0 - логин и пароль
            int posAuth = getSharedPreferences("WorkPlace", MODE_PRIVATE).getInt("AuthPosition", 0);

            switch (posAuth) {
                case 0:
                    loginForm();
                    break;
                case 1:
                    passwordForm();
                    break;
                case 2:
                    cardForm();
                    break;
            }
        }

        txt_other_methods.setOnClickListener(v -> {
            others.setVisibility(View.INVISIBLE);
            others_buttons.setVisibility(View.VISIBLE);
        });

        btn_idno_auth.setOnClickListener(v -> changeLoginForm());
        btn_pass_auth.setOnClickListener(v -> changePasswordForm());
        btn_card_auth.setOnClickListener(v -> changeCardForm());

        btn_go.setOnClickListener(v -> {
            //если это окно регистраций то получаем данные и регистрируем приложение
            if (frame_idno.getVisibility() == View.VISIBLE) {
                String idno = et_idno.getText().toString().trim();
                String email = et_user_idno.getText().toString().trim();
                String pass = et_pass_idno.getText().toString().trim();

                String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

                bodyRegisterApp = new BodyRegisterApp();
                bodyRegisterApp.setDeviceId(androidID);
                bodyRegisterApp.setPlatform(2); // 2 - android
                bodyRegisterApp.setProductType(1); // 1 - casa Market
                bodyRegisterApp.setEmail(email);
                bodyRegisterApp.setIdno(idno);
                bodyRegisterApp.setPassword(pass);

                //сохраняем регистрационные данные
                getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).edit()
                        .putString("DeviceId",androidID)
                        .putString("Email",email)
                        .putString("IDNO",idno)
                        .putString("Password",pass)
                        .apply();
                //регистрируем приложение
                doRegisterAppToBrokerServer();
            }
            //окно логина и пароля
            else if(frame_start.getVisibility() == View.VISIBLE){
                User authentificateUser = new User();
                BaseApplication.getInstance().setUserPasswordsNotHashed(et_user_password.getText().toString());
                String passGenerate = GetSHA1HashUserPassword("This is the code for UserPass",et_user_password.getText().toString()).replace("\n","");

                Realm realm = Realm.getDefaultInstance();
                User result = realm.where(User.class)
                        .equalTo("userName",et_user_name.getText().toString())
                        .and()
                        .equalTo("password",passGenerate)
                        .findFirst();
                if(result != null) {
                    authentificateUser = realm.copyFromRealm(result);
                    //пароли совпадают и проходим аутентификацию
                    Intent main = new Intent(StartedActivity.this,MainActivity.class);
                    BaseApplication.getInstance().setUser(authentificateUser);
                    startActivity(main);
                    finish();
                }
                else{
                    //если пользователь не найден то показываем
                    String uri = getSharedPreferences("Settings",MODE_PRIVATE).getString("URI",null);
                    String install_id = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("InstallationID",null);

                    CommandServices commandServices = ApiUtils.commandEposService(uri);

                    Call<AuthentificateUserResult> call = commandServices.autentificateUser(install_id,et_user_name.getText().toString(),et_user_password.getText().toString());

                    User finalAuthentificateUser = authentificateUser;
                    call.enqueue(new Callback<AuthentificateUserResult>() {
                        @Override
                        public void onResponse(Call<AuthentificateUserResult> call, Response<AuthentificateUserResult> response) {
                            AuthentificateUserResult authentificateUserResult = response.body();
                            if(authentificateUserResult != null){
                                TokenReceivedFromAutenficateUser token = authentificateUserResult.getAuthentificateUserResult();
                                if(token.getErrorCode() == 0){
                                    getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).edit().putString("Token",token.getToken()).apply();
                                    String date = token.getTokenValidTo();
                                    date = date.replace("/Date(","");
                                    date = date.replace("+0200)/","");
                                    long dateLong = Long.parseLong(date);
                                    getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).edit().putLong("TokenValidTo",dateLong).apply();
                                    Intent main = new Intent(StartedActivity.this,MainActivity.class);
                                    BaseApplication.getInstance().setUser(finalAuthentificateUser);
                                    startActivity(main);
                                    finish();
                                }
                                else{
                                    AlertDialog.Builder dialog_user = new AlertDialog.Builder(StartedActivity.this);
                                    dialog_user.setTitle("Atentie!");
                                    dialog_user.setMessage("Eroare!Codul: " + token.getErrorCode());
                                    dialog_user.setPositiveButton("Ok", (dialog, which) -> {
                                        dialog.dismiss();
                                    });
                                    dialog_user.setNeutralButton("Oricum intra",(dialog,which) -> {
                                        Intent main = new Intent(StartedActivity.this,MainActivity.class);
                                        BaseApplication.getInstance().setUser(finalAuthentificateUser);
                                        startActivity(main);
                                        finish();
                                    });
                                    dialog_user.show();
                                }
                            }
                            else{
                                AlertDialog.Builder dialog_user = new AlertDialog.Builder(StartedActivity.this);
                                dialog_user.setTitle("Atentie!");
                                dialog_user.setMessage("Nu este raspuns de la serviciu!");
                                dialog_user.setPositiveButton("Ok", (dialog, which) -> {
                                    dialog.dismiss();
                                });
                                dialog_user.setNeutralButton("Oricum intra",(dialog,which) -> {
                                    Intent main = new Intent(StartedActivity.this,MainActivity.class);
                                    BaseApplication.getInstance().setUser(finalAuthentificateUser);
                                    startActivity(main);
                                    finish();
                                });
                                dialog_user.show();
                            }
                        }

                        @Override
                        public void onFailure(Call<AuthentificateUserResult> call, Throwable t) {
                            AlertDialog.Builder dialog_user = new AlertDialog.Builder(StartedActivity.this);
                            dialog_user.setTitle("Atentie!");
                            dialog_user.setMessage("Eroare!");
                            dialog_user.setPositiveButton("Ok", (dialog, which) -> {
                                dialog.dismiss();
                            });
                            dialog_user.setNeutralButton("Oricum intra",(dialog,which) -> {
                                Intent main = new Intent(StartedActivity.this,MainActivity.class);
                                BaseApplication.getInstance().setUser(finalAuthentificateUser);
                                startActivity(main);
                                finish();
                            });
                            dialog_user.show();
                        }
                    });
                }
            }
            //если это окно пароля
            else if(frame_pass.getVisibility() == View.VISIBLE){

            }
        });

        btn_exit.setOnClickListener(v -> {
            LayoutInflater inflater = StartedActivity.this.getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.dialog_exit_app, null);

            final AlertDialog exitApp = new AlertDialog.Builder(StartedActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
            exitApp.setCancelable(false);
            exitApp.setView(dialogView);

            Button btn_Cancel = dialogView.findViewById(R.id.btn_no_close);
            Button btn_ok = dialogView.findViewById(R.id.btn_yes_close);

            btn_Cancel.setOnClickListener(v1 -> exitApp.dismiss());

            btn_ok.setOnClickListener(v12 -> finish());

            exitApp.show();
        });

        // NFC settings
        readFromIntent(getIntent());

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };

        //Device load
        initUSBDevice();
    }

    private void doRegisterAppToBrokerServer(){
        CommandServices commandServices = ApiUtils.commandBrokerService();
        Call<RegisterApplicationResult> call = commandServices.registerApplicationCall(bodyRegisterApp);
        call.enqueue(new Callback<RegisterApplicationResult>() {
            @Override
            public void onResponse(Call<RegisterApplicationResult> call, Response<RegisterApplicationResult> response) {
                RegisterApplicationResult result = response.body();
                if (result == null){
                    Toast.makeText(StartedActivity.this, "Nu a fost primit raspuns de la server!", Toast.LENGTH_SHORT).show();
                }
                else{
                    int erroreCode = result.getErrorCode();
                    String instalation_id = result.getInstalationId();

                    if(erroreCode == 0 && instalation_id != null){
                        //если регистрация прошла успешно ,сохраняем Install ID
                        getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).edit().putString("InstallationID",instalation_id).apply();
                        getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).edit().putString("CompanyName",result.getName()).apply();
                        //и запрашиваем URI по данному ID
                        doGetURIFromBrokerServer(instalation_id,true);
                    }
                    //TODO надо ставить проверку на других ошибок
                    else {
                        Toast.makeText(StartedActivity.this, result.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RegisterApplicationResult> call, Throwable t) {
                Toast.makeText(StartedActivity.this, "Conect to broker error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                //получаем ответ от запроса URI
                if(uriResult != null){
                    int erroreCode = uriResult.getErrorCode();
                    String uri = uriResult.getUri();
                    String dateValid = uriResult.getInstalationidvalidto();
                    String dateServer = uriResult.getDateNow();
                    //сохраняем время с устройство когда получили ответ
                    long currentDate = new Date().getTime();

                    if(erroreCode == 0 && uri != null){  // если все успешно то проходим дальше
                        //получаем время валидаций ID
                        dateValid = dateValid.replace("/Date(","");
                        dateValid = dateValid.replace("+0200)/","");
                        long validDate = Long.parseLong(dateValid);
                        //получаем время с брокер сервера
                        dateServer = dateServer.replace("/Date(","");
                        dateServer = dateServer.replace("+0200)/","");
                        long serverDate = Long.parseLong(dateServer);
                        //сохраняем все полученые данные
                        getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).edit()
                                .putString("URI",uri)
                                .putLong("DateValid",validDate)
                                .putLong("DateGetURI",currentDate)
                                .putLong("ServerDate",serverDate)
                                .apply();
                    }
                    //TODO надо ставить проверку на других ошибок
                    else {
                        Toast.makeText(StartedActivity.this, uriResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                    processAfterGetURI(registerApp);
                }
                else {
                    Toast.makeText(StartedActivity.this, "Get uri iss null", Toast.LENGTH_SHORT).show();
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
            loginForm();
//            startActivity(new Intent(StartedActivity.this,MainActivity.class));
//            BaseApplication.getInstance().setUser(null);
//            finish();
        }
    }

    private void registerForm(){
        frame_card.setVisibility(View.INVISIBLE);
        frame_text_card.setVisibility(View.INVISIBLE);
        frame_start.setVisibility(View.INVISIBLE);
        frame_pass.setVisibility(View.INVISIBLE);
        others.setVisibility(View.INVISIBLE);
        frame_idno.setVisibility(View.VISIBLE);
        frame_btn_login.setVisibility(View.VISIBLE);
    }
    private void loginForm(){
        frame_card.setVisibility(View.INVISIBLE);
        frame_text_card.setVisibility(View.INVISIBLE);
        frame_idno.setVisibility(View.INVISIBLE);
        frame_pass.setVisibility(View.INVISIBLE);
        others.setVisibility(View.VISIBLE);
        others_buttons.setVisibility(View.INVISIBLE);
        frame_start.setVisibility(View.VISIBLE);
        frame_btn_login.setVisibility(View.VISIBLE);
    }
    private void passwordForm(){
        frame_card.setVisibility(View.INVISIBLE);
        frame_text_card.setVisibility(View.INVISIBLE);
        frame_start.setVisibility(View.INVISIBLE);
        frame_idno.setVisibility(View.INVISIBLE);
        others.setVisibility(View.VISIBLE);
        others_buttons.setVisibility(View.INVISIBLE);
        frame_btn_login.setVisibility(View.VISIBLE);
        frame_pass.setVisibility(View.VISIBLE);
    }
    private void cardForm(){
        frame_start.setVisibility(View.INVISIBLE);
        frame_pass.setVisibility(View.INVISIBLE);
        frame_idno.setVisibility(View.INVISIBLE);
        frame_btn_login.setVisibility(View.INVISIBLE);
        others_buttons.setVisibility(View.INVISIBLE);
        others.setVisibility(View.VISIBLE);
        frame_card.setVisibility(View.VISIBLE);
        frame_text_card.setVisibility(View.VISIBLE);
    }

    private void changeLoginForm(){
        frame_card.setVisibility(View.INVISIBLE);
        frame_text_card.setVisibility(View.INVISIBLE);
        frame_idno.setVisibility(View.INVISIBLE);
        frame_pass.setVisibility(View.INVISIBLE);
        frame_start.setVisibility(View.VISIBLE);
        frame_btn_login.setVisibility(View.VISIBLE);
    }
    private void changePasswordForm(){
        frame_card.setVisibility(View.INVISIBLE);
        frame_text_card.setVisibility(View.INVISIBLE);
        frame_start.setVisibility(View.INVISIBLE);
        frame_idno.setVisibility(View.INVISIBLE);
        frame_btn_login.setVisibility(View.VISIBLE);
        frame_pass.setVisibility(View.VISIBLE);
    }
    private void changeCardForm(){
        frame_start.setVisibility(View.INVISIBLE);
        frame_pass.setVisibility(View.INVISIBLE);
        frame_idno.setVisibility(View.INVISIBLE);
        frame_btn_login.setVisibility(View.INVISIBLE);
        others_buttons.setVisibility(View.VISIBLE);
        frame_card.setVisibility(View.VISIBLE);
        frame_text_card.setVisibility(View.VISIBLE);
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


        postToast(sb.toString());

        for (int i = 0; i < bufferLength; i++) {

            String hexChar = Integer.toHexString(buffer[i] & 0xFF);
            if (hexChar.length() == 1) {
                hexChar = "0" + hexChar;
            }

            if (i % 16 == 0) {

                if (bufferString != "") {

                    postToast(bufferString);
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
                        if(frame_card.getVisibility() == View.VISIBLE){
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
                            });
                        }

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
                        if(frame_card.getVisibility() == View.VISIBLE){
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
                            });
                        }

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

        appendLog("item.getConnectorType(): " + item.getConnectorType());
        item.getConnectorType();
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    try {
                        item.connect();
                        appendLog("item conect ");
                    } catch (Exception e) {
                        fail("Connection error: " + e.getMessage());
                        appendLog("Connection error item: " + e.getMessage());
                        return;
                    }

                    try {
                        PrinterManager.instance.init(item);
                        appendLog("item init: ");
                    } catch (Exception e) {
                        try {
                            item.close();
                            appendLog("item close: ");
                        } catch (IOException e1) {
                            fail("Error e1: " + e1.getMessage() + " close");
                            appendLog("Error e1: " + e1.getMessage() + " close");
                            e1.printStackTrace();
                        }
                        fail("Error: " + e.getMessage());
                        appendLog("Error init item: " + e.getMessage());
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
            View mDecorView = getWindow().getDecorView();
            mDecorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
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

    public void appendLog(String text) {
        File file = null;
        File teste = new File(Environment.getExternalStorageDirectory(),"/IntelectSoft");
        if (!teste.mkdirs()) {
            Log.e("LOG TAG", "Directory not created");
        }
        file = new File(Environment.getExternalStorageDirectory(),"/IntelectSoft/CashNew_log.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
            Date datess = new Date();
            // To TimeZone Europe/Chisinau
            SimpleDateFormat sdfChisinau = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
            TimeZone tzInChisinau = TimeZone.getTimeZone("Europe/Chisinau");
            sdfChisinau.setTimeZone(tzInChisinau);
            String sDateInChisinau = sdfChisinau.format(datess); // Convert to String first
            String err = sDateInChisinau+ ": ConectorActivity: " + text;
            buf.append(err);
            //buf.write(text);
            buf.newLine();
            buf.close(); }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


}
