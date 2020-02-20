package edi.md.androidcash;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.UUID;

import edi.md.androidcash.Fragments.FragmentBills;
import edi.md.androidcash.NetworkUtils.Promotion;
import edi.md.androidcash.RealmHelper.AssortmentRealm;
import edi.md.androidcash.RealmHelper.Bill;
import edi.md.androidcash.RealmHelper.BillString;
import edi.md.androidcash.RealmHelper.Shift;
import edi.md.androidcash.Utils.BaseEnum;
import edi.md.androidcash.adapters.CustomBillStringRealmListAdapter;
import edi.md.androidcash.adapters.CustomRCBillStringRealmAdapter;
import edi.md.androidcash.adapters.ViewPageAdapterRightMenu;
import edi.md.androidcash.connectors.AbstractConnector;
import edi.md.androidcash.connectors.UsbDeviceConnector;
import io.realm.Realm;
import io.realm.RealmResults;

import static edi.md.androidcash.BaseApplication.ModeFiscalWork;
import static edi.md.androidcash.BaseApplication.SharedPrefSettings;
import static edi.md.androidcash.BaseApplication.SharedPrefWorkPlaceSettings;
import static edi.md.androidcash.BaseApplication.deviceId;


public class MainActivity extends AppCompatActivity {
    private static Context context;
    private TextView tvDiscountBill;
    private static Button btnPay, btnAddCustomer, btnCheckPrice;
    MaterialButton btnNewBill, btnAddItem;
    private static RecyclerView recyclerView;
    private ImageButton btnSettings;
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

    private Button myDynamicPayTypeButton;
    private static String openedBillId;
    private String searchBarcode = "";
    private boolean shiftOpenButtonPay = false;
    private boolean shiftClosedButtonPay = false;

    private static DrawerLayout drawer;
    private static NavigationView navigationView;

