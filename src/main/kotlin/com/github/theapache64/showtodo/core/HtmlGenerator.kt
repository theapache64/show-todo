package com.github.theapache64.showtodo.core

import com.github.theapache64.showtodo.model.Todo
import java.io.File
import kotlin.io.path.createDirectories
import kotlin.io.path.div


internal const val KEY_PAGE_TITLE = "{{PAGE_TITLE}}"
internal const val KEY_PAGE_CONTENT = "{{PAGE_CONTENT}}"

object HtmlGenerator {
    fun generateReport(projectDir: File, todos: List<Todo>) {
        // Create directory
        val showTodoDir = projectDir.toPath() / "build" / "show-todo"
        showTodoDir.toFile().deleteRecursively()
        showTodoDir.createDirectories()

        // com.github.theapache64.showtodo.Author map
        val authorMap = todos.groupBy { it.author }
            .toSortedMap(comparator = { author1, author2 -> "${author1.name}${author1.email}".compareTo("${author2.name}${author2.email}") })

        // Create index
        IndexPageGenerator.generatePage(showTodoDir, projectDir, authorMap)

        // Create author pages
        AuthorPageGenerator.generatePage(showTodoDir, authorMap)
    }

}