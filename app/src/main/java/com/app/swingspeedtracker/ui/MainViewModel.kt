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
    private val _uiState = MutableStateFlow(TrackerUiState())
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = applicationContext.getSystemService(ComponentActivity.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private var bluetoothLeScanCallback: ScanCallback
    private lateinit var bluetoothGattCallback: BluetoothGattCallback

    @SuppressLint("MissingPermission")
    private fun scanForSensor() {
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

    private fun setConnectedStatus(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                isSensorConnected = value
            )
        }
    }

    private fun clearHistory() {
        _uiState.update { currentState ->
            currentState.copy(
                sensorData = listOf()
            )
        }
    }

    private fun clearSelection() {
        _uiState.update { currentState ->
            currentState.copy(
                sensorData = currentState.sensorData.map { item -> item.copy(isSelected = false) }
            )
        }
    }

    fun getAveragedStats(): TrackerData {
        var averageClubSpeed = 0.0
        var averageBallSpeed = 0.0
        var averageCarry = 0.0

        var list = _uiState.value.sensorData.filter { data -> data.isSelected }

        if (list.isEmpty())
            list = _uiState.value.sensorData

        if (list.isNotEmpty()) {
            averageClubSpeed = list.map { item -> item.clubSpeed }.average()
            averageBallSpeed = list.map { item -> item.ballSpeed }.average()
            averageCarry = list.map { item -> item.distance }.average()
        }

        return TrackerData(averageClubSpeed, averageBallSpeed, averageCarry, 0)
    }

    fun onEvent(event: UiEvent) {
        when(event) {
            is UiEvent.ItemSelected -> {
                val newSensorDataValue = _uiState.value.sensorData.map { item ->
                    if (item == event.selectedObject)
                        item.copy(isSelected = !item.isSelected)
                    else
                        item
                }
                _uiState.update { currentState ->
                    currentState.copy(
                        sensorData = newSensorDataValue
                    )
                }
            }
            is UiEvent.ClearSelection -> {
                clearSelection()
            }
            is UiEvent.ClearHistory -> {
                clearHistory()
            }
        }
    }

    init {
        @SuppressLint("MissingPermission")
        bluetoothLeScanCallback = object: ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                Log.d(TAG, "FOUND DEVICE " + result.device.address)

                val bluetoothSensor = bluetoothAdapter.getRemoteDevice(result.device.address)
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
                        gatt?.disconnect()
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
