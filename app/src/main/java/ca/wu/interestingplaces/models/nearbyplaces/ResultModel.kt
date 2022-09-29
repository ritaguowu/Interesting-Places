package ca.wu.interestingplaces.models.nearbyplaces

class ResultModel {
    var photos: Array<PhotoModel>?= null
    var id:String? = null
    var place_id:String? = null
    var price_level: Int = 0
    var rating: Double = 0.0
    var reference:String? = null
    var scrope: String? = null
    var types: Array<String>? =null
    var vicinity:String? = null
    var opening_hours: OpeningHourModel? = null
    var icon:String? = null
    var geometry: GeometryModel? = null
    var name: String? = null
}