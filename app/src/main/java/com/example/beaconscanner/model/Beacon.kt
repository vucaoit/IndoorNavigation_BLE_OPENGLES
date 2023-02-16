package com.example.beaconscanner.model

import kotlin.math.pow

class Beacon(mac: String?) {
    enum class beaconType {
        iBeacon, eddystoneUID, any
    }
    val macAddress = mac
    var manufacturer: String? = null
    var type: beaconType = beaconType.any
    var uuid: String? = null
    var major: Int? = null
    var minor: Int? = null
    var namespace: String? = null
    var instance: String? = null
    var rssi: Int? = null
    var raw: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Beacon) return false

        if (macAddress != other.macAddress) return false

        return true
    }
    fun beaconToDistance(measuredPower:Int,N:Float):Float{
        return 10.0.pow((measuredPower - rssi!!*1.0)/(10 * N)).toFloat()
    }
    override fun hashCode(): Int {
        return macAddress?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "${macAddress},${uuid},${major},${minor},${rssi}\n"
    }
}