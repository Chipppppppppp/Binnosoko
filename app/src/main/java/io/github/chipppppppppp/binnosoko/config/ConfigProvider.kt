package io.github.chipppppppppp.binnosoko.config

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import io.github.chipppppppppp.binnosoko.BuildConfig
import io.github.chipppppppppp.binnosoko.util.Logger
import java.io.File
import java.io.FileNotFoundException

class ConfigProvider : ContentProvider() {
    companion object {
        private const val TAG = "Futa-ConfigProvider"
    }

    override fun onCreate(): Boolean {
        return true
    }

    private fun getFile(name: String): File {
        return when (name) {
            "/main_config" -> File(context!!.filesDir, "main_config.json")
            "/internal_config" -> File(context!!.filesDir, "internal_config.json")
            else -> throw FileNotFoundException("File not found: $name")
        }
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val name = uri.encodedPath ?: return null
        val file = getFile(name)
        val modeBits = ParcelFileDescriptor.parseMode(mode)
        if (
            (modeBits and ParcelFileDescriptor.MODE_WRITE_ONLY) != 0 &&
            callingPackage != BuildConfig.APPLICATION_ID
        ) {
            Logger.w(TAG, "Package $callingPackage cannot write")
            return null
        }
        return try {
            ParcelFileDescriptor.open(file, modeBits)
        } catch (e: FileNotFoundException) {
            null
        }
    }

    override fun getType(uri: Uri): String? {
        throw UnsupportedOperationException()
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException()
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException()
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        throw UnsupportedOperationException()
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?
    ): Int {
        throw UnsupportedOperationException()
    }
}