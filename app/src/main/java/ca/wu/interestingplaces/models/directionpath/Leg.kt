package ca.wu.interestingplaces.models.directionpath

import java.util.*

class Leg {
    var distance:Distance? = null
    var duration:Duration? = null
    var end_address:String? = null
    var end_location:EndLocation? = null
    var start_address:String? = null
    var start_location:StartLocation? = null
    var steps:Array<Step>? = null

}