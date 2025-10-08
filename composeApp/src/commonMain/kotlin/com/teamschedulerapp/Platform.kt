package com.teamschedulerapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform