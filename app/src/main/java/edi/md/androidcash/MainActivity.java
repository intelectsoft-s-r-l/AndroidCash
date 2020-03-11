package edi.md.androidcash;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.UUID;

import edi.md.androidcash.Fragments.FragmentBills;
import edi.md.androidcash.NetworkUtils.PaymentType;
import edi.md.androidcash.NetworkUtils.Promotion;
import edi.md.androidcash.RealmHelper.AssortmentRealm;
import edi.md.androidcash.RealmHelper.Bill;
import edi.md.androidcash.RealmHelper.BillPaymentType;
import edi.md.androidcash.RealmHelper.BillString;
import edi.md.androidcash.RealmHelper.Shift;
import edi.md.androidcash.Utils.BaseEnum;
import edi.md.androidcash.adapters.CustomBillStringRealmListAdapter;
import edi.md.androidcash.adapters.CustomRCBillStringRealmAdapter;
import edi.md.androidcash.adapters.ViewPageAdapterRightMenu;
import edi.md.androidcash.connectors.AbstractConnector;
import edi.md.androidcash.connectors.UsbDeviceConnector;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

import static edi.md.androidcash.BaseApplication.SharedPrefSettings;
import static edi.md.androidcash.BaseApplication.SharedPrefWorkPlaceSettings;
import static edi.md.androidcash.BaseApplication.deviceId;


public class MainActivity extends AppCompatActivity {
    private static Context context;
    private static TextView tvDiscountBill, tvSubTotalBill;
    private TextView tvCheckPriceItem;
    private TextView tvScanBarcode, tvTotalSumBillPay;
    private static MaterialButton btnPay;
    MaterialButton btnNewBill, btnAddItem, btnCheckPrice, btnAddClient;
    private static RecyclerView recyclerView;
    private ViewPager viewPager;
    public static TabLayout tabLayout;

    private static Realm mRealm;

    private static CustomRCBillStringRealmAdapter adapter;

    //declare timer for shift
    private CountDownTimer countDownShiftTimer = null;
    SimpleDateFormat simpleDateFormatMD;
    TimeZone timeZoneMD;

    //Reader ACR
    private Reader mReader;

    //NFC variables
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter writeTagFilters[];

    //USB variables
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private PendingIntent mPermissionIntent;
    private UsbManager mUSBManager;

    //datecs variables
    public DatecsFiscalDevice datecsFiscalDevice = null;
    private cmdReceipt.FiscalReceipt datecsFiscalReceipt;

    private static Button myDynamicPayTypeButton;
    static AlertDialog paymentDialog;
    private static cmdReceipt.FiscalReceipt fiscalReceipt;

    private static String openedBillId;
    private boolean shiftOpenButtonPay = false;
    private boolean shiftClosedButtonPay = false;
    private static double billPaymentedSum;

    private static DrawerLayout drawer;
    private static ConstraintLayout drawerConstraint;
    private static ConstraintLayout navigationView;

    public static LayoutInflater inflater;

    static Display display;

    ViewPageAdapterRightMenu adapterRightMenu;
    static TextView tvInputSumBillForPayment;
    static double sumBillToPay;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if(getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getInt("ModeFiscalWork", BaseEnum.NONE_SELECTED_FISCAL_MODE) == BaseEnum.FISCAL_DEVICE){
                        if (device.getManufacturerName().equals("Datecs")) {
                            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                                AbstractConnector connector = new UsbDeviceConnector(MainActivity.this, mUSBManager, device);
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

                            // Open reader
                            new OpenTask().execute(device);

                            HashMap<String, UsbDevice> deviceList = mUSBManager.getDeviceList();

                            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                            while (deviceIterator.hasNext()) {
                                UsbDevice devices = deviceIterator.next();

                                if ((devices.getVendorId() == BaseEnum.DATECS_USB_VID) || (devices.getVendorId() == BaseEnum.FTDI_USB_VID) && (devices.getManufacturerName().equals("Datecs"))) {
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

                    if(getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getInt("ModeFiscalWork", BaseEnum.NONE_SELECTED_FISCAL_MODE) == BaseEnum.FISCAL_DEVICE){
//                        fiscal_printer.setImageDrawable(getResources().getDrawable(R.drawable.fiscal_off));
                    }
                }
            }
            else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                for (UsbDevice device : mUSBManager.getDeviceList().values()) {
                     if(getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getInt("ModeFiscalWork", BaseEnum.NONE_SELECTED_FISCAL_MODE) == BaseEnum.FISCAL_DEVICE){
                         if (device.getManufacturerName().equals("Datecs")) {
                             mUSBManager.requestPermission(device, mPermissionIntent);
                         }
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

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        registerReceiver(mReceiver, filter);

        setContentView(R.layout.drawer_layout_bill);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = this;
        mRealm = Realm.getDefaultInstance();

        recyclerView = findViewById(R.id.rc_list_billstring);
        btnPay = findViewById(R.id.btn_pay_bill);
        tvDiscountBill = findViewById(R.id.txt_discount_summ_bill);
//        btnSettings = toolbar.findViewById(R.id.img_button_settings);
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

        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        initRecyclerView();
        findOpenedShift();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null)
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();

        simpleDateFormatMD = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        timeZoneMD = TimeZone.getTimeZone("Europe/Chisinau");
        simpleDateFormatMD.setTimeZone(timeZoneMD);

        datecsFiscalReceipt = new cmdReceipt.FiscalReceipt();
        datecsFiscalDevice = ((BaseApplication)getApplication()).getMyFiscalDevice();
        // Initialize reader ACR
        mReader = new Reader(mUSBManager);
        viewPager.setAdapter(null);
        adapterRightMenu = new ViewPageAdapterRightMenu(this, getSupportFragmentManager());
        viewPager.setAdapter(adapterRightMenu);
        viewPager.setOffscreenPageLimit(4);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

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
                    Toast.makeText(MainActivity.this, "Item not found!", Toast.LENGTH_SHORT).show();
                    tvScanBarcode.setText("");
                }
            }
            return false;
        });
