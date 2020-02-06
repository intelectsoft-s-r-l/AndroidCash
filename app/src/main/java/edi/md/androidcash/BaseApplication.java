package edi.md.androidcash;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.datecs.fiscalprinter.SDK.model.DatecsFiscalDevice;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReceipt;

import edi.md.androidcash.NetworkUtils.AssortmentServiceEntry;
import edi.md.androidcash.NetworkUtils.EposResult.AssortmentListService;
import edi.md.androidcash.NetworkUtils.EposResult.GetAssortmentListResult;
import edi.md.androidcash.NetworkUtils.Promotion;
import edi.md.androidcash.NetworkUtils.QuickGroup;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.CommandServices;
import edi.md.androidcash.RealmHelper.AssortmentRealm;
import edi.md.androidcash.RealmHelper.Barcodes;
import edi.md.androidcash.RealmHelper.QuickGroupRealm;
import io.fabric.sdk.android.Fabric;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edi.md.androidcash.FiscalService.BillLineFiscalService;
import edi.md.androidcash.FiscalService.BillPaymentFiscalService;
import edi.md.androidcash.FiscalService.PrintBillFiscalService;
import edi.md.androidcash.NetworkUtils.EposResult.ResultEposSimple;
import edi.md.androidcash.NetworkUtils.FiscalServiceResult.SimpleResult;
import edi.md.androidcash.NetworkUtils.PaymentType;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.ApiUtils;
import edi.md.androidcash.NetworkUtils.SaveBill;
import edi.md.androidcash.NetworkUtils.SaveBillLine;
import edi.md.androidcash.NetworkUtils.SaveBillPayment;
import edi.md.androidcash.NetworkUtils.SaveShift;
import edi.md.androidcash.NetworkUtils.SendBillsToServer;
import edi.md.androidcash.NetworkUtils.SendShiftToServer;
import edi.md.androidcash.NetworkUtils.User;
import edi.md.androidcash.RealmHelper.Bill;
import edi.md.androidcash.RealmHelper.BillPaymentType;
import edi.md.androidcash.RealmHelper.BillString;
import edi.md.androidcash.RealmHelper.RealmMigrations;
import edi.md.androidcash.RealmHelper.Shift;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.datecs.fiscalprinter.SDK.model.DatecsFiscalDevice.getConnectedModelV2;

public class BaseApplication extends Application {
    public static BaseApplication instance = null;
    private Realm mRealm;

    TimerTask timerTaskSync;
    Timer sync;
    TimerTask timerTaskSyncBackground;
    Timer syncBackground;

    public DatecsFiscalDevice myFiscalDevice = null;
    private User user = null;
    private String userPassWithoutHash;
    private Shift shift = null;

    public static final String SharedPrefSyncSettings = "SyncSetting";
    public static final String SharedPrefFiscalService = "FiscalService";
    public static final String SharedPrefSettings = "Settings";
    public static final String SharedPrefWorkPlaceSettings = "WorkPlace";

    boolean pressed = false;
    boolean inProccesSync = false;
    private ProgressDialog pDialog;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        Realm.init(this);

        instance = this;

