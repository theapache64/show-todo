package com.github.theapache64.showtodo.core

import com.github.theapache64.showtodo.model.Author
import com.github.theapache64.showtodo.model.Todo
import java.io.File
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.writeText

internal object AuthorPageGenerator {
    private val TEMPLATE = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <title>{{PAGE_TITLE}}</title>

            <meta name="viewport" content="width=device-width, initial-scale=1">
            <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
            <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
            <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
            <link rel="stylesheet"
                  href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.4.0/styles/default.min.css">
            <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.4.0/highlight.min.js"></script>
            <script>hljs.highlightAll();</script>
        </head>
        <body>
            {{PAGE_CONTENT}}
        </body>

        </html>
    """.trimIndent()

    fun generatePage(projectDir: File, showTodoDir: Path, authorMap: Map<Author, List<Todo>>) {
        for ((author, authorTodos) in authorMap) {
            createAuthorPage(projectDir, showTodoDir, author, authorTodos)
        }
    }

    private fun createAuthorPage(projectDir: File, showToDoDir: Path, author: Author, authorTodos: List<Todo>) {
        val authorPage = showToDoDir / hashedHtmlFileName(author)
        authorPage.toFile().delete()
        val authorPageContent = TEMPLATE
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