//        btnSettings.setOnClickListener(v -> startActivityForResult(new Intent(".Settings"),111));
        btnNewBill.setOnClickListener(v->{
            if(openedBillId != null){
                openedBillId = null;
                initRecyclerView();
            }
        });
        btnAddItem.setOnClickListener(v->{
            View dialogView = inflater.inflate(R.layout.dialog_add_position, null);

            final AlertDialog addPosition = new AlertDialog.Builder(MainActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
            addPosition.setCancelable(false);
            addPosition.setView(dialogView);
            addPosition.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            TextView tv_input_barcode = dialogView.findViewById(R.id.et_input_data);
            TextView tv_barcode = dialogView.findViewById(R.id.text_code_barcode);
            TextView tvNameItem = dialogView.findViewById(R.id.tvName_finded_item);
            TextView tvCategoryNameItem = dialogView.findViewById(R.id.tvCategory_name_item);
            TextView tvPriceItem = dialogView.findViewById(R.id.txt_price_add_item);
            TextView tvDiscountItem = dialogView.findViewById(R.id.txt_discount_price_add_item);
            ImageButton btn_Cancel = dialogView.findViewById(R.id.btnClose_add_item);
            ImageButton btn_search = dialogView.findViewById(R.id.btn_search_barcode_add_item);
            ImageButton btn_add = dialogView.findViewById(R.id.btn_add_item_to_bill);
            MaterialButton btn_clear = dialogView.findViewById(R.id.btn_add_position_clear);
            MaterialButton btn_delete = dialogView.findViewById(R.id.btn_add_position_delete);

            MaterialButton number_1 = dialogView.findViewById(R.id.btn_add_position_1);
            MaterialButton number_2 = dialogView.findViewById(R.id.btn_add_position_2);
            MaterialButton number_3 = dialogView.findViewById(R.id.btn_add_position_3);
            MaterialButton number_4 = dialogView.findViewById(R.id.btn_add_position_4);
            MaterialButton number_5 = dialogView.findViewById(R.id.btn_add_position_5);
            MaterialButton number_6 = dialogView.findViewById(R.id.btn_add_position_6);
            MaterialButton number_7 = dialogView.findViewById(R.id.btn_add_position_7);
            MaterialButton number_8 = dialogView.findViewById(R.id.btn_add_position_8);
            MaterialButton number_9 = dialogView.findViewById(R.id.btn_add_position_9);
            MaterialButton number_0 = dialogView.findViewById(R.id.btn_add_position_0);

            number_1.setOnClickListener(v121 -> tv_input_barcode.append("1"));
            number_2.setOnClickListener(v122 -> tv_input_barcode.append("2"));
            number_3.setOnClickListener(v123 -> tv_input_barcode.append("3"));
            number_4.setOnClickListener(v124 -> tv_input_barcode.append("4"));
            number_5.setOnClickListener(v125 -> tv_input_barcode.append("5"));
            number_6.setOnClickListener(v126 -> tv_input_barcode.append("6"));
            number_7.setOnClickListener(v127 -> tv_input_barcode.append("7"));
            number_8.setOnClickListener(v128 -> tv_input_barcode.append("8"));
            number_9.setOnClickListener(v129 -> tv_input_barcode.append("9"));
            number_0.setOnClickListener(v130 -> tv_input_barcode.append("0"));

            btn_delete.setOnClickListener(v131 -> {
                if (!tv_input_barcode.getText().toString().equals("")) {
                    tv_input_barcode.setText(tv_input_barcode.getText().toString().substring(0, tv_input_barcode.getText().toString().length() - 1));
                    btn_search.setVisibility(View.VISIBLE);
                    btn_add.setVisibility(View.GONE);
                }
            });

            btn_clear.setOnClickListener(v132 -> {
                tv_input_barcode.setText("");
                btn_add.setVisibility(View.GONE);
                btn_search.setVisibility(View.GONE);
                tv_barcode.setVisibility(View.VISIBLE);
            });

            tv_input_barcode.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if(!charSequence.equals("")){
                        btn_search.setVisibility(View.VISIBLE);
                        tv_barcode.setVisibility(View.GONE);
                        btn_add.setVisibility(View.GONE);
                        tvNameItem.setText("");
                        tvCategoryNameItem.setText("");
                        tvPriceItem.setText("MDL 0.00");
                        tvDiscountItem.setText("MDL 0.00");
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
                AssortmentRealm assortmentEntryCategory;
                if(tv_input_barcode.getText().toString().length() == 13 || tv_input_barcode.getText().toString().length() == 8){
                    assortmentEntry = mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",tv_input_barcode.getText().toString()).or().equalTo("code",tv_input_barcode.getText().toString()).findFirst();
                }
                else{
                    assortmentEntry =  mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",tv_input_barcode.getText().toString()).findFirst();
                }
                if(assortmentEntry != null){
                    tv_input_barcode.setText("");

                    AssortmentRealm assortmentFind = mRealm.copyFromRealm(assortmentEntry);
                    assortmentEntryCategory = mRealm.where(AssortmentRealm.class).equalTo("id",assortmentFind.getParentID()).findFirst();

                    tvNameItem.setText(assortmentFind.getName());

                    if (assortmentEntryCategory != null) {
                        tvCategoryNameItem.setText(assortmentEntryCategory.getName());
                    }

                    CheckedAssortmentItemToPromo promo = checkedAssortmentItemToPromo(assortmentFind);
                    if (promo != null)
                        tvDiscountItem.setText("MDL " + String.format("%.2f", promo.getPromoPrice()).replace(",","."));
                    tvPriceItem.setText("MDL " + String.format("%.2f", assortmentFind.getPrice()).replace(",","."));

                    btn_add.setVisibility(View.VISIBLE);
                    btn_search.setVisibility(View.GONE);
                    tv_barcode.setVisibility(View.GONE);

                    btn_add.setOnClickListener(view -> {
                        addPosition.dismiss();
                        addItemsToOpenedBill(assortmentFind,1,tv_input_barcode.getText().toString(),true);
                    });
                }
                else{
                    tvNameItem.setText("");
                    tvCategoryNameItem.setText("");
                    tvPriceItem.setText("MDL 0.00");
                    tvDiscountItem.setText("MDL 0.00");

                    Toast.makeText(MainActivity.this, "Nu a fost gasit!", Toast.LENGTH_SHORT).show();
                }
            });



            btn_Cancel.setOnClickListener(v134 -> {
                addPosition.dismiss();
            });

            // Set the dialog to not focusable.
            addPosition.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            addPosition.show();
            addPosition.getWindow().setLayout(410,LinearLayout.LayoutParams.WRAP_CONTENT);

        });
        btnCheckPrice.setOnClickListener(view -> {
            LayoutInflater inflater = MainActivity.this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_check_price, null);

            final AlertDialog checkPriceDialog = new AlertDialog.Builder(MainActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
            //checkPriceDialog.setCancelable(false);
            checkPriceDialog.setView(dialogView);
            checkPriceDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            tvCheckPriceItem = dialogView.findViewById(R.id.tv_check_pricedata);
            Button btn_Cancel = dialogView.findViewById(R.id.btn_cancel_check_price);
            Button btn_add = dialogView.findViewById(R.id.btn_add);
            Button btn_search = dialogView.findViewById(R.id.btn_search);
            Button btn_clear = dialogView.findViewById(R.id.btn_check_price_clear);
            Button btn_delete = dialogView.findViewById(R.id.btn_check_price_delete);
            final TextView txtName = dialogView.findViewById(R.id.txt_name_assortment);
            final TextView txtPriceWithDiscount = dialogView.findViewById(R.id.price_with_discount);
            final TextView txtPriceWithoutDiscount = dialogView.findViewById(R.id.price_without_discount);

            Button number_1 = dialogView.findViewById(R.id.btn_check_price_1);
            Button number_2 = dialogView.findViewById(R.id.btn_check_price_2);
            Button number_3 = dialogView.findViewById(R.id.btn_check_price_3);
            Button number_4 = dialogView.findViewById(R.id.btn_check_price_4);
            Button number_5 = dialogView.findViewById(R.id.btn_check_price_5);
            Button number_6 = dialogView.findViewById(R.id.btn_check_price_6);
            Button number_7 = dialogView.findViewById(R.id.btn_check_price_7);
            Button number_8 = dialogView.findViewById(R.id.btn_check_price_8);
            Button number_9 = dialogView.findViewById(R.id.btn_check_price_9);
            Button number_0 = dialogView.findViewById(R.id.btn_check_price_0);

            number_1.setOnClickListener(v135 -> tvCheckPriceItem.append("1"));
            number_2.setOnClickListener(v136 -> tvCheckPriceItem.append("2"));
            number_3.setOnClickListener(v137 -> tvCheckPriceItem.append("3"));
            number_4.setOnClickListener(v138 -> tvCheckPriceItem.append("4"));
            number_5.setOnClickListener(v139 -> tvCheckPriceItem.append("5"));
            number_6.setOnClickListener(v140 -> tvCheckPriceItem.append("6"));
            number_7.setOnClickListener(v141 -> tvCheckPriceItem.append("7"));
            number_8.setOnClickListener(v142 -> tvCheckPriceItem.append("8"));
            number_9.setOnClickListener(v143 -> tvCheckPriceItem.append("9"));
            number_0.setOnClickListener(v144 -> tvCheckPriceItem.append("0"));

            final AssortmentRealm[] assortmentEntry = new AssortmentRealm[1];
            btn_delete.setOnClickListener(v145 -> { if (!tvCheckPriceItem.getText().toString().equals("")) tvCheckPriceItem.setText(tvCheckPriceItem.getText().toString().substring(0, tvCheckPriceItem.getText().toString().length() - 1)); });
            btn_clear.setOnClickListener(v146 -> tvCheckPriceItem.setText(""));
            btn_search.setOnClickListener(v147 -> {
                if(tvCheckPriceItem.getText().toString().length() == 13 || tvCheckPriceItem.getText().toString().length() == 8){
                    assortmentEntry[0] = mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar", tvCheckPriceItem.getText().toString()).findFirst();
                }
                else{
                    assortmentEntry[0] =  mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar", tvCheckPriceItem.getText().toString()).findFirst();
                }
                if(assortmentEntry[0] != null){
                    AssortmentRealm assortmentFind = mRealm.copyFromRealm(assortmentEntry[0]);
                    txtName.setText(assortmentFind.getName());

                    CheckedAssortmentItemToPromo promo = checkedAssortmentItemToPromo(assortmentFind);
                    if (promo != null)
                        txtPriceWithDiscount.setText(String.format("%.2f", promo.getPromoPrice()).replace(",","."));

                    txtPriceWithoutDiscount.setText(String.format("%.2f",assortmentFind.getPrice()).replace(",","."));
                }
                else{
                    txtName.setText("Nu a fost gasit!");
                    Toast.makeText(MainActivity.this, "Item not found!", Toast.LENGTH_SHORT).show();
                }
            });

            btn_Cancel.setOnClickListener(v148 -> {
                checkPriceDialog.dismiss();
            });
            btn_add.setOnClickListener(v149 -> {
                AssortmentRealm assortmentFind = mRealm.copyFromRealm(assortmentEntry[0]);
                addItemsToOpenedBill(assortmentFind,1, tvCheckPriceItem.getText().toString(),true);
                checkPriceDialog.dismiss();
            });
            checkPriceDialog.show();

            DialogInterface.OnKeyListener keylistener = new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent KEvent) {
                    int keyaction = KEvent.getAction();

                    if(keyaction == KeyEvent.ACTION_DOWN)
                    {
                        int keycode = KEvent.getKeyCode();
                        int keyunicode = KEvent.getUnicodeChar(KEvent.getMetaState() );

                        switch (keycode) {
                            case KeyEvent.KEYCODE_1 : {
                                tvCheckPriceItem.append("1");
                                tvCheckPriceItem.requestFocus();
                            }break;
                            case KeyEvent.KEYCODE_2 : {
                                tvCheckPriceItem.append("2");
                                tvCheckPriceItem.requestFocus();
                            }break;
                            case KeyEvent.KEYCODE_3 : {
                                tvCheckPriceItem.append("3");
                                tvCheckPriceItem.requestFocus();
                            }break;
                            case KeyEvent.KEYCODE_4 : {
                                tvCheckPriceItem.append("4");
                                tvCheckPriceItem.requestFocus();
                            }break;
                            case KeyEvent.KEYCODE_5 : {
                                tvCheckPriceItem.append("5");
                                tvCheckPriceItem.requestFocus();
                            }break;
                            case KeyEvent.KEYCODE_6 : {
                                tvCheckPriceItem.append("6");
                                tvCheckPriceItem.requestFocus();
                            }break;
                            case KeyEvent.KEYCODE_7 : {
                                tvCheckPriceItem.append("7");
                                tvCheckPriceItem.requestFocus();
                            }break;
                            case KeyEvent.KEYCODE_8 : {
                                tvCheckPriceItem.append("8");
                                tvCheckPriceItem.requestFocus();
                            }break;
                            case KeyEvent.KEYCODE_9 : {
                                tvCheckPriceItem.append("9");
                                tvCheckPriceItem.requestFocus();
                            }break;
                            case KeyEvent.KEYCODE_0 : {
                                tvCheckPriceItem.append("0");
                                tvCheckPriceItem.requestFocus();
                            }break;
                            case KeyEvent.KEYCODE_ENTER : {
                                if(tvCheckPriceItem.getText().toString().length() == 13 || tvCheckPriceItem.getText().toString().length() == 8){
                                    assortmentEntry[0] = mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar", tvCheckPriceItem.getText().toString()).findFirst();
                                }
                                else{
                                    assortmentEntry[0] =  mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar", tvCheckPriceItem.getText().toString()).findFirst();
                                }
                                if(assortmentEntry[0] != null){

                                    AssortmentRealm assortmentFind = mRealm.copyFromRealm(assortmentEntry[0]);
                                    txtName.setText(assortmentFind.getName());
                                    txtPriceWithDiscount.setText(String.format("%.2f",assortmentFind.getPrice()).replace(",","."));
                                    double priceWithDisc = -1;

                                    if(!assortmentFind.getPromotions().isEmpty()){
                                        Promotion promo = assortmentFind.getPromotions().first();

                                        long startDate = replaceDate(promo.getStartDate());
                                        long endDate = replaceDate(promo.getEndDate());
                                        Date curentDate = new Date();
                                        long currDate = curentDate.getTime();

                                        long timeBegin = 0;
                                        long timeEnd = 0;

                                        if(promo.getTimeBegin() != null)    timeBegin = replaceDate(promo.getTimeBegin());
                                        if(promo.getTimeEnd() != null)    timeEnd = replaceDate(promo.getTimeEnd());

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
                                                    priceWithDisc = promo.getPrice();
                                                }
                                                else{
                                                    priceWithDisc = assortmentFind.getPrice();
                                                }
                                            }
                                            else{
                                                priceWithDisc = promo.getPrice();
                                            }
                                        }
                                        else{
                                            priceWithDisc = assortmentFind.getPrice();
                                        }
                                    }
                                    else{
                                        priceWithDisc = assortmentFind.getPrice();
                                    }
                                    txtPriceWithoutDiscount.setText(String.format("%.2f",priceWithDisc).replace(",","."));
                                    tvCheckPriceItem.setText("");
                                }
                                else{
                                    txtName.setText("Item not found!");
                                    Toast.makeText(MainActivity.this, "Item not found!", Toast.LENGTH_SHORT).show();
                                }
                            }break;
                            default:break;
                        }
                    }
                    return false;
                }
            };
            checkPriceDialog.setOnKeyListener(keylistener );
        });
        btnAddClient.setOnClickListener(view -> {
            View dialogView = inflater.inflate(R.layout.dialog_payment_bill_version0, null);

            final AlertDialog checkPriceDialog = new AlertDialog.Builder(MainActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
            //checkPriceDialog.setCancelable(false);
            checkPriceDialog.setView(dialogView);
            checkPriceDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            checkPriceDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            checkPriceDialog.show();
            checkPriceDialog.getWindow().setLayout(470,LinearLayout.LayoutParams.WRAP_CONTENT);
        });
        btnPay.setOnClickListener(v->{
            if(shiftOpenButtonPay){
                new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                        .setTitle("Attention!")
                        .setMessage("Do you want open shift?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            long opened_new_shift = new Date().getTime();
                            long need_close = opened_new_shift + 28800000;

                            Shift shiftEntry = new Shift();
                            shiftEntry.setName("SHF " + simpleDateFormatMD.format(opened_new_shift));
                            shiftEntry.setWorkPlaceId(getSharedPreferences(SharedPrefWorkPlaceSettings, MODE_PRIVATE).getString("WorkPlaceID", "null"));
                            shiftEntry.setAuthor(BaseApplication.getInstance().getUserId());
                            shiftEntry.setStartDate(new Date().getTime());
                            shiftEntry.setClosed(false);
                            shiftEntry.setNeedClose(need_close);
                            shiftEntry.setId(UUID.randomUUID().toString());

                            mRealm.beginTransaction();
                            mRealm.insert(shiftEntry);
                            mRealm.commitTransaction();

                            BaseApplication.getInstance().setShift(shiftEntry);
                            startTimer(need_close - new Date().getTime());
                            functionOpenedShift();
                        })
                        .setNegativeButton("No",((dialogInterface, i) -> {
                            dialogInterface.dismiss();
                        }))

                .show();
            }
            else if(shiftClosedButtonPay){
                new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                        .setTitle("Attention!")
                        .setMessage("Do you want close this shift?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            closeShift();
                        })
                        .setNegativeButton("No",((dialogInterface, i) -> {
                            dialogInterface.dismiss();
                        }))
                        .show();
            }
            else{
                paymentBill(Double.valueOf(btnPay.getText().toString().replace("MDL ","")));
            }

        });

