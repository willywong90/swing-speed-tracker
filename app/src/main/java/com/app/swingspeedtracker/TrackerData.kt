package com.app.swingspeedtracker

private const val MS_TO_MPH_CONVERSION = 2.23694
private val CLUB_LABEL = listOf("W1", "W3", "W5", "W7", "W9", "U2", "U3", "U4", "U5", "U6", "I3", "I4", "I5", "I6", "I7", "I8", "I9", "PW", "AW", "Sw", "LW", "PT")

class TrackerData(
    clubHeadSpeed: Double,
    ballSpeed: Double,
    distance: Double,
    club: Int
) {
    val club: Int = club

    val clubHeadSpeed: Double = clubHeadSpeed
        get() = field * MS_TO_MPH_CONVERSION

    val ballSpeed: Double = ballSpeed
        get() = field * MS_TO_MPH_CONVERSION

    val carry: Double = distance
        get() = if (club == 21) {
                field/100
            } else {
                field
            }

    val smashFactor: Double
        get() = if (ballSpeed == 0.0 || clubHeadSpeed == 0.0) {
                    0.0
                }
            else {
                ballSpeed/clubHeadSpeed
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