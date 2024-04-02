package com.mercadolivro.service

import com.mercadolivro.enum.CustomerStatus
import com.mercadolivro.enum.Roles
import com.mercadolivro.exception.NotFoundException
import com.mercadolivro.model.CustomerModel
import com.mercadolivro.repository.CustomerRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.UUID
import java.util.Random
import java.util.Optional

@ExtendWith(MockKExtension::class)
class CustomerServiceTest {
    @InjectMockKs
    private lateinit var customerService: CustomerService

    @MockK
    private lateinit var customerRepository: CustomerRepository

    @MockK
    private lateinit var bookService: BookService

    @MockK
    private lateinit var bCrypt: BCryptPasswordEncoder

    @Test
    fun `should return all customers`() {
        val fakeCustomers = PageImpl(listOf(buildCustomers(), buildCustomers()))
        val pageRequest: PageRequest = PageRequest.of(0, 10)

        every { customerRepository.findAll(any(PageRequest::class)) } returns fakeCustomers

        val customers = customerService.getAll(null, pageRequest)

        assertEquals(fakeCustomers, customers)

        verify(exactly = 1) { customerRepository.findAll(any(PageRequest::class)) }
        verify(exactly = 0) { customerRepository.findByNameContaining(any(), any()) }
    }

    @Test
    fun `should return customers when name is informed`() {
        val name = UUID.randomUUID().toString()
        val fakeCustomers = PageImpl(listOf(buildCustomers(), buildCustomers()))
        val pageRequest: PageRequest = PageRequest.of(0, 10)

        every {
            customerRepository.findByNameContaining(name, any(PageRequest::class))
        } returns fakeCustomers

        val customers = customerService.getAll(name, pageRequest)

        assertEquals(fakeCustomers, customers)

        verify(exactly = 0) { customerRepository.findAll(any(PageRequest::class)) }
        verify(exactly = 1) { customerRepository.findByNameContaining(name, any()) }
    }

    @Test
    fun `should create customer and encrypt password`() {
        val initialPassword = Math.random().toString()
        val fakePassword = UUID.randomUUID().toString()
        val fakeCustomer = buildCustomers(password = initialPassword)
        val fakeCustomerEncrypted = fakeCustomer.copy(password = fakePassword)

        every { bCrypt.encode(initialPassword) } returns fakePassword
        every { customerRepository.save(fakeCustomerEncrypted) } returns fakeCustomer

        customerService.createCustomer(fakeCustomer)

        verify(exactly = 1) { bCrypt.encode(initialPassword) }
        verify(exactly = 1) { customerRepository.save(fakeCustomerEncrypted) }
    }

    @Test
    fun `should find customer by id`() {
        val id = Random().nextInt()
        val fakeCustomer = buildCustomers(id = id)

        every { customerRepository.findById(id) } returns Optional.of(fakeCustomer)

        val customer = customerService.findById(id)
        assertEquals(fakeCustomer, customer)

        verify(exactly = 1) { customerRepository.findById(id) }
    }

    @Test
    fun `should fail find customer by id`() {
        val id = Random().nextInt()

        every { customerRepository.findById(id) } returns Optional.empty()

        val error = assertThrows<NotFoundException> {
            customerService.findById(id)
        }

        assertEquals("Customer [$id] not exists", error.message)
        assertEquals("ML-201", error.errorCode)

        verify(exactly = 1) { customerRepository.findById(id) }
    }

    private fun buildCustomers(
        id: Int? = null,
        name: String = "customer name",
        email: String = "${UUID.randomUUID()}@email.com",
        password: String = "password"
    ) = CustomerModel (
        id = id,
        name = name,
        email = email,
        status = CustomerStatus.ACTIVE,
        password = password,
        roles = setOf(Roles.CUSTOMER)
    )
}