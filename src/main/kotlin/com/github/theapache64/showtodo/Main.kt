package com.github.theapache64.showtodo

import com.github.theapache64.showtodo.core.HtmlGenerator
import com.github.theapache64.showtodo.core.TodoParser
import java.awt.Desktop
import java.io.File
import java.net.URI
import kotlin.io.path.absolutePathString

enum class Mode {
    TODO,
    DOUBLE_BANG,
    DOUBLE_BANG_NO_TESTS
}

fun main(args: Array<String>) {

    val currentDir = File(System.getProperty("user.dir"))
    println("🗂 Project : ${currentDir.absolutePath}")
    // val currentDir = File("/Users/theapache64/Documents/projects/compose-jb")
    if (!currentDir.resolve(".git").exists()) {
        println("$currentDir is not a git project")
        return
    }

    val mode = when {
        args.contains("bang") -> Mode.DOUBLE_BANG
        args.contains("bang-no-tests") -> Mode.DOUBLE_BANG_NO_TESTS
        else -> Mode.TODO
    }
    println("➡️ Mode: $mode")

    val isOpen = args.contains("open")
    if (isOpen) {
        // Open index file in browser
        Desktop.getDesktop().browse(
            URI(
                "file://${HtmlGenerator.getIndexFile(currentDir).absolutePathString()}"
            )
        )
    } else {


        val todos = TodoParser.parseTodo(projectDir = currentDir, mode = mode)
        if (todos.isNotEmpty()) {
            HtmlGenerator.generateReport(currentDir, todos, mode)
        }
    }
}

