package md.intelectsoft.salesepos.DatcesNewFile;

import android.os.Environment;
import android.util.Log;

import com.datecs.fiscalprinter.SDK.FiscalSocket;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MoelDetector extends TransportProtocolV3 {
    public MoelDetector(InputStream in, OutputStream out) {
        super(in, out, 1251);
    }

    public MoelDetector(InputStream in, OutputStream out, int encoding) {
        super(in, out, encoding);
    }

    public MoelDetector(FiscalSocket socket, int encoding) {
        super(socket, encoding);
    }

    public MoelDetector(FiscalSocket socket) {
        super(socket);
    }

    public String getVendorName() throws IOException {
        String res = this.customCommand(90, "");
        appendLog("Res comand 90:" + res);
        String[] myOutputList = res.split(",", 10);
        return myOutputList[0];
    }

    public String detectConnectedModel() throws IOException {
        return this.getVendorName();
    }

    public TransportProtocolV3 getTransportProtocol() {
        return this;
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
