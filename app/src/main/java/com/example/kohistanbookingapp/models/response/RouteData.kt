package com.example.kohistanbookingapp.models.response

import com.google.gson.annotations.SerializedName

data class RouteData(
    @SerializedName("RouteId")
    val routeId: Int? = null,

    @SerializedName("RouteCode")
    val routeCode: String? = null,

    @SerializedName("DropPointId")
    val dropPointId: Int? = null,

    @SerializedName("DropPointName")
    val dropPointName: String? = null,

    @SerializedName("FromCityId")
    val fromCityId: Int? = null,

    @SerializedName("FromCity")
    val fromCity: String? = null,

    @SerializedName("ToCityId")
    val toCityId: Int? = null,

    @SerializedName("ToCity")
    val toCity: String? = null,

    @SerializedName("RouteTimeId")
    val routeTimeId: Long? = null,

    @SerializedName("RouteTimeValue")
    val routeTimeValue: String? = null,

    @SerializedName("RouteTimeType")
    val routeTimeType: String? = null,

    @SerializedName("VehicleTypeId")
    val vehicleTypeId: Int? = null,

    @SerializedName("VehicleType")
    val vehicleType: String? = null,

    @SerializedName("Fare")
    val fare: Double? = null,

    @SerializedName("FinalFare")
    val finalFare: Double? = null,

    @SerializedName("PromotionId")
    val promotionId: Int? = null,

    @SerializedName("PromotionName")
    val promotionName: String? = null,

    @SerializedName("PromotionDescription")
    val promotionDescription: String? = null,

    @SerializedName("DiscountPercentage")
    val discountPercentage: Double? = null,

    @SerializedName("DiscountAmount")
    val discountAmount: Double? = null,

    @SerializedName("SeatCapacity")
    val seatCapacity: Int? = null,

    @SerializedName("DisableSeats")
    val disableSeats: String? = null,

    @SerializedName("RemainingSeats")
    val remainingSeats: Int? = null
) {
    /**
     * Returns the payable per-seat fare (falls back to original fare if FinalFare is null).
     */
    fun getEffectiveFare(): Double = finalFare ?: fare ?: 0.0

    /**
     * True when a promotion/discount is active on this trip.
     */
    fun hasPromotion(): Boolean =
        (promotionId != null && promotionId > 0) ||
                ((discountAmount ?: 0.0) > 0.0) ||
                (finalFare != null && fare != null && finalFare < fare)

    /**
     * Parses CSV DisableSeats (e.g. "1,2,3,4,5,6,7,8") into a Set<Int> for BusSeatAdapter.
     */
    fun getDisabledSeatSet(): Set<Int> {
        return disableSeats
            ?.split(",")
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?.toSet()
            ?: emptySet()
    }
}