package edi.md.androidcash;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import edi.md.androidcash.NetworkUtils.AssortmentServiceEntry;
import edi.md.androidcash.NetworkUtils.EposResult.AssortmentListService;
import edi.md.androidcash.NetworkUtils.EposResult.GetAssortmentListResult;
import edi.md.androidcash.NetworkUtils.Promotion;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.ApiUtils;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.CommandServices;
import edi.md.androidcash.RealmHelper.AssortmentRealm;
import edi.md.androidcash.RealmHelper.Barcodes;
import edi.md.androidcash.RealmHelper.Bill;
import edi.md.androidcash.RealmHelper.Shift;
import edi.md.androidcash.adapters.CustomAssortmentFolderRealmAdapter;
import edi.md.androidcash.adapters.CustomAssortmentRealmAdapter;
import edi.md.androidcash.adapters.CustomSwipedAdapterAssortmentItem;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static edi.md.androidcash.BaseApplication.SharedPrefSettings;
import static edi.md.androidcash.BaseApplication.SharedPrefWorkPlaceSettings;

public class AssortmentActivity extends AppCompatActivity {
    ListView lw_folders_assortment;
    RecyclerView LW_Assortiment_items;
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
    CustomSwipedAdapterAssortmentItem adapterAssortmentItem;
    private ArrayList<AssortmentRealm> imageModelArrayList;

    private Paint p = new Paint();

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

//        LW_Assortiment_items.setOnItemClickListener((parent, view, position, id) -> {
//            item_clicked = true;
//            assortmentRealm = assortmentRealmAdapter.getItem(position);
//        });

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

//                        addAssortmentToBill(assortmentRealm,Double.valueOf(txtTotalCount.getText().toString()), barcode);
                        MainActivity.addAssortmentToBill(assortmentRealm,Double.valueOf(txtTotalCount.getText().toString()), barcode,false);
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

                txtNames.setText(assortmentRealm.getName());

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
                        if(assortmentRealm.getBarcodes() != null && assortmentRealm.getBarcodes().first() != null && !assortmentRealm.getBarcodes().isEmpty())
                            barcode = assortmentRealm.getBarcodes().first().getBar();

                        MainActivity.addAssortmentToBill(assortmentRealm,Double.valueOf(txtTotalCount.getText().toString()), barcode,false);
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


        enableSwipe();
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
            String uri = getSharedPreferences("Settings",MODE_PRIVATE).getString("URI",null);
            CommandServices commandServices = ApiUtils.commandEposService(uri);

            final Call<AssortmentListService> assortiment = commandServices.getAssortiment(token, workplaceId);
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
                setResult(RESULT_OK);
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

        adapterAssortmentItem = new CustomSwipedAdapterAssortmentItem(resultsNoFolders[0],false);

        lw_folders_assortment.setAdapter(folderRealmAdapter);

        LW_Assortiment_items.setAdapter(adapterAssortmentItem);

    }

    private void enableSwipe(){
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (direction == ItemTouchHelper.RIGHT){
                    AssortmentRealm item = adapterAssortmentItem.getItem(position);
                    MainActivity.addAssortmentToBill(item,1,"swipe",false);
                    // showing snack bar with Undo option
                    Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), item.getName() + " adaugat!", Snackbar.LENGTH_LONG);
                    snackbar.setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // undo is selected, restore the deleted item
                            MainActivity.deleteItemFromBill(item,false);
                        }
                    });
                    snackbar.setActionTextColor(Color.YELLOW);
                    snackbar.show();

                    adapterAssortmentItem.notifyDataSetChanged();

                }
            }
            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                Bitmap icon;
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;
                    if(dX > 0){

                        p.setColor(Color.parseColor("#388E3C"));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());
                        c.drawRect(background,p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.add_black_36dp);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width ,(float) itemView.getTop() + width,(float) itemView.getLeft()+ 2*width,(float)itemView.getBottom() - width);
                        c.drawBitmap(icon,null,icon_dest,p);

                    }
//                    else {
//                        p.setColor(Color.parseColor("#D32F2F"));
//                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
//                        c.drawRect(background,p);
//                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.add_black_36dp);
//                        RectF icon_dest = new RectF((float) itemView.getRight() - 2*width ,(float) itemView.getTop() + width,(float) itemView.getRight() - width,(float)itemView.getBottom() - width);
//                        c.drawBitmap(icon,null,icon_dest,p);
//                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(LW_Assortiment_items);

        adapterAssortmentItem.notifyDataSetChanged();
    }
}
