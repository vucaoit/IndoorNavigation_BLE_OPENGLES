package com.example.beaconscanner.model

class Point(X:Float,Y:Float) {
    var x = X
    var y = Y
    override fun toString(): String {
        return "($x,$y)"
    }
    fun revert():Point{
        return Point(-x,-y)
    }
}