    private static ConstraintLayout drawerConstraint;
    private static Display display;

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

//                            // Open reader
//                            new OpenTask().execute(device);
//
//                            HashMap<String, UsbDevice> deviceList = mUSBManager.getDeviceList();
//
//                            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
//                            while (deviceIterator.hasNext()) {
//                                UsbDevice devices = deviceIterator.next();
//
//                                if ((devices.getVendorId() == BaseEnum.DATECS_USB_VID) || (devices.getVendorId() == BaseEnum.FTDI_USB_VID) && (devices.getManufacturerName().equals("Datecs"))) {
//                                    if (!mUSBManager.hasPermission(devices)) {
//                                        mUSBManager.requestPermission(devices, mPermissionIntent);
//                                    }
//                                }
//                            }

                        }
                    }
                }

            }
            else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (device != null && device.equals(mReader.getDevice())) {
                        // Close reader
//                        new CloseTask().execute();
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
        btnSettings = toolbar.findViewById(R.id.img_button_settings);
        btnAddItem = findViewById(R.id.btn_add_items);
        btnNewBill = findViewById(R.id.btn_new_bill);
        viewPager = findViewById(R.id.vp_right_container);
        tabLayout = findViewById(R.id.tab_items_right_menu);

        drawer = findViewById(R.id.drawer_layout);
        drawerConstraint = findViewById(R.id.nav_view);
        display = getWindowManager().getDefaultDisplay();

        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        initRecyclerView();
        findOpenedShift();

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

        ViewPageAdapterRightMenu adapterRightMenu = new ViewPageAdapterRightMenu(this, getSupportFragmentManager());
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

        btnSettings.setOnClickListener(v -> startActivityForResult(new Intent(".Settings"),111));

        btnNewBill.setOnClickListener(v->{
            if(openedBillId != null){
                openedBillId = null;
                initRecyclerView();
            }
        });
        btnAddItem.setOnClickListener(v->{
            LayoutInflater inflater = MainActivity.this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_add_position, null);

            final AlertDialog addPosition = new AlertDialog.Builder(MainActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
            addPosition.setCancelable(false);
            addPosition.setView(dialogView);
            addPosition.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            TextView tv_input_barcode = dialogView.findViewById(R.id.et_input_data);
            MaterialButton btn_Cancel = dialogView.findViewById(R.id.btn_cancel);
            MaterialButton btn_ok = dialogView.findViewById(R.id.btn_ok);
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


            btn_delete.setOnClickListener(v131 -> { if (!tv_input_barcode.getText().toString().equals("")) tv_input_barcode.setText(tv_input_barcode.getText().toString().substring(0, tv_input_barcode.getText().toString().length() - 1)); });
            btn_clear.setOnClickListener(v132 -> tv_input_barcode.setText(""));
            btn_ok.setOnClickListener(v133 -> {
                AssortmentRealm assortmentEntry;
                if(tv_input_barcode.getText().toString().length() == 13 || tv_input_barcode.getText().toString().length() == 8){
                    assortmentEntry = mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",tv_input_barcode.getText().toString()).findFirst();
                }
                else{
                    assortmentEntry =  mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",tv_input_barcode.getText().toString()).findFirst();
                }
                if(assortmentEntry != null){
                    AssortmentRealm assortmentFind = mRealm.copyFromRealm(assortmentEntry);
                    addItemsToOpenedBill(assortmentFind,1,tv_input_barcode.getText().toString(),true);
                }
                else{
                    Toast.makeText(MainActivity.this, "Nu a fost gasit!", Toast.LENGTH_SHORT).show();
                }
                addPosition.dismiss();
            });

            btn_Cancel.setOnClickListener(v134 -> {
                addPosition.dismiss();
            });
            addPosition.show();
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
            if(shiftClosedButtonPay){
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

        });

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//
//        TextView name_magazin = toolbar.findViewById(R.id.toolbar_title);
//        inpput = toolbar.findViewById(R.id.toolbar_barcode_input);
//        btn_settings = toolbar.findViewById(R.id.img_button_settings);
//        tv_user_name = toolbar.findViewById(R.id.tv_user_name);
//        tv_user_name.setText(((BaseApplication)getApplication()).getUser().getFullName());
//        context = this;
//
//        name_magazin.setText(getSharedPreferences(SharedPrefWorkPlaceSettings,MODE_PRIVATE).getString("WorkPlaceName",""));
//
//        setSupportActionBar(toolbar);
//
//        initUIElements();
//
//        String wokPlaceID= getSharedPreferences(SharedPrefWorkPlaceSettings,MODE_PRIVATE).getString("WorkPlaceID",null);
//
//        if( wokPlaceID == null){
////            LayoutInflater inflater1 = this.getLayoutInflater();
////            final View dialogView = inflater1.inflate(R.layout.dialog_not_workplaces, null);
////
////            final android.app.AlertDialog exitApp = new android.app.AlertDialog.Builder(this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
////            exitApp.setCancelable(false);
////            exitApp.setView(dialogView);
////
////            Button btn_ok = dialogView.findViewById(R.id.btn_yes_select_workplace);
////
////            btn_ok.setOnClickListener(new View.OnClickListener() {
////                @Override
////                public void onClick(View view) {
////                    exitApp.dismiss();
////                    startActivityForResult(new Intent(".Settings"), REQUEST_ACTIVITY_LIST_SETTING);
////                }
////            });
////
////            exitApp.show();
//        }
//
//
//        if( ((BaseApplication)getApplication()).getUser() == null){
//            btn_payment_bill.setEnabled(false);
//            btn_add_position.setEnabled(false);
//            btn_check_price.setEnabled(false);
//            btn_new_bill.setEnabled(false);
//            btn_list_bill.setEnabled(false);
//            btn_asl_list.setEnabled(false);
//
//            frm_add_position.setEnabled(false);
//            frm_new_bill.setEnabled(false);
//            frm_check_price.setEnabled(false);
//            frm_apply_disc.setEnabled(false);
//            frm_check_disc.setEnabled(false);
//            frm_delete_disc.setEnabled(false);
//            frm_listBills.setEnabled(false);
//            frm_ListAssortment.setEnabled(false);
//        }
//
//        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
//
//        if (nfcAdapter == null) {
//            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
//        }
//
//        inpput.requestFocus();
//        inpput.requestFocusFromTouch();
//
//        sdfChisinau = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
//        tzInChisinau = TimeZone.getTimeZone("Europe/Chisinau");
//        sdfChisinau.setTimeZone(tzInChisinau);
//
//        mRealm = Realm.getDefaultInstance();
//
//        fiscalReceipt = new cmdReceipt.FiscalReceipt();
//        myFiscalDevice = ((BaseApplication)getApplication()).getMyFiscalDevice();
//
//        // Initialize reader ACR
//        mReader = new Reader(mManager);
//
//        mReader.setOnStateChangeListener((slotNum, prevState, currState) -> {
//
//            if (prevState < Reader.CARD_UNKNOWN || prevState > Reader.CARD_SPECIFIC) {
//                prevState = Reader.CARD_UNKNOWN;
//            }
//            if (currState < Reader.CARD_UNKNOWN || currState > Reader.CARD_SPECIFIC) {
//                currState = Reader.CARD_UNKNOWN;
//            }
//            if (currState == Reader.CARD_PRESENT) {
//
//
//            }
//        });
//
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);
//
//        if(myFiscalDevice !=null ){
//            boolean isConect = myFiscalDevice.isConnectedDeviceV2();
//
//            if(isConect){
//                fiscal_printer.setImageDrawable(getResources().getDrawable(R.drawable.fiscal_on));
//            }
//            else{
//                fiscal_printer.setImageDrawable(getResources().getDrawable(R.drawable.fiscal_off));
//            }
//        }
//        else{
//            fiscal_printer.setImageDrawable(getResources().getDrawable(R.drawable.fiscal_off));
//        }
//
//        initRecyclerView();
//
//        LW_NewBill.setOnItemClickListener((parent, view, position, id) -> {
//            item_newbill_clicked = true;
//            billStringEntry = adapterString.getItem(position);
//        });
//        btn_settings.setOnClickListener(v -> startActivityForResult(new Intent(".Settings"), REQUEST_ACTIVITY_LIST_SETTING));
//
//        //listener for TextView on scan barcode assortment
//        inpput.setOnEditorActionListener((v, actionId, event) -> {
//             if(event.getKeyCode()==KeyEvent.KEYCODE_ENTER){
//                 scaned_item = true;
//                 AssortmentRealm realmResult = mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",inpput.getText().toString()).findFirst();
//                 if(realmResult != null){
//                     AssortmentRealm assortmentFind = mRealm.copyFromRealm(realmResult);
//                     addItemsToOpenedBill(assortmentFind,1,inpput.getText().toString(),true);
//                     inpput.setText("");
//                     initRecyclerView();
//                 }
//                 else{
//                     Toast.makeText(MainActivity.this, "Nu a fost gasit!", Toast.LENGTH_SHORT).show();
//                     inpput.setText("");
//                 }
//            }
//            return false;
//        });
//
//        btn_up_count.setOnClickListener(v -> {
//            if(item_newbill_clicked){
//                double quantity = billStringEntry.getQuantity();
//                quantity += 1;
//                double sum = billStringEntry.getPrice() * quantity;
//                double sumWithDisc = billStringEntry.getPriceWithDiscount() * quantity;
//                editLineCount(sumWithDisc,sum,quantity);
//            }
//            else{
//                Toast.makeText(MainActivity.this, "Alegeti pozitia!", Toast.LENGTH_SHORT).show();
//            }
//        });
//        btn_down_count.setOnClickListener(v -> {
//            if(item_newbill_clicked){
//                double qoantity = billStringEntry.getQuantity();
//                if(qoantity - 1 >0){
//                    qoantity  -= 1;
//                    double sum = billStringEntry.getPrice() * qoantity;
//                    double sumWithDisc = billStringEntry.getPriceWithDiscount() * qoantity;
//                    editLineCount(sumWithDisc, sum, qoantity);
//                }
//
//            }
//            else{
//                Toast.makeText(MainActivity.this, "Alegeti pozitia!", Toast.LENGTH_SHORT).show();
//            }
//        });
//        btn_edit_item.setOnClickListener(v -> {
//            if(item_newbill_clicked) {
//
//                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
//                View dialogView = inflater.inflate(R.layout.dialog_change_count_position, null);
//
//                final AlertDialog setCount = new AlertDialog.Builder(MainActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
//                setCount.setCancelable(false);
//                setCount.setView(dialogView);
//                setCount.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//
//                TextView txtTotalCount = dialogView.findViewById(R.id.et_input_count);
//                TextView txtName = dialogView.findViewById(R.id.txt_name_position);
//                Button btn_Cancel = dialogView.findViewById(R.id.btn_cancel);
//                Button btn_ok = dialogView.findViewById(R.id.btn_ok);
//                btn_ok.setText("OK");
//                Button btn_clear = dialogView.findViewById(R.id.btn_changecount_clear);
//                Button btn_delete = dialogView.findViewById(R.id.btn_changecount_delete);
//
//                txtName.setText(billStringEntry.getAssortmentFullName());
//
//                Button number_1 = dialogView.findViewById(R.id.btn_changecount_1);
//                Button number_2 = dialogView.findViewById(R.id.btn_changecount_2);
//                Button number_3 = dialogView.findViewById(R.id.btn_changecount_3);
//                Button number_4 = dialogView.findViewById(R.id.btn_changecount_4);
//                Button number_5 = dialogView.findViewById(R.id.btn_changecount_5);
//                Button number_6 = dialogView.findViewById(R.id.btn_changecount_6);
//                Button number_7 = dialogView.findViewById(R.id.btn_changecount_7);
//                Button number_8 = dialogView.findViewById(R.id.btn_changecount_8);
//                Button number_9 = dialogView.findViewById(R.id.btn_changecount_9);
//                Button number_0 = dialogView.findViewById(R.id.btn_changecount_0);
//
//                number_1.setOnClickListener(v121 -> txtTotalCount.append("1"));
//                number_2.setOnClickListener(v122 -> txtTotalCount.append("2"));
//                number_3.setOnClickListener(v123 -> txtTotalCount.append("3"));
//                number_4.setOnClickListener(v124 -> txtTotalCount.append("4"));
//                number_5.setOnClickListener(v125 -> txtTotalCount.append("5"));
//                number_6.setOnClickListener(v126 -> txtTotalCount.append("6"));
//                number_7.setOnClickListener(v127 -> txtTotalCount.append("7"));
//                number_8.setOnClickListener(v128 -> txtTotalCount.append("8"));
//                number_9.setOnClickListener(v129 -> txtTotalCount.append("9"));
//                number_0.setOnClickListener(v130 -> txtTotalCount.append("0"));
//
//
//                btn_delete.setOnClickListener(v131 -> { if (!txtTotalCount.getText().toString().equals("")) txtTotalCount.setText(txtTotalCount.getText().toString().substring(0, txtTotalCount.getText().toString().length() - 1)); });
//                btn_clear.setOnClickListener(v132 -> txtTotalCount.setText(""));
//                btn_ok.setOnClickListener(v133 -> {
//                    if (!txtTotalCount.getText().toString().equals("0") && !txtTotalCount.getText().toString().equals("") && !txtTotalCount.getText().toString().equals("0.0") && !txtTotalCount.getText().toString().equals("0.00") ) {
//                        double qoantity = Double.valueOf(txtTotalCount.getText().toString());
//                        double sum = billStringEntry.getPrice() * qoantity;
//                        double sumWithDisc = billStringEntry.getPriceWithDiscount() * qoantity;
//                        editLineCount(sumWithDisc,sum,qoantity);
//
//                        setCount.dismiss();
//                    }else {
//                        Toast.makeText(MainActivity.this, "Introduceti cantitatea!", Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//                btn_Cancel.setOnClickListener(v134 -> {
//                    if_addPositionActive = false;
//                    setCount.dismiss();
//                });
//                setCount.show();
//            }
//            else{
//                Toast.makeText(MainActivity.this, "Alegeti pozitia!", Toast.LENGTH_SHORT).show();
//            }
//        });
//        btn_list_bill.setOnClickListener(v -> startActivityForResult(new Intent(".ListBills"), REQUEST_ACTIVITY_LIST_BILL));
//        btn_new_bill.setOnClickListener(v -> {
//            openedBillId = null;
//            initRecyclerView();
//        });
//        btn_asl_list.setOnClickListener(v -> {
//            Intent listBill = new Intent(".Assortiment");
//
//            listBill.putExtra("id",openedBillId);
//            startActivityForResult(listBill, REQUEST_ACTIVITY_ASSORTIMENT);
//        });
//
//        btn_delete_item.setOnClickListener(v -> {
//            if(item_newbill_clicked) {
//                mRealm.executeTransaction(realm -> {
//                    BillString billStringRealmResults = realm.where(BillString.class).equalTo("billID", openedBillId).and().equalTo("id",billStringEntry.getId()).findFirst();
//                    if (billStringRealmResults != null) {
//                        billStringRealmResults.setDeleted(true);
//                        billStringRealmResults.setDeletionDate(new Date().getTime());
//                        billStringRealmResults.setDeleteBy(((BaseApplication)getApplication()).getUser().getId());
//                    }
//                    Bill billEntryRealmResults = realm.where(Bill.class).equalTo("id", openedBillId).findFirst();
//                    if (billEntryRealmResults != null) {
//                        billEntryRealmResults.setSum(billEntryRealmResults.getSum()  - (billStringEntry.getPrice() * billStringEntry.getQuantity()));
//                        billEntryRealmResults.setSumWithDiscount(billEntryRealmResults.getSumWithDiscount() - (billStringEntry.getPriceWithDiscount() * billStringEntry.getQuantity()));
//                    }
//
//                });
//                item_newbill_clicked = false;
//                billStringEntry = null;
//                initRecyclerView();
//            }
//            else{
//                Toast.makeText(MainActivity.this, "Alegeti pozitia!", Toast.LENGTH_SHORT).show();
//            }
//        });
//        btn_check_price.setOnClickListener(v -> {
//            if_check_priceActive = true;
//            LayoutInflater inflater = MainActivity.this.getLayoutInflater();
//            View dialogView = inflater.inflate(R.layout.dialog_check_price, null);
//
//            final AlertDialog checkPriceDialog = new AlertDialog.Builder(MainActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
//            //checkPriceDialog.setCancelable(false);
//            checkPriceDialog.setView(dialogView);
//            checkPriceDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//
//            tv_input_barcode = dialogView.findViewById(R.id.et_input_data);
//            Button btn_Cancel = dialogView.findViewById(R.id.btn_cancel_check_price);
//            Button btn_add = dialogView.findViewById(R.id.btn_add);
//            Button btn_search = dialogView.findViewById(R.id.btn_search);
//            Button btn_clear = dialogView.findViewById(R.id.btn_add_position_clear);
//            Button btn_delete = dialogView.findViewById(R.id.btn_add_position_delete);
//            final TextView txtName = dialogView.findViewById(R.id.txt_name_assortment);
//            final TextView txtPriceWithDiscount = dialogView.findViewById(R.id.price_with_discount);
//            final TextView txtPriceWithoutDiscount = dialogView.findViewById(R.id.price_without_discount);
//
//            Button number_1 = dialogView.findViewById(R.id.btn_add_position_1);
//            Button number_2 = dialogView.findViewById(R.id.btn_add_position_2);
//            Button number_3 = dialogView.findViewById(R.id.btn_add_position_3);
//            Button number_4 = dialogView.findViewById(R.id.btn_add_position_4);
//            Button number_5 = dialogView.findViewById(R.id.btn_add_position_5);
//            Button number_6 = dialogView.findViewById(R.id.btn_add_position_6);
//            Button number_7 = dialogView.findViewById(R.id.btn_add_position_7);
//            Button number_8 = dialogView.findViewById(R.id.btn_add_position_8);
//            Button number_9 = dialogView.findViewById(R.id.btn_add_position_9);
//            Button number_0 = dialogView.findViewById(R.id.btn_add_position_0);
//
//            number_1.setOnClickListener(v135 -> tv_input_barcode.append("1"));
//            number_2.setOnClickListener(v136 -> tv_input_barcode.append("2"));
//            number_3.setOnClickListener(v137 -> tv_input_barcode.append("3"));
//            number_4.setOnClickListener(v138 -> tv_input_barcode.append("4"));
//            number_5.setOnClickListener(v139 -> tv_input_barcode.append("5"));
//            number_6.setOnClickListener(v140 -> tv_input_barcode.append("6"));
//            number_7.setOnClickListener(v141 -> tv_input_barcode.append("7"));
//            number_8.setOnClickListener(v142 -> tv_input_barcode.append("8"));
//            number_9.setOnClickListener(v143 -> tv_input_barcode.append("9"));
//            number_0.setOnClickListener(v144 -> tv_input_barcode.append("0"));
//
//            final AssortmentRealm[] assortmentEntry = new AssortmentRealm[1];
//            btn_delete.setOnClickListener(v145 -> { if (!tv_input_barcode.getText().toString().equals("")) tv_input_barcode.setText(tv_input_barcode.getText().toString().substring(0, tv_input_barcode.getText().toString().length() - 1)); });
//            btn_clear.setOnClickListener(v146 -> tv_input_barcode.setText(""));
//            btn_search.setOnClickListener(v147 -> {
//                tv_input_barcode.setText("");
//                if(tv_input_barcode.getText().toString().length() == 13 || tv_input_barcode.getText().toString().length() == 8){
//                    assortmentEntry[0] = mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",tv_input_barcode.getText().toString()).findFirst();
//                }
//                else{
//                    assortmentEntry[0] =  mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",tv_input_barcode.getText().toString()).findFirst();
//                }
//                if(assortmentEntry[0] != null){
//                    AssortmentRealm assortmentFind = mRealm.copyFromRealm(assortmentEntry[0]);
//                    txtName.setText(assortmentFind.getName());
//                    double priceWithDisc = -1;
//
//                    if(!assortmentFind.getPromotions().isEmpty()){
//                        Promotion promo = assortmentFind.getPromotions().first();
//
//                        long startDate = replaceDate(promo.getStartDate());
//                        long endDate = replaceDate(promo.getEndDate());
//                        Date curentDate = new Date();
//                        long currDate = curentDate.getTime();
//
//                        long timeBegin = 0;
//                        long timeEnd = 0;
//
//                        if(promo.getTimeBegin() != null)    timeBegin = replaceDate(promo.getTimeBegin());
//                        if(promo.getTimeEnd() != null)    timeEnd = replaceDate(promo.getTimeEnd());
//
//                        if(currDate > startDate && currDate < endDate){
//                            if(timeBegin != 0 && timeEnd != 0){
//                                Date timeStart = new Date(timeBegin);
//                                int hourS = timeStart.getHours();
//                                int minS = timeStart.getMinutes();
//
//                                Date timeFinis = new Date(timeEnd);
//                                int hourE = timeFinis.getHours();
//                                int minE = timeFinis.getMinutes();
//
//                                Date one = new Date();
//                                one.setHours(hourS);
//                                one.setMinutes(minS);
//                                one.setSeconds(0);
//
//                                Date two = new Date();
//                                two.setHours(hourE);
//                                two.setMinutes(minE);
//                                two.setSeconds(0);
//
//                                if(hourE < hourS)
//                                    two.setDate(two.getDate() + 1);
//
//                                if(curentDate.after(one) && curentDate.before(two)){
//                                    priceWithDisc = promo.getPrice();
//                                }
//                                else{
//                                    priceWithDisc = assortmentFind.getPrice();
//                                }
//                            }
//                            else{
//                                priceWithDisc = promo.getPrice();
//                            }
//                        }
//                        else{
//                            priceWithDisc = assortmentFind.getPrice();
//                        }
//                    }
//                    else{
//                        priceWithDisc = assortmentFind.getPrice();
//                    }
//
//                    txtPriceWithDiscount.setText(String.format("%.2f",priceWithDisc).replace(",","."));
//                    txtPriceWithoutDiscount.setText(String.format("%.2f",assortmentFind.getPrice()).replace(",","."));
//                }
//                else{
//                    txtName.setText("Nu a fost gasit!");
//                    Toast.makeText(MainActivity.this, "Nu a fost gasit!", Toast.LENGTH_SHORT).show();
//                }
//            });
//
//            btn_Cancel.setOnClickListener(v148 -> {
//                if_check_priceActive = false;
//                checkPriceDialog.dismiss();
//            });
//            btn_add.setOnClickListener(v149 -> {
//                AssortmentRealm assortmentFind = mRealm.copyFromRealm(assortmentEntry[0]);
//                addItemsToOpenedBill(assortmentFind,1,tv_input_barcode.getText().toString(),true);
//                if_check_priceActive = false;
//                checkPriceDialog.dismiss();
//            });
//            checkPriceDialog.show();
//
//            DialogInterface.OnKeyListener keylistener = new DialogInterface.OnKeyListener() {
//
//                @Override
//                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent KEvent) {
//                    int keyaction = KEvent.getAction();
//
//                    if(keyaction == KeyEvent.ACTION_DOWN)
//                    {
//                        int keycode = KEvent.getKeyCode();
//                        int keyunicode = KEvent.getUnicodeChar(KEvent.getMetaState() );
//
//                        switch (keycode) {
//                            case KeyEvent.KEYCODE_1 : {
//                                tv_input_barcode.append("1");
//                                tv_input_barcode.requestFocus();
//                            }break;
//                            case KeyEvent.KEYCODE_2 : {
//                                tv_input_barcode.append("2");
//                                tv_input_barcode.requestFocus();
//                            }break;
//                            case KeyEvent.KEYCODE_3 : {
//                                tv_input_barcode.append("3");
//                                tv_input_barcode.requestFocus();
//                            }break;
//                            case KeyEvent.KEYCODE_4 : {
//                                tv_input_barcode.append("4");
//                                tv_input_barcode.requestFocus();
//                            }break;
//                            case KeyEvent.KEYCODE_5 : {
//                                tv_input_barcode.append("5");
//                                tv_input_barcode.requestFocus();
//                            }break;
//                            case KeyEvent.KEYCODE_6 : {
//                                tv_input_barcode.append("6");
//                                tv_input_barcode.requestFocus();
//                            }break;
//                            case KeyEvent.KEYCODE_7 : {
//                                tv_input_barcode.append("7");
//                                tv_input_barcode.requestFocus();
//                            }break;
//                            case KeyEvent.KEYCODE_8 : {
//                                tv_input_barcode.append("8");
//                                tv_input_barcode.requestFocus();
//                            }break;
//                            case KeyEvent.KEYCODE_9 : {
//                                tv_input_barcode.append("9");
//                                tv_input_barcode.requestFocus();
//                            }break;
//                            case KeyEvent.KEYCODE_0 : {
//                                tv_input_barcode.append("0");
//                                tv_input_barcode.requestFocus();
//                            }break;
//                            case KeyEvent.KEYCODE_ENTER : {
//                                tv_input_barcode.setText("");
//
//                                if(tv_input_barcode.getText().toString().length() == 13 || tv_input_barcode.getText().toString().length() == 8){
//                                    assortmentEntry[0] = mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",tv_input_barcode.getText().toString()).findFirst();
//                                }
//                                else{
//                                    assortmentEntry[0] =  mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",tv_input_barcode.getText().toString()).findFirst();
//                                }
//                                if(assortmentEntry[0] != null){
//
//                                    AssortmentRealm assortmentFind = mRealm.copyFromRealm(assortmentEntry[0]);
//                                    txtName.setText(assortmentFind.getName());
//                                    txtPriceWithDiscount.setText(String.format("%.2f",assortmentFind.getPrice()).replace(",","."));
//                                    double priceWithDisc = -1;
//
//                                    if(!assortmentFind.getPromotions().isEmpty()){
//                                        Promotion promo = assortmentFind.getPromotions().first();
//
//                                        long startDate = replaceDate(promo.getStartDate());
//                                        long endDate = replaceDate(promo.getEndDate());
//                                        Date curentDate = new Date();
//                                        long currDate = curentDate.getTime();
//
//                                        long timeBegin = 0;
//                                        long timeEnd = 0;
//
//                                        if(promo.getTimeBegin() != null)    timeBegin = replaceDate(promo.getTimeBegin());
//                                        if(promo.getTimeEnd() != null)    timeEnd = replaceDate(promo.getTimeEnd());
//
//                                        if(currDate > startDate && currDate < endDate){
//                                            if(timeBegin != 0 && timeEnd != 0){
//                                                Date timeStart = new Date(timeBegin);
//                                                int hourS = timeStart.getHours();
//                                                int minS = timeStart.getMinutes();
//
//                                                Date timeFinis = new Date(timeEnd);
//                                                int hourE = timeFinis.getHours();
//                                                int minE = timeFinis.getMinutes();
//
//                                                Date one = new Date();
//                                                one.setHours(hourS);
//                                                one.setMinutes(minS);
//                                                one.setSeconds(0);
//
//                                                Date two = new Date();
//                                                two.setHours(hourE);
//                                                two.setMinutes(minE);
//                                                two.setSeconds(0);
//
//                                                if(hourE < hourS)
//                                                    two.setDate(two.getDate() + 1);
//
//                                                if(curentDate.after(one) && curentDate.before(two)){
//                                                    priceWithDisc = promo.getPrice();
//                                                }
//                                                else{
//                                                    priceWithDisc = assortmentFind.getPrice();
//                                                }
//                                            }
//                                            else{
//                                                priceWithDisc = promo.getPrice();
//                                            }
//                                        }
//                                        else{
//                                            priceWithDisc = assortmentFind.getPrice();
//                                        }
//                                    }
//                                    else{
//                                        priceWithDisc = assortmentFind.getPrice();
//                                    }
//                                    txtPriceWithoutDiscount.setText(String.format("%.2f",priceWithDisc).replace(",","."));
//
//                                }
//                                else{
//                                    txtName.setText("Nu a fost gasit!");
//                                    Toast.makeText(MainActivity.this, "Nu a fost gasit!", Toast.LENGTH_SHORT).show();
//                                }
//                            }break;
//                            default:break;
//                        }
//                    }
//                    return false;
//                }
//            };
//            checkPriceDialog.setOnKeyListener(keylistener );
//        });
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
//                            tv_input_barcode.append("1");
//                            tv_input_barcode.requestFocus();
//                        }break;
//                        case KeyEvent.KEYCODE_2 : {
//                            tv_input_barcode.append("2");
//                            tv_input_barcode.requestFocus();
//                        }break;
//                        case KeyEvent.KEYCODE_3 : {
//                            tv_input_barcode.append("3");
//                            tv_input_barcode.requestFocus();
//                        }break;
//                        case KeyEvent.KEYCODE_4 : {
//                            tv_input_barcode.append("4");
//                            tv_input_barcode.requestFocus();
//                        }break;
//                        case KeyEvent.KEYCODE_5 : {
//                            tv_input_barcode.append("5");
//                            tv_input_barcode.requestFocus();
//                        }break;
//                        case KeyEvent.KEYCODE_6 : {
//                            tv_input_barcode.append("6");
//                            tv_input_barcode.requestFocus();
//                        }break;
//                        case KeyEvent.KEYCODE_7 : {
//                            tv_input_barcode.append("7");
//                            tv_input_barcode.requestFocus();
//                        }break;
//                        case KeyEvent.KEYCODE_8 : {
//                            tv_input_barcode.append("8");
//                            tv_input_barcode.requestFocus();
//                        }break;
//                        case KeyEvent.KEYCODE_9 : {
//                            tv_input_barcode.append("9");
//                            tv_input_barcode.requestFocus();
//                        }break;
//                        case KeyEvent.KEYCODE_0 : {
//                            tv_input_barcode.append("0");
//                            tv_input_barcode.requestFocus();
//                        }break;
//                        case KeyEvent.KEYCODE_ENTER : {
//
//                            AssortmentRealm assortmentEntry;
//                            if(tv_input_barcode.getText().toString().length() == 13 || tv_input_barcode.getText().toString().length() == 8){
//                                assortmentEntry = mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",tv_input_barcode.getText().toString()).findFirst();
//                            }
//                            else{
//                                assortmentEntry =  mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",tv_input_barcode.getText().toString()).findFirst();
//                            }
//                            if(assortmentEntry != null){
//                                AssortmentRealm assortmentFind = mRealm.copyFromRealm(assortmentEntry);
//                                addItemsToOpenedBill(assortmentFind,1,tv_input_barcode.getText().toString(),true);
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
//        //оплата - закрыть счет
//        btn_payment_bill.setOnClickListener(v -> {
//            sumBillToPay = Double.valueOf(txt_total_sum_for_pay.getText().toString());
//
//            LayoutInflater inflater = MainActivity.this.getLayoutInflater();
//            View dialogView = inflater.inflate(R.layout.dialog_payment_bill, null);
//
//            payment = new AlertDialog.Builder(MainActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
//            payment.setCancelable(false);
//            payment.setView(dialogView);
//            payment.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//
//            txt_input_sum = dialogView.findViewById(R.id.et_input_data2);
//            TextView txt_total_bon = dialogView.findViewById(R.id.txt_total_payment);
//            final TextView txt_incasat = dialogView.findViewById(R.id.txt_incasat_payment);
//            final TextView txt_rest_de_incasat = dialogView.findViewById(R.id.txt_rest_incasat_payment);
//            final TextView txt_rest = dialogView.findViewById(R.id.txt_rest_payment);
//
//            LinearLayout LL_btn_pay  = dialogView.findViewById(R.id.LL_btn_pay);
//            LinearLayout LL_payments  = dialogView.findViewById(R.id.LL_paymentMode);
//
//            //caut daca contul a fost achitat partial, si adaug text mai jos cu ce tip de plata si ce suma
//            //plus la asta daca este vreo achitare deja facuta, verific daca este necesar de imprimat bonul fiscal si daca da, filtrez tipruile de plata dupa criteriu - printFiscalReceip
//            RealmResults<BillPaymentType> bill = mRealm.where(BillPaymentType.class).equalTo("billID",openedBillId).findAll();
//            boolean printCheck = false;    // daca trebuie bon fiscal sau nu
//            boolean isPaymentOnlyOne = false;   // daca exista vreo achitare
//
//            if(!bill.isEmpty()){
//                for (int i = 0; i < bill.size(); i++){
//                    BillPaymentType paymentType = bill.get(i);
//                    isPaymentOnlyOne = true;
//                    //dupa id a achitarii contului cautam tipul de plata
//                    String id = paymentType.getPaymentTypeID();
//                    PaymentType payment = mRealm.where(PaymentType.class).equalTo("externalId",id).findFirst();
//                    if(payment != null){
//                        printCheck = payment.getPrintFiscalCheck();
//                    }
//
//                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT,1);
//                    LinearLayout.LayoutParams layoutParamsPayments = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,1);
//                    LinearLayout LLpayment = new LinearLayout(MainActivity.this);
//                    LLpayment.setOrientation(LinearLayout.VERTICAL);
//
//                    TextView tvName = new TextView(MainActivity.this);
//                    TextView tvSum = new TextView(MainActivity.this);
//
//                    tvName.setText(paymentType.getName());
//                    tvName.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
//                    tvName.setTextSize(18);
//                    tvName.setTextColor(getResources().getColor(R.color.toolbar_color));
//                    tvName.setLayoutParams(layoutParams);
//
//                    tvSum.setText(String.format("%.2f", paymentType.getSum()).replace(",","."));
//                    tvSum.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
//                    tvSum.setTextSize(18);
//                    tvSum.setTextColor(getResources().getColor(R.color.toolbar_color));
//                    tvSum.setLayoutParams(layoutParams);
//
//                    LLpayment.addView(tvName,layoutParamsPayments);
//                    LLpayment.addView(tvSum,layoutParamsPayments);
//
//                    LL_payments.addView(LLpayment,layoutParams);
//                    billPaymentedSum += paymentType.getSum();
//                }
//            }
//            //daca este vreo achitare verificam cum o fost achitat ,cu bin sau fara bon
//            if(isPaymentOnlyOne){
//                if(printCheck){
//                    //caut tipurile de plata care sunt in baza si le adaug butoane filtrind dupa
//                    RealmResults<PaymentType> paymentTypesResult = mRealm.where(PaymentType.class).equalTo("printFiscalCheck",true).findAll();
//                    if(!paymentTypesResult.isEmpty()){
//                        for(int o = 0; o <paymentTypesResult.size(); o++){
//                            PaymentType paymentType = new PaymentType();
//                            paymentType.setCode(paymentTypesResult.get(o).getCode());
//                            paymentType.setPredefinedIndex(paymentTypesResult.get(o).getPredefinedIndex());
//                            paymentType.setPrintFiscalCheck(paymentTypesResult.get(o).getPrintFiscalCheck());
//                            paymentType.setExternalId(paymentTypesResult.get(o).getExternalId());
//                            paymentType.setName(paymentTypesResult.get(o).getName());
//
//                            myButton = new Button(MainActivity.this);
//                            myButton.setText(paymentType.getName());
//                            myButton.setTag(paymentType);
//                            myButton.setOnClickListener(test);
//
//                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT,1);
//                            LL_btn_pay.addView(myButton, lp);
//                        }
//                    }
//                }
//                else{
//                    //caut tipurile de plata care sunt in baza si le adaug butoane filtrind dupa
//                    RealmResults<PaymentType> paymentTypesResult = mRealm.where(PaymentType.class).equalTo("printFiscalCheck",false).findAll();
//                    if(!paymentTypesResult.isEmpty()){
//                        for(int o = 0; o <paymentTypesResult.size(); o++){
//                            PaymentType paymentType = new PaymentType();
//                            paymentType.setCode(paymentTypesResult.get(o).getCode());
//                            paymentType.setPredefinedIndex(paymentTypesResult.get(o).getPredefinedIndex());
//                            paymentType.setPrintFiscalCheck(paymentTypesResult.get(o).getPrintFiscalCheck());
//                            paymentType.setExternalId(paymentTypesResult.get(o).getExternalId());
//                            paymentType.setName(paymentTypesResult.get(o).getName());
//
//                            myButton = new Button(MainActivity.this);
//                            myButton.setText(paymentType.getName());
//                            myButton.setTag(paymentType);
//                            myButton.setOnClickListener(test);
//
//                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT,1);
//                            LL_btn_pay.addView(myButton, lp);
//                        }
//                    }
//                }
//            }
//            else{
//                //caut tipurile de plata care sunt in baza si le adaug butoane
//                RealmResults<PaymentType> paymentTypesResult = mRealm.where(PaymentType.class).findAll();
//                if(!paymentTypesResult.isEmpty()){
//                    for(int o = 0; o <paymentTypesResult.size(); o++){
//                        PaymentType paymentType = new PaymentType();
//                        paymentType.setCode(paymentTypesResult.get(o).getCode());
//                        paymentType.setPredefinedIndex(paymentTypesResult.get(o).getPredefinedIndex());
//                        paymentType.setPrintFiscalCheck(paymentTypesResult.get(o).getPrintFiscalCheck());
//                        paymentType.setExternalId(paymentTypesResult.get(o).getExternalId());
//                        paymentType.setName(paymentTypesResult.get(o).getName());
//
//                        myButton = new Button(MainActivity.this);
//                        myButton.setText(paymentType.getName());
//                        myButton.setTag(paymentType);
//                        myButton.setOnClickListener(test);
//
//                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT,1);
//                        LL_btn_pay.addView(myButton, lp);
//                    }
//                }
//            }
//
//            Button btn_Cancel = dialogView.findViewById(R.id.btn_cancel_payment);
//            Button clear = dialogView.findViewById(R.id.btn_payment_clear);
//            Button delete = dialogView.findViewById(R.id.btn_payment_delete);
//
//            txt_input_sum.setText(String.format("%.2f",sumBillToPay - billPaymentedSum).replace(",","."));
//            txt_total_bon.setText(txt_total_sum_for_pay.getText().toString());
//            txt_incasat.setText(txt_total_sum_for_pay.getText().toString());
//
//            Button number_1 = dialogView.findViewById(R.id.btn_payment_1);
//            Button number_2 = dialogView.findViewById(R.id.btn_payment_2);
//            Button number_3 = dialogView.findViewById(R.id.btn_payment_3);
//            Button number_4 = dialogView.findViewById(R.id.btn_payment_4);
//            Button number_5 = dialogView.findViewById(R.id.btn_payment_5);
//            Button number_6 = dialogView.findViewById(R.id.btn_payment_6);
//            Button number_7 = dialogView.findViewById(R.id.btn_payment_7);
//            Button number_8 = dialogView.findViewById(R.id.btn_payment_8);
//            Button number_9 = dialogView.findViewById(R.id.btn_payment_9);
//            Button number_0 = dialogView.findViewById(R.id.btn_payment_0);
//            Button number_20 = dialogView.findViewById(R.id.btn_payment_20);
//            Button number_50 = dialogView.findViewById(R.id.btn_payment_50);
//            Button number_100 = dialogView.findViewById(R.id.btn_payment_100);
//            Button number_200 = dialogView.findViewById(R.id.btn_payment_200);
//            Button number_500 = dialogView.findViewById(R.id.btn_payment_500);
//            Button number_1000 = dialogView.findViewById(R.id.btn_payment_1000);
//            Button point = dialogView.findViewById(R.id.btn_payment_point);
//
//            number_1.setOnClickListener(v1 -> txt_input_sum.append("1"));
//            number_2.setOnClickListener(v12 -> txt_input_sum.append("2"));
//            number_3.setOnClickListener(v13 -> txt_input_sum.append("3"));
//            number_4.setOnClickListener(v14 -> txt_input_sum.append("4"));
//            number_5.setOnClickListener(v15 -> txt_input_sum.append("5"));
//            number_6.setOnClickListener(v16 -> txt_input_sum.append("6"));
//            number_7.setOnClickListener(v17 -> txt_input_sum.append("7"));
//            number_8.setOnClickListener(v18 -> txt_input_sum.append("8"));
//            number_9.setOnClickListener(v19 -> txt_input_sum.append("9"));
//            number_0.setOnClickListener(v110 -> txt_input_sum.append("0"));
//            number_20.setOnClickListener(v111 -> txt_input_sum.setText("20"));
//            number_50.setOnClickListener(v112 -> txt_input_sum.setText("50"));
//            number_100.setOnClickListener(v113 -> txt_input_sum.setText("100"));
//            number_200.setOnClickListener(v114 -> txt_input_sum.setText("200"));
//            number_500.setOnClickListener(v115 -> txt_input_sum.setText("500"));
//            number_1000.setOnClickListener(v116 -> txt_input_sum.setText("1000"));
//
//            txt_input_sum.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//                }
//
//                @Override
//                public void onTextChanged(CharSequence s, int start, int before, int count) {
//                    if(txt_input_sum.getText().toString().equals("")){
//                        txt_incasat.setText(String.format("%.2f", billPaymentedSum).replace(",","."));
//                        txt_rest_de_incasat.setText( String.format("%.2f", sumBillToPay - billPaymentedSum).replace(",","."));
//                        txt_rest.setText("0.00");
//                    }
//                    else{
//                        double incasat = 0.0;
//                        try{
//                            incasat = Double.valueOf(txt_input_sum.getText().toString());
//                        }catch (Exception e){
//                            incasat = Double.valueOf(txt_input_sum.getText().toString().replace(",","."));
//                        }
//
//                        txt_incasat.setText(String.format("%.2f", incasat + billPaymentedSum).replace(",","."));
//                        double restIncasat = 0.0;
//                        double rest = 0.0;
//
//                        if( (incasat +billPaymentedSum) <= sumBillToPay){
//                            txt_rest.setText("0.00");
//                            restIncasat = sumBillToPay - (incasat + billPaymentedSum);
//                            txt_rest_de_incasat.setText( String.format("%.2f", restIncasat).replace(",","."));
//                        }else{
//                            txt_rest_de_incasat.setText("0.00");
//                            rest = (incasat + billPaymentedSum) - sumBillToPay;
//                            txt_rest.setText( String.format("%.2f", rest).replace(",","."));
//                        }
//                    }
//
//                }
//
//                @Override
//                public void afterTextChanged(Editable s) {
//
//                }
//            });
//
//            point.setOnClickListener(v117 -> {
//                String test = txt_input_sum.getText().toString();
//                boolean contains = false;
//                for (int i = 0; i < test.length(); i++) {
//                    String chars = String.valueOf(test.charAt(i));
//                    if (chars.equals(".")) {
//                        contains = true;
//                    }
//                }
//                if (!contains) {
//                    if(txt_input_sum.getText().toString().equals(""))
//                        txt_input_sum.append("0.");
//                    else
//                        txt_input_sum.append(".");
//                }
//            });
//
//            delete.setOnClickListener(v118 -> { if (!txt_input_sum.getText().toString().equals("")) txt_input_sum.setText(txt_input_sum.getText().toString().substring(0, txt_input_sum.getText().toString().length() - 1)); });
//            clear.setOnClickListener(v119 -> txt_input_sum.setText(""));
//            btn_Cancel.setOnClickListener(v120 -> { payment.dismiss(); billPaymentedSum = 0; });
//
//            payment.show();
//            payment.getWindow().setLayout(800,730);
//
//
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

    public static void draweOpen(Bill bill){
        drawer.openDrawer(GravityCompat.END);
//        DrawerLayout.LayoutParams mMainParams = new DrawerLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
//        double mDisplayWidth = display.getWidth();
//        mMainParams.width = (int)(mDisplayWidth * 0.58);
//        drawerConstraint.setLayoutParams(mMainParams);

        TextView clientBill = drawerConstraint.findViewById(R.id.txt_client);
        TextView totalItemsBill = drawerConstraint.findViewById(R.id.txttotal_items);
        TextView totalBill = drawerConstraint.findViewById(R.id.txt_total);
        TextView numberBill = drawerConstraint.findViewById(R.id.bill_number_nav);
        TextView discount = drawerConstraint.findViewById(R.id.txt_discount);

        MaterialButton btnPayBill = drawerConstraint.findViewById(R.id.btnPay);
        MaterialButton btnEditBill = drawerConstraint.findViewById(R.id.btnEdit);
        MaterialButton btnDeleteBill = drawerConstraint.findViewById(R.id.btnDelete);

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
            if(adapter.getItemCount() > 0){
                new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                        .setTitle("Attention open bill!")
                        .setMessage("Current bill is not empty!Do you want save current bill and opent selected?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            openedBillId = bill.getId();
                            drawer.closeDrawer(GravityCompat.END);
                            initRecyclerView();
                        })
                        .setNegativeButton("No",(dialogInterface, i) -> {
                            dialogInterface.dismiss();
                        })
                        .show();
            }
        });


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

            mRealm.executeTransaction(realm ->{
                RealmResults<Bill> result = realm.where(Bill.class).equalTo("shiftId", shift.getId()).equalTo("state",BaseEnum.BILL_OPEN).findAll();
                if(!result.isEmpty()){
//                    BadgeDrawable badge = tabLayout.getTabAt(2).getOrCreateBadge();
//                    badge.setVisible(true);
//                    badge.setNumber(result.size());
//                    BaseApplication.getInstance().setBadgeNumbers(result.size());
                }
            });
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

