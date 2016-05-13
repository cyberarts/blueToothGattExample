/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.example.android.bluetoothlegatt.libs.FileHelper;
import com.example.android.bluetoothlegatt.libs.XMLNode;
import com.example.android.bluetoothlegatt.libs.fileReader;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */

public class DeviceControlActivity extends Activity {
    String[] recvStr=new String[2];
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField,mCard_no_field;
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    public static final UUID TX_POWER_UUID = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
    public static final UUID TX_POWER_LEVEL_UUID = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID FIRMWARE_REVISON_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    /*
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
*/
    String setting_folder="/bleReader";
    String setting_file="/bleReader/device.txt";
    String savedName="";
    static Button readCardBtn=null;
    static Switch hexSwitch=null;
    int cmdReadCard[] = {0x04, 0x01, 0x16, (byte)0xE4 };

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "action:"+action);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                savedName=intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.d(TAG,"connected to "+savedName);
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
                String dir = Environment.getExternalStorageDirectory().toString();
                String path=dir+setting_file;
                boolean hasFile = FileHelper.checkExist(path);
                File settingFolder = new File(dir + setting_folder);
                if (!settingFolder.exists()) {
                    settingFolder.mkdir();
                    Log.d(TAG,"making dir"+dir + setting_folder);
                }
                if (hasFile) {
                    String myFileUrl = path;
                    File myFile = new File(myFileUrl);
                    if (myFile.exists()) {
                        fileReader f = new fileReader();
                        f.initParser();
                        f.openXMLfile(myFileUrl);
                       String devName= f.rootNode.getAttr("devName");
                        if(!devName.equals(savedName)) {
                            f.rootNode.setAttr("devName", savedName);
                            FileHelper.saveXML(path, f.rootNode);
                            Log.d(TAG, "updating deviceName");
                        }else{
                            Log.d(TAG, "same deviceName");
                        }
                    } else {
                        XMLNode node1=new XMLNode("dev");
                        node1.setAttr("devName",savedName);
                        FileHelper.saveXML(path, node1);
                        Log.d(TAG, "saving deviceName to new file:"+path);
                    }
                }else{
                    XMLNode node1=new XMLNode("dev");
                    node1.setAttr("devName",savedName);
                    FileHelper.saveXML(path, node1);
                    Log.d(TAG, "saving deviceName to new file:"+path);
                }
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
                enableTXNotification();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String text = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                recvStr[1]=recvStr[0];
                recvStr[0]=text;

                displayData(recvStr[1]+"\n"+recvStr[0]);
                String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                Log.d(TAG, "[" + currentDateTimeString + "] RX:" + text);
                byte[] bytes = intent.getByteArrayExtra(BluetoothLeService.EXTRA_BYTE);
                if (bytes.length >= 5){
                    if (bytes[2] == 0x16 && bytes[3] == 0) {
                        final StringBuilder stringBuilder = new StringBuilder(bytes.length-5);
                        for(int i=4;i<bytes.length-1;i++) {
                            stringBuilder.append(String.format("%02X ", bytes[i]));
                        }
                        displayData("card no.:"+stringBuilder.toString(),mCard_no_field);
                    }
                    bytes=null;
                }

            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);
                        }
                        return true;
                    }
                    return false;
                }
    };

    private void clearUI() {
        //mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }
    EditText mEditText;
    boolean useHex=true;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.button_control);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        mEditText=(EditText) this.findViewById(R.id.editText);
        // Sets up UI references.
        /*
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
    mConnectionState = (TextView) findViewById(R.id.connection_state);
    */
        mDataField = (TextView) findViewById(R.id.data_value);
        mCard_no_field= (TextView) findViewById(R.id.card_no);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        //if(readCardBtn==null) {
            readCardBtn = (Button) findViewById(R.id.readCardBtn);
            hexSwitch = (Switch) findViewById(R.id.hexSwitch);
            hexSwitch.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            useHex=isChecked;
                            mEditText.setText("");
                        }
                    }
            );
            readCardBtn.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    bleSendData(cmdReadCard);
                    Log.d(TAG,"read a card....");
                }
            });
        //}
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);

        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }
    private void displayData(String data,TextView mTextView) {
        if (data != null) {
            mTextView.setText(data);
        }
    }
    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void onClickWrite(View v){
        if(!useHex) {
            String tmpStr=mEditText.getText().toString();
            if(tmpStr.length()<=20) {
                bleSendData(tmpStr.getBytes());
            }else{
                byte[] tmpArr=tmpStr.getBytes();
                for(int i=0;i<tmpArr.length;i+=20){
                    if(tmpArr.length-i>=20){
                        byte[] tmpOut=new byte[20];
                        for(int j=0;j<20;j++){
                            tmpOut[j]=tmpArr[i+j] ;

                        }
                        bleSendData(tmpOut);
                        tmpOut=null;
                    }else{
                        byte[] tmpOut=new byte[tmpArr.length-i];
                        for(int j=0;j<tmpArr.length-i;j++){
                            tmpOut[j]=tmpArr[i+j];
                        }
                        bleSendData(tmpOut);
                        tmpOut=null;
                    }
                }
            }
        }else{
            String tmpStr=mEditText.getText().toString();
            String[] tmpArr=tmpStr.split(" ");
            int[] tmpData=new int[tmpArr.length];
            byte[] tmpBytes=new byte[tmpArr.length];
            String hexStr="";
            for(int i=0;i<tmpArr.length;i++){
                tmpData[i]=Integer.parseInt(tmpArr[i],16);
                tmpBytes[i]=(byte)(tmpData[i] & 0xff);
                hexStr+=Integer.toHexString(tmpData[i])+" ";
            }
            Log.d(TAG,hexStr);
            if(tmpArr.length<=20) {
                bleSendData(tmpBytes);
            }else{
                for(int i=0;i<tmpArr.length;i+=20){
                    if(tmpArr.length-i>=20){
                        byte[] tmpOut=new byte[20];
                        for(int j=0;j<20;j++){
                            tmpOut[j]=(byte)(tmpData[i+j] & 0xff);
                        }
                        bleSendData(tmpOut);
                    }else{
                        byte[] tmpOut=new byte[tmpArr.length-i];
                        for(int j=0;j<tmpArr.length-i;j++){
                            tmpOut[j]=(byte)(tmpData[i+j] & 0xff);
                        }
                        bleSendData(tmpOut);
                    }
                }
            }
            tmpStr=null;
            tmpArr=null;
            tmpBytes=null;
        }
    }

    public void onClickReadCard(View v){
        bleSendData(mEditText.getText().toString().getBytes());
    }
    public void bleSendData(int[] data){
        if(mBluetoothLeService != null) {
            BluetoothGatt mBluetoothGatt =mBluetoothLeService.mBluetoothGatt;
            BluetoothGattService RxService =mBluetoothGatt.getService(RX_SERVICE_UUID);
            if (RxService == null) {
                Log.d(TAG, "no service");
                return;
            }
            BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(RX_CHAR_UUID);
            if (RxChar == null) {
                Log.d(TAG, "no rx Charateristic");
                return;
            }
            byte dataBytes[]=new byte[data.length];
            String output="";
            for(int i=0;i<data.length;i++){
                dataBytes[i]=(byte)(data[i] & 0x000000ff);
                int k=dataBytes[i];
                output+=Integer.toHexString(dataBytes[i])+" ";
            }

            Log.d(TAG,"writing:"+output);
            RxChar.setValue(dataBytes);
            boolean status = mBluetoothGatt.writeCharacteristic(RxChar);
            Log.d(TAG, "write TXchar - status=" + status);
            //mBluetoothLeService.writeCustomCharacteristic(0xAA);
        }
    }
    public void bleSendData(byte[] data){
        if(mBluetoothLeService != null) {
            BluetoothGatt mBluetoothGatt =mBluetoothLeService.mBluetoothGatt;
            BluetoothGattService RxService =mBluetoothGatt.getService(RX_SERVICE_UUID);
            if (RxService == null) {
                Log.d(TAG, "no service");
                return;
            }
            BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(RX_CHAR_UUID);
            if (RxChar == null) {
                Log.d(TAG, "no rx Charateristic");
                return;
            }
            RxChar.setValue(data);
            boolean status = mBluetoothGatt.writeCharacteristic(RxChar);
            Log.d(TAG, "write TXchar - status=" + status);
            //mBluetoothLeService.writeCustomCharacteristic(0xAA);
        }
    }
    public void onClickRead(View v){
        if(mBluetoothLeService != null) {
            mBluetoothLeService.readCustomCharacteristic();
        }
    }
    public void enableTXNotification()
    {
    	/*
    	if (mBluetoothGatt == null) {
    		showMessage("mBluetoothGatt null" + mBluetoothGatt);
    		broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
    		return;
    	}
    		*/
        BluetoothGatt mBluetoothGatt =mBluetoothLeService.mBluetoothGatt;
        BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
        if (RxService == null) {
            //showMessage("Rx service not found!");
            //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            Log.d(TAG,"Rx service not found!");
            return;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);
        if (TxChar == null) {
            //showMessage("Tx charateristic not found!");
            //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            Log.d(TAG,"Tx charateristic not found!");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(TxChar,true);

        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);

    }
}
