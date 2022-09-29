package ca.wu.interestingplaces.models.placedetails

import ca.wu.interestingplaces.models.nearbyplaces.GeometryModel
import ca.wu.interestingplaces.models.nearbyplaces.OpeningHourModel
import ca.wu.interestingplaces.models.nearbyplaces.PhotoModel

class ResultModel {
    var photos: Array<PhotoModel>?= null
    var id:String? = null
    var place_id:String? = null
    var price_level: Int = 0
    var rating: Double = 0.0
    var reference:String? = null
    var scrope: String? = null
    var type: Array<String>? =null
    var vicinity:String? = null
    var opening_hours: OpeningHourModel? = null
    var name: String? = null
    var address_components:Array<AddressComponentModel>? = null
    var adr_address:String? = null
    var formatted_address:String? = null
    var formtted_phone_number:String? =null
    var geometry: GeometryModel? = null
    var reviews:Array<ReviewModel>? = null
    var utc_offset:Int = 0
    var website:String? = null
    var url:String? = null
}