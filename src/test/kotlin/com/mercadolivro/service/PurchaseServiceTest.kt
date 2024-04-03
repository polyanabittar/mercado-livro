package com.mercadolivro.service

import com.mercadolivro.enum.BookStatus
import com.mercadolivro.events.PurchaseEvent
import com.mercadolivro.exception.BadRequestException
import com.mercadolivro.helper.buildBook
import com.mercadolivro.helper.buildCustomers
import com.mercadolivro.helper.buildPurchase
import com.mercadolivro.repository.PurchaseRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.ApplicationEventPublisher
import kotlin.random.Random

@ExtendWith(MockKExtension::class)
class PurchaseServiceTest {

    @InjectMockKs
    private lateinit var purchaseService: PurchaseService

    @MockK
    private lateinit var purchaseRepository: PurchaseRepository

    @MockK
    private lateinit var applicationEventPublisher: ApplicationEventPublisher

    val publishEventSlot = slot<PurchaseEvent>()

    @Test
    fun `should create and publish purchase`() {
        val purchase = buildPurchase()

        every { applicationEventPublisher.publishEvent(any()) } just runs

        purchaseService.create(purchase)

        verify(exactly = 1) { applicationEventPublisher.publishEvent(capture(publishEventSlot)) }

        assertEquals(purchase, publishEventSlot.captured.purchaseModel)
    }

    @Test
    fun `should not create and publish purchase when status inactive`() {
        val book = buildBook(status = BookStatus.CANCELADO)
        val purchase = buildPurchase(books = mutableListOf(book))

        val error = assertThrows<BadRequestException> {
            purchaseService.create(purchase)
        }

        assertEquals("Cannot buy book with inactive status", error.message)
        assertEquals("ML-103", error.errorCode)

        verify(exactly = 0) { applicationEventPublisher.publishEvent(any()) }
    }

    @Test
    fun `should update customer`() {
        val purchase = buildPurchase()

        every { purchaseRepository.save(purchase) } returns purchase

        purchaseService.update(purchase)

        verify(exactly = 1) { purchaseRepository.save(purchase) }
    }

    @Test
    fun `should get purchased books by customer`() {
        val id = Random.nextInt()
        val customer = buildCustomers(id = id)
        val books = mutableListOf(buildBook(customer = customer, status = BookStatus.VENDIDO))

        every { purchaseRepository.findAllByCustomerId(id) } returns books

        val purchasedBooks = purchaseService.getPurchasedBooks(id)

        assertEquals(books, purchasedBooks)

        verify(exactly = 1) { purchaseRepository.findAllByCustomerId(id) }
    }

    @Test
    fun `should get sold books by customer`() {
        val id = Random.nextInt()
        val customer = buildCustomers(id = id)
        val books = mutableListOf(buildBook(customer = customer, status = BookStatus.VENDIDO))

        every { purchaseRepository.findAllByBookCustomerId(id) } returns books

        val purchasedBooks = purchaseService.getSoldBooks(id)

        assertEquals(books, purchasedBooks)

        verify(exactly = 1) { purchaseRepository.findAllByBookCustomerId(id) }
    }
}