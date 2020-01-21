package edi.md.androidcash;

/**
 * @author Datecs Ltd. Software Department
 */

import android.os.Environment;
import android.util.Log;

import com.datecs.fiscalprinter.SDK.FiscalDeviceV2;
import com.datecs.fiscalprinter.SDK.FiscalException;
import com.datecs.fiscalprinter.SDK.model.AL.DP25_AL;
import com.datecs.fiscalprinter.SDK.model.AL.FDModelDetector;
import com.datecs.fiscalprinter.SDK.model.AL.FP700_AL;
import com.datecs.fiscalprinter.SDK.model.AL.WP500_AL;
import com.datecs.fiscalprinter.SDK.model.DatecsFiscalDevice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import edi.md.androidcash.connectors.AbstractConnector;

public class PrinterManager {

    private static String sConnectorType;
    private String modelVendorName = "";

    public String getModelVendorName() {
        return modelVendorName;
    }

    public static DatecsFiscalDevice getFiscalDevice() {
        return fiscalDevice;
    }

    private static DatecsFiscalDevice fiscalDevice;

    private AbstractConnector mConnector;

    public static final PrinterManager instance;

    static {
        instance = new PrinterManager();
    }

    private PrinterManager() {
    }

    public static String getsConnectorType() {
        return sConnectorType;
    }

