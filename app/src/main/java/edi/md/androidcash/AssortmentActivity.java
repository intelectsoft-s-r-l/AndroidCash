package edi.md.androidcash;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import edi.md.androidcash.NetworkUtils.AssortmentServiceEntry;
import edi.md.androidcash.NetworkUtils.EposResult.AssortmentListService;
import edi.md.androidcash.NetworkUtils.EposResult.GetAssortmentListResult;
import edi.md.androidcash.NetworkUtils.Promotion;
import edi.md.androidcash.NetworkUtils.ApiUtils;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.GetAssortmentListService;
import edi.md.androidcash.RealmHelper.AssortmentRealm;
import edi.md.androidcash.RealmHelper.Barcodes;
import edi.md.androidcash.RealmHelper.Bill;
import edi.md.androidcash.RealmHelper.BillString;
import edi.md.androidcash.RealmHelper.Shift;
import edi.md.androidcash.adapters.CustomAssortmentFolderRealmAdapter;
import edi.md.androidcash.adapters.CustomAssortmentRealmAdapter;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static edi.md.androidcash.GlobalVariables.SharedPrefSettings;
import static edi.md.androidcash.GlobalVariables.SharedPrefWorkPlaceSettings;

public class AssortmentActivity extends AppCompatActivity {
    ListView LW_Assortiment_items, lw_folders_assortment;
    AlertDialog setCount ;
    Button btn_add_close,btn_add,btn_refresh;
    ImageButton btn_home_assortment;
    LinearLayout layout_buttons;

    private ProgressDialog pgH;
    String guidItem = "00000000-0000-0000-0000-000000000000",name_clicked_item_assortment, billID = null;
    String token, workplaceId, uri;
    private int MESSAGE_SUCCES = 0,MESSAGE_ERROR = 1,MESSAGE_FAILURE = 2;

    boolean item_clicked= false;
    boolean shiftIsActive = false;

    JSONArray added_items_clicked;

    Shift shiftEntry;

    //Realm data bases
    private Realm mRealm;
    AssortmentRealm assortmentRealm;