        final RealmConfiguration configuration = new RealmConfiguration.Builder().name("cash.realm").schemaVersion(1).migration(new RealmMigrations()).build();
        Realm.setDefaultConfiguration(configuration);
        Realm.getInstance(configuration);
        mRealm  = Realm.getDefaultInstance();

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);


        sync = new Timer();
        sheduleSendBillSHiftToServer();
        sync.schedule(timerTaskSync, 10000, 60000);

        autoUpdateAssortment ();
    }

    public static BaseApplication getInstance(){
        return instance;
    }

    @Override
    public void onTerminate() {
        Realm.getDefaultInstance().close();
        if(sync!=null)
            sync.cancel();

        super.onTerminate();
    }

    @NonNull
    public User getUser() {
        return user;
    }

    public String getUserPasswordsNotHashed(){
      return userPassWithoutHash;
    }
    public void setUserPasswordsNotHashed(String pass){
        this.userPassWithoutHash = pass;
    }
    public void setUser(User user) {
        this.user = user;
    }

    @NonNull
    public Shift getShift() {
        return shift;
    }
    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public void sendShiftToServer(Shift shift){
        String uri = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("URI",null);
        String token = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("InstallationID","null");
        CommandServices commandServices = ApiUtils.commandEposService(uri);

        SaveShift saveShift = new SaveShift();
        SendShiftToServer sendShiftToServer = new SendShiftToServer();

        saveShift.setiD(shift.getId());
        saveShift.setCashID(shift.getWorkPlaceId());
        saveShift.setOpenedById(shift.getAuthor());

        SimpleDateFormat format = new SimpleDateFormat("Z");
        Date date = new Date(shift.getStartDate());
        String createDate = "/Date(" + String.valueOf(shift.getStartDate()) + format.format(date) + ")/";
        saveShift.setCreationDate(createDate);

        if(shift.isClosed()){
            Date dateClosed = new Date(shift.getEndDate());
            String closedDate = "/Date(" + String.valueOf(shift.getEndDate()) + format.format(dateClosed) + ")/";
            saveShift.setClosed(shift.isClosed());
            saveShift.setClosingDate(closedDate);
            saveShift.setClosedByID(shift.getClosedBy());
        }

        sendShiftToServer.setShift(saveShift);
        sendShiftToServer.setToken(token);

        Call<ResultEposSimple> call = commandServices.saveShiftCall(sendShiftToServer);
        call.enqueue(new Callback<ResultEposSimple>() {
            @Override
            public void onResponse(Call<ResultEposSimple> call, Response<ResultEposSimple> response) {
                ResultEposSimple resultEposSimple = response.body();
                if(resultEposSimple != null){
                    int error = resultEposSimple.getErrorCode();

                    if(error == 0){
                        mRealm  = Realm.getDefaultInstance();
                        mRealm.executeTransaction(realm -> {
                            Shift results = mRealm.where(Shift.class).equalTo("id",saveShift.getiD()).findFirst();
                            if(results != null){
                                results.setSended(true);
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<ResultEposSimple> call, Throwable t) {
                String tt= t.getMessage();
            }
        });

    }

    public void sendBilltToServer(RealmResults<Bill> billRealmResults){
        String uri = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("URI",null);
        String token = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("InstallationID","null");
        CommandServices commandServices = ApiUtils.commandEposService(uri);

        SimpleDateFormat format = new SimpleDateFormat("Z");

        RealmList<Bill> billRealmList = new RealmList<>();

        for(Bill bill:billRealmResults){
            billRealmList.add(bill);

            SendBillsToServer sendBillsToServer = new SendBillsToServer();

            List<SaveBill> listBill = new ArrayList<>();
            List<SaveBillLine> listLines = new ArrayList<>();
            List<SaveBillPayment> listBillPayment = new ArrayList<>();

            SaveBill saveBill = new SaveBill();

            saveBill.setID(bill.getId());
            saveBill.setClosedByID(bill.getClosedBy());

            Date date = new Date(bill.getCloseDate());
            String closingDate = "/Date(" + String.valueOf(bill.getCloseDate()) + format.format(date) + ")/";
            saveBill.setClosingDate(closingDate);

            Date dateCreate = new Date(bill.getCreateDate());
            String createDate = "/Date(" + String.valueOf(bill.getCreateDate()) + format.format(dateCreate) + ")/";
            saveBill.setCreationDate(createDate);

            saveBill.setDiscountCardId(bill.getDiscountCardId());

            RealmList<BillString> billStringRealm = bill.getBillStrings();
            for(BillString billString: billStringRealm){
                SaveBillLine saveBillLine = new SaveBillLine();

                saveBillLine.setCount(billString.getQuantity());
                saveBillLine.setCreatedByID(billString.getCreateBy());

                Date dateCreateLine = new Date(billString.getCreateDate());
                String createDateLine = "/Date(" + String.valueOf(billString.getCreateDate()) + format.format(dateCreateLine) + ")/";
                saveBillLine.setCreationDate(createDateLine);

                saveBillLine.setDeletedByID(billString.getDeleteBy());

                Date dateDelet = new Date(billString.getDeletionDate());
                String deletingDate = "/Date(" + String.valueOf(billString.getDeletionDate()) + format.format(dateDelet) + ")/";
                saveBillLine.setDeletionDate(deletingDate);

                saveBillLine.setIsDeleted(billString.isDeleted());
                saveBillLine.setPrice(billString.getPrice());
                saveBillLine.setPriceLineID(billString.getPriceLineID());
                saveBillLine.setPromoPrice(billString.getPromoPrice());
                saveBillLine.setSum(billString.getSum());
                saveBillLine.setSumWithDiscount(billString.getSumWithDiscount());
                saveBillLine.setVATQuote(billString.getVat());

                listLines.add(saveBillLine);
            }

            RealmList<BillPaymentType> billPaymentTypeRealmList = bill.getBillPaymentTypes();
            for(BillPaymentType billPaymentType: billPaymentTypeRealmList){
                SaveBillPayment saveBillPayment = new SaveBillPayment();

                saveBillPayment.setCreatedByID(billPaymentType.getAuthor());
                saveBillPayment.setID(billPaymentType.getId());
                saveBillPayment.setPaymentTypeID(billPaymentType.getPaymentTypeID());
                saveBillPayment.setSum(billPaymentType.getSum());

                Date createDatePayment = new Date(billPaymentType.getCreateDate());
                String createDatePayments = "/Date(" + String.valueOf(billPaymentType.getCreateDate()) + format.format(createDatePayment) + ")/";
                saveBillPayment.setCreationDate(createDatePayments);

                listBillPayment.add(saveBillPayment);
            }

            saveBill.setNumber(bill.getShiftReceiptNumSoftware());
            saveBill.setOpenedByID(bill.getAuthor());

            saveBill.setLines(listLines);
            saveBill.setPayments(listBillPayment);

            listBill.add(saveBill);

            sendBillsToServer.setBills(listBill);
            sendBillsToServer.setShiftID(bill.getShiftId());
            sendBillsToServer.setToken(token);



            Call<ResultEposSimple> call = commandServices.saveBillCall(sendBillsToServer);
            call.enqueue(new Callback<ResultEposSimple>() {
                @Override
                public void onResponse(Call<ResultEposSimple> call, Response<ResultEposSimple> response) {
                    ResultEposSimple resultEposSimple = response.body();
                    if(resultEposSimple != null){
                        int error = resultEposSimple.getErrorCode();

                        if(error == 0){
                            String bilId = saveBill.getID();
                            mRealm  = Realm.getDefaultInstance();
                            mRealm.executeTransaction(realm -> {
                                Bill bilRealm = realm.where(Bill.class).equalTo("id",bilId).findFirst();
                                if(bilRealm != null){
                                    bilRealm.setSinchronized(true);
                                }
                            });
                    }
                        else{
                            String bilId = saveBill.getID();
                            mRealm  = Realm.getDefaultInstance();
                            mRealm.executeTransaction(realm -> {
                                Bill bilRealm = realm.where(Bill.class).equalTo("id",bilId).findFirst();
                                if(bilRealm != null){
                                    bilRealm.setSinchronized(false);
                                    bilRealm.setInProcessOfSync(2);
                                }

                            });

                        }
                    }
                }
                @Override
                public void onFailure(Call<ResultEposSimple> call, Throwable t) {
                    String tt= t.getMessage();
                }
            });
        }
    }

    public DatecsFiscalDevice getMyFiscalDevice() {
        return myFiscalDevice;
    }

    public void setMyFiscalDevice(DatecsFiscalDevice myFiscalDevice) {
        this.myFiscalDevice = myFiscalDevice;
    }

    public int printFiscalReceipt(cmdReceipt.FiscalReceipt fiscalReceipt, RealmList<BillString> billString, PaymentType paymentType, double suma, RealmList<BillPaymentType> paymentList,int number){
        String resCloseBill = "0";
        //                String resFreText = getConnectedModelV2().customCommand(54,"Intelect Soft");
        try {
            if (!fiscalReceipt.isOpen()) {

                //Open Fiscal bon in current receipt
                String resultStringOpenReceipt = openFiscalReceipt("30","000","1","");

                String result = getConnectedModelV2().customCommand(48,resultStringOpenReceipt);
                String numberString = String.valueOf(number);
                fiscalReceipt.printFreeText(numberString);
                //print bill strings
                for(BillString billStringEntry: billString){
                    String name = billStringEntry.getAssortmentFullName();
                    if(name.length() > 25)
                        name = name.substring(0,24);
                    String taxCod = String.format("%.0f", billStringEntry.getVat());
                    String codeVat = "1";
                    if(taxCod.equals("20"))
                        codeVat = "1";
                    else if (taxCod.equals("8"))
                        codeVat = "2";
                    else
                        codeVat = "3";
                    String price = String.format("%.2f", billStringEntry.getPrice()).replace(",",".");
                    String count = String.format("%.2f", billStringEntry.getQuantity()).replace(",",".");

                    double discVal = billStringEntry.getPrice() - billStringEntry.getPriceWithDiscount();

                    String resultStringRegSales = "";
                    if(discVal == 0) {
                        resultStringRegSales = regSalesVar0Version0(name,codeVat,price,count,"","","0");
                    }
                    else
                        resultStringRegSales = regSalesVar0Version0(name,codeVat,price,count,"4",String.format("%.2f", discVal).replace(",","."),"0");

                    //inregistrez pozitia in bonul fiscal
                    getConnectedModelV2().customCommand(49,resultStringRegSales);
                }

                if(!paymentList.isEmpty()){
                    for(BillPaymentType billPaymentType:paymentList){
                        String code = String.valueOf(billPaymentType.getPaymentCode());
                        String summ = String.format("%.2f", billPaymentType.getSum()).replace(",",".");

                        //Pay in calc TOTAL receip
                        String PayBill = PaymentBill(code,summ,"1");

                        //payment
                        getConnectedModelV2().customCommand(53,PayBill);
                    }
                    String code = String.valueOf(paymentType.getCode());
                    String summ = String.format("%.2f",suma).replace(",",".");

                    //Pay in calc TOTAL receip
                    String PayBill = PaymentBill(code,summ,"1");

                    //payment
                    getConnectedModelV2().customCommand(53,PayBill);
                }
                else{
                    String code = String.valueOf(paymentType.getCode());
                    String summ = String.format("%.2f",suma).replace(",",".");

                    //Pay in calc TOTAL receip
                    String PayBill = PaymentBill(code,summ,"1");

                    //payment
                    getConnectedModelV2().customCommand(53,PayBill);
                }

                fiscalReceipt.printFreeText("Intelect Soft S.R.L.");
                //close bill
                resCloseBill = getConnectedModelV2().customCommand(56,"");

                String erroreCod = Character.toString(resCloseBill.charAt(0));

                if(erroreCod.equals("0")){
                    char[] myNameChars = resCloseBill.toCharArray();
                    myNameChars[0] = ' ';
                    resCloseBill = String.valueOf(myNameChars);

                    resCloseBill = resCloseBill.trim();

                    postToast("resCloseBill after trim and replace: '" + resCloseBill + "'");

                    return Integer.valueOf(resCloseBill);
                }
                else{
                    return 0;
                }


            }
            else {
                cancelSale(fiscalReceipt);
                return Integer.valueOf(resCloseBill);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if(e.getMessage().contains("-112001"))
                postToast("Fiscal printer error: Fiscal printer command invalid syntax");
            if(e.getMessage().equals("-112024"))
                postToast("Registration mode error: End of 24 hour blocking");
            else if(e.getMessage().contains("-111003"))
                postToast("Registration mode error: Cannot do operation");
            else if(e.getMessage().contains("-111018"))
                postToast("Registration mode error: Payment is initiated");
            else{
                postToast(e.getMessage());
            }
            return Integer.valueOf(resCloseBill);
        }
    }

    public void printReceiptFiscalService (RealmList<BillString> billString, PaymentType paymentType, double suma, RealmList<BillPaymentType> paymentList){
        List<BillPaymentFiscalService> paymentFiscalServices = new ArrayList<>();
        List<BillLineFiscalService> lineFiscalService = new ArrayList<>();
        PrintBillFiscalService printBillFiscalService = new PrintBillFiscalService();

        for(BillString billStringEntry: billString){
            BillLineFiscalService billLineFiscalService = new BillLineFiscalService();

            String taxCod = String.format("%.0f", billStringEntry.getVat());
            String codeVat = "1";
            if(taxCod.equals("20"))
                codeVat = "1";
            else if (taxCod.equals("8"))
                codeVat = "2";
            else
                codeVat = "3";

            billLineFiscalService.setAmount(billStringEntry.getQuantity());
            billLineFiscalService.setName(billStringEntry.getAssortmentFullName());
            billLineFiscalService.setPrice(billStringEntry.getPriceWithDiscount());
            billLineFiscalService.setVAT(codeVat);

            lineFiscalService.add(billLineFiscalService);
        }

        if(!paymentList.isEmpty()){
            for(BillPaymentType billPaymentType:paymentList){
                BillPaymentFiscalService billPaymentFiscalService = new BillPaymentFiscalService();

//                billPaymentFiscalService.setCode(String.valueOf(paymentType.getCode()));
                billPaymentFiscalService.setPaymentSum(billPaymentType.getSum());
                billPaymentFiscalService.setCode("1");

                paymentFiscalServices.add(billPaymentFiscalService);
            }
            BillPaymentFiscalService billPaymentFiscalService = new BillPaymentFiscalService();

            billPaymentFiscalService.setPaymentSum(suma);
            billPaymentFiscalService.setCode("1");
//            billPaymentFiscalService.setCode(String.valueOf(paymentType.getCode()));

            paymentFiscalServices.add(billPaymentFiscalService);
        }
        else{
            BillPaymentFiscalService billPaymentFiscalService = new BillPaymentFiscalService();

            billPaymentFiscalService.setPaymentSum(suma);
            billPaymentFiscalService.setCode("1");
//            billPaymentFiscalService.setCode(String.valueOf(paymentType.getCode()));

            paymentFiscalServices.add(billPaymentFiscalService);
        }

        printBillFiscalService.setLines(lineFiscalService);
        printBillFiscalService.setPayments(paymentFiscalServices);

        String ip = getSharedPreferences(SharedPrefFiscalService, MODE_PRIVATE).getString("IpAdressFiscalService",null);
        String port = getSharedPreferences(SharedPrefFiscalService, MODE_PRIVATE).getString("PortFiscalService",null);

        int[] result = {101};
        if(ip != null && port != null) {
            String uri = ip + ":" + port;

            CommandServices commandServices = ApiUtils.commandEposService(uri);
            Call<SimpleResult> call = commandServices.printBill(printBillFiscalService);

            call.enqueue(new Callback<SimpleResult>() {
                @Override
                public void onResponse(Call<SimpleResult> call, Response<SimpleResult> response) {
                    SimpleResult result1 = response.body();
                    if(result1 != null){
                        result[0] = result1.getErrorCode();
                    }
                }

                @Override
                public void onFailure(Call<SimpleResult> call, Throwable t) {
                    result[0] = 111;
                }
            });
        }
    }

    private String regSalesVar0Version0 (String PluName, String taxCod,String price,String count,String DiscType,String Discval,String Departament){
        String InputStringRegSales = "";
        InputStringRegSales = InputStringRegSales + PluName + "\t";
        InputStringRegSales = InputStringRegSales + taxCod + "\t";
        InputStringRegSales = InputStringRegSales + price + "\t";
        InputStringRegSales = InputStringRegSales + count + "\t";
        InputStringRegSales = InputStringRegSales + DiscType + "\t";
        InputStringRegSales = InputStringRegSales + Discval + "\t";
        InputStringRegSales = InputStringRegSales + Departament + "\t";

        /*Mandatory parameters: PluName, TaxCd, Price
             PluName - Name of product, up to 72 characters not empty string;
             TaxCd - Tax code;
             '1' - vat group A;
             '2' - vat group B;
             '3' - vat group C;
             '4' - vat group D;
             '5' - vat group E;
             '6' - vat group F;
             '7' - vat group G;
             '8' - vat group H;
             Price - Product price, with sign '-' at void operations. Format: 2 decimals; up to *9999999.99
             Department - Number of the department 0..99; If '0' - Without department;
            Optional parameters: Quantity, DiscountType, DiscountValue
             Quantity - Quantity of the product ( default: 1.000 ); Format: 3 decimals; up to *999999.999
             Unit - Unit name, up to 6 characters not empty string;
        !!! Max value of Price * Quantity is *9999999.99. !!!
             DiscountType - type of discount.
             '0' or empty - no discount;
             '1' - surcharge by percentage;
             '2' - discount by percentage;
             '3' - surcharge by sum;
             '4' - discount by sum; If DiscountType is non zero, DiscountValue have to contain value. The format must be a value with two decimals.
             DiscountValue - value of discount.
             a number from 0.01 to 9999999.99 for sum operations;
             a number from 0.01 to 99.99 for percentage operations;
            Note
            If DiscountType is zero or empty, parameter DiscountValue must be empty.
         */

        return InputStringRegSales;
    }
    private String openFiscalReceipt ( String OperCode,String OperPass,String TillNumber,String Invoice){
        String InputString = "";
        InputString = InputString + OperCode + "\t";             //Operator number from 1...30;
        InputString = InputString + OperPass + "\t";             //Operator password, ascii string of digits. Lenght from 1...8;
        InputString = InputString + TillNumber + "\t";           // Number of point of sale from 1...99999;
        InputString = InputString + Invoice + "\t";              //If this parameter has value 'I' it opens an invoice receipt. If left blank it opens fiscal receipt;

        return InputString;
    }
    private String PaymentBill ( String payMode, String amount, String type){
        String PayBill = "";
        PayBill = PayBill + payMode + "\t";         //Type of payment; '0' - cash; '1' - credit card; '2' - debit card; '3' - other pay#3 '4' - other pay#4 '5' - other pay#5
        PayBill = PayBill + amount + "\t";          // Amount to pay ( 0.00...9999999.99 or 0...999999999 depending dec point position );
        PayBill = PayBill + type;            //Type of card payment. Only for payment with debit card;   '1' - with money; '12'- with points from loyal scheme;

        return PayBill;

    }

    private void cancelSale(final cmdReceipt.FiscalReceipt fiscalReceipt) {

        try {
            if (fiscalReceipt.isOpen()) {
                fiscalReceipt.closeFiscalReceipt();
                return;
            }

            if (fiscalReceipt.isOpen()) {
                final Double owedSum = new cmdReceipt.FiscalTransaction().getNotPaid();//owedSum=Amount-Tender
                Double payedSum = new cmdReceipt.FiscalTransaction().getPaid();
                //If a TOTAL in the opened receipt has not been set, it will be canceled
                if (payedSum == 0.0) {
                    fiscalReceipt.cancel();
                    return;
                }

                //If a TOTAL is set with a partial payment, there is a Amount and Tender is positive.
                //Offer payment of the amount and completion of the sale.
                if (owedSum > 0.0) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setTitle("Cancel opened receipt.");
                    String sQuestion = String.format("Cannot cancel receipt, payment has already started.\n\r" +
                            "Do you want to pay the owed sum: %2.2f -and close it?", owedSum);
                    dialog.setMessage(sQuestion);
                    dialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                //TOTAL
                                new cmdReceipt.FiscalSale().saleTotal(
                                        cmdReceipt.FiscalSale.PaymentType.cash,
                                        "0.0"    //Whole sum
                                );
                                fiscalReceipt.closeFiscalReceipt();
                            } catch (Exception e) {
                                postToast(e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    });
                    //The operator decides on his own what to do with the unpaid receipts !
                    // So if the answer is NO, the receipt is not closed
                    dialog.setNegativeButton("no", null);
                    dialog.show();
                } else {
                    //If a TOTAL is set with a full payment, there is a Amount-Tender=0.
                    //All is OK, completion of the sale!
                    fiscalReceipt.closeFiscalReceipt();
                }
            }

        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();
        }
    }

    private void postToast(final String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void sheduleSendBillSHiftToServer(){
        timerTaskSync = new TimerTask() {
            @Override
            public void run() {
                mRealm  = Realm.getDefaultInstance();
                mRealm.executeTransaction(realm -> {
                    RealmResults<Shift> resultShift = realm.where(Shift.class).equalTo("isSended",false).findAll();
                    if(!resultShift.isEmpty()){
                        for(Shift shift:resultShift){
                            sendShiftToServer(shift);
                        }
                    }
                    RealmResults<Bill> resultBills = realm.where(Bill.class)
                            .equalTo("state",1)
                            .and()
                            .equalTo("isSinchronized",false)
                            .findAll();

                    if(!resultBills.isEmpty())
                        sendBilltToServer(resultBills);

                });

            }
        };

    }

    private void sheduleUpdateAuto(){
        timerTaskSyncBackground = new TimerTask() {
            @Override
            public void run() {
                boolean enableAutoSync = getSharedPreferences(SharedPrefSyncSettings,MODE_PRIVATE).getBoolean("AutoSync",false);
                if(enableAutoSync){
                    int timeUpdate = getSharedPreferences(SharedPrefSyncSettings,MODE_PRIVATE).getInt("intervalForAutoUpdate",0);

                    if(timeUpdate != 0) {
                        String uri = getSharedPreferences("Settings",MODE_PRIVATE).getString("URI",null);
                        String token = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("Token",null);
                        String workplaceId = getSharedPreferences(SharedPrefWorkPlaceSettings,MODE_PRIVATE).getString("WorkPlaceID",null);
                        if(token != null && workplaceId != null) {
                            CommandServices commandServices = ApiUtils.commandEposService(uri);
                            Call<AssortmentListService> call = commandServices.getAssortiment(token, workplaceId);

                            call.enqueue(new Callback<AssortmentListService>() {
                                @Override
                                public void onResponse(Call<AssortmentListService> call, Response<AssortmentListService> response) {
                                    AssortmentListService assortmentListService = response.body();
                                    if (assortmentListService != null) {
                                        GetAssortmentListResult getAssortmentListResult = assortmentListService.getGetAssortmentListResult();
                                        int errorCode = getAssortmentListResult.getErrorCode();
                                        if (errorCode == 0) {  //успешно получили ответ
                                            List<AssortmentServiceEntry> list = getAssortmentListResult.getAssortments();

                                            insertNewAssortment(list,getAssortmentListResult.getQuickGroups());
                                        } else if (errorCode == 401) {   // нужен новый токен

                                        } else if (errorCode == 405) {   //нет прав/полномочий

                                        } else if (errorCode == 500) {   // внутреняя ошибка сервера

                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<AssortmentListService> call, Throwable t) {
                                    String test = t.getMessage();
                                    String msg = "fsadgzxvzxvzx";
                                }
                            });
                        }
                    }
                }
            }
        };

    }

    public void autoUpdateAssortment (){
        boolean enableAutoSync = getSharedPreferences(SharedPrefSyncSettings,MODE_PRIVATE).getBoolean("AutoSync",false);

        if(enableAutoSync){
            int timeUpdate = getSharedPreferences(SharedPrefSyncSettings,MODE_PRIVATE).getInt("intervalForAutoUpdate",0);

            if(timeUpdate != 0) {
                syncBackground = new Timer();
                sheduleUpdateAuto();
                syncBackground.schedule(timerTaskSyncBackground, timeUpdate, timeUpdate);
            }
        }
    }

    public void insertNewAssortment (List<AssortmentServiceEntry> list, List<QuickGroup>  listGroup){

//        pDialog = new ProgressDialog(this);
//        pDialog.setIndeterminate(true);
//        pDialog.setMessage("sincronizare");
//        pDialog.show();
        mRealm = Realm.getDefaultInstance();
        mRealm.executeTransaction((realm -> {
            realm.delete(AssortmentRealm.class);
            realm.delete(Barcodes.class);
            realm.delete(Promotion.class);
            realm.delete(QuickGroupRealm.class);

            for(AssortmentServiceEntry assortmentServiceEntry: list){
                AssortmentRealm ass = new AssortmentRealm();

                RealmList<Barcodes> listBarcode = new RealmList<>();
                RealmList<Promotion> listPromotion = new RealmList<>();

                if(assortmentServiceEntry.getBarcodes() != null)
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
                ass.setSaleStartTime(MainActivity.replaceDate(assortmentServiceEntry.getSaleStartTime()));
                ass.setSaleEndTime(MainActivity.replaceDate(assortmentServiceEntry.getSaleEndTime()));
                ass.setPriceLineStartDate(MainActivity.replaceDate(assortmentServiceEntry.getPriceLineStartDate()));
                ass.setPriceLineEndDate(MainActivity.replaceDate(assortmentServiceEntry.getPriceLineEndDate()));

                realm.insert(ass);
            }

            if(listGroup != null){
                for(QuickGroup quickGroup : listGroup){
                    QuickGroupRealm quickGroupRealm = new QuickGroupRealm();

                    String nameGroup = quickGroup.getName();
                    RealmList<String> assortment = new RealmList<>();
                    assortment.addAll(quickGroup.getAssortmentID());

                    quickGroupRealm.setGroupName(nameGroup);
                    quickGroupRealm.setAssortmentId(assortment);

                    realm.insert(quickGroupRealm);
                }
            }
        }));

//        pDialog.dismiss();
    }
}
