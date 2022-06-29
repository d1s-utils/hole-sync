package dev.d1s.holesync.service

import dev.d1s.holesync.entity.IndexEntry

interface DirectoryWatchingService {

    fun visitAllDirectories()

    fun visitDirectory(indexEntry: IndexEntry)
}