package md.intelectsoft.salesepos;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.viewpager.widget.ViewPager;

import com.acs.smartcard.Reader;
import com.datecs.fiscalprinter.SDK.model.DatecsFiscalDevice;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReceipt;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReport;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

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
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import md.intelectsoft.salesepos.DatcesNewFile.PrinterManager;
import md.intelectsoft.salesepos.Fragments.FragmentAssortmentList;
import md.intelectsoft.salesepos.Fragments.FragmentBills;
import md.intelectsoft.salesepos.NetworkUtils.AssortmentServiceEntry;
import md.intelectsoft.salesepos.NetworkUtils.EposResult.AssortmentListService;
import md.intelectsoft.salesepos.NetworkUtils.EposResult.AuthentificateUserResult;
import md.intelectsoft.salesepos.NetworkUtils.EposResult.GetAssortmentListResult;
import md.intelectsoft.salesepos.NetworkUtils.EposResult.GetUsersListResult;
import md.intelectsoft.salesepos.NetworkUtils.EposResult.GetWorkPlaceService;
import md.intelectsoft.salesepos.NetworkUtils.EposResult.GetWorkplaceSettingsResult;
import md.intelectsoft.salesepos.NetworkUtils.EposResult.GetWorkplacesResult;
import md.intelectsoft.salesepos.NetworkUtils.EposResult.TokenReceivedFromAutenficateUser;
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
import md.intelectsoft.salesepos.NetworkUtils.WorkplaceEntry;
import md.intelectsoft.salesepos.RealmHelper.AssortmentRealm;
import md.intelectsoft.salesepos.RealmHelper.Barcodes;
import md.intelectsoft.salesepos.RealmHelper.Bill;
import md.intelectsoft.salesepos.RealmHelper.BillPaymentType;
import md.intelectsoft.salesepos.RealmHelper.BillString;
import md.intelectsoft.salesepos.RealmHelper.History;
import md.intelectsoft.salesepos.RealmHelper.QuickGroupRealm;
import md.intelectsoft.salesepos.RealmHelper.Shift;
import md.intelectsoft.salesepos.Utils.BaseEnum;
import md.intelectsoft.salesepos.Utils.Rfc2898DerivesBytes;
import md.intelectsoft.salesepos.adapters.BillStringInBillRealmListAdapter;
import md.intelectsoft.salesepos.adapters.NewBillStringsRealmRCAdapter;
import md.intelectsoft.salesepos.adapters.TabQuickMenuAdapter;
import md.intelectsoft.salesepos.connectors.AbstractConnector;
import md.intelectsoft.salesepos.connectors.UsbDeviceConnector;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static md.intelectsoft.salesepos.BaseApplication.SharedPrefSettings;
import static md.intelectsoft.salesepos.BaseApplication.SharedPrefWorkPlaceSettings;
import static md.intelectsoft.salesepos.BaseApplication.deviceId;
import static md.intelectsoft.salesepos.Utils.BaseEnum.DATECS_USB_VID;
import static md.intelectsoft.salesepos.Utils.BaseEnum.FTDI_USB_VID;


public class MainActivity extends AppCompatActivity{
    private static Context context;
    private static Activity activity;
    private static ProgressDialog progressDialogPrintReport;
    private static TextView tvDiscountBill, tvSubTotalBill;
    private TextView tvScanBarcode;
    TextView tvUserNameNav;
    TextView tvUserEmailNav;
    private static MaterialButton btnPay;
    MaterialButton btnNewBill, btnAddItem, btnCheckPrice, btnAddClient;
    private static RecyclerView recyclerView;
    private ViewPager viewPager;
    public static TabLayout tabLayout;

    private static Realm mRealm;

    private static NewBillStringsRealmRCAdapter adapter;

    //declare timer for shift
    private CountDownTimer countDownShiftTimer = null;
    SimpleDateFormat simpleDateFormatMD;
    SimpleDateFormat simpleDateFormatShift;
    TimeZone timeZoneMD;

    //Reader ACR
    private static Reader mReader;

    //NFC variables
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter writeTagFilters[];

    //USB variables
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static PendingIntent mPermissionIntent;
    private static UsbManager mUSBManager;

    //datecs variables
    public static DatecsFiscalDevice datecsFiscalDevice = null;

    static Dialog paymentDialog;
    private static cmdReceipt.FiscalReceipt fiscalReceipt;

    private static String openedBillId, workPlaceID, tokenId;

    private boolean shiftOpenButtonPay = false;
    private boolean shiftClosedButtonPay = false;
    private static double billPaymentedSum;

    private static DrawerLayout drawer;
    private static ConstraintLayout drawerConstraint;
    private static ConstraintLayout navigationView;
    public static LayoutInflater inflater;
    static Display display;

    TabQuickMenuAdapter adapterRightMenu;
    private static TextView tvInputSumBillForPayment;
    static double sumBillToPay;
    private static TextView tvToPay;

    private ConstraintLayout csl_sales;
    private ConstraintLayout csl_shifts;
    private ConstraintLayout csl_reports;
    private ConstraintLayout csl_finReport;
    private ConstraintLayout csl_history;
    private ConstraintLayout csl_settings;

    public static DisplayMetrics displayMetrics;

    private static ProgressDialog pgH;
    private List<WorkplaceEntry> workplaceEntryList = new ArrayList<>();
    private SharedPreferences sharedPreferenceSettings, sharedPreferenceWorkPlace;

    private static boolean onlyFiscalPay = false;
    private static boolean billInitPay = false;

    static MaterialButton btnCreditCard;
    static MaterialButton btnCashPay;

    private static ProgressDialog progress;

