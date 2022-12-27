package com.app.swingspeedtracker.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.swingspeedtracker.MainViewModel
import com.app.swingspeedtracker.ui.components.BottomNavigationBar
import com.app.swingspeedtracker.ui.components.NavigationItem
import com.app.swingspeedtracker.ui.theme.SwingSpeedTrackerTheme

@RequiresApi(Build.VERSION_CODES.M)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val navController = rememberNavController()

    SwingSpeedTrackerTheme {
        Scaffold(
            bottomBar = { BottomNavigationBar(navController) },
            content = { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    NavHost(navController, startDestination = NavigationItem.Home.route) {
                        composable(NavigationItem.Home.route) {
                            TrackerScreen(
                                dataList = viewModel.sensorData,
                                bluetoothStatus = viewModel.getBluetoothState()
                            )
                        }
                        composable(NavigationItem.History.route) {
                            HistoryScreen(
                                dataList = viewModel.sensorData,
                                averageStats = viewModel.getAveragedStats(),
                                clearHistory = { viewModel.clearHistory() }
                            )
                        }
                    }
                }
            },
            backgroundColor = MaterialTheme.colors.background // Set background color to avoid the white flashing when you switch between screens
        )
    }
}

@RequiresApi(Build.VERSION_CODES.M)
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}