package md.intelectsoft.salesepos;

import android.app.Application;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.datecs.fiscalprinter.SDK.FiscalException;
import com.datecs.fiscalprinter.SDK.model.DatecsFiscalDevice;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReceipt;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.BuildConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;


import md.intelectsoft.salesepos.NetworkUtils.RetrofitRemote.CommandServices;
import md.intelectsoft.salesepos.Utils.UpdateHelper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import md.intelectsoft.salesepos.FiscalService.BillLineFiscalService;
import md.intelectsoft.salesepos.FiscalService.BillPaymentFiscalService;
import md.intelectsoft.salesepos.FiscalService.PrintBillFiscalService;
import md.intelectsoft.salesepos.NetworkUtils.EposResult.ResultEposSimple;
import md.intelectsoft.salesepos.NetworkUtils.FiscalServiceResult.SimpleResult;
import md.intelectsoft.salesepos.NetworkUtils.PaymentType;
import md.intelectsoft.salesepos.NetworkUtils.RetrofitRemote.ApiUtils;
import md.intelectsoft.salesepos.NetworkUtils.SaveBill;
import md.intelectsoft.salesepos.NetworkUtils.SaveBillLine;
import md.intelectsoft.salesepos.NetworkUtils.SaveBillPayment;
import md.intelectsoft.salesepos.NetworkUtils.SaveShift;
import md.intelectsoft.salesepos.NetworkUtils.SendBillsToServer;
import md.intelectsoft.salesepos.NetworkUtils.SendShiftToServer;
import md.intelectsoft.salesepos.NetworkUtils.User;
import md.intelectsoft.salesepos.RealmHelper.Bill;
import md.intelectsoft.salesepos.RealmHelper.BillPaymentType;
import md.intelectsoft.salesepos.RealmHelper.BillString;
import md.intelectsoft.salesepos.RealmHelper.RealmMigrations;
import md.intelectsoft.salesepos.RealmHelper.Shift;
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

    private TimerTask timerTaskSyncBill;
    private Timer timerSyncBill;
    private TimerTask timerTaskSyncBackground;
    private Timer syncBackground;

    public DatecsFiscalDevice myFiscalDevice = null;
    private User user = null;
    private String userPassWithoutHash;
    private Shift shift = null;

    //SharedPreference name
    public static final String SharedPrefSyncSettings = "SyncSetting";
    public static final String SharedPrefFiscalService = "FiscalService";
    public static final String SharedPrefSettings = "Settings";
    public static final String SharedPrefWorkPlaceSettings = "WorkPlace";

    //SharedPreference variable
    public static final String deviceId = "DeviceId";

    private UsbDevice deviceFiscal;


    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);