    int fiscalMode;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if(fiscalMode == BaseEnum.FISCAL_DEVICE){
                        if (device.getManufacturerName().equals("Datecs")) {
                            BaseApplication.getInstance().setDeviceFiscal(device);
                            Log.d("TAG", "ACTION_USB_PERMISSION: item connect" + device.getDeviceName());
                            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                                AbstractConnector connector = new UsbDeviceConnector(context, mUSBManager, device);
                                HashMap<String, UsbDevice> deviceList = mUSBManager.getDeviceList();
                                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                                while (deviceIterator.hasNext()) {
                                    UsbDevice devices = deviceIterator.next();
                                    if (devices.getManufacturerName().equals("ACS")) {
                                        if (!mUSBManager.hasPermission(devices)) {
                                            mUSBManager.requestPermission(devices, mPermissionIntent);
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

                            HashMap<String, UsbDevice> deviceList = mUSBManager.getDeviceList();

                            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                            while (deviceIterator.hasNext()) {
                                UsbDevice devices = deviceIterator.next();

                                if ((devices.getVendorId() == DATECS_USB_VID) || (devices.getVendorId() == FTDI_USB_VID) && (devices.getManufacturerName().equals("Datecs"))) {
                                    if (!mUSBManager.hasPermission(devices)) {
                                        mUSBManager.requestPermission(devices, mPermissionIntent);
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
                        // Close reader
                        new CloseTask().execute();
                    }
                    if (device.getManufacturerName().equals("Datecs")) {
                        BaseApplication.getInstance().setMyFiscalDevice(null);
                    }
                }
            }
            else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                for (UsbDevice device : mUSBManager.getDeviceList().values()) {
                     if (device.getManufacturerName().equals("Datecs")) {
                         if(mUSBManager.hasPermission(device)){
                             AbstractConnector connector = new UsbDeviceConnector(context, mUSBManager, device);
                             HashMap<String, UsbDevice> deviceList = mUSBManager.getDeviceList();
                             Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                             while (deviceIterator.hasNext()) {
                                 UsbDevice devices = deviceIterator.next();
                                 if (devices.getManufacturerName().equals("ACS")) {
                                     if (!mUSBManager.hasPermission(devices)) {
                                         mUSBManager.requestPermission(devices, mPermissionIntent);
                                     }
                                 }
                             }
                             deviceConnect(connector);
                         }
                         else
                             mUSBManager.requestPermission(device, mPermissionIntent);
                     }
                    if (device.getManufacturerName().equals("ACS")) {
                        mUSBManager.requestPermission(device, mPermissionIntent);
                    }
                }

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get USB manager
        mUSBManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        // Register receiver for USB permission
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        fiscalMode = getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getInt("ModeFiscalWork", 0);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        registerReceiver(mReceiver, filter);

        setContentView(R.layout.drawer_layout_bill);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = this;
        activity = this;
        mRealm = Realm.getDefaultInstance();

        recyclerView = findViewById(R.id.rc_list_billstring);
        btnPay = findViewById(R.id.btn_pay_bill);
        tvDiscountBill = findViewById(R.id.txt_discount_summ_bill);
        btnAddItem = findViewById(R.id.btn_add_items);
        btnNewBill = findViewById(R.id.btn_new_bill);
        btnCheckPrice = findViewById(R.id.btn_check_price_);
        viewPager = findViewById(R.id.vp_right_container);
        tabLayout = findViewById(R.id.tab_items_right_menu);
        tvScanBarcode = findViewById(R.id.tv_scan_barcode_main);
        tvSubTotalBill = findViewById(R.id.txt_subtotal_bill);
        display = getWindowManager().getDefaultDisplay();
        drawer = findViewById(R.id.drawer_layout);
        drawerConstraint = findViewById(R.id.nav_view);
        navigationView = findViewById(R.id.nav_view_menu);
        btnAddClient= findViewById(R.id.btn_add_customer);
        inflater = MainActivity.this.getLayoutInflater();

        csl_sales = findViewById(R.id.csl_sales);
        csl_shifts = findViewById(R.id.csl_shift);
        csl_reports = findViewById(R.id.csl_reports);
        csl_finReport = findViewById(R.id.csl_fin_reports);
        csl_history = findViewById(R.id.csl_history);
        csl_settings = findViewById(R.id.csl_setting_nav);
        tvUserNameNav = findViewById(R.id.tv_user_name_nav);
        tvUserEmailNav = findViewById(R.id.tv_email_auth_user);

        progress = new ProgressDialog(this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme);
        pgH = new ProgressDialog(this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme);
        sharedPreferenceSettings = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE);
        sharedPreferenceWorkPlace = getSharedPreferences(SharedPrefWorkPlaceSettings, MODE_PRIVATE);

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        initRecyclerView();
        findOpenedShift();

//        workPlaceID = getSharedPreferences(SharedPrefWorkPlaceSettings,MODE_PRIVATE).getString("WorkPlaceID",null);
//        if (workPlaceID != null) {
//            viewPager.setAdapter(null);
//            //адаптер вкладок с права
//            adapterRightMenu = new TabQuickMenuAdapter(context,getSupportFragmentManager());
//            viewPager.setAdapter(adapterRightMenu);
//            viewPager.setOffscreenPageLimit(4);
//
//            tabLayout.setupWithViewPager(viewPager);
//            tabLayout.setTabMode(TabLayout.MODE_FIXED);
//            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
//        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

//        if (nfcAdapter == null)
//            Toast.makeText(this, R.string.message_device_not_support_NFC, Toast.LENGTH_LONG).show();

        simpleDateFormatMD = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        timeZoneMD = TimeZone.getTimeZone("Europe/Chisinau");
        simpleDateFormatMD.setTimeZone(timeZoneMD);

        simpleDateFormatShift = new SimpleDateFormat("mm:ss");
        timeZoneMD = TimeZone.getTimeZone("Europe/Chisinau");
        simpleDateFormatShift.setTimeZone(timeZoneMD);

        datecsFiscalDevice = ((BaseApplication)getApplication()).getMyFiscalDevice();
        // Initialize reader ACRw
        mReader = new Reader(mUSBManager);

        mReader.setOnStateChangeListener((slotNum, prevState, currState) -> {

            if (prevState < Reader.CARD_UNKNOWN || prevState > Reader.CARD_SPECIFIC) {
                prevState = Reader.CARD_UNKNOWN;
            }
            if (currState < Reader.CARD_UNKNOWN || currState > Reader.CARD_SPECIFIC) {
                currState = Reader.CARD_UNKNOWN;
            }
            if (currState == Reader.CARD_PRESENT) {

            }
        });

        // NFC settings
        readFromIntent(getIntent());

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        tvScanBarcode.requestFocus();
        tvScanBarcode.requestFocusFromTouch();

        tvScanBarcode.setOnEditorActionListener((v, actionId, event) -> {
            if(event.getKeyCode()==KeyEvent.KEYCODE_ENTER){
                AssortmentRealm realmResult = mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",tvScanBarcode.getText().toString()).findFirst();
                if(realmResult != null){
                    AssortmentRealm assortmentFind = mRealm.copyFromRealm(realmResult);
                    addItemsToOpenedBill(assortmentFind,1,tvScanBarcode.getText().toString(),true);
                    tvScanBarcode.setText("");
                    initRecyclerView();
                }
                else{
                    Toast.makeText(MainActivity.this, R.string.message_item_not_found, Toast.LENGTH_SHORT).show();
                    tvScanBarcode.setText("");
                }
            }
            return false;
        });
        csl_sales.setOnClickListener(view -> {
            drawer.closeDrawer(GravityCompat.START);
        });
        csl_shifts.setOnClickListener(view -> {
            startActivityForResult(new Intent(this, ShiftsActivity.class), BaseEnum.Activity_Shifts);
            drawer.closeDrawer(GravityCompat.START);
        });
        csl_reports.setOnClickListener(view -> {
            startActivityForResult(new Intent(this, ReportsActivity.class), BaseEnum.Activity_Reports);
            drawer.closeDrawer(GravityCompat.START);
        });
        csl_finReport.setOnClickListener(view -> {
            startActivityForResult(new Intent(this, FinancialRepActivity.class),BaseEnum.Activity_FinRep);
            drawer.closeDrawer(GravityCompat.START);
        });
        csl_history.setOnClickListener(view -> {
            startActivityForResult(new Intent(this, HistoryActivity.class),BaseEnum.Activity_History);
            drawer.closeDrawer(GravityCompat.START);
        });
        csl_settings.setOnClickListener(v ->{
            startActivityForResult(new Intent(this, SettingsActivity.class),BaseEnum.Activity_Settings);
            drawer.closeDrawer(GravityCompat.START);
        });



        btnNewBill.setOnClickListener(v->{
            if(openedBillId != null){
                openedBillId = null;
                initRecyclerView();
            }
        });
        btnAddItem.setOnClickListener(v->{
            if(!shiftOpenButtonPay){
                btnAddItem.setEnabled(false);
                checkItem_Dialog(BaseEnum.Dialog_AddItem,btnAddItem);
            }
            else
                postToastMessage(getString(R.string.message_shift_not_active));

        });
        btnCheckPrice.setOnClickListener(view -> {
            if(!shiftOpenButtonPay){
                btnCheckPrice.setEnabled(false);
                checkItem_Dialog(BaseEnum.Dialog_CheckPrice,btnCheckPrice);
            }
            else
                postToastMessage(getString(R.string.message_shift_not_active));

        });
        btnAddClient.setOnClickListener(view -> {
            View dialogView = inflater.inflate(R.layout.dialog_add_client, null);

            AlertDialog addClient = new AlertDialog.Builder(MainActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
            addClient.setView(dialogView);
            addClient.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel_add_client);

            btnCancel.setOnClickListener(view1 -> {
                addClient.dismiss();
            });

            addClient.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            addClient.show();

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int displayWidth = displayMetrics.widthPixels;
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(addClient.getWindow().getAttributes());
            int dialogWindowWidth = (int) (displayWidth * 0.4f);
            layoutParams.width = dialogWindowWidth;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            addClient.getWindow().setAttributes(layoutParams);

            addClient.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            addClient.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        });
        btnPay.setOnClickListener(v->{
            if(shiftOpenButtonPay){
                btnPay.setEnabled(false);
                new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                        .setTitle(R.string.message_attention)
                        .setMessage(R.string.message_open_shift)
                        .setCancelable(false)
                        .setPositiveButton(R.string.btn_yes, (dialogInterface, i) -> {
                            long opened_new_shift = new Date().getTime();
                            long setShiftDuring= getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getLong("ShiftDuringSettings",14400000);
                            long need_close = opened_new_shift + setShiftDuring;

                            Shift shiftEntry = new Shift();
                            shiftEntry.setName("SHF " + simpleDateFormatMD.format(opened_new_shift));
                            shiftEntry.setWorkPlaceId(getSharedPreferences(SharedPrefWorkPlaceSettings, MODE_PRIVATE).getString("WorkPlaceID", "null"));
                            shiftEntry.setWorkPlaceName(getSharedPreferences(SharedPrefWorkPlaceSettings, MODE_PRIVATE).getString("WorkPlaceName", "null"));
                            shiftEntry.setAuthor(BaseApplication.getInstance().getUserId());
                            shiftEntry.setAuthorName(BaseApplication.getInstance().getUser().getFullName());
                            shiftEntry.setStartDate(new Date().getTime());
                            shiftEntry.setClosed(false);
                            shiftEntry.setNeedClose(need_close);
                            shiftEntry.setId(UUID.randomUUID().toString());

                            mRealm.executeTransaction(realm -> realm.insert(shiftEntry));

                            History history = new History();
                            history.setDate(new Date().getTime());
                            history.setMsg("Shift: " + shiftEntry.getName());
                            history.setType(BaseEnum.History_OpenShift);
                            mRealm.executeTransaction(realm -> realm.insert(history));

                            BaseApplication.getInstance().setShift(shiftEntry);
                            startTimer(need_close - new Date().getTime());
                            functionOpenedShift();
                        })
                        .setNegativeButton(R.string.btn_no,((dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            btnPay.setEnabled(true);
                        }))

                .show();
            }
            else if(shiftClosedButtonPay){
                btnPay.setEnabled(false);
                new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                        .setTitle(R.string.message_attention)
                        .setMessage(R.string.message_close_shift)
                        .setCancelable(false)
                        .setPositiveButton(R.string.btn_yes, (dialogInterface, i) -> {
                            closeShift();
                        })
                        .setNegativeButton(R.string.btn_no,((dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            btnPay.setEnabled(true);
                        }))
                        .show();
            }
            else if(openedBillId != null){
                btnPay.setEnabled(false);
                paymentBill(Double.parseDouble(btnPay.getText().toString().replace("MDL ","")));
            }
        });

        //Device load
        initUSBDevice();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == BaseEnum.Activity_Shifts){
            FragmentBills.showBillList();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();

        fiscalReceipt = new cmdReceipt.FiscalReceipt();

        if (!( adapterRightMenu== null)) {
            adapterRightMenu.notifyDataSetChanged();
        }

        //check nfc adapter from device
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
        }

        Shift shift = BaseApplication.getInstance().getShift();
        checkShift(shift);

        workPlaceID = getSharedPreferences(SharedPrefWorkPlaceSettings,MODE_PRIVATE).getString("WorkPlaceID",null);
        if (workPlaceID == null) {
            pgH.setMessage(getString(R.string.message_loading_workplace));
            pgH.setIndeterminate(true);
            pgH.show();

            String uri = sharedPreferenceSettings.getString("URI",null);
            tokenId = sharedPreferenceSettings.getString("Token",null);
            getSyncWorkplace(uri,tokenId);
        }

        if (workPlaceID != null) {
            tvUserNameNav.setText(String.format("%s %s", BaseApplication.getInstance().getUser().getFirstName(), BaseApplication.getInstance().getUser().getLastName()));
            tvUserEmailNav.setText(BaseApplication.getInstance().getUser().getEmail());
        }

        boolean syncToStart = sharedPreferenceSettings.getBoolean("SyncToStart",false);
        boolean startApp = sharedPreferenceSettings.getBoolean("startApp",false);
        boolean afterSynched = sharedPreferenceSettings.getBoolean("Synched",false);
        workPlaceID = getSharedPreferences(SharedPrefWorkPlaceSettings,MODE_PRIVATE).getString("WorkPlaceID",null);
        boolean workPlaceChanged = getSharedPreferences(SharedPrefWorkPlaceSettings,MODE_PRIVATE).getBoolean("WorkPlaceChanged",false);

        if(syncToStart && workPlaceID != null && !workPlaceChanged && startApp) {

            tokenId = sharedPreferenceSettings.getString("Token",null);

            pgH.setMessage(getString(R.string.message_loading_assortment));
            pgH.setCancelable(false);
            pgH.setIndeterminate(true);
            pgH.show();

            new AssortmentTask().execute();
        }
        else if(afterSynched){
            sharedPreferenceSettings.edit().putBoolean("Synched",false).apply();

            viewPager.setAdapter(null);
            adapterRightMenu = new TabQuickMenuAdapter(context,getSupportFragmentManager());
            viewPager.setAdapter(adapterRightMenu);
            viewPager.setOffscreenPageLimit(4);

            tabLayout = findViewById(R.id.tab_items_right_menu);

            tabLayout.setupWithViewPager(viewPager);
            tabLayout.setTabMode(TabLayout.MODE_FIXED);
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        }
        else{
            viewPager.setAdapter(null);
            adapterRightMenu = new TabQuickMenuAdapter(context,getSupportFragmentManager());
            viewPager.setAdapter(adapterRightMenu);
            viewPager.setOffscreenPageLimit(4);

            tabLayout = findViewById(R.id.tab_items_right_menu);

            tabLayout.setupWithViewPager(viewPager);
            tabLayout.setTabMode(TabLayout.MODE_FIXED);
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        }
        sharedPreferenceSettings.edit().putBoolean("startApp",false).apply();
        if(workPlaceChanged)
            getSharedPreferences(SharedPrefWorkPlaceSettings,MODE_PRIVATE).edit().putBoolean("WorkPlaceChanged",false).apply();
    }

    private void checkShift(Shift shift){

        if(shift != null){
            boolean shiftOpened = shift.isClosed();
            long shiftNeedClose = shift.getNeedClose();
            long currentTime = new Date().getTime();

            if(!shiftOpened && currentTime < shiftNeedClose){
                //if shift is opened and time to close is smaller current time, when shift is valid
                shiftOpenButtonPay = false;
                btnPay.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                cancelTimer();
                startTimer(shiftNeedClose - new Date().getTime());

            }
            else if(!shiftOpened && currentTime > shiftNeedClose && shiftNeedClose != 0){
                //if shift is open but time when need to close is greater than that current time, when shift is not valid
                //set timer 00.00.00
                cancelTimer();

                //TODO set other function when shift is not valid

                btnPay.setText(R.string.text_close_shift);
                btnPay.setBackgroundColor(getResources().getColor(R.color.btnPay));
                shiftClosedButtonPay = true;

                new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                        .setTitle(R.string.message_attention)
                        .setMessage(R.string.message_shift_expired_want_close)
                        .setCancelable(false)
                        .setPositiveButton(R.string.btn_yes, (dialogInterface, i) -> {
                            closeShift();
                        })
                        .setNegativeButton(R.string.btn_no,((dialogInterface, i) -> {
                            dialogInterface.dismiss();
                        }))
                        .show();
            }

        }
        else{
            //open shift is not find, when shift is not opened and set timer 00.00.00
            cancelTimer();

            //TODO set other function when shift is not opened
            btnPay.setText(R.string.text_open_shift);
            btnPay.setBackgroundColor(getResources().getColor(R.color.btnPay));
            shiftOpenButtonPay = true;
        }
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

                        ListAdapter adapterDialog = new ArrayAdapter<WorkplaceEntry>(context, R.layout.item_workplace_main_dialog, workplaceEntryList) {

                            ViewHolder holder;

                            class ViewHolder {
                                TextView title;
                            }

                            public View getView(int position, View convertView, ViewGroup parent) {
                                if (convertView == null) {
                                    convertView = inflater.inflate(R.layout.item_workplace_main_dialog, null);

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
                        pgH.dismiss();

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(R.string.text_select_workplace);
                        builder.setCancelable(false);
                        builder.setAdapter(adapterDialog, (dialog, position) -> {
                            sharedPreferenceWorkPlace.edit().putString("WorkPlaceID", workplaceEntryList.get(position).getID()).apply();
                            sharedPreferenceWorkPlace.edit().putString("WorkPlaceName", workplaceEntryList.get(position).getName()).apply();
                            workPlaceID =  workplaceEntryList.get(position).getID();
                            dialog.dismiss();
                            pgH.setMessage(getString(R.string.message_loading_assortment));
                            pgH.setCancelable(false);
                            pgH.setIndeterminate(true);
                            pgH.show();

                            new AssortmentTask().execute();

                        });

                        AlertDialog alert = builder.create();
                        alert.show();

                        int displayWidth = displayMetrics.widthPixels;
                        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                        layoutParams.copyFrom(alert.getWindow().getAttributes());
                        int dialogWindowWidth = (int) (displayWidth * 0.4f);
                        layoutParams.width = dialogWindowWidth;
                        layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                        alert.getWindow().setAttributes(layoutParams);


                    }
                }
                else if(errorecode == 405){
                    pgH.dismiss();
                    // не прав на просмотр рабочих мест
                        new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                .setTitle(R.string.message_attention)
                                .setMessage(R.string.message_no_right_to_read_workplace)
                                .setCancelable(false)
                                .setPositiveButton("YES", (dialogInterface, i) -> {
                                    View dialogView = inflater.inflate(R.layout.dialog_login_user, null);

                                    final android.app.AlertDialog reLogin = new android.app.AlertDialog.Builder(context,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
                                    reLogin.setCancelable(false);
                                    reLogin.setView(dialogView);

                                    EditText LetUserName = dialogView.findViewById(R.id.et_login_user_form);
                                    EditText LetPassword = dialogView.findViewById(R.id.et_password_login_user);
                                    MaterialButton btnLogin = dialogView.findViewById(R.id.btn_login_user_form);

                                    btnLogin.setOnClickListener(view -> {
                                        pgH.setMessage(getString(R.string.text_loading));
                                        pgH.setIndeterminate(true);
                                        pgH.show();

                                        authUserToServer(LetUserName.getText().toString(),LetPassword.getText().toString());
                                    });
                                    reLogin.show();

                                    dialogInterface.dismiss();
                                })
                                .setNegativeButton(R.string.btn_no,(dialogInterface, i) -> {

                                })
                                .show();
                }
                else if(errorecode == 401){
                    //необходимо обновить токен
//                    String login = BaseApplication.getInstance().getUser().getUserName();
                    String login = "Admin";
                    String pass = BaseApplication.getInstance().getUserPasswordsNotHashed();

                    authUserToServer(login,pass);
                }
                else{
                    pgH.dismiss();
                    Toast.makeText(MainActivity.this, getString(R.string.message_error) + errorecode, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<GetWorkPlaceService> call, Throwable t) {
                //on failure
                pgH.dismiss();
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void authUserToServer(String login , String pass){
        String uri = sharedPreferenceSettings.getString("URI",null);
        String install_id = sharedPreferenceSettings.getString("InstallationID",null);

        CommandServices commandServices = ApiUtils.commandEposService(uri);

        Call<AuthentificateUserResult> call = commandServices.autentificateUser(install_id,login,pass);

        call.enqueue(new Callback<AuthentificateUserResult>() {
            @Override
            public void onResponse(Call<AuthentificateUserResult> call, Response<AuthentificateUserResult> response) {
                AuthentificateUserResult authentificateUserResult = response.body();
                if(authentificateUserResult != null){
                    TokenReceivedFromAutenficateUser token = authentificateUserResult.getAuthentificateUserResult();
                    if(token.getErrorCode() == 0){
                        tokenId = token.getToken();
                        sharedPreferenceSettings.edit().putString("Token", tokenId).apply();
                        String date = token.getTokenValidTo();
                        date = date.replace("/Date(","");
                        date = date.replace("+0200)/","");
                        date = date.replace("+0300)/","");
                        long dateLong = Long.parseLong(date);
                        sharedPreferenceSettings.edit().putLong("TokenValidTo", dateLong).apply();

                        getSyncWorkplace(uri,token.getToken());
                    }
                    else{
                        AlertDialog.Builder dialog_user = new AlertDialog.Builder(context);
                        dialog_user.setTitle(R.string.message_attention);
                        dialog_user.setMessage(getString(R.string.message_error_code)+ token.getErrorCode());
                        dialog_user.setPositiveButton(R.string.btn_ok, (dialog, which) -> {
                            dialog.dismiss();
                        });
                        dialog_user.show();
                    }
                }
                else{
                    AlertDialog.Builder dialog_user = new AlertDialog.Builder(context);
                    dialog_user.setTitle(R.string.message_attention);
                    dialog_user.setMessage(R.string.message_doesn_response_from_service);
                    dialog_user.setPositiveButton(R.string.btn_ok, (dialog, which) -> {
                        dialog.dismiss();
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
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

    }
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
    public static void draweOpen(Bill bill){
        drawer.openDrawer(GravityCompat.END);

        TextView clientBill = drawerConstraint.findViewById(R.id.txt_client);
        TextView totalItemsBill = drawerConstraint.findViewById(R.id.txttotal_items);
        TextView totalBill = drawerConstraint.findViewById(R.id.txt_total);
        TextView numberBill = drawerConstraint.findViewById(R.id.bill_number_nav);
        TextView discount = drawerConstraint.findViewById(R.id.txt_discount);

        ImageButton btnPayBill = drawerConstraint.findViewById(R.id.btnPay);
        ImageButton btnEditBill = drawerConstraint.findViewById(R.id.btnEdit);
        ImageButton btnDeleteBill = drawerConstraint.findViewById(R.id.btnDelete);
        ImageButton closeDrawer = drawerConstraint.findViewById(R.id.btnClose_drawer);

        btnDeleteBill.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                    .setTitle(R.string.message_attention_delete_bill)
                    .setMessage(R.string.message_want_delete_bill)
                    .setCancelable(false)
                    .setPositiveButton(R.string.btn_yes, (dialogInterface, i) -> {
                        mRealm.executeTransaction(realm -> {
                           Bill bils =  realm.where(Bill.class).equalTo("id", bill.getId()).findFirst();
                           if(bils != null){
                               bils.setCloseDate(new Date().getTime());
                               bils.setClosedBy(BaseApplication.getInstance().getUserId());
                               bils.setState(BaseEnum.BILL_DELETED);

                               History history = new History();
                               history.setDate(new Date().getTime());
                               history.setMsg(context.getString(R.string.message_bill_deleted) + bill.getShiftReceiptNumSoftware());
                               history.setType(BaseEnum.History_DeletedBill);
                               realm.insert(history);


                           }

                        });

                        if(bill.getId().equals(openedBillId)){
                            openedBillId = null;
                            initRecyclerView();
                        }
                        drawer.closeDrawer(GravityCompat.END);

                        Snackbar.make(view, context.getString(R.string.text_bill) + bill.getShiftReceiptNumSoftware() + context.getString(R.string.message_is_deleted), Snackbar.LENGTH_LONG)
                                .setAction(R.string.text_undo, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        mRealm.executeTransaction(realm -> {
                                            Bill bils =  realm.where(Bill.class).equalTo("id", bill.getId()).findFirst();
                                            if(bils != null){
                                                bils.setCloseDate(0);
                                                bils.setClosedBy("");
                                                bils.setState(BaseEnum.BILL_OPEN);

                                                History history = new History();
                                                history.setDate(new Date().getTime());
                                                history.setMsg(context.getString(R.string.message_bill_returned_open) + bill.getShiftReceiptNumSoftware());
                                                history.setType(BaseEnum.History_RecreatBill);
                                                realm.insert(history);
                                            }
                                        });

                                    }
                                }).show();


                    })
                    .setNegativeButton(R.string.btn_no,((dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    }))
                    .show();
        });
        btnEditBill.setOnClickListener(view -> {
            Shift shif = BaseApplication.getInstance().getShift();
            boolean shiftOpened = shif.isClosed();
            long shiftNeedClose = shif.getNeedClose();
            long currentTime = new Date().getTime();

            if(!shiftOpened && currentTime > shiftNeedClose && shiftNeedClose != 0){
                Snackbar.make(view, R.string.message_shift_expired, Snackbar.LENGTH_SHORT).show();
            }
            else{
                openedBillId = bill.getId();
                drawer.closeDrawer(GravityCompat.END);
                initRecyclerView();
            }

        });
        btnPayBill.setOnClickListener(view ->{
            openedBillId = bill.getId();
            paymentBill(bill.getSumWithDiscount());
        });
        closeDrawer.setOnClickListener(view -> drawer.closeDrawer(GravityCompat.END));

        ListView listContent = drawerConstraint.findViewById(R.id.list_string_item);
        numberBill.setText(String.valueOf(bill.getShiftReceiptNumSoftware()));
        final RealmResults<BillString>[] results = new RealmResults[]{null};
        mRealm.executeTransaction(realm -> {
            results[0] = realm.where(BillString.class).equalTo("billID",bill.getId()).and().equalTo("isDeleted",false).findAll();
        });

        if(results[0] != null || !results[0].isEmpty() ){
            totalItemsBill.setText(String.valueOf(results[0].size()));
            BillStringInBillRealmListAdapter adapter = new BillStringInBillRealmListAdapter(results[0]);
            listContent.setAdapter(adapter);
        }
        totalBill.setText(String.format("%.2f",bill.getSumWithDiscount()).replace(",","."));
        clientBill.setText(bill.getDiscountCardNumber());
        discount.setText(String.format("%.2f", bill.getSum() - bill.getSumWithDiscount()).replace(",","."));
    }



    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(MainActivity.this, "click setOnSearchClickListener", Toast.LENGTH_SHORT).show();
                TabLayout.Tab tabs = tabLayout.getTabAt(1);
                tabLayout.selectTab(tabs);
                hideSystemUI();
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                FragmentAssortmentList.homeAssortment();
                TabLayout.Tab tabs = tabLayout.getTabAt(0);
                tabLayout.selectTab(tabs);
                hideSystemUI();
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                FragmentAssortmentList.searchAssortiment(s);
                hideSystemUI();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    static View.OnClickListener clickListenerDynamicPayButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PaymentType paymentType = (PaymentType) v.getTag();
            boolean printFiscalCheck = paymentType.getPrintFiscalCheck();
            String code = paymentType.getCode();
            if(code == null)
                code = "404";
            int resultCloseReceip = 0;

            //primesc rindurile la cont
            RealmList<BillString> billStrings = new RealmList<>();
            RealmList<BillPaymentType> billPaymentTypes = new RealmList<>();

            RealmResults<BillString> billStringsResult = mRealm.where(BillString.class)
                    .equalTo("billID", openedBillId)
                    .and()
                    .equalTo("isDeleted",false)
                    .findAll();
            if (!billStringsResult.isEmpty()) {
                billStrings.addAll(billStringsResult);
            }

            //tipurile de achitare deja facute la cont in caz ca nu a fost achitat integral
            RealmResults<BillPaymentType> billPayResult = mRealm.where(BillPaymentType.class)
                    .equalTo("billID", openedBillId).findAll();
            if(!billPayResult.isEmpty()){
                billPaymentTypes.addAll(billPayResult);
            }

            int bilNumber = 0;
            Bill bilResult = mRealm.where(Bill.class).equalTo("id",openedBillId).findFirst();
            if(bilResult != null)
                bilNumber = bilResult.getShiftReceiptNumSoftware();

            double inputSum = 0;
            try {
                inputSum = Double.parseDouble(tvInputSumBillForPayment.getText().toString());
            }
            catch (Exception e) {
                if(tvInputSumBillForPayment.getText().toString().equals(""))
                    inputSum = 0;
                else
                    inputSum = Double.parseDouble(tvInputSumBillForPayment.getText().toString().replace(",", "."));
            }

            //daca suma introdusa si suma achitarilor anterioare(daca au fost , daca nu e 0) mai mare sau egala cu suma contului, se inchide contul
            if ((billPaymentedSum + inputSum) >= sumBillToPay) {

                int modeFiscalWork = context.getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getInt("ModeFiscalWork",BaseEnum.FISCAL_SERVICE);

                // daca contul se inchide cu imprimarea bonului fiscal sau nu
                if (printFiscalCheck) {
                    if(paymentType.getName().toLowerCase().contains("credit")){

                        //daca e credit card verifica ca suma introdusa cu suma achitarilor sa nu fie mai mare ca suma contului
                        if ((billPaymentedSum + inputSum) > sumBillToPay){
                            tvInputSumBillForPayment.setText("");
                            postToastMessage(context.getResources().getString(R.string.credit_card_exceed_pay_amount));
                        }
                        else if ((billPaymentedSum + inputSum) < sumBillToPay) {
                            BillPaymentType billPaymentType = new BillPaymentType();
                            billPaymentType.setId(UUID.randomUUID().toString());
                            billPaymentType.setBillID(openedBillId);
                            billPaymentType.setName(paymentType.getName());
                            billPaymentType.setPaymentCode(Integer.valueOf(code));
                            billPaymentType.setPaymentTypeID(paymentType.getExternalId());
                            billPaymentType.setSum(inputSum);
                            billPaymentType.setAuthor(BaseApplication.getInstance().getUser().getId());
                            billPaymentType.setCreateDate(new Date().getTime());

                            mRealm.executeTransaction(realm ->{
                                Bill bill = realm.where(Bill.class).equalTo("id",openedBillId).findFirst();
                                if(bill != null){
                                    bill.setState(0);
                                    bill.getBillPaymentTypes().add(billPaymentType);
                                }
                            });
                            billPaymentedSum = billPaymentedSum + inputSum;
                            tvToPay.setText(String.format("%.2f", sumBillToPay - billPaymentedSum).replace(",","."));
                            tvInputSumBillForPayment.setText(String.format("%.2f",sumBillToPay - billPaymentedSum).replace(",","."));
                        }
                        else if ((billPaymentedSum + inputSum) == sumBillToPay){
                            if(modeFiscalWork == BaseEnum.FISCAL_DEVICE){
                                progress.setTitle(" close bill...");
                                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                progress.setCancelable(false);
                                progress.show();

                                DatecsFiscalDevice fiscalDevice = null;
                                if(BaseApplication.getInstance().getMyFiscalDevice() != null){
                                    fiscalDevice = BaseApplication.getInstance().getMyFiscalDevice();
                                }
                                if(fiscalDevice != null && fiscalDevice.isConnectedDeviceV2()){

                                    resultCloseReceip = BaseApplication.getInstance().printFiscalReceipt(fiscalReceipt, billStrings, paymentType, inputSum, billPaymentTypes,bilNumber);
                                    if (resultCloseReceip != 0) {
                                        BillPaymentType billPaymentType= new BillPaymentType();
                                        billPaymentType.setId(UUID.randomUUID().toString());
                                        billPaymentType.setBillID(openedBillId);
                                        billPaymentType.setName(paymentType.getName());
                                        billPaymentType.setPaymentCode(Integer.valueOf(code));
                                        billPaymentType.setPaymentTypeID(paymentType.getExternalId());
                                        billPaymentType.setSum(inputSum);
                                        billPaymentType.setAuthor(BaseApplication.getInstance().getUser().getId());
                                        billPaymentType.setCreateDate(new Date().getTime());

                                        int finalResultCloseReceip = resultCloseReceip;
                                        mRealm.executeTransaction(realm ->{
                                            Bill bill = realm.where(Bill.class).equalTo("id",openedBillId).findFirst();
                                            if(bill != null){
                                                bill.setReceiptNumFiscalMemory(finalResultCloseReceip);
                                                bill.setState(1);
                                                bill.setCloseDate(new Date().getTime());
                                                bill.setClosedBy(BaseApplication.getInstance().getUser().getId());
                                                bill.setClosedByName(BaseApplication.getInstance().getUser().getFullName());
                                                bill.getBillPaymentTypes().add(billPaymentType);

                                            }
                                        });
                                        progress.dismiss();

                                        initRecyclerView();

                                        billPaymentedSum = 0;
                                        paymentDialog.dismiss();
                                        btnPay.setEnabled(true);


                                        if(drawer.isDrawerOpen(GravityCompat.END))
                                            drawer.closeDrawer(GravityCompat.END);
                                        openedBillId = null;
                                    }
                                    else{
                                        progress.dismiss();
                                    }
                                }
                                else{
                                    progress.dismiss();
                                    Toast.makeText(context, R.string.message_fiscal_device_not_connected, Toast.LENGTH_SHORT).show();
                                }
                            }
                            if(modeFiscalWork == BaseEnum.FISCAL_SERVICE){
                                BaseApplication.getInstance().printReceiptFiscalService(billStrings, paymentType, inputSum, billPaymentTypes,"1");

                                BillPaymentType billPaymentType= new BillPaymentType();
                                billPaymentType.setId(UUID.randomUUID().toString());
                                billPaymentType.setBillID(openedBillId);
                                billPaymentType.setName(paymentType.getName());
                                billPaymentType.setPaymentCode(Integer.valueOf(code));
                                billPaymentType.setPaymentTypeID(paymentType.getExternalId());
                                billPaymentType.setSum(inputSum);
                                billPaymentType.setAuthor(BaseApplication.getInstance().getUser().getId());
                                billPaymentType.setCreateDate(new Date().getTime());

                                mRealm.executeTransaction(realm ->{
                                    Bill bill = realm.where(Bill.class).equalTo("id",openedBillId).findFirst();
                                    if(bill != null){
                                        bill.setReceiptNumFiscalMemory(0);
                                        bill.setState(1);
                                        bill.setCloseDate(new Date().getTime());
                                        bill.setClosedBy(BaseApplication.getInstance().getUser().getId());
                                        bill.setClosedByName(BaseApplication.getInstance().getUser().getFullName());
                                        bill.getBillPaymentTypes().add(billPaymentType);

                                    }
                                });

                                initRecyclerView();

                                billPaymentedSum = 0;
                                paymentDialog.dismiss();
                                btnPay.setEnabled(true);

                                if(drawer.isDrawerOpen(GravityCompat.END))
                                    drawer.closeDrawer(GravityCompat.END);
                                openedBillId = null;
                            }
                        }
                    }
                    else{
                        if(modeFiscalWork == BaseEnum.FISCAL_DEVICE){
                            progress.setTitle(" close bill...");
                            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            progress.setCancelable(false);
                            progress.show();

                            DatecsFiscalDevice fiscalDevice = null;
                            if(BaseApplication.getInstance().getMyFiscalDevice() != null){
                                fiscalDevice = BaseApplication.getInstance().getMyFiscalDevice();
                            }
                            if(fiscalDevice != null && fiscalDevice.isConnectedDeviceV2()){

                                resultCloseReceip = BaseApplication.getInstance().printFiscalReceipt(fiscalReceipt, billStrings, paymentType, inputSum, billPaymentTypes,bilNumber);
                                if (resultCloseReceip != 0) {
                                    BillPaymentType billPaymentType= new BillPaymentType();
                                    billPaymentType.setId(UUID.randomUUID().toString());
                                    billPaymentType.setBillID(openedBillId);
                                    billPaymentType.setName(paymentType.getName());
                                    billPaymentType.setPaymentCode(Integer.valueOf(code));
                                    billPaymentType.setPaymentTypeID(paymentType.getExternalId());
                                    billPaymentType.setSum(inputSum);
                                    billPaymentType.setAuthor(BaseApplication.getInstance().getUser().getId());
                                    billPaymentType.setCreateDate(new Date().getTime());

                                    int finalResultCloseReceip = resultCloseReceip;
                                    mRealm.executeTransaction(realm ->{
                                        Bill bill = realm.where(Bill.class).equalTo("id",openedBillId).findFirst();
                                        if(bill != null){
                                            bill.setReceiptNumFiscalMemory(finalResultCloseReceip);
                                            bill.setState(1);
                                            bill.setCloseDate(new Date().getTime());
                                            bill.setClosedBy(BaseApplication.getInstance().getUser().getId());
                                            bill.setClosedByName(BaseApplication.getInstance().getUser().getFullName());
                                            bill.getBillPaymentTypes().add(billPaymentType);

                                        }
                                    });
                                    progress.dismiss();

                                    initRecyclerView();

                                    billPaymentedSum = 0;
                                    paymentDialog.dismiss();
                                    btnPay.setEnabled(true);

                                    if(drawer.isDrawerOpen(GravityCompat.END))
                                        drawer.closeDrawer(GravityCompat.END);
                                    openedBillId = null;
                                }
                                else{
                                    progress.dismiss();
                                }
                            }
                            else{
                                progress.dismiss();
                                Toast.makeText(context, R.string.message_fiscal_device_not_connected, Toast.LENGTH_SHORT).show();
                            }
                        }
                        else if(modeFiscalWork == BaseEnum.FISCAL_SERVICE){
                            BaseApplication.getInstance().printReceiptFiscalService(billStrings, paymentType, inputSum, billPaymentTypes,"1");

                            BillPaymentType billPaymentType= new BillPaymentType();
                            billPaymentType.setId(UUID.randomUUID().toString());
                            billPaymentType.setBillID(openedBillId);
                            billPaymentType.setName(paymentType.getName());
                            billPaymentType.setPaymentCode(Integer.valueOf(code));
                            billPaymentType.setPaymentTypeID(paymentType.getExternalId());
                            billPaymentType.setSum(inputSum);
                            billPaymentType.setAuthor(BaseApplication.getInstance().getUser().getId());
                            billPaymentType.setCreateDate(new Date().getTime());

                            mRealm.executeTransaction(realm ->{
                                Bill bill = realm.where(Bill.class).equalTo("id",openedBillId).findFirst();
                                if(bill != null){
                                    bill.setReceiptNumFiscalMemory(0);
                                    bill.setState(1);
                                    bill.setCloseDate(new Date().getTime());
                                    bill.setClosedBy(BaseApplication.getInstance().getUser().getId());
                                    bill.setClosedByName(BaseApplication.getInstance().getUser().getFullName());
                                    bill.getBillPaymentTypes().add(billPaymentType);

                                }
                            });

                            initRecyclerView();

                            billPaymentedSum = 0;
                            paymentDialog.dismiss();
                            btnPay.setEnabled(true);

                            if(drawer.isDrawerOpen(GravityCompat.END))
                                drawer.closeDrawer(GravityCompat.END);
                            openedBillId = null;
                        }
                        else{
                            postToastMessage("Not selected fiscal mode work");
                        }
                    }
                }
                else {
                    BillPaymentType billPaymentType= new BillPaymentType();
                    billPaymentType.setId(UUID.randomUUID().toString());
                    billPaymentType.setBillID(openedBillId);
                    billPaymentType.setName(paymentType.getName());
                    billPaymentType.setPaymentCode(Integer.valueOf(code));
                    billPaymentType.setPaymentTypeID(paymentType.getExternalId());
                    billPaymentType.setSum(inputSum);
                    billPaymentType.setAuthor(BaseApplication.getInstance().getUser().getId());
                    billPaymentType.setCreateDate(new Date().getTime());

                    mRealm.executeTransaction(realm ->{
                        Bill bill = realm.where(Bill.class).equalTo("id",openedBillId).findFirst();
                        if(bill != null){
                            bill.setReceiptNumFiscalMemory(0);
                            bill.setState(1);
                            bill.setCloseDate(new Date().getTime());
                            bill.setClosedBy(BaseApplication.getInstance().getUser().getId());
                            bill.setClosedByName(BaseApplication.getInstance().getUser().getFullName());
                            bill.getBillPaymentTypes().add(billPaymentType);
                        }
                    });
                    doAfterCloseBill();
                }
            }
            else if ((billPaymentedSum + inputSum) < sumBillToPay && inputSum != 0) {
                BillPaymentType billPaymentType = new BillPaymentType();
                billPaymentType.setId(UUID.randomUUID().toString());
                billPaymentType.setBillID(openedBillId);
                billPaymentType.setName(paymentType.getName());
                billPaymentType.setPaymentCode(Integer.valueOf(code));
                billPaymentType.setPaymentTypeID(paymentType.getExternalId());
                billPaymentType.setSum(inputSum);
                billPaymentType.setAuthor(BaseApplication.getInstance().getUser().getId());
                billPaymentType.setCreateDate(new Date().getTime());

                mRealm.executeTransaction(realm ->{
                    Bill bill = realm.where(Bill.class).equalTo("id",openedBillId).findFirst();
                    if(bill != null){
                        bill.setState(0);
                        bill.getBillPaymentTypes().add(billPaymentType);
                    }
                });
                billPaymentedSum = billPaymentedSum + inputSum;
                tvToPay.setText(String.format("%.2f", sumBillToPay - billPaymentedSum).replace(",","."));
                tvInputSumBillForPayment.setText(String.format("%.2f",sumBillToPay - billPaymentedSum).replace(",","."));

                if(paymentType.getPrintFiscalCheck()){
                    billInitPay = true;
                    onlyFiscalPay = true;
                }
            }
            else if(inputSum == 0){
                Toast.makeText(context, "Introdu valoare mai mare ca 0!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public static void doAfterCloseBill(){
        initRecyclerView();

        billPaymentedSum = 0;
        paymentDialog.dismiss();
        if(drawer.isDrawerOpen(GravityCompat.END))
            drawer.closeDrawer(GravityCompat.END);
        btnPay.setEnabled(true);
        openedBillId = null;
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public static void deleteBillString(BillString billString){
        mRealm.executeTransaction(realm -> {

            billString.setDeleted(true);
            billString.setDeletionDate(new Date().getTime());
            billString.setDeleteBy(BaseApplication.getInstance().getUserId());

            Bill billEntryRealmResults = realm.where(Bill.class).equalTo("id", openedBillId).findFirst();
            if (billEntryRealmResults != null) {
                billEntryRealmResults.setSum(billEntryRealmResults.getSum()  - (billString.getPrice() * billString.getQuantity()));
                billEntryRealmResults.setSumWithDiscount(billEntryRealmResults.getSumWithDiscount() - (billString.getPriceWithDiscount() * billString.getQuantity()));

                btnPay.setText("MDL " + String.format("%.2f", billEntryRealmResults.getSumWithDiscount()).replace(",","."));
                tvDiscountBill.setText(String.format("%.2f",billEntryRealmResults.getSum() - billEntryRealmResults.getSumWithDiscount()).replace(",","."));
                tvSubTotalBill.setText(String.format("%.2f", billEntryRealmResults.getSum()).replace(",","."));
            }
            History history = new History();
            history.setDate(new Date().getTime());
            history.setMsg(context.getString(R.string.message_delete_bill_string) + billString.getAssortmentFullName() + " - " + billString.getSumWithDiscount());
            history.setType(BaseEnum.History_DeletedFromBill);
            realm.insert(history);
        });
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public static void editLineCount(BillString billString, double sumWithDiscount, double sum, double quantity){
        mRealm.executeTransaction(realm -> {
            billString.setQuantity(quantity);
            billString.setSum(sum);
            billString.setSumWithDiscount(sumWithDiscount);

            History history = new History();
            history.setDate(new Date().getTime());
            history.setMsg(context.getString(R.string.message_change_count_bill_string) + billString.getAssortmentFullName() + " + " + quantity );
            history.setType(BaseEnum.History_ChangeItemCount);
            realm.insert(history);

            double sumTotal = 0;
            double sumWithDisc = 0;

            RealmResults<BillString> result = realm.where(BillString.class)
                    .equalTo("billID",openedBillId)
                    .and()
                    .equalTo("isDeleted",false)
                    .findAll();
            if(!result.isEmpty()){
                for (BillString string: result){
                    sumTotal += string.getSum();
                    sumWithDisc += string.getSumWithDiscount();
                }
            }

            Bill billEntryRealmResults = realm.where(Bill.class).equalTo("id", openedBillId).findFirst();
            if (billEntryRealmResults != null) {
                billEntryRealmResults.setSum(sumTotal);
                billEntryRealmResults.setSumWithDiscount(sumWithDisc);

                btnPay.setText("MDL " + String.format("%.2f", billEntryRealmResults.getSumWithDiscount()).replace(",","."));
                tvDiscountBill.setText(String.format("%.2f",billEntryRealmResults.getSum() - billEntryRealmResults.getSumWithDiscount()).replace(",","."));
                tvSubTotalBill.setText(String.format("%.2f", billEntryRealmResults.getSum()).replace(",","."));
            }
        });
    }

    @Override
    protected void onDestroy() {
        // Close reader
        mReader.close();
        // Unregister receiver
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
    @Override
    public void onPause(){
        super.onPause();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }



    private static boolean createNewBill(String uid){
        Shift shift = BaseApplication.getInstance().getShift();
        final boolean[] onSucces = {false};
        if(shift != null && new Date().getTime() < shift.getNeedClose()){
            Bill bill = new Bill();
            bill.setId(uid);
            bill.setCreateDate(new Date().getTime());
            bill.setShiftReceiptNumSoftware(shift.getBillCounter() + 1);
            bill.setAuthor(BaseApplication.getInstance().getUser().getId());
            bill.setAuthorName(BaseApplication.getInstance().getUser().getFullName());
            bill.setSumWithDiscount(0.0);
            bill.setSum(0.0);
            bill.setState(0);
            bill.setShiftId(shift.getId());
            bill.setSynchronized(false);
            String version ="0.0";
            try {
                PackageInfo pInfo = BaseApplication.getInstance().getPackageManager().getPackageInfo(BaseApplication.getInstance().getPackageName(), 0);
                version = pInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            bill.setCurrentSoftwareVersion(version);
            bill.setDeviceId(BaseApplication.getInstance().getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString(deviceId,null));
            shift.setBillCounter(shift.getBillCounter() + 1);
            mRealm.executeTransaction(realm -> {
                Shift shift1 = realm.where(Shift.class).equalTo("id",shift.getId()).findFirst();
                if(shift1 != null)
                    shift1.setBillCounter(shift.getBillCounter());
                realm.insert(bill);

                History createdBill = new History();
                createdBill.setDate(bill.getCreateDate());
                createdBill.setMsg(context.getString(R.string.message_bill_created_nr) + bill.getShiftReceiptNumSoftware());
                createdBill.setType(BaseEnum.History_CreateBill);
                realm.insert(createdBill);

                onSucces[0] = true;
            });
        }
        else{
            if(!activity.isDestroyed())
                Toast.makeText(MainActivity.getContext(), R.string.message_shift_not_active, Toast.LENGTH_SHORT).show();
        }

        return onSucces[0];
    }

    public static boolean addItemsToOpenedBill (@NonNull AssortmentRealm assortmentEntry, double count, String barcode, boolean updateInterface){
        int countArray = 0;
        if (adapter != null) {
            countArray = adapter.getItemCount();
        }
        final boolean[] createBillString = {false};

        BillString lastBillString = null;
        if(countArray >= 1)
            lastBillString = adapter.getItem(0);

        if(openedBillId == null) {
            openedBillId = UUID.randomUUID().toString();
            if(!createNewBill((openedBillId))){
                openedBillId = null;
            }
        }

        if(openedBillId != null){
            if(lastBillString != null && assortmentEntry.getId().equals(lastBillString.getAssortmentExternID()) ){
                double sumBefore = lastBillString.getSum();
                double sumWithDiscBefore = lastBillString.getSumWithDiscount();
                double quantity = lastBillString.getQuantity() + count;
                double sum = lastBillString.getPrice() * quantity;
                double sumWithDisc = lastBillString.getPriceWithDiscount() * quantity;
                String id = lastBillString.getId();

                mRealm.executeTransaction(realm -> {
                    BillString string = realm.where(BillString.class).equalTo("id", id).findFirst();
                    if(string != null){
                        string.setQuantity(quantity);
                        string.setSum(sum);
                        string.setSumWithDiscount(sumWithDisc);
                    }

                    Bill billEntryRealmResults = realm.where(Bill.class).equalTo("id", openedBillId).findFirst();
                    if (billEntryRealmResults != null) {
                        billEntryRealmResults.setSum(billEntryRealmResults.getSum() + (sum - sumBefore));
                        billEntryRealmResults.setSumWithDiscount(billEntryRealmResults.getSumWithDiscount() + (sumWithDisc - sumWithDiscBefore));
                    }
                });
            }
            else{
                BillString billString = new BillString();
                double priceWithDisc = assortmentEntry.getPrice();

                CheckedAssortmentItemToPromo assortmentItemToPromo = checkedAssortmentItemToPromo(assortmentEntry);

                if(assortmentItemToPromo != null){
                    billString.setPromoLineID(assortmentItemToPromo.getPromoId());
                    priceWithDisc = assortmentItemToPromo.getPromoPrice();
                }

                billString.setCreateBy(BaseApplication.getInstance().getUser().getId());
                billString.setAssortmentExternID(assortmentEntry.getId());
                billString.setAssortmentFullName(assortmentEntry.getName());
                billString.setBillID(openedBillId);
                billString.setId(UUID.randomUUID().toString());
                billString.setQuantity(count);
                billString.setPrice(assortmentEntry.getPrice());
                billString.setPriceLineID(assortmentEntry.getPriceLineId());
                billString.setAllowNonInteger(assortmentEntry.isAllowNonInteger());
                billString.setAllowDiscounts(assortmentEntry.isAllowDiscounts());
                billString.setBarcode(barcode);
                billString.setVat(assortmentEntry.getVat());
                billString.setCreateDate(new Date().getTime());
                billString.setDeleted(false);
                billString.setPriceWithDiscount(priceWithDisc);

                billString.setSum(assortmentEntry.getPrice() * count);
                billString.setSumWithDiscount(priceWithDisc * count);

                double finalPriceWithDisc = priceWithDisc;
                mRealm.executeTransaction(realm -> {

                Bill billEntryRealmResults = realm.where(Bill.class).equalTo("id", openedBillId).findFirst();
                if (billEntryRealmResults != null) {
                    billEntryRealmResults.setSum(billEntryRealmResults.getSum() + (assortmentEntry.getPrice() * count));
                    billEntryRealmResults.setSumWithDiscount(billEntryRealmResults.getSumWithDiscount() + (finalPriceWithDisc * count));
                    billEntryRealmResults.getBillStrings().add(billString);
                }
                    createBillString[0] = true;
                });

                if(createBillString[0]){
                    History history = new History();
                    history.setDate(new Date().getTime());
                    history.setMsg(context.getString(R.string.messaeg_added_to_bill_item) + billString.getAssortmentFullName() + " + " + count  + " = " + billString.getSumWithDiscount());
                    history.setType(BaseEnum.History_AddedToBill);
                    mRealm.executeTransaction(realm -> realm.insert(history));
                }
            }
            if(updateInterface)
                initRecyclerView();
        }
        return createBillString[0];
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Log.d("myLogs", "KeyEvent DOWN" + event);
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_1: {
                    tvScanBarcode.append("1");
                    tvScanBarcode.requestFocus();
                    tvScanBarcode.requestFocusFromTouch();
                }
                break;
                case KeyEvent.KEYCODE_2: {
                    tvScanBarcode.append("2");
                    tvScanBarcode.requestFocus();
                    tvScanBarcode.requestFocusFromTouch();
                }
                break;
                case KeyEvent.KEYCODE_3: {
                    tvScanBarcode.append("3");
                    tvScanBarcode.requestFocus();
                    tvScanBarcode.requestFocusFromTouch();
                }
                break;
                case KeyEvent.KEYCODE_4: {
                    tvScanBarcode.append("4");
                    tvScanBarcode.requestFocus();
                    tvScanBarcode.requestFocusFromTouch();
                }
                break;
                case KeyEvent.KEYCODE_5: {
                    tvScanBarcode.append("5");
                    tvScanBarcode.requestFocus();
                    tvScanBarcode.requestFocusFromTouch();
                }
                break;
                case KeyEvent.KEYCODE_6: {
                    tvScanBarcode.append("6");
                    tvScanBarcode.requestFocus();
                    tvScanBarcode.requestFocusFromTouch();
                }
                break;
                case KeyEvent.KEYCODE_7: {
                    tvScanBarcode.append("7");
                    tvScanBarcode.requestFocus();
                    tvScanBarcode.requestFocusFromTouch();
                }
                break;
                case KeyEvent.KEYCODE_8: {
                    tvScanBarcode.append("8");
                    tvScanBarcode.requestFocus();
                    tvScanBarcode.requestFocusFromTouch();
                }
                break;
                case KeyEvent.KEYCODE_9: {
                    tvScanBarcode.append("9");
                    tvScanBarcode.requestFocus();
                    tvScanBarcode.requestFocusFromTouch();
                }
                break;
                case KeyEvent.KEYCODE_0: {
                    tvScanBarcode.append("0");
                    tvScanBarcode.requestFocus();
                    tvScanBarcode.requestFocusFromTouch();
                }
                break;
                default:
                    break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        //nfc read smart card id
        readFromIntent(intent);

//        if(myFiscalDevice == null || !myFiscalDevice.isConnectedDeviceV2())
//            fiscal_printer.setImageDrawable(getResources().getDrawable(R.drawable.fiscal_off));
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
                        Log.d("NFC", "MifareUltralight " + sb.toString());
                        //TODO if ultra light card


                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            //MifareUltralight disconected
                            mUltra.close();
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
                        Log.d("NFC", "MifareClassic " + sb.toString());

                        //TODO if ultra light card

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            //Mifare Classic disconected
                            mfc.close();
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

//
//    private void initFiscalService(){
//        String ip = getSharedPreferences(SharedPrefFiscalService, MODE_PRIVATE).getString("IpAdressFiscalService",null);
//        String port = getSharedPreferences(SharedPrefFiscalService, MODE_PRIVATE).getString("PortFiscalService",null);
//
//        if(ip != null && port != null){
//            String uri = ip + ":" + port;
//
//            CommandServices commandServices = ApiUtils.commandFPService(uri);
//            Call<SimpleResult> call = commandServices.getState();
//            call.enqueue(new Callback<SimpleResult>() {
//                @Override
//                public void onResponse(Call<SimpleResult> call, Response<SimpleResult> response) {
//                    SimpleResult result = response.body();
//                    if(result != null){
//                        int errorCode = result.getErrorCode();
//                        String errorMsg = result.getErrorMessage();
//
//                        if(errorCode == 0){
//                            fiscal_printer.setImageDrawable(getResources().getDrawable(R.drawable.fiscal_on));
//                        }
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<SimpleResult> call, Throwable t) {
//                    String msg = t.getMessage();
//                }
//            });
//        }
//    }
//

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private static void initRecyclerView(){
        final RealmResults<BillString>[] results = new RealmResults[]{null};
        final Bill[] bill = new Bill[1];
        Shift shift = BaseApplication.getInstance().getShift();
        if(shift != null){
            mRealm.executeTransaction(realm -> {
                bill[0] = mRealm.where(Bill.class).equalTo("shiftId", shift.getId()).and().equalTo("state", BaseEnum.BILL_OPEN).and().equalTo("id",openedBillId).findFirst();
                if (bill[0] != null) {
                    openedBillId = bill[0].getId();
                    results[0] = mRealm.where(BillString.class).equalTo("billID", openedBillId).and().equalTo("isDeleted", false).sort("createDate", Sort.DESCENDING).findAll();
                }
            });
            adapter = new NewBillStringsRealmRCAdapter(results[0],false);

            recyclerView.setAdapter(adapter);
            if (adapter.getItemCount() > 0)
                recyclerView.smoothScrollToPosition(0);

            if(bill[0] != null){
                btnPay.setText("MDL " + String.format("%.2f", bill[0].getSumWithDiscount()).replace(",","."));
                tvDiscountBill.setText(String.format("%.2f",bill[0].getSum() - bill[0].getSumWithDiscount()).replace(",","."));
                tvSubTotalBill.setText(String.format("%.2f", bill[0].getSum()).replace(",","."));
            }
            else{
                btnPay.setText(context.getString(R.string.text_mdl_0));
                tvDiscountBill.setText("0.00");
                tvSubTotalBill.setText("0.00");
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private void functionOpenedShift(){
        postToastMessage(getString(R.string.message_shift_is_opened));
        shiftOpenButtonPay = false;
        btnPay.setEnabled(true);
        btnPay.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        FragmentBills.showBillList();
        btnPay.setText(context.getString(R.string.text_mdl_0));
    }

    private void findOpenedShift(){
        mRealm.executeTransaction(realm ->{
            Shift result = realm.where(Shift.class).equalTo("closed",false).findFirst();
            if(result != null) {
                Shift shift = realm.copyFromRealm(result);
                BaseApplication.getInstance().setShift(shift);
            }
        });
    }

    private void closeShift(){
        Shift shift = BaseApplication.getInstance().getShift();
        RealmResults<Bill> billEntryResult = mRealm.where(Bill.class)
                .equalTo("shiftId",shift.getId())
                .and()
                .equalTo("state",0)
                .findAll();
        if(!billEntryResult.isEmpty()){
            new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                    .setTitle(R.string.message_attention)
                    .setMessage(getString(R.string.message_cannot_close_shift_bill_active) + billEntryResult.size() + getString(R.string.message_open_bills))
                    .setCancelable(false)
                    .setPositiveButton(R.string.btn_ok, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        btnPay.setEnabled(true);
                    })
                    .show();
        }
        else{
            long close = new Date().getTime();
            shift.setClosedBy(BaseApplication.getInstance().getUserId());
            shift.setEndDate(close);
            shift.setClosed(true);

            mRealm.executeTransaction(realm -> {
                RealmResults<Shift> shifts = realm.where(Shift.class).equalTo("id", shift.getId()).findAll();
                shifts.setString("closedBy",BaseApplication.getInstance().getUserId());
                shifts.setString("closedByName",BaseApplication.getInstance().getUser().getFullName());
                shifts.setLong("endDate", close);
                shifts.setBoolean("closed", true);
                shifts.setBoolean("isSended",false);
            });
            History history = new History();
            history.setDate(new Date().getTime());
            history.setMsg("Shift: " + shift.getName());
            history.setType(BaseEnum.History_ClosedShift);
            mRealm.executeTransaction(realm -> realm.insert(history));

            int workFisc = getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getInt("ModeFiscalWork",BaseEnum.FISCAL_SERVICE);

            if(workFisc == BaseEnum.FISCAL_DEVICE) {
                if (datecsFiscalDevice != null && datecsFiscalDevice.isConnectedDeviceV2())
                    printZReport();
            }
            if(workFisc == BaseEnum.FISCAL_SERVICE){
                String uri = getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getString("FiscalServiceAddress","0.0.0.0:1111");
                printZReportFiscalService(uri);
            }
            shiftClosedButtonPay = false;
            shiftOpenButtonPay = true;
            btnPay.setEnabled(true);
            btnPay.setText(context.getString(R.string.text_open_shift));
        }
    }

    //start timer function
    void startTimer(long time) {
        final boolean[] showDialogs = {false};
        long timeSettings = getSharedPreferences(SharedPrefSettings, Context.MODE_PRIVATE).getLong("ShiftNotificationSettings",0);
        if(timeSettings != 0){
            countDownShiftTimer = new CountDownTimer(time, 1000) {
                public void onTick(long millisUntilFinished) {
                    long second = (millisUntilFinished / 1000) % 60;
                    long minute = (millisUntilFinished / (1000 * 60)) % 60;
                    long hour = (millisUntilFinished / (1000 * 60 * 60)) % 24;

                    String time = String.format("%02d:%02d:%02d", hour, minute, second);
                    Log.d("TimerShift", "Shift need closed! " + time);


                    if(millisUntilFinished <= timeSettings && !showDialogs[0]){
                        long secondEnd = (millisUntilFinished / 1000) % 60;
                        long minuteEnd = (millisUntilFinished / (1000 * 60)) % 60;
                        String timeSec = String.format("%02d",secondEnd);
                        String timeMin = String.format("%02d", minuteEnd);

                        new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                .setTitle(R.string.message_attention)
                                .setMessage(timeMin +  " minutes, " + timeSec + " sec. are left before the shift closes.")
                                .setCancelable(false)
                                .setPositiveButton(R.string.btn_ok , (dialogInterface, i) -> {
                                    dialogInterface.dismiss();
                                })
                                .show();
                        showDialogs[0] = true;
                    }

                }
                public void onFinish() {
                    btnPay.setText(R.string.text_close_shift);
                    btnPay.setBackgroundColor(getResources().getColor(R.color.btnPay));
                    shiftClosedButtonPay = true;

                    new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                            .setTitle(R.string.message_attention)
                            .setMessage(R.string.message_shift_expired_want_close)
                            .setCancelable(false)
                            .setPositiveButton(R.string.btn_yes, (dialogInterface, i) -> {
                                closeShift();
                            })
                            .setNegativeButton(R.string.btn_no,((dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            }))
                            .show();
                }
            };
            countDownShiftTimer.start();
        }
        else{
            cancelTimer();
        }

    }
    //cancel timer
    void cancelTimer() {
        if(countDownShiftTimer != null){
            countDownShiftTimer.cancel();
        }
    }

    private void initUSBDevice() {
        if (mUSBManager != null) {
            HashMap<String, UsbDevice> deviceList = mUSBManager.getDeviceList();

            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            while (deviceIterator.hasNext()) {
                UsbDevice device = deviceIterator.next();

                if ((device.getVendorId() == DATECS_USB_VID) || (device.getVendorId() == FTDI_USB_VID && device.getManufacturerName().equals("Datecs")) ) {
                    if(mUSBManager.hasPermission(device)){
                        AbstractConnector connector = new UsbDeviceConnector(this, mUSBManager, device);
                        deviceConnect(connector);
                    }
                    else{
                        mUSBManager.requestPermission(device, mPermissionIntent);
                    }
                }
                else if(device.getManufacturerName().equals("ACS")) {
                    if(mUSBManager.hasPermission(device)){
                        new OpenTask().execute(device);
                    }
                    else{
                        mUSBManager.requestPermission(device, mPermissionIntent);
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
                        Log.d("TAG", "run: item connect");
                    } catch (Exception e) {
                        return;
                    }

                    try {
                        PrinterManager.instance.init(item);
                    } catch (Exception e) {
                        try {
                            item.close();

                        } catch (IOException e1) {
                            postToastMessage("Error e1: " + e1.getMessage() + " close");

                            e1.printStackTrace();
                        }
                        postToastMessage("Error: " + e.getMessage());
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
                }
            }
        });
        thread.start();
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
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }

    public static void postToastMessage(final String message) {
        if(!activity.isDestroyed())
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
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

    public static Context getContext (){
        return context;
    }

    public static Activity getActivity (){
        return activity;
    }

    private static class OpenTask extends AsyncTask<UsbDevice, Void, Exception> {
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
                postToastMessage(result.toString());
            }
            else {
                postToastMessage("Reader fail: " + mReader.getReaderName());
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

    public static void printZReport(){
        progressDialogPrintReport = new ProgressDialog(context,R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog);
        progressDialogPrintReport.setTitle(context.getString(R.string.message_z_report_starting));
        progressDialogPrintReport.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialogPrintReport.setCancelable(false);
        progressDialogPrintReport.show();
        final int[] reportNumber = {0};
        final cmdReport.ReportSummary reportSummary = new cmdReport.ReportSummary();

        new Thread(new Runnable() {
            @Override
            public void run() {
                // do the thing that takes a long time
                try {
                    cmdReport cmd = new cmdReport();
                    reportNumber[0] = cmd.PrintZreport(reportSummary);

                    History history = new History();
                    history.setDate(new Date().getTime());
                    history.setMsg(context.getString(R.string.message_z_report) + reportNumber[0]);
                    history.setType(BaseEnum.History_Printed_Z);
                    mRealm.executeTransaction(realm -> realm.insert(history));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    progressDialogPrintReport.dismiss();
                }

               activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "Z report print!", Toast.LENGTH_SHORT).show();

//                        View dialogView = inflater.inflate(R.layout.dialog_x_z_total,null);
//
//                        final AlertDialog dialog_summary = new AlertDialog.Builder(activity,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
//                        dialog_summary.setCancelable(false);
//                        dialog_summary.setView(dialogView);
//                        dialog_summary.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//
//                        TextView tvTaxA = dialogView.findViewById(R.id.tvTaxA);
//                        TextView tvTaxB = dialogView.findViewById(R.id.tvTaxB);
//                        TextView tvTaxC = dialogView.findViewById(R.id.tvTaxC);
//                        TextView tvTaxD = dialogView.findViewById(R.id.tvTaxD);
//                        TextView tvTaxE = dialogView.findViewById(R.id.tvTaxE);
//                        TextView tvTaxF = dialogView.findViewById(R.id.tvTaxF);
//                        TextView tvTaxG = dialogView.findViewById(R.id.tvTaxG);
//                        TextView tvTaxH = dialogView.findViewById(R.id.tvTaxH);
//                        Button btnOk = dialogView.findViewById(R.id.btn_total_reports);
//
//                        btnOk.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                dialog_summary.dismiss();
//                            }
//                        });
//
//                        tvTaxA.setText(String.valueOf(reportSummary.totalA));
//                        tvTaxB.setText(String.valueOf(reportSummary.totalB));
//                        tvTaxC.setText(String.valueOf(reportSummary.totalC));
//                        tvTaxD.setText(String.valueOf(reportSummary.totalD));
//                        tvTaxE.setText(String.valueOf(reportSummary.totalE));
//                        tvTaxF.setText(String.valueOf(reportSummary.totalF));
//                        tvTaxG.setText(String.valueOf(reportSummary.totalG));
//                        tvTaxH.setText(String.valueOf(reportSummary.totalH));
//
//                        dialog_summary.show();
//
//                        DisplayMetrics metrics = new DisplayMetrics(); //get metrics of screen
//                        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
//                        int width = (int) (metrics.widthPixels * 0.4); //set width to 50% of display
//                        int height = (int) (metrics.heightPixels * 0.9); //set height to 90% of display
//                        dialog_summary.getWindow().setLayout(width, height); //set layout
                        progressDialogPrintReport.dismiss();
                    }
                });
            }
        }).start();
    }
    public static void printZReportFiscalService(String uri) {
        progressDialogPrintReport = new ProgressDialog(activity,R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog);
        progressDialogPrintReport.setTitle(context.getString(R.string.message_z_report_working));
        progressDialogPrintReport.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialogPrintReport.setCancelable(false);
        progressDialogPrintReport.show();

        CommandServices commandServices = ApiUtils.commandFPService(uri);
        Call<SimpleResult> responseCall = commandServices.printZReport(BaseEnum.FiscalPrint_Master);

        responseCall.enqueue(new Callback<SimpleResult>() {
            @Override
            public void onResponse(Call<SimpleResult> call, Response<SimpleResult> response) {
                SimpleResult zResponse = response.body();
                if (zResponse != null) {
                    int errorCode = zResponse.getErrorCode();
                    if (errorCode == 0) {
                        progressDialogPrintReport.dismiss();
                        History history = new History();
                        history.setDate(new Date().getTime());
                        history.setMsg(context.getString(R.string.message_report_z_printed_task_id) + zResponse.getTaskId());
                        history.setType(BaseEnum.History_Printed_Z);
                        mRealm.executeTransaction(realm -> realm.insert(history));
                    }

                }
            }

            @Override
            public void onFailure(Call<SimpleResult> call, Throwable t) {
                progressDialogPrintReport.dismiss();
                Toast.makeText(activity, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
    private static void paymentBill(double summBill){
//        sumBillToPay = Double.valueOf(btnPay.getText().toString().replace("MDL ",""));
        sumBillToPay = summBill;
        View dialogView = inflater.inflate(R.layout.dialog_payment_bill_version0, null);


        paymentDialog = new Dialog(context,R.style.CustomDialog);
        paymentDialog.setCancelable(false);
        paymentDialog.setContentView(dialogView);
        paymentDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        tvInputSumBillForPayment = dialogView.findViewById(R.id.tv_pay_input_data_sum);
        TextView tvTotalBill = dialogView.findViewById(R.id.txt_total_payment);
        tvToPay = dialogView.findViewById(R.id.txt_topay_payment);
        TextView tvChange = dialogView.findViewById(R.id.txt_rest_payment);
        ImageButton btn_Cancel = dialogView.findViewById(R.id.btnClose_payment);
        MaterialButton clear = dialogView.findViewById(R.id.btn_pay_ce);
        ImageButton delete = dialogView.findViewById(R.id.btn_pay_delete_number);
        btnCreditCard = dialogView.findViewById(R.id.btn_card_payment);
        btnCashPay = dialogView.findViewById(R.id.btn_cash_payment);
        MaterialButton btnOtherPay = dialogView.findViewById(R.id.btn_other_payment);

        //caut daca contul a fost achitat partial, si adaug text mai jos cu ce tip de plata si ce suma
        //plus la asta daca este vreo achitare deja facuta, verific daca este necesar de imprimat bonul fiscal si daca da, filtrez tipruile de plata dupa criteriu - printFiscalReceip
        RealmResults<BillPaymentType> billPaymentTypes = mRealm.where(BillPaymentType.class).equalTo("billID",openedBillId).findAll();

        if(!billPaymentTypes.isEmpty()){
            for (int i = 0; i < billPaymentTypes.size(); i++){
                BillPaymentType paymentType = billPaymentTypes.get(i);
                if (paymentType != null) {
                    billPaymentedSum += paymentType.getSum();
                }
            }
            PaymentType paymentTypesResult = mRealm.where(PaymentType.class).equalTo("externalId", billPaymentTypes.get(0).getPaymentTypeID()).findFirst();
            if(paymentTypesResult != null){
                onlyFiscalPay = paymentTypesResult.getPrintFiscalCheck();  // este achitare fiscala
            }
            billInitPay = true;  //contul are macar o achitare
        }
        else{
            billInitPay = false;
            onlyFiscalPay = false;
        }

        //caut tipurile de plata care sunt in baza si le adaug la cash si card plus altele
        RealmResults<PaymentType> paymentTypesResult = mRealm.where(PaymentType.class).findAll();
        if(!paymentTypesResult.isEmpty()){
            for(int o = 0; o <paymentTypesResult.size(); o++){
                PaymentType paymentType = new PaymentType();
                paymentType.setCode(paymentTypesResult.get(o).getCode());
                paymentType.setPredefinedIndex(paymentTypesResult.get(o).getPredefinedIndex());
                paymentType.setPrintFiscalCheck(paymentTypesResult.get(o).getPrintFiscalCheck());
                paymentType.setExternalId(paymentTypesResult.get(o).getExternalId());
                paymentType.setName(paymentTypesResult.get(o).getName());

                if(paymentTypesResult.get(o).getPredefinedIndex() == BaseEnum.Pay_Cash) {
                    btnCashPay.setTag(paymentType);
                    btnCashPay.setOnClickListener(clickListenerDynamicPayButton);

                    if(!onlyFiscalPay && billInitPay)
                        btnCashPay.setEnabled(false);   //daca este vreo achitare nefiscala , dezactivez buton cash
                }
                else if(paymentTypesResult.get(o).getPredefinedIndex() == BaseEnum.Pay_CreditCard){
                    btnCreditCard.setTag(paymentType);
                    btnCreditCard.setOnClickListener(clickListenerDynamicPayButton);

                    if(!onlyFiscalPay && billInitPay)
                        btnCreditCard.setEnabled(false);   //daca este vreo achitare nefiscala , dezactivez buton card
                }
            }
        }

        tvInputSumBillForPayment.setText(String.format("%.2f",sumBillToPay - billPaymentedSum).replace(",","."));
        tvTotalBill.setText(String.format("%.2f",sumBillToPay).replace(",","."));
        tvToPay.setText(String.format("%.2f",sumBillToPay - billPaymentedSum).replace(",","."));

        MaterialButton number_1 = dialogView.findViewById(R.id.btn_pay_1);
        MaterialButton number_2 = dialogView.findViewById(R.id.btn_pay_2);
        MaterialButton number_3 = dialogView.findViewById(R.id.btn_pay_3);
        MaterialButton number_4 = dialogView.findViewById(R.id.btn_pay_4);
        MaterialButton number_5 = dialogView.findViewById(R.id.btn_pay_5);
        MaterialButton number_6 = dialogView.findViewById(R.id.btn_pay_6);
        MaterialButton number_7 = dialogView.findViewById(R.id.btn_pay_7);
        MaterialButton number_8 = dialogView.findViewById(R.id.btn_pay_8);
        MaterialButton number_9 = dialogView.findViewById(R.id.btn_pay_9);
        MaterialButton number_0 = dialogView.findViewById(R.id.btn_pay_0);
        MaterialButton number_50 = dialogView.findViewById(R.id.btn_pay_50);
        MaterialButton number_100 = dialogView.findViewById(R.id.btn_pay_100);
        MaterialButton number_200 = dialogView.findViewById(R.id.btn_pay_200);
        MaterialButton number_500 = dialogView.findViewById(R.id.btn_pay_500);
        MaterialButton point = dialogView.findViewById(R.id.btn_pay_point);

        number_1.setOnClickListener(v1 -> addNumberToSumBill("1"));
        number_2.setOnClickListener(v12 -> addNumberToSumBill("2"));
        number_3.setOnClickListener(v13 -> addNumberToSumBill("3"));
        number_4.setOnClickListener(v14 -> addNumberToSumBill("4"));
        number_5.setOnClickListener(v15 -> addNumberToSumBill("5"));
        number_6.setOnClickListener(v16 -> addNumberToSumBill("6"));
        number_7.setOnClickListener(v17 -> addNumberToSumBill("7"));
        number_8.setOnClickListener(v18 -> addNumberToSumBill("8"));
        number_9.setOnClickListener(v19 -> addNumberToSumBill("9"));
        number_0.setOnClickListener(v110 -> addNumberToSumBill("0"));
        number_50.setOnClickListener(v112 -> tvInputSumBillForPayment.setText("50"));
        number_100.setOnClickListener(v113 -> tvInputSumBillForPayment.setText("100"));
        number_200.setOnClickListener(v114 -> tvInputSumBillForPayment.setText("200"));
        number_500.setOnClickListener(v115 -> tvInputSumBillForPayment.setText("500"));

        tvInputSumBillForPayment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(tvInputSumBillForPayment.getText().toString().equals("")){
                    tvChange.setText("0.00");
                }
                else{
                    double incasat = 0.0;
                    try{
                        incasat = Double.parseDouble(tvInputSumBillForPayment.getText().toString());
                    }catch (Exception e){
                        incasat = Double.parseDouble(tvInputSumBillForPayment.getText().toString().replace(",","."));
                    }

                    if( (incasat + billPaymentedSum) <= sumBillToPay){
                        tvChange.setText("0.00");
                    }else{
                        tvChange.setText( String.format("%.2f", (incasat + billPaymentedSum) - sumBillToPay).replace(",","."));
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        point.setOnClickListener(v117 -> {
            String test = tvInputSumBillForPayment.getText().toString();
            boolean contains = false;
            for (int i = 0; i < test.length(); i++) {
                String chars = String.valueOf(test.charAt(i));
                if (chars.equals(".")) {
                    contains = true;
                }
            }
            if (!contains) {
                if(tvInputSumBillForPayment.getText().toString().equals(""))
                    tvInputSumBillForPayment.append("0.");
                else
                    tvInputSumBillForPayment.append(".");
            }
        });

        delete.setOnClickListener(v118 -> { if (!tvInputSumBillForPayment.getText().toString().equals("")) tvInputSumBillForPayment.setText(tvInputSumBillForPayment.getText().toString().substring(0, tvInputSumBillForPayment.getText().toString().length() - 1)); });
        clear.setOnClickListener(v119 -> tvInputSumBillForPayment.setText(""));
        btn_Cancel.setOnClickListener(v120 -> {
            paymentDialog.dismiss();
            billPaymentedSum = 0;
            btnPay.setEnabled(true);
            if(drawer.isDrawerOpen(GravityCompat.END))
                drawer.closeDrawer(GravityCompat.END);
        });
        btnOtherPay.setOnClickListener(view -> {
            showOtherPaymentType();
        });

        paymentDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        paymentDialog.show();


        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(paymentDialog.getWindow().getAttributes());
        int dialogWindowWidth = (int) (displayWidth * 0.4f);
        int dialogWindowHeight = (int) (displayHeight * 0.95f);
        layoutParams.width = dialogWindowWidth;
        layoutParams.height = dialogWindowHeight;  //LinearLayout.LayoutParams.WRAP_CONTENT
        paymentDialog.getWindow().setAttributes(layoutParams);
//
//        paymentDialog.getWindow().setLayout(470,LinearLayout.LayoutParams.WRAP_CONTENT);
        paymentDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        paymentDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    private static void addNumberToSumBill(String number){
        String text = tvInputSumBillForPayment.getText().toString();
        if(text.contains(".")){
            int index = text.indexOf(".");
            if(text.length() - 1 <= index || text.length() - 2 == index){
                tvInputSumBillForPayment.append(number);
            }
        }
        else{
            tvInputSumBillForPayment.append(number);
        }
    }

    private static void showOtherPaymentType(){
        mRealm.executeTransaction(realm -> {
            RealmResults<PaymentType> result = realm.where(PaymentType.class)
                    .notEqualTo("predefinedIndex",BaseEnum.Pay_Cash)
                    .and()
                    .notEqualTo("predefinedIndex",BaseEnum.Pay_CreditCard)
                    .findAll();
            if(!result.isEmpty()){
                List<PaymentType> list = new ArrayList<>();
                list.addAll(result);

                OtherPaymentTypes adapter = new OtherPaymentTypes(list);

//                ListAdapter adapterDialog = new ArrayAdapter<PaymentType>(context, R.layout.item_workplace_main_dialog, list) {
//
//                    ViewHolder holder;
//
//                    class ViewHolder {
//                        TextView title;
//                    }
//
//                    public View getView(int position, View convertView, ViewGroup parent) {
//                        if (convertView == null) {
//                            convertView = inflater.inflate(R.layout.item_workplace_main_dialog, null);
//
//                            holder = new ViewHolder();
//                            holder.title = (TextView) convertView.findViewById(R.id.textView122);
//                            convertView.setTag(holder);
//                        } else {
//                            // view already defined, retrieve view holder
//                            holder = (ViewHolder) convertView.getTag();
//                        }
//                        holder.title.setText(list.get(position).getName());
//                        holder.title.setTag(list.get(position));
//
//                        return convertView;
//                    }
//                };

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.message_select_other_pay_type);
                builder.setAdapter(adapter, (dialog, position) -> {
                    PaymentType paySelected = list.get(position);

                    setClickListenerOtherPay(paySelected);

                    dialog.dismiss();
                });
                builder.setNegativeButton(R.string.btn_cancel,(dialogInterface, i) -> dialogInterface.dismiss());

                AlertDialog alert = builder.create();

                alert.show();

                int displayWidth = displayMetrics.widthPixels;
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(alert.getWindow().getAttributes());
                int dialogWindowWidth = (int) (displayWidth * 0.4f);
                layoutParams.width = dialogWindowWidth;
                layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                alert.getWindow().setAttributes(layoutParams);
            }
        });
    }

   private static void setClickListenerOtherPay(PaymentType paymentType){
       boolean printFiscalCheck = paymentType.getPrintFiscalCheck();
       String code = paymentType.getCode();
       if(code == null)
           code = "404";
       int resultCloseReceip = 0;

       //primesc rindurile la cont
       RealmList<BillString> billStrings = new RealmList<>();
       RealmList<BillPaymentType> billPaymentTypes = new RealmList<>();

       RealmResults<BillString> billStringsResult = mRealm.where(BillString.class)
               .equalTo("billID", openedBillId)
               .and()
               .equalTo("isDeleted",false)
               .findAll();
       if (!billStringsResult.isEmpty()) {
           billStrings.addAll(billStringsResult);
       }

       //tipurile de achitare deja facute la cont in caz ca nu a fost achitat integral
       RealmResults<BillPaymentType> billPayResult = mRealm.where(BillPaymentType.class)
               .equalTo("billID", openedBillId).findAll();
       if(!billPayResult.isEmpty()){
           billPaymentTypes.addAll(billPayResult);
       }

       int bilNumber = 0;
       Bill bilResult = mRealm.where(Bill.class).equalTo("id",openedBillId).findFirst();
       if(bilResult != null)
           bilNumber = bilResult.getShiftReceiptNumSoftware();

       if(!tvInputSumBillForPayment.getText().toString().equals("")){
           double inputSum = 0;
           try {
               inputSum = Double.parseDouble(tvInputSumBillForPayment.getText().toString());
           } catch (Exception e) {

               inputSum = Double.parseDouble(tvInputSumBillForPayment.getText().toString().replace(",", "."));
           }



           if ((billPaymentedSum + inputSum) >= sumBillToPay) {
               int modeFiscalWork = context.getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getInt("ModeFiscalWork",BaseEnum.NONE_SELECTED_FISCAL_MODE);

               if(printFiscalCheck){
                   if(modeFiscalWork == BaseEnum.FISCAL_DEVICE){
                       DatecsFiscalDevice fiscalDevice = null;
                       if(BaseApplication.getInstance().getMyFiscalDevice() != null){
                           fiscalDevice = BaseApplication.getInstance().getMyFiscalDevice();
                       }
                       if(fiscalDevice != null && fiscalDevice.isConnectedDeviceV2()){
                           resultCloseReceip = BaseApplication.getInstance().printFiscalReceipt(fiscalReceipt, billStrings, paymentType, inputSum, billPaymentTypes,bilNumber);
                           if (resultCloseReceip != 0) {
                               BillPaymentType billPaymentType= new BillPaymentType();
                               billPaymentType.setId(UUID.randomUUID().toString());
                               billPaymentType.setBillID(openedBillId);
                               billPaymentType.setName(paymentType.getName());
                               billPaymentType.setPaymentCode(Integer.valueOf(code));
                               billPaymentType.setPaymentTypeID(paymentType.getExternalId());
                               billPaymentType.setSum(inputSum);
                               billPaymentType.setAuthor(BaseApplication.getInstance().getUser().getId());
                               billPaymentType.setCreateDate(new Date().getTime());

                               int finalResultCloseReceip = resultCloseReceip;
                               mRealm.executeTransaction(realm ->{
                                   Bill bill = realm.where(Bill.class).equalTo("id",openedBillId).findFirst();
                                   if(bill != null){
                                       bill.setReceiptNumFiscalMemory(finalResultCloseReceip);
                                       bill.setState(1);
                                       bill.setCloseDate(new Date().getTime());
                                       bill.setClosedBy(BaseApplication.getInstance().getUser().getId());
                                       bill.getBillPaymentTypes().add(billPaymentType);

                                   }
                               });

                               initRecyclerView();

                               billPaymentedSum = 0;
                               paymentDialog.dismiss();
                               btnPay.setEnabled(true);

                               if(drawer.isDrawerOpen(GravityCompat.END))
                                   drawer.closeDrawer(GravityCompat.END);
                               openedBillId = null;
                           }
                       }
                       else{
                           Toast.makeText(context, R.string.message_fiscal_device_not_connected, Toast.LENGTH_SHORT).show();
                       }
                   }
                   if(modeFiscalWork == BaseEnum.FISCAL_SERVICE){
                       BaseApplication.getInstance().printReceiptFiscalService(billStrings, paymentType, inputSum, billPaymentTypes,"1");

                       BillPaymentType billPaymentType= new BillPaymentType();
                       billPaymentType.setId(UUID.randomUUID().toString());
                       billPaymentType.setBillID(openedBillId);
                       billPaymentType.setName(paymentType.getName());
                       billPaymentType.setPaymentCode(Integer.valueOf(code));
                       billPaymentType.setPaymentTypeID(paymentType.getExternalId());
                       billPaymentType.setSum(inputSum);
                       billPaymentType.setAuthor(BaseApplication.getInstance().getUser().getId());
                       billPaymentType.setCreateDate(new Date().getTime());

                       mRealm.executeTransaction(realm ->{
                           Bill bill = realm.where(Bill.class).equalTo("id",openedBillId).findFirst();
                           if(bill != null){
                               bill.setReceiptNumFiscalMemory(0);
                               bill.setState(1);
                               bill.setCloseDate(new Date().getTime());
                               bill.setClosedBy(BaseApplication.getInstance().getUser().getId());
                               bill.getBillPaymentTypes().add(billPaymentType);

                           }
                       });
                   }

               }
               else {
                   BillPaymentType billPaymentType= new BillPaymentType();
                   billPaymentType.setId(UUID.randomUUID().toString());
                   billPaymentType.setBillID(openedBillId);
                   billPaymentType.setName(paymentType.getName());
                   billPaymentType.setPaymentCode(Integer.valueOf(code));
                   billPaymentType.setPaymentTypeID(paymentType.getExternalId());
                   billPaymentType.setSum(sumBillToPay - billPaymentedSum);
                   billPaymentType.setAuthor(BaseApplication.getInstance().getUser().getId());
                   billPaymentType.setCreateDate(new Date().getTime());

                   mRealm.executeTransaction(realm ->{
                       Bill bill = realm.where(Bill.class).equalTo("id",openedBillId).findFirst();
                       if(bill != null){
                           bill.setReceiptNumFiscalMemory(0);
                           bill.setState(1);
                           bill.setCloseDate(new Date().getTime());
                           bill.setClosedBy(BaseApplication.getInstance().getUser().getId());
                           bill.getBillPaymentTypes().add(billPaymentType);
                       }
                   });
                   doAfterCloseBill();
               }
           }
           else if ((billPaymentedSum + inputSum) < sumBillToPay) {
               BillPaymentType billPaymentType = new BillPaymentType();
               billPaymentType.setId(UUID.randomUUID().toString());
               billPaymentType.setBillID(openedBillId);
               billPaymentType.setName(paymentType.getName());
               billPaymentType.setPaymentCode(Integer.valueOf(code));
               billPaymentType.setPaymentTypeID(paymentType.getExternalId());
               billPaymentType.setSum(inputSum);
               billPaymentType.setAuthor(BaseApplication.getInstance().getUser().getId());
               billPaymentType.setCreateDate(new Date().getTime());

               mRealm.executeTransaction(realm ->{
                   Bill bill = realm.where(Bill.class).equalTo("id",openedBillId).findFirst();
                   if(bill != null){
                       bill.setState(0);
                       bill.getBillPaymentTypes().add(billPaymentType);
                   }
               });

               billPaymentedSum = billPaymentedSum + inputSum;
               tvToPay.setText(String.format("%.2f", sumBillToPay - billPaymentedSum).replace(",","."));
               tvInputSumBillForPayment.setText(String.format("%.2f",sumBillToPay - billPaymentedSum).replace(",","."));

               if(!paymentType.getPrintFiscalCheck()){
                   btnCashPay.setEnabled(false);
                   btnCreditCard.setEnabled(false);
               }
           }
       }
       else{
           postToastMessage("Enter sum for pay!");
       }


   }

    private void checkItem_Dialog(int dialog, MaterialButton button){

        final boolean[] isCloseFromEnter = {false};
        View dialogView = inflater.inflate(R.layout.dialog_check_assortment_item, null);

        final Dialog dialogCheckItem = new Dialog(context,R.style.CustomDialog);
        dialogCheckItem.setCancelable(false);
        dialogCheckItem.setContentView(dialogView);
        dialogCheckItem.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        TextView tvInputBarcode = dialogView.findViewById(R.id.et_input_data);
        TextView tvBarcodeOrCodeText = dialogView.findViewById(R.id.text_code_barcode);
        TextView tvNameItem = dialogView.findViewById(R.id.tvName_finded_item);
        TextView tvPriceItem = dialogView.findViewById(R.id.txt_price_dialog);
        TextView tvDialogName = dialogView.findViewById(R.id.txt_dialog_name);
        TextView tvDiscountItem = dialogView.findViewById(R.id.txt_discount_price_dialog);
        ImageButton btn_Cancel = dialogView.findViewById(R.id.btnClose_dialog);
        ImageButton btn_search = dialogView.findViewById(R.id.btn_search_barcode_dialog);
        ImageButton btn_add = dialogView.findViewById(R.id.btn_add_item_to_bill);
        MaterialButton btn_clear = dialogView.findViewById(R.id.btn_clear);
        MaterialButton btn_delete = dialogView.findViewById(R.id.btn_delete);

        MaterialButton number_1 = dialogView.findViewById(R.id.btn_1);
        MaterialButton number_2 = dialogView.findViewById(R.id.btn_2);
        MaterialButton number_3 = dialogView.findViewById(R.id.btn_3);
        MaterialButton number_4 = dialogView.findViewById(R.id.btn_4);
        MaterialButton number_5 = dialogView.findViewById(R.id.btn_5);
        MaterialButton number_6 = dialogView.findViewById(R.id.btn_6);
        MaterialButton number_7 = dialogView.findViewById(R.id.btn_7);
        MaterialButton number_8 = dialogView.findViewById(R.id.btn_8);
        MaterialButton number_9 = dialogView.findViewById(R.id.btn_9);
        MaterialButton number_0 = dialogView.findViewById(R.id.btn_0);

        number_1.setOnClickListener(v121 -> tvInputBarcode.append("1"));
        number_2.setOnClickListener(v122 -> tvInputBarcode.append("2"));
        number_3.setOnClickListener(v123 -> tvInputBarcode.append("3"));
        number_4.setOnClickListener(v124 -> tvInputBarcode.append("4"));
        number_5.setOnClickListener(v125 -> tvInputBarcode.append("5"));
        number_6.setOnClickListener(v126 -> tvInputBarcode.append("6"));
        number_7.setOnClickListener(v127 -> tvInputBarcode.append("7"));
        number_8.setOnClickListener(v128 -> tvInputBarcode.append("8"));
        number_9.setOnClickListener(v129 -> tvInputBarcode.append("9"));
        number_0.setOnClickListener(v130 -> tvInputBarcode.append("0"));

        if(dialog == BaseEnum.Dialog_CheckPrice)
            tvDialogName.setText(R.string.title_dialog_check_price);
        else if (dialog == BaseEnum.Dialog_AddItem)
            tvDialogName.setText(R.string.title_dialog_add_item);

        btn_delete.setOnClickListener(v131 -> {
            if (!tvInputBarcode.getText().toString().equals("")) {
                tvInputBarcode.setText(tvInputBarcode.getText().toString().substring(0, tvInputBarcode.getText().toString().length() - 1));
                btn_search.setVisibility(View.VISIBLE);
                btn_add.setVisibility(View.GONE);
            }
        });
        btn_clear.setOnClickListener(v132 -> {
            tvInputBarcode.setText("");
            btn_add.setVisibility(View.GONE);
            btn_search.setVisibility(View.GONE);
            tvBarcodeOrCodeText.setVisibility(View.VISIBLE);
        });
        tvInputBarcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.equals("")){
                    btn_search.setVisibility(View.VISIBLE);
                    tvBarcodeOrCodeText.setVisibility(View.GONE);
                    btn_add.setVisibility(View.GONE);
                    tvNameItem.setText("");
                    tvPriceItem.setText(context.getString(R.string.text_mdl_0));
                    tvDiscountItem.setText(context.getString(R.string.text_mdl_0));
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        btn_search.setOnClickListener(v133 -> {
            AssortmentRealm assortmentEntry;
            if(tvInputBarcode.getText().toString().length() == 13 || tvInputBarcode.getText().toString().length() == 8){
                assortmentEntry = mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",tvInputBarcode.getText().toString()).or().equalTo("code",tvInputBarcode.getText().toString()).findFirst();
            }
            else{
                assortmentEntry =  mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",tvInputBarcode.getText().toString()).findFirst();
            }
            if(assortmentEntry != null){
                tvInputBarcode.setText("");

                AssortmentRealm assortmentFind = mRealm.copyFromRealm(assortmentEntry);
                tvNameItem.setText(assortmentFind.getName());

                CheckedAssortmentItemToPromo promo = checkedAssortmentItemToPromo(assortmentFind);
                if (promo != null)
                    tvDiscountItem.setText("MDL " + String.format("%.2f", promo.getPromoPrice()).replace(",","."));
                tvPriceItem.setText("MDL " + String.format("%.2f", assortmentFind.getPrice()).replace(",","."));

                btn_add.setVisibility(View.VISIBLE);
                btn_search.setVisibility(View.GONE);
                tvBarcodeOrCodeText.setVisibility(View.GONE);

                btn_add.setOnClickListener(view -> {
                    dialogCheckItem.dismiss();
                    button.setEnabled(true);

                    addItemsToOpenedBill(assortmentFind,1,tvInputBarcode.getText().toString(),true);
                });
            }
            else{
                tvNameItem.setText("");
                tvPriceItem.setText(context.getString(R.string.text_mdl_0));
                tvDiscountItem.setText(context.getString(R.string.text_mdl_0));

                Toast.makeText(MainActivity.this, R.string.message_item_not_found, Toast.LENGTH_SHORT).show();
            }
        });
        btn_Cancel.setOnClickListener(v134 -> {
            if(!isCloseFromEnter[0]){
                dialogCheckItem.dismiss();
                button.setEnabled(true);
            }
            isCloseFromEnter[0] = false;
        });



        // Set the dialog to not focusable.
//        dialogCheckItem.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialogCheckItem.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialogCheckItem.show();

        tvInputBarcode.requestFocus();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialogCheckItem.getWindow().getAttributes());
        int dialogWindowWidth = (int) (displayWidth * 0.4f);
        int dialogWindowHeight = (int) (displayHeight * 0.95f);
        layoutParams.width = dialogWindowWidth;
        layoutParams.height = dialogWindowHeight;
        dialogCheckItem.getWindow().setAttributes(layoutParams);

        dialogCheckItem.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        dialogCheckItem.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        DialogInterface.OnKeyListener keylistener = new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent KEvent) {
                int keyaction = KEvent.getAction();

                if(keyaction == KeyEvent.ACTION_DOWN)
                {
                    int keycode = KEvent.getKeyCode();

                    switch (keycode) {
                        case KeyEvent.KEYCODE_1 : {
                            tvInputBarcode.append("1");
                        }break;
                        case KeyEvent.KEYCODE_2 : {
                            tvInputBarcode.append("2");
                        }break;
                        case KeyEvent.KEYCODE_3 : {
                            tvInputBarcode.append("3");
                        }break;
                        case KeyEvent.KEYCODE_4 : {
                            tvInputBarcode.append("4");
                        }break;
                        case KeyEvent.KEYCODE_5 : {
                            tvInputBarcode.append("5");
                        }break;
                        case KeyEvent.KEYCODE_6 : {
                            tvInputBarcode.append("6");
                        }break;
                        case KeyEvent.KEYCODE_7 : {
                            tvInputBarcode.append("7");
                        }break;
                        case KeyEvent.KEYCODE_8 : {
                            tvInputBarcode.append("8");
                        }break;
                        case KeyEvent.KEYCODE_9 : {
                            tvInputBarcode.append("9");
                        }break;
                        case KeyEvent.KEYCODE_0 : {
                            tvInputBarcode.append("0");
                        }break;
                        case KeyEvent.KEYCODE_ENTER : {
                            AssortmentRealm assortmentEntry;
                            if(tvInputBarcode.getText().toString().length() == 13 || tvInputBarcode.getText().toString().length() == 8){
                                assortmentEntry = mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",tvInputBarcode.getText().toString()).or().equalTo("code",tvInputBarcode.getText().toString()).findFirst();
                            }
                            else{
                                assortmentEntry =  mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",tvInputBarcode.getText().toString()).findFirst();
                            }
                            if(assortmentEntry != null) {
                                tvInputBarcode.setText("");

                                AssortmentRealm assortmentFind = mRealm.copyFromRealm(assortmentEntry);
                                tvNameItem.setText(assortmentFind.getName());

                                CheckedAssortmentItemToPromo promo = checkedAssortmentItemToPromo(assortmentFind);
                                if (promo != null)
                                    tvDiscountItem.setText("MDL " + String.format("%.2f", promo.getPromoPrice()).replace(",", "."));
                                tvPriceItem.setText("MDL " + String.format("%.2f", assortmentFind.getPrice()).replace(",", "."));

                                btn_add.setVisibility(View.VISIBLE);
                                btn_search.setVisibility(View.GONE);
                                tvBarcodeOrCodeText.setVisibility(View.GONE);

                                btn_add.setOnClickListener(view -> {
                                    dialogCheckItem.dismiss();
                                    button.setEnabled(true);
                                    addItemsToOpenedBill(assortmentFind, 1, tvInputBarcode.getText().toString(), true);
                                });
                                isCloseFromEnter[0] = true;
                            }
                            else{
                                tvNameItem.setText("");
                                tvPriceItem.setText(context.getString(R.string.text_mdl_0));
                                tvDiscountItem.setText(context.getString(R.string.text_mdl_0));

                                Toast.makeText(MainActivity.this, R.string.message_item_not_found, Toast.LENGTH_SHORT).show();
                            }
                        }break;
                    }
                }
                return false;
            }
        };
        dialogCheckItem.setOnKeyListener(keylistener );
    }

    private static CheckedAssortmentItemToPromo checkedAssortmentItemToPromo(AssortmentRealm assortmentRealm){
        CheckedAssortmentItemToPromo promoItem = new CheckedAssortmentItemToPromo();
        Promotion promotion = null;

        if(!assortmentRealm.getPromotions().isEmpty()){
            promotion = assortmentRealm.getPromotions().first();

            long startDate = replaceDate(promotion.getStartDate());
            long endDate = replaceDate(promotion.getEndDate());
            Date curentDate = new Date();
            long currDate = curentDate.getTime();

            long timeBegin = 0;
            long timeEnd = 0;

            if(promotion.getTimeBegin() != null)    timeBegin = replaceDate(promotion.getTimeBegin());
            if(promotion.getTimeEnd() != null)    timeEnd = replaceDate(promotion.getTimeEnd());

            if(currDate > startDate && currDate < endDate){
                if(timeBegin != 0 && timeEnd != 0){
                    Date timeStart = new Date(timeBegin);
                    int hourS = timeStart.getHours();
                    int minS = timeStart.getMinutes();

                    Date timeFinis = new Date(timeEnd);
                    int hourE = timeFinis.getHours();
                    int minE = timeFinis.getMinutes();

                    Date one = new Date();
                    one.setHours(hourS);
                    one.setMinutes(minS);
                    one.setSeconds(0);

                    Date two = new Date();
                    two.setHours(hourE);
                    two.setMinutes(minE);
                    two.setSeconds(0);

                    if(hourE < hourS)
                        two.setDate(two.getDate() + 1);

                    if(curentDate.after(one) && curentDate.before(two)){
                        promoItem.setPromoId(promotion.getId());
                        promoItem.setPromoPrice(promotion.getPrice());
                        return promoItem;
                    }
                    else return null;
                }
                else{
                    promoItem.setPromoId(promotion.getId());
                    promoItem.setPromoPrice(promotion.getPrice());
                    return promoItem;
                }
            }
            else return null;
        }
        else return null;
    }

    private static class CheckedAssortmentItemToPromo{
        String promoId;
        double promoPrice;

        String getPromoId() {
            return promoId;
        }

        void setPromoId(String promoId) {
            this.promoId = promoId;
        }

        double getPromoPrice() {
            return promoPrice;
        }

        void setPromoPrice(double promoPrice) {
            this.promoPrice = promoPrice;
        }
    }

    private void readAssortment (final Call<AssortmentListService> assortiment){
        assortiment.enqueue(new Callback<AssortmentListService>() {
            @Override
            public void onResponse(Call<AssortmentListService> call, Response<AssortmentListService> response) {
                AssortmentListService assortiment_body = response.body();
                GetAssortmentListResult result = assortiment_body != null ? assortiment_body.getGetAssortmentListResult() : null;

                int errorecode = 101;
                if (result != null) {
                    errorecode = result.getErrorCode();
                }
                if(errorecode == 0){

                    mRealm.executeTransaction(realm -> {
                        realm.delete(QuickGroupRealm.class);
                        realm.delete(Barcodes.class);
                        realm.delete(Promotion.class);
                        realm.delete(AssortmentRealm.class);
                    });

                    List<AssortmentServiceEntry> assortmentListData = result.getAssortments();
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
                    new UserTask().execute();

                }else{
                    Toast.makeText(MainActivity.this, "Error download assortment! Code: " + errorecode, Toast.LENGTH_SHORT).show();
                    pgH.dismiss();
                }
            }
            @Override
            public void onFailure(Call<AssortmentListService> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error download assortment! Message: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                pgH.dismiss();
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

                    mRealm.executeTransaction(realm -> {
                        realm.delete(PaymentType.class);
                        realm.delete(FiscalDevice.class);
                    });

                    if(result.getPaymentTypes() != null){
                        List<PaymentType> paymentTypes = result.getPaymentTypes();
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
                    pgH.dismiss();

                    viewPager.setAdapter(null);
                    adapterRightMenu = new TabQuickMenuAdapter(context,getSupportFragmentManager());
                    viewPager.setAdapter(adapterRightMenu);
                    viewPager.setOffscreenPageLimit(4);

                    tabLayout = findViewById(R.id.tab_items_right_menu);

                    tabLayout.setupWithViewPager(viewPager);
                    tabLayout.setTabMode(TabLayout.MODE_FIXED);
                    tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

                }else{
                    Toast.makeText(MainActivity.this, "Error download work places! Code: " + errorecode, Toast.LENGTH_SHORT).show();
                    pgH.dismiss();
                }
            }
            @Override
            public void onFailure(Call<WorkPlaceSettings> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error download work places! Message: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                pgH.dismiss();
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

                    mRealm.executeTransaction(realm -> {
                        realm.delete(User.class);
                    });

                    getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).edit().putBoolean("UserAuthentificate",true).apply();
                    List<User> users = result.getUsers();
                    mRealm.executeTransaction(realm -> {
                        for(User user : users){
                            String login = BaseApplication.getInstance().getUser().getUserName();
                            String pass = GetSHA1HashUserPassword("This is the code for UserPass",BaseApplication.getInstance().getUserPasswordsNotHashed()).replace("\n","");
                            if(user.getUserName().equals(login) && user.getPassword().equals(pass)) {
                                BaseApplication.getInstance().setUser(user);
                                tvUserNameNav.setText(String.format("%s %s", user.getFirstName(), user.getLastName()));
                            }
                            realm.insert(user);
                        }
                    });
                    new WorkPlaceTask().execute();

                }else{
                    Toast.makeText(MainActivity.this, "Error download user! Code: " + errorecode, Toast.LENGTH_SHORT).show();
                    pgH.dismiss();
                }
            }
            @Override
            public void onFailure(Call<UserListServiceResult> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error download user! Message: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                pgH.dismiss();
            }
        });
    }

    class AssortmentTask extends AsyncTask<Void,Void,Void> {


        @Override
        protected Void doInBackground(Void... dates) {
            String uri = sharedPreferenceSettings.getString("URI",null);
            CommandServices commandServices = ApiUtils.commandEposService(uri);

            final Call<AssortmentListService> assortiment = commandServices.getAssortiment(tokenId, workPlaceID);
            readAssortment(assortiment);
            return null;
        }
    }
    class UserTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pgH.dismiss();
            pgH.setCancelable(false);
            pgH.setMessage(getString(R.string.message_loading_users));
            pgH.setIndeterminate(true);
            pgH.show();
        }

        @Override
        protected Void doInBackground(Void... dates) {
            String uri = sharedPreferenceSettings.getString("URI",null);
            CommandServices commandServices = ApiUtils.commandEposService(uri);

            final Call<UserListServiceResult> userListServiceResultCall = commandServices.getUsers(tokenId, workPlaceID);
            readUsers(userListServiceResultCall);
            return null;
        }
    }
    class WorkPlaceTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pgH.dismiss();
            pgH.setMessage(getString(R.string.message_loading_workplace_settings));
            pgH.setIndeterminate(true);
            pgH.setCancelable(false);
            pgH.show();
        }

        @Override
        protected Void doInBackground(Void... dates) {
            String uri = sharedPreferenceSettings.getString("URI",null);
            CommandServices commandServices = ApiUtils.commandEposService(uri);

            final Call<WorkPlaceSettings> workPlaceSettingsCall = commandServices.getWorkplaceSettings(tokenId, workPlaceID);
            readWorkPlaceSettings(workPlaceSettingsCall);
            return null;
        }
    }

//    public static void  initUSBDevice() {
//        if (mUSBManager != null) {
//            HashMap<String, UsbDevice> deviceList = mUSBManager.getDeviceList();
//
//            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
//            while (deviceIterator.hasNext()) {
//                UsbDevice device = deviceIterator.next();
//
//                if ((device.getVendorId() == BaseEnum.DATECS_USB_VID) || (device.getVendorId() == BaseEnum.FTDI_USB_VID && device.getManufacturerName().equals("Datecs")) ) {
//                    if(mUSBManager.hasPermission(device)){
//                        AbstractConnector connector = new UsbDeviceConnector(context, mUSBManager, device);
//                        deviceConnect(connector);
//                    }
//                    else{
//                        mUSBManager.requestPermission(device, mPermissionIntent);
//                    }
//                }
//                else if(device.getManufacturerName().equals("ACS")) {
//                    if(mUSBManager.hasPermission(device)){
//                        new OpenTask().execute(device);
//                    }
//                    else{
//                        mUSBManager.requestPermission(device, mPermissionIntent);
//                    }
//                }
//            }
//        }
//    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                v.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    hideSystemUI();
                }

            }
        }
        return super.dispatchTouchEvent(event);
    }

    private static class OtherPaymentTypes implements ListAdapter {

        List<PaymentType> list;

        public OtherPaymentTypes(List<PaymentType> list) {
            this.list = list;
        }

        ViewHolder holder;

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int i) {
            if(onlyFiscalPay && billInitPay && list.get(i).getPrintFiscalCheck())
                return true;
            else if(!onlyFiscalPay && billInitPay && !list.get(i).getPrintFiscalCheck())
                return true;
            else if (!onlyFiscalPay && !billInitPay && !list.get(i).getPrintFiscalCheck())
                return true;
            else
                return false;

        }

        class ViewHolder {
            TextView title;
        }


        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public PaymentType getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return list.get(i).getPredefinedIndex();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_workplace_main_dialog, null);

                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.textView122);
                convertView.setTag(holder);
            } else {
                // view already defined, retrieve view holder
                holder = (ViewHolder) convertView.getTag();
            }

            PaymentType item = list.get(position);
            holder.title.setText(item.getName());
            holder.title.setTag(item);



            convertView.setEnabled( isEnabled(position));

            return convertView;
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return list.size();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}