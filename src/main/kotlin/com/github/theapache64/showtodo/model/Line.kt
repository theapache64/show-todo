package com.github.theapache64.showtodo.model

import java.io.File

data class Line(
    val file: File,
    val lineNo: Int,
    val author: Author
)
