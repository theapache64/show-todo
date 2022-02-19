package com.github.theapache64.showtodo.core

import com.github.theapache64.showtodo.model.Todo
import java.awt.Desktop
import java.io.File
import java.net.URI
import kotlin.io.path.createDirectories
import kotlin.io.path.div


internal const val KEY_PAGE_TITLE = "{{PAGE_TITLE}}"
internal const val KEY_PAGE_CONTENT = "{{PAGE_CONTENT}}"

object HtmlGenerator {
    fun generateReport(projectDir: File, todos: List<Todo>) {
        println("📝 Generating report...")
        // Create directory
        val showTodoDir = projectDir.toPath() / "build" / "show-todo"
        showTodoDir.toFile().deleteRecursively()
        showTodoDir.createDirectories()

        // author map
        val authorMap = todos.groupBy { it.author }
            .toSortedMap(comparator = { author1, author2 ->
                author1.toString().compareTo(author2.toString())
            })

        // Create index
        val indexPagePath = IndexPageGenerator.generatePage(showTodoDir, projectDir, authorMap)

        // Create author pages
        AuthorPageGenerator.generatePage(projectDir, showTodoDir, authorMap)

        val fileUri = "file://${indexPagePath.toFile().absolutePath}"
        println("✅ Report generated: $fileUri")
        Desktop.getDesktop().browse(URI(fileUri))
    }

}