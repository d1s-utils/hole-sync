package dev.d1s.holesync.entity

import java.nio.file.Path

data class IndexEntry(
    val group: String,
    val directory: Path,
)