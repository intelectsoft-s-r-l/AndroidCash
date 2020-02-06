package edi.md.androidcash;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;

import com.datecs.fiscalprinter.SDK.model.DatecsFiscalDevice;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReceipt;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import edi.md.androidcash.NetworkUtils.PaymentType;
import edi.md.androidcash.RealmHelper.Bill;
import edi.md.androidcash.RealmHelper.BillPaymentType;
import edi.md.androidcash.RealmHelper.BillString;
import edi.md.androidcash.RealmHelper.Shift;
import edi.md.androidcash.adapters.CustomBillRealmAdapter;
import edi.md.androidcash.adapters.CustomBillStringRealmAdapter;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class ListBills extends AppCompatActivity {

    ListView LW_Right_BillStrings,LW_Left_ListBills;
    Button btn_filter,btn_new_bill,btn_edit_bill,btn_close_bill,btn_delete;
    Button myButton;
    TextView txt_input_sum;

    String BillID,billStringID;
    boolean bill_selected=false;
    boolean billString_selected = false;

    double billPaymentedSum = 0;
    double sumBillToPay = 0;

    AlertDialog payment;

    Bill billEntry;
    Shift shiftEntry;

    CustomBillRealmAdapter adapterBillList;
    CustomBillStringRealmAdapter billStringRealmAdapter;

    BillPaymentType billPaymentTypeEntry;

    public DatecsFiscalDevice myFiscalDevice = null;
    private cmdReceipt.FiscalReceipt fiscalReceipt;

    //realm data bases
    private Realm mRealm;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home : {
                finish();
            }break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_list_bills);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_list_bill);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        LW_Left_ListBills = findViewById(R.id.LWLeft);
        LW_Right_BillStrings = findViewById(R.id.LWRight);
        btn_delete = findViewById(R.id.btn_delete_bill);
        btn_filter = findViewById(R.id.btn_filter_bills);
        btn_new_bill = findViewById(R.id.btn_new_bill);
        btn_edit_bill = findViewById(R.id.btn_edit_bill);
        btn_close_bill = findViewById(R.id.btn_close_bill);

        mRealm = Realm.getDefaultInstance();

        showBillList();

        LW_Left_ListBills.setOnItemClickListener((parent, view, position, id) -> {
            bill_selected = true;
            billString_selected = false;
            billEntry = adapterBillList.getItem(position);
            BillID = billEntry.getId();
            showBillString();
        });
        LW_Right_BillStrings.setOnItemClickListener(((parent, view, position, id) -> {
            billString_selected = true;
            billStringID = billStringRealmAdapter.getItem(position).getId();
        }));

        btn_delete.setOnClickListener(v -> {
            if(billString_selected){
                final double[] sumString = {0};
                mRealm.executeTransaction(realm -> {
                    BillString billString = realm.where(BillString.class).equalTo("id",billStringID).findFirst();
                    if(billString != null){
                        billString.setDeleted(true);
                        billString.setDeleteBy(((BaseApplication)getApplication()).getUser().getId());
                        sumString[0] = billString.getSum();
                    }

                    Bill bills = realm.where(Bill.class).equalTo("id",BillID).findFirst();
                    if(bills != null){
                        bills.setSum(bills.getSum() - sumString[0]);
                        bills.setSumWithDiscount(bills.getSumWithDiscount() - sumString[0]);
                        bills.setLastEditAuthor(((BaseApplication)getApplication()).getUser().getId());
                    }
                });
                showBillString();
            }
            else if(bill_selected){
                int states = billEntry.getState();
                if(states == 0){
                    mRealm.executeTransaction(realm -> {
                        Bill bill = realm.where(Bill.class).equalTo("id",BillID).findFirst();
                        if(bill != null){
                            bill.setState(2);

                        }
                        RealmResults<BillString> results = realm.where(BillString.class).equalTo("billID",BillID).findAll();
                        results.setValue("isDeleted",true);
                    });
                    showBillList();
                }
                else{
                    Toast.makeText(this, "Contul este inchis sau sters", Toast.LENGTH_SHORT).show();
                }

            }
        });
        btn_filter.setOnClickListener(v -> showPopup(v));
        btn_new_bill.setOnClickListener(v -> finish());
        btn_edit_bill.setOnClickListener(v -> {
            if(bill_selected){
                int state = billEntry.getState();
                if(BillID != null && state == 0){
                    setResult(RESULT_OK,new Intent().putExtra("BillID",BillID));
                    finish();
                }
                else{
                    Toast.makeText(this, "Alegeti contul!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_close_bill.setOnClickListener(v -> {
            if(bill_selected){
                sumBillToPay = billEntry.getSumWithDiscount();
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_payment_bill, null);

                payment = new AlertDialog.Builder(ListBills.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
                payment.setCancelable(false);
                payment.setView(dialogView);
                payment.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//                String.format("%.2f", priceTotal).replace(",",".")

                txt_input_sum = dialogView.findViewById(R.id.et_input_data2);

                TextView txt_total_bon = dialogView.findViewById(R.id.txt_total_payment);
                final TextView txt_incasat = dialogView.findViewById(R.id.txt_incasat_payment);
                final TextView txt_rest_de_incasat = dialogView.findViewById(R.id.txt_rest_incasat_payment);
                final TextView txt_rest = dialogView.findViewById(R.id.txt_rest_payment);

                LinearLayout LL_btn_pay  = dialogView.findViewById(R.id.LL_btn_pay);
                LinearLayout LL_payments  = dialogView.findViewById(R.id.LL_paymentMode);

                //caut daca contul a fost achitat partial, si adaug text mai jos cu ce tip de plata si ce suma
                //plus la asta daca este vreo achitare deja facuta, verific daca este necesar de imprimat bonul fiscal si daca da, filtrez tipruile de plata dupa criteriu - printFiscalReceip
                RealmResults<BillPaymentType> bill = mRealm.where(BillPaymentType.class).equalTo("billID",BillID).findAll();
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

                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT,1);
                        LinearLayout.LayoutParams layoutParamsPayments = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,1);
                        LinearLayout LLpayment = new LinearLayout(ListBills.this);
                        LLpayment.setOrientation(LinearLayout.VERTICAL);

                        TextView tvName = new TextView(ListBills.this);
                        TextView tvSum = new TextView(ListBills.this);

                        tvName.setText(paymentType.getName());
                        tvName.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                        tvName.setTextSize(18);
                        tvName.setTextColor(getResources().getColor(R.color.toolbar_color));

                        tvSum.setText(String.format("%.2f", paymentType.getSum()).replace(",","."));
                        tvSum.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                        tvSum.setTextSize(18);
                        tvSum.setTextColor(getResources().getColor(R.color.toolbar_color));

                        LLpayment.addView(tvName,layoutParamsPayments);
                        LLpayment.addView(tvSum,layoutParamsPayments);

                        LL_payments.addView(LLpayment,lp);
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

                                myButton = new Button(ListBills.this);
                                myButton.setText(paymentType.getName());
                                myButton.setTag(paymentType);
                                myButton.setOnClickListener(closeBill);

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

                                myButton = new Button(ListBills.this);
                                myButton.setText(paymentType.getName());
                                myButton.setTag(paymentType);
                                myButton.setOnClickListener(closeBill);

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

                            myButton = new Button(ListBills.this);
                            myButton.setText(paymentType.getName());
                            myButton.setTag(paymentType);
                            myButton.setOnClickListener(closeBill);

                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT,1);
                            LL_btn_pay.addView(myButton, lp);
                        }
                    }
                }

                Button btn_Cancel = dialogView.findViewById(R.id.btn_cancel_payment);
                Button clear = dialogView.findViewById(R.id.btn_payment_clear);
                Button delete = dialogView.findViewById(R.id.btn_payment_delete);

                txt_input_sum.setText(String.format("%.2f",sumBillToPay - billPaymentedSum).replace(",","."));
                txt_total_bon.setText(String.format("%.2f",sumBillToPay).replace(",","."));
                txt_incasat.setText(String.format("%.2f",sumBillToPay).replace(",","."));

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

                point.setOnClickListener(v120 -> {
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

                delete.setOnClickListener(v119 -> { if (!txt_input_sum.getText().toString().equals("")) txt_input_sum.setText(txt_input_sum.getText().toString().substring(0, txt_input_sum.getText().toString().length() - 1)); });

                clear.setOnClickListener(v118 -> txt_input_sum.setText(""));

                btn_Cancel.setOnClickListener(v117 -> { payment.dismiss(); billPaymentedSum = 0;
                });

                payment.show();

                payment.getWindow().setLayout(770,730);

            }
            else{
                Toast.makeText(this, "Alegeti contul!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    View.OnClickListener closeBill = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            fiscalReceipt = new cmdReceipt.FiscalReceipt();
            myFiscalDevice = ((BaseApplication) getApplication()).getMyFiscalDevice();

            PaymentType paymentType = (PaymentType) v.getTag();
            boolean printFiscalCheck = paymentType.getPrintFiscalCheck();
            String code = paymentType.getCode();
            if (code == null)
                code = "404";
            int resultCloseReceip = 0;
            RealmList<BillString> billStrings = new RealmList<>();
            RealmList<BillPaymentType> billPaymentTypes = new RealmList<>();

            RealmResults<BillString> billStringsResult = mRealm.where(BillString.class).equalTo("id", BillID).findAll();
            if (!billStringsResult.isEmpty()) {
                billStrings.addAll(billStringsResult);
            }

            //tipurile de achitare deja facute la cont in caz ca nu a fost achitat integral
            RealmResults<BillPaymentType> billPayResult = mRealm.where(BillPaymentType.class)
                    .equalTo("billID", BillID).findAll();
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
                DatecsFiscalDevice fiscalDevice = null;
                if( ((BaseApplication) getApplication()).getMyFiscalDevice() != null){
                    fiscalDevice = ((BaseApplication) getApplication()).getMyFiscalDevice();
                }

                if (printFiscalCheck) {
                    if(fiscalDevice != null && fiscalDevice.isConnectedDeviceV2()){
                        resultCloseReceip = ((BaseApplication) getApplication()).printFiscalReceipt(fiscalReceipt, billStrings, paymentType, inputSum, billPaymentTypes,billEntry.getShiftReceiptNumSoftware());
                        if (resultCloseReceip != 0) {
                            BillPaymentType billPaymentType = new BillPaymentType();
                            billPaymentType.setId(UUID.randomUUID().toString());
                            billPaymentType.setBillID(BillID);
                            billPaymentType.setName(paymentType.getName());
                            billPaymentType.setPaymentCode(Integer.valueOf(code));
                            billPaymentType.setPaymentTypeID(paymentType.getExternalId());
                            billPaymentType.setSum(sumBillToPay - billPaymentedSum);
                            billPaymentType.setAuthor(((BaseApplication) getApplication()).getUser().getId());
                            billPaymentType.setCreateDate(new Date().getTime());

                            int finalResultCloseReceip = resultCloseReceip;
                            mRealm.executeTransaction(realm -> {
                                Bill bill = realm.where(Bill.class).equalTo("id", BillID).findFirst();
                                if (bill != null) {
                                    bill.setReceiptNumFiscalMemory(finalResultCloseReceip);
                                    bill.setState(1);
                                    bill.setCloseDate(new Date().getTime());
                                    bill.setClosedBy(((BaseApplication) getApplication()).getUser().getId());
                                    bill.getBillPaymentTypes().add(billPaymentType);
                                }
                            });
                            BillID = null;
                            showBillList();
                            showBillString();

                            billPaymentedSum = 0;
                            payment.dismiss();
                        }
                    }
                    else{
                        Toast.makeText(ListBills.this, "Aparatul fiscal nu este conectat!", Toast.LENGTH_SHORT).show();
                    }

                } else {

                    BillPaymentType billPaymentType = new BillPaymentType();
                    billPaymentType.setId(UUID.randomUUID().toString());
                    billPaymentType.setBillID(BillID);
                    billPaymentType.setName(paymentType.getName());
                    billPaymentType.setPaymentCode(Integer.valueOf(code));
                    billPaymentType.setPaymentTypeID(paymentType.getExternalId());
                    billPaymentType.setSum(sumBillToPay - billPaymentedSum);
                    billPaymentType.setAuthor(((BaseApplication) getApplication()).getUser().getId());
                    billPaymentType.setCreateDate(new Date().getTime());

                    mRealm.executeTransaction(realm -> {
                        Bill bill = realm.where(Bill.class).equalTo("id", BillID).findFirst();
                        if (bill != null) {
                            bill.setReceiptNumFiscalMemory(0);
                            bill.setState(1);
                            bill.setCloseDate(new Date().getTime());
                            bill.setClosedBy(((BaseApplication) getApplication()).getUser().getId());
                            bill.getBillPaymentTypes().add(billPaymentType);
                        }
                    });
                    BillID = null;
                    showBillList();
                    showBillString();


                    billPaymentedSum = 0;
                    payment.dismiss();
                }
            } else if ((billPaymentedSum + inputSum) < sumBillToPay) {

                BillPaymentType billPaymentType = new BillPaymentType();
                billPaymentType.setId(UUID.randomUUID().toString());
                billPaymentType.setBillID(BillID);
                billPaymentType.setName(paymentType.getName());
                billPaymentType.setPaymentCode(Integer.valueOf(code));
                billPaymentType.setPaymentTypeID(paymentType.getExternalId());
                billPaymentType.setSum(inputSum);
                billPaymentType.setAuthor(((BaseApplication) getApplication()).getUser().getId());
                billPaymentType.setCreateDate(new Date().getTime());

                mRealm.executeTransaction(realm -> {
                    Bill bill = realm.where(Bill.class).equalTo("id", BillID).findFirst();
                    if (bill != null) {
                        bill.setState(0);
                        bill.getBillPaymentTypes().add(billPaymentType);
                    }
                });
                showBillList();
                showBillString();

                payment.dismiss();
                billPaymentedSum = 0;
            }
        }
    };

    private void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        // Inflate the menu from xml
        popup.getMenuInflater().inflate(R.menu.popup_filter_list_bills, popup.getMenu());
        // Setup menu item selection
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.filter_today:{
                        showBillListFilterToday();
                        return true;
                    }
                    case R.id.filter_this_week: {
                        showBillListFilterThisWeek();
                        return true;
                    }
                    case R.id.filter_this_month:
                        showBillListFilterThisMonth();
                        return true;
                    case R.id.filter_custom:
                        Toast.makeText(ListBills.this, "filter_custom", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.filter_reset:{
                        showBillList();
                        return true;
                    }

                    default:
                        return false;
                }
            }
        });
        // Handle dismissal with: popup.setOnDismissListener(...);
        // Show the menu
        popup.show();
    }

    private void showBillList(){
        bill_selected = false;
        final RealmResults<Bill>[] results = new RealmResults[]{null};
        mRealm.executeTransaction(realm -> {
                    Shift result = mRealm.where(Shift.class).equalTo("closed", false).findFirst();
                    if (result != null) {
                        shiftEntry = mRealm.copyFromRealm(result);
                        results[0] = mRealm.where(Bill.class).equalTo("shiftId", shiftEntry.getId()).and().equalTo("state", 0).findAll();
                    }
                });
        adapterBillList = new CustomBillRealmAdapter(results[0]);

        LW_Left_ListBills.setAdapter(adapterBillList);
    }

    private void showBillListFilterToday(){
        Calendar rightNow = Calendar.getInstance();

        rightNow.set(Calendar.HOUR_OF_DAY,0);
        rightNow.set(Calendar.MINUTE,0);
        rightNow.set(Calendar.SECOND,1);
        long first_Hours = rightNow.getTimeInMillis();

        rightNow.set(Calendar.HOUR_OF_DAY,23);
        rightNow.set(Calendar.MINUTE,59);
        rightNow.set(Calendar.SECOND,59);
        long lastHours = rightNow.getTimeInMillis();

        bill_selected = false;
        final RealmResults<Bill>[] results = new RealmResults[]{null};
        mRealm.executeTransaction(realm -> {
            results[0] = mRealm.where(Bill.class)
                    .greaterThanOrEqualTo("createDate", first_Hours)
                    .and()
                    .lessThanOrEqualTo("createDate",lastHours)
                    .findAll();
        });
        adapterBillList = new CustomBillRealmAdapter(results[0]);

        LW_Left_ListBills.setAdapter(adapterBillList);
    }
    private void showBillListFilterThisWeek(){
        Calendar rightNow = Calendar.getInstance();

        rightNow.set(Calendar.HOUR_OF_DAY,0);
        rightNow.set(Calendar.MINUTE,0);
        rightNow.set(Calendar.SECOND,1);
        rightNow.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
        long first_Hours = rightNow.getTimeInMillis();

        rightNow.set(Calendar.HOUR_OF_DAY,23);
        rightNow.set(Calendar.MINUTE,59);
        rightNow.set(Calendar.SECOND,59);
        rightNow.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        long lastHours = rightNow.getTimeInMillis();

        bill_selected = false;
        final RealmResults<Bill>[] results = new RealmResults[]{null};
        mRealm.executeTransaction(realm -> {
            results[0] = mRealm.where(Bill.class)
                    .greaterThanOrEqualTo("createDate", first_Hours)
                    .and()
                    .lessThanOrEqualTo("createDate",lastHours)
                    .findAll();
        });
        adapterBillList = new CustomBillRealmAdapter(results[0]);

        LW_Left_ListBills.setAdapter(adapterBillList);
    }
    private void showBillListFilterThisMonth(){
        Calendar rightNow = Calendar.getInstance();

        rightNow.set(Calendar.HOUR_OF_DAY,0);
        rightNow.set(Calendar.MINUTE,0);
        rightNow.set(Calendar.SECOND,1);
        rightNow.set(Calendar.DAY_OF_MONTH,1);
        long first_Hours = rightNow.getTimeInMillis();

        rightNow.set(Calendar.HOUR_OF_DAY,23);
        rightNow.set(Calendar.MINUTE,59);
        rightNow.set(Calendar.SECOND,59);
        rightNow.set(Calendar.DATE,rightNow.getActualMaximum(Calendar.DAY_OF_MONTH));
        long lastHours = rightNow.getTimeInMillis();

        bill_selected = false;
        final RealmResults<Bill>[] results = new RealmResults[]{null};
        mRealm.executeTransaction(realm -> {
            results[0] = mRealm.where(Bill.class)
                    .greaterThanOrEqualTo("createDate", first_Hours)
                    .and()
                    .lessThanOrEqualTo("createDate",lastHours)
                    .findAll();
        });
        adapterBillList = new CustomBillRealmAdapter(results[0]);

        LW_Left_ListBills.setAdapter(adapterBillList);
    }

    private void showBillString() {
        billString_selected = false;
        final RealmResults<BillString>[] results = new RealmResults[]{null};
        mRealm.executeTransaction(realm -> {
            results[0] = mRealm.where(BillString.class).equalTo("billID", BillID).and().equalTo("isDeleted", false).findAll();
        });
        billStringRealmAdapter = new CustomBillStringRealmAdapter(results[0]);

        LW_Right_BillStrings.setAdapter(billStringRealmAdapter);
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
}
