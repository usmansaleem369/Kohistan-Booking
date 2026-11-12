package com.example.kohistanbookingapp.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

object TravelTimeEstimator {

    private const val AVERAGE_BUS_SPEED_KMH = 80.0

    // Distance map in KM from FSD, LHR, and ISB/RWP
    private val distanceFromFsd = mapOf(
        "abdul hakeem" to 155,
        "alipur" to 335,
        "bahawalpur" to 330,
        "chandka qila" to 815,
        "chichawatni" to 125,
        "chiniot" to 40,
        "dg khan" to 345,
        "daska" to 185,
        "duniapur" to 275,
        "gujranwala" to 150,
        "hunza" to 1085,
        "islamabad" to 320,
        "rawalpindi" to 320,
        "jatoi" to 345,
        "kabirwala" to 176,
        "kamalia" to 95,
        "kamoki" to 135,
        "khanpur" to 465,
        "khanewal" to 195,
        "khangarh" to 280,
        "khayali bypass" to 148,
        "lahore" to 150,
        "lodhran" to 295,
        "mandi jahanian" to 220,
        "mansehra" to 485,
        "metla chowk" to 265,
        "mian channu" to 165,
        "multan" to 240,
        "muridke" to 145,
        "murree" to 380,
        "muzaffargarh" to 275,
        "peshawar" to 495,
        "rahim yar khan" to 505,
        "rajana" to 70,
        "rohelanwali" to 305,
        "sadiqabad" to 530,
        "sambrial" to 195,
        "samundri" to 45,
        "shah jamal" to 295,
        "shehr sultan" to 320,
        "sheikhupura" to 115,
        "sialkot" to 210,
        "uch sharif" to 355,
        "zahir pir" to 440
    )

    private val distanceFromLhr = mapOf(
        "abdul hakeem" to 290,
        "alipur" to 465,
        "bahawalpur" to 420,
        "chandka qila" to 945,
        "chichawatni" to 235,
        "chiniot" to 155,
        "dg khan" to 455,
        "daska" to 105,
        "duniapur" to 380,
        "faisalabad" to 150,
        "gujranwala" to 75,
        "hunza" to 1145,
        "islamabad" to 375,
        "rawalpindi" to 375,
        "jatoi" to 465,
        "kabirwala" to 305,
        "kamalia" to 235,
        "kamoki" to 50,
        "khanpur" to 575,
        "khanewal" to 300,
        "khangarh" to 395,
        "khayali bypass" to 78,
        "lodhran" to 395,
        "mandi jahanian" to 325,
        "mansehra" to 540,
        "metla chowk" to 370,
        "mian channu" to 265,
        "multan" to 345,
        "muridke" to 30,
        "murree" to 435,
        "muzaffargarh" to 385,
        "peshawar" to 550,
        "rahim yar khan" to 620,
        "rajana" to 215,
        "rohelanwali" to 420,
        "sadiqabad" to 645,
        "sambrial" to 115,
        "samundri" to 185,
        "shah jamal" to 410,
        "shehr sultan" to 435,
        "sheikhupura" to 40,
        "sialkot" to 130,
        "uch sharif" to 475,
        "zahir pir" to 555
    )

    private val distanceFromIsbRwp = mapOf(
        "abdul hakeem" to 430,
        "alipur" to 605,
        "bahawalpur" to 615,
        "chandka qila" to 1090,
        "chichawatni" to 440,
        "chiniot" to 280,
        "dg khan" to 555,
        "daska" to 240,
        "duniapur" to 555,
        "faisalabad" to 320,
        "gujranwala" to 215,
        "hunza" to 765,
        "jatoi" to 610,
        "kabirwala" to 450,
        "kamalia" to 405,
        "kamoki" to 240,
        "khanpur" to 750,
        "khanewal" to 470,
        "khangarh" to 540,
        "khayali bypass" to 210,
        "lahore" to 375,
        "lodhran" to 575,
        "mandi jahanian" to 495,
        "mansehra" to 165,
        "metla chowk" to 540,
        "mian channu" to 455,
        "multan" to 520,
        "muridke" to 260,
        "murree" to 60,
        "muzaffargarh" to 535,
        "peshawar" to 180,
        "rahim yar khan" to 790,
        "rajana" to 385,
        "rohelanwali" to 565,
        "sadiqabad" to 815,
        "sambrial" to 235,
        "samundri" to 360,
        "shah jamal" to 555,
        "shehr sultan" to 580,
        "sheikhupura" to 265,
        "sialkot" to 230,
        "uch sharif" to 630,
        "zahir pir" to 725
    )

