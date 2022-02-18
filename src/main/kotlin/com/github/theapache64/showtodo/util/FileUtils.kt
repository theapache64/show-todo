package com.github.theapache64.showtodo.util

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException


object FileUtils {
    /**
     * To check if the given file is binary or not
     */
    @Throws(FileNotFoundException::class, IOException::class)
    fun isBinaryFile(f: File?): Boolean {
        val `in` = FileInputStream(f)
        var size = `in`.available()
        if (size > 1024) size = 1024
        val data = ByteArray(size)
        `in`.read(data)
        `in`.close()
        var ascii = 0
        var other = 0
        for (i in data.indices) {
            val b = data[i]
            if (b < 0x09) return true
            if (b.toInt() == 0x09 || b.toInt() == 0x0A || b.toInt() == 0x0C || b.toInt() == 0x0D) {
                ascii++
            } else {
                if (b in 0x20..0x7E) ascii++ else other++
            }
        }
        return if (other == 0) false else 100 * other / (ascii + other) > 95
    }

    fun String.readAsResource(): String {
        return FileUtils::class.java.classLoader.getResourceAsStream(this).bufferedReader().readText()
    }
}

