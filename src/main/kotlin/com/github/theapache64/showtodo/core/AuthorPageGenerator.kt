package com.github.theapache64.showtodo.core

import com.github.theapache64.showtodo.model.Author
import com.github.theapache64.showtodo.model.Todo
import com.github.theapache64.showtodo.util.FileUtils.readAsResource
import java.io.File
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.writeText

internal object AuthorPageGenerator {
    fun generatePage(projectDir: File, showTodoDir: Path, authorMap: Map<Author, List<Todo>>) {
        for ((author, authorTodos) in authorMap) {
            createAuthorPage(projectDir, showTodoDir, author, authorTodos)
        }
    }

    private fun createAuthorPage(projectDir: File, showToDoDir: Path, author: Author, authorTodos: List<Todo>) {
        val authorPage = showToDoDir / hashedHtmlFileName(author)
        authorPage.toFile().delete()
        val authorPageContent = "author_template.html".readAsResource()
            .replace(KEY_PAGE_TITLE, "${author.name} - ${author.email} | show-todo")
            .replace(KEY_PAGE_CONTENT, getAuthorPageContent(projectDir, author, authorTodos))

        authorPage.writeText(authorPageContent)
    }

    private fun getAuthorPageContent(projectDir: File, author: Author, authorTodos: List<Todo>): String {
        val sb = StringBuilder()
        sb.append(
            """
            <div class="container">
            <h2>${author.name} <small>(${author.email})</small> </h2>
        """.trimIndent()
        )
        for (todo in authorTodos) {
            val highlightContent = parseHighlightContent(todo)
            sb.append(
                """
                   <div>
                   <h4>${todo.file.name} <small>${
                    todo.file.absolutePath.replace(
                        projectDir.absolutePath + "/",
                        ""
                    )
                }</small></h4>
                   <pre><code class="language-${getLanguage(todo.file)}">${highlightContent.trim()}</code></pre>
                   </div>
               """.trimIndent()
            )
        }
        sb.append(
            """
            </div>
        """.trimIndent()
        )
        return sb.toString()
    }


    private fun getLanguage(file: File): String {
        return when (val ext = file.extension) {
            "kt", "kts" -> "kotlin"
            else -> ext
        }
    }

    private fun parseHighlightContent(todo: Todo): String {
        val lines = todo.file.readLines()
        val lineRange = (todo.lineNo - 2).coerceAtLeast(0)..(todo.lineNo + 2).coerceAtMost(lines.size)
        return todo.file
            .readLines()
            .filterIndexed { index, _ -> (index + 1) in lineRange }
            .joinToString(separator = "\n")
    }

}