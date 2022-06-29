package dev.d1s.holesync.service.impl

import dev.d1s.hole.client.core.HoleClient
import dev.d1s.hole.client.exception.HoleClientException
import dev.d1s.holesync.service.UploadingService
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.codec.digest.DigestUtils
import org.lighthousegames.logging.logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.name

@Service
class UploadingServiceImpl : UploadingService {

    @set:Autowired
    lateinit var holeClient: HoleClient

    private val log = logging()

    override suspend fun tryUpload(path: Path, objectGroup: String) {
        log.d {
            "Trying to upload $path"
        }

        if (path.isDirectory()) {
            throw IllegalArgumentException("$path is a directory. Directories are not allowed.")
        }

        val name = path.normalize().name

        var objectExists = false

        withContext(Dispatchers.IO) {
            Files.newInputStream(path)
        }.use { input ->
            suspend fun upload(objectId: String? = null) {
                log.d {
                    "Uploading $name..."
                }

                objectId?.let { id ->
                    holeClient.putRawObject(id) {
                        content = input
                        fileName = name
                    }
                } ?: run {
                    holeClient.postObject(true) {
                        content = input
                        fileName = name
                        group = objectGroup
                    }
                }

                log.i {
                    "Object $name was successfully uploaded into $objectGroup"
                }
            }

            try {
                holeClient.getGroup(objectGroup).storageObjects.forEach { obj ->
                    val exists = obj.name == name

                    log.d {
                        if (exists) {
                            "Object already exists"
                        } else {
                            null
                        }
                    }

                    if (!objectExists) {
                        objectExists = exists
                    }

                    if (
                        exists && obj.digest != Files.newInputStream(path).use {
                            DigestUtils.sha256Hex(it)
                        }
                    ) {
                        log.i {
                            "Change detected at $path"
                        }

                        upload(obj.id)
                    }
                }
            } catch (e: HoleClientException) {
                if (e.error?.status != HttpStatusCode.NotFound.value) {
                    log.e(e) {
                        "An error occurred."
                    }
                }
            }

            if (!objectExists) {
                upload()
            }
        }
    }
}