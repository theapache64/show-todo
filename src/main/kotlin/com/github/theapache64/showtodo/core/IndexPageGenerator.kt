package com.github.theapache64.showtodo.core

import com.github.theapache64.showtodo.model.Author
import com.github.theapache64.showtodo.model.Todo
import com.github.theapache64.showtodo.util.FileUtils.readAsResource
import java.io.File
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.writeText

internal object IndexPageGenerator {

    fun generatePage(showTodoDir: Path, projectDir: File, authorMap: Map<Author, List<Todo>>) {
        val indexHtml = showTodoDir / "index.html"
        val indexContent = getIndexContent(projectDir, authorMap)
        indexHtml.writeText(indexContent)
    }

    private fun getIndexContent(projectDir: File, authorMap: Map<Author, List<Todo>>): String {
        val sb = StringBuilder()

        sb.append(
            """
        
        <div class="container">
            <h2>${projectDir.name} - TODOs (${authorMap.values.sumOf { it.size }})</h2>
            <table class="table table-bordered">
                <thead>
                <tr>
                    <th>Name</th>
                    <th>Email</th>
                    <th>TODO Count</th>
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

        val template = "index_template.html".readAsResource()
        return template
            .replace(KEY_PAGE_TITLE, "${projectDir.name} | Index | show-todo")
            .replace(KEY_PAGE_CONTENT, sb.toString())
    }

}