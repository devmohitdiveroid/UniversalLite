package com.diveroid.camera.underfilter

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException

object FileUtils {
    const val TAG = "FileUtils"
    const val OUTPUT_DIR = "HWEncodingExperiments" // Directory relative to External or Internal (fallback) Storage

    /**
     * Returns a Java File initialized to a directory of given name
     * at the root storage location, with preference to external storage.
     * If the directory did not exist, it will be created at the conclusion of this call.
     * If a file with conflicting name exists, this method returns null;
     *
     * @param c the context to determine the internal storage location, if external is unavailable
     * @param directory_name the name of the directory desired at the storage location
     * @return a File pointing to the storage directory, or null if a file with conflicting name
     * exists
     */
    fun getRootStorageDirectory(c: Context, directory_name: String?): File? {
        // First, try getting access to the sdcard partition
        val result: File = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            Log.d(TAG, "Using sdcard")
            File(Environment.getExternalStorageDirectory(), directory_name)
        } else {
            // Else, use the internal storage directory for this application
            Log.d(TAG, "Using internal storage")
            File(c.applicationContext.filesDir, directory_name)
        }
        if (!result.exists()) result.mkdir() else if (result.isFile) {
            return null
        }
        Log.d("getRootStorageDirectory", result.absolutePath)
        return result
    }

    /**
     * Returns a Java File initialized to a directory of given name
     * within the given location.
     *
     * @param parent_directory a File representing the directory in which the new child will reside
     * @return a File pointing to the desired directory, or null if a file with conflicting name
     * exists or if getRootStorageDirectory was not called first
     */
    fun getStorageDirectory(parent_directory: File?, new_child_directory_name: String?): File? {
        val result = File(parent_directory, new_child_directory_name)
        if (!result.exists()) return if (result.mkdir()) result else {
            Log.e("getStorageDirectory", "Error creating " + result.absolutePath)
            null
        } else if (result.isFile) {
            return null
        }
        Log.d("getStorageDirectory", "directory ready: " + result.absolutePath)
        return result
    }

    /**
     * Returns a TempFile with given root, filename, and extension.
     * The resulting TempFile is safe for use with Android's MediaRecorder
     * @param c
     * @param root
     * @param filename
     * @param extension
     * @return
     */
    fun createTempFile(c: Context?, root: File?, filename: String?, extension: String): File? {
        var extension = extension
        var output: File? = null
        return try {
            if (filename != null) {
                if (!extension.contains(".")) extension = ".$extension"
                output = File(root, filename + extension)
                output.createNewFile()
                //output = File.createTempFile(filename, extension, root);
                Log.i(TAG, "Created temp file: " + output.absolutePath)
            }
            output
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun createTempFileInRootAppStorage(c: Context, filename: String): File? {
        val recordingDir = getRootStorageDirectory(c, OUTPUT_DIR)
        return createTempFile(c, recordingDir, filename.split("\\.").toTypedArray()[0], filename.split("\\.").toTypedArray()[1])
    }
}