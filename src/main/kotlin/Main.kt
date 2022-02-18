import java.io.File
import java.util.Base64
import kotlin.io.path.*


private val todoRegex by lazy {
    "todo|fixme".toRegex(RegexOption.IGNORE_CASE)
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
    val currentDir = File(System.getProperty("user.dir"))

    if (!currentDir.resolve(".git").exists()) {
        println("$currentDir is not a git project")
        return
    }

    val todos = parseTodo(projectDir = currentDir)
    generateHtmlReport(currentDir, todos)

}

fun generateHtmlReport(projectDir: File, todos: List<Todo>) {
    // Create directory
    val showTodoDir = Path(System.getProperty("user.dir")) / "build" / "show-todo"
    showTodoDir.toFile().deleteRecursively()
    showTodoDir.createDirectories()

    // Create index
    val indexHtml = showTodoDir / "index.html"
    val indexContent = getIndexContent(projectDir, todos)
    indexHtml.writeText(indexContent)
}

private const val KEY_PAGE_TITLE = "{{PAGE_TITLE}}"
private const val KEY_PAGE_CONTENT = "{{PAGE_CONTENT}}"

fun getIndexContent(projectDir: File, totalTodos: List<Todo>): String {
    val sb = StringBuilder()
    val authorMap = totalTodos.groupBy { it.author }
    sb.append(
        """
        
        <div class="container">
            <h2>${projectDir.name} - Index</h2>
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
            <td><a href="${hash(author)}.html">View</a></td>
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

    val template = "template.html".readAsResource()
    return template
        .replace(KEY_PAGE_TITLE, "${projectDir.name} | Index | show-todo")
        .replace(KEY_PAGE_CONTENT, sb.toString())
}

fun hash(author: Author): String {
    return Base64.getEncoder().encodeToString(
        author.toString().toByteArray()
    ).replace("\\W+".toRegex(), "")
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
        .filter { file -> !file.isDirectory && !isGitIgnored(projectDir, file) }
        .forEach { file ->
            file.readLines().forEachIndexed { index, line ->
                if (hasTodo(line)) {
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
    return todos
}

/**
 * TODO: Detection should be improved. This implementation only detects if the line has [todoRegex]
 */
fun hasTodo(line: String): Boolean = line.contains(todoRegex)


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

