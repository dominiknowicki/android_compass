package pl.donow.compass

import android.location.Location
import kotlin.math.absoluteValue

fun Double.latitudeToDms(): String {
    val direction = if (this > 0) "N" else "S"
    var strLatitude = Location.convert(this.absoluteValue, Location.FORMAT_SECONDS)
    strLatitude = replaceSpecialChars(strLatitude) + " $direction"
    return strLatitude
}

fun Double.longitudeToDms(): String {
    val direction = if (this > 0) "W" else "E"
    var strLongitude = Location.convert(this.absoluteValue, Location.FORMAT_SECONDS)
    strLongitude = replaceSpecialChars(strLongitude) + " $direction"
    return strLongitude
}

fun replaceSpecialChars(string: String): String {
    var str = string
    str = str.replaceFirst(":".toRegex(), "°")
    str = str.replaceFirst(":".toRegex(), "'")
    str = str.replace(",".toRegex(), ".")
    val pointIndex = str.indexOf(".")
    if (pointIndex < str.length) {
        str = str.substring(0, pointIndex)
    }
    str += "\""
    return str
}