//        Thread.setDefaultUncaughtExceptionHandler(handleAppCrash);

        instance = this;

        final RealmConfiguration configuration = new RealmConfiguration.Builder().name("cash.realm").schemaVersion(1).migration(new RealmMigrations()).build();
        Realm.setDefaultConfiguration(configuration);
        Realm.getInstance(configuration);
        mRealm  = Realm.getDefaultInstance();

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);


        timerSyncBill = new Timer();
        sheduleSendBillSHiftToServer();
        timerSyncBill.schedule(timerTaskSyncBill, 10000, 60000);

        checkUpdates();
    }

    public void checkUpdates(){
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(6)
                .build();
        remoteConfig.setConfigSettingsAsync(configSettings);

        //defaultvalue
        Map<String,Object> defaultValue = new HashMap<>();
        defaultValue.put(UpdateHelper.KEY_UPDATE_URL,"https://edi.md/androidapps/cash.apk");
        defaultValue.put(UpdateHelper.KEY_UPDATE_VERSION,"1.0");
        defaultValue.put(UpdateHelper.KEY_UPDATE_ENABLE,false);
        defaultValue.put(UpdateHelper.KEY_UPDATE_CHANGES,"");

        defaultValue.put(UpdateHelper.KEY_UPDATE_TRIAL_URL,"https://edi.md/androidapps/cash_trial.apk");
        defaultValue.put(UpdateHelper.KEY_UPDATE_TRIAL_VERSION,"1.1");
        defaultValue.put(UpdateHelper.KEY_UPDATE_TRIAL_ENABLE,false);
        defaultValue.put(UpdateHelper.KEY_UPDATE_TRIAL_CHANGES,"");

        remoteConfig.setDefaultsAsync(defaultValue);

        remoteConfig.fetch(6).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d("TAG", "remote config is fetched.");
                    remoteConfig.activate();
                }
            }
        });
    }

    public static BaseApplication getInstance(){
        return instance;
    }

    @Override
    public void onTerminate() {
        Realm.getDefaultInstance().close();
        if(timerSyncBill !=null)
            timerSyncBill.cancel();

        super.onTerminate();
    }

    public User getUser() {
        return user;
    }
    public String getUserId(){
        return user.getId();
    }
    public String getUserPasswordsNotHashed(){
      return userPassWithoutHash;
    }
    public void setUserPasswordsNotHashed(String pass){
        this.userPassWithoutHash = pass;
    }
    public void setUser(User user235) {
        this.user = user235;
    }

    public Shift getShift() {
        return shift;
    }
    public String getShiftID(){
        return shift.getId();
    }
    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public UsbDevice getDeviceFiscal() {
        return deviceFiscal;
    }

    public void setDeviceFiscal(UsbDevice deviceFiscal) {
        this.deviceFiscal = deviceFiscal;
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

                    double discVal = billStringEntry.getSum() - billStringEntry.getSumWithDiscount();

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

                    //TODO close bill message succes
//                    postToast("Receipt number: " + resCloseBill + " was printed");

                    return Integer.valueOf(resCloseBill);
                }
                else{
                    return 0;
                }
            }
            else {
                boolean isCanceled = cancelSale(fiscalReceipt);

                if(!isCanceled){
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

                        double discVal = billStringEntry.getSum() - billStringEntry.getSumWithDiscount();

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

                        //TODO close bill message succes
//                    postToast("Receipt number: " + resCloseBill + " was printed");

                        return Integer.valueOf(resCloseBill);
                    }
                    else{
                        return 0;
                    }
                }
                else{
                    return Integer.valueOf(resCloseBill);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            if(e.getMessage() != null){

                String erMsg = e.getMessage();
                erMsg = erMsg.replace("\n\r","");

                String er = "";

                try{
                    er = erMsg.substring(0,7);
                }
                catch (Exception e1){
                    er = erMsg.substring(0,7);
                }

                int errore = Integer.parseInt(er);

                String msg = FiscalException.locale(errore,1073741824);
                postToast(msg);
            }
            return Integer.valueOf(resCloseBill);
        }
    }
    public void printReceiptFiscalService (RealmList<BillString> billString, PaymentType paymentType, double suma, RealmList<BillPaymentType> paymentList,String numberBill){
        List<BillPaymentFiscalService> paymentFiscalServices = new ArrayList<>();
        List<BillLineFiscalService> lineFiscalService = new ArrayList<>();
        PrintBillFiscalService printBillFiscalService = new PrintBillFiscalService();
        printBillFiscalService.setHeaderText(user.getFirstName() + " " +  user.getLastName());
        printBillFiscalService.setNumber(numberBill);



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
            if(billStringEntry.getPrice() != billStringEntry.getPriceWithDiscount()){
                billLineFiscalService.setDiscount(billStringEntry.getPrice() - billStringEntry.getPriceWithDiscount());
            }
            billLineFiscalService.setVAT(codeVat);

            lineFiscalService.add(billLineFiscalService);
        }

        if(!paymentList.isEmpty()){
            for(BillPaymentType billPaymentType:paymentList){
                BillPaymentFiscalService billPaymentFiscalService = new BillPaymentFiscalService();

                billPaymentFiscalService.setCode(String.valueOf(paymentType.getCode()));
                billPaymentFiscalService.setPaymentSum(billPaymentType.getSum());

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
            billPaymentFiscalService.setCode(String.valueOf(paymentType.getCode()));

            paymentFiscalServices.add(billPaymentFiscalService);
        }

        printBillFiscalService.setLines(lineFiscalService);
        printBillFiscalService.setPayments(paymentFiscalServices);

        String uri = getSharedPreferences(SharedPrefSettings, MODE_PRIVATE).getString("FiscalServiceAddress","0.0.0.0:1111");



        CommandServices commandServices = ApiUtils.commandFPService(uri);
        Call<SimpleResult> call = commandServices.printBill(printBillFiscalService);

        call.enqueue(new Callback<SimpleResult>() {
            @Override
            public void onResponse(Call<SimpleResult> call, Response<SimpleResult> response) {
                SimpleResult result1 = response.body();
                if(result1 != null){
                    if(result1.getErrorCode() == 0){
                        MainActivity.doAfterCloseBill();
                    }
                    else{
                        postToast("Error code: " + result1.getErrorCode());
                    }
                }
            }

            @Override
            public void onFailure(Call<SimpleResult> call, Throwable t) {
                postToast("Error service: " + t.getMessage());
            }
        });
    }

    //fiscal device operation
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

    private boolean cancelSale(final cmdReceipt.FiscalReceipt fiscalReceipt) {
        try {
            if (fiscalReceipt.isOpen()) {

//                fiscalReceipt.closeFiscalReceipt();
                fiscalReceipt.cancel();
                return fiscalReceipt.isOpen();
            }
            else
                return true;

//            if (fiscalReceipt.isOpen()) {
//                postToast("fiscal is open a doua oara");
//                final Double owedSum = new cmdReceipt.FiscalTransaction().getNotPaid();//owedSum=Amount-Tender
//                Double payedSum = new cmdReceipt.FiscalTransaction().getPaid();
//                //If a TOTAL in the opened receipt has not been set, it will be canceled
//                if (payedSum == 0.0) {
//                    fiscalReceipt.cancel();
//                    return;
//                }
//
//                //If a TOTAL is set with a partial payment, there is a Amount and Tender is positive.
//                //Offer payment of the amount and completion of the sale.
//                if (owedSum > 0.0) {
//                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//                    dialog.setTitle("Cancel opened receipt.");
//                    String sQuestion = String.format("Cannot cancel receipt, payment has already started.\n\r" +
//                            "Doц you want to pay the owed sum: %2.2f -and close it?", owedSum);
//                    dialog.setMessage(sQuestion);
//                    dialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            try {
//                                //TOTAL
//                                new cmdReceipt.FiscalSale().saleTotal(
//                                        cmdReceipt.FiscalSale.PaymentType.cash,
//                                        "0.0"    //Whole sum
//                                );
//                                fiscalReceipt.closeFiscalReceipt();
//                            } catch (Exception e) {
//                                if(e.getMessage().contains("-111018"))
//                                    postToast("Cancel sales! Registration mode error: Payment is initiated");
//                                else
//                                    postToast(e.getMessage());
//
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//                    //The operator decides on his own what to do with the unpaid receipts !
//                    // So if the answer is NO, the receipt is not closed
//                    dialog.setNegativeButton("no", null);
//                    dialog.show();
//                } else {
//                    //If a TOTAL is set with a full payment, there is a Amount-Tender=0.
//                    //All is OK, completion of the sale!
//                    fiscalReceipt.closeFiscalReceipt();
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //sync shift and bills to server background
    public void sendShiftToServer(Shift shift){
        String uri = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("URI",null);
        String token = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("Token","null");
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
        String token = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("Token","null");
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
                                    bilRealm.setSynchronized(true);
                                }
                            });
                        }
                        else{
                            String bilId = saveBill.getID();
                            mRealm  = Realm.getDefaultInstance();
                            mRealm.executeTransaction(realm -> {
                                Bill bilRealm = realm.where(Bill.class).equalTo("id",bilId).findFirst();
                                if(bilRealm != null){
                                    bilRealm.setSynchronized(false);
                                    bilRealm.setInProcessOfSync(2);
                                }

                            });

                        }
                    }
                }
                @Override
                public void onFailure(Call<ResultEposSimple> call, Throwable t) {
                    String tt = t.getMessage();
                }
            });
        }
    }
    private void sheduleSendBillSHiftToServer(){
        timerTaskSyncBill = new TimerTask() {
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
                            .equalTo("isSynchronized",false)
                            .findAll();

                    if(!resultBills.isEmpty())
                        sendBilltToServer(resultBills);
                });
            }
        };
    }

    //sync in background assortment
