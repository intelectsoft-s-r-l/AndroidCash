package edi.md.androidcash.DatcesNewFile.model;

import com.datecs.fiscalprinter.SDK.FiscalSocket;
import com.datecs.fiscalprinter.SDK.TransportProtocolV2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FDModelDetector extends TransportProtocolV2 {
    public FDModelDetector(InputStream in, OutputStream out) {
        super(in, out, 1251);
    }

    public FDModelDetector(InputStream in, OutputStream out, int encoding) {
        super(in, out, encoding);
    }

    public FDModelDetector(FiscalSocket socket, int encoding) {
        super(socket, encoding);
    }

    public FDModelDetector(FiscalSocket socket) {
        super(socket);
    }

    public String getVendorName() throws IOException {
        String res = this.customCommand(74, "");
        res = this.customCommand(90, "");
        String[] myOutputList = res.split(",", 10);
        return myOutputList[0];
    }

    public String detectConnectedModel() throws IOException {
        return this.getVendorName();
    }

    public TransportProtocolV2 getTransportProtocol() {
        return this;
    }
}