//        btn_add_position.setOnClickListener(v -> {
//            if_addPositionActive = true;

//            DialogInterface.OnKeyListener keylistener = (dialog, keyCode, KEvent) -> {
//                int keyaction = KEvent.getAction();
//
//                if(keyaction == KeyEvent.ACTION_DOWN)
//                {
//                    int keycode = KEvent.getKeyCode();
//                    int keyunicode = KEvent.getUnicodeChar(KEvent.getMetaState() );
//                    char character = (char) keyunicode;
//
//                    switch (keycode) {
//                        case KeyEvent.KEYCODE_1 : {
//                            tvCheckPriceItem.append("1");
//                            tvCheckPriceItem.requestFocus();
//                        }break;
//                        case KeyEvent.KEYCODE_2 : {
//                            tvCheckPriceItem.append("2");
//                            tvCheckPriceItem.requestFocus();
//                        }break;
//                        case KeyEvent.KEYCODE_3 : {
//                            tvCheckPriceItem.append("3");
//                            tvCheckPriceItem.requestFocus();
//                        }break;
//                        case KeyEvent.KEYCODE_4 : {
//                            tvCheckPriceItem.append("4");
//                            tvCheckPriceItem.requestFocus();
//                        }break;
//                        case KeyEvent.KEYCODE_5 : {
//                            tvCheckPriceItem.append("5");
//                            tvCheckPriceItem.requestFocus();
//                        }break;
//                        case KeyEvent.KEYCODE_6 : {
//                            tvCheckPriceItem.append("6");
//                            tvCheckPriceItem.requestFocus();
//                        }break;
//                        case KeyEvent.KEYCODE_7 : {
//                            tvCheckPriceItem.append("7");
//                            tvCheckPriceItem.requestFocus();
//                        }break;
//                        case KeyEvent.KEYCODE_8 : {
//                            tvCheckPriceItem.append("8");
//                            tvCheckPriceItem.requestFocus();
//                        }break;
//                        case KeyEvent.KEYCODE_9 : {
//                            tvCheckPriceItem.append("9");
//                            tvCheckPriceItem.requestFocus();
//                        }break;
//                        case KeyEvent.KEYCODE_0 : {
//                            tvCheckPriceItem.append("0");
//                            tvCheckPriceItem.requestFocus();
//                        }break;
//                        case KeyEvent.KEYCODE_ENTER : {
//
//                            AssortmentRealm assortmentEntry;
//                            if(tvCheckPriceItem.getText().toString().length() == 13 || tvCheckPriceItem.getText().toString().length() == 8){
//                                assortmentEntry = mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",tvCheckPriceItem.getText().toString()).findFirst();
//                            }
//                            else{
//                                assortmentEntry =  mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",tvCheckPriceItem.getText().toString()).findFirst();
//                            }
//                            if(assortmentEntry != null){
//                                AssortmentRealm assortmentFind = mRealm.copyFromRealm(assortmentEntry);
//                                addItemsToOpenedBill(assortmentFind,1,tvCheckPriceItem.getText().toString(),true);
//                            }
//                            else{
//                                Toast.makeText(MainActivity.this, "Nu a fost gasit!", Toast.LENGTH_SHORT).show();
//                            }
//                            if_addPositionActive = false;
//                            addPosition.dismiss();
//                        }break;
//                        default:break;
//                    }
//                }
//                return false;
//            };
//            addPosition.setOnKeyListener(keylistener );
//        });

//
//        checkDiscount.setOnClickListener(v->{
//            byte[] comman = { (byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x0, (byte) 0x0 };
//
//            TransmitParams params = new TransmitParams();
//            params.slotNum = 0;
//            params.controlCode = 3500;
//            params.command = comman;
//
//            new TransmitTask().execute(params);
//        });
//        initQuickButtons();
//
//        // NFC settings
//        readFromIntent(getIntent());
//
//        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
//        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
//        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
//        writeTagFilters = new IntentFilter[] { tagDetected };
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
                    .setTitle("Attention deleting bill!")
                    .setMessage("Do you want delete it bill?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        mRealm.executeTransaction(realm -> {
                           Bill bils =  realm.where(Bill.class).equalTo("id", bill.getId()).findFirst();
                           if(bils != null){
                               bils.setCloseDate(new Date().getTime());
                               bils.setClosedBy(BaseApplication.getInstance().getUserId());
                               bils.setState(BaseEnum.BILL_DELETED);
                           }
                        });
                        drawer.closeDrawer(GravityCompat.END);

                        Snackbar.make(view, "Bill " + bill.getShiftReceiptNumSoftware() + " is deleted!", Snackbar.LENGTH_LONG)
                                .setAction("UNDO", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        mRealm.executeTransaction(realm -> {
                                            Bill bils =  realm.where(Bill.class).equalTo("id", bill.getId()).findFirst();
                                            if(bils != null){
                                                bils.setCloseDate(0);
                                                bils.setClosedBy("");
                                                bils.setState(BaseEnum.BILL_OPEN);
                                            }
                                        });

                                    }
                                }).show();


                    })
                    .setNegativeButton("No",((dialogInterface, i) -> {
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
                Snackbar.make(view, "Shift has expired!", Snackbar.LENGTH_SHORT).show();
            }
            else{
                openedBillId = bill.getId();
                drawer.closeDrawer(GravityCompat.END);
                initRecyclerView();
            }

        });
        btnPayBill.setOnClickListener(view ->{
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
            CustomBillStringRealmListAdapter adapter = new CustomBillStringRealmListAdapter(results[0]);
            listContent.setAdapter(adapter);
        }
        totalBill.setText(String.format("%.2f",bill.getSumWithDiscount()).replace(",","."));
        clientBill.setText(bill.getDiscountCardNumber());
        discount.setText(String.format("%.2f", bill.getSum() - bill.getSumWithDiscount()).replace(",","."));
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();

        fiscalReceipt = new cmdReceipt.FiscalReceipt();

        if (!( adapterRightMenu== null)) {
            adapterRightMenu.notifyDataSetChanged();
        }
        //check fiscal mode work is selected in settings
//        if(getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getInt(ModeFiscalWork, BaseEnum.NONE_SELECTED_FISCAL_MODE) == BaseEnum.FISCAL_SERVICE){
////            initFiscalService();
//        }

        //check nfc adapter from device
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
        }

//        if(myFiscalDevice == null || !myFiscalDevice.isConnectedDeviceV2()){
//            fiscal_printer.setImageDrawable(getResources().getDrawable(R.drawable.fiscal_off));
//        }

        Shift shift = BaseApplication.getInstance().getShift();
        if(shift != null){
            shiftOpenButtonPay = false;

            boolean shiftOpened = shift.isClosed();
            long shiftNeedClose = shift.getNeedClose();
            long currentTime = new Date().getTime();

            if(!shiftOpened && currentTime < shiftNeedClose){
                //if shift is opened and time to close is smaller current time, when shift is valid
                //start timer as long as it remained
                startTimer(shiftNeedClose - currentTime);

                //TODO other function set when shift is valid

            }
            else if(!shiftOpened && currentTime > shiftNeedClose && shiftNeedClose != 0){
                //if shift is open but time when need to close is greater than that current time, when shift is not valid
                //set timer 00.00.00
                cancelTimer();

                //TODO set other function when shift is not valid

                btnPay.setText("Close shift");
                shiftClosedButtonPay = true;

                new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                        .setTitle("Attention!")
                        .setMessage("Shift has expired, want to close?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            closeShift();
                        })
                        .setNegativeButton("No",((dialogInterface, i) -> {
                            dialogInterface.dismiss();
                        }))
                        .show();
            }

        }
        else{
            //open shift is not find, when shift is not opened and set timer 00.00.00
            cancelTimer();

            //TODO set other function when shift is not opened
            btnPay.setText("Open shift");
            shiftOpenButtonPay = true;
        }


    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

