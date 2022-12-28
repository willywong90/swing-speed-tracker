package com.app.swingspeedtracker.ui

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import com.app.swingspeedtracker.data.TrackerData
import com.app.swingspeedtracker.data.TrackerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "SwingSpeedTracker"
private val UUID_GST_7_SERVICE = UUID.fromString("dc030000-7c54-44fa-bca6-c61732a248ef")
private val UUID_SENSOR_CHARACTERISTIC = UUID.fromString("dc030001-7c54-44fa-bca6-c61732a248ef")
private val UUID_SENSOR_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

@RequiresApi(Build.VERSION_CODES.M)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val applicationContext = getApplication<Application>().applicationContext
    //private var isSensorConnected by mutableStateOf(false)
    private var bluetoothLeScanCallback: ScanCallback
    private lateinit var bluetoothSensor: BluetoothDevice
    private lateinit var bluetoothGattCallback: BluetoothGattCallback
    //val sensorData = mutableStateListOf<TrackerData>()
    private val _uiState = MutableStateFlow(TrackerUiState())
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = applicationContext.getSystemService(ComponentActivity.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
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
        Log.d(TAG, "STARTING BLUETOOTH SCAN")
        bluetoothLeScanner.startScan(scanFilters, scanSettings, bluetoothLeScanCallback)
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

    fun getAveragedStats(): TrackerData {
        var averageClubSpeed = 0.0
        var averageBallSpeed = 0.0
        var averageCarry = 0.0

        if (_uiState.value.sensorData.isNotEmpty()) {
            _uiState.value.sensorData.forEach {
                averageClubSpeed += it.clubSpeed
                averageBallSpeed += it.ballSpeed
                averageCarry += it.carry
            }

            averageClubSpeed /= _uiState.value.sensorData.size
            averageBallSpeed /= _uiState.value.sensorData.size
            averageCarry /= _uiState.value.sensorData.size
        }

        return TrackerData(averageClubSpeed, averageBallSpeed, averageCarry, 0)
    }

    private fun setConnectedStatus(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                isSensorConnected = value
            )
        }
    }

    fun clearHistory() {
        _uiState.update { currentState ->
            currentState.copy(
                sensorData = listOf()
            )
        }
    }

    init {
        @SuppressLint("MissingPermission")
        bluetoothLeScanCallback = object: ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                Log.d(TAG, "FOUND DEVICE " + result.device.address)

                bluetoothSensor = bluetoothAdapter.getRemoteDevice(result.device.address)
                bluetoothSensor.connectGatt(applicationContext, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE)
            }
        }

        @SuppressLint("MissingPermission")
        bluetoothGattCallback = object: BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.d(TAG, "CONNECTED TO GATT SERVER")

                        setConnectedStatus(true)
                        gatt?.discoverServices()
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.e(TAG, "DISCONNECTED FROM GATT SERVER")

                        setConnectedStatus(false)
                        gatt?.close()
                    }
                } else {
                    Log.e(TAG, "DISCONNECTED FROM GATT SERVER")

                    setConnectedStatus(false)
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

                    setConnectedStatus(false)
                    gatt?.close()
                }
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                super.onCharacteristicChanged(gatt, characteristic)

                if (characteristic?.uuid.toString() == UUID_SENSOR_CHARACTERISTIC.toString()) {
                    val data: ByteArray? = characteristic?.value

                    if (data?.size!! > 4) {
                        val hexString: String = data.joinToString(separator = " ") {
                            String.format("%02X", it)
                        }
                        Log.d(TAG, "DATA RECEIVED: $hexString")

                        val newList = _uiState.value.sensorData.toMutableList()
                        newList.add(0, convertData(data))

                        _uiState.update { currentState ->
                            currentState.copy(
                                sensorData = newList
                            )
                        }
                    }
                }
            }
        }

        scanForSensor()
    }
}