    /**
     * Normalizes terminal/spelling variations from the API (e.g., "BahawalpurTT" -> "bahawalpur").
     */
    private fun normalizeCity(rawName: String?): String {
        if (rawName.isNullOrBlank()) return ""
        val cleaned = rawName.trim().lowercase(Locale.ENGLISH)
            .replace(Regex("\\s+"), " ")
            .removeSuffix(" tt")
            .removeSuffix("tt")
            .removeSuffix(" b")
            .removeSuffix("b")
            .removeSuffix(" e")
            .removeSuffix(" f")
            .removeSuffix(" m")
            .removeSuffix(" c")
            .removeSuffix(" t")
            .removeSuffix("26")
            .trim()

        return when {
            cleaned.contains("faisalabad") -> "faisalabad"
            cleaned.contains("lahore") || cleaned.startsWith("lhr") -> "lahore"
            cleaned.contains("islamabad") -> "islamabad"
            cleaned.contains("rawalp") -> "rawalpindi"
            cleaned.contains("ali pur") || cleaned.contains("alipur") -> "alipur"
            cleaned.contains("chand ka") || cleaned.contains("chandka") -> "chandka qila"
            cleaned.contains("chicha watni") || cleaned.contains("chichawatni") -> "chichawatni"
            cleaned.contains("daira ghazi") || cleaned.contains("dg khan") -> "dg khan"
            cleaned.contains("kamoki") -> "kamoki"
            cleaned.contains("khan pur") || cleaned.contains("khanpur") -> "khanpur"
            cleaned.contains("mandijahania") -> "mandi jahanian"
            cleaned.contains("metlachowk") || cleaned.contains("metla chowk") -> "metla chowk"
            cleaned.contains("mian chano") || cleaned.contains("mianchannu") -> "mian channu"
            cleaned.contains("muree") || cleaned.contains("murree") -> "murree"
            cleaned.contains("mureed") -> "muridke"
            cleaned.contains("muzafar") -> "muzaffargarh"
            cleaned.contains("pashawer") || cleaned.contains("peshawar") -> "peshawar"
            cleaned.contains("rahim") -> "rahim yar khan"
            cleaned.contains("sadaqabad") || cleaned.contains("sadiqabad") -> "sadiqabad"
            cleaned.contains("zahir") -> "zahir pir"
            else -> cleaned
        }
    }

    /**
     * Returns estimated distance in KM between origin and destination (supports reverse lookup).
     */
    fun getDistanceKm(fromCity: String?, toCity: String?): Int {
        val from = normalizeCity(fromCity)
        val to = normalizeCity(toCity)
        if (from.isEmpty() || to.isEmpty() || from == to) return 0

        return when (from) {
            "faisalabad" -> distanceFromFsd[to]
            "lahore" -> distanceFromLhr[to]
            "islamabad", "rawalpindi" -> distanceFromIsbRwp[to]
            else -> when (to) {
                "faisalabad" -> distanceFromFsd[from]
                "lahore" -> distanceFromLhr[from]
                "islamabad", "rawalpindi" -> distanceFromIsbRwp[from]
                else -> null
            }
        } ?: 0
    }

    /**
     * Calculates travel duration in minutes at 80 km/h.
     */
    fun getEstimatedMinutes(fromCity: String?, toCity: String?): Int {
        val km = getDistanceKm(fromCity, toCity)
        if (km <= 0) return 0
        return ((km / AVERAGE_BUS_SPEED_KMH) * 60.0).roundToInt()
    }

    /**
     * Formats duration as "2h 30m".
     */
    fun getFormattedDuration(fromCity: String?, toCity: String?): String {
        val totalMinutes = getEstimatedMinutes(fromCity, toCity)
        if (totalMinutes <= 0) return ""
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        return when {
            hours > 0 && mins > 0 -> "${hours}h ${mins}m"
            hours > 0 -> "${hours}h"
            else -> "${mins}m"
        }
    }

    /**
     * Calculates formatted AM/PM arrival time from 24h departure string (e.g. "16:10").
     */
    fun calculateArrivalTime(
        departureTime24: String?,
        fromCity: String?,
        toCity: String?
    ): String {
        if (departureTime24.isNullOrBlank()) return ""
        val durationMinutes = getEstimatedMinutes(fromCity, toCity)
        if (durationMinutes <= 0) return ""

        return try {
            val inputFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
            val date = inputFormat.parse(departureTime24.trim()) ?: return ""

            val calendar = Calendar.getInstance().apply {
                time = date
                add(Calendar.MINUTE, durationMinutes)
            }
            outputFormat.format(calendar.time)
        } catch (e: Exception) {
            ""
        }
    }
}