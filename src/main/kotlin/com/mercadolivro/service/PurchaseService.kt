package com.mercadolivro.service

import com.mercadolivro.enum.BookStatus
import com.mercadolivro.enum.Errors
import com.mercadolivro.events.PurchaseEvent
import com.mercadolivro.exception.BadRequestException
import com.mercadolivro.model.BookModel
import com.mercadolivro.model.PurchaseModel
import com.mercadolivro.repository.PurchaseRepository
import jakarta.transaction.Transactional
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class PurchaseService(
    private val purchaseRepository: PurchaseRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    @Transactional
    fun create(purchaseModel: PurchaseModel) {
        val inactiveBooks = purchaseModel.books.filter { it.status != BookStatus.ATIVO }

        if(inactiveBooks.isNotEmpty()) {
            throw BadRequestException(Errors.ML103.message, Errors.ML103.code)
        }
        
        println("Disparando evento de compra")
        applicationEventPublisher.publishEvent(PurchaseEvent(this, purchaseModel))
        println("Finalizando processamento")
    }

    fun update(purchaseModel: PurchaseModel) {
        purchaseRepository.save(purchaseModel)
    }

    fun getPurchasedBooks(id: Int): List<BookModel> {
        val books = purchaseRepository.findAllByCustomerId(id)
        return books.filter { it.status == BookStatus.VENDIDO }
    }

    fun getSoldBooks(id: Int): List<BookModel> {
        val books = purchaseRepository.findAllByBookCustomerId(id)
        return books.filter { it.status == BookStatus.VENDIDO }
    }
}
