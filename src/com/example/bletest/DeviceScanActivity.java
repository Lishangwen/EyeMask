package com.example.bletest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;



@SuppressLint("NewApi")
public class DeviceScanActivity extends Activity {
	
	 private int mConnectionState = STATE_DISCONNECTED;

	    private static final int STATE_DISCONNECTED = 0;
	    private static final int STATE_CONNECTING = 1;
	    private static final int STATE_CONNECTED = 2;
	    private static final int STATE_SCAN = 3;
	    private static final int STATE_ServicesDiscovered  = 4;
	    private static final int STATE_STOP_EXECUTE_DEVICE = 5;
	    private static final int STATE_EXECUTE_DEVICE = 6;
	    
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
	    
	    public final static String kServiceUUID = "713D0000-503E-4C75-BA94-3148F18D941E";
	  //0x713D0002503E4C75BA943148F18D941E
	    public final static String kUUIDReadDevStatus = "713D0002-503E-4C75-BA94-3148F18D941E";
	  //0x713D0003503E4C75BA943148F18D941E
	    public final static String kUUIDWriteCommand = "713D0003-503E-4C75-BA94-3148F18D941E";
	  //0x713D0003503E4C75BA943148F18D941E
	    public final static String kUUIDDeviceId = "713D0004-503E-4C75-BA94-3148F18D941E";


    private BluetoothAdapter mBluetoothAdapter  = null;
    private boolean mScanning = false;
    private Handler mHandler = new Handler();
    BluetoothDevice mDevice = null;
    BluetoothGatt mBluetoothGatt = null;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
	private static final int REQUEST_ENABLE_BT = 0;
	
	Button speedBut;
	Button modeBut;
	Button connBut;
	Button disconBut;
	Button executeBut;
	Button stopBut;
	Spinner modelSpiner;
	TextView msgTextView;
	TextView posTextView;
	EditText speedLevelEditText;
	SeekBar  speedSeekBar;
	private ArrayAdapter<CharSequence>sAd = null;
	private List<CharSequence>sDatas = null;
	private String[][]cDatas = new String[][]{{"1","2","3","4","5"},{"66","77","88"}};
	@SuppressLint("NewApi")
	
