package edi.md.androidcash.Utils;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import edi.md.androidcash.connectors.AbstractConnector;
import edi.md.androidcash.connectors.UsbDeviceConnector;

public class GlobalReceiver extends BroadcastReceiver {
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    public static final int DATECS_USB_VID = 65520;
    public static final int FTDI_USB_VID = 1027;
    private Context context;
    private UsbManager mManager;
    private PendingIntent mPermissionIntent;

    public GlobalReceiver(Context context) {
        this.context = context;
        mManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Android global receiver: " + intent.getAction() , Toast.LENGTH_SHORT).show();

        try {
            if (intent != null) {
                Locale.Category category = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    category = (Locale.Category) intent.getSerializableExtra("category");
                }
                Toast.makeText(context, "Android:category.name() " + category.name(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("catch", e.toString());
        }

        String action = intent.getAction();

        if (ACTION_USB_PERMISSION.equals(action)) {

            synchronized (this) {

                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                if (device.getManufacturerName().equals("Datecs")) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                        AbstractConnector connector = new UsbDeviceConnector(context, mManager, device);

                        HashMap<String, UsbDevice> deviceList = mManager.getDeviceList();

                        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                        while (deviceIterator.hasNext()) {
                            UsbDevice devices = deviceIterator.next();

                            if (devices.getManufacturerName().equals("ACS")) {
                                if (!mManager.hasPermission(devices)) {
                                    mManager.requestPermission(devices, mPermissionIntent);
                                }
                            }

                        }
//                        deviceConnect(connector);
                    } else {
                        postToast("Permission denied for device " + device.getDeviceName());
                    }
                }
                if (device.getManufacturerName().equals("ACS")) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                        // Open reader
                        postToast("Opening reader: " + device.getDeviceName() + "...");
                        new OpenTask().execute(device);

                        HashMap<String, UsbDevice> deviceList = mManager.getDeviceList();

                        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                        while (deviceIterator.hasNext()) {
                            UsbDevice devices = deviceIterator.next();

                            if ((devices.getVendorId() == DATECS_USB_VID) || (devices.getVendorId() == FTDI_USB_VID) && (devices.getManufacturerName().equals("Datecs"))) {
                                if (!mManager.hasPermission(devices)) {
                                    mManager.requestPermission(devices, mPermissionIntent);
                                }
                            }
                        }

                    } else {

                        postToast("Permission denied for device " + device.getDeviceName());
                    }
                }
            }

        }
    }

    private class OpenTask extends AsyncTask<UsbDevice, Void, Exception> {

        @Override
        protected Exception doInBackground(UsbDevice... params) {

            Exception result = null;

            try {

//                mReader.open(params[0]);

            } catch (Exception e) {

                result = e;
            }

            return result;
        }

        @Override
        protected void onPostExecute(Exception result) {

            if (result != null) {

                postToast(result.toString());

            } else {

//                postToast("Reader name: " + mReader.getReaderName());
//
//                int numSlots = mReader.getNumSlots();
////                    postToast("Number of slots: " + numSlots);
//
//                // Add slot items
//                mSlotAdapter.clear();
//                for (int i = 0; i < numSlots; i++) {
//                    mSlotAdapter.add(Integer.toString(i));
//                }
            }
        }
    }

    private class CloseTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

//            mReader.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

        }

    }

    private void postToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
