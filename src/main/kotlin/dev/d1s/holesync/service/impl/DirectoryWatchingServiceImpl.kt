package dev.d1s.holesync.service.impl

import dev.d1s.holesync.entity.IndexEntry
import dev.d1s.holesync.properties.IndexConfigurationProperties
import dev.d1s.holesync.service.DirectoryWatchingService
import dev.d1s.holesync.service.UploadingService
import kotlinx.coroutines.runBlocking
import org.lighthousegames.logging.logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Lazy
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.io.path.forEachDirectoryEntry
import kotlin.io.path.isDirectory
import kotlin.io.path.isReadable

@Service
class DirectoryWatchingServiceImpl : DirectoryWatchingService {

    @set:Autowired
    lateinit var indexConfiguration: IndexConfigurationProperties

    @set:Autowired
    lateinit var uploadingService: UploadingService

    @set:Autowired
    lateinit var applicationContext: ApplicationContext

    @set:Lazy
    @set:Autowired
    @set:Qualifier("directoryWatchingServiceImpl")
    lateinit var directoryWatchingServiceImpl: DirectoryWatchingService

    private val log = logging()

    private val mapping by lazy {
        indexConfiguration.mapping.map {
            IndexEntry(
                it.key,
                Paths.get(it.value)
            )
        }
    }

    @Scheduled(fixedDelay = 2, timeUnit = TimeUnit.SECONDS)
    override fun visitAllDirectories() {
        log.d {
            "Visiting all directories: $mapping"
        }

        mapping.forEach {
            try {
                directoryWatchingServiceImpl.visitDirectory(it)
            } catch (e: IllegalArgumentException) {
                log.e {
                    e.message!!
                }

                SpringApplication.exit(applicationContext)
            }
        }
    }

    @Async
    override fun visitDirectory(indexEntry: IndexEntry) {
        val directory = indexEntry.directory

        if (!directory.isDirectory()) {
            throw IllegalArgumentException("Path $directory is not a directory.")
        }

        if (!directory.isReadable()) {
            throw IllegalArgumentException("Directory $directory is not readable.")
        }

        runBlocking {
            directory.forEachDirectoryEntry {
                uploadingService.tryUpload(it, indexEntry.group)
            }
        }
    }
}