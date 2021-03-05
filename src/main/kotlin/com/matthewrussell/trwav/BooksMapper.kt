package com.matthewrussell.trwav

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

class BooksMapper {
    private val mapper = ObjectMapper().registerKotlinModule()
    fun toJSON(book: Book): String = mapper.writeValueAsString(book)
    fun fromJSON(json: String): List<Book> = mapper.readValue(json)
}
