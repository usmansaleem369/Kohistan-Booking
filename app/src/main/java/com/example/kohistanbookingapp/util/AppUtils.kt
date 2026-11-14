package com.example.kohistanbookingapp.util

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.kohistanbookingapp.R
import com.example.kohistanbookingapp.databinding.ErrorDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout

object AppUtils {


    fun showCustomToast(
        toastText: String,
        layoutInflater: LayoutInflater,
        context: Context,
    ) {
        val layout: View = layoutInflater.inflate(R.layout.custom_toast, null, false)

        layout.rootView.setBackgroundResource(R.drawable.toast_bg)

        val textView: TextView = layout.findViewById(R.id.toast_text)
        textView.text = toastText
        val toast = Toast(context)
        toast.duration = Toast.LENGTH_LONG
        toast.view = layout
        toast.show()
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun showErrorDialog(
        activity: Activity,
        layoutInflater: LayoutInflater,
        description: String,
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            return
        }

        val errorDialogBinding =
            ErrorDialogBinding.inflate(layoutInflater)
        errorDialogBinding.tvDescription.text = description
        
        
        val errorDialog =
            MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialog_Rounded).setView(
                errorDialogBinding.root
            ).create()
        errorDialogBinding.btnOk.setOnClickListener {
            errorDialog.dismiss()
        }
        errorDialog.setCancelable(false)
        errorDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun parseDateTimeToDate(inputDateTime: String): Date? {
        val trimmed = inputDateTime.trim()
        if (trimmed.isEmpty()) return null

        val karachiZone = ZoneId.of("Asia/Karachi")

        // 1. Parse ISO-8601 timestamps containing a timezone
        try {
            return Date.from(OffsetDateTime.parse(trimmed).toInstant())
        } catch (_: Exception) {
        }

        try {
            return Date.from(Instant.parse(trimmed))
        } catch (_: Exception) {
        }

        // 2. Parse local date/time without a timezone as Karachi time
        try {
            val normalized = trimmed.replace(" ", "T")
            val localDateTime = LocalDateTime.parse(normalized)
            return Date.from(localDateTime.atZone(karachiZone).toInstant())
        } catch (_: Exception) {
        }

        // 3. Fallback for supported date formats
        val patterns = arrayOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd hh:mm:ss a",
            "yyyy-MM-dd"
        )

        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                sdf.isLenient = false

                // Z means UTC; formats without a timezone use Karachi time
                sdf.timeZone = if (pattern.endsWith("'Z'")) {
                    TimeZone.getTimeZone("UTC")
                } else {
                    TimeZone.getTimeZone("Asia/Karachi")
                }

                val position = java.text.ParsePosition(0)
                val date = sdf.parse(trimmed, position)

