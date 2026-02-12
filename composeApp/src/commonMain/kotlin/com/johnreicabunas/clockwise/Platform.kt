package com.johnreicabunas.clockwise

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform