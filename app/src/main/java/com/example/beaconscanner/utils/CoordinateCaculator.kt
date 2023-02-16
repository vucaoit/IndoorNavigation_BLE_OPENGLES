package com.example.beaconscanner.utils

import android.util.Log
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
        val degreeBetweenAAndPointAt0 = findAngleBy2PointAndDiameter(aPoint, pointAt0Degree, diameter)
        return  circleXY(middlePoint, diameter, 360 - (degreeBetweenAAndPointAt0 - angle))
    }
    fun findAngleBy2PointAndDiameter(point1: Point, point2: Point, diameter: Float): Float {
        //point1 is point at 0
        if(point2.x>point1.x){
            //point2 at left of point1
            return ((asin((calculateDistance(point1, point2) / 2) / diameter) * 180 / Math.PI) * 2).toFloat()
        }else{
            //point2 at right of point 1
            return 360 - ((asin((calculateDistance(point1, point2) / 2) / diameter) * 180 / Math.PI) * 2).toFloat()
        }
    }
    fun findAngleByMiddleAndPoint(middlePoint: Point,point: Point):Float{
        val distance = calculateDistance(middlePoint,point)
        Log.d("Distance","$distance")
        val pointAt0 = Point(middlePoint.x,middlePoint.y + distance)
        Log.d("PointAt0",pointAt0.toString())
        val angle = findAngleBy2PointAndDiameter(pointAt0,point,distance)
        return angle;
    }
    fun findCoordinateByMiddleAndAngleAndDiameter(middlePoint: Point,point: Point,diameterofMiddle: Float):Point{
        var angle= findAngleByMiddleAndPoint(middlePoint,point).roundToInt()
        Log.e("Angle","${middlePoint.toString()} $angle")
        if(angle>180){
            angle = 360- angle
            val coor = circleXY(middlePoint,diameterofMiddle,angle *1f)
            return Point(coor.x - diameterofMiddle*2,coor.y)
        }
        return circleXY(middlePoint,diameterofMiddle,angle *1f)
    }
    fun findCenterCoordinateBetween2Point(
        point1: Point,
        diameter1: Float,
        point2: Point,
        diameter2: Float
    ):Point{
        val crossPointAtPoint1 = findCoordinateByMiddleAndAngleAndDiameter(point1,point2,diameter1)
        Log.e("p1",crossPointAtPoint1.toString())

        val crossPointAtPoint2 = findCoordinateByMiddleAndAngleAndDiameter(point2,point1,diameter2)
        Log.e("p2",crossPointAtPoint2.toString())
        return Point((crossPointAtPoint1.x + crossPointAtPoint2.x)/2,(crossPointAtPoint1.y + crossPointAtPoint2.y)/2)
    }
    fun calculateDistance(point1: Point, point2: Point): Float {
        return sqrt((point2.y - point1.y) * (point2.y - point1.y) + (point2.x - point1.x) * (point2.x - point1.x))
    }
    fun circleXY(middlePoint: Point, r: Float, angle: Float) : Point {
        //return point give angle,middle point,diameter
        // Convert angle to radians
        val theta = (angle - 90) * Math.PI / 180;

        return Point(
            (r * cos(theta) + middlePoint.x).toFloat(),
            (-r * sin(theta) + middlePoint.y).toFloat()
        )
    }
    fun calculateAngle(
        P1X: Float, P1Y: Float, P2X: Float, P2Y: Float,
        P3X: Float, P3Y: Float
    ): Float {
        //p1--------------p2
        //.
        // .
        //  .p3
        val numerator = P2Y * (P1X - P3X) + P1Y * (P3X - P2X) + P3Y * (P2X - P1X)
        val denominator = (P2X - P1X) * (P1X - P3X) + (P2Y - P1Y) * (P1Y - P3Y)
        val ratio = numerator / denominator
        val angleRad: Double = Math.atan(ratio.toDouble())
        var angleDeg = angleRad * 180 / Math.PI
        if (angleDeg < 0) {
            angleDeg += 180
        }
        return angleDeg.toFloat()
    }
    fun getTrilateration(position1:Point,r1:Float, position2:Point,r2:Float, position3:Point,r3:Float) :Point{
        var xa = position1.x * 1.0;
        var ya = position1.y* 1.0;
        var xb = position2.x* 1.0;
        var yb = position2.y* 1.0;
        var xc = position3.x* 1.0;
        var yc = position3.y* 1.0;
        var ra = r1* 1.0;
        var rb = r2* 1.0;
        var rc = r3* 1.0;

        var S = (Math.pow(xc,2.00) - Math.pow(xb,2.0) + Math.pow(yc,2.0) - Math.pow(
            yb,2.0) + Math.pow(rb,2.0) - Math.pow(rc,2.0)) /2.00;
        var T = (Math.pow(xa,2.0) - Math.pow(xb,2.0) + Math.pow(ya,2.0) - Math.pow(
            yb,2.0) + Math.pow(rb,2.0) - Math.pow(ra,2.0)) /2.00;
        var y = ((T * (xb - xc)) - (S * (xb - xa))) / (((ya - yb) * (xb - xc)) - ((yc - yb) * (xb - xa)));
        var x = ((y * (ya - yb)) - T) / (xb - xa);
        return Point(x.toFloat(),y.toFloat())
    }
}