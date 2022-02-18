package com.github.theapache64.showtodo

import com.github.theapache64.showtodo.core.HtmlGenerator
import com.github.theapache64.showtodo.core.TodoParser
import java.io.File

fun main(args: Array<String>) {
    val currentDir = File(System.getProperty("user.dir"))
    // val currentDir = File("/Users/theapache64/Documents/projects/compose-jb")
    if (!currentDir.resolve(".git").exists()) {
        println("$currentDir is not a git project")
        return
    }

    val todos = TodoParser.parseTodo(projectDir = currentDir)
    if (todos.isNotEmpty()) {
        HtmlGenerator.generateReport(currentDir, todos)
    }
}

