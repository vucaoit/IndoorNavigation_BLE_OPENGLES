package com.example.opengl

object Func {
    fun stringRBGaToColorArray(inp:String):Array<Float>{
//        rgba(255, 136, 71, 0.5)
        val temps = inp.replace("rgba","").replace("(","").replace(")","").split(",")
        var arr:ArrayList<Float> = arrayListOf()

        for (temp in temps) {
            arr.add((255/temp.toFloat()))
        }
        return arr.toTypedArray()
    }
}