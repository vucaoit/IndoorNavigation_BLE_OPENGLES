package com.example.beaconscanner.controller

class ToolController {
    var mode = 0

    companion object {
        const val VIEW_MODE = 0
        const val DRAW_MAP = 1
        const val MOVE_BEACON = 2
        const val ADJUST_BEACON = 3
        const val MOVE_AND_ZOOM = 4
    }

}