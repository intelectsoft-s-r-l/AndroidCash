package edi.md.androidcash;

import android.app.AlertDialog;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.datecs.fiscalprinter.SDK.FiscalException;
import com.datecs.fiscalprinter.SDK.model.DatecsFiscalDevice;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReceipt;

import io.fabric.sdk.android.Fabric;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import edi.md.androidcash.FiscalService.BillLineFiscalService;
import edi.md.androidcash.FiscalService.BillPaymentFiscalService;
import edi.md.androidcash.FiscalService.PrintBillFiscalService;
import edi.md.androidcash.NetworkUtils.EposResult.ResultEposSimple;
import edi.md.androidcash.NetworkUtils.FiscalServiceResult.SimpleResult;
import edi.md.androidcash.NetworkUtils.PaymentType;
import edi.md.androidcash.NetworkUtils.ApiUtils;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.PrintBillService;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.SendBillsService;
import edi.md.androidcash.NetworkUtils.RetrofitRemote.SendShiftService;
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

public class GlobalVariables extends Application {
    private BroadcastReceiver br;
    private Realm mRealm;

    TimerTask timerTaskSync;
    Timer sync;

    @Override
    public void onCreate() {
        //init Realm data base
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        Realm.init(this);

        final RealmConfiguration configuration = new RealmConfiguration.Builder().name("cash.realm").schemaVersion(1).migration(new RealmMigrations()).build();
        Realm.setDefaultConfiguration(configuration);
        Realm.getInstance(configuration);
        mRealm  = Realm.getDefaultInstance();

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);


        sync=new Timer();
        sheduleSendBillSHiftToServer();
        sync.schedule(timerTaskSync, 10000, 60000);

//        br = new GlobalReceiver(getApplicationContext());
//        registerReceiver(br,filter);
    }
    @Override
    public void onTerminate() {
        Realm.getDefaultInstance().close();
//        unregisterReceiver(br);
        if(sync!=null)
            sync.cancel();

        super.onTerminate();
    }

    public DatecsFiscalDevice myFiscalDevice = null;
    private cmdReceipt.NonFiscalReceipt noFiscalReceipt;
    private User user = null;


    public static final String SharedPrefSyncSettings = "SyncSetting";
    public static final String SharedPrefFiscalService = "FiscalService";
    public static final String SharedPrefSettings = "Settings";
    public static final String SharedPrefWorkPlaceSettings = "WorkPlace";

    public String getUserName() {
        if(user != null ){
            return  user.getFirstName() + user.getLastName();
        }
        else{
            return null;
        }

    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void sendShiftToServer(Shift shift){
        String uri = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("URI",null);
        String token = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("InstallationID","null");
        SendShiftService sendShiftService = ApiUtils.getSaveShiftService(uri);

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

        Call<ResultEposSimple> call = sendShiftService.saveShiftCall(sendShiftToServer);
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
        SendBillsService sendShiftToServer = ApiUtils.getSaveBillService(uri);

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



            Call<ResultEposSimple> call = sendShiftToServer.saveBillCall(sendBillsToServer);
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

    public String printDuplicateReceipt(cmdReceipt.FiscalReceipt fiscalReceipt){
        String resCloseBill = "0";
        try {
            if (!fiscalReceipt.isOpen()) {
//               getConnectedModelV2().command109Variant0Version0();
                resCloseBill = getConnectedModelV2().customCommand(109,"");
            }
        } catch (IOException e) {
            e.printStackTrace();
            postToast(e.getMessage());
        } catch (FiscalException e) {
            e.printStackTrace();
            postToast(e.getMessage());
        }
        postToast(resCloseBill);
        return resCloseBill;
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

            PrintBillService printBillService = ApiUtils.printBillService(uri);
            Call<SimpleResult> call = printBillService.printBill(printBillFiscalService);

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

    public String closeNonFiscalReceipt(cmdReceipt.FiscalReceipt fiscalReceipt, String billStringDirectory,String sum,String paymentMode){ //String[] billStrings
        String resCloseBill = null;
        try {
            if (!fiscalReceipt.isOpen()) {

                //Open Fiscal bon in current receipt
                String resultStringOpenReceipt = openFiscalReceipt("30","000","1","");
                String result = getConnectedModelV2().customCommand(48,resultStringOpenReceipt);

                //print bill strings
//                for(BillString billStringEntry:billStringDirectory){
//                    String name = billStringEntry.getAssortmentFullName();
//                    double taxCod = billStringEntry.getVat();
//                    postToast("taxCod: " + taxCod);
//                    String price = String.format("%.2f", billStringEntry.getPrice()).replace(",",".");
//                    String count = String.format("%.2f", billStringEntry.getQuantity()).replace(",",".");
//
//                    String resultStringRegSales = regSalesVar0Version0(name,"1",price,count,"","","0");
//                    getConnectedModelV2().customCommand(49,resultStringRegSales);
//                }
//                String resultStringRegSales2 = regSalesVar0Version0("Pchet","2","28.60","3.00","4","11.49","0");

                //Pay in calc TOTAL receip
                String PayBill = PaymentBill(paymentMode,sum,"1");
//                String resFreText = getConnectedModelV2().customCommand(54,"Intelect Soft");
                String respayBill = getConnectedModelV2().customCommand(53,PayBill);

                //close bill
                resCloseBill = getConnectedModelV2().customCommand(56,"");

                String erroreCod = Character.toString(resCloseBill.charAt(0));
                if(erroreCod.equals("0")){
                    char[] myNameChars = resCloseBill.toCharArray();
                    myNameChars[0] = ' ';
                    resCloseBill = String.valueOf(myNameChars);

                    resCloseBill = resCloseBill.trim();

                    postToast("resCloseBill after trim and replace: '" + resCloseBill + "'");

                    return resCloseBill;
                }
                else{
                    return null;
                }


            }
            else {
                cancelSale(fiscalReceipt);
                return resCloseBill;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if(e.getMessage().contains("-112001"))
                postToast("Fiscal printer error: Fiscal printer command invalid syntax");
            else if(e.getMessage().contains("-112024"))
                postToast("Registration mode error: End of 24 hour blocking");
            else if(e.getMessage().contains("-111003"))
                postToast("Registration mode error: Cannot do operation");
            else{
                postToast(e.getMessage());
            }
            return resCloseBill;
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
            if (noFiscalReceipt.isOpen()) {
                noFiscalReceipt.closeNonFiscalReceipt();
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
    //записывание логов в папку IntelectSoft в корневую директорию устройства
    public void appendLog(String text, Context context) {
        File file = null;
        File teste = new File(Environment.getExternalStorageDirectory(),"/IntelectSoft");
        if (!teste.mkdirs()) {
            Log.e("LOG TAG", "Directory not created");
        }
        file = new File(Environment.getExternalStorageDirectory(),"/IntelectSoft/Cash2020.txt");
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
            String err = sDateInChisinau+ ": " + context.getClass() + ": " + text;
            buf.append(err);
            //buf.write(text);
            buf.newLine();
            buf.close(); }
        catch (IOException e) {
            e.printStackTrace();
        }
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

}
