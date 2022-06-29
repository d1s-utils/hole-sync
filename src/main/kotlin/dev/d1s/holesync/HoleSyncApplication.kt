package dev.d1s.holesync

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HoleSyncApplication

fun main(args: Array<String>) {
    runApplication<HoleSyncApplication>(*args)
}