//        SearchManager searchManager = (SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);
//
//        SearchView searchView = null;
//        if (searchItem != null) {
//            searchView = (SearchView) searchItem.getActionView();
//        }
//        if (searchView != null) {
//            searchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
//        }
        return super.onCreateOptionsMenu(menu);
    }

    private static void paymentBill(double summBill){
//        sumBillToPay = Double.valueOf(btnPay.getText().toString().replace("MDL ",""));
        sumBillToPay = summBill;
        View dialogView = inflater.inflate(R.layout.dialog_payment_bill_version0, null);

        paymentDialog = new AlertDialog.Builder(context,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
        paymentDialog.setCancelable(false);
        paymentDialog.setView(dialogView);
        paymentDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        tvInputSumBillForPayment = dialogView.findViewById(R.id.tv_pay_input_data_sum);
        TextView tvTotalBill = dialogView.findViewById(R.id.txt_total_payment);
        TextView tvToPay = dialogView.findViewById(R.id.txt_topay_payment);
        TextView tvChange = dialogView.findViewById(R.id.txt_rest_payment);
        ImageButton btn_Cancel = dialogView.findViewById(R.id.btnClose_payment);
        MaterialButton clear = dialogView.findViewById(R.id.btn_pay_ce);
        ImageButton delete = dialogView.findViewById(R.id.btn_pay_delete_number);
        MaterialButton btnCreditCard = dialogView.findViewById(R.id.btn_card_payment);
        MaterialButton btnCashPay = dialogView.findViewById(R.id.btn_cash_payment);
        MaterialButton btnOtherPay = dialogView.findViewById(R.id.btn_other_payment);

        //caut daca contul a fost achitat partial, si adaug text mai jos cu ce tip de plata si ce suma
        //plus la asta daca este vreo achitare deja facuta, verific daca este necesar de imprimat bonul fiscal si daca da, filtrez tipruile de plata dupa criteriu - printFiscalReceip
        RealmResults<BillPaymentType> billPaymentTypes = mRealm.where(BillPaymentType.class).equalTo("billID",openedBillId).findAll();
        boolean printCheck = false;    // daca trebuie bon fiscal sau nu
        boolean isPaymentOnlyOne = false;   // daca exista vreo achitare

        if(!billPaymentTypes.isEmpty()){
            for (int i = 0; i < billPaymentTypes.size(); i++){
                BillPaymentType paymentType = billPaymentTypes.get(i);
                if (paymentType != null) {
                    billPaymentedSum += paymentType.getSum();
                }
            }
        }

        //caut tipurile de plata care sunt in baza si le adaug butoane
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
                }
                else if(paymentTypesResult.get(o).getPredefinedIndex() == BaseEnum.Pay_CreditCard){
                    btnCreditCard.setTag(paymentType);
                    btnCreditCard.setOnClickListener(clickListenerDynamicPayButton);
                }
            }
        }

        tvInputSumBillForPayment.setText(String.format("%.2f",sumBillToPay - billPaymentedSum).replace(",","."));
        tvTotalBill.setText(btnPay.getText().toString().replace("MDL ",""));
        tvToPay.setText(btnPay.getText().toString().replace("MDL ",""));

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

        number_1.setOnClickListener(v1 -> tvInputSumBillForPayment.append("1"));
        number_2.setOnClickListener(v12 -> tvInputSumBillForPayment.append("2"));
        number_3.setOnClickListener(v13 -> tvInputSumBillForPayment.append("3"));
        number_4.setOnClickListener(v14 -> tvInputSumBillForPayment.append("4"));
        number_5.setOnClickListener(v15 -> tvInputSumBillForPayment.append("5"));
        number_6.setOnClickListener(v16 -> tvInputSumBillForPayment.append("6"));
        number_7.setOnClickListener(v17 -> tvInputSumBillForPayment.append("7"));
        number_8.setOnClickListener(v18 -> tvInputSumBillForPayment.append("8"));
        number_9.setOnClickListener(v19 -> tvInputSumBillForPayment.append("9"));
        number_0.setOnClickListener(v110 -> tvInputSumBillForPayment.append("0"));
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
                    tvToPay.setText(String.format("%.2f", sumBillToPay - billPaymentedSum).replace(",","."));
                    tvChange.setText("0.00");
                }
                else{
                    double incasat = 0.0;
                    try{
                        incasat = Double.valueOf(tvInputSumBillForPayment.getText().toString());
                    }catch (Exception e){
                        incasat = Double.valueOf(tvInputSumBillForPayment.getText().toString().replace(",","."));
                    }

                    tvToPay.setText(String.format("%.2f", incasat + billPaymentedSum).replace(",","."));
                    double rest = 0.0;

                    if( (incasat + billPaymentedSum) <= sumBillToPay){
                        tvChange.setText("0.00");
                    }else{
                        rest = (incasat + billPaymentedSum) - sumBillToPay;
                        tvChange.setText( String.format("%.2f", rest).replace(",","."));
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
        btn_Cancel.setOnClickListener(v120 -> { paymentDialog.dismiss(); billPaymentedSum = 0; });

        paymentDialog.show();
        paymentDialog.getWindow().setLayout(470,LinearLayout.LayoutParams.WRAP_CONTENT);
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
                inputSum = Double.valueOf(tvInputSumBillForPayment.getText().toString());
            } catch (Exception e) {
                inputSum = Double.valueOf(tvInputSumBillForPayment.getText().toString().replace(",", "."));
            }

            if ((billPaymentedSum + inputSum) >= sumBillToPay) {
                int modeFiscalWork = context.getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getInt("ModeFiscalWork",BaseEnum.NONE_SELECTED_FISCAL_MODE);

                if (printFiscalCheck) {
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

                                openedBillId = null;
                            }
                        }
                        else{
                            Toast.makeText(context, "Aparatul fiscal nu este conectat!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(modeFiscalWork == BaseEnum.FISCAL_SERVICE){
                        BaseApplication.getInstance().printReceiptFiscalService(billStrings, paymentType, inputSum, billPaymentTypes);

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
                        initRecyclerView();

                        billPaymentedSum = 0;
                        paymentDialog.dismiss();

                        openedBillId = null;
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
                    initRecyclerView();

                    billPaymentedSum = 0;
                    paymentDialog.dismiss();
                    openedBillId = null;
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
                //TODO after payment partial
                billPaymentedSum = 0;
            }
        }
    };

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
            }
        });
    }

    public static void editLineCount(BillString billString, double sumWithDiscount, double sum, double quantity){
        mRealm.executeTransaction(realm -> {
            billString.setQuantity(quantity);
            billString.setSum(sum);
            billString.setSumWithDiscount(sumWithDiscount);

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
            bill.setAuthor( BaseApplication.getInstance().getUser().getId());
            bill.setSumWithDiscount(0.0);
            bill.setSum(0.0);
            bill.setState(0);
            bill.setShiftId(shift.getId());
            bill.setSinchronized(false);
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
                onSucces[0] = true;
            });
        }
        else{
            Toast.makeText(MainActivity.getContext(), "Tura nu este activa", Toast.LENGTH_SHORT).show();
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
            lastBillString = adapter.getItem(countArray - 1);

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

                billString.setBarcode(barcode);
                billString.setVat(assortmentEntry.getVat());
                billString.setCreateDate(new Date().getTime());
                billString.setDeleted(false);
                billString.setPriceWithDiscount(priceWithDisc);

                billString.setSum(assortmentEntry.getPrice() * count);
                billString.setSumWithDiscount(priceWithDisc * count);

                double finalPriceWithDisc = priceWithDisc;
                mRealm.executeTransaction(realm -> {
//                    realm.insert(billString);

                Bill billEntryRealmResults = realm.where(Bill.class).equalTo("id", openedBillId).findFirst();
                if (billEntryRealmResults != null) {
                    billEntryRealmResults.setSum(billEntryRealmResults.getSum() + (assortmentEntry.getPrice() * count));
                    billEntryRealmResults.setSumWithDiscount(billEntryRealmResults.getSumWithDiscount() + (finalPriceWithDisc * count));
                    billEntryRealmResults.getBillStrings().add(billString);
                }
                    createBillString[0] = true;
                });
            }

            if(updateInterface)
                initRecyclerView();
        }
        return createBillString[0];
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Log.d("myLogs", "KeyEvent DOWN" + event);
        switch (event.getAction()){
            case KeyEvent.ACTION_DOWN :{
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_1 : {
                        tvScanBarcode.append("1");
                        tvScanBarcode.requestFocus();
                        tvScanBarcode.requestFocusFromTouch();
                    }break;
                    case KeyEvent.KEYCODE_2 : {
                        tvScanBarcode.append("2");
                        tvScanBarcode.requestFocus();
                        tvScanBarcode.requestFocusFromTouch();
                    }break;
                    case KeyEvent.KEYCODE_3 : {
                        tvScanBarcode.append("3");
                        tvScanBarcode.requestFocus();
                        tvScanBarcode.requestFocusFromTouch();
                    }break;
                    case KeyEvent.KEYCODE_4 : {
                        tvScanBarcode.append("4");
                        tvScanBarcode.requestFocus();
                        tvScanBarcode.requestFocusFromTouch();
                    }break;
                    case KeyEvent.KEYCODE_5 : {
                        tvScanBarcode.append("5");
                        tvScanBarcode.requestFocus();
                        tvScanBarcode.requestFocusFromTouch();
                    }break;
                    case KeyEvent.KEYCODE_6 : {
                        tvScanBarcode.append("6");
                        tvScanBarcode.requestFocus();
                        tvScanBarcode.requestFocusFromTouch();
                    }break;
                    case KeyEvent.KEYCODE_7 : {
                        tvScanBarcode.append("7");
                        tvScanBarcode.requestFocus();
                        tvScanBarcode.requestFocusFromTouch();
                    }break;
                    case KeyEvent.KEYCODE_8 : {
                        tvScanBarcode.append("8");
                        tvScanBarcode.requestFocus();
                        tvScanBarcode.requestFocusFromTouch();
                    }break;
                    case KeyEvent.KEYCODE_9 : {
                        tvScanBarcode.append("9");
                        tvScanBarcode.requestFocus();
                        tvScanBarcode.requestFocusFromTouch();
                    }break;
                    case KeyEvent.KEYCODE_0 : {
                        tvScanBarcode.append("0");
                        tvScanBarcode.requestFocus();
                        tvScanBarcode.requestFocusFromTouch();
                    }break;
                    default:break;
                }
            }break;
            default:break;
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

    private static void initRecyclerView(){
        final RealmResults<BillString>[] results = new RealmResults[]{null};
        final Bill[] bill = new Bill[1];
        Shift shift = BaseApplication.getInstance().getShift();
        if(shift != null){
            mRealm.executeTransaction(realm -> {
                bill[0] = mRealm.where(Bill.class).equalTo("shiftId", shift.getId()).and().equalTo("state", BaseEnum.BILL_OPEN).and().equalTo("id",openedBillId).findFirst();
                if (bill[0] != null) {
                    openedBillId = bill[0].getId();
                    results[0] = mRealm.where(BillString.class).equalTo("billID", openedBillId).and().equalTo("isDeleted", false).sort("createDate").findAll();
                }
            });
            adapter = new CustomRCBillStringRealmAdapter(results[0],false);

            recyclerView.setAdapter(adapter);
            if (adapter.getItemCount() > 0)
                recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);

            if(bill[0] != null){
                btnPay.setText("MDL " + String.format("%.2f", bill[0].getSumWithDiscount()).replace(",","."));
                tvDiscountBill.setText(String.format("%.2f",bill[0].getSum() - bill[0].getSumWithDiscount()).replace(",","."));
                tvSubTotalBill.setText(String.format("%.2f", bill[0].getSum()).replace(",","."));
            }
            else{
                btnPay.setText("MDL 0.00");
                tvDiscountBill.setText("0.00");
                tvSubTotalBill.setText("0.00");
            }

        }
    }

    private void functionOpenedShift(){
        postToastMessage("Shift is opened!");
        shiftOpenButtonPay = false;
        FragmentBills.showBillList();
        btnPay.setText("MDL 0.00");
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
                    .setTitle("Attention!")
                    .setMessage("You cannot close a shift while there are open bills!\nYou have left " + billEntryResult.size() + " open bills.")
                    .setCancelable(false)
                    .setPositiveButton("OKAY", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
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
                shifts.setLong("endDate", close);
                shifts.setBoolean("closed", true);
                shifts.setBoolean("isSended",false);
            });

            shiftClosedButtonPay = false;
            shiftOpenButtonPay = true;
            btnPay.setText("Open shift");
        }
    }

    //start timer function
    void startTimer(long time) {
        countDownShiftTimer = new CountDownTimer(time, 1000) {
            public void onTick(long millisUntilFinished) {
                long second = (millisUntilFinished / 1000) % 60;
                long minute = (millisUntilFinished / (1000 * 60)) % 60;
                long hour = (millisUntilFinished / (1000 * 60 * 60)) % 24;

                String time = String.format("%02d:%02d:%02d", hour, minute, second);
//                tv_schedule_shift.setText(time);

                if(millisUntilFinished == 600000){
                    new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                            .setTitle("Attention!")
                            .setMessage("10 minutes are left before the shift closes.")
                            .setCancelable(false)
                            .setPositiveButton("OKAY", (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            })
                            .show();
                }
                if(millisUntilFinished == 0){
                    //TODO close shift probabil
                }

            }
            public void onFinish() {
            }
        };
        countDownShiftTimer.start();
    }
    //cancel timer
    void cancelTimer() {
        if(countDownShiftTimer != null){
            countDownShiftTimer.onFinish();
            countDownShiftTimer.cancel();
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
                        postToastMessage("Connection error: " + e.getMessage());
                        return;
                    }

                    try {
                        PrinterManager.instance.init(item);
                    } catch (Exception e) {
                        try {
                            item.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        postToastMessage("Error: " + e.getMessage());
                        return;
                    }
                } finally {
                    ((BaseApplication)getApplication()).setMyFiscalDevice(PrinterManager.instance.getFiscalDevice());
                    datecsFiscalDevice = PrinterManager.instance.getFiscalDevice();

                    if(datecsFiscalDevice != null && datecsFiscalDevice.isConnectedDeviceV2()){
                        runOnUiThread(() -> {
//                                fiscal_printer.setImageDrawable(getResources().getDrawable(R.drawable.fiscal_on));
                        });
                    }


                }
            }
        });
        thread.start();
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

    private void postToastMessage (final String message) {
        this.runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
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

}