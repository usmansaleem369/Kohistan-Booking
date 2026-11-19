package com.example.kohistanbookingapp.util

object JazzCashFailureMessageHandler {

    fun formatJazzCashFailureMessage(rawMessage: String?): String {
        if (rawMessage.isNullOrBlank()) {
            return "JazzCash payment could not be completed."
        }

        return when {
            rawMessage.contains("short message 'N'", ignoreCase = true) ||
                    rawMessage.contains("cancel a transaction", ignoreCase = true) -> {
                "Payment was cancelled or the JazzCash MPIN prompt expired. No amount was deducted—please try again."
            }

            rawMessage.contains("insufficient", ignoreCase = true) -> {
                "Insufficient balance in your JazzCash account. Please recharge and try again."
            }

            rawMessage.contains("invalid mpin", ignoreCase = true) ||
                    rawMessage.contains("incorrect mpin", ignoreCase = true) -> {
                "Incorrect JazzCash MPIN entered. Please try again."
            }

            else -> rawMessage
        }
    }
}