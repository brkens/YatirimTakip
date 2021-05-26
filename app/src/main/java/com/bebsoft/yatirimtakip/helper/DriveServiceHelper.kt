package com.bebsoft.yatirimtakip.helper

import com.bebsoft.yatirimtakip.Constants
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.InputStreamContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class DriveServiceHelper (private val mDriveService: Drive) {
    private val mExecutor: Executor = Executors.newSingleThreadExecutor()

    /**
     * To upload sqlite db file
     */
    fun uploadDBFile(): Task<String?>? {
        return Tasks.call(mExecutor, {

            val myDbFile = java.io.File(Constants.DATABASE_FULL_PATH)

            val metadata = File()
                .setParents(Collections.singletonList("root"))
                .setMimeType("application/vnd.sqlite3")
                .setName(Constants.DATABASE_NAME_WITH_EXTENSION)

            val targetStream: InputStream = FileInputStream(myDbFile)
            val inputStreamContent = InputStreamContent("application/vnd.sqlite3", targetStream)

            val googleFile = mDriveService.files().create(metadata, inputStreamContent).execute()
                ?: throw IOException("Null result when requesting file creation.")

            googleFile.id
        })
    }

    /**
     * To update an existing file
     */
    fun updateDBFile(fileId: String): Task<Void?>? {
        return Tasks.call(mExecutor, {

            val myDbFile = java.io.File(Constants.DATABASE_FULL_PATH)

            val metadata = File()
                .setMimeType("application/vnd.sqlite3")
                .setName(Constants.DATABASE_NAME_WITH_EXTENSION)

            val targetStream: InputStream = FileInputStream(myDbFile)
            val inputStreamContent = InputStreamContent("application/vnd.sqlite3", targetStream)

            mDriveService.files().update(fileId, metadata, inputStreamContent).execute()

            null
        })
    }

    fun uploadTextFile(name: String?, fileTest: java.io.File?): Task<String?>? {
        return Tasks.call(mExecutor, {

            val metadata = File()
                .setParents(Collections.singletonList("root"))
                .setMimeType("text/plain")
                .setName(name)

            val targetStream: InputStream = FileInputStream(fileTest)
            val inputStreamContent = InputStreamContent("text/plain", targetStream)

            val googleFile = mDriveService.files().create(metadata, inputStreamContent).execute()
                ?: throw IOException("Null result when requesting file creation.")

            googleFile.id
        })
    }

    fun createFolder(): Task<String?>? {
        return Tasks.call(mExecutor, {

            val metadata = File()
                .setMimeType("application/vnd.google-apps.folder")
                .setName("yatirimtakipapp")

            val googleFolder = mDriveService.files().create(metadata).setFields("id").execute()

            googleFolder.id
        })
    }

    /**
     * Creates an empty file
     */
    fun createTextFile(): Task<String>? {
        return Tasks.call(mExecutor, {

            val metadata =
                File()
                    .setParents(Collections.singletonList("root"))
                    .setMimeType("text/plain")
                    .setName("just_a_file")

            val googleFile =
                mDriveService.files().create(metadata).execute()
                    ?: throw IOException("Null result when requesting file creation.")

            googleFile.id
        })
    }

    fun updateTextFile(fileId: String?, name: String?, content: String?): Task<Void?>? {
        return Tasks.call(mExecutor, {

            val metadata = File().setName(name)

            val contentStream = ByteArrayContent.fromString("text/plain", content)

            mDriveService.files().update(fileId, metadata, contentStream).execute()

            null
        })
    }

    /**
     * Get file list created by Google Drive API
     */
    fun queryFiles(): Task<FileList?>? {
        return Tasks.call(mExecutor, {

            mDriveService.files()
                .list()
                .setSpaces("drive")
                .setFields("nextPageToken, files(id, name)")
                .execute()
        })
    }

    /**
     * Get folder list created by Google Drive API (Change root in setQ if you want another folder)
     */
    fun queryFolders(): Task<FileList?>? {
        return Tasks.call(mExecutor, {

            mDriveService.files()
                .list()
                .setQ("'root' in parents and mimeType != 'application/vnd.google-apps.folder' and trashed = false")
                .setSpaces("drive")
                .execute()
        })
    }
}