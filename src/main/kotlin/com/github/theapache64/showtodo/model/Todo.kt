package com.github.theapache64.showtodo.model

import java.io.File

data class Todo(
    val file: File,
    val lineNo: Int,
    val author: Author
)
