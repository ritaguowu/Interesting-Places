package ca.wu.interestingplaces.models.directionpath

import ca.wu.interestingplaces.models.GeocodedWaypoint

class PathModel {
    var geocoded_waypoints: Array<GeocodedWaypoint>? = null
    var routes: Array<Route>? = null
    var status: String? = null
}