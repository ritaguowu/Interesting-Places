package ca.wu.interestingplaces.models

import android.os.Parcel
import android.os.Parcelable

data class InterestingPlaceModel(
    val id: Int,
    val title: String?,
    val image: String?,
    val address: String?,
    val date: String?,
    val location: String?,
    val latitude: Double,
    val longitude: Double
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readDouble()
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(image)
        parcel.writeString(address)
        parcel.writeString(date)
        parcel.writeString(location)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return " title: $title description: $address"
    }

    companion object CREATOR : Parcelable.Creator<InterestingPlaceModel> {
        override fun createFromParcel(parcel: Parcel): InterestingPlaceModel {
            return InterestingPlaceModel(parcel)
        }

        override fun newArray(size: Int): Array<InterestingPlaceModel?> {
            return arrayOfNulls(size)
        }
    }
}