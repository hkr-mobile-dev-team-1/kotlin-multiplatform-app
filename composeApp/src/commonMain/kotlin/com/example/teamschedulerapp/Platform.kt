package com.example.teamschedulerapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform