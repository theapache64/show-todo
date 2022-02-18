package com.github.theapache64.showtodo.util

import com.github.theapache64.showtodo.model.Author
import java.io.File

object GitUtils {
    fun isGitIgnored(projectDir: File, file: File): Boolean {
        return executeCommand(
            command = arrayOf(
                "git",
                "-C",
                projectDir.absolutePath,
                "check-ignore",
                file.absolutePath
            ),
            dir = projectDir
        ).isNotBlank()
    }


    fun getAuthor(projectDir: File, file: File, lineNo: Int): Author {
        val gitBlameCommand = arrayOf(
            "git",
            "-C",
            projectDir.absolutePath,
            "--no-pager",
            "blame",
            "-L",
            "$lineNo,$lineNo",
            file.absolutePath,
            "--porcelain"
        )
        val result = executeCommand(command = gitBlameCommand, dir = projectDir)
        return "author (.+)\\nauthor-mail <(.+)>".toRegex().find(result)?.groupValues?.let {
            Author(
                name = it[1],
                email = it[2],
            )
        } ?: Author("Uncommitted (you)", "-")
    }

    private fun executeCommand(command: Array<String>, dir: File): String {
        return ProcessBuilder(*command)
            .directory(File(dir.absolutePath))
            .start()
            .inputStream
            .bufferedReader()
            .readText()
    }

}