//    private void sheduleUpdateAuto(){
//        timerTaskSyncBackground = new TimerTask() {
//            @Override
//            public void run() {
//                boolean enableAutoSync = getSharedPreferences(SharedPrefSyncSettings,MODE_PRIVATE).getBoolean("AutoSync",false);
//                if(enableAutoSync){
//                    int timeUpdate = getSharedPreferences(SharedPrefSyncSettings,MODE_PRIVATE).getInt("intervalForAutoUpdate",0);
//
//                    if(timeUpdate != 0) {
//                        String uri = getSharedPreferences("SettingsActivity",MODE_PRIVATE).getString("URI",null);
//                        String token = getSharedPreferences(SharedPrefSettings,MODE_PRIVATE).getString("Token",null);
//                        String workplaceId = getSharedPreferences(SharedPrefWorkPlaceSettings,MODE_PRIVATE).getString("WorkPlaceID",null);
//                        if(token != null && workplaceId != null) {
//                            CommandServices commandServices = ApiUtils.commandEposService(uri);
//                            Call<AssortmentListService> call = commandServices.getAssortiment(token, workplaceId);
//
//                            call.enqueue(new Callback<AssortmentListService>() {
//                                @Override
//                                public void onResponse(Call<AssortmentListService> call, Response<AssortmentListService> response) {
//                                    AssortmentListService assortmentListService = response.body();
//                                    if (assortmentListService != null) {
//                                        GetAssortmentListResult getAssortmentListResult = assortmentListService.getGetAssortmentListResult();
//                                        int errorCode = getAssortmentListResult.getErrorCode();
//                                        if (errorCode == 0) {  //успешно получили ответ
//                                            List<AssortmentServiceEntry> list = getAssortmentListResult.getAssortments();
//
//                                            insertNewAssortment(list,getAssortmentListResult.getQuickGroups());
//                                        } else if (errorCode == 401) {   // нужен новый токен
//
//                                        } else if (errorCode == 405) {   //нет прав/полномочий
//
//                                        } else if (errorCode == 500) {   // внутреняя ошибка сервера
//
//                                        }
//                                    }
//                                }
//
//                                @Override
//                                public void onFailure(Call<AssortmentListService> call, Throwable t) {
//                                    String test = t.getMessage();
//                                    String msg = "fsadgzxvzxvzx";
//                                }
//                            });
//                        }
//                    }
//                }
//            }
//        };
//
//    }
//    public void autoUpdateAssortment (){
//        boolean enableAutoSync = getSharedPreferences(SharedPrefSyncSettings,MODE_PRIVATE).getBoolean("AutoSync",false);
//
//        if(enableAutoSync){
//            int timeUpdate = getSharedPreferences(SharedPrefSyncSettings,MODE_PRIVATE).getInt("intervalForAutoUpdate",0);
//
//            if(timeUpdate != 0) {
//                syncBackground = new Timer();
//                sheduleUpdateAuto();
//                syncBackground.schedule(timerTaskSyncBackground, timeUpdate, timeUpdate);
//            }
//        }
//    }
//    public void insertNewAssortment (List<AssortmentServiceEntry> list, List<QuickGroup>  listGroup){
//
////        pDialog = new ProgressDialog(this);
////        pDialog.setIndeterminate(true);
////        pDialog.setMessage("sincronizare");
////        pDialog.show();
//        mRealm = Realm.getDefaultInstance();
//        mRealm.executeTransaction((realm -> {
//            realm.delete(AssortmentRealm.class);
//            realm.delete(Barcodes.class);
//            realm.delete(Promotion.class);
//            realm.delete(QuickGroupRealm.class);
//
//            for(AssortmentServiceEntry assortmentServiceEntry: list){
//                AssortmentRealm ass = new AssortmentRealm();
//
//                RealmList<Barcodes> listBarcode = new RealmList<>();
//                RealmList<Promotion> listPromotion = new RealmList<>();
//
//                if(assortmentServiceEntry.getBarcodes() != null)
//                for(String barcodes : assortmentServiceEntry.getBarcodes()){
//                    Barcodes barcodes1 = new Barcodes();
//                    barcodes1.setBar(barcodes);
//                    listBarcode.add(barcodes1);
//                }
//
//                if(assortmentServiceEntry.getPromotions()!= null){
//                    listPromotion.addAll(assortmentServiceEntry.getPromotions());
//                }
//                ass.setId(assortmentServiceEntry.getID());
//                ass.setName(assortmentServiceEntry.getName());
//                ass.setBarcodes(listBarcode);
//                ass.setFolder(assortmentServiceEntry.getIsFolder());
//                ass.setPromotions(listPromotion);
//                ass.setAllowDiscounts(assortmentServiceEntry.getAllowDiscounts());
//                ass.setAllowNonInteger(assortmentServiceEntry.getAllowNonInteger());
//                ass.setCode(assortmentServiceEntry.getCode());
//                ass.setEnableSaleTimeRange(assortmentServiceEntry.getEnableSaleTimeRange());
//                ass.setMarking(assortmentServiceEntry.getMarking());
//                ass.setParentID(assortmentServiceEntry.getParentID());
//                ass.setPrice(assortmentServiceEntry.getPrice());
//                ass.setPriceLineId(assortmentServiceEntry.getPriceLineId());
//                ass.setShortName(assortmentServiceEntry.getShortName());
//                ass.setVat(assortmentServiceEntry.getVAT());
//                ass.setUnit(assortmentServiceEntry.getUnit());
//                ass.setQuickButtonNumber(assortmentServiceEntry.getQuickButtonNumber());
//                ass.setQuickGroupName(assortmentServiceEntry.getQuickGroupName());
//                ass.setStockBalance(assortmentServiceEntry.getStockBalance());
//                ass.setStockBalanceDate(assortmentServiceEntry.getStockBalanceDate());
////                ass.setSaleStartTime(MainActivity.replaceDate(assortmentServiceEntry.getSaleStartTime()));
////                ass.setSaleEndTime(MainActivity.replaceDate(assortmentServiceEntry.getSaleEndTime()));
////                ass.setPriceLineStartDate(MainActivity.replaceDate(assortmentServiceEntry.getPriceLineStartDate()));
////                ass.setPriceLineEndDate(MainActivity.replaceDate(assortmentServiceEntry.getPriceLineEndDate()));
//
//                realm.insert(ass);
//            }
//
//            if(listGroup != null){
//                for(QuickGroup quickGroup : listGroup){
//                    QuickGroupRealm quickGroupRealm = new QuickGroupRealm();
//
//                    String nameGroup = quickGroup.getName();
//                    RealmList<String> assortment = new RealmList<>();
//                    assortment.addAll(quickGroup.getAssortmentID());
//
//                    quickGroupRealm.setGroupName(nameGroup);
//                    quickGroupRealm.setAssortmentId(assortment);
//
//                    realm.insert(quickGroupRealm);
//                }
//            }
//        }));
//
////        pDialog.dismiss();
//    }

    private static void postToast(final String message) {
        Toast.makeText(getInstance(), message, Toast.LENGTH_LONG).show();
    }

    private Thread.UncaughtExceptionHandler handleAppCrash = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            new RetrieveFeedTask().execute(ex.toString());
        }
    };

    public class RetrieveFeedTask extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... voids) {
            String version ="0.0";
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                version = pInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            String apiToken = "1155488985:AAEzmGo_NN8B1hVPNgLGv6eKZAB4Bi_vKuM";

            String chatId = "362622044";
            String text = "Android casa v:" + version + "     " + voids[0];

            String urlString = "https://api.telegram.org/bot" +apiToken +  "/sendMessage?chat_id="+ chatId + "&text=" + text;
            //https://api.telegram.org/bot664321744:AAGimqEuidlzO84qMoY1-_C-1OsNWRQ8FyM/sendMessage?chat_id=-1001349137188&amp&text=Hello+World
            urlString = String.format(urlString, apiToken, chatId, text);

            try {
                URL url = new URL(urlString);
                URLConnection conn = url.openConnection();
                InputStream is = new BufferedInputStream(conn.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
