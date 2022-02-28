package com.github.theapache64.showtodo.core

import com.github.theapache64.showtodo.Mode
import com.github.theapache64.showtodo.model.Author
import com.github.theapache64.showtodo.model.Line
import java.io.File
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.writeText

internal object IndexPageGenerator {
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
        </head>
        <body>
            {{PAGE_CONTENT}}
        </body>

        </html>
    """.trimIndent()

    fun generatePage(showTodoDir: Path, projectDir: File, authorMap: Map<Author, List<Line>>, mode :Mode): Path {
        val indexHtml = showTodoDir / "index.html"
        val indexContent = getIndexContent(projectDir, authorMap, mode)
        indexHtml.writeText(indexContent)
        return indexHtml
    }

    private fun getIndexContent(projectDir: File, authorMap: Map<Author, List<Line>>, mode : Mode): String {
        val sb = StringBuilder()

        sb.append(
            """
        
        <div class="container">
            <h2>${projectDir.name} - $mode (${authorMap.values.sumOf { it.size }})</h2>
            <table class="table table-bordered">
                <thead>
                <tr>
                    <th>Name</th>
                    <th>Email</th>
                    <th>$mode Count</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                
             

    """.trimIndent()
        )
        for ((author, authorTodos) in authorMap) {
            sb.append(
                """
           <tr>
            <td>${author.name}</td>
            <td>${author.email}</td>
            <td>${authorTodos.size}</td>
            <td><a href="${hashedHtmlFileName(author)}">View</a></td>
           </tr>
        """.trimIndent()
            )
        }

        sb.append(
            """
            </tbody>
            </table>
        </div>
        """.trimIndent()
        )

        return TEMPLATE
            .replace(KEY_PAGE_TITLE, "${projectDir.name} | Index | show-todo")
            .replace(KEY_PAGE_CONTENT, sb.toString())
    }
}