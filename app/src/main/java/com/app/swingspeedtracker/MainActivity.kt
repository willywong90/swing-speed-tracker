package com.app.swingspeedtracker

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.app.swingspeedtracker.ui.theme.SwingSpeedTrackerTheme
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "SwingSpeedTracker"
private val UUID_GST_7_SERVICE = UUID.fromString("dc030000-7c54-44fa-bca6-c61732a248ef")
private val UUID_SENSOR_CHARACTERISTIC = UUID.fromString("dc030001-7c54-44fa-bca6-c61732a248ef")
private val UUID_SENSOR_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

@RequiresApi(Build.VERSION_CODES.Q)
class MainActivity : ComponentActivity() {
    private var isBluetoothConnected by mutableStateOf(false)
    private var sensorData = mutableStateListOf<TrackerData>()
    private lateinit var getPermission: ActivityResultLauncher<String>
    private lateinit var getBluetoothIntentResult: ActivityResultLauncher<Intent>
    private lateinit var bluetoothSensor: BluetoothDevice

    private val isBluetoothPermissionGranted
        get() = hasPermission(Manifest.permission.BLUETOOTH_CONNECT)

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission ->
            if (!hasPermission) {
                Toast.makeText(applicationContext, "No permission to perform this action", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        getBluetoothIntentResult  = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Toast.makeText(applicationContext, "Bluetooth turned on", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(applicationContext, "Bluetooth turned off", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        initializeBluetooth()
        scanForSensor()

        setContent {
            MainView(isBluetoothConnected, sensorData)
        }
    }

    private fun initializeBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            getPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
            if (isBluetoothPermissionGranted) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                getBluetoothIntentResult.launch(enableBtIntent)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun scanForSensor() {
        val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        val scanFilters: MutableList<ScanFilter> = ArrayList()
        val scanSettings = ScanSettings.Builder()
            .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH)
            .build()
        val nameFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(UUID_GST_7_SERVICE))
            .build()

        scanFilters.add(nameFilter)

        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            getPermission.launch(Manifest.permission.BLUETOOTH_SCAN)
        }

        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            getPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
        }

        Log.d(TAG, "STARTING BLUETOOTH SCAN")
        bluetoothLeScanner.startScan(scanFilters, scanSettings, bluetoothLeScanCallback)
    }

    @SuppressLint("MissingPermission")
    private val bluetoothLeScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            Log.d(TAG, "FOUND DEVICE " + result.device.address)

            bluetoothSensor = bluetoothAdapter.getRemoteDevice(result.device.address)
            bluetoothSensor.connectGatt(applicationContext, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE)
        }
    }

    @SuppressLint("MissingPermission")
    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // successfully connected to the GATT Server
                    Log.d(TAG, "CONNECTED TO GATT SERVER")
                    isBluetoothConnected = true
                    gatt?.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.e(TAG, "DISCONNECTED FROM GATT SERVER")
                    isBluetoothConnected = false
                    gatt?.close()
                }
            } else {
                Log.e(TAG, "DISCONNECTED FROM GATT SERVER")
                isBluetoothConnected = false
                gatt?.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val sensorService = gatt?.getService(UUID_GST_7_SERVICE)
                val sensorCharacteristic = sensorService?.getCharacteristic(UUID_SENSOR_CHARACTERISTIC)
                if (sensorCharacteristic != null) {
                    gatt.setCharacteristicNotification(sensorCharacteristic, true)
                    val descriptor = sensorCharacteristic.getDescriptor(UUID_SENSOR_CHARACTERISTIC_CONFIG)
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                }
            } else {
                Log.e(TAG, "Service discovery failed")
                isBluetoothConnected = false
                gatt?.close()
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)

            if (characteristic?.uuid.toString() == UUID_SENSOR_CHARACTERISTIC.toString()) {
                val data: ByteArray? = characteristic?.value

                if (data?.isNotEmpty() == true && data?.size > 4) {
                    val hexString: String = data.joinToString(separator = " ") {
                        String.format("%02X", it)
                    }
                    Log.d(TAG, "DATA RECEIVED: $hexString")
                    sensorData.add(0, convertData(data))
                }
            }
        }
    }

    private fun convertData(data: ByteArray): TrackerData {
        val hexString: String = data.joinToString(separator = " ") {
            String.format("%02X", it)
        }
        val hexList = hexString.split(" ").reversed()

        val clubHeadSpeed = hexToDec(hexList.subList(12,14)).toDouble()/10
        val ballSpeed = hexToDec(hexList.subList(10,12)).toDouble()/10
        val distance = hexToDec(hexList.subList(7,9)).toDouble()
        val club = hexList[9].toInt(16)

        return TrackerData(clubHeadSpeed, ballSpeed, distance, club)
    }

    private fun hexToDec(hex: List<String>): Int {
        return hex.joinToString(separator = "").toInt(16)
    }

    private fun hasPermission(permissionType: String): Boolean {
        return ActivityCompat.checkSelfPermission(this, permissionType) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun MainView(bluetoothStatus: Boolean, dataList: MutableList<TrackerData>) {
    var btStatusColor = Color.DarkGray
    if (bluetoothStatus)
        btStatusColor = Color.Blue

    val displayData = if (dataList.size > 0) dataList[0] else null

    SwingSpeedTrackerTheme {
        Surface (
            modifier = Modifier.fillMaxSize()
        ) {
            Column {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(btStatusColor)
                )
                TrackerDisplay(displayData)
                TrackerList(dataList)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val testDisplay = TrackerData(11.11, 22.22, 33.33, 0)
    val testList = remember {mutableStateListOf<TrackerData>()}
    testList.add(testDisplay)
    testList.add(testDisplay)
    testList.add(testDisplay)

    MainView(true, testList)
}