    /**
     * Depending on the response of the connected device, we create the corresponding instance of  Supported Device:
     * <p>
     * List of  all supported device by Protocol V2
     * <p>
     * DATECS FMP-350X
     * DATECS FMP-55X
     * DATECS FP-700X
     * DATECS WP-500X
     * DATECS WP-50X
     * DATECS DP-25X
     * DATECS DP-150X
     *
     * @param connector - Provides access to the input streaming stream
     * @throws IOException
     * @throws FiscalException
     */
    public void init(AbstractConnector connector) throws IOException, FiscalException {
        fiscalDevice = new DatecsFiscalDevice(256);
        mConnector = connector;
        sConnectorType = connector.getConnectorType();

        appendLog("Print manager: mConnector : " + connector);
        appendLog("Print manager: fiscalDevice : " + fiscalDevice);
        appendLog("Print manager: sConnectorType : " + sConnectorType);

        /**
         *  Datecs JavaSDK types of exceptions:
         *
         *   The FiscalException are throws in Datecs JavaSDK,
         *
         *  FiscalException of Status Bytes they depend on whether a status is marked as critical or not,
         *  the SDK method setIsStatusCritical() - enables SDK users to select Status of device
         *  to be a critical exception for their application or not.
         *  For example, Status of (Byte[0] Bit[3])-"No client display connected"
         *  may be a critical error in a POS application running in a store but
         *  if fiscal printer is an office application and no need of client display, this exception must to turned off.
         *
         *  FiscalException - Command Error
         *  These are all exceptions for which a Fiscal Device command can not be execute the request,
         *  after the execution of each command, the device returns a response contain error code,
         *  If the error code is different 0-OK, Datecs JavaSDK - converts the code into messages and added it to
         *
         *   Two setters define the content of the message:
         *   setThrowErrorCode() - add the error code.
         *   setThrowErrorMessage()-add the description of the error.
         *
         * Use fiscalDevice.getConnectedPrinterV2().setMsgSeparator(); to setup desired separator char or string,
         * by default separator is "\n\r"
         *
         * All messages are in English.
         */



        //Enables whether a status of the device is a critical exception for user application !
        FiscalDeviceV2.setIsStatusCritical(initCriticalStatus());



        /**
         *
         *     This instance is intended to determine the model of the connected device.
         *   In the Datecs JavaSDK for each model, the appropriate instance of a device is created for the use of precision settings.*
         *   If communication has already been established and this "connection" is successful, can be used for later communication with the device.
         *   Use:
         *   deviceClassName(datecsBGRmodelV1.getTransportProtocol()); to create fiscal device instance;
         *   To work with a specified device and not to use (FDModelDetectorV1), create the device with a constructor of the type:
         *   deviceClassName(InputStream in, OutputStream out)
         *
         */
        FDModelDetector datecsBGRmodelV2 = new FDModelDetector(mConnector.getInputStream(), mConnector.getOutputStream());
//
//
//        appendLog("Print manager:  datecsBGRmodelV2: " +datecsBGRmodelV2);
//        appendLog("Print manager:  datecsBGRmodelV2 inp out stream: " + mConnector.getInputStream().toString() + mConnector.getOutputStream().toString());
//        appendLog("Print manager: modelVendorName: " + datecsBGRmodelV2.detectConnectedModel());
//

        modelVendorName = datecsBGRmodelV2.detectConnectedModel();


        switch (modelVendorName) {
            case "FMP-350":
                appendLog("Print manager: FMP-350" + datecsBGRmodelV2.getTransportProtocol());
               // fiscalDevice.setConnectedModel(new FMP350_AL(datecsBGRmodelV2.getTransportProtocol()));
                break;

            case "DP-25":
                appendLog("Print manager: DP-25 class :" + datecsBGRmodelV2.getTransportProtocol());
                fiscalDevice.setConnectedModel(new DP25_AL(datecsBGRmodelV2.getTransportProtocol()));
                break;

            case "DP-150":
                fiscalDevice.setConnectedModel(new DP25_AL(datecsBGRmodelV2.getTransportProtocol()));
                appendLog("Print manager: DP-150 class:" + datecsBGRmodelV2.getTransportProtocol());
                break;

            case "FMP-55":
                appendLog("Print manager: FMP-55" + datecsBGRmodelV2.getTransportProtocol());
               // fiscalDevice.setConnectedModel(new FMP55_AL(datecsBGRmodelV2.getTransportProtocol()));
                break;

            case "FP-700":
               fiscalDevice.setConnectedModel(new FP700_AL(datecsBGRmodelV2.getTransportProtocol()));
                appendLog("Print manager: FP-700 class: " + datecsBGRmodelV2.getTransportProtocol());
                break;

            case "WP-500":
                appendLog("Print manager: WP-500" + datecsBGRmodelV2.getTransportProtocol());
                fiscalDevice.setConnectedModel(new WP500_AL(datecsBGRmodelV2.getTransportProtocol()));
                break;
            case "WP-50":
                appendLog("Print manager: WP-50 " + datecsBGRmodelV2.getTransportProtocol());
               // fiscalDevice.setConnectedModel(new WP50_AL(datecsBGRmodelV2.getTransportProtocol()));
                break;

            default:
                modelVendorName = "Unsupported model:" + modelVendorName;
                appendLog("Unsupported model" + modelVendorName);
                break;
        }


    }


