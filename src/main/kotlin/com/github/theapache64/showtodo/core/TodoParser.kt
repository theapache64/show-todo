package com.github.theapache64.showtodo.core

import com.github.theapache64.showtodo.model.Todo
import com.github.theapache64.showtodo.util.FileUtils
import com.github.theapache64.showtodo.util.GitUtils
import me.tongfei.progressbar.ProgressBar
import java.io.File

object TodoParser {

    private val todoRegex by lazy {
        "(todo|fixme)".toRegex(RegexOption.IGNORE_CASE)
    }


    fun parseTodo(projectDir: File): List<Todo> {
        if (projectDir.isFile) error("Directory expected, but ${projectDir.name} is a file.")

        val iterator = projectDir.walk()
            .onEnter { directory -> !GitUtils.isGitIgnored(projectDir, directory) && directory.name != ".git" }
            .filter { file ->
                !file.isDirectory && !GitUtils.isGitIgnored(projectDir, file) && !FileUtils.isBinaryFile(file)
            }
            .iterator()

        val todos = mutableListOf<Todo>()
        for (file in ProgressBar.wrap(iterator, "🔍 Analysing...")) {
            file.readLines().forEachIndexed { index, line ->
                if (hasTodo(line.trim())) {
                    val lineNo = index + 1
                    val author = GitUtils.getAuthor(projectDir, file, lineNo)
                    todos.add(
                        Todo(
                            file = file,
                            lineNo = lineNo,
                            author = author
                        )
                    )
                }
            }
        }
        println("👌🏻 Analysis finished: Found ${todos.size} TODO(s)")
        return todos
    }


    /**
     * TODO: Detection should be improved. This implementation only detects if the line has [todoRegex]
     */
    fun hasTodo(line: String): Boolean {
        val word = todoRegex.find(line)?.groupValues?.get(1) ?: return false
        return line.startsWith("*") || line.startsWith("/*") || hasSlashBeforeTodo(word, line)
    }

    fun hasSlashBeforeTodo(word: String, line: String): Boolean {
        if (!line.contains("//")) return false
        val slashIndex = line.lastIndexOf("//")
        val todoIndex = line.lastIndexOf(word)
        return todoIndex > slashIndex
    }

}