//
//    private final TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
//        /**
//         * Called when the input method default action key is pressed.
//         */
//        @Override
//        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//            if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
//                searchAssortment.setQuery("bla bla bla",true);
//            return true;
//        }
//    };

//    View.OnClickListener test = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            PaymentType paymentType = (PaymentType) v.getTag();
//            boolean printFiscalCheck = paymentType.getPrintFiscalCheck();
//            String code = paymentType.getCode();
//            if(code == null)
//                code = "404";
//            int resultCloseReceip = 0;
//
//            //primesc rindurile la cont
//            RealmList<BillString> billStrings = new RealmList<>();
//            RealmList<BillPaymentType> billPaymentTypes = new RealmList<>();
//
//            RealmResults<BillString> billStringsResult = mRealm.where(BillString.class)
//                    .equalTo("billID", openedBillId)
//                    .and()
//                    .equalTo("isDeleted",false)
//                    .findAll();
//            if (!billStringsResult.isEmpty()) {
//                billStrings.addAll(billStringsResult);
//            }
//
//            //tipurile de achitare deja facute la cont in caz ca nu a fost achitat integral
//            RealmResults<BillPaymentType> billPayResult = mRealm.where(BillPaymentType.class)
//                    .equalTo("billID", openedBillId).findAll();
//            if(!billPayResult.isEmpty()){
//                billPaymentTypes.addAll(billPayResult);
//            }
//
//            double inputSum = 0;
//            try {
//                inputSum = Double.valueOf(txt_input_sum.getText().toString());
//            } catch (Exception e) {
//                inputSum = Double.valueOf(txt_input_sum.getText().toString().replace(",", "."));
//            }
//
//            if ((billPaymentedSum + inputSum) >= sumBillToPay) {
//                int workFisc = getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getInt("ModeFiscalWork",0);
//
//                if (printFiscalCheck) {
//                    if(workFisc == 1){
//                        DatecsFiscalDevice fiscalDevice = null;
//                        if( ((BaseApplication) getApplication()).getMyFiscalDevice() != null){
//                            fiscalDevice = ((BaseApplication) getApplication()).getMyFiscalDevice();
//                        }
//                        if(fiscalDevice != null && fiscalDevice.isConnectedDeviceV2()){
//                            resultCloseReceip = ((BaseApplication) getApplication()).printFiscalReceipt(fiscalReceipt, billStrings, paymentType, inputSum, billPaymentTypes,(shiftEntry.getBillCounter() + 1));
//                            if (resultCloseReceip != 0) {
//                                BillPaymentType billPaymentType= new BillPaymentType();
//                                billPaymentType.setId(UUID.randomUUID().toString());
//                                billPaymentType.setBillID(openedBillId);
//                                billPaymentType.setName(paymentType.getName());
//                                billPaymentType.setPaymentCode(Integer.valueOf(code));
//                                billPaymentType.setPaymentTypeID(paymentType.getExternalId());
//                                billPaymentType.setSum(inputSum);
//                                billPaymentType.setAuthor(((BaseApplication) getApplication()).getUser().getId());
//                                billPaymentType.setCreateDate(new Date().getTime());
//
//                                int finalResultCloseReceip = resultCloseReceip;
//                                mRealm.executeTransaction(realm ->{
//                                    Bill bill = realm.where(Bill.class).equalTo("id",openedBillId).findFirst();
//                                    if(bill != null){
//                                        bill.setReceiptNumFiscalMemory(finalResultCloseReceip);
//                                        bill.setState(1);
//                                        bill.setCloseDate(new Date().getTime());
//                                        bill.setClosedBy(((BaseApplication) getApplication()).getUser().getId());
//                                        bill.getBillPaymentTypes().add(billPaymentType);
//
//                                    }
//                                });
//
//                                tv_primit_lei.setText(String.format("%.2f", inputSum + billPaymentedSum).replace(",", "."));
//                                tv_rest_lei.setText(String.format("%.2f", (inputSum + billPaymentedSum)- sumBillToPay).replace(",", "."));
//
//                                initRecyclerView();
//
//                                billPaymentedSum = 0;
//                                payment.dismiss();
//
//                                openedBillId = null;
//                            }
//                        }
//                        else{
//                            Toast.makeText(MainActivity.this, "Aparatul fiscal nu este conectat!", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                    if(workFisc == 2){
//                       ((BaseApplication) getApplication()).printReceiptFiscalService(billStrings, paymentType, inputSum, billPaymentTypes);
//
//                        BillPaymentType billPaymentType= new BillPaymentType();
//                        billPaymentType.setId(UUID.randomUUID().toString());
//                        billPaymentType.setBillID(openedBillId);
//                        billPaymentType.setName(paymentType.getName());
//                        billPaymentType.setPaymentCode(Integer.valueOf(code));
//                        billPaymentType.setPaymentTypeID(paymentType.getExternalId());
//                        billPaymentType.setSum(inputSum);
//                        billPaymentType.setAuthor(((BaseApplication) getApplication()).getUser().getId());
//                        billPaymentType.setCreateDate(new Date().getTime());
//
//                        mRealm.executeTransaction(realm ->{
//                            Bill bill = realm.where(Bill.class).equalTo("id",openedBillId).findFirst();
//                            if(bill != null){
//                                bill.setReceiptNumFiscalMemory(0);
//                                bill.setState(1);
//                                bill.setCloseDate(new Date().getTime());
//                                bill.setClosedBy(((BaseApplication) getApplication()).getUser().getId());
//                                bill.getBillPaymentTypes().add(billPaymentType);
//
//                            }
//                        });
//
//                        tv_primit_lei.setText(String.format("%.2f", inputSum + billPaymentedSum).replace(",", "."));
//                        tv_rest_lei.setText(String.format("%.2f", (inputSum + billPaymentedSum)- sumBillToPay).replace(",", "."));
//
//                        initRecyclerView();
//
//                        billPaymentedSum = 0;
//                        payment.dismiss();
//
//                        openedBillId = null;
//                    }
//
//                }
//                else {
//                    BillPaymentType billPaymentType= new BillPaymentType();
//                    billPaymentType.setId(UUID.randomUUID().toString());
//                    billPaymentType.setBillID(openedBillId);
//                    billPaymentType.setName(paymentType.getName());
//                    billPaymentType.setPaymentCode(Integer.valueOf(code));
//                    billPaymentType.setPaymentTypeID(paymentType.getExternalId());
//                    billPaymentType.setSum(sumBillToPay - billPaymentedSum);
//                    billPaymentType.setAuthor(((BaseApplication) getApplication()).getUser().getId());
//                    billPaymentType.setCreateDate(new Date().getTime());
//
//                    mRealm.executeTransaction(realm ->{
//                        Bill bill = realm.where(Bill.class).equalTo("id",openedBillId).findFirst();
//                        if(bill != null){
//                            bill.setReceiptNumFiscalMemory(0);
//                            bill.setState(1);
//                            bill.setCloseDate(new Date().getTime());
//                            bill.setClosedBy(((BaseApplication) getApplication()).getUser().getId());
//                            bill.getBillPaymentTypes().add(billPaymentType);
//                        }
//                    });
//
//                    tv_primit_lei.setText(String.format("%.2f", inputSum + billPaymentedSum).replace(",", "."));
//                    tv_rest_lei.setText(String.format("%.2f", (inputSum +billPaymentedSum ) - sumBillToPay).replace(",", "."));
//                    initRecyclerView();
//
//                    billPaymentedSum = 0;
//                    payment.dismiss();
//                    openedBillId = null;
//                }
//            }
//            else if ((billPaymentedSum + inputSum) < sumBillToPay) {
//
//                if (printFiscalCheck ){
//
//                }
//
//                BillPaymentType billPaymentType = new BillPaymentType();
//                billPaymentType.setId(UUID.randomUUID().toString());
//                billPaymentType.setBillID(openedBillId);
//                billPaymentType.setName(paymentType.getName());
//                billPaymentType.setPaymentCode(Integer.valueOf(code));
//                billPaymentType.setPaymentTypeID(paymentType.getExternalId());
//                billPaymentType.setSum(inputSum);
//                billPaymentType.setAuthor(((BaseApplication) getApplication()).getUser().getId());
//                billPaymentType.setCreateDate(new Date().getTime());
//                //TODO add billPayment type to bill
//                mRealm.executeTransaction(realm ->{
//                    Bill bill = realm.where(Bill.class).equalTo("id",openedBillId).findFirst();
//                    if(bill != null){
//                        bill.setState(0);
//                        bill.getBillPaymentTypes().add(billPaymentType);
//                    }
//                });
//                tv_primit_lei.setText(String.format("%.2f", inputSum + billPaymentedSum).replace(",", "."));
//
//                payment.dismiss();
//                billPaymentedSum = 0;
//            }
//        }
//    };
//

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
                double priceWithDisc = -1;

                Promotion promo = null;

                if(!assortmentEntry.getPromotions().isEmpty()){
                    promo = assortmentEntry.getPromotions().first();

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
                                billString.setPromoLineID(promo.getId());
                                billString.setPromoPrice(promo.getPrice());
                            }
                            else{
                                priceWithDisc = assortmentEntry.getPrice();
                            }
                        }
                        else{
                            priceWithDisc = promo.getPrice();
                            billString.setPromoLineID(promo.getId());
                            billString.setPromoPrice(promo.getPrice());
                        }
                    }
                    else{
                        priceWithDisc = assortmentEntry.getPrice();
                    }
                }
                else{
                    priceWithDisc = assortmentEntry.getPrice();
                }

                billString.setCreateBy(BaseApplication.getInstance().getUser().getId());
                billString.setAssortmentExternID(assortmentEntry.getId());
                billString.setAssortmentFullName(assortmentEntry.getName());
                billString.setBillID(openedBillId);
                billString.setId(UUID.randomUUID().toString());
                billString.setQuantity(count);
                billString.setPrice(assortmentEntry.getPrice());
                billString.setPriceLineID(assortmentEntry.getPriceLineId());
                if(promo !=null)
                    billString.setPromoLineID(promo.getId());
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
//        if (scaned_item)
//            searchAssortment.setText("");
        Toast.makeText(MainActivity.this, event.getKeyCode(), Toast.LENGTH_SHORT).show();

        switch (event.getAction()){
            case KeyEvent.ACTION_DOWN :{
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_1 : {
                        searchBarcode = searchBarcode + "1";
                    }break;
                    case KeyEvent.KEYCODE_2 : {
                        searchBarcode = searchBarcode + "2";
                    }break;
                    case KeyEvent.KEYCODE_3 : {
//                        if (!if_check_priceActive && !if_addPositionActive) {
//                            scaned_item=false;
//                            inpput.append("3");
//                            inpput.requestFocus();
//                            inpput.requestFocusFromTouch();
//                        }
                        searchBarcode = searchBarcode + "3";
                    }break;
                    case KeyEvent.KEYCODE_4 : {
//                        if (!if_check_priceActive && !if_addPositionActive) {
//                            scaned_item = false;
//                            inpput.append("4");
//                            inpput.requestFocus();
//                            inpput.requestFocusFromTouch();
//                        }
                        searchBarcode = searchBarcode + "4";
                    }break;
                    case KeyEvent.KEYCODE_5 : {
//                        if (!if_check_priceActive && !if_addPositionActive) {
//                            scaned_item = false;
//                            inpput.append("5");
//                            inpput.requestFocus();
//                            inpput.requestFocusFromTouch();
//                        }
                        searchBarcode = searchBarcode + "5";
                    }break;
                    case KeyEvent.KEYCODE_6 : {
//                        if (!if_check_priceActive && !if_addPositionActive) {
//                            scaned_item = false;
//                            inpput.append("6");
//                            inpput.requestFocus();
//                            inpput.requestFocusFromTouch();
//                        }
                        searchBarcode = searchBarcode + "6";
                    }break;
                    case KeyEvent.KEYCODE_7 : {
//                        if (!if_check_priceActive && !if_addPositionActive) {
//                            scaned_item = false;
//                            inpput.append("7");
//                            inpput.requestFocus();
//                            inpput.requestFocusFromTouch();
//                        }
                        searchBarcode = searchBarcode + "7";
                    }break;
                    case KeyEvent.KEYCODE_8 : {
//                        if (!if_check_priceActive && !if_addPositionActive) {
//                            scaned_item = false;
//                            inpput.append("8");
//                            inpput.requestFocus();
//                            inpput.requestFocusFromTouch();
//                        }
                        searchBarcode = searchBarcode + "8";
                    }break;
                    case KeyEvent.KEYCODE_9 : {
//                        if (!if_check_priceActive && !if_addPositionActive) {
//                            scaned_item = false;
//                            inpput.append("9");
//                            inpput.requestFocus();
//                            inpput.requestFocusFromTouch();
//                        }
                        searchBarcode = searchBarcode + "9";
                    }break;
                    case KeyEvent.KEYCODE_0 : {
//                        if (!if_check_priceActive && !if_addPositionActive) {
//                            scaned_item = false;
//                            inpput.append("0");
//                            inpput.requestFocus();
//                            inpput.requestFocusFromTouch();
//                        }
                        searchBarcode = searchBarcode + "0";
                    }break;


                    case KeyEvent.KEYCODE_ENTER:{
                        Toast.makeText(MainActivity.this, "KeyEnter", Toast.LENGTH_SHORT).show();
                        searchBarcode = "";
                    }
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

            if(bill[0] != null)
                btnPay.setText("MDL " + String.format("%.2f", bill[0].getSumWithDiscount()));
            else
                btnPay.setText("MDL 0.00");
        }
    }

    private void functionOpenedShift(){
        postToastMessage("Shift is opened!");
        shiftOpenButtonPay = false;
        FragmentBills.showBillList();
        btnPay.setText("MDL 0.00");
    }

    private Shift findOpenedShift(){
        final Shift[] shift = {null};
        mRealm.executeTransaction(realm ->{
            Shift result = realm.where(Shift.class).equalTo("closed",false).findFirst();
            if(result != null) {
                shift[0] = realm.copyFromRealm(result);
                BaseApplication.getInstance().setShift(shift[0]);
            }
        });

        return shift[0];
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
                    .setPositiveButton("OKEY", (dialogInterface, i) -> {
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
                   //TODO dialog what shift is finis
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
}