import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Path
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.writeText


private val todoRegex by lazy {
    "(todo|fixme)".toRegex(RegexOption.IGNORE_CASE)
}

data class Todo(
    val file: File,
    val lineNo: Int,
    val author: Author
)

data class Author(
    val name: String,
    val email: String
)


fun main(args: Array<String>) {
    // val currentDir = File(System.getProperty("user.dir"))
    val currentDir = File("/Users/theapache64/Documents/projects/hotstar/hotstar-android-mobile")
    if (!currentDir.resolve(".git").exists()) {
        println("$currentDir is not a git project")
        return
    }

    val todos = parseTodo(projectDir = currentDir)
    generateHtmlReport(currentDir, todos)
}

fun generateHtmlReport(projectDir: File, todos: List<Todo>) {
    // Create directory
    val showTodoDir = projectDir.toPath() / "build" / "show-todo"
    showTodoDir.toFile().deleteRecursively()
    showTodoDir.createDirectories()

    // Author map
    val authorMap = todos.groupBy { it.author }
        .toSortedMap(comparator = { author1, author2 -> "${author1.name}${author1.email}".compareTo("${author2.name}${author2.email}") })

    // Create index
    val indexHtml = showTodoDir / "index.html"
    val indexContent = getIndexContent(projectDir, authorMap)
    indexHtml.writeText(indexContent)

    // Create author pages
    for ((author, authorTodos) in authorMap) {
        createAuthorPage(showTodoDir, author, authorTodos)
    }
}

fun createAuthorPage(showToDoDir: Path, author: Author, authorTodos: List<Todo>) {
    val authorPage = showToDoDir / hashedHtmlFileName(author)
    authorPage.toFile().delete()
    val authorPageContent = "author_template.html".readAsResource()
        .replace(KEY_PAGE_TITLE, "${author.name} - ${author.email} | show-todo")
        .replace(KEY_PAGE_CONTENT, getAuthorPageContent(author, authorTodos))

    authorPage.writeText(authorPageContent)
}

fun getAuthorPageContent(author: Author, authorTodos: List<Todo>): String {
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
            <pre><code class="language-${getLanguage(todo.file)}">${highlightContent.trim()}</code></pre>
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

fun getLanguage(file: File): String {
    return when (val ext = file.extension) {
        "kt", "kts" -> "kotlin"
        else -> ext
    }
}

fun parseHighlightContent(todo: Todo): String {
    val lines = todo.file.readLines()
    val lineRange = (todo.lineNo - 2).coerceAtLeast(0)..(todo.lineNo + 2).coerceAtMost(lines.size)
    return todo.file
        .readLines()
        .filterIndexed { index, _ -> (index + 1) in lineRange }
        .joinToString(separator = "\n")
}

private const val KEY_PAGE_TITLE = "{{PAGE_TITLE}}"
private const val KEY_PAGE_CONTENT = "{{PAGE_CONTENT}}"

fun getIndexContent(projectDir: File, authorMap: Map<Author, List<Todo>>): String {
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


fun hashedHtmlFileName(author: Author): String {
    return Base64.getEncoder().encodeToString(
        author.toString().toByteArray()
    ).replace("\\W+".toRegex(), "") + ".html"
}

class Main

fun String.readAsResource(): String {
    return Main::class.java.getResource(this).readText()
}

fun isGitIgnored(projectDir: File, file: File): Boolean {
    return executeCommand(
        command = arrayOf(
            "git",
            "-C",
            projectDir.absolutePath,
            "check-ignore",
            file.absolutePath
        ),
        dir = projectDir
    ).isNotBlank()
}

fun parseTodo(projectDir: File): List<Todo> {
    if (projectDir.isFile) error("Directory expected, but ${projectDir.name} is a file.")

    val todos = mutableListOf<Todo>()
    projectDir.walk()
        .onEnter { directory -> !isGitIgnored(projectDir, directory) && directory.name != ".git" }
        .filter { file ->
            !file.isDirectory && !isGitIgnored(projectDir, file) && !isBinaryFile(file)
        }
        .forEach { file ->
            println(file.absolutePath)
            file.readLines().forEachIndexed { index, line ->
                if (hasTodo(line.trim())) {
                    val lineNo = index + 1
                    val author = getAuthor(projectDir, file, lineNo)
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
    println("Done")
    return todos
}

/**
 * Guess whether given file is binary. Just checks for anything under 0x09.
 */
@Throws(FileNotFoundException::class, IOException::class)
fun isBinaryFile(f: File?): Boolean {
    val `in` = FileInputStream(f)
    var size = `in`.available()
    if (size > 1024) size = 1024
    val data = ByteArray(size)
    `in`.read(data)
    `in`.close()
    var ascii = 0
    var other = 0
    for (i in data.indices) {
        val b = data[i]
        if (b < 0x09) return true
        if (b.toInt() == 0x09 || b.toInt() == 0x0A || b.toInt() == 0x0C || b.toInt() == 0x0D) {
            ascii++
        } else {
            if (b in 0x20..0x7E) ascii++ else other++
        }
    }
    return if (other == 0) false else 100 * other / (ascii + other) > 95
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


fun getAuthor(projectDir: File, file: File, lineNo: Int): Author {
    val gitBlameCommand = arrayOf(
        "git",
        "-C",
        projectDir.absolutePath,
        "--no-pager",
        "blame",
        "-L",
        "$lineNo,$lineNo",
        file.absolutePath,
        "--porcelain"
    )
    val result = executeCommand(command = gitBlameCommand, dir = projectDir)
    return "author (.+)\\nauthor-mail <(.+)>".toRegex().find(result)?.groupValues?.let {
        Author(
            name = it[1],
            email = it[2],
        )
    } ?: Author("Unknown", "unknown@email.com")
}

fun executeCommand(command: Array<String>, dir: File): String {
    return ProcessBuilder(*command)
        .directory(File(dir.absolutePath))
        .start()
        .inputStream
        .bufferedReader()
        .readText()
}

