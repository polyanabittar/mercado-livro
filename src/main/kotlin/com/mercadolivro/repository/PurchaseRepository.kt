package com.mercadolivro.repository

import com.mercadolivro.model.BookModel
import com.mercadolivro.model.PurchaseModel
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface PurchaseRepository: CrudRepository<PurchaseModel, Int> {

    @Query("select p.books from purchase p where p.customer.id = ?1")
    fun findAllByCustomerId(id: Int): List<BookModel>
}