    CustomAssortmentFolderRealmAdapter folderRealmAdapter;
    CustomAssortmentRealmAdapter assortmentRealmAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_assortiment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_ASL);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        LW_Assortiment_items=findViewById(R.id.LW_content_assortment_entry);
        lw_folders_assortment = findViewById(R.id.list_assortment_folder);
        btn_add_close=findViewById(R.id.btn_add_close_assortiment);
        btn_add =findViewById(R.id.btn_add_assortiment);
        btn_refresh = findViewById(R.id.button_refresh_assortment);
        pgH = new ProgressDialog(this);

        btn_home_assortment =findViewById(R.id.img_btn_home_assortment);
        layout_buttons = findViewById(R.id.LL_butons_ierarhii);

        mRealm = Realm.getDefaultInstance();

        added_items_clicked = new JSONArray();
        billID = getIntent().getStringExtra("id");

        Shift results = mRealm.where(Shift.class).equalTo("closed",false).findFirst();
        if(results != null) {
            shiftEntry = mRealm.copyFromRealm(results);
            long start = new Date().getTime();
            long neeedClose = shiftEntry.getNeedClose();
            if(start < neeedClose){
                shiftIsActive = true;
            }
        }

        showAssortmentList();

        LW_Assortiment_items.setOnItemClickListener((parent, view, position, id) -> {
            item_clicked = true;
            assortmentRealm = assortmentRealmAdapter.getItem(position);
        });
        lw_folders_assortment.setOnItemClickListener((parent, view, position, id) -> {
            AssortmentRealm assortmentRealm = folderRealmAdapter.getItem(position);
            guidItem = assortmentRealm.getId();

            Button button = new Button(AssortmentActivity.this);
            button.setText(assortmentRealm.getName());
            button.setTag(assortmentRealm);
            button.setOnClickListener(butons_);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            layout_buttons.addView(button, lp);

            showAssortmentList();
        });

        btn_add_close.setOnClickListener(v -> {
            if (item_clicked && shiftIsActive){
                LayoutInflater inflater = AssortmentActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_add_position_assortment_activity, null);

                setCount = new AlertDialog.Builder(AssortmentActivity.this).create();
                setCount.setCancelable(false);
                setCount.setView(dialogView);
                Button btnOK = dialogView.findViewById(R.id.btn_ok);
                Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
                final TextView txtTotalCount = dialogView.findViewById(R.id.et_input_assortment_count);
                final TextView txtNames = dialogView.findViewById(R.id.txt_name_assortment);

                txtNames.setText(name_clicked_item_assortment);

                TextView txt1 = dialogView.findViewById(R.id.btn_add_item_1);
                TextView txt2 = dialogView.findViewById(R.id.btn_add_item_2);
                TextView txt3 = dialogView.findViewById(R.id.btn_add_item_3);
                TextView txt4 = dialogView.findViewById(R.id.btn_add_item_4);
                TextView txt5 = dialogView.findViewById(R.id.btn_add_item_5);
                TextView txt6 = dialogView.findViewById(R.id.btn_add_item_6);
                TextView txt7 = dialogView.findViewById(R.id.btn_add_item_7);
                TextView txt8 = dialogView.findViewById(R.id.btn_add_item_8);
                TextView txt9 = dialogView.findViewById(R.id.btn_add_item_9);
                TextView txt0 = dialogView.findViewById(R.id.btn_add_item_0);
                TextView txt_point = dialogView.findViewById(R.id.btn_add_item_point);
                TextView txt_deleteALL = dialogView.findViewById(R.id.btn_add_item_clear);

                btnOK.setOnClickListener(v115 -> {
                    if (!txtTotalCount.getText().toString().equals("0") && !txtTotalCount.getText().toString().equals("") && !txtTotalCount.getText().toString().equals("0.0") && !txtTotalCount.getText().toString().equals("0.00") ) {
                        String barcode = null;
                        if(assortmentRealm.getBarcodes() != null && assortmentRealm.getBarcodes().first() != null)
                            barcode = assortmentRealm.getBarcodes().first().getBar();

                        addAssortmentToBill(assortmentRealm,Double.valueOf(txtTotalCount.getText().toString()), barcode);
                        setCount.dismiss();
                        Intent result = new Intent();
                        result.putExtra("BillID",billID);
                        setResult(RESULT_OK,result);
                        finish();
                    }else {
                        Toast.makeText(AssortmentActivity.this, "Introduceti cantitatea!", Toast.LENGTH_SHORT).show();
                    }

                });
                btnCancel.setOnClickListener(v114 -> setCount.dismiss());
                txt0.setOnClickListener(v113 -> {
                    String senf = txtTotalCount.getText().toString() + "0";
                    double nolevoi = Double.valueOf(senf);
                    if (nolevoi != 0.0)
                        txtTotalCount.append("0");
                });
                txt1.setOnClickListener(v19 -> txtTotalCount.append("1"));
                txt2.setOnClickListener(v18 -> txtTotalCount.append("2"));
                txt3.setOnClickListener(v17 -> txtTotalCount.append("3"));
                txt4.setOnClickListener(v16 -> txtTotalCount.append("4"));
                txt5.setOnClickListener(v15 -> txtTotalCount.append("5"));
                txt6.setOnClickListener(v14 -> txtTotalCount.append("6"));
                txt7.setOnClickListener(v13 -> txtTotalCount.append("7"));
                txt8.setOnClickListener(v1 -> txtTotalCount.append("8"));
                txt9.setOnClickListener(v12 -> txtTotalCount.append("9"));
                txt_point.setOnClickListener(v111 -> {
                    String test = txtTotalCount.getText().toString();
                    boolean contains = false;
                    for (int i = 0; i < test.length(); i++) {
                        String chars = String.valueOf(test.charAt(i));
                        if (chars.equals(".")) {
                            contains = true;
                        }
                    }
                    if (!contains) {
                        txtTotalCount.append(".");
                    }

                });
                txt_deleteALL.setOnClickListener(v112 -> txtTotalCount.setText(""));
                setCount.show();
            }else{
                Toast.makeText(AssortmentActivity.this, "Alegeti pozitia sau tura nu este activa!", Toast.LENGTH_SHORT).show();
            }
        });
        btn_add.setOnClickListener(v -> {
            if (item_clicked && shiftIsActive){
                LayoutInflater inflater = AssortmentActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_add_position_assortment_activity, null);

                setCount = new AlertDialog.Builder(AssortmentActivity.this).create();
                setCount.setCancelable(false);
                setCount.setView(dialogView);
                Button btnOK = dialogView.findViewById(R.id.btn_ok);
                Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
                final TextView txtTotalCount = dialogView.findViewById(R.id.et_input_assortment_count);
                final TextView txtNames = dialogView.findViewById(R.id.txt_name_assortment);

                txtNames.setText(name_clicked_item_assortment);

                TextView txt1 = dialogView.findViewById(R.id.btn_add_item_1);
                TextView txt2 = dialogView.findViewById(R.id.btn_add_item_2);
                TextView txt3 = dialogView.findViewById(R.id.btn_add_item_3);
                TextView txt4 = dialogView.findViewById(R.id.btn_add_item_4);
                TextView txt5 = dialogView.findViewById(R.id.btn_add_item_5);
                TextView txt6 = dialogView.findViewById(R.id.btn_add_item_6);
                TextView txt7 = dialogView.findViewById(R.id.btn_add_item_7);
                TextView txt8 = dialogView.findViewById(R.id.btn_add_item_8);
                TextView txt9 = dialogView.findViewById(R.id.btn_add_item_9);
                TextView txt0 = dialogView.findViewById(R.id.btn_add_item_0);
                TextView txt_point = dialogView.findViewById(R.id.btn_add_item_point);
                TextView txt_deleteALL = dialogView.findViewById(R.id.btn_add_item_clear);

                btnOK.setOnClickListener(v116 -> {
                    if (!txtTotalCount.getText().toString().equals("0") && !txtTotalCount.getText().toString().equals("") && !txtTotalCount.getText().toString().equals("0.0") && !txtTotalCount.getText().toString().equals("0.00") ) {
                        String barcode = null;
                        if(assortmentRealm.getBarcodes() != null && assortmentRealm.getBarcodes().first() != null)
                            barcode = assortmentRealm.getBarcodes().first().getBar();

                        addAssortmentToBill(assortmentRealm,Double.valueOf(txtTotalCount.getText().toString()),barcode);
                        setCount.dismiss();
                    }else {
                        Toast.makeText(AssortmentActivity.this, "Introduceti cantitatea!", Toast.LENGTH_SHORT).show();
                    }
                });
                btnCancel.setOnClickListener(v117 -> setCount.dismiss());
                txt0.setOnClickListener(v118 -> {
                    String senf = txtTotalCount.getText().toString() + "0";
                    double nolevoi = Double.valueOf(senf);
                    if (nolevoi != 0)
                        txtTotalCount.append("0");
                });
                txt1.setOnClickListener(v119 -> txtTotalCount.append("1"));
                txt2.setOnClickListener(v120 -> txtTotalCount.append("2"));
                txt3.setOnClickListener(v121 -> txtTotalCount.append("3"));
                txt4.setOnClickListener(v122 -> txtTotalCount.append("4"));
                txt5.setOnClickListener(v123 -> txtTotalCount.append("5"));
                txt6.setOnClickListener(v124 -> txtTotalCount.append("6"));
                txt7.setOnClickListener(v125 -> txtTotalCount.append("7"));
                txt8.setOnClickListener(v126 -> txtTotalCount.append("8"));
                txt9.setOnClickListener(v127 -> txtTotalCount.append("9"));

                txt_point.setOnClickListener(v129 -> {
                    String test = txtTotalCount.getText().toString();
                    boolean contains = false;
                    for (int i = 0; i < test.length(); i++) {
                        String chars = String.valueOf(test.charAt(i));
                        if (chars.equals(".")) {
                            contains = true;
                        }
                    }
                    if (!contains) {
                        txtTotalCount.append(".");
                    }
                });
                txt_deleteALL.setOnClickListener(v130 -> txtTotalCount.setText(""));
                setCount.show();
            }else{
                Toast.makeText(AssortmentActivity.this, "Alegeti pozitia sau tura nu este activa!", Toast.LENGTH_SHORT).show();
            }
        });
        btn_home_assortment.setOnClickListener(v -> {
            ViewGroup parent  = (ViewGroup) v.getParent();
            int count = parent.getChildCount();

            for (int i=count-1; i>0; i--){
                    parent.removeViewAt(i);
            }
            guidItem = "00000000-0000-0000-0000-000000000000";
            showAssortmentList();
        });
        btn_refresh.setOnClickListener(v -> {
            RealmResults<Bill> billEntryResult = mRealm.where(Bill.class)
                    .equalTo("shiftId",shiftEntry.getId())
                    .and()
                    .equalTo("state",0)
                    .findAll();
            if(!billEntryResult.isEmpty()){
                LayoutInflater inflater = this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.dialog_refresh_assortment, null);

                final AlertDialog exitApp = new AlertDialog.Builder(this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
                exitApp.setCancelable(false);
                exitApp.setView(dialogView);

                Button btn_ok = dialogView.findViewById(R.id.btn_understand);
                TextView text_msg = dialogView.findViewById(R.id.text_message);

                text_msg.setText("Нельзя обновить список товаров пока есть открытые счета!\nУ вас осталось " + billEntryResult.size() + " открытых счетов.");

                btn_ok.setOnClickListener(v131 -> exitApp.dismiss());

                exitApp.show();
            }
            else{
                mRealm.executeTransaction(realm -> {
                    realm.delete(Promotion.class);
                    realm.delete(Barcodes.class);
                    realm.delete(AssortmentRealm.class);
                });
                uri = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("URI",null);
                workplaceId = getSharedPreferences(SharedPrefWorkPlaceSettings, MODE_PRIVATE).getString("WorkPlaceID", "null");
                token = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("InstallationID","null");
                new AssortmentTask().execute();
            }
        });
    }

    View.OnClickListener butons_ = v -> {
        AssortmentRealm assortmentEntry = (AssortmentRealm)v.getTag();
        ViewGroup parent  = (ViewGroup) v.getParent();
        int count = parent.getChildCount();

        for (int i=count-1; i>0; i--){
            Button vi = (Button) parent.getChildAt(i);
            AssortmentRealm entry = (AssortmentRealm)vi.getTag();
            if(!entry.getId().equals(assortmentEntry.getId())){
                parent.removeViewAt(i);
            }
            else if(entry.getId().equals(assortmentEntry.getId())){
                break;
            }
        }
        guidItem = assortmentEntry.getId();
        showAssortmentList();
    };

    private void createNewBill(String uid){
        Bill bill = new Bill();
        bill.setId(uid);
        bill.setShiftReceiptNumSoftware(shiftEntry.getBillCounter() + 1);
        bill.setCreateDate(new Date().getTime());
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

    private void addAssortmentToBill(AssortmentRealm assortmentEntry,double count, String barcode){
        if(billID == null) {
            billID = UUID.randomUUID().toString();
            createNewBill((billID));
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

        billString.setAssortmentExternID(assortmentEntry.getId());
        billString.setAssortmentFullName(assortmentEntry.getName());
        billString.setBillID(billID);
        billString.setId(UUID.randomUUID().toString());
        billString.setQuantity(count);
        billString.setPrice(assortmentEntry.getPrice());
        billString.setPriceLineID(assortmentEntry.getPriceLineId());
        billString.setVat(assortmentEntry.getVat());
        billString.setCreateDate(new Date().getTime());
        billString.setDeleted(false);
        billString.setPriceWithDiscount(priceWithDisc);
        billString.setSum(priceWithDisc * count);
        if(promo !=null)
            billString.setPromoLineID(promo.getId());
        billString.setBarcode(barcode);

        double finalPriceWithDisc = priceWithDisc;
        mRealm.executeTransaction(realm -> {
            Bill billEntryRealmResults = realm.where(Bill.class).equalTo("id", billID).findFirst();
            if (billEntryRealmResults != null) {
                billEntryRealmResults.setSum(billEntryRealmResults.getSum() + (assortmentEntry.getPrice() * count));
                billEntryRealmResults.setSumWithDiscount(billEntryRealmResults.getSumWithDiscount() + (finalPriceWithDisc * count));
                billEntryRealmResults.getBillStrings().add(billString);
            }
        });
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
                    List<AssortmentServiceEntry> assortmentListData= result.getAssortments();

                    mRealm.executeTransaction(realm -> {
                        for(AssortmentServiceEntry assortmentServiceEntry: assortmentListData){
                            AssortmentRealm ass = new AssortmentRealm();

                            RealmList<Barcodes> listBarcode = new RealmList<>();
                            RealmList<Promotion> listPromotion = new RealmList<>();

                            for(String barcodes : assortmentServiceEntry.getBarcodes()){
                                Barcodes barcodes1 = new Barcodes();
                                barcodes1.setBar(barcodes);
                                listBarcode.add(barcodes1);
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
                    });
                    mHandlerBills.obtainMessage(MESSAGE_SUCCES).sendToTarget();

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

    class AssortmentTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pgH.setMessage("Actualizarea assortimentului...");
            pgH.setIndeterminate(true);
            pgH.show();
        }

        @Override
        protected Void doInBackground(Void... dates) {
            GetAssortmentListService assortiment_API = ApiUtils.getAssortmentListService(AssortmentActivity.this);

            final Call<AssortmentListService> assortiment = assortiment_API.getAssortiment(token, workplaceId);
            readAssortment(assortiment);
            return null;
        }
    }

    private final Handler mHandlerBills = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            pgH.dismiss();
            if (msg.what == MESSAGE_SUCCES) {
                mRealm.close();
                AlertDialog.Builder failureAsl = new AlertDialog.Builder(AssortmentActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme);
                failureAsl.setTitle("Atentie!");
                failureAsl.setMessage("Assortimentul a fost actualizat");
                failureAsl.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showAssortmentList();
                    }
                });
                failureAsl.show();
            }
            else if(msg.what == MESSAGE_ERROR) {
                AlertDialog.Builder failureAsl = new AlertDialog.Builder(AssortmentActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme);
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
                AlertDialog.Builder failureAsl = new AlertDialog.Builder(AssortmentActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme);
                failureAsl.setCancelable(false);
                failureAsl.setTitle("Atentie eroare!");
                failureAsl.setMessage("Eroare la descarcarea asortimentului.Mesajul erorii: "+ msg.obj.toString()+"\n" +"Incercati din nou!");
                failureAsl.setPositiveButton("Da", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new AssortmentTask().execute();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home : {

                if(billID != null ){
                    if(!billID.equals("null")){
                        Intent result = new Intent();
                        result.putExtra("BillID",billID);
                        setResult(RESULT_OK,result);
                        finish();
                    }

                }
                setResult(RESULT_CANCELED);
                finish();
            }break;
        }
        return true;
    }
    private void showAssortmentList(){
        final RealmResults<AssortmentRealm>[] results = new RealmResults[]{null};
        final RealmResults<AssortmentRealm>[] resultsNoFolders = new RealmResults[]{null};
        mRealm.executeTransaction(realm -> {
            results[0] = realm.where(AssortmentRealm.class)
                    .equalTo("parentID",guidItem)
                    .and()
                    .equalTo("isFolder",true)
                    .sort("name")
                    .findAll();
            resultsNoFolders[0] = realm.where(AssortmentRealm.class)
                    .equalTo("parentID",guidItem)
                    .and()
                    .equalTo("isFolder",false)
                    .sort("name")
                    .findAll();

        });
        folderRealmAdapter = new CustomAssortmentFolderRealmAdapter(results[0]);
        assortmentRealmAdapter = new CustomAssortmentRealmAdapter(resultsNoFolders[0]);

        lw_folders_assortment.setAdapter(folderRealmAdapter);
        LW_Assortiment_items.setAdapter(assortmentRealmAdapter);
    }
//    private void show() {
//        SQLiteDatabase db = marketDbHelper.getWritableDatabase();
//        db.isOpen();
//        String[] projection = {COLUMN_ASL_FullName , COLUMN_ASL_ParentUid , COLUMN_ASL_Folder , COLUMN_ASL_ExternalID , COLUMN_ASL_Guid,COLUMN_ASL_Code,COLUMN_ASL_Barcode,COLUMN_ASL_Price,COLUMN_ASL_PriceLineID,COLUMN_ASL_VAT};
//        Cursor cursor = db.query(TABLE_ASSORTIMENT,                      // таблица
//                projection,                         // столбцы
//                COLUMN_ASL_ParentUid + " =?",                  // столбцы для условия WHERE
//                new String[]{guid},                  // значения для условия WHERE
//                null,                  // Don't group the rows
//                null,                  // Don't filter by row groups
//                null);
//        if (cursor.moveToFirst()) {
//            int nameColumnIndex = cursor.getColumnIndex(COLUMN_ASL_FullName);
//            int folderColumnIndex = cursor.getColumnIndex(COLUMN_ASL_Folder);
//            int UidColumnIndex = cursor.getColumnIndex(COLUMN_ASL_Guid);
//            int externalGuidColumnIndex = cursor.getColumnIndex(COLUMN_ASL_ExternalID);
//
//            int CodedColumnIndex = cursor.getColumnIndex(COLUMN_ASL_Code);
//            int BarcodeColumnIndex = cursor.getColumnIndex(COLUMN_ASL_Barcode);
//            int PriceColumnIndex = cursor.getColumnIndex(COLUMN_ASL_Price);
//            int VatColumnIndex = cursor.getColumnIndex(COLUMN_ASL_VAT);
//            int PriceLineIdColumnIndex = cursor.getColumnIndex(COLUMN_ASL_PriceLineID);
//
//            do {
//               m = new HashMap<>();
//                String currentName = cursor.getString(nameColumnIndex);
//                String currentExternalGuid = cursor.getString(externalGuidColumnIndex);
//                String currentFolder = cursor.getString(folderColumnIndex);
//                String currentUid = cursor.getString(UidColumnIndex);
//
//                String currentCode = cursor.getString(CodedColumnIndex);
//                String currentBarcode = cursor.getString(BarcodeColumnIndex);
//                String currentPrice = cursor.getString(PriceColumnIndex);
//                String currentPriceLineID = cursor.getString(PriceLineIdColumnIndex);
//                String currentVAt = cursor.getString(VatColumnIndex);
//
//                if (currentFolder.equals("true")) {
//                    m.put("name", currentName);
//                    m.put("uid", currentUid);
//                    m.put("externGuid", currentExternalGuid);
//                    m.put("folder",currentFolder );
//                    groupData.add(m);
//
//                }
//                else{
//                    HashMap<String, Object> asl_ = new HashMap<>();
//                    asl_.put("Name", currentName);
//                    asl_.put("Price",currentPrice );
//                    asl_.put("Barcode",currentBarcode);
//                    asl_.put("Vat",currentVAt);
//                    asl_.put("PriceLineId",currentPriceLineID);
//                    asl_.put("ID",currentUid);
//                    asl_list_in_bill.add(asl_);
//
//                }
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//    }
//    private TreeNode.TreeNodeClickListener nodeClickListener = new TreeNode.TreeNodeClickListener() {
//            @Override
//            public void onClick(TreeNode node, Object value) {
//                IconTreeItemHolder.IconTreeItem item = (IconTreeItemHolder.IconTreeItem) value;
//                if(node.size()==0) {
//                    asl_list_in_bill.clear();
//                    groupData.clear();
//                    item_clicked=false;
//                    getFolderItemsClick(node, item.GUID);
//                    if(asl_list_in_bill.size()>0){
//                        LastFolder_clicked.setText(item.text);
//                    }
//                }else{
//                    asl_list_in_bill.clear();
//                    item_clicked=false;
////                    getItemsClick(item.GUID);
////                    if(asl_list_in_bill.size()>0){
////                        LastFolder_clicked.setText(item.text);
////                    }else{
////                        LastFolder_clicked.setText("");
////                    }
//                }
//            }
//        };
//
//    private TreeNode.TreeNodeLongClickListener nodeLongClickListener = new TreeNode.TreeNodeLongClickListener() {
//            @Override
//            public boolean onLongClick(TreeNode node, Object value) {
//                IconTreeItemHolder.IconTreeItem item = (IconTreeItemHolder.IconTreeItem) value;
//                Toast.makeText(Assortiment.this, "Long click: " + item.text, Toast.LENGTH_SHORT).show();
//
//                return true;
//            }
//        };

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//            super.onSaveInstanceState(outState);
//            outState.putString("tState", tView.getSaveState());
//        }
//    private void getFolderItemsClick (TreeNode node,String Guid){
//            SQLiteDatabase db = marketDbHelper.getWritableDatabase();
//            db.isOpen();
//            String[] projection = {COLUMN_ASL_FullName , COLUMN_ASL_ParentUid , COLUMN_ASL_Folder , COLUMN_ASL_ExternalID , COLUMN_ASL_Guid,COLUMN_ASL_Code,COLUMN_ASL_Barcode,COLUMN_ASL_Price};
//            Cursor cursor = db.query(TABLE_ASSORTIMENT,                      // таблица
//                    projection,                         // столбцы
//                    COLUMN_ASL_ParentUid + " =?",                  // столбцы для условия WHERE
//                    new String[]{Guid},                  // значения для условия WHERE
//                    null,                  // Don't group the rows
//                    null,                  // Don't filter by row groups
//                    null);
//            if (cursor.moveToFirst()) {
//                int nameColumnIndex = cursor.getColumnIndex(COLUMN_ASL_FullName);
//                int folderColumnIndex = cursor.getColumnIndex(COLUMN_ASL_Folder);
//                int UidColumnIndex = cursor.getColumnIndex(COLUMN_ASL_Guid);
//                int parentColumnIndex = cursor.getColumnIndex(COLUMN_ASL_ParentUid);
//                int externalGuidColumnIndex = cursor.getColumnIndex(COLUMN_ASL_ExternalID);
//
//                int CodedColumnIndex = cursor.getColumnIndex(COLUMN_ASL_Code);
//                int BarcodeColumnIndex = cursor.getColumnIndex(COLUMN_ASL_Barcode);
//                int PriceColumnIndex = cursor.getColumnIndex(COLUMN_ASL_Price);
//                do {
//                    m = new HashMap<>();
//                    String currentName = cursor.getString(nameColumnIndex);
//                    String currentExternalGuid = cursor.getString(externalGuidColumnIndex);
//                    String currentFolder = cursor.getString(folderColumnIndex);
//                    String currentUid = cursor.getString(UidColumnIndex);
//                    String currentParentUid = cursor.getString(parentColumnIndex);
//
//                    String currentCode = cursor.getString(CodedColumnIndex);
//                    String currentBarcode = cursor.getString(BarcodeColumnIndex);
//                    String currentPrice = cursor.getString(PriceColumnIndex);
//
//                    if (currentFolder.equals("true")) {
//                        m.put("name", currentName);
//                        m.put("uid", currentUid);
//                        m.put("externGuid", currentExternalGuid);
//                        m.put("folder",currentFolder );
//                        m.put("parentUid",currentParentUid);
//                        groupData.add(m);
//                    }
//                    else{
//                        HashMap<String, Object> asl_ = new HashMap<>();
//                        asl_.put("Name", currentName);
//                        asl_.put("Price",currentPrice );
//                        asl_.put("Barcode",currentBarcode);
//                        asl_.put("ID",currentUid);
//                        asl_.put("ExternID",currentExternalGuid);
//                        asl_list_in_bill.add(asl_);
//                    }
//                } while (cursor.moveToNext());
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    groupData.sort(new Comparator<HashMap<String, Object>>() {
//                        @Override
//                        public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {
//                            return o1.get("name").toString().compareTo(o2.get("name").toString());
//                        }
//                    });
//                }
//                if(groupData.size()>0) {
//                    for (int i = 0; i < groupData.size(); i++) {
//                        TreeNode file1 = new TreeNode(new IconTreeItemHolder.IconTreeItem(R.string.ic_folder, (String) groupData.get(i).get("name"), (String) groupData.get(i).get("uid")));
//                        node.addChildren(file1);
//                    }
//                }else{
//                    node.setExpanded(true);
//                }
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        asl_list_in_bill.sort(new Comparator<HashMap<String, Object>>() {
//                            @Override
//                            public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {
//                                return o1.get("Name").toString().compareTo(o2.get("Name").toString());
//                            }
//                        });
//                    }
//                    LW_Assortiment.setAdapter(adapterContent);
//            }
//            cursor.close();
//
//        }
//    private void getItemsClick (String Guid){
//        SQLiteDatabase db = marketDbHelper.getWritableDatabase();
//        db.isOpen();
//        String[] projection = {COLUMN_ASL_FullName , COLUMN_ASL_ParentUid , COLUMN_ASL_Folder , COLUMN_ASL_ExternalID , COLUMN_ASL_Guid,COLUMN_ASL_Code,COLUMN_ASL_Barcode,COLUMN_ASL_Price};
//        Cursor cursor = db.query(TABLE_ASSORTIMENT,                      // таблица
//                projection,                         // столбцы
//                COLUMN_ASL_ParentUid + " =?",                  // столбцы для условия WHERE
//                new String[]{Guid},                  // значения для условия WHERE
//                null,                  // Don't group the rows
//                null,                  // Don't filter by row groups
//                null);
//        if (cursor.moveToFirst()) {
//            int nameColumnIndex = cursor.getColumnIndex(COLUMN_ASL_FullName);
//            int folderColumnIndex = cursor.getColumnIndex(COLUMN_ASL_Folder);
//            int UidColumnIndex = cursor.getColumnIndex(COLUMN_ASL_Guid);
//            int parentColumnIndex = cursor.getColumnIndex(COLUMN_ASL_ParentUid);
//            int externalGuidColumnIndex = cursor.getColumnIndex(COLUMN_ASL_ExternalID);
//
//            int CodedColumnIndex = cursor.getColumnIndex(COLUMN_ASL_Code);
//            int BarcodeColumnIndex = cursor.getColumnIndex(COLUMN_ASL_Barcode);
//            int PriceColumnIndex = cursor.getColumnIndex(COLUMN_ASL_Price);
//            do {
//                m = new HashMap<>();
//                String currentName = cursor.getString(nameColumnIndex);
//                String currentExternalGuid = cursor.getString(externalGuidColumnIndex);
//                String currentFolder = cursor.getString(folderColumnIndex);
//                String currentUid = cursor.getString(UidColumnIndex);
//
//                String currentCode = cursor.getString(CodedColumnIndex);
//                String currentBarcode = cursor.getString(BarcodeColumnIndex);
//                String currentPrice = cursor.getString(PriceColumnIndex);
//
//                if (!currentFolder.equals("true")) {
//                    HashMap<String, Object> asl_ = new HashMap<>();
//                    asl_.put("Code", currentCode);
//                    asl_.put("Name", currentName);
//                    asl_.put("Price",currentPrice );
//                    asl_.put("Barcode",currentBarcode);
//                    asl_.put("ID",currentUid);
//                    asl_.put("ExternID",currentExternalGuid);
//                    asl_list_in_bill.add(asl_);
//                }
//            } while (cursor.moveToNext());
//
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    asl_list_in_bill.sort(new Comparator<HashMap<String, Object>>() {
//                        @Override
//                        public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {
//                            return o1.get("Name").toString().compareTo(o2.get("Name").toString());
//                        }
//                    });
//                }
//                LW_Assortiment.setAdapter(adapterContent);
//        }
//        cursor.close();
//
//    }
}
