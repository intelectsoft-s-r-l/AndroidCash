/*
 * @author Datecs Ltd. Software Department
 */

package md.intelectsoft.salesepos.connectors;

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

public class UsbDeviceConnector extends AbstractConnector {

    private static final boolean DEBUG = false;
    private int FIRST_BYTE=0; //FTDI packets of 64 bytes which contain 2 status bytes and 62 data bytes.
    private class InputStreamImpl extends InputStream {
        private static final int TIMEOUT = 1000;
        
        private UsbDeviceConnection mConnection;
        private UsbEndpoint mEndPoint;
        private List<Byte> mDataBuffer;
        private String mLastError;

        public InputStreamImpl(UsbDeviceConnection conn, UsbEndpoint ep) {
            if (conn.equals(null))
                throw new NullPointerException("The 'conn' is null");
            
            if (ep == null) 
                throw new NullPointerException("The 'ep' is null");
            
            if (ep.getDirection() != UsbConstants.USB_DIR_IN)
                throw new IllegalArgumentException("The endpoint direction is incorrect");
            
            this.mConnection = conn;        
            this.mEndPoint = ep;        
            this.mDataBuffer = new LinkedList<Byte>();

            final Thread t = new Thread(new Runnable() {


                @Override
                public void run() {
                    byte[] tmp = new byte[64];

                    while (mLastError == null) {
                        long ms = System.currentTimeMillis() + TIMEOUT / 2;
                        int len = mConnection.bulkTransfer(mEndPoint, tmp, tmp.length, TIMEOUT);

                        if (len < 0) {
                            debug("Read bulkTransfer failed: " + len);

                            try {
                                if (ms > System.currentTimeMillis()) {
                                    mLastError = "Read failed";
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (len > 0) {
                            debug("Read " + len + " bytes");

                            synchronized (mDataBuffer) {
                                for (int i = FIRST_BYTE; i < len; i++) {
                                    mDataBuffer.add(tmp[i]);
                                }
                            }
                        }
                    }
                }

            });
            t.start();
        }

        @Override
        public int available() throws IOException {
            if (mLastError != null) {
                throw new IOException(mLastError);
            }        
            
            synchronized (mDataBuffer) {
                return mDataBuffer.size();
            }
        }

        @Override
        public void close() {
            mLastError = "The stream is closed";
        }
        
        @Override
        public int read() throws IOException {
            do {
                if (mLastError != null) {
                    throw new IOException(mLastError);
                }

                synchronized (mDataBuffer) {
                    int count = mDataBuffer.size();

                    if (count > 0) {
                        return mDataBuffer.remove(0) & 0xFF;
                    }
                }

                SystemClock.sleep(10);
            } while (true);
        }
        
        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            do {
                if (mLastError != null) {
                    throw new IOException(mLastError);
                }

                synchronized (mDataBuffer) {
                    int count = mDataBuffer.size();

                    if (count > 0) {
                        int chunkSize = Math.min(length, count);

                        for (int i = 0; i < chunkSize; i++) {
                            byte value = mDataBuffer.remove(0).byteValue();
                            buffer[offset + i] = value;
                        }

                        return chunkSize;
                    }
                }

                SystemClock.sleep(10);
            } while (true);
        }

        @Override
        public int read(byte[] buffer) throws IOException {
            return read(buffer, 0, buffer.length);
        }
    }

    private class OutputStreamImpl extends OutputStream {
        private UsbDeviceConnection mConnection;
        private UsbEndpoint mEndPoint;
        private byte[] mPacketBuffer;
        private int mPacketLength;  
        private String mLastError;
        
        public OutputStreamImpl(UsbDeviceConnection conn, UsbEndpoint ep) {
           if (conn == null) 
                throw new NullPointerException("The 'conn' is null");
            
            if (ep == null) 
                throw new NullPointerException("The 'ep' is null");
            
            if (ep.getDirection() != UsbConstants.USB_DIR_OUT)
                throw new IllegalArgumentException("The endpoint direction is incorrect");
                    
            this.mConnection = conn;        
            this.mEndPoint = ep;
            this.mPacketBuffer = new byte[2048];
            this.mPacketLength = 0;        
        }

        @Override
        public synchronized void write(int oneByte) throws IOException {
            if (mLastError != null) {
                throw new IOException(mLastError);
            }
            
            if (mPacketBuffer.length == mPacketLength) {
                flush();
            }
            
            mPacketBuffer[mPacketLength++] = (byte)oneByte;        
        }

        @Override
        public void close() {
            mLastError = "The stream is closed";
        }

        @Override
        public synchronized void flush() throws IOException {
            while (mPacketLength > 0) {
                if (mLastError != null) {
                    throw new IOException(mLastError);
                }
                
                int len = mConnection.bulkTransfer(mEndPoint, mPacketBuffer, mPacketLength, 100);
                
                if (len < 0) {
                    debug("Write bulkTransfer failed: " + len);
                    mLastError = "Write error " + len;
                } else {
                    mPacketLength -= len;
                    System.arraycopy(mPacketBuffer,  len, mPacketBuffer, 0, mPacketLength);
                }
            }
        }
        
    }
    
    private UsbManager mUsbManager;
    private UsbDevice mUsbDevice;
    private UsbDeviceConnection mDeviceConn;
    private UsbEndpoint[] mEndpoints;
    private InputStream mInputStream;
    private OutputStream mOutputStream;

    public UsbDeviceConnector(Context context, UsbManager manager, UsbDevice device) {
        super(context);
        this.mUsbManager = manager;
        this.mUsbDevice = device;
        this.mEndpoints = new UsbEndpoint[2];
    }

    private static void debug(String text) {
        if (DEBUG) {
            System.out.println("<UsbDeviceConnector> " + text);
        }
    }

    private UsbDeviceConnection openConnection(UsbDevice device, UsbEndpoint[] endpoints) throws IOException {
        // Can we connect to device ?
        appendLog("Open conn: " + device.getDeviceName());
        if (!mUsbManager.hasPermission(device)) {
            throw new IOException("Permission denied");
        }
                
       // Enumerate interfaces
            for (int i = 0; i <  device.getInterfaceCount(); i++) {
                final UsbInterface iface = device.getInterface(i);
                UsbEndpoint usbEpInp = null;
                UsbEndpoint usbEpOut = null;

                // Enumerate end points
            for (int j = 0; j < iface.getEndpointCount(); j++) {
                UsbEndpoint endpoint = iface.getEndpoint(j);
                
                // Check interface type
                if (endpoint.getType() != UsbConstants.USB_ENDPOINT_XFER_BULK) continue;
                                    
                if (endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                    usbEpInp = endpoint;
                }
                
                if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                    usbEpOut = endpoint;
                }
            }
            
            if (usbEpInp != null || usbEpOut != null) {
                final UsbDeviceConnection usbDevConn = mUsbManager.openDevice(device);
                appendLog("Product name : " + device.getProductName());
                if(device.getProductName().equals("FT232R USB UART"))
                {   FIRST_BYTE=2; //FTDI USB packets 2 status bytes and 62 data bytes, so first byte in packet is 2
                    // Configure FTDI port.
                    usbDevConn.controlTransfer(0x40, 0, 0, 0, null, 0, 0); // Reset
                    usbDevConn.controlTransfer(0x40, 0, 1, 0, null, 0, 0); // Clear Rx
                    usbDevConn.controlTransfer(0x40, 0, 2, 0, null, 0, 0); // Clear Tx
                    //usbDevConn.controlTransfer(0x40, 0x03, 0x4138, 0, null, 0, 0); // Set baud rate to 9600
                    usbDevConn.controlTransfer(0x40, 0x03, 0x001A, 0, null, 0, 0); // Set baud rate to 115200
                }

                appendLog("usb dev conn: " + usbDevConn);
                // Check connection
                if (usbDevConn == null) {
                    throw new IOException("Open failed");
                }

                if (!usbDevConn.claimInterface(iface, true)) {
                    usbDevConn.close();
                    throw new IOException("Access denied");
                }
                
                endpoints[0] = usbEpInp;
                endpoints[1] = usbEpOut;
                return usbDevConn;                   
            }            
        }
        
        throw new IOException("Open failed");
    }      

    public UsbDevice getDevice() {
        return mUsbDevice;
    }

    public synchronized void connect() throws IOException {
        mDeviceConn = openConnection(mUsbDevice, mEndpoints);
        mInputStream = null;
        mOutputStream = null;
        setConnectorType("USB");
    }
    
    public synchronized void close() {
        if (mDeviceConn != null) {
            try {
                mDeviceConn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public synchronized InputStream getInputStream() {
        if (mInputStream == null) {
            mInputStream = new InputStreamImpl(mDeviceConn, mEndpoints[0]);
        }
        
        return mInputStream;
    }
    
    public synchronized OutputStream getOutputStream() {
        if (mOutputStream == null) {
            mOutputStream = new OutputStreamImpl(mDeviceConn, mEndpoints[1]);
        }
        
        return mOutputStream;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof UsbDeviceConnector) {
            return mUsbDevice.equals(((UsbDeviceConnector)o).mUsbDevice);
        }

        return false;
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
            String err = sDateInChisinau+ ": USBDeviceConnector: " + text;
            buf.append(err);
            //buf.write(text);
            buf.newLine();
            buf.close(); }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
