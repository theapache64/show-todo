package com.github.theapache64.showtodo.core

import com.github.theapache64.showtodo.model.Author
import java.io.File
import java.util.*




fun hashedHtmlFileName(author: Author): String {
    return Base64.getEncoder().encodeToString(
        author.toString().toByteArray()
    ).replace("\\W+".toRegex(), "") + ".html"
}