                if (date != null && position.index == trimmed.length) {
                    return date
                }
            } catch (_: Exception) {
            }
        }

        return null
    }

    // Check whether location permission is granted
    fun isLocationPermissionGranted(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    // Check whether precise location permission is granted
    fun isFineLocationPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Check whether device location/GPS is enabled
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    // Check whether location permission was permanently denied
    fun shouldShowLocationPermissionRationale(activity: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    // Open device location settings
    fun openLocationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }

    fun formatToAmPm(time24: String?): String {
        if (time24.isNullOrBlank()) return ""
        return try {
            val inputFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
            val date = inputFormat.parse(time24.trim())
            date?.let { outputFormat.format(it) } ?: time24
        } catch (e: Exception) {
            time24 // Fallback to raw string if parsing fails
        }
    }

    fun getTimeGreeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good night"
        }
    }

    fun EditText.clearErrorOnTextChange() {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (error != null) error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }



    private fun createUtcDateFormat(pattern: String = TRAVEL_DATE_PATTERN): SimpleDateFormat =
        SimpleDateFormat(pattern, Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

    fun attachTravelDatePicker(
        fragmentManager: FragmentManager,
        triggerViews: List<View>,
        title: String = "Select Travel Date",
        dateFormatPattern: String = TRAVEL_DATE_PATTERN,
        allowPastDates: Boolean = false,
        tag: String = "TRAVEL_DATE_PICKER",
        currentDateProvider: () -> String? = { null },
        onDateSelected: (selectionMillis: Long, formattedDate: String) -> Unit
    ) {
        val openPickerListener = View.OnClickListener {
            if (fragmentManager.findFragmentByTag(tag) != null) return@OnClickListener

            val utcFormat = createUtcDateFormat(dateFormatPattern)
            val initialMillis = runCatching {
                currentDateProvider()?.let { utcFormat.parse(it)?.time }
            }.getOrNull() ?: MaterialDatePicker.todayInUtcMilliseconds()

            val builder = MaterialDatePicker.Builder.datePicker()
                .setTitleText(title)
                .setTheme(R.style.ThemeOverlay_App_DatePicker_Gold)
                .setSelection(initialMillis)

            if (!allowPastDates) {
                val constraints = CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointForward.now())
                    .build()
                builder.setCalendarConstraints(constraints)
            }

            val datePicker = builder.build()

            datePicker.addOnPositiveButtonClickListener { selectionMillis ->
                val formattedDate = utcFormat.format(Date(selectionMillis))
                onDateSelected(selectionMillis, formattedDate)
            }

            datePicker.show(fragmentManager, tag)
        }

        triggerViews.forEach { it.setOnClickListener(openPickerListener) }
    }

    /** Activity convenience extension (works with btnCalendar, EditText, etc.) */
    fun FragmentActivity.setupTravelDatePicker(
        vararg triggerViews: View,
        sharedPreferencesHelper: SharedPreferencesHelper,
        title: String = "Select Travel Date",
        onDateSelected: (formattedDate: String) -> Unit
    ) {
        attachTravelDatePicker(
            fragmentManager = supportFragmentManager,
            triggerViews = triggerViews.toList(),
            title = title,
            currentDateProvider = { sharedPreferencesHelper.selectedTravelDate }
        ) { _, formattedDate ->
            sharedPreferencesHelper.selectedTravelDate = formattedDate
            onDateSelected(formattedDate)
        }
    }

    fun Fragment.setupTravelDatePicker(
        editText: EditText,
        textInputLayout: TextInputLayout? = null,
        sharedPreferencesHelper: SharedPreferencesHelper,
        title: String = "Select Travel Date",
        onDateSelected: ((formattedDate: String) -> Unit)? = null
    ) {
        val initialText = sharedPreferencesHelper.selectedTravelDate
            ?: createUtcDateFormat().format(Date(MaterialDatePicker.todayInUtcMilliseconds()))
        editText.setText(initialText)

        attachTravelDatePicker(
            fragmentManager = parentFragmentManager,
            triggerViews = listOf(editText),
            title = title,
            currentDateProvider = { sharedPreferencesHelper.selectedTravelDate }
        ) { _, formattedDate ->
            editText.setText(formattedDate)
            editText.error = null
            sharedPreferencesHelper.selectedTravelDate = formattedDate
            onDateSelected?.invoke(formattedDate)
        }

        textInputLayout?.setStartIconOnClickListener { editText.performClick() }
    }

    fun FragmentActivity.setupDobDatePicker(
        editText: EditText,
        title: String = "Select Date of Birth",
        onDateSelected: ((formattedDob: String) -> Unit)? = null
    ) {
        attachTravelDatePicker(
            fragmentManager = supportFragmentManager,
            triggerViews = listOf(editText),
            title = title,
            dateFormatPattern = "yyyy-MM-dd",
            allowPastDates = true,
            tag = "DOB_DATE_PICKER",
            currentDateProvider = { editText.text.toString().trim().ifEmpty { "1998-01-01" } }
        ) { _, formattedDob ->
            editText.setText(formattedDob)
            editText.error = null
            onDateSelected?.invoke(formattedDob)
        }
    }
}


