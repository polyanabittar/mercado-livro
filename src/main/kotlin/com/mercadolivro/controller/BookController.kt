package com.mercadolivro.controller

import com.mercadolivro.controller.request.PostBookRequest
import com.mercadolivro.controller.request.PutBookRequest
import com.mercadolivro.controller.response.BookResponse
import com.mercadolivro.extension.toBookModel
import com.mercadolivro.extension.toResponse
import com.mercadolivro.service.BookService
import com.mercadolivro.service.CustomerService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("books")
class BookController(
    val bookService: BookService,
    val customerService: CustomerService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createBook(@Valid @RequestBody book: PostBookRequest) {
        val customer = customerService.findById(book.customerId)
        bookService.createBook(book.toBookModel(customer))
    }

    @GetMapping
    fun findAll(@PageableDefault(page = 0, size = 10) pageable: Pageable): Page<BookResponse> =
        bookService.findAll(pageable).map { it.toResponse() }

    @GetMapping("/actives")
    fun findActives(@PageableDefault(page = 0, size = 10) pageable: Pageable): Page<BookResponse> =
        bookService.findActives(pageable).map { it.toResponse() }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Int): BookResponse =
        bookService.findById(id).toResponse()

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Int) {
        bookService.delete(id)
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun update(@PathVariable id: Int, @RequestBody book: PutBookRequest) {
        val bookSaved = bookService.findById(id)
        bookService.update(book.toBookModel(bookSaved))
    }
}