package dev.d1s.holesync.service

import java.nio.file.Path

interface UploadingService {

    suspend fun tryUpload(path: Path, objectGroup: String)
}