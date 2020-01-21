package edi.md.androidcash.DatcesNewFile;


import android.os.Environment;
import android.util.Log;

import com.datecs.fiscalprinter.SDK.AbstractTransportProtocol;
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

public class TransportProtocolV3 extends AbstractTransportProtocol {
    public static final int MAX_PACKET_SIZE = 512;
    public static final int MAX_DATA_SIZE = 488;
    private byte[] mSB = new byte[8];

    public TransportProtocolV3(InputStream in, OutputStream out, int encoding) {
        super(in, out, encoding);
        this.mPackageSequance = 32;
    }

    public TransportProtocolV3(FiscalSocket socket, int encoding) {
        super(socket, encoding);
        this.mPackageSequance = 32;
    }

    public TransportProtocolV3(FiscalSocket socket) {
        super(socket);
        this.mPackageSequance = 32;
    }

    public String customCommand(int command, String data) throws IOException {
        ++this.mPackageSequance;
        if (this.mPackageSequance > 127) {
            this.mPackageSequance = 32;
        }

        this.mSocket.clear();
        LOGGER.severe(String.format("> (%d) %s", command, data));
        appendLog("TransportProtocolV3: severe 1: "+  String.format("> (%d) %s", command, data));
        StopWatch go = new StopWatch();
        this.writePacket(command, data);
        String result = this.readPacket();
        appendLog("TransportProtocolV3: " + result);
        LOGGER.severe(String.format("< (%d) returned in %dms: \"%s\"", command, (int)go.getElapsedTime(), result));
        appendLog("TransportProtocolV3 severe 2: " + String.format("< (%d) returned in %dms: \"%s\"", command, (int)go.getElapsedTime(), result));
        return result;
    }

    protected void writePacket(int command, String data) throws IOException {
        for(int retry = 0; retry < 2; ++retry) {
            byte[] buf = new byte[488];
            int offs = 0;
            int crc = 0;
            int len = data != null ? data.length() : 0;
            if (len > 488) {
                throw new IllegalArgumentException("Lenght of the packet exceeds the limits!");
            }

            offs = offs + 1;
            buf[offs] = 1;

            int alllen = len + 32;
            alllen += 10;
            buf[offs++] = (byte)((alllen >> 12 & 15) + 48);
            buf[offs++] = (byte)((alllen >> 8 & 15) + 48);
            buf[offs++] = (byte)((alllen >> 4 & 15) + 48);
            buf[offs++] = (byte)((alllen >> 0 & 15) + 48);
            buf[offs++] = (byte)this.mPackageSequance;
            buf[offs++] = (byte)((command >> 12 & 15) + 48);
            buf[offs++] = (byte)((command >> 8 & 15) + 48);
            buf[offs++] = (byte)((command >> 4 & 15) + 48);
            buf[offs++] = (byte)((command >> 0 & 15) + 48);
            toAnsi(data, buf, offs, this.mEncoding);
            offs += len;
            buf[offs++] = 5;

            for(int i = 1; i < offs; ++i) {
                crc += buf[i] & 255;
            }

            buf[offs++] = (byte)((crc >> 12 & 15) + 48);
            buf[offs++] = (byte)((crc >> 8 & 15) + 48);
            buf[offs++] = (byte)((crc >> 4 & 15) + 48);
            buf[offs++] = (byte)((crc >> 0 & 15) + 48);
            buf[offs++] = 3;
            this.mSocket.write(buf, 0, offs);
            this.mSocket.flush();

            do {
                this.mSocket.read(buf, 0, 1, 6000);
            } while((buf[0] & 255) == 22);

            if (buf[0] != 21) {
                if (buf[0] != 1) {
                    throw new IOException("Invalid data received!");
                }

                return;
            }
        }

        throw new IOException("Invalid packet checksum!");
    }

    protected String readPacket() throws IOException {
        byte[] buf = new byte[488];
        int len = 0;
        int crc = 0;

        int b;
        int i;
        for(i = 0; i < 4; ++i) {
            b = this.mSocket.read(6000);
            crc += b;
            len = (len << 4) + b - 48;
        }

        len -= 51;
        b = this.mSocket.read(6000);
        crc += b;

        for(i = 0; i < 4; ++i) {
            b = this.mSocket.read(6000);
            crc += b;
        }

        this.mSocket.read(buf, 0, len, 6000);

        for(i = 0; i < len; ++i) {
            crc += buf[i] & 255;
        }

        b = this.mSocket.read(6000);
        if (b != 4) {
            throw new IOException("Invalid data received!");
        } else {
            crc += b;
            this.mSocket.read(this.mSB, 0, this.mSB.length, 6000);
            byte[] var9 = this.mSB;
            int var6 = var9.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                byte by = var9[var7];
                crc += by & 255;
            }

            b = this.mSocket.read(6000);
            if (b != 5) {
                throw new IOException("Invalid data received!");
            } else {
                crc += b;
                b = this.mSocket.read(6000);
                crc -= b - 48 << 12;
                b = this.mSocket.read(6000);
                crc -= b - 48 << 8;
                b = this.mSocket.read(6000);
                crc -= b - 48 << 4;
                b = this.mSocket.read(6000);
                crc -= b - 48;
                if (crc != 0) {
                    throw new IOException("Invalid CRC!");
                } else {
                    b = this.mSocket.read(6000);
                    if (b != 3) {
                        appendLog("Invalid data received!" );
                        throw new IOException("Invalid data received!");
                    } else {
                        appendLog("Read packet transportProtocolV3: " +toUnicode(buf, 0, len, this.mEncoding) );
                        return toUnicode(buf, 0, len, this.mEncoding);
                    }
                }
            }
        }
    }

    public boolean isStatusBitTriggered(int byteIndex, int bitIndex) {
        return (this.mSB[byteIndex] & 1 << bitIndex) > 0;
    }

    public byte[] getStatusBytes() {
        return this.mSB;
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