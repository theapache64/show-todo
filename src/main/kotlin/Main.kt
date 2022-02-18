import java.io.File


private val todoRegex by lazy {
    "todo|fixme".toRegex(RegexOption.IGNORE_CASE)
}

class Todo(
    val file: File,
    val lineNo: Int,
    val author: String
)

fun main(args: Array<String>) {
    val currentDir = File(System.getProperty("user.dir"))

    if (!currentDir.resolve(".git").exists()) {
        println("$currentDir is not a git project")
        return
    }

    val todos = parseTodo(projectDir = currentDir)

    // Try adding program arguments via Run/Debug configuration
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")
}

fun parseTodo(projectDir: File): List<Todo> {
    if (projectDir.isFile) error("Directory expected, but ${projectDir.name} is a file.")

    val todos = mutableListOf<Todo>()
    projectDir.walk()
        .filter { file -> !file.isDirectory }
        .forEach { file ->
        file.readLines().forEachIndexed { index, line ->
            if (hasTodo(line)) {
                val lineNo = index + 1
                val author = getAuthor(projectDir, file, lineNo)
                println(author)
            }
        }
    }
    return todos
}

/**
 * TODO: Detection should be improved. This implementation only detects if the line has [todoRegex]
 */
fun hasTodo(line: String): Boolean = line.contains(todoRegex)


fun getAuthor(projectDir: File, file: File, lineNo: Int): String {
    val gitBlameCommand =
        "cd '${projectDir.absolutePath}' && git --no-pager blame -L $lineNo,$lineNo '${file.absolutePath}'"
    println(gitBlameCommand)
    return Runtime.getRuntime()
        .exec(gitBlameCommand)
        .inputStream
        .bufferedReader()
        .readText()
}
