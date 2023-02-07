package com.example.beaconscanner.utils

import com.example.beaconscanner.model.Point
import kotlin.math.*

object CoordinateCaculator {
    fun calculatePointByDistanceAndAngle(point: Point, distance:Float, angle:Float): Point {
        val x1 = point.x + distance * sin(angleToRadians(angle));
        val y1 = point.y + distance * cos(angleToRadians(angle));
        return Point(x1,y1)
    }
    fun angleToRadians(value:Float):Float {
        return ((value / 360) * 2 * PI).toFloat()
    }
    fun calculateCoordinateByPointAndAngle(aPoint: Point, diameter:Float, middlePoint: Point, angle: Float) : Point {
        val pointAt0Degree = Point(middlePoint.x, middlePoint.y + diameter)
        val degreeBetweenAAndPointAt0 = findAngleBy2PointAndAngle(aPoint, pointAt0Degree, diameter)
        return  circleXY(middlePoint, diameter, 360 - (degreeBetweenAAndPointAt0 - angle))
    }
    fun findAngleBy2PointAndAngle(point1: Point, point2: Point, diameter: Float): Float {
        return ((asin((calculateDistance(point1, point2) / 2) / diameter) * 180 / Math.PI) * 2).toFloat()
    }
    fun calculateDistance(point1: Point, point2: Point): Float {
        return sqrt((point2.y - point1.y) * (point2.y - point1.y) + (point2.x - point1.x) * (point2.x - point1.x))
    }
    fun circleXY(middlePoint: Point, r: Float, angle: Float) : Point {
        // Convert angle to radians
        val theta = (angle - 90) * Math.PI / 180;

        return Point(
            (r * cos(theta) + middlePoint.x).toFloat(),
            (-r * sin(theta) + middlePoint.y).toFloat()
        )
    }
}