	void SetModel(int modelNo)
	{
		List<BluetoothGattService> listService = mBluetoothGatt.getServices();
    	UUID kUUIDDeviceCommand = UUID.fromString(kServiceUUID);
    	UUID writeUUID = UUID.fromString(kUUIDWriteCommand);
    	BluetoothGattService gattService       = null;
    	BluetoothGattCharacteristic btGattChar = null;
    	for (int i=0; i<listService.size(); i++)
    	{
    		BluetoothGattService tempgattService = listService.get(i);
    		UUID deviceUUID = tempgattService.getUuid();
        	if (deviceUUID.compareTo(kUUIDDeviceCommand) ==0) 
        	{
        		gattService = tempgattService;
        		break;
        	}
    	}
    	
    	if (gattService == null)
    	{
    		return;
    	}
    
    	List<BluetoothGattCharacteristic> gattChacterList = gattService.getCharacteristics();
    	for (int i=0; i<gattChacterList.size(); i++)
    	{
    		BluetoothGattCharacteristic tempbtGattChar = gattChacterList.get(i);
    		if (tempbtGattChar.getUuid().compareTo(writeUUID) == 0 )
    		{
    			btGattChar = tempbtGattChar;
    			break;
    		}
    		
    	}
    	if (btGattChar == null)
    	{
    		return;
    	}
    	mBluetoothGatt.setCharacteristicNotification(btGattChar, true);
		//byte[] model_buffer = new byte[6];
    	final byte[] model_buffer = {0,0,0,0,0};
		for (int i=0; i<5; i++)
		{
			model_buffer[i] = 0;
		}
		
		//0xFA, 0x05, 0x03, 0x00, 0xFC
		
		model_buffer[0] = (byte) 0xfa;
		model_buffer[1] = 0x05;
		model_buffer[2] = (byte)modelNo;
		model_buffer[3] = 0x00;
		model_buffer[4] = (byte) (model_buffer[0] ^ model_buffer[1] ^ model_buffer[2] ^ model_buffer[3]);
		//model_buffer[4] = (byte) 0xfc;
		
		mBluetoothGatt.beginReliableWrite();
		btGattChar.setValue(model_buffer);
		btGattChar.setWriteType(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE);
		Boolean writechar = mBluetoothGatt.writeCharacteristic(btGattChar);
		mBluetoothGatt.readCharacteristic(btGattChar);
		Log.i("writechar", writechar.toString());
	}
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ble_display);
		msgTextView = (TextView)this.findViewById(R.id.msg_textView);
		posTextView = (TextView)this.findViewById(R.id.pos_textView);
		speedSeekBar = (SeekBar)this.findViewById(R.id.speed_seekBar);
		speedSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				int mSpeedPos = seekBar.getProgress(); 
				posTextView.setText(String.valueOf(mSpeedPos));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				Log.i("modelSpiner", "onStartTrackingTouch");
			}

			@SuppressLint("NewApi")
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				Log.i("modelSpiner", "onStopTrackingTouch");
				if (mBluetoothGatt == null)
				{
					return;
				}
				int nSpeedPos = seekBar.getProgress(); 
				
	        	List<BluetoothGattService> listService = mBluetoothGatt.getServices();
	        	UUID kUUIDDeviceCommand = UUID.fromString(kServiceUUID);
	        	UUID writeUUID = UUID.fromString(kUUIDWriteCommand);
	        	BluetoothGattService gattService       = null;
	        	BluetoothGattCharacteristic btGattChar = null;
	        	for (int i=0; i<listService.size(); i++)
	        	{
	        		BluetoothGattService tempgattService = listService.get(i);
	        		UUID deviceUUID = tempgattService.getUuid();
		        	if (deviceUUID.compareTo(kUUIDDeviceCommand) ==0) 
		        	{
		        		gattService = tempgattService;
		        		break;
		        	}
	        	}
	        	
	        	if (gattService == null)
	        	{
	        		return;
	        	}
	        
	        	List<BluetoothGattCharacteristic> gattChacterList = gattService.getCharacteristics();
	        	for (int i=0; i<gattChacterList.size(); i++)
	        	{
	        		BluetoothGattCharacteristic tempbtGattChar = gattChacterList.get(i);
	        		if (tempbtGattChar.getUuid().compareTo(writeUUID) == 0 )
	        		{
	        			btGattChar = tempbtGattChar;
	        			break;
	        		}
	        	}
	        	if (btGattChar == null)
	        	{
	        		return;
	        	}
	        	mBluetoothGatt.setCharacteristicNotification(btGattChar, true);
	

				byte[] speedbuffer = {0,0,0,0,0};
				for (int i=0; i<5; i++)
				{
					speedbuffer[i] = 0;
				}
				// middle speed 0xFA, 0x07, 0x32, 0x00, 0xCF
				
				speedbuffer[0] = (byte)0xfa;
				speedbuffer[1] = 0x07;
				speedbuffer[2] = (byte)nSpeedPos;
				speedbuffer[3] = 0x00;
				speedbuffer[4] = (byte)(speedbuffer[0] ^ speedbuffer[1] ^ speedbuffer[2] ^ speedbuffer[3]);
				//speedbuffer[4] = (byte) 0xcf;
				
				btGattChar.setValue(speedbuffer);
				
				Boolean writechar = mBluetoothGatt.writeCharacteristic(btGattChar);
				Log.i("writechar2", writechar.toString());
				btGattChar.setWriteType(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE);
				
			}});
		
		
		modelSpiner = (Spinner)this.findViewById(R.id.model_spinner);
		modelSpiner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@SuppressLint("NewApi")
			@Override
			public void onItemSelected(AdapterView<?> ad, View v, int index, long id) {

				String prvi = ad.getItemAtPosition(index).toString();
				Log.i("modelSpiner", prvi);
				
				if (mBluetoothGatt == null)
				{
					return;
				}
				
				SetModel(index);
			}
			@Override
			public void onNothingSelected(AdapterView<?> ad) {}
		});
		
		stopBut = (Button)this.findViewById(R.id.stop_but);
		stopBut.setOnClickListener(new View.OnClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				if (mBluetoothGatt == null)
				{
					return ;
				}
				SetModel(0);
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
         	   	Message msg = new Message();          
        	    msg.what = STATE_STOP_EXECUTE_DEVICE;
        	    mHandler1.sendMessage(msg);
				
			}
		});
		
		executeBut = (Button)this.findViewById(R.id.execute_but);
		executeBut.setOnClickListener(new View.OnClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				if (mBluetoothGatt != null)
				{
					Boolean execWrite = mBluetoothGatt.executeReliableWrite();
					Log.i("execWrite", execWrite.toString());
				}
         	   	Message msg = new Message();          
        	    msg.what = STATE_EXECUTE_DEVICE;
        	    mHandler1.sendMessage(msg);
				
			}
		});
		
		
		
		disconBut = (Button)this.findViewById(R.id.disconn_but);
		disconBut.setVisibility(View.INVISIBLE);
		disconBut.setOnClickListener(new View.OnClickListener() {
			//******************************************************
			// Method:     onClick
			// Access:     public 
			// Returns:    void
			// Parameter:  
			// Note:	   关闭通信
			// Author      muzongcun  2012/11/12 create
			//*******************************************************
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				SetModel(0);
				if (mBluetoothGatt != null)
				{
					Boolean execWrite = mBluetoothGatt.executeReliableWrite();
					Log.i("execWrite", execWrite.toString());
				}
				
				SetModel(0);
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
         	   	Message msg = new Message();          
        	    msg.what = STATE_DISCONNECTED;
        	    mHandler1.sendMessage(msg);
				close();		
			}
		});
		
		connBut = (Button)this.findViewById(R.id.conn_but);
		//******************************************************
		// Method:     onClick
		// Access:     public 
		// Returns:    void
		// Parameter:  
		// Note:	   连接蓝牙设备
		// Author      muzongcun  2012/11/12 create
		//*******************************************************
		connBut.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				scanLeDevice(true);
			}
		});
		
		modeBut = (Button)this.findViewById(R.id.mode_but);
		modeBut.setVisibility(View.INVISIBLE);
		//******************************************************
		// Method:     onClick
		// Access:     public 
		// Returns:    void
		// Parameter:  
		// Note:	  发送模式
		// Author      muzongcun  2012/11/12 create
		//*******************************************************
		modeBut.setOnClickListener(new View.OnClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
	        	List<BluetoothGattService> listService = mBluetoothGatt.getServices();
	        	UUID kUUIDDeviceCommand = UUID.fromString(kServiceUUID);
	        	UUID writeUUID = UUID.fromString(kUUIDWriteCommand);
	        	BluetoothGattService gattService       = null;
	        	BluetoothGattCharacteristic btGattChar = null;
	        	for (int i=0; i<listService.size(); i++)
	        	{
	        		BluetoothGattService tempgattService = listService.get(i);
	        		UUID deviceUUID = tempgattService.getUuid();
		        	if (deviceUUID.compareTo(kUUIDDeviceCommand) ==0) 
		        	{
		        		gattService = tempgattService;
		        		break;
		        	}
	        	}
	        	
	        	if (gattService == null)
	        	{
	        		return;
	        	}
	        
	        	List<BluetoothGattCharacteristic> gattChacterList = gattService.getCharacteristics();
	        	for (int i=0; i<gattChacterList.size(); i++)
	        	{
	        		BluetoothGattCharacteristic tempbtGattChar = gattChacterList.get(i);
	        		if (tempbtGattChar.getUuid().compareTo(writeUUID) == 0 )
	        		{
	        			btGattChar = tempbtGattChar;
	        			break;
	        		}
	        		
	        	}
	        	if (btGattChar == null)
	        	{
	        		return;
	        	}
	        	mBluetoothGatt.setCharacteristicNotification(btGattChar, true);
				//byte[] model_buffer = new byte[6];
	        	final byte[] model_buffer = {0,0,0,0,0};
				for (int i=0; i<5; i++)
				{
					model_buffer[i] = 0;
				}
				
				//0xFA, 0x05, 0x03, 0x00, 0xFC
				
				model_buffer[0] = (byte) 0xfa;
				model_buffer[1] = 0x05;
				model_buffer[2] = 0x03;
				model_buffer[3] = 0x00;
				//model_buffer[4] = (byte) (model_buffer[0] ^ model_buffer[1] ^ model_buffer[2] ^ model_buffer[3]);
				model_buffer[4] = (byte) 0xfc;
				
				mBluetoothGatt.beginReliableWrite();
				btGattChar.setValue(model_buffer);
				btGattChar.setWriteType(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE);
				Boolean writechar = mBluetoothGatt.writeCharacteristic(btGattChar);
				mBluetoothGatt.readCharacteristic(btGattChar);
				Log.i("writechar", writechar.toString());
			}
		});
		
		speedBut = (Button)this.findViewById(R.id.speed_but);
		speedBut.setVisibility(View.INVISIBLE);
		speedBut.setOnClickListener(new View.OnClickListener() {
			//******************************************************
			// Method:     onClick
			// Access:     public 
			// Returns:    void
			// Parameter:  
			// Note:	  发送速度
			// Author      muzongcun  2012/11/12 create
			//*******************************************************
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				
	        	List<BluetoothGattService> listService = mBluetoothGatt.getServices();
	        	UUID kUUIDDeviceCommand = UUID.fromString(kServiceUUID);
	        	UUID writeUUID = UUID.fromString(kUUIDWriteCommand);
	        	BluetoothGattService gattService       = null;
	        	BluetoothGattCharacteristic btGattChar = null;
	        	for (int i=0; i<listService.size(); i++)
	        	{
	        		BluetoothGattService tempgattService = listService.get(i);
	        		UUID deviceUUID = tempgattService.getUuid();
		        	if (deviceUUID.compareTo(kUUIDDeviceCommand) ==0) 
		        	{
		        		gattService = tempgattService;
		        		break;
		        	}
	        	}
	        	
	        	if (gattService == null)
	        	{
	        		return;
	        	}
	        
	        	List<BluetoothGattCharacteristic> gattChacterList = gattService.getCharacteristics();
	        	for (int i=0; i<gattChacterList.size(); i++)
	        	{
	        		BluetoothGattCharacteristic tempbtGattChar = gattChacterList.get(i);
	        		if (tempbtGattChar.getUuid().compareTo(writeUUID) == 0 )
	        		{
	        			btGattChar = tempbtGattChar;
	        			break;
	        		}
	        	}
	        	if (btGattChar == null)
	        	{
	        		return;
	        	}
	        	mBluetoothGatt.setCharacteristicNotification(btGattChar, true);

				byte[] speedbuffer = {0,0,0,0,0};
				for (int i=0; i<5; i++)
				{
					speedbuffer[i] = 0;
				}
				// middle speed 0xFA, 0x07, 0x32, 0x00, 0xCF
				
				speedbuffer[0] = (byte)0xfa;
				speedbuffer[1] = 0x07;
				speedbuffer[2] = 0x32;
				speedbuffer[3] = 0x00;
				//speedbuffer[4] = (byte)(speedbuffer[0] ^ speedbuffer[1] ^ speedbuffer[2] ^ speedbuffer[3]);
				speedbuffer[4] = (byte) 0xcf;
				
				btGattChar.setValue(speedbuffer);
				
				Boolean writechar = mBluetoothGatt.writeCharacteristic(btGattChar);
				Log.i("writechar2", writechar.toString());
				btGattChar.setWriteType(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE);
				//Boolean execWrite = mBluetoothGatt.executeReliableWrite();
				//mBluetoothGatt.beginReliableWrite();
				//Log.i("execWrite2", execWrite.toString());
			}
		});

		
	}
	//******************************************************
	// Method:     scanLeDevice
	// Access:     public 
	// Returns:    void
	// Parameter:  
	// Note:	  开始扫描设备
	// Author      muzongcun  2012/11/12 create
	//*******************************************************
    @SuppressLint("NewApi")
	private void scanLeDevice(final boolean enable) {
    	
		// Use this check to determine whether BLE is supported on the device. Then
		// you can selectively disable BLE-related features.
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
		    Toast.makeText(this, "no support device", Toast.LENGTH_SHORT).show();
		    finish();
		}
		
		// Initializes Bluetooth adapter.
		final BluetoothManager bluetoothManager =
		        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		 mBluetoothAdapter = bluetoothManager.getAdapter();
		 
		// Ensures Bluetooth is available on the device and it is enabled. If not,
		// displays a dialog requesting user permission to enable Bluetooth.
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
    	
        if (enable)
        {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() 
            {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
     
    }
    

    // Device scan callback.
    @SuppressLint("NewApi")
	private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
    	//******************************************************
    	// Method:     onLeScan
    	// Access:     public 
    	// Returns:    void
    	// Parameter:  
    	// Note:	  扫描设备回调函数
    	// Author      muzongcun  2012/11/12 create
    	//*******************************************************
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                byte[] scanRecord) {
            runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   //mLeDeviceListAdapter.addDevice(device);
                  // mLeDeviceListAdapter.notifyDataSetChanged();
            	   mDevice = device;
            	   Log.i("SCAN", mDevice.getName());
            	   Message msg = new Message();
            	   msg.obj = mDevice.getName();
            	   msg.what = STATE_SCAN;
            	   mHandler1.sendMessage(msg);
            	
               }
           });
       }
    };
	//******************************************************
	// Method:     onLeScan
	// Access:     public 
	// Returns:    void
	// Parameter:  
	// Note:	   回调消息处理
	// Author      muzongcun  2012/11/12 create
	//*******************************************************
	protected Handler mHandler1 = new Handler()
	{         
		// @Override         
		public void handleMessage(Message msg) 
		{         
			super.handleMessage(msg);
			switch(msg.what)
			{
			case STATE_SCAN:
				String deviceName = (String)msg.obj;
				if (deviceName.equalsIgnoreCase("Biscuit"))
				{
					msgTextView.setText("scan name of device is: "+deviceName);
					connectGatt();
				}
				
				break;
			case STATE_CONNECTING:
				break;
			case STATE_CONNECTED:
				msgTextView.setText("Connected to GATT server,Attempting to start service discovery");
				break;
			case STATE_DISCONNECTED:
				msgTextView.setText("Disconnected from GATT server.");
				break;
			case STATE_ServicesDiscovered:
				msgTextView.setText("Services Discovered");
				break;
			case STATE_STOP_EXECUTE_DEVICE:
				msgTextView.setText("Stop Execute Device!");
				if (mBluetoothGatt != null)
				{
					Boolean execWrite = mBluetoothGatt.executeReliableWrite();
					Log.i("execWrite", execWrite.toString());
				}
				break;
			case STATE_EXECUTE_DEVICE:
				msgTextView.setText("Start Execute Device!");
				break;
				
			default:
				break;
			}
			
		}
	}; 
	//******************************************************
	// Method:     connectGatt
	// Access:     public 
	// Returns:    void
	// Parameter:  
	// Note:	    连接蓝牙设备
	// Author      muzongcun  2012/11/12 create
	//*******************************************************
	@SuppressLint("NewApi")
	void connectGatt()
	{
		mBluetoothGatt = mDevice.connectGatt(this, false, mGattCallback);
		
	}
	
	//******************************************************
	// Method:     BluetoothGattCallback
	// Access:     public 
	// Returns:    void
	// Parameter:  
	// Note:	    Various callback methods defined by the BLE API.
	// Author      muzongcun  2012/11/12 create
	//*******************************************************
    // 
    @SuppressLint("NewApi")
	private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
        @SuppressLint("NewApi")
		@Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                int newState) {

            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                //broadcastUpdate(intentAction);
                Log.i("conn", "Connected to GATT server.");
                Log.i("conn", "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());
         	   	Message msg = new Message();
        
        	    msg.what = STATE_CONNECTED;
        	    mHandler1.sendMessage(msg);
                
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i("disconn", "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
                Message msg = new Message();
        	    msg.what = STATE_DISCONNECTED;
        	    mHandler1.sendMessage(msg);
            }
        }
    	//******************************************************
    	// Method:     onServicesDiscovered
    	// Access:     public 
    	// Returns:    void
    	// Parameter:  
    	// Note:	     New services discovered.
    	// Author      muzongcun  2012/11/12 create
    	//*******************************************************
        @Override
        //
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        	
        	Log.i("discovered", "my onServicesDiscovered received: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                Message msg = new Message();
        	    msg.what = STATE_ServicesDiscovered;
        	    mHandler1.sendMessage(msg);
        	    //*************************************************************/
        	    //*************************************************************/
        	    //*************************************************************/
        	    //*************************************************************/
        	    
            	List<BluetoothGattService> listService = gatt.getServices();
	        	UUID kUUIDDeviceCommand = UUID.fromString(kServiceUUID);
	        	UUID readUUID = UUID.fromString(kUUIDReadDevStatus);
	        	BluetoothGattService gattService       = null;
	        	BluetoothGattCharacteristic btGattChar = null;
	        	for (int i=0; i<listService.size(); i++)
	        	{
	        		BluetoothGattService tempgattService = listService.get(i);
	        		UUID deviceUUID = tempgattService.getUuid();
		        	if (deviceUUID.compareTo(kUUIDDeviceCommand) ==0) 
		        	{
		        		gattService = tempgattService;
		        		break;
		        	}
	        	}
	        	
	        	if (gattService == null)
	        	{
	        		return;
	        	}
	        
	        	List<BluetoothGattCharacteristic> gattChacterList = gattService.getCharacteristics();
	        	for (int i=0; i<gattChacterList.size(); i++)
	        	{
	        		BluetoothGattCharacteristic tempbtGattChar = gattChacterList.get(i);
	        		if (tempbtGattChar.getUuid().compareTo(readUUID) == 0 )
	        		{
	        			btGattChar = tempbtGattChar;
	        			break;
	        		}
	        		
	        	}
	        	if (btGattChar == null)
	        	{
	        		return;
	        	}
	        	
	        	byte []readValue = btGattChar.getValue();
	        	//Log.i("ReadValue", readValue.toString());
	        	
        	    //*************************************************************/
        	    //*************************************************************/
        	    //*************************************************************/
        	    //*************************************************************/
            } else {
                Log.i("discovered", "onServicesDiscovered received: " + status);
            }
        }
    	//******************************************************
    	// Method:     onCharacteristicRead
    	// Access:     public 
    	// Returns:    void
    	// Parameter:  
    	// Note:	  Result of a characteristic read operation
    	// Author      muzongcun  2012/11/12 create
    	//*******************************************************
        @Override
        // 
        public void onCharacteristicRead(BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic,
                int status) {
        	Log.i("onCharacteristicRead", "onCharacteristicRead: " + status + characteristic);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            	Log.i("discovered", "onServicesDiscovered received: " + status + characteristic);
            }
        }
    	//******************************************************
    	// Method:     onDescriptorWrite
    	// Access:     public 
    	// Returns:    void
    	// Parameter:  
    	// Note:	  Result of a characteristic read operation
    	// Author      muzongcun  2012/11/12 create
    	//*******************************************************
        @Override
        public void  onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) 
        {
        	Log.i("onDescriptorWrite", "onDescriptorWrite: " + status + descriptor);
        }
    	//******************************************************
    	// Method:     onCharacteristicChanged
    	// Access:     public 
    	// Returns:    void
    	// Parameter:  
    	// Note:	  Result of a characteristic read operation
    	// Author      muzongcun  2012/11/12 create
    	//*******************************************************
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic) {
           	Log.i("onCharacteristicChanged", "onCharacteristicChanged");
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
        
    };
	private Object UUID_HEART_RATE_MEASUREMENT;

	private Object UUID_HEART_RATE_MEASUREMENT1;
    
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    @SuppressLint("NewApi")
	private void broadcastUpdate(final String action,
            final BluetoothGattCharacteristic characteristic) {
    		final Intent intent = new Intent(action);

    		// This is special handling for the Heart Rate Measurement profile. Data
    		// parsing is carried out as per profile specifications.
    		UUID uid = characteristic.getUuid();
    		Log.i("uuid", uid.toString());
    		if (UUID_HEART_RATE_MEASUREMENT1.equals(characteristic.getUuid())) {
    			int flag = characteristic.getProperties();
    			int format = -1;
    			if ((flag & 0x01) != 0) {
    				format = BluetoothGattCharacteristic.FORMAT_UINT16;
    				Log.d("broadcastUpdate", "Heart rate format UINT16.");
    			} else {
    				format = BluetoothGattCharacteristic.FORMAT_UINT8;
    				Log.d("broadcastUpdate", "Heart rate format UINT8.");
    			}
    			final int heartRate = characteristic.getIntValue(format, 1);
    			Log.d("", String.format("Received heart rate: %d", heartRate));
    			//intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
    		} else {
    			// For all other profiles, writes the data formatted in HEX.
    			final byte[] data = characteristic.getValue();
    			if (data != null && data.length > 0) {
    				final StringBuilder stringBuilder = new StringBuilder(data.length);
    				for(byte byteChar : data)
    					stringBuilder.append(String.format("%02X ", byteChar));
    				//intent.putExtra(EXTRA_DATA, new String(data) + "\n" +
    				//		stringBuilder.toString());
    			}
    		}
    		sendBroadcast(intent);
    }
    
	//******************************************************
	// Method:     close
	// Access:     public 
	// Returns:    void
	// Parameter:  
	// Note:	  close ble
	// Author      muzongcun  2012/11/12 create
	//*******************************************************
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

}