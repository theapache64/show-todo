package com.github.theapache64.showtodo.core

import com.github.theapache64.showtodo.Mode
import com.github.theapache64.showtodo.model.Line
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div


internal const val KEY_PAGE_TITLE = "{{PAGE_TITLE}}"
internal const val KEY_PAGE_CONTENT = "{{PAGE_CONTENT}}"

object HtmlGenerator {
    fun generateReport(projectDir: File, lines: List<Line>, mode: Mode) {
        println("📝 Generating report...")
        // Create directory
        val showTodoDir = getReportDir(projectDir)
        showTodoDir.toFile().deleteRecursively()
        showTodoDir.createDirectories()

        // author map
        val authorMap = lines.groupBy { it.author }
            .toSortedMap(comparator = { author1, author2 ->
                author1.toString().compareTo(author2.toString())
            })

        // Create index
        val indexPagePath = IndexPageGenerator.generatePage(showTodoDir, projectDir, authorMap, mode)

        // Create author pages
        AuthorPageGenerator.generatePage(projectDir, showTodoDir, authorMap)

        val fileUri = "file://${indexPagePath.toFile().absolutePath}"
        println("✅ Report generated: $fileUri")
        Desktop.getDesktop().browse(URI(fileUri))
    }

    fun getIndexFile(projectDir: File) : Path{
        return getReportDir(projectDir) / "index.html"
    }

    private fun getReportDir(projectDir: File) = projectDir.toPath() / "build" / "show-todo"

}