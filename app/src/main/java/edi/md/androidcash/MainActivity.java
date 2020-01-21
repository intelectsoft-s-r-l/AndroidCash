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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.acs.smartcard.Reader;
import com.datecs.fiscalprinter.SDK.model.DatecsFiscalDevice;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReceipt;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import edi.md.androidcash.DynamicTabs.TabAdapter;
import edi.md.androidcash.NetworkUtils.ApiUtils;
import edi.md.androidcash.NetworkUtils.FiscalServiceResult.SimpleResult;
import edi.md.androidcash.NetworkUtils.PaymentType;
import edi.md.androidcash.NetworkUtils.Promotion;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.GetStateFiscalService;
import edi.md.androidcash.RealmHelper.AssortmentRealm;
import edi.md.androidcash.RealmHelper.Bill;
import edi.md.androidcash.RealmHelper.BillPaymentType;
import edi.md.androidcash.RealmHelper.BillString;
import edi.md.androidcash.RealmHelper.QuickGroupRealm;
import edi.md.androidcash.RealmHelper.Shift;
import edi.md.androidcash.adapters.CustomNewBillRealmAdapter;
import edi.md.androidcash.connectors.AbstractConnector;
import edi.md.androidcash.connectors.UsbDeviceConnector;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static edi.md.androidcash.GlobalVariables.SharedPrefFiscalService;
import static edi.md.androidcash.GlobalVariables.SharedPrefSettings;
import static edi.md.androidcash.GlobalVariables.SharedPrefWorkPlaceSettings;

public class MainActivity extends AppCompatActivity {
    Button btn_list_bill,btn_asl_list,btn_new_bill,btn_check_price,btn_add_position,btn_payment_bill;
    TextView txt_total_sum_for_pay,inpput,tv_schedule_shift, txt_input_sum , tv_primit_lei,tv_reducere_lei,tv_rest_lei;
    ImageView fiscal_printer;
    AlertDialog payment;
    ListView LW_NewBill;

    int REQUEST_ACTIVITY_ASSORTIMENT = 222;
    int REQUEST_ACTIVITY_LIST_BILL = 666;
    int REQUEST_ACTIVITY_LIST_SETTING = 999;

    boolean item_newbill_clicked = false;
    boolean scaned_item = false;
    boolean if_check_priceActive = false;
    boolean if_addPositionActive = false;

    double billPaymentedSum = 0;
    double sumBillToPay = 0;

    ImageButton btn_up_count,btn_down_count,btn_edit_item,btn_delete_item,btn_settings;
    TextView tv_input_barcode,tv_shift_state,tv_user_name;
    //tv_input_barcode - text view introducerea barcodului la adaugarea prin buton sau la verificarea pretului

    String billUid = null;
    Button myButton;

    FrameLayout frm_add_position,frm_new_bill, frm_check_price, frm_check_disc, frm_apply_disc, frm_delete_disc, frm_listBills, frm_ListAssortment;

    BillString billStringEntry;
    //datecs variables
    public DatecsFiscalDevice myFiscalDevice = null;
    private cmdReceipt.FiscalReceipt fiscalReceipt;

    //Declare timer
    CountDownTimer cTimer = null;

    //realm data bases
    private Realm mRealm;
    Shift shiftEntry = null;

    CustomNewBillRealmAdapter adapterString;

    SimpleDateFormat sdfChisinau;
    TimeZone tzInChisinau;

    //DynamicTabs
    private TabAdapter adapter;
    private TabLayout tab;
    private ViewPager viewPager;

    //deviceconect
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private PendingIntent mPermissionIntent;
    private UsbManager mManager;

