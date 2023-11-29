package com.app.swingspeedtracker.data

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime

private const val MS_TO_MPH_CONVERSION = 2.23694
private val CLUB_LABEL = listOf("W1", "W3", "W5", "W7", "W9", "U2", "U3", "U4", "U5", "U6", "I3", "I4", "I5", "I6", "I7", "I8", "I9", "PW", "AW", "Sw", "LW", "PT")

@RequiresApi(Build.VERSION_CODES.O)
data class TrackerData(
    val clubSpeed: Double,
    val ballSpeed: Double,
    val distance: Double,
    val club: Int,
    val isSelected: Boolean = false,
    val date: LocalDateTime = LocalDateTime.now()
) {
    val clubSpeedMph: Double = clubSpeed
        get() = field * MS_TO_MPH_CONVERSION

    val ballSpeedMph: Double = ballSpeed
        get() = field * MS_TO_MPH_CONVERSION

    val carry: Double = distance
        get() = if (club == 21) {
                field/100
            } else {
                field
            }

    val smashFactor: Double
        get() = if (ballSpeed == 0.0 || clubSpeed == 0.0) {
                    0.0
                }
            else {
                ballSpeed/clubSpeed
            }

    val carryUnit: String
        get() = if (club == 21) {
                "m"
            } else {
                "yd"
            }

    fun getClubLabel(): String {
        return CLUB_LABEL[club]
    }
}