    /**
     * Enables whether a status is a critical exception
     *
     * @return
     */
    private boolean[][] initCriticalStatus() {

        boolean[][] myCriticalStatusSet = new boolean[8][8];
        myCriticalStatusSet[0][7] = false; //"For internal use  1."
        myCriticalStatusSet[0][6] = true;////"Cover is Open"
        myCriticalStatusSet[0][5] = true; // General error - this is OR of all errors marked with #.
        myCriticalStatusSet[0][4] = true;////"# Failure in printing mechanism"
        myCriticalStatusSet[0][3] = false;//"Always 0."
        myCriticalStatusSet[0][2] = false;//"The real time clock is not synchronized"
        myCriticalStatusSet[0][1] = true;//"# Command code is invalid"
        myCriticalStatusSet[0][0] = true;//"# Syntax error"
        myCriticalStatusSet[1][7] = false;//"For internal use  1"
        myCriticalStatusSet[1][6] = false;//"For internal use  0"
        myCriticalStatusSet[1][5] = false;//"For internal use  0"
        myCriticalStatusSet[1][4] = false;//"For internal use  0"
        myCriticalStatusSet[1][3] = false;//"For internal use  0"
        myCriticalStatusSet[1][2] = false;//"For internal use  0"
        myCriticalStatusSet[1][1] = true;//"# Command is not permitted"
        myCriticalStatusSet[1][0] = true;//"# Overflow during command execution"
        myCriticalStatusSet[2][7] = false;//"For internal use  1."
        myCriticalStatusSet[2][6] = false;//"For internal use  0."
        myCriticalStatusSet[2][5] = false;//"Non fiscal receipt is open"
        myCriticalStatusSet[2][4] = false;//"EJ nearly full."
        myCriticalStatusSet[2][3] = false;//"Fiscal receipt is Open."
        myCriticalStatusSet[2][2] = true; //"EJ full."
        myCriticalStatusSet[2][1] = false; //"Near paper end."
        myCriticalStatusSet[2][0] = true;  //"#End of paper."
        myCriticalStatusSet[3][7] = false; //"For internal use  1"
        myCriticalStatusSet[3][6] = false; //"For internal use  0"
        myCriticalStatusSet[3][5] = false; //"For internal use  0"
        myCriticalStatusSet[3][4] = false; //"For internal use  0"
        myCriticalStatusSet[3][3] = false; //"For internal use  0"
        myCriticalStatusSet[3][2] = false; //"For internal use  0"
        myCriticalStatusSet[3][1] = false; //"For internal use  0"
        myCriticalStatusSet[3][0] = false; //"For internal use  0"
        myCriticalStatusSet[4][7] = false;//"For internal use  1"
        myCriticalStatusSet[4][6] = true;//"Fiscal memory is not found or damaged"
        myCriticalStatusSet[4][5] = true; //"OR of all errors marked with *  Bytes 4 - 5"
        myCriticalStatusSet[4][4] = true;//"* Fiscal memory is full."
        myCriticalStatusSet[4][3] = false; //"There is space for less then 60 reports in Fiscal memory."
        myCriticalStatusSet[4][2] = false; //"Serial number and number of FM are not set"
        myCriticalStatusSet[4][1] = false;//"Tax number is not set"
        myCriticalStatusSet[4][0] = true;//"* Error accessing data in the FM"
        myCriticalStatusSet[5][7] = false;//"For internal use  1."
        myCriticalStatusSet[5][6] = false;//"For internal use  0."
        myCriticalStatusSet[5][5] = false; //"For internal use  0."
        myCriticalStatusSet[5][4] = false; //"VAT are not set at least once."
        myCriticalStatusSet[5][3] = false;//"Device is not fiscalized."
        myCriticalStatusSet[5][2] = false; //"For internal use  0."
        myCriticalStatusSet[5][1] = true;//"FM is not formated."
        myCriticalStatusSet[5][0] = false;//"For internal use  0"
        myCriticalStatusSet[6][7] = false;//"For internal use  1"
        myCriticalStatusSet[6][6] = false;//"For internal use  0"
        myCriticalStatusSet[6][5] = false;//"For internal use  0"
        myCriticalStatusSet[6][4] = false;//"For internal use  0"
        myCriticalStatusSet[6][3] = false;//"For internal use  0"
        myCriticalStatusSet[6][2] = false;//"For internal use  0"
        myCriticalStatusSet[6][1] = false;//"For internal use  0"
        myCriticalStatusSet[6][0] = false;//"For internal use  0"

        return myCriticalStatusSet;

    }

    public void close() {
        if (mConnector != null) {
            try {
                mConnector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void appendLog(String text) {
        File file = null;
        File teste = new File(Environment.getExternalStorageDirectory(),"/IntelectSoft");
        if (!teste.mkdirs()) {
            Log.e("LOG TAG", "Directory not created");
        }
        file = new File(Environment.getExternalStorageDirectory(),"/IntelectSoft/CashNew_log.txt");
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
            String err = sDateInChisinau+ ": " + text;
            buf.append(err);
            //buf.write(text);
            buf.newLine();
            buf.close(); }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