    //NFC variables
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];

    //Reader ACR
    private Reader mReader;
    private ArrayAdapter<String> mReaderAdapter;
    private ArrayAdapter<String> mSlotAdapter;
    private static final String[] stateStrings = {"Unknown", "Absent",
            "Present", "Swallowed", "Powered", "Negotiable", "Specific"};

    public static final int DATECS_USB_VID = 65520;
    public static final int FTDI_USB_VID = 1027;

    boolean isFiscalPrinter = false;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {

                synchronized (this) {

                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if(isFiscalPrinter){
                        if (device.getManufacturerName().equals("Datecs")) {
                            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                                AbstractConnector connector = new UsbDeviceConnector(MainActivity.this, mManager, device);

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

                            // Open reader
                            postToast("Opening reader: " + device.getDeviceName() + "...");
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

            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (this) {
                    // Update reader list
                    mReaderAdapter.clear();
                    for (UsbDevice device : mManager.getDeviceList().values()) {
                        if (mReader.isSupported(device)) {
                            mReaderAdapter.add(device.getDeviceName());
                        }
                    }

                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (device != null && device.equals(mReader.getDevice())) {

                        // Clear slot items
                        mSlotAdapter.clear();

                        // Close reader
                        postToast("Closing reader...");
                        new CloseTask().execute();
                    }

                    if(isFiscalPrinter){
                        runOnUiThread(() -> fiscal_printer.setImageDrawable(getResources().getDrawable(R.drawable.fiscal_off)));
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
                        mManager.requestPermission(device, mPermissionIntent);
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
        mManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        // Register receiver for USB permission
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        registerReceiver(mReceiver, filter);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        TextView name_magazin = toolbar.findViewById(R.id.toolbar_title);
        inpput = toolbar.findViewById(R.id.toolbar_barcode_input);
        btn_settings = toolbar.findViewById(R.id.img_button_settings);
        tv_user_name = toolbar.findViewById(R.id.tv_user_name);
        tv_user_name.setText(((GlobalVariables)getApplication()).getUserName());


        name_magazin.setText(getSharedPreferences(SharedPrefWorkPlaceSettings,MODE_PRIVATE).getString("WorkPlaceName",""));

        setSupportActionBar(toolbar);

        initUIElements();


        if( ((GlobalVariables)getApplication()).getUser() == null){
//            tv_user_name.setText(((GlobalVariables)getApplication()).getUserName());
            btn_payment_bill.setEnabled(false);
            btn_add_position.setEnabled(false);
            btn_check_price.setEnabled(false);
            btn_new_bill.setEnabled(false);
            btn_list_bill.setEnabled(false);
            btn_asl_list.setEnabled(false);

            frm_add_position.setEnabled(false);
            frm_new_bill.setEnabled(false);
            frm_check_price.setEnabled(false);
            frm_apply_disc.setEnabled(false);
            frm_check_disc.setEnabled(false);
            frm_delete_disc.setEnabled(false);
            frm_listBills.setEnabled(false);
            frm_ListAssortment.setEnabled(false);
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
        }

        inpput.requestFocus();
        inpput.requestFocusFromTouch();

        sdfChisinau = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        tzInChisinau = TimeZone.getTimeZone("Europe/Chisinau");
        sdfChisinau.setTimeZone(tzInChisinau);

        mRealm = Realm.getDefaultInstance();

        fiscalReceipt = new cmdReceipt.FiscalReceipt();
        myFiscalDevice = ((GlobalVariables)getApplication()).getMyFiscalDevice();

        // Initialize slot spinner
        mSlotAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        mReaderAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);

        // Initialize reader ACR
        mReader = new Reader(mManager);
        mReader.setOnStateChangeListener((slotNum, prevState, currState) -> {
            postToast(" currstate " + currState);
            if (prevState < Reader.CARD_UNKNOWN || prevState > Reader.CARD_SPECIFIC) {
                prevState = Reader.CARD_UNKNOWN;
            }
            if (currState < Reader.CARD_UNKNOWN || currState > Reader.CARD_SPECIFIC) {
                currState = Reader.CARD_UNKNOWN;
            }
            if (currState == Reader.CARD_PRESENT) {

//                    byte[] command = {(byte)0xFF, (byte)0xCA, (byte)0x00, (byte)0x00, (byte)0x04};
//                    byte[] response = new byte[300];
//                    int responseLength = 0;
//
//                    try {
//
//                        responseLength = mReader.transmit(slotNum, command, command.length, response, response.length);
//
//                    } catch (ReaderException e) {
//
//                        e.printStackTrace();
//                        postToast(" errorr "+e.getMessage());
//                    }
//                    postToast("responseLength: " + responseLength);
//                    postToast("response: " + response[0]);


//
//                    byte[] command = { (byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x04 };
//                    byte[] response = new byte[300];
//                    int responseLength = 0;
//                    try {
//                        responseLength = mReader.transmit(slotNum, command, command.length, response,response.length);
//                    } catch (ReaderException e) {
//                        e.printStackTrace();
//                        postToast(" errorr "+e.getMessage());
//                    }
//                    postToast("responseLength: " + responseLength);
//                    postToast("response: " + response[0]);

                int actionNum = Reader.CARD_WARM_RESET;
                PowerParams params = new PowerParams();
                params.slotNum = slotNum;
                params.action = actionNum;

                new PowerTask().execute(params);

//                    try {
//                        // Get ATR
//                        postToast("Slot " + slotNum + ": Getting ATR...");
//                        byte[] atr = mReader.getAtr(slotNum);
//
//                        // Show ATR
//                        if (atr != null) {
//                            postToast("ATR:");
//                            logBuffer(atr, atr.length);
//                        } else {
//                            postToast("ATR: None");
//                        }
//
//                    } catch (IllegalArgumentException e) {
//                        postToast(e.toString());
//                    }
            }

//                // Show output
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        postToast(outputString);
//                    }
//                });
        });

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if(myFiscalDevice !=null ){
            boolean isConect = myFiscalDevice.isConnectedDeviceV2();

            if(isConect){
                fiscal_printer.setImageDrawable(getResources().getDrawable(R.drawable.fiscal_on));
            }
            else{
                fiscal_printer.setImageDrawable(getResources().getDrawable(R.drawable.fiscal_off));
            }
        }
        else{
            fiscal_printer.setImageDrawable(getResources().getDrawable(R.drawable.fiscal_off));
        }
        initQuickButtons();

        initRecyclerView();

        LW_NewBill.setOnItemClickListener((parent, view, position, id) -> {
            item_newbill_clicked = true;
            billStringEntry = adapterString.getItem(position);
        });
        btn_settings.setOnClickListener(v -> startActivityForResult(new Intent(".Settings"), REQUEST_ACTIVITY_LIST_SETTING));

        //listener for TextView on scan barcode assortment
        inpput.setOnEditorActionListener((v, actionId, event) -> {
             if(event.getKeyCode()==KeyEvent.KEYCODE_ENTER){
                 scaned_item = true;
                 AssortmentRealm realmResult = mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",inpput.getText().toString()).findFirst();
                 if(realmResult != null){
                     AssortmentRealm assortmentFind = mRealm.copyFromRealm(realmResult);
                     addAssortmentToBill(assortmentFind,inpput.getText().toString());
                     inpput.setText("");
                     initRecyclerView();
                 }
                 else{
                     Toast.makeText(MainActivity.this, "Nu a fost gasit!", Toast.LENGTH_SHORT).show();
                     inpput.setText("");
                 }
            }
            return false;
        });

        btn_up_count.setOnClickListener(v -> {
            if(item_newbill_clicked){
                double quantity = billStringEntry.getQuantity();
                quantity += 1;
                double sum = billStringEntry.getPriceWithDiscount() * quantity;
                final double[] totalForPay = {0};
                double finalQuantity = quantity;
                mRealm.executeTransaction(realm -> {
//                    BillString billStringRealmResults = realm.where(BillString.class).equalTo("id", billUid).and().equalTo("id",billStringEntry.getId()).findFirst();
//                    if (billStringRealmResults != null) {
//                        billStringRealmResults.setSum(sum);
//                        billStringRealmResults.setQuantity(finalQuantity);
//                    }
                    billStringEntry.setQuantity(finalQuantity);
                    billStringEntry.setSum(sum);

                    Bill billEntryRealmResults = realm.where(Bill.class).equalTo("id", billUid).findFirst();
                    if (billEntryRealmResults != null) {
                        billEntryRealmResults.setSum(billEntryRealmResults.getSum() + billStringEntry.getPrice());
                        billEntryRealmResults.setSumWithDiscount(billEntryRealmResults.getSumWithDiscount() + billStringEntry.getPriceWithDiscount());
                        totalForPay[0] = billEntryRealmResults.getSumWithDiscount();
                    }

                });
//                initRecyclerView();
                txt_total_sum_for_pay.setText(String.format("%.2f", totalForPay[0]).replace(",","."));
                adapterString.notifyDataSetChanged();
            }
            else{
                Toast.makeText(MainActivity.this, "Alegeti pozitia!", Toast.LENGTH_SHORT).show();
            }
        });
        btn_down_count.setOnClickListener(v -> {
            if(item_newbill_clicked){
                double qoantity = billStringEntry.getQuantity();
                if(qoantity - 1 >0){
                    qoantity  -= 1;
                    double sum = billStringEntry.getPriceWithDiscount() * qoantity;
                    double finalQuantity = qoantity;
                    final double[] totalForPay = {0};
                    mRealm.executeTransaction(realm -> {
//                        BillString billStringRealmResults = realm.where(BillString.class).equalTo("id", billUid).and().equalTo("id",billStringEntry.getId()).findFirst();
//                        if (billStringRealmResults != null) {
//                            billStringRealmResults.setSum(sum);
//                            billStringRealmResults.setQuantity(finalQuantity);
//                        }
                        billStringEntry.setQuantity(finalQuantity);
                        billStringEntry.setSum(sum);
                        Bill billEntryRealmResults = realm.where(Bill.class).equalTo("id", billUid).findFirst();
                        if (billEntryRealmResults != null) {
                            billEntryRealmResults.setSum(billEntryRealmResults.getSum() - billStringEntry.getPrice());
                            billEntryRealmResults.setSumWithDiscount(billEntryRealmResults.getSumWithDiscount() - billStringEntry.getPriceWithDiscount());
                            totalForPay[0] = billEntryRealmResults.getSumWithDiscount();
                        }
                    });
//                    initRecyclerView();
                    txt_total_sum_for_pay.setText(String.format("%.2f", totalForPay[0]).replace(",","."));
                    adapterString.notifyDataSetChanged();
                }

            }
            else{
                Toast.makeText(MainActivity.this, "Alegeti pozitia!", Toast.LENGTH_SHORT).show();
            }
        });
        btn_list_bill.setOnClickListener(v -> startActivityForResult(new Intent(".ListBills"), REQUEST_ACTIVITY_LIST_BILL));
        btn_new_bill.setOnClickListener(v -> {
            billUid = null;
            initRecyclerView();
        });
        btn_asl_list.setOnClickListener(v -> {
            Intent listBill = new Intent(".Assortiment");

            listBill.putExtra("id",billUid);
            startActivityForResult(listBill, REQUEST_ACTIVITY_ASSORTIMENT);
        });
        btn_edit_item.setOnClickListener(v -> {
            if(item_newbill_clicked) {

                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_change_count_position, null);

                final AlertDialog setCount = new AlertDialog.Builder(MainActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
                setCount.setCancelable(false);
                setCount.setView(dialogView);
                setCount.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

                TextView txtTotalCount = dialogView.findViewById(R.id.et_input_count);
                TextView txtName = dialogView.findViewById(R.id.txt_name_position);
                Button btn_Cancel = dialogView.findViewById(R.id.btn_cancel);
                Button btn_ok = dialogView.findViewById(R.id.btn_ok);
                btn_ok.setText("OK");
                Button btn_clear = dialogView.findViewById(R.id.btn_changecount_clear);
                Button btn_delete = dialogView.findViewById(R.id.btn_changecount_delete);

                txtName.setText(billStringEntry.getAssortmentFullName());

                Button number_1 = dialogView.findViewById(R.id.btn_changecount_1);
                Button number_2 = dialogView.findViewById(R.id.btn_changecount_2);
                Button number_3 = dialogView.findViewById(R.id.btn_changecount_3);
                Button number_4 = dialogView.findViewById(R.id.btn_changecount_4);
                Button number_5 = dialogView.findViewById(R.id.btn_changecount_5);
                Button number_6 = dialogView.findViewById(R.id.btn_changecount_6);
                Button number_7 = dialogView.findViewById(R.id.btn_changecount_7);
                Button number_8 = dialogView.findViewById(R.id.btn_changecount_8);
                Button number_9 = dialogView.findViewById(R.id.btn_changecount_9);
                Button number_0 = dialogView.findViewById(R.id.btn_changecount_0);

                number_1.setOnClickListener(v121 -> txtTotalCount.append("1"));
                number_2.setOnClickListener(v122 -> txtTotalCount.append("2"));
                number_3.setOnClickListener(v123 -> txtTotalCount.append("3"));
                number_4.setOnClickListener(v124 -> txtTotalCount.append("4"));
                number_5.setOnClickListener(v125 -> txtTotalCount.append("5"));
                number_6.setOnClickListener(v126 -> txtTotalCount.append("6"));
                number_7.setOnClickListener(v127 -> txtTotalCount.append("7"));
                number_8.setOnClickListener(v128 -> txtTotalCount.append("8"));
                number_9.setOnClickListener(v129 -> txtTotalCount.append("9"));
                number_0.setOnClickListener(v130 -> txtTotalCount.append("0"));


                btn_delete.setOnClickListener(v131 -> { if (!txtTotalCount.getText().toString().equals("")) txtTotalCount.setText(txtTotalCount.getText().toString().substring(0, txtTotalCount.getText().toString().length() - 1)); });
                btn_clear.setOnClickListener(v132 -> txtTotalCount.setText(""));
                btn_ok.setOnClickListener(v133 -> {
                    if (!txtTotalCount.getText().toString().equals("0") && !txtTotalCount.getText().toString().equals("") && !txtTotalCount.getText().toString().equals("0.0") && !txtTotalCount.getText().toString().equals("0.00") ) {
                        double qoantity = Double.valueOf(txtTotalCount.getText().toString());

                        double sum = billStringEntry.getPrice() * qoantity;

                        mRealm.executeTransaction(realm -> {
                            billStringEntry.setSum(sum);
                            billStringEntry.setQuantity(qoantity);
                        });

                        setCount.dismiss();
                        adapterString.notifyDataSetChanged();
                    }else {
                        Toast.makeText(MainActivity.this, "Introduceti cantitatea!", Toast.LENGTH_SHORT).show();
                    }
                });

                btn_Cancel.setOnClickListener(v134 -> {
                    if_addPositionActive = false;
                    setCount.dismiss();
                });
                setCount.show();
            }
            else{
                Toast.makeText(MainActivity.this, "Alegeti pozitia!", Toast.LENGTH_SHORT).show();
            }
        });
        btn_delete_item.setOnClickListener(v -> {
            if(item_newbill_clicked) {
                mRealm.executeTransaction(realm -> {
                    BillString billStringRealmResults = realm.where(BillString.class).equalTo("billID", billUid).and().equalTo("id",billStringEntry.getId()).findFirst();
                    if (billStringRealmResults != null) {
                        billStringRealmResults.setDeleted(true);
                        billStringRealmResults.setDeletionDate(new Date().getTime());
                        billStringRealmResults.setDeleteBy(((GlobalVariables)getApplication()).getUser().getId());
                    }
                    Bill billEntryRealmResults = realm.where(Bill.class).equalTo("id", billUid).findFirst();
                    if (billEntryRealmResults != null) {
                        billEntryRealmResults.setSum(billEntryRealmResults.getSum()  - (billStringEntry.getPrice() * billStringEntry.getQuantity()));
                        billEntryRealmResults.setSumWithDiscount(billEntryRealmResults.getSumWithDiscount() - (billStringEntry.getPriceWithDiscount() * billStringEntry.getQuantity()));
                    }

                });
                item_newbill_clicked = false;
                billStringEntry = null;
                initRecyclerView();
            }
            else{
                Toast.makeText(MainActivity.this, "Alegeti pozitia!", Toast.LENGTH_SHORT).show();
            }
        });
        btn_check_price.setOnClickListener(v -> {
            if_check_priceActive = true;
            LayoutInflater inflater = MainActivity.this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_check_price, null);

            final AlertDialog checkPriceDialog = new AlertDialog.Builder(MainActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
            //checkPriceDialog.setCancelable(false);
            checkPriceDialog.setView(dialogView);
            checkPriceDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            tv_input_barcode = dialogView.findViewById(R.id.et_input_data);
            Button btn_Cancel = dialogView.findViewById(R.id.btn_cancel_check_price);
            Button btn_add = dialogView.findViewById(R.id.btn_add);
            Button btn_search = dialogView.findViewById(R.id.btn_search);
            Button btn_clear = dialogView.findViewById(R.id.btn_add_position_clear);
            Button btn_delete = dialogView.findViewById(R.id.btn_add_position_delete);
            final TextView txtName = dialogView.findViewById(R.id.txt_name_assortment);
            final TextView txtPriceWithDiscount = dialogView.findViewById(R.id.price_with_discount);
            final TextView txtPriceWithoutDiscount = dialogView.findViewById(R.id.price_without_discount);

            Button number_1 = dialogView.findViewById(R.id.btn_add_position_1);
            Button number_2 = dialogView.findViewById(R.id.btn_add_position_2);
            Button number_3 = dialogView.findViewById(R.id.btn_add_position_3);
            Button number_4 = dialogView.findViewById(R.id.btn_add_position_4);
            Button number_5 = dialogView.findViewById(R.id.btn_add_position_5);
            Button number_6 = dialogView.findViewById(R.id.btn_add_position_6);
            Button number_7 = dialogView.findViewById(R.id.btn_add_position_7);
            Button number_8 = dialogView.findViewById(R.id.btn_add_position_8);
            Button number_9 = dialogView.findViewById(R.id.btn_add_position_9);
            Button number_0 = dialogView.findViewById(R.id.btn_add_position_0);

            number_1.setOnClickListener(v135 -> tv_input_barcode.append("1"));
            number_2.setOnClickListener(v136 -> tv_input_barcode.append("2"));
            number_3.setOnClickListener(v137 -> tv_input_barcode.append("3"));
            number_4.setOnClickListener(v138 -> tv_input_barcode.append("4"));
            number_5.setOnClickListener(v139 -> tv_input_barcode.append("5"));
            number_6.setOnClickListener(v140 -> tv_input_barcode.append("6"));
            number_7.setOnClickListener(v141 -> tv_input_barcode.append("7"));
            number_8.setOnClickListener(v142 -> tv_input_barcode.append("8"));
            number_9.setOnClickListener(v143 -> tv_input_barcode.append("9"));
            number_0.setOnClickListener(v144 -> tv_input_barcode.append("0"));

            final AssortmentRealm[] assortmentEntry = new AssortmentRealm[1];
            btn_delete.setOnClickListener(v145 -> { if (!tv_input_barcode.getText().toString().equals("")) tv_input_barcode.setText(tv_input_barcode.getText().toString().substring(0, tv_input_barcode.getText().toString().length() - 1)); });
            btn_clear.setOnClickListener(v146 -> tv_input_barcode.setText(""));
            btn_search.setOnClickListener(v147 -> {
                if(tv_input_barcode.getText().toString().length() == 13 || tv_input_barcode.getText().toString().length() == 8){
                    assortmentEntry[0] = mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",tv_input_barcode.getText().toString()).findFirst();
                }
                else{
                    assortmentEntry[0] =  mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",tv_input_barcode.getText().toString()).findFirst();
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

                }
                else{
                    txtName.setText("Nu a fost gasit!");
                    Toast.makeText(MainActivity.this, "Nu a fost gasit!", Toast.LENGTH_SHORT).show();
                }
            });

            btn_Cancel.setOnClickListener(v148 -> {
                if_check_priceActive = false;
                checkPriceDialog.dismiss();
            });
            btn_add.setOnClickListener(v149 -> {
                AssortmentRealm assortmentFind = mRealm.copyFromRealm(assortmentEntry[0]);
                addAssortmentToBill(assortmentFind,tv_input_barcode.getText().toString());
                if_check_priceActive = false;
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
                        char character = (char) keyunicode;

                        switch (keycode) {
                            case KeyEvent.KEYCODE_1 : {
                                tv_input_barcode.append("1");
                                tv_input_barcode.requestFocus();
                            }break;
                            case KeyEvent.KEYCODE_2 : {
                                tv_input_barcode.append("2");
                                tv_input_barcode.requestFocus();
                            }break;
                            case KeyEvent.KEYCODE_3 : {
                                tv_input_barcode.append("3");
                                tv_input_barcode.requestFocus();
                            }break;
                            case KeyEvent.KEYCODE_4 : {
                                tv_input_barcode.append("4");
                                tv_input_barcode.requestFocus();
                            }break;
                            case KeyEvent.KEYCODE_5 : {
                                tv_input_barcode.append("5");
                                tv_input_barcode.requestFocus();
                            }break;
                            case KeyEvent.KEYCODE_6 : {
                                tv_input_barcode.append("6");
                                tv_input_barcode.requestFocus();
                            }break;
                            case KeyEvent.KEYCODE_7 : {
                                tv_input_barcode.append("7");
                                tv_input_barcode.requestFocus();
                            }break;
                            case KeyEvent.KEYCODE_8 : {
                                tv_input_barcode.append("8");
                                tv_input_barcode.requestFocus();
                            }break;
                            case KeyEvent.KEYCODE_9 : {
                                tv_input_barcode.append("9");
                                tv_input_barcode.requestFocus();
                            }break;
                            case KeyEvent.KEYCODE_0 : {
                                tv_input_barcode.append("0");
                                tv_input_barcode.requestFocus();
                            }break;
                            case KeyEvent.KEYCODE_ENTER : {
                                if(tv_input_barcode.getText().toString().length() == 13 || tv_input_barcode.getText().toString().length() == 8){
                                    assortmentEntry[0] = mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",tv_input_barcode.getText().toString()).findFirst();
                                }
                                else{
                                    assortmentEntry[0] =  mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",tv_input_barcode.getText().toString()).findFirst();
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

                                }
                                else{
                                    txtName.setText("Nu a fost gasit!");
                                    Toast.makeText(MainActivity.this, "Nu a fost gasit!", Toast.LENGTH_SHORT).show();
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
        btn_add_position.setOnClickListener(v -> {
            if_addPositionActive = true;
            LayoutInflater inflater = MainActivity.this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_add_position, null);

            final AlertDialog addPosition = new AlertDialog.Builder(MainActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
            addPosition.setCancelable(false);
            addPosition.setView(dialogView);
            addPosition.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            tv_input_barcode = dialogView.findViewById(R.id.et_input_data);
            Button btn_Cancel = dialogView.findViewById(R.id.btn_cancel);
            Button btn_ok = dialogView.findViewById(R.id.btn_ok);
            Button btn_clear = dialogView.findViewById(R.id.btn_add_position_clear);
            Button btn_delete = dialogView.findViewById(R.id.btn_add_position_delete);

            Button number_1 = dialogView.findViewById(R.id.btn_add_position_1);
            Button number_2 = dialogView.findViewById(R.id.btn_add_position_2);
            Button number_3 = dialogView.findViewById(R.id.btn_add_position_3);
            Button number_4 = dialogView.findViewById(R.id.btn_add_position_4);
            Button number_5 = dialogView.findViewById(R.id.btn_add_position_5);
            Button number_6 = dialogView.findViewById(R.id.btn_add_position_6);
            Button number_7 = dialogView.findViewById(R.id.btn_add_position_7);
            Button number_8 = dialogView.findViewById(R.id.btn_add_position_8);
            Button number_9 = dialogView.findViewById(R.id.btn_add_position_9);
            Button number_0 = dialogView.findViewById(R.id.btn_add_position_0);

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
                    addAssortmentToBill(assortmentFind,tv_input_barcode.getText().toString());
                }
                else{
                    Toast.makeText(MainActivity.this, "Nu a fost gasit!", Toast.LENGTH_SHORT).show();
                }
                if_addPositionActive = false;
                addPosition.dismiss();
            });

            btn_Cancel.setOnClickListener(v134 -> {
                if_addPositionActive = false;
                addPosition.dismiss();
            });
            addPosition.show();
            DialogInterface.OnKeyListener keylistener = (dialog, keyCode, KEvent) -> {
                int keyaction = KEvent.getAction();

                if(keyaction == KeyEvent.ACTION_DOWN)
                {
                    int keycode = KEvent.getKeyCode();
                    int keyunicode = KEvent.getUnicodeChar(KEvent.getMetaState() );
                    char character = (char) keyunicode;

                    switch (keycode) {
                        case KeyEvent.KEYCODE_1 : {
                            tv_input_barcode.append("1");
                            tv_input_barcode.requestFocus();
                        }break;
                        case KeyEvent.KEYCODE_2 : {
                            tv_input_barcode.append("2");
                            tv_input_barcode.requestFocus();
                        }break;
                        case KeyEvent.KEYCODE_3 : {
                            tv_input_barcode.append("3");
                            tv_input_barcode.requestFocus();
                        }break;
                        case KeyEvent.KEYCODE_4 : {
                            tv_input_barcode.append("4");
                            tv_input_barcode.requestFocus();
                        }break;
                        case KeyEvent.KEYCODE_5 : {
                            tv_input_barcode.append("5");
                            tv_input_barcode.requestFocus();
                        }break;
                        case KeyEvent.KEYCODE_6 : {
                            tv_input_barcode.append("6");
                            tv_input_barcode.requestFocus();
                        }break;
                        case KeyEvent.KEYCODE_7 : {
                            tv_input_barcode.append("7");
                            tv_input_barcode.requestFocus();
                        }break;
                        case KeyEvent.KEYCODE_8 : {
                            tv_input_barcode.append("8");
                            tv_input_barcode.requestFocus();
                        }break;
                        case KeyEvent.KEYCODE_9 : {
                            tv_input_barcode.append("9");
                            tv_input_barcode.requestFocus();
                        }break;
                        case KeyEvent.KEYCODE_0 : {
                            tv_input_barcode.append("0");
                            tv_input_barcode.requestFocus();
                        }break;
                        case KeyEvent.KEYCODE_ENTER : {

                            AssortmentRealm assortmentEntry;
                            if(tv_input_barcode.getText().toString().length() == 13 || tv_input_barcode.getText().toString().length() == 8){
                                assortmentEntry = mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",tv_input_barcode.getText().toString()).findFirst();
                            }
                            else{
                                assortmentEntry =  mRealm.where(AssortmentRealm.class).equalTo("barcodes.bar",tv_input_barcode.getText().toString()).findFirst();
                            }
                            if(assortmentEntry != null){
                                AssortmentRealm assortmentFind = mRealm.copyFromRealm(assortmentEntry);
                                addAssortmentToBill(assortmentFind,tv_input_barcode.getText().toString());
                            }
                            else{
                                Toast.makeText(MainActivity.this, "Nu a fost gasit!", Toast.LENGTH_SHORT).show();
                            }
                            if_addPositionActive = false;
                            addPosition.dismiss();
                        }break;
                        default:break;
                    }
                }
                return false;
            };
            addPosition.setOnKeyListener(keylistener );
        });
        //оплата - закрыть счет
        btn_payment_bill.setOnClickListener(v -> {
            sumBillToPay = Double.valueOf(txt_total_sum_for_pay.getText().toString());

            LayoutInflater inflater = MainActivity.this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_payment_bill, null);

            payment = new AlertDialog.Builder(MainActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
            payment.setCancelable(false);
            payment.setView(dialogView);
            payment.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            txt_input_sum = dialogView.findViewById(R.id.et_input_data2);
            TextView txt_total_bon = dialogView.findViewById(R.id.txt_total_payment);
            final TextView txt_incasat = dialogView.findViewById(R.id.txt_incasat_payment);
            final TextView txt_rest_de_incasat = dialogView.findViewById(R.id.txt_rest_incasat_payment);
            final TextView txt_rest = dialogView.findViewById(R.id.txt_rest_payment);

            LinearLayout LL_btn_pay  = dialogView.findViewById(R.id.LL_btn_pay);
            LinearLayout LL_payments  = dialogView.findViewById(R.id.LL_paymentMode);

            //caut daca contul a fost achitat partial, si adaug text mai jos cu ce tip de plata si ce suma
            //plus la asta daca este vreo achitare deja facuta, verific daca este necesar de imprimat bonul fiscal si daca da, filtrez tipruile de plata dupa criteriu - printFiscalReceip
            RealmResults<BillPaymentType> bill = mRealm.where(BillPaymentType.class).equalTo("billID",billUid).findAll();
            boolean printCheck = false;    // daca trebuie bon fiscal sau nu
            boolean isPaymentOnlyOne = false;   // daca exista vreo achitare

            if(!bill.isEmpty()){
                for (int i = 0; i < bill.size(); i++){
                    BillPaymentType paymentType = bill.get(i);
                    isPaymentOnlyOne = true;
                    //dupa id a achitarii contului cautam tipul de plata
                    String id = paymentType.getPaymentTypeID();
                    PaymentType payment = mRealm.where(PaymentType.class).equalTo("externalId",id).findFirst();
                    if(payment != null){
                        printCheck = payment.getPrintFiscalCheck();
                    }

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT,1);
                    LinearLayout.LayoutParams layoutParamsPayments = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,1);
                    LinearLayout LLpayment = new LinearLayout(MainActivity.this);
                    LLpayment.setOrientation(LinearLayout.VERTICAL);

                    TextView tvName = new TextView(MainActivity.this);
                    TextView tvSum = new TextView(MainActivity.this);

                    tvName.setText(paymentType.getName());
                    tvName.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    tvName.setTextSize(18);
                    tvName.setTextColor(getResources().getColor(R.color.toolbar_color));
                    tvName.setLayoutParams(layoutParams);

                    tvSum.setText(String.format("%.2f", paymentType.getSum()).replace(",","."));
                    tvSum.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    tvSum.setTextSize(18);
                    tvSum.setTextColor(getResources().getColor(R.color.toolbar_color));
                    tvSum.setLayoutParams(layoutParams);

                    LLpayment.addView(tvName,layoutParamsPayments);
                    LLpayment.addView(tvSum,layoutParamsPayments);

                    LL_payments.addView(LLpayment,layoutParams);
                    billPaymentedSum += paymentType.getSum();
                }
            }
            //daca este vreo achitare verificam cum o fost achitat ,cu bin sau fara bon
            if(isPaymentOnlyOne){
                if(printCheck){
                    //caut tipurile de plata care sunt in baza si le adaug butoane filtrind dupa
                    RealmResults<PaymentType> paymentTypesResult = mRealm.where(PaymentType.class).equalTo("printFiscalCheck",true).findAll();
                    if(!paymentTypesResult.isEmpty()){
                        for(int o = 0; o <paymentTypesResult.size(); o++){
                            PaymentType paymentType = new PaymentType();
                            paymentType.setCode(paymentTypesResult.get(o).getCode());
                            paymentType.setPredefinedIndex(paymentTypesResult.get(o).getPredefinedIndex());
                            paymentType.setPrintFiscalCheck(paymentTypesResult.get(o).getPrintFiscalCheck());
                            paymentType.setExternalId(paymentTypesResult.get(o).getExternalId());
                            paymentType.setName(paymentTypesResult.get(o).getName());

                            myButton = new Button(MainActivity.this);
                            myButton.setText(paymentType.getName());
                            myButton.setTag(paymentType);
                            myButton.setOnClickListener(test);

                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT,1);
                            LL_btn_pay.addView(myButton, lp);
                        }
                    }
                }
                else{
                    //caut tipurile de plata care sunt in baza si le adaug butoane filtrind dupa
                    RealmResults<PaymentType> paymentTypesResult = mRealm.where(PaymentType.class).equalTo("printFiscalCheck",false).findAll();
                    if(!paymentTypesResult.isEmpty()){
                        for(int o = 0; o <paymentTypesResult.size(); o++){
                            PaymentType paymentType = new PaymentType();
                            paymentType.setCode(paymentTypesResult.get(o).getCode());
                            paymentType.setPredefinedIndex(paymentTypesResult.get(o).getPredefinedIndex());
                            paymentType.setPrintFiscalCheck(paymentTypesResult.get(o).getPrintFiscalCheck());
                            paymentType.setExternalId(paymentTypesResult.get(o).getExternalId());
                            paymentType.setName(paymentTypesResult.get(o).getName());

                            myButton = new Button(MainActivity.this);
                            myButton.setText(paymentType.getName());
                            myButton.setTag(paymentType);
                            myButton.setOnClickListener(test);

                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT,1);
                            LL_btn_pay.addView(myButton, lp);
                        }
                    }
                }
            }
            else{
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

                        myButton = new Button(MainActivity.this);
                        myButton.setText(paymentType.getName());
                        myButton.setTag(paymentType);
                        myButton.setOnClickListener(test);

                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT,1);
                        LL_btn_pay.addView(myButton, lp);
                    }
                }
            }

            Button btn_Cancel = dialogView.findViewById(R.id.btn_cancel_payment);
            Button clear = dialogView.findViewById(R.id.btn_payment_clear);
            Button delete = dialogView.findViewById(R.id.btn_payment_delete);

            txt_input_sum.setText(String.format("%.2f",sumBillToPay - billPaymentedSum).replace(",","."));
            txt_total_bon.setText(txt_total_sum_for_pay.getText().toString());
            txt_incasat.setText(txt_total_sum_for_pay.getText().toString());

            Button number_1 = dialogView.findViewById(R.id.btn_payment_1);
            Button number_2 = dialogView.findViewById(R.id.btn_payment_2);
            Button number_3 = dialogView.findViewById(R.id.btn_payment_3);
            Button number_4 = dialogView.findViewById(R.id.btn_payment_4);
            Button number_5 = dialogView.findViewById(R.id.btn_payment_5);
            Button number_6 = dialogView.findViewById(R.id.btn_payment_6);
            Button number_7 = dialogView.findViewById(R.id.btn_payment_7);
            Button number_8 = dialogView.findViewById(R.id.btn_payment_8);
            Button number_9 = dialogView.findViewById(R.id.btn_payment_9);
            Button number_0 = dialogView.findViewById(R.id.btn_payment_0);
            Button number_20 = dialogView.findViewById(R.id.btn_payment_20);
            Button number_50 = dialogView.findViewById(R.id.btn_payment_50);
            Button number_100 = dialogView.findViewById(R.id.btn_payment_100);
            Button number_200 = dialogView.findViewById(R.id.btn_payment_200);
            Button number_500 = dialogView.findViewById(R.id.btn_payment_500);
            Button number_1000 = dialogView.findViewById(R.id.btn_payment_1000);
            Button point = dialogView.findViewById(R.id.btn_payment_point);

            number_1.setOnClickListener(v1 -> txt_input_sum.append("1"));
            number_2.setOnClickListener(v12 -> txt_input_sum.append("2"));
            number_3.setOnClickListener(v13 -> txt_input_sum.append("3"));
            number_4.setOnClickListener(v14 -> txt_input_sum.append("4"));
            number_5.setOnClickListener(v15 -> txt_input_sum.append("5"));
            number_6.setOnClickListener(v16 -> txt_input_sum.append("6"));
            number_7.setOnClickListener(v17 -> txt_input_sum.append("7"));
            number_8.setOnClickListener(v18 -> txt_input_sum.append("8"));
            number_9.setOnClickListener(v19 -> txt_input_sum.append("9"));
            number_0.setOnClickListener(v110 -> txt_input_sum.append("0"));
            number_20.setOnClickListener(v111 -> txt_input_sum.setText("20"));
            number_50.setOnClickListener(v112 -> txt_input_sum.setText("50"));
            number_100.setOnClickListener(v113 -> txt_input_sum.setText("100"));
            number_200.setOnClickListener(v114 -> txt_input_sum.setText("200"));
            number_500.setOnClickListener(v115 -> txt_input_sum.setText("500"));
            number_1000.setOnClickListener(v116 -> txt_input_sum.setText("1000"));

            txt_input_sum.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(txt_input_sum.getText().toString().equals("")){
                        txt_incasat.setText(String.format("%.2f", billPaymentedSum).replace(",","."));
                        txt_rest_de_incasat.setText( String.format("%.2f", sumBillToPay - billPaymentedSum).replace(",","."));
                        txt_rest.setText("0.00");
                    }
                    else{
                        double incasat = 0.0;
                        try{
                            incasat = Double.valueOf(txt_input_sum.getText().toString());
                        }catch (Exception e){
                            incasat = Double.valueOf(txt_input_sum.getText().toString().replace(",","."));
                        }

                        txt_incasat.setText(String.format("%.2f", incasat + billPaymentedSum).replace(",","."));
                        double restIncasat = 0.0;
                        double rest = 0.0;

                        if( (incasat +billPaymentedSum) <= sumBillToPay){
                            txt_rest.setText("0.00");
                            restIncasat = sumBillToPay - (incasat + billPaymentedSum);
                            txt_rest_de_incasat.setText( String.format("%.2f", restIncasat).replace(",","."));
                        }else{
                            txt_rest_de_incasat.setText("0.00");
                            rest = (incasat + billPaymentedSum) - sumBillToPay;
                            txt_rest.setText( String.format("%.2f", rest).replace(",","."));
                        }
                    }

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            point.setOnClickListener(v117 -> {
                String test = txt_input_sum.getText().toString();
                boolean contains = false;
                for (int i = 0; i < test.length(); i++) {
                    String chars = String.valueOf(test.charAt(i));
                    if (chars.equals(".")) {
                        contains = true;
                    }
                }
                if (!contains) {
                    txt_input_sum.append(".");
                }
            });

            delete.setOnClickListener(v118 -> { if (!txt_input_sum.getText().toString().equals("")) txt_input_sum.setText(txt_input_sum.getText().toString().substring(0, txt_input_sum.getText().toString().length() - 1)); });
            clear.setOnClickListener(v119 -> txt_input_sum.setText(""));
            btn_Cancel.setOnClickListener(v120 -> { payment.dismiss(); billPaymentedSum = 0; });

            payment.show();

            payment.getWindow().setLayout(770,730);

        });

        // NFC settings
        readFromIntent(getIntent());

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };
    }
    View.OnClickListener test = new View.OnClickListener() {
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
                    .equalTo("billID", billUid)
                    .and()
                    .equalTo("isDeleted",false)
                    .findAll();
            if (!billStringsResult.isEmpty()) {
                billStrings.addAll(billStringsResult);
            }

            //tipurile de achitare deja facute la cont in caz ca nu a fost achitat integral
            RealmResults<BillPaymentType> billPayResult = mRealm.where(BillPaymentType.class)
                    .equalTo("billID", billUid).findAll();
            if(!billPayResult.isEmpty()){
                billPaymentTypes.addAll(billPayResult);
            }

            double inputSum = 0;
            try {
                inputSum = Double.valueOf(txt_input_sum.getText().toString());
            } catch (Exception e) {
                inputSum = Double.valueOf(txt_input_sum.getText().toString().replace(",", "."));
            }

            if ((billPaymentedSum + inputSum) >= sumBillToPay) {
                int workFisc = getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getInt("ModeFiscalWork",0);

                if (printFiscalCheck) {
                    if(workFisc == 1){
                        DatecsFiscalDevice fiscalDevice = null;
                        if( ((GlobalVariables) getApplication()).getMyFiscalDevice() != null){
                            fiscalDevice = ((GlobalVariables) getApplication()).getMyFiscalDevice();
                        }
                        if(fiscalDevice != null && fiscalDevice.isConnectedDeviceV2()){
                            resultCloseReceip = ((GlobalVariables) getApplication()).printFiscalReceipt(fiscalReceipt, billStrings, paymentType, inputSum, billPaymentTypes,(shiftEntry.getBillCounter() + 1));
                            if (resultCloseReceip != 0) {
                                BillPaymentType billPaymentType= new BillPaymentType();
                                billPaymentType.setId(UUID.randomUUID().toString());
                                billPaymentType.setBillID(billUid);
                                billPaymentType.setName(paymentType.getName());
                                billPaymentType.setPaymentCode(Integer.valueOf(code));
                                billPaymentType.setPaymentTypeID(paymentType.getExternalId());
                                billPaymentType.setSum(inputSum);
                                billPaymentType.setAuthor(((GlobalVariables) getApplication()).getUser().getId());
                                billPaymentType.setCreateDate(new Date().getTime());

                                int finalResultCloseReceip = resultCloseReceip;
                                mRealm.executeTransaction(realm ->{
                                    Bill bill = realm.where(Bill.class).equalTo("id",billUid).findFirst();
                                    if(bill != null){
                                        bill.setReceiptNumFiscalMemory(finalResultCloseReceip);
                                        bill.setState(1);
                                        bill.setCloseDate(new Date().getTime());
                                        bill.setClosedBy(((GlobalVariables) getApplication()).getUser().getId());
                                        bill.getBillPaymentTypes().add(billPaymentType);

                                    }
                                });

                                tv_primit_lei.setText(String.format("%.2f", inputSum + billPaymentedSum).replace(",", "."));
                                tv_rest_lei.setText(String.format("%.2f", (inputSum + billPaymentedSum)- sumBillToPay).replace(",", "."));

                                initRecyclerView();

                                billPaymentedSum = 0;
                                payment.dismiss();

                                billUid = null;
                            }
                        }
                        else{
                            Toast.makeText(MainActivity.this, "Aparatul fiscal nu este conectat!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(workFisc == 2){
                       ((GlobalVariables) getApplication()).printReceiptFiscalService(billStrings, paymentType, inputSum, billPaymentTypes);

                        BillPaymentType billPaymentType= new BillPaymentType();
                        billPaymentType.setId(UUID.randomUUID().toString());
                        billPaymentType.setBillID(billUid);
                        billPaymentType.setName(paymentType.getName());
                        billPaymentType.setPaymentCode(Integer.valueOf(code));
                        billPaymentType.setPaymentTypeID(paymentType.getExternalId());
                        billPaymentType.setSum(inputSum);
                        billPaymentType.setAuthor(((GlobalVariables) getApplication()).getUser().getId());
                        billPaymentType.setCreateDate(new Date().getTime());

                        mRealm.executeTransaction(realm ->{
                            Bill bill = realm.where(Bill.class).equalTo("id",billUid).findFirst();
                            if(bill != null){
                                bill.setReceiptNumFiscalMemory(0);
                                bill.setState(1);
                                bill.setCloseDate(new Date().getTime());
                                bill.setClosedBy(((GlobalVariables) getApplication()).getUser().getId());
                                bill.getBillPaymentTypes().add(billPaymentType);

                            }
                        });

                        tv_primit_lei.setText(String.format("%.2f", inputSum + billPaymentedSum).replace(",", "."));
                        tv_rest_lei.setText(String.format("%.2f", (inputSum + billPaymentedSum)- sumBillToPay).replace(",", "."));

                        initRecyclerView();

                        billPaymentedSum = 0;
                        payment.dismiss();

                        billUid = null;
                    }

                }
                else {
                    BillPaymentType billPaymentType= new BillPaymentType();
                    billPaymentType.setId(UUID.randomUUID().toString());
                    billPaymentType.setBillID(billUid);
                    billPaymentType.setName(paymentType.getName());
                    billPaymentType.setPaymentCode(Integer.valueOf(code));
                    billPaymentType.setPaymentTypeID(paymentType.getExternalId());
                    billPaymentType.setSum(sumBillToPay - billPaymentedSum);
                    billPaymentType.setAuthor(((GlobalVariables) getApplication()).getUser().getId());
                    billPaymentType.setCreateDate(new Date().getTime());

                    mRealm.executeTransaction(realm ->{
                        Bill bill = realm.where(Bill.class).equalTo("id",billUid).findFirst();
                        if(bill != null){
                            bill.setReceiptNumFiscalMemory(0);
                            bill.setState(1);
                            bill.setCloseDate(new Date().getTime());
                            bill.setClosedBy(((GlobalVariables) getApplication()).getUser().getId());
                            bill.getBillPaymentTypes().add(billPaymentType);
                        }
                    });

                    tv_primit_lei.setText(String.format("%.2f", inputSum + billPaymentedSum).replace(",", "."));
                    tv_rest_lei.setText(String.format("%.2f", (inputSum +billPaymentedSum ) - sumBillToPay).replace(",", "."));
                    initRecyclerView();

                    billPaymentedSum = 0;
                    payment.dismiss();
                    billUid = null;
                }
            }
            else if ((billPaymentedSum + inputSum) < sumBillToPay) {

                if (printFiscalCheck ){

                }

                BillPaymentType billPaymentType = new BillPaymentType();
                billPaymentType.setId(UUID.randomUUID().toString());
                billPaymentType.setBillID(billUid);
                billPaymentType.setName(paymentType.getName());
                billPaymentType.setPaymentCode(Integer.valueOf(code));
                billPaymentType.setPaymentTypeID(paymentType.getExternalId());
                billPaymentType.setSum(inputSum);
                billPaymentType.setAuthor(((GlobalVariables) getApplication()).getUser().getId());
                billPaymentType.setCreateDate(new Date().getTime());
                //TODO add billPayment type to bill
                mRealm.executeTransaction(realm ->{
                    Bill bill = realm.where(Bill.class).equalTo("id",billUid).findFirst();
                    if(bill != null){
                        bill.setState(0);
                        bill.getBillPaymentTypes().add(billPaymentType);
                    }
                });
                tv_primit_lei.setText(String.format("%.2f", inputSum + billPaymentedSum).replace(",", "."));

                payment.dismiss();
                billPaymentedSum = 0;
            }
        }
    };
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

    @Override
    protected void onResume() {
        super.onResume();

        isFiscalPrinter = getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getInt("ModeFiscalWork", 0) == 1;

        if(getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getInt("ModeFiscalWork", 0) == 2){
            initFiscalService();
        }

        Shift result = mRealm.where(Shift.class).equalTo("closed",false).findFirst();
        if(result != null){
            shiftEntry = mRealm.copyFromRealm(result);

            boolean shiftOpened = shiftEntry.isClosed();
            long need_closeTo = shiftEntry.getNeedClose();
            long currentTime = new Date().getTime();

            if(!shiftOpened && currentTime<need_closeTo){
                tv_shift_state.setText("Открыта");
                tv_shift_state.setTextColor(getResources().getColor(R.color.blue_color));
                startTimer(need_closeTo - currentTime);

                frm_add_position.setEnabled(true);
                btn_add_position.setEnabled(true);
                btn_check_price.setEnabled(true);
                btn_new_bill.setEnabled(true);
                btn_payment_bill.setEnabled(true);

                frm_add_position.setEnabled(true);
                frm_new_bill.setEnabled(true);
                frm_check_price.setEnabled(true);
                frm_apply_disc.setEnabled(true);
                frm_check_disc.setEnabled(true);
                frm_delete_disc.setEnabled(true);
            }
            else if(!shiftOpened && currentTime>need_closeTo && need_closeTo != 0){
                cancelTimer();
                tv_shift_state.setText("Не действительна");
                tv_shift_state.setTextColor(getResources().getColor(R.color.colorAccent));
                tv_schedule_shift.setText("00:00:00");

                btn_payment_bill.setEnabled(false);
                btn_add_position.setEnabled(false);
                btn_check_price.setEnabled(false);
                btn_new_bill.setEnabled(false);

                frm_add_position.setEnabled(false);
                frm_new_bill.setEnabled(false);
                frm_check_price.setEnabled(false);
                frm_apply_disc.setEnabled(false);
                frm_check_disc.setEnabled(false);
                frm_delete_disc.setEnabled(false);
            }

        }
        else{
            cancelTimer();
            tv_shift_state.setText("Закрыта");
            tv_schedule_shift.setText("00:00:00");
            tv_shift_state.setTextColor(getResources().getColor(R.color.colorAccent));

            btn_payment_bill.setEnabled(false);
            btn_add_position.setEnabled(false);
            btn_check_price.setEnabled(false);
            btn_new_bill.setEnabled(false);

            frm_add_position.setEnabled(false);
            frm_new_bill.setEnabled(false);
            frm_check_price.setEnabled(false);
            frm_apply_disc.setEnabled(false);
            frm_check_disc.setEnabled(false);
            frm_delete_disc.setEnabled(false);
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
        }

        if(myFiscalDevice == null || !myFiscalDevice.isConnectedDeviceV2()){
            fiscal_printer.setImageDrawable(getResources().getDrawable(R.drawable.fiscal_off));
        }
    }

    private void createNewBill(String uid){
        Bill bill = new Bill();
        bill.setId(uid);
        bill.setCreateDate(new Date().getTime());
        bill.setShiftReceiptNumSoftware(shiftEntry.getBillCounter() + 1);
        bill.setAuthor(((GlobalVariables)getApplication()).getUser().getId());
        bill.setSumWithDiscount(0.0);
        bill.setSum(0.0);
        bill.setState(0);
        bill.setShiftId(shiftEntry.getId());
        bill.setSinchronized(false);
        String version ="0.0";
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        bill.setCurrentSoftwareVersion(version);
        bill.setDeviceId(getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("DeviceId",null));

        mRealm.executeTransaction(realm -> {
            Shift shift = realm.where(Shift.class).equalTo("id", shiftEntry.getId()).findFirst();
            if (shift != null) {
               shift.setBillCounter(shiftEntry.getBillCounter() + 1);
               shiftEntry.setBillCounter(shiftEntry.getBillCounter() + 1);
            }
            realm.insert(bill);
        });
    }

    private void addAssortmentToBill(AssortmentRealm assortmentEntry,String barcode){
        if(billUid == null) {
            billUid = UUID.randomUUID().toString();
            createNewBill((billUid));
        }
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

        billString.setCreateBy(((GlobalVariables)getApplication()).getUser().getId());
        billString.setAssortmentExternID(assortmentEntry.getId());
        billString.setAssortmentFullName(assortmentEntry.getName());
        billString.setBillID(billUid);
        billString.setId(UUID.randomUUID().toString());
        billString.setQuantity(1);
        billString.setPrice(assortmentEntry.getPrice());
        billString.setPriceLineID(assortmentEntry.getPriceLineId());
        if(promo !=null)
            billString.setPromoLineID(promo.getId());
        billString.setBarcode(barcode);
        billString.setVat(assortmentEntry.getVat());
        billString.setCreateDate(new Date().getTime());
        billString.setDeleted(false);
        billString.setPriceWithDiscount(priceWithDisc);
        billString.setSum(assortmentEntry.getPrice());
        billString.setSumWithDiscount(priceWithDisc);

        double finalPriceWithDisc = priceWithDisc;
        mRealm.executeTransaction(realm -> {
            Bill billEntryRealmResults = realm.where(Bill.class).equalTo("id", billUid).findFirst();
            if (billEntryRealmResults != null) {
                billEntryRealmResults.setSum(billEntryRealmResults.getSum() + assortmentEntry.getPrice());
                billEntryRealmResults.setSumWithDiscount(billEntryRealmResults.getSumWithDiscount() + finalPriceWithDisc);
                billEntryRealmResults.getBillStrings().add(billString);
            }
        });

        initRecyclerView();
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

    @Override
    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ACTIVITY_ASSORTIMENT){
            if(resultCode==RESULT_OK){
                billUid = data.getStringExtra("BillID");
                initRecyclerView();
            }
        }
        else if(requestCode == REQUEST_ACTIVITY_LIST_BILL){
            if(resultCode == RESULT_OK){
                billUid = data.getStringExtra("BillID");
                initRecyclerView();
            }
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Log.d("myLogs", "KeyEvent DOWN" + event);
        if (scaned_item)
            inpput.setText("");

        switch (event.getAction()){
            case KeyEvent.ACTION_DOWN :{
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_1 : {
                        if (!if_check_priceActive && !if_addPositionActive) {
                            scaned_item = false;
                            inpput.append("1");
                            inpput.requestFocus();
                            inpput.requestFocusFromTouch();
                        }
                    }break;
                    case KeyEvent.KEYCODE_2 : {
                        if (!if_check_priceActive && !if_addPositionActive) {
                            scaned_item=false;
                            inpput.append("2");
                            inpput.requestFocus();
                            inpput.requestFocusFromTouch();
                        }
                    }break;
                    case KeyEvent.KEYCODE_3 : {
                        if (!if_check_priceActive && !if_addPositionActive) {
                            scaned_item=false;
                            inpput.append("3");
                            inpput.requestFocus();
                            inpput.requestFocusFromTouch();
                        }
                    }break;
                    case KeyEvent.KEYCODE_4 : {
                        if (!if_check_priceActive && !if_addPositionActive) {
                            scaned_item = false;
                            inpput.append("4");
                            inpput.requestFocus();
                            inpput.requestFocusFromTouch();
                        }
                    }break;
                    case KeyEvent.KEYCODE_5 : {
                        if (!if_check_priceActive && !if_addPositionActive) {
                            scaned_item = false;
                            inpput.append("5");
                            inpput.requestFocus();
                            inpput.requestFocusFromTouch();
                        }
                    }break;
                    case KeyEvent.KEYCODE_6 : {
                        if (!if_check_priceActive && !if_addPositionActive) {
                            scaned_item = false;
                            inpput.append("6");
                            inpput.requestFocus();
                            inpput.requestFocusFromTouch();
                        }
                    }break;
                    case KeyEvent.KEYCODE_7 : {
                        if (!if_check_priceActive && !if_addPositionActive) {
                            scaned_item = false;
                            inpput.append("7");
                            inpput.requestFocus();
                            inpput.requestFocusFromTouch();
                        }
                    }break;
                    case KeyEvent.KEYCODE_8 : {
                        if (!if_check_priceActive && !if_addPositionActive) {
                            scaned_item = false;
                            inpput.append("8");
                            inpput.requestFocus();
                            inpput.requestFocusFromTouch();
                        }
                    }break;
                    case KeyEvent.KEYCODE_9 : {
                        if (!if_check_priceActive && !if_addPositionActive) {
                            scaned_item = false;
                            inpput.append("9");
                            inpput.requestFocus();
                            inpput.requestFocusFromTouch();
                        }
                    }break;
                    case KeyEvent.KEYCODE_0 : {
                        if (!if_check_priceActive && !if_addPositionActive) {
                            scaned_item = false;
                            inpput.append("0");
                            inpput.requestFocus();
                            inpput.requestFocusFromTouch();
                        }
                    }break;
                    default:break;
                }
            }break;
            default:break;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View mDecorView = getWindow().getDecorView();
            mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
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
                        Log.d("NFC", "MifareUltralight " + sb.toString());
                        //TODO if ultralight



                        byte[] id = tagFromIntent.getId();
                        Log.d("NFC", "MifareUltralight Reverse " + toReversedHex(id));

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
                        Log.d("NFC", "MifareClassic " + sb.toString());
//                        if(frame_card.getVisibility() == View.VISIBLE){
//                            if(sb.toString().equals("48197191098409899100101102103104105")){
//                                Intent main = new Intent(StartedActivity.this,MainActivity.class);
//                                startActivity(main);
//                                finish();
//                            }
//                        }

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

    private void initQuickButtons(){
        mRealm.executeTransaction(realm -> {
            RealmResults<QuickGroupRealm> result = realm.where(QuickGroupRealm.class).findAll();
            if(!result.isEmpty()) {
                for (int i = 0; i < result.size(); i++){
                    QuickGroupRealm quickGroupRealm = realm.copyFromRealm(result.get(i));
                    List<String> string = quickGroupRealm.getAssortmentId();
                    tab.addTab(tab.newTab().setText("" + quickGroupRealm.getGroupName()));
                }
            }
        });
        for ( int i = 0; i < 5 ;i++){
            tab.addTab(tab.newTab().setText("Test " + i));
        }
        adapter = new TabAdapter(getSupportFragmentManager(), tab.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tab));
    }

    private void initUIElements(){
        btn_list_bill=findViewById(R.id.button_list_bills);
        tv_schedule_shift=findViewById(R.id.txtTimeShiftClosed);
        btn_asl_list=findViewById(R.id.button_open_assortment);
        btn_up_count=findViewById(R.id.btn_up_count_item_newbill);
        btn_down_count=findViewById(R.id.btn_down_count_item_newbill);
        txt_total_sum_for_pay = findViewById(R.id.txtTotalSum_for_Pay);
        btn_edit_item=findViewById(R.id.btn_edit_item_new_bill);
        btn_delete_item = findViewById(R.id.btn_delete_item_new_bill);

//        gridLayout_quickButtons = findViewById(R.id.grid_quick_buttons);
//        gridLayout_quickButtons.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        btn_new_bill = findViewById(R.id.button_new_bill);
        btn_check_price = findViewById(R.id.button_check_price);
        btn_add_position = findViewById(R.id.button_add_posotion);
        btn_payment_bill = findViewById(R.id.btn_payment_bill);
        fiscal_printer = findViewById(R.id.txt_fiscal_device_state);
        tv_shift_state = findViewById(R.id.txt_shift_state);
        LW_NewBill = findViewById(R.id.LW_NewBill);
//        tv_name_magazin = findViewById(R.id.txt_name_magazine);

        tv_primit_lei = findViewById(R.id.tv_primit_lei);
        tv_rest_lei = findViewById(R.id.tv_rest_lei);
        tv_reducere_lei = findViewById(R.id.tv_reducere_lei);

        //dynamic tabs
        viewPager =  findViewById(R.id.viewPager);
        tab = findViewById(R.id.tabLayout);

        frm_add_position = findViewById(R.id.btn_add_position);
        frm_new_bill = findViewById(R.id.btn_new_bill);
        frm_check_price = findViewById(R.id.btn_check_price);
        frm_check_disc = findViewById(R.id.btn_check_disc);
        frm_apply_disc = findViewById(R.id.btn_apply_disc);
        frm_delete_disc = findViewById(R.id.btn_delete_disc);
        frm_listBills = findViewById(R.id.btn_list_bill);
        frm_ListAssortment = findViewById(R.id.btn_assortiment_for_sales);

//        tv_name_magazin.setText(getSharedPreferences(SharedPrefWorkPlaceSettings,MODE_PRIVATE).getString("WorkPlaceName",""));

    }

    private void initFiscalService(){
        String ip = getSharedPreferences(SharedPrefFiscalService, MODE_PRIVATE).getString("IpAdressFiscalService",null);
        String port = getSharedPreferences(SharedPrefFiscalService, MODE_PRIVATE).getString("PortFiscalService",null);

        if(ip != null && port != null){
            String uri = ip + ":" + port;

            GetStateFiscalService getStateFiscalService = ApiUtils.getStateFiscalService(uri);

            Call<SimpleResult> call = getStateFiscalService.getState();
            call.enqueue(new Callback<SimpleResult>() {
                @Override
                public void onResponse(Call<SimpleResult> call, Response<SimpleResult> response) {
                    SimpleResult result = response.body();
                    if(result != null){
                        int errorCode = result.getErrorCode();
                        String errorMsg = result.getErrorMessage();

                        if(errorCode == 0){
                            fiscal_printer.setImageDrawable(getResources().getDrawable(R.drawable.fiscal_on));
                        }
                    }
                }

                @Override
                public void onFailure(Call<SimpleResult> call, Throwable t) {
                    String msg = t.getMessage();
                }
            });
        }
    }

    private void initRecyclerView(){
        final RealmResults<BillString>[] results = new RealmResults[]{null};
        final Bill[] bill = new Bill[1];
        mRealm.executeTransaction(realm -> {
                    Shift result = mRealm.where(Shift.class).equalTo("closed", false).findFirst();
                    if (result != null) {
                        shiftEntry = mRealm.copyFromRealm(result);
                        bill[0] = mRealm.where(Bill.class).equalTo("shiftId", shiftEntry.getId()).and().equalTo("state", 0).and().equalTo("id",billUid).findFirst();
                        if (bill[0] != null) {
                            billUid = bill[0].getId();
                            results[0] = mRealm.where(BillString.class).equalTo("billID", billUid).and().equalTo("isDeleted", false).sort("createDate").findAll();

                        }
                    }
                });
        if(results[0] != null){
            adapterString = new CustomNewBillRealmAdapter(results[0]);
            txt_total_sum_for_pay.setText(String.format("%.2f", bill[0].getSumWithDiscount()).replace(",","."));
        }
        else{
            adapterString = new CustomNewBillRealmAdapter(results[0]);
            txt_total_sum_for_pay.setText("0.0");
        }
        LW_NewBill.setAdapter(adapterString);
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
            android.app.AlertDialog.Builder bytes = new android.app.AlertDialog.Builder(MainActivity.this);
            bytes.setTitle("Log buffer");
            bytes.setMessage("SB: " + sb.toString() + "\nBuferAray: " + bufferString + "\nreversedHex " + toReversedHex(buffer) + "\ntoHexStrng: " + toHexString(buffer));
            bytes.show();
        }
    }

    //start timer function
    void startTimer(long time) {
        cTimer = new CountDownTimer(time, 1000) {
            public void onTick(long millisUntilFinished) {
                long second = (millisUntilFinished / 1000) % 60;
                long minute = (millisUntilFinished / (1000 * 60)) % 60;
                long hour = (millisUntilFinished / (1000 * 60 * 60)) % 24;

                String time = String.format("%02d:%02d:%02d", hour, minute, second);
                tv_schedule_shift.setText(time);

                if(millisUntilFinished == 600000){
                   //TODO dialog what shift is finis
                }

            }
            public void onFinish() {
            }
        };
        cTimer.start();
    }
    //cancel timer
    void cancelTimer() {
        if(cTimer != null){
            cTimer.onFinish();
            cTimer.cancel();
        }

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

            } else {

                postToast("Reader name: " + mReader.getReaderName());

                int numSlots = mReader.getNumSlots();
//                    postToast("Number of slots: " + numSlots);

                // Add slot items
                mSlotAdapter.clear();
                for (int i = 0; i < numSlots; i++) {
                    mSlotAdapter.add(Integer.toString(i));
                }
            }
        }
    }

    private class CloseTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            mReader.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

        }

    }

    private class PowerParams {

        public int slotNum;
        public int action;
    }

    private class PowerResult {

        public byte[] atr;
        public Exception e;
    }

    private class PowerTask extends AsyncTask<PowerParams, Void, PowerResult> {
        @Override
        protected PowerResult doInBackground(PowerParams... params) {
            PowerResult result = new PowerResult();
            try {
                result.atr = mReader.power(params[0].slotNum, params[0].action);
            } catch (Exception e) {
                result.e = e;
            }
            return result;
        }

        @Override
        protected void onPostExecute(PowerResult result) {
            if (result.e != null) {
                postToast(result.e.toString());
            } else {
                // Show ATR
                if (result.atr != null) {
                    logBuffer(result.atr, result.atr.length);

                } else {
                    postToast("ATR: None");
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
                        postToast("Connection error: " + e.getMessage());
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
                        postToast("Error: " + e.getMessage());
                        return;
                    }
                } finally {
                    ((GlobalVariables)getApplication()).setMyFiscalDevice(PrinterManager.instance.getFiscalDevice());
                    myFiscalDevice = PrinterManager.instance.getFiscalDevice();

                    if(myFiscalDevice != null && myFiscalDevice.isConnectedDeviceV2()){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fiscal_printer.setImageDrawable(getResources().getDrawable(R.drawable.fiscal_on));
                            }
                        });
                    }


                }
            }
        });
        thread.start();
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

    private